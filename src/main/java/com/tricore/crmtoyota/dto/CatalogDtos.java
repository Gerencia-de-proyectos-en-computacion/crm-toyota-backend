package com.tricore.crmtoyota.dto;

import com.tricore.crmtoyota.domain.enums.PartBrand;
import com.tricore.crmtoyota.domain.enums.StockAlertLevel;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class CatalogDtos {
    private CatalogDtos() {}

    public record CompatibilitySpec(
            @NotBlank String model,
            @NotNull @Min(1990) Integer yearFrom,
            @NotNull @Max(2030) Integer yearTo,
            String trimVersion
    ) {}

    public record PartRequest(
            @NotBlank @Size(max = 40) String sku,
            @NotBlank @Size(max = 40) String oemReferenceCode,
            @NotNull PartBrand brand,
            @NotBlank @Size(max = 160) String name,
            @NotBlank @Size(max = 40) String category,
            @NotBlank @Size(max = 140) String compatibleSummary,
            @NotNull @DecimalMin("0.01") BigDecimal salePriceIgv,
            @NotNull @PositiveOrZero Integer currentStock,
            @NotNull @PositiveOrZero Integer minStock,
            List<CompatibilitySpec> compatibilities
    ) {}

    public record EquivalentPartOption(
            Long partId,
            String sku,
            String oemReferenceCode,
            PartBrand brand,
            String name,
            BigDecimal salePriceIgv,
            int currentStock,
            StockAlertLevel alertLevel
    ) {}

    public record PartResponse(
            Long id,
            String sku,
            String oemReferenceCode,
            PartBrand brand,
            String name,
            String category,
            String compatibleSummary,
            BigDecimal salePriceIgv,
            int currentStock,
            int minStock,
            Integer reorderPoint,
            StockAlertLevel alertLevel,
            List<EquivalentPartOption> availableEquivalents
    ) {}

    public record LaborRateRequest(
            @NotBlank @Size(max = 40) String code,
            @NotBlank @Size(max = 160) String serviceName,
            @NotNull @DecimalMin("0.10") BigDecimal estimatedHours,
            Integer intervalKm,
            @NotNull @DecimalMin("1.00") BigDecimal priceIgv
    ) {}

    public record LaborRateResponse(
            Long id,
            String code,
            String serviceName,
            BigDecimal estimatedHours,
            Integer intervalKm,
            BigDecimal priceIgv
    ) {}

    public record KitPartLine(
            Long partId,
            String sku,
            String oemReferenceCode,
            PartBrand brand,
            String name,
            int quantity,
            BigDecimal unitPriceIgv,
            int currentStock,
            StockAlertLevel alertLevel
    ) {}

    public record MaintenanceKitResponse(
            Long id,
            String code,
            String name,
            String model,
            int intervalKm,
            LaborRateResponse laborRate,
            List<KitPartLine> parts,
            BigDecimal estimatedKitTotalIgv
    ) {}

    public record StockReplenishmentRequest(
            @NotNull(message = "El repuesto es obligatorio")
            Long partId,

            @NotNull @Min(value = 1, message = "La cantidad ingresada debe ser mayor a cero")
            Integer quantityAdded,

            @NotBlank(message = "El proveedor es obligatorio")
            @Size(max = 120)
            String supplierName,

            @NotBlank(message = "El documento de referencia (guía/factura) es obligatorio")
            @Size(max = 60)
            String referenceDoc,

            @DecimalMin("0.01")
            BigDecimal unitCost
    ) {}

    public record InventoryMovementResponse(
            Long id,
            Long partId,
            String sku,
            String partName,
            String movementType,
            int quantityDelta,
            int stockAfter,
            String supplierName,
            String referenceDoc,
            BigDecimal unitCost,
            String performedBy,
            OffsetDateTime createdAt
    ) {}
}
