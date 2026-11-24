package com.tricore.crmtoyota.service;

import com.tricore.crmtoyota.domain.entity.*;
import com.tricore.crmtoyota.domain.enums.MovementType;
import com.tricore.crmtoyota.domain.enums.StockAlertLevel;
import com.tricore.crmtoyota.dto.CatalogDtos.*;
import com.tricore.crmtoyota.exception.DuplicateResourceException;
import com.tricore.crmtoyota.exception.ResourceNotFoundException;
import com.tricore.crmtoyota.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class CatalogInventoryService {

    private final PartRepository partRepository;
    private final VehicleRepository vehicleRepository;
    private final LaborRateRepository laborRateRepository;
    private final MaintenanceKitRepository maintenanceKitRepository;
    private final DemandForecastRepository demandForecastRepository;
    private final InventoryMovementRepository movementRepository;

    public CatalogInventoryService(
            PartRepository partRepository,
            VehicleRepository vehicleRepository,
            LaborRateRepository laborRateRepository,
            MaintenanceKitRepository maintenanceKitRepository,
            DemandForecastRepository demandForecastRepository,
            InventoryMovementRepository movementRepository
    ) {
        this.partRepository = partRepository;
        this.vehicleRepository = vehicleRepository;
        this.laborRateRepository = laborRateRepository;
        this.maintenanceKitRepository = maintenanceKitRepository;
        this.demandForecastRepository = demandForecastRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    public List<PartResponse> searchParts(String query, String category, Long vehicleId) {
        Vehicle filterVehicle = vehicleId != null
                ? vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado: " + vehicleId))
                : null;

        Map<Long, Integer> ropByPart = new HashMap<>();
        for (DemandForecast df : demandForecastRepository.findAll()) {
            ropByPart.put(df.getPart().getId(), df.getReorderPoint());
        }

        String normalizedQuery = query != null ? query.trim().toLowerCase() : "";
        String normalizedCat = category != null && !category.equalsIgnoreCase("Todos") ? category.trim().toLowerCase() : "";

        List<Part> allActive = partRepository.findByActiveTrueOrderByCategoryAscNameAsc();
        List<PartResponse> results = new ArrayList<>();

        for (Part part : allActive) {
            if (!normalizedCat.isEmpty() && !part.getCategory().toLowerCase().equals(normalizedCat)) {
                continue;
            }
            if (!normalizedQuery.isEmpty() && !matchesPartQuery(part, normalizedQuery)) {
                continue;
            }
            if (filterVehicle != null && !isCompatibleWithVehicle(part, filterVehicle)) {
                continue;
            }
            results.add(toPartResponse(part, ropByPart.get(part.getId()), ropByPart));
        }
        return results;
    }

    @Transactional
    public PartResponse createPart(PartRequest req) {
        String normalizedSku = req.sku().trim().toUpperCase();
        if (partRepository.existsBySkuIgnoreCase(normalizedSku)) {
            throw new DuplicateResourceException("Ya existe un repuesto con SKU " + normalizedSku);
        }
        Part part = new Part();
        applyPartFields(part, req, normalizedSku);
        Part saved = partRepository.save(part);
        return toPartResponse(saved, null, Map.of());
    }

    @Transactional
    public PartResponse updatePart(Long id, PartRequest req) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repuesto no encontrado: " + id));
        String normalizedSku = req.sku().trim().toUpperCase();
        if (!part.getSku().equalsIgnoreCase(normalizedSku) && partRepository.existsBySkuIgnoreCase(normalizedSku)) {
            throw new DuplicateResourceException("Ya existe un repuesto con SKU " + normalizedSku);
        }
        applyPartFields(part, req, normalizedSku);
        Integer rop = demandForecastRepository.findByPartId(id).map(DemandForecast::getReorderPoint).orElse(null);
        return toPartResponse(partRepository.save(part), rop, rop != null ? Map.of(id, rop) : Map.of());
    }

    @Transactional(readOnly = true)
    public List<LaborRateResponse> listLaborRates() {
        return laborRateRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(CatalogInventoryService::toLaborResponse)
                .toList();
    }

    @Transactional
    public LaborRateResponse createLaborRate(LaborRateRequest req) {
        String code = req.code().trim().toUpperCase();
        if (laborRateRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Ya existe una tarifa de mano de obra con código " + code);
        }
        LaborRate lr = new LaborRate();
        lr.setCode(code);
        lr.setServiceName(req.serviceName().trim());
        lr.setEstimatedHours(req.estimatedHours());
        lr.setIntervalKm(req.intervalKm());
        lr.setPriceIgv(req.priceIgv());
        lr.setActive(true);
        return toLaborResponse(laborRateRepository.save(lr));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceKitResponse> listKits() {
        return maintenanceKitRepository.findByActiveTrueOrderByModelAscIntervalKmAsc().stream()
                .map(this::toKitResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<MaintenanceKitResponse> suggestKitForVehicle(String model, int currentMileageKm) {
        List<MaintenanceKit> kits = maintenanceKitRepository.findByModelIgnoreCaseAndActiveTrueOrderByIntervalKmAsc(model);
        if (kits.isEmpty()) {
            return Optional.empty();
        }
        for (MaintenanceKit kit : kits) {
            if (kit.getIntervalKm() >= currentMileageKm - 1500) {
                return Optional.of(toKitResponse(kit));
            }
        }
        return Optional.of(toKitResponse(kits.getLast()));
    }

    @Transactional
    public InventoryMovementResponse replenishStock(StockReplenishmentRequest req, String performedBy) {
        Part part = partRepository.findById(req.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Repuesto no encontrado: " + req.partId()));

        int updatedStock = part.getCurrentStock() + req.quantityAdded();
        part.setCurrentStock(updatedStock);
        partRepository.save(part);

        InventoryMovement mov = new InventoryMovement();
        mov.setPart(part);
        mov.setMovementType(MovementType.INGRESO_COMPRA);
        mov.setQuantityDelta(req.quantityAdded());
        mov.setStockAfter(updatedStock);
        mov.setSupplierName(req.supplierName().trim());
        mov.setReferenceDoc(req.referenceDoc().trim().toUpperCase());
        mov.setUnitCost(req.unitCost());
        mov.setPerformedBy(performedBy);
        mov.setCreatedAt(OffsetDateTime.now());
        InventoryMovement saved = movementRepository.save(mov);
        return toMovementResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> listRecentMovements() {
        return movementRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toMovementResponse)
                .toList();
    }

    public List<EquivalentPartOption> findAvailableEquivalents(Part part, int minRequiredQty, Map<Long, Integer> ropMap) {
        return partRepository
                .findByOemReferenceCodeIgnoreCaseAndIdNotAndActiveTrueAndCurrentStockGreaterThanOrderBySalePriceIgvAsc(
                        part.getOemReferenceCode(), part.getId(), Math.max(0, minRequiredQty - 1))
                .stream()
                .map(eq -> new EquivalentPartOption(
                        eq.getId(),
                        eq.getSku(),
                        eq.getOemReferenceCode(),
                        eq.getBrand(),
                        eq.getName(),
                        eq.getSalePriceIgv(),
                        eq.getCurrentStock(),
                        StockSemaphorePolicy.evaluate(eq.getCurrentStock(), eq.getMinStock(), ropMap.get(eq.getId()))
                ))
                .toList();
    }

    public static LaborRateResponse toLaborResponse(LaborRate lr) {
        return new LaborRateResponse(
                lr.getId(),
                lr.getCode(),
                lr.getServiceName(),
                lr.getEstimatedHours(),
                lr.getIntervalKm(),
                lr.getPriceIgv()
        );
    }

    private void applyPartFields(Part part, PartRequest req, String normalizedSku) {
        part.setSku(normalizedSku);
        part.setOemReferenceCode(req.oemReferenceCode().trim().toUpperCase());
        part.setBrand(req.brand());
        part.setName(req.name().trim());
        part.setCategory(req.category().trim());
        part.setCompatibleSummary(req.compatibleSummary().trim());
        part.setSalePriceIgv(req.salePriceIgv());
        part.setCurrentStock(req.currentStock());
        part.setMinStock(req.minStock());
        part.setActive(true);
        part.getCompatibilities().clear();
        if (req.compatibilities() != null && !req.compatibilities().isEmpty()) {
            for (CompatibilitySpec spec : req.compatibilities()) {
                PartCompatibility pc = new PartCompatibility();
                pc.setPart(part);
                pc.setModel(spec.model().trim());
                pc.setYearFrom(spec.yearFrom());
                pc.setYearTo(spec.yearTo());
                pc.setTrimVersion(spec.trimVersion() != null && !spec.trimVersion().isBlank() ? spec.trimVersion().trim() : "ALL");
                part.getCompatibilities().add(pc);
            }
        } else {
            PartCompatibility universal = new PartCompatibility();
            universal.setPart(part);
            universal.setModel("ALL");
            universal.setYearFrom(2000);
            universal.setYearTo(2030);
            universal.setTrimVersion("ALL");
            part.getCompatibilities().add(universal);
        }
    }

    private boolean matchesPartQuery(Part p, String q) {
        return p.getSku().toLowerCase().contains(q)
                || p.getOemReferenceCode().toLowerCase().contains(q)
                || p.getName().toLowerCase().contains(q)
                || p.getBrand().name().toLowerCase().contains(q)
                || p.getCompatibleSummary().toLowerCase().contains(q);
    }

    private boolean isCompatibleWithVehicle(Part part, Vehicle v) {
        if (part.getCompatibilities().isEmpty()) {
            return true;
        }
        for (PartCompatibility pc : part.getCompatibilities()) {
            boolean modelOk = "ALL".equalsIgnoreCase(pc.getModel()) || pc.getModel().equalsIgnoreCase(v.getModel());
            boolean yearOk = v.getManufactureYear() >= pc.getYearFrom() && v.getManufactureYear() <= pc.getYearTo();
            boolean trimOk = "ALL".equalsIgnoreCase(pc.getTrimVersion()) || pc.getTrimVersion().equalsIgnoreCase(v.getTrimVersion());
            if (modelOk && yearOk && trimOk) {
                return true;
            }
        }
        return false;
    }

    private PartResponse toPartResponse(Part part, Integer rop, Map<Long, Integer> ropMap) {
        StockAlertLevel level = StockSemaphorePolicy.evaluate(part.getCurrentStock(), part.getMinStock(), rop);
        List<EquivalentPartOption> equivalents = part.getCurrentStock() == 0
                ? findAvailableEquivalents(part, 1, ropMap)
                : findAvailableEquivalents(part, 1, ropMap);
        return new PartResponse(
                part.getId(),
                part.getSku(),
                part.getOemReferenceCode(),
                part.getBrand(),
                part.getName(),
                part.getCategory(),
                part.getCompatibleSummary(),
                part.getSalePriceIgv(),
                part.getCurrentStock(),
                part.getMinStock(),
                rop,
                level,
                equivalents
        );
    }

    private MaintenanceKitResponse toKitResponse(MaintenanceKit kit) {
        BigDecimal total = kit.getLaborRate().getPriceIgv();
        List<KitPartLine> lines = new ArrayList<>();
        for (MaintenanceKitItem item : kit.getItems()) {
            Part p = item.getPart();
            BigDecimal sub = p.getSalePriceIgv().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(sub);
            StockAlertLevel level = StockSemaphorePolicy.evaluate(p.getCurrentStock(), p.getMinStock(), null);
            lines.add(new KitPartLine(
                    p.getId(),
                    p.getSku(),
                    p.getOemReferenceCode(),
                    p.getBrand(),
                    p.getName(),
                    item.getQuantity(),
                    p.getSalePriceIgv(),
                    p.getCurrentStock(),
                    level
            ));
        }
        return new MaintenanceKitResponse(
                kit.getId(),
                kit.getCode(),
                kit.getName(),
                kit.getModel(),
                kit.getIntervalKm(),
                toLaborResponse(kit.getLaborRate()),
                lines,
                total
        );
    }

    private InventoryMovementResponse toMovementResponse(InventoryMovement m) {
        return new InventoryMovementResponse(
                m.getId(),
                m.getPart().getId(),
                m.getPart().getSku(),
                m.getPart().getName(),
                m.getMovementType().name(),
                m.getQuantityDelta(),
                m.getStockAfter(),
                m.getSupplierName(),
                m.getReferenceDoc(),
                m.getUnitCost(),
                m.getPerformedBy(),
                m.getCreatedAt()
        );
    }
}
