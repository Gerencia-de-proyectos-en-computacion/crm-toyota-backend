package com.tricore.crmtoyota.dto;

import com.tricore.crmtoyota.domain.enums.PartBrand;
import com.tricore.crmtoyota.domain.enums.QuoteItemType;
import com.tricore.crmtoyota.domain.enums.QuoteStatus;
import com.tricore.crmtoyota.domain.enums.RejectionReason;
import com.tricore.crmtoyota.dto.CatalogDtos.EquivalentPartOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public final class QuoteDtos {
    private QuoteDtos() {}

    public record QuoteItemRequest(
            @NotNull QuoteItemType itemType,
            Long partId,
            Long laborRateId,
            @NotNull @Min(1) Integer quantity
    ) {}

    public record QuoteUpsertRequest(
            @NotNull(message = "El cliente es obligatorio")
            Long customerId,

            @NotNull(message = "El vehículo Toyota es obligatorio")
            Long vehicleId,

            @NotNull @PositiveOrZero
            Integer mileageAtQuote,

            @DecimalMin("0.00") @DecimalMax("15.00")
            BigDecimal discountPercent,

            @Size(max = 300)
            String notes,

            @NotEmpty(message = "La cotización debe incluir al menos un repuesto o servicio de mano de obra")
            @Valid
            List<QuoteItemRequest> items
    ) {}

    public record QuoteRejectRequest(
            @NotNull(message = "El motivo de rechazo es obligatorio")
            RejectionReason rejectionReason,

            @Size(max = 300)
            String rejectionNotes
    ) {}

    public record SubstitutePartRequest(
            @NotNull Long missingPartId,
            @NotNull Long replacementPartId,
            boolean autoApproveAfterSubstitute
    ) {}

    public record QuoteItemResponse(
            Long id,
            QuoteItemType itemType,
            Long partId,
            Long laborRateId,
            String itemCode,
            String description,
            int quantity,
            BigDecimal unitPriceIgv,
            BigDecimal lineTotalIgv,
            Integer availableStock
    ) {}

    public record QuoteResponse(
            Long id,
            String quoteNumber,
            QuoteStatus status,
            RejectionReason rejectionReason,
            String rejectionNotes,
            Long customerId,
            String customerName,
            String customerDocumentType,
            String customerDocumentNumber,
            String customerPhone,
            Long vehicleId,
            String licensePlate,
            String vehicleSummary,
            int mileageAtQuote,
            BigDecimal grossTotalIgv,
            BigDecimal discountPercent,
            BigDecimal discountAmount,
            BigDecimal taxableSubtotal,
            BigDecimal igvAmount,
            BigDecimal finalTotalIgv,
            LocalDate validUntil,
            String notes,
            String createdByName,
            String whatsappUrl,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime closedAt,
            List<QuoteItemResponse> items
    ) {}

    public record StockConflictDetail(
            Long partId,
            String sku,
            String oemReferenceCode,
            String partName,
            PartBrand brand,
            int requestedQty,
            int availableQty,
            List<EquivalentPartOption> compatibleEquivalents
    ) {}
}
