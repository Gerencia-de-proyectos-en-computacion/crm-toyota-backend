package com.tricore.crmtoyota.dto;

import com.tricore.crmtoyota.domain.enums.PartBrand;
import com.tricore.crmtoyota.domain.enums.StockAlertLevel;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class ForecastDtos {
    private ForecastDtos() {}

    public record ModelHealthMetrics(
            String modelVersion,
            String horizonLabel,
            BigDecimal r2,
            BigDecimal wapePercent,
            BigDecimal rmse,
            BigDecimal mapeNonzeroPercent,
            BigDecimal adi,
            String executionSource,
            OffsetDateTime calculatedAt
    ) {}

    public record LostSalesSummary(
            BigDecimal lostAmountLackOfStockSoles,
            long rejectedQuotesLackOfStockCount,
            long totalRejectedQuotesCount
    ) {}

    public record ConversionSummary(
            BigDecimal conversionRatePercent,
            long approvedOrClosedCount,
            long totalDecidedOrActiveCount
    ) {}

    public record RestockAlertRow(
            Long partId,
            String sku,
            String oemReferenceCode,
            String partName,
            PartBrand brand,
            String compatibleModels,
            int currentStock,
            int minStock,
            BigDecimal predictedDemand,
            int reorderPoint,
            int suggestedOrderQty,
            StockAlertLevel alertLevel
    ) {}

    public record ForecastDashboardResponse(
            ModelHealthMetrics modelHealth,
            long criticalCount,
            long warningCount,
            LostSalesSummary lostSales,
            ConversionSummary conversion,
            List<RestockAlertRow> alerts
    ) {}
}
