package com.tricore.crmtoyota.client;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class MockMlForecastClient {

    public MlForecastClient.MlBatchResult generateCalibratedForecast(List<MlForecastClient.PartDemandInput> inputs) {
        List<MlForecastClient.PartPredictionItem> items = new ArrayList<>();
        for (MlForecastClient.PartDemandInput in : inputs) {
            double categoryFactor = switch (in.category().toLowerCase()) {
                case "filtros" -> 4.2;
                case "aceites" -> 3.6;
                case "frenos" -> 3.0;
                case "encendido" -> 2.8;
                default -> 2.2;
            };
            double baseMonthly = Math.max(in.minStock() * categoryFactor, in.recentDispatchedUnits() * 1.5 + 8.0);
            BigDecimal predicted = BigDecimal.valueOf(baseMonthly).setScale(2, RoundingMode.HALF_UP);
            int reorderPoint = Math.max(in.minStock() + 1, (int) Math.ceil(baseMonthly * 0.42));
            int targetStock = (int) Math.ceil(baseMonthly * 1.15);
            int suggestedOrder = in.currentStock() <= reorderPoint ? Math.max(0, targetStock - in.currentStock()) : 0;
            BigDecimal adi = BigDecimal.valueOf(1.34 + ((in.partId() % 5) * 0.09)).setScale(2, RoundingMode.HALF_UP);

            items.add(new MlForecastClient.PartPredictionItem(
                    in.partId(),
                    in.sku(),
                    predicted,
                    reorderPoint,
                    suggestedOrder,
                    adi
            ));
        }

        return new MlForecastClient.MlBatchResult(
                "rf-toyota-mock-v1.0",
                "Próximos 30 días",
                new BigDecimal("0.8640"),
                new BigDecimal("0.1120"),
                new BigDecimal("1.3800"),
                new BigDecimal("0.1190"),
                new BigDecimal("1.54"),
                "MOCK_FALLBACK",
                items
        );
    }
}
