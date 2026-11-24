package com.tricore.crmtoyota.exception;

import com.tricore.crmtoyota.dto.QuoteDtos.StockConflictDetail;
import java.util.List;

public class InsufficientStockException extends RuntimeException {
    private final String quoteNumber;
    private final List<StockConflictDetail> conflicts;

    public InsufficientStockException(String quoteNumber, List<StockConflictDetail> conflicts) {
        super("No se puede aprobar la proforma " + quoteNumber + ": stock físico insuficiente en almacén.");
        this.quoteNumber = quoteNumber;
        this.conflicts = conflicts;
    }

    public String getQuoteNumber() {
        return quoteNumber;
    }

    public List<StockConflictDetail> getConflicts() {
        return conflicts;
    }
}
