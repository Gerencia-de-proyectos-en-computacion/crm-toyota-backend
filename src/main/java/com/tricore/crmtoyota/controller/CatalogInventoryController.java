package com.tricore.crmtoyota.controller;

import com.tricore.crmtoyota.dto.CatalogDtos.*;
import com.tricore.crmtoyota.service.CatalogInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "04. Catálogo OEM, Kits y Almacén", description = "Repuestos OEM/Alternativos, tarifas de mano de obra, kits por km y reposición rápida")
public class CatalogInventoryController {

    private final CatalogInventoryService service;

    public CatalogInventoryController(CatalogInventoryService service) {
        this.service = service;
    }

    @GetMapping("/parts")
    @Operation(summary = "Consultar catálogo de repuestos filtrando por texto, categoría y compatibilidad vehicular")
    public List<PartResponse> searchParts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long vehicleId
    ) {
        return service.searchParts(q, category, vehicleId);
    }

    @PostMapping("/parts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACEN')")
    @Operation(summary = "Crear repuesto en catálogo (ADMIN / ALMACEN)")
    public PartResponse createPart(@Valid @RequestBody PartRequest request) {
        return service.createPart(request);
    }

    @PutMapping("/parts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACEN')")
    @Operation(summary = "Actualizar repuesto en catálogo (ADMIN / ALMACEN)")
    public PartResponse updatePart(@PathVariable Long id, @Valid @RequestBody PartRequest request) {
        return service.updatePart(id, request);
    }

    @GetMapping("/labor-rates")
    @Operation(summary = "Listar tarifario estándar de mano de obra Toyota (precios inc. IGV)")
    public List<LaborRateResponse> listLaborRates() {
        return service.listLaborRates();
    }

    @PostMapping("/labor-rates")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar tarifa estándar de mano de obra (solo ADMIN)")
    public LaborRateResponse createLaborRate(@Valid @RequestBody LaborRateRequest request) {
        return service.createLaborRate(request);
    }

    @GetMapping("/maintenance-kits")
    @Operation(summary = "Listar paquetes preventivos Toyota por modelo y kilometraje")
    public List<MaintenanceKitResponse> listKits() {
        return service.listKits();
    }

    @GetMapping("/maintenance-kits/suggest")
    @Operation(summary = "Sugerir paquete preventivo ideal según modelo Toyota y kilometraje actual")
    public MaintenanceKitResponse suggestKit(@RequestParam String model, @RequestParam int mileageKm) {
        return service.suggestKitForVehicle(model, mileageKm).orElse(null);
    }

    @PostMapping("/inventory/replenishments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACEN')")
    @Operation(summary = "Registrar ingreso rápido de compra/reposición de stock (levanta alertas rojas en vivo)")
    public InventoryMovementResponse replenishStock(
            @Valid @RequestBody StockReplenishmentRequest request,
            Authentication authentication
    ) {
        return service.replenishStock(request, authentication.getName());
    }

    @GetMapping("/inventory/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACEN')")
    @Operation(summary = "Listar últimos 50 movimientos auditables de kardex")
    public List<InventoryMovementResponse> listMovements() {
        return service.listRecentMovements();
    }
}
