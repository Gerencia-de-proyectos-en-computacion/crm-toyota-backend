package com.tricore.crmtoyota.service;

import com.tricore.crmtoyota.domain.enums.StockAlertLevel;

public final class StockSemaphorePolicy {

    private StockSemaphorePolicy() {}

    public static StockAlertLevel evaluate(int currentStock, int minStock, Integer reorderPoint) {
        int halfRop = reorderPoint != null ? (int) Math.ceil(reorderPoint * 0.5) : 0;
        if (currentStock <= 0 || currentStock <= minStock || (reorderPoint != null && currentStock <= halfRop)) {
            return StockAlertLevel.CRITICO;
        }
        int effectiveAlertThreshold = Math.max(minStock * 2, reorderPoint != null ? reorderPoint : 0);
        if (currentStock <= effectiveAlertThreshold) {
            return StockAlertLevel.ALERTA;
        }
        return StockAlertLevel.NORMAL;
    }
}
