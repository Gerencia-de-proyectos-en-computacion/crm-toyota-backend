package com.tricore.crmtoyota.service;

import com.tricore.crmtoyota.client.MlForecastClient;
import com.tricore.crmtoyota.client.MlForecastClient.MlBatchResult;
import com.tricore.crmtoyota.client.MlForecastClient.PartDemandInput;
import com.tricore.crmtoyota.client.MlForecastClient.PartPredictionItem;
import com.tricore.crmtoyota.domain.entity.*;
import com.tricore.crmtoyota.domain.enums.MovementType;
import com.tricore.crmtoyota.domain.enums.QuoteStatus;
import com.tricore.crmtoyota.domain.enums.RejectionReason;
import com.tricore.crmtoyota.domain.enums.StockAlertLevel;
import com.tricore.crmtoyota.dto.ForecastDtos.*;
import com.tricore.crmtoyota.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ForecastDashboardService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final PartRepository partRepository;
    private final DemandForecastRepository forecastRepository;
    private final MlModelRunRepository modelRunRepository;
    private final QuoteRepository quoteRepository;
    private final InventoryMovementRepository movementRepository;
    private final MlForecastClient mlForecastClient;

    public ForecastDashboardService(
            PartRepository partRepository,
            DemandForecastRepository forecastRepository,
            MlModelRunRepository modelRunRepository,
            QuoteRepository quoteRepository,
            InventoryMovementRepository movementRepository,
            MlForecastClient mlForecastClient
    ) {
        this.partRepository = partRepository;
        this.forecastRepository = forecastRepository;
        this.modelRunRepository = modelRunRepository;
        this.quoteRepository = quoteRepository;
        this.movementRepository = movementRepository;
        this.mlForecastClient = mlForecastClient;
    }

    @Transactional(readOnly = true)
    public ForecastDashboardResponse getDashboard() {
        ModelHealthMetrics health = modelRunRepository.findTopByOrderByCalculatedAtDesc()
                .map(this::toHealthMetrics)
                .orElseGet(() -> new ModelHealthMetrics(
                        "rf-toyota-pending",
                        "Próximos 30 días",
                        new BigDecimal("0.8500"),
                        new BigDecimal("12.00"),
                        new BigDecimal("1.5000"),
                        new BigDecimal("12.50"),
                        new BigDecimal("1.45"),
                        "DEFAULT",
                        OffsetDateTime.now()
                ));

        Map<Long, DemandForecast> forecastByPart = new HashMap<>();
        for (DemandForecast df : forecastRepository.findAll()) {
            forecastByPart.put(df.getPart().getId(), df);
        }

        List<RestockAlertRow> rows = new ArrayList<>();
        long criticalCount = 0;
        long warningCount = 0;

        for (Part part : partRepository.findByActiveTrueOrderByCategoryAscNameAsc()) {
            DemandForecast df = forecastByPart.get(part.getId());
            Integer rop = df != null ? df.getReorderPoint() : part.getMinStock() * 2;
            BigDecimal pred = df != null ? df.getPredictedDemand() : BigDecimal.valueOf(rop * 2L);
            int suggested = df != null
                    ? df.getSuggestedOrderQty()
                    : Math.max(0, rop + part.getMinStock() - part.getCurrentStock());

            StockAlertLevel level = StockSemaphorePolicy.evaluate(part.getCurrentStock(), part.getMinStock(), rop);
            if (level == StockAlertLevel.CRITICO) {
                criticalCount++;
            } else if (level == StockAlertLevel.ALERTA) {
                warningCount++;
            }

            rows.add(new RestockAlertRow(
                    part.getId(),
                    part.getSku(),
                    part.getOemReferenceCode(),
                    part.getName(),
                    part.getBrand(),
                    part.getCompatibleSummary(),
                    part.getCurrentStock(),
                    part.getMinStock(),
                    pred,
                    rop,
                    suggested,
                    level
            ));
        }

        rows.sort(Comparator.comparingInt((RestockAlertRow r) -> priorityOrder(r.alertLevel()))
                .thenComparingInt(RestockAlertRow::currentStock));

        List<Quote> allQuotes = quoteRepository.findAll();
        BigDecimal lostStockAmount = BigDecimal.ZERO;
        long lostStockCount = 0;
        long totalRejected = 0;
        long convertedCount = 0;

        for (Quote q : allQuotes) {
            if (q.getStatus() == QuoteStatus.RECHAZADA) {
                totalRejected++;
                if (q.getRejectionReason() == RejectionReason.FALTA_STOCK) {
                    lostStockCount++;
                    lostStockAmount = lostStockAmount.add(q.getFinalTotalIgv());
                }
            } else if (q.getStatus() == QuoteStatus.APROBADA || q.getStatus() == QuoteStatus.CERRADA) {
                convertedCount++;
            }
        }

        long totalQuotes = allQuotes.size();
        BigDecimal conversionRate = totalQuotes > 0
                ? BigDecimal.valueOf(convertedCount * 100.0 / totalQuotes).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new ForecastDashboardResponse(
                health,
                criticalCount,
                warningCount,
                new LostSalesSummary(lostStockAmount.setScale(2, RoundingMode.HALF_UP), lostStockCount, totalRejected),
                new ConversionSummary(conversionRate, convertedCount, totalQuotes),
                rows
        );
    }

    @Transactional
    public ForecastDashboardResponse recalculateBatchForecast() {
        Map<Long, Integer> dispatchedByPart = new HashMap<>();
        for (InventoryMovement mov : movementRepository.findAll()) {
            if (mov.getMovementType() == MovementType.SALIDA_COTIZACION) {
                dispatchedByPart.merge(mov.getPart().getId(), Math.abs(mov.getQuantityDelta()), Integer::sum);
            }
        }

        List<Part> activeParts = partRepository.findByActiveTrueOrderByCategoryAscNameAsc();
        Map<Long, Part> partById = new HashMap<>();
        List<PartDemandInput> inputs = new ArrayList<>();
        for (Part p : activeParts) {
            partById.put(p.getId(), p);
            inputs.add(new PartDemandInput(
                    p.getId(),
                    p.getSku(),
                    p.getOemReferenceCode(),
                    p.getCategory(),
                    p.getCurrentStock(),
                    p.getMinStock(),
                    dispatchedByPart.getOrDefault(p.getId(), 0)
            ));
        }

        MlBatchResult batchResult = mlForecastClient.predictBatch(inputs);
        OffsetDateTime now = OffsetDateTime.now();

        MlModelRun run = new MlModelRun();
        run.setModelVersion(batchResult.modelVersion());
        run.setHorizonLabel(batchResult.horizonLabel());
        run.setR2(batchResult.r2());
        run.setWape(batchResult.wape());
        run.setRmse(batchResult.rmse());
        run.setMapeNonzero(batchResult.mapeNonzero());
        run.setAdi(batchResult.adi());
        run.setExecutionSource(batchResult.executionSource());
        run.setCalculatedAt(now);
        MlModelRun savedRun = modelRunRepository.save(run);

        for (PartPredictionItem item : batchResult.predictions()) {
            Part part = partById.get(item.partId());
            if (part == null) {
                continue;
            }
            DemandForecast df = forecastRepository.findByPartId(part.getId()).orElseGet(() -> {
                DemandForecast created = new DemandForecast();
                created.setPart(part);
                return created;
            });
            df.setRun(savedRun);
            df.setPredictedDemand(item.predictedDemand());
            df.setReorderPoint(item.reorderPoint());
            df.setSuggestedOrderQty(item.suggestedOrderQty());
            df.setAdi(item.adi());
            df.setCalculatedAt(now);
            forecastRepository.save(df);
        }

        return getDashboard();
    }

    private int priorityOrder(StockAlertLevel level) {
        return switch (level) {
            case CRITICO -> 0;
            case ALERTA -> 1;
            case NORMAL -> 2;
        };
    }

    private ModelHealthMetrics toHealthMetrics(MlModelRun r) {
        return new ModelHealthMetrics(
                r.getModelVersion(),
                r.getHorizonLabel(),
                r.getR2().setScale(2, RoundingMode.HALF_UP),
                r.getWape().multiply(ONE_HUNDRED).setScale(1, RoundingMode.HALF_UP),
                r.getRmse().setScale(2, RoundingMode.HALF_UP),
                r.getMapeNonzero().multiply(ONE_HUNDRED).setScale(1, RoundingMode.HALF_UP),
                r.getAdi(),
                r.getExecutionSource(),
                r.getCalculatedAt()
        );
    }
}
