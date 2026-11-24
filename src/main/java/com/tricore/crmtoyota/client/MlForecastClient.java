package com.tricore.crmtoyota.client;

import java.math.BigDecimal;
import java.util.List;

public interface MlForecastClient {

    MlBatchResult predictBatch(List<PartDemandInput> inputs);

    record PartDemandInput(
            Long partId,
            String sku,
            String oemReferenceCode,
            String category,
            int currentStock,
            int minStock,
            int recentDispatchedUnits
    ) {}

    record PartPredictionItem(
            Long partId,
            String sku,
            BigDecimal predictedDemand,
            int reorderPoint,
            int suggestedOrderQty,
            BigDecimal adi
    ) {}

    record MlBatchResult(
            String modelVersion,
            String horizonLabel,
            BigDecimal r2,
            BigDecimal wape,
            BigDecimal rmse,
            BigDecimal mapeNonzero,
            BigDecimal adi,
            String executionSource,
            List<PartPredictionItem> predictions
    ) {}
}
