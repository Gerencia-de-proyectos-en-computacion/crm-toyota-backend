package com.tricore.crmtoyota.service;

import com.tricore.crmtoyota.domain.entity.*;
import com.tricore.crmtoyota.domain.enums.*;
import com.tricore.crmtoyota.dto.QuoteDtos.*;
import com.tricore.crmtoyota.exception.BusinessRuleException;
import com.tricore.crmtoyota.exception.InsufficientStockException;
import com.tricore.crmtoyota.exception.ResourceNotFoundException;
import com.tricore.crmtoyota.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class QuoteService {

    private static final BigDecimal IGV_DIVISOR = new BigDecimal("1.18");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final QuoteRepository quoteRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final PartRepository partRepository;
    private final LaborRateRepository laborRateRepository;
    private final UserRepository userRepository;
    private final InventoryMovementRepository movementRepository;
    private final CatalogInventoryService catalogService;
    private final WorkshopSettingsService workshopSettingsService;

    public QuoteService(
            QuoteRepository quoteRepository,
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            PartRepository partRepository,
            LaborRateRepository laborRateRepository,
            UserRepository userRepository,
            InventoryMovementRepository movementRepository,
            CatalogInventoryService catalogService,
            WorkshopSettingsService workshopSettingsService
    ) {
        this.quoteRepository = quoteRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.partRepository = partRepository;
        this.laborRateRepository = laborRateRepository;
        this.userRepository = userRepository;
        this.movementRepository = movementRepository;
        this.catalogService = catalogService;
        this.workshopSettingsService = workshopSettingsService;
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> listQuotes(QuoteStatus status, String search) {
        List<Quote> base = status != null
                ? quoteRepository.findByStatusOrderByIdDesc(status)
                : quoteRepository.findAllByOrderByIdDesc();
        String q = search != null ? search.trim().toLowerCase() : "";
        WorkshopSettings ws = workshopSettingsService.getEntity();

        return base.stream()
                .filter(quote -> q.isEmpty() || matchesSearch(quote, q))
                .map(quote -> toQuoteResponse(quote, ws))
                .toList();
    }

    @Transactional(readOnly = true)
    public Quote getQuoteEntity(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public QuoteResponse getQuote(Long id) {
        return toQuoteResponse(getQuoteEntity(id), workshopSettingsService.getEntity());
    }

    @Transactional
    public QuoteResponse createQuote(QuoteUpsertRequest req, String username) {
        User actor = getActiveUser(username);
        Quote quote = new Quote();
        Long seq = quoteRepository.nextQuoteSequence();
        quote.setQuoteNumber(String.format("COT-2026-%05d", seq));
        quote.setCreatedBy(actor);
        quote.setStatus(QuoteStatus.PENDIENTE);
        quote.setCreatedAt(OffsetDateTime.now());

        WorkshopSettings ws = workshopSettingsService.getEntity();
        quote.setValidUntil(LocalDate.now().plusDays(ws.getQuoteValidityDays()));

        applyUpsertData(quote, req, actor);
        return toQuoteResponse(quoteRepository.save(quote), ws);
    }

    @Transactional
    public QuoteResponse updateQuote(Long id, QuoteUpsertRequest req, String username) {
        User actor = getActiveUser(username);
        Quote quote = getQuoteEntity(id);
        if (quote.getStatus() != QuoteStatus.PENDIENTE) {
            throw new BusinessRuleException("Solo se pueden editar cotizaciones en estado PENDIENTE.");
        }
        applyUpsertData(quote, req, actor);
        return toQuoteResponse(quoteRepository.save(quote), workshopSettingsService.getEntity());
    }

    @Transactional
    public QuoteResponse approveQuote(Long id, String username) {
        Quote quote = getQuoteEntity(id);
        if (quote.getStatus() != QuoteStatus.PENDIENTE) {
            throw new BusinessRuleException("Solo una cotización PENDIENTE puede pasar a APROBADA.");
        }

        Map<Long, Integer> requiredByPartId = new LinkedHashMap<>();
        Map<Long, Part> partMap = new LinkedHashMap<>();
        for (QuoteItem item : quote.getItems()) {
            if (item.getItemType() == QuoteItemType.PART && item.getPart() != null) {
                Part part = partRepository.findById(item.getPart().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Repuesto no encontrado: " + item.getPart().getId()));
                partMap.put(part.getId(), part);
                requiredByPartId.merge(part.getId(), item.getQuantity(), Integer::sum);
            }
        }

        List<StockConflictDetail> conflicts = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : requiredByPartId.entrySet()) {
            Part part = partMap.get(entry.getKey());
            int requested = entry.getValue();
            if (part.getCurrentStock() < requested) {
                var equivalents = catalogService.findAvailableEquivalents(part, requested, Map.of());
                conflicts.add(new StockConflictDetail(
                        part.getId(),
                        part.getSku(),
                        part.getOemReferenceCode(),
                        part.getName(),
                        part.getBrand(),
                        requested,
                        part.getCurrentStock(),
                        equivalents
                ));
            }
        }

        if (!conflicts.isEmpty()) {
            throw new InsufficientStockException(quote.getQuoteNumber(), conflicts);
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (Map.Entry<Long, Integer> entry : requiredByPartId.entrySet()) {
            Part part = partMap.get(entry.getKey());
            int delta = entry.getValue();
            int remaining = part.getCurrentStock() - delta;
            part.setCurrentStock(remaining);
            partRepository.saveAndFlush(part);

            InventoryMovement mov = new InventoryMovement();
            mov.setPart(part);
            mov.setMovementType(MovementType.SALIDA_COTIZACION);
            mov.setQuantityDelta(-delta);
            mov.setStockAfter(remaining);
            mov.setReferenceDoc(quote.getQuoteNumber());
            mov.setPerformedBy(username);
            mov.setCreatedAt(now);
            movementRepository.save(mov);
        }

        quote.setStatus(QuoteStatus.APROBADA);
        quote.setUpdatedAt(now);
        return toQuoteResponse(quoteRepository.save(quote), workshopSettingsService.getEntity());
    }

    @Transactional
    public QuoteResponse substitutePartInQuote(Long quoteId, SubstitutePartRequest req, String username) {
        Quote quote = getQuoteEntity(quoteId);
        if (quote.getStatus() != QuoteStatus.PENDIENTE) {
            throw new BusinessRuleException("Solo se pueden sustituir repuestos en cotizaciones PENDIENTES.");
        }
        Part replacement = partRepository.findById(req.replacementPartId())
                .orElseThrow(() -> new ResourceNotFoundException("Repuesto sustituto no encontrado: " + req.replacementPartId()));

        boolean replacedAny = false;
        for (QuoteItem item : quote.getItems()) {
            if (item.getItemType() == QuoteItemType.PART && item.getPart() != null
                    && item.getPart().getId().equals(req.missingPartId())) {
                item.setPart(replacement);
                item.setItemCode(replacement.getSku());
                item.setDescription(replacement.getName() + " (" + replacement.getBrand().name() + ")");
                item.setUnitPriceIgv(replacement.getSalePriceIgv());
                item.setLineTotalIgv(replacement.getSalePriceIgv().multiply(BigDecimal.valueOf(item.getQuantity())));
                replacedAny = true;
            }
        }
        if (!replacedAny) {
            throw new BusinessRuleException("El repuesto indicado no forma parte de la proforma " + quote.getQuoteNumber());
        }

        recalculateTotals(quote, quote.getDiscountPercent());
        quote.setUpdatedAt(OffsetDateTime.now());
        quoteRepository.save(quote);

        if (req.autoApproveAfterSubstitute()) {
            return approveQuote(quoteId, username);
        }
        return toQuoteResponse(quote, workshopSettingsService.getEntity());
    }

    @Transactional
    public QuoteResponse rejectQuote(Long id, QuoteRejectRequest req, String username) {
        Quote quote = getQuoteEntity(id);
        if (quote.getStatus() == QuoteStatus.CERRADA || quote.getStatus() == QuoteStatus.RECHAZADA) {
            throw new BusinessRuleException("No se puede rechazar una cotización en estado " + quote.getStatus());
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (quote.getStatus() == QuoteStatus.APROBADA) {
            for (QuoteItem item : quote.getItems()) {
                if (item.getItemType() == QuoteItemType.PART && item.getPart() != null) {
                    Part part = partRepository.findById(item.getPart().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Repuesto no encontrado: " + item.getPart().getId()));
                    int restored = part.getCurrentStock() + item.getQuantity();
                    part.setCurrentStock(restored);
                    partRepository.saveAndFlush(part);

                    InventoryMovement mov = new InventoryMovement();
                    mov.setPart(part);
                    mov.setMovementType(MovementType.REINGRESO_RECHAZO);
                    mov.setQuantityDelta(item.getQuantity());
                    mov.setStockAfter(restored);
                    mov.setReferenceDoc(quote.getQuoteNumber());
                    mov.setPerformedBy(username);
                    mov.setCreatedAt(now);
                    movementRepository.save(mov);
                }
            }
        }

        quote.setStatus(QuoteStatus.RECHAZADA);
        quote.setRejectionReason(req.rejectionReason());
        quote.setRejectionNotes(req.rejectionNotes() != null ? req.rejectionNotes().trim() : null);
        quote.setUpdatedAt(now);
        return toQuoteResponse(quoteRepository.save(quote), workshopSettingsService.getEntity());
    }

    @Transactional
    public QuoteResponse closeQuote(Long id) {
        Quote quote = getQuoteEntity(id);
        if (quote.getStatus() != QuoteStatus.APROBADA) {
            throw new BusinessRuleException("Solo una cotización APROBADA puede pasar a estado CERRADA.");
        }
        quote.setStatus(QuoteStatus.CERRADA);
        quote.setUpdatedAt(OffsetDateTime.now());
        return toQuoteResponse(quoteRepository.save(quote), workshopSettingsService.getEntity());
    }

    private void applyUpsertData(Quote quote, QuoteUpsertRequest req, User actor) {
        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + req.customerId()));
        Vehicle vehicle = vehicleRepository.findById(req.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado: " + req.vehicleId()));
        if (!vehicle.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessRuleException("El vehículo seleccionado no pertenece al cliente indicado.");
        }

        BigDecimal discountPct = req.discountPercent() != null ? req.discountPercent() : BigDecimal.ZERO;
        int maxAllowed = AuthService.maxDiscountForRole(actor.getRole());
        if (discountPct.compareTo(BigDecimal.valueOf(maxAllowed)) > 0) {
            throw new BusinessRuleException(
                    "El rol " + actor.getRole() + " solo puede aplicar hasta " + maxAllowed + "% de descuento."
            );
        }

        if (req.mileageAtQuote() > vehicle.getCurrentMileageKm()) {
            vehicle.setCurrentMileageKm(req.mileageAtQuote());
            vehicleRepository.save(vehicle);
        }

        List<QuoteItem> builtItems = new ArrayList<>();
        for (QuoteItemRequest ir : req.items()) {
            QuoteItem qi = new QuoteItem();
            qi.setItemType(ir.itemType());
            qi.setQuantity(ir.quantity());
            if (ir.itemType() == QuoteItemType.PART) {
                if (ir.partId() == null) {
                    throw new BusinessRuleException("Todo ítem de tipo PART requiere partId.");
                }
                Part part = partRepository.findById(ir.partId())
                        .orElseThrow(() -> new ResourceNotFoundException("Repuesto no encontrado: " + ir.partId()));
                qi.setPart(part);
                qi.setItemCode(part.getSku());
                qi.setDescription(part.getName() + " (" + part.getBrand().name() + ")");
                qi.setUnitPriceIgv(part.getSalePriceIgv());
                qi.setLineTotalIgv(part.getSalePriceIgv().multiply(BigDecimal.valueOf(ir.quantity())));
            } else {
                if (ir.laborRateId() == null) {
                    throw new BusinessRuleException("Todo ítem de tipo LABOR requiere laborRateId.");
                }
                LaborRate lr = laborRateRepository.findById(ir.laborRateId())
                        .orElseThrow(() -> new ResourceNotFoundException("Tarifa de mano de obra no encontrada: " + ir.laborRateId()));
                qi.setLaborRate(lr);
                qi.setItemCode(lr.getCode());
                qi.setDescription(lr.getServiceName());
                qi.setUnitPriceIgv(lr.getPriceIgv());
                qi.setLineTotalIgv(lr.getPriceIgv().multiply(BigDecimal.valueOf(ir.quantity())));
            }
            builtItems.add(qi);
        }

        quote.setCustomer(customer);
        quote.setVehicle(vehicle);
        quote.setMileageAtQuote(req.mileageAtQuote());
        quote.setNotes(req.notes() != null ? req.notes().trim() : null);
        quote.replaceItems(builtItems);
        recalculateTotals(quote, discountPct);
        quote.setUpdatedAt(OffsetDateTime.now());
    }

    private void recalculateTotals(Quote quote, BigDecimal discountPct) {
        BigDecimal gross = BigDecimal.ZERO;
        for (QuoteItem qi : quote.getItems()) {
            gross = gross.add(qi.getLineTotalIgv());
        }
        gross = gross.setScale(2, RoundingMode.HALF_UP);
        BigDecimal pct = discountPct.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmt = gross.multiply(pct).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = gross.subtract(discountAmt).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxable = finalTotal.divide(IGV_DIVISOR, 2, RoundingMode.HALF_UP);
        BigDecimal igv = finalTotal.subtract(taxable).setScale(2, RoundingMode.HALF_UP);

        quote.setGrossTotalIgv(gross);
        quote.setDiscountPercent(pct);
        quote.setDiscountAmount(discountAmt);
        quote.setTaxableSubtotal(taxable);
        quote.setIgvAmount(igv);
        quote.setFinalTotalIgv(finalTotal);
    }

    private User getActiveUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + username));
    }

    private boolean matchesSearch(Quote quote, String q) {
        return quote.getQuoteNumber().toLowerCase().contains(q)
                || quote.getVehicle().getLicensePlate().toLowerCase().contains(q)
                || quote.getCustomer().getDocumentNumber().toLowerCase().contains(q)
                || quote.getCustomer().getFullName().toLowerCase().contains(q)
                || quote.getVehicle().getModel().toLowerCase().contains(q);
    }

    private QuoteResponse toQuoteResponse(Quote q, WorkshopSettings ws) {
        List<QuoteItemResponse> itemDtos = q.getItems().stream()
                .map(i -> new QuoteItemResponse(
                        i.getId(),
                        i.getItemType(),
                        i.getPart() != null ? i.getPart().getId() : null,
                        i.getLaborRate() != null ? i.getLaborRate().getId() : null,
                        i.getItemCode(),
                        i.getDescription(),
                        i.getQuantity(),
                        i.getUnitPriceIgv(),
                        i.getLineTotalIgv(),
                        i.getPart() != null ? i.getPart().getCurrentStock() : null
                ))
                .toList();

        Vehicle v = q.getVehicle();
        Customer c = q.getCustomer();
        String vehicleSummary = "Toyota " + v.getModel() + " " + v.getTrimVersion() + " (" + v.getManufactureYear() + ")";
        String waUrl = buildWhatsAppUrl(q, c, v, ws);

        return new QuoteResponse(
                q.getId(),
                q.getQuoteNumber(),
                q.getStatus(),
                q.getRejectionReason(),
                q.getRejectionNotes(),
                c.getId(),
                c.getFullName(),
                c.getDocumentType().name(),
                c.getDocumentNumber(),
                c.getPhone(),
                v.getId(),
                v.getLicensePlate(),
                vehicleSummary,
                q.getMileageAtQuote(),
                q.getGrossTotalIgv(),
                q.getDiscountPercent(),
                q.getDiscountAmount(),
                q.getTaxableSubtotal(),
                q.getIgvAmount(),
                q.getFinalTotalIgv(),
                q.getValidUntil(),
                q.getNotes(),
                q.getCreatedBy().getFullName(),
                waUrl,
                q.getCreatedAt(),
                q.getUpdatedAt(),
                itemDtos
        );
    }

    private String buildWhatsAppUrl(Quote q, Customer c, Vehicle v, WorkshopSettings ws) {
        String digits = c.getPhone().replaceAll("\\D", "");
        String phone = digits.startsWith("51") ? digits : "51" + digits;
        StringBuilder msg = new StringBuilder();
        msg.append("Hola ").append(c.getFullName()).append(", te enviamos el resumen de tu *Proforma ")
                .append(q.getQuoteNumber()).append("* en *").append(ws.getBusinessName()).append("*:\n")
                .append("• Vehículo: Toyota ").append(v.getModel()).append(" (").append(v.getLicensePlate()).append(")\n");
        for (QuoteItem i : q.getItems()) {
            msg.append("  - ").append(i.getQuantity()).append("x ").append(i.getDescription())
                    .append(": S/ ").append(i.getLineTotalIgv()).append("\n");
        }
        msg.append("• *Total (inc. IGV 18%): S/ ").append(q.getFinalTotalIgv()).append("*\n")
                .append("• Vigencia: ").append(q.getValidUntil()).append(" (7 días calendario).");
        return "https://wa.me/" + phone + "?text=" + URLEncoder.encode(msg.toString(), StandardCharsets.UTF_8);
    }
}
