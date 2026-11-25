package com.tricore.crmtoyota.service;

import com.tricore.crmtoyota.domain.enums.*;
import com.tricore.crmtoyota.dto.CatalogDtos.*;
import com.tricore.crmtoyota.dto.CustomerVehicleDtos.*;
import com.tricore.crmtoyota.dto.QuoteDtos.*;
import com.tricore.crmtoyota.dto.WorkshopSettingsDtos.WorkshopSettingsUpdateRequest;
import com.tricore.crmtoyota.exception.BusinessRuleException;
import com.tricore.crmtoyota.exception.DuplicateResourceException;
import com.tricore.crmtoyota.exception.ResourceNotFoundException;
import com.tricore.crmtoyota.repository.DemandForecastRepository;
import com.tricore.crmtoyota.repository.MlModelRunRepository;
import com.tricore.crmtoyota.repository.WorkshopSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceEdgeCasesCoverageTest {

    @Autowired private AuthService authService;
    @Autowired private WorkshopSettingsService workshopSettingsService;
    @Autowired private WorkshopSettingsRepository workshopSettingsRepository;
    @Autowired private CustomerVehicleService customerVehicleService;
    @Autowired private CatalogInventoryService catalogInventoryService;
    @Autowired private QuoteService quoteService;
    @Autowired private QuotePdfService quotePdfService;
    @Autowired private ForecastDashboardService forecastDashboardService;
    @Autowired private DemandForecastRepository demandForecastRepository;
    @Autowired private MlModelRunRepository mlModelRunRepository;

    @Test
    void stockSemaphorePolicy_coversAllThresholds() {
        assertThat(StockSemaphorePolicy.evaluate(0, 5, 10)).isEqualTo(StockAlertLevel.CRITICO);
        assertThat(StockSemaphorePolicy.evaluate(3, 5, 10)).isEqualTo(StockAlertLevel.CRITICO);
        assertThat(StockSemaphorePolicy.evaluate(5, 2, 10)).isEqualTo(StockAlertLevel.CRITICO);
        assertThat(StockSemaphorePolicy.evaluate(8, 3, 10)).isEqualTo(StockAlertLevel.ALERTA);
        assertThat(StockSemaphorePolicy.evaluate(6, 4, null)).isEqualTo(StockAlertLevel.ALERTA);
        assertThat(StockSemaphorePolicy.evaluate(25, 4, 10)).isEqualTo(StockAlertLevel.NORMAL);
        assertThat(StockSemaphorePolicy.evaluate(15, 4, null)).isEqualTo(StockAlertLevel.NORMAL);
    }

    @Test
    void authAndWorkshopSettings_coverFallbacksAndRoles() {
        assertThat(AuthService.maxDiscountForRole(Role.ADMIN)).isEqualTo(15);
        assertThat(AuthService.maxDiscountForRole(Role.RECEPCIONISTA)).isEqualTo(5);
        assertThat(AuthService.maxDiscountForRole(Role.ALMACEN)).isZero();

        assertThatThrownBy(() -> authService.refreshSession("nonexistent"))
                .isInstanceOf(BadCredentialsException.class);

        workshopSettingsRepository.deleteAll();
        var fallback = workshopSettingsService.getSettings();
        assertThat(fallback.businessName()).isEqualTo("[Nombre del Taller Automotriz]");

        var recreated = workshopSettingsService.updateSettings(new WorkshopSettingsUpdateRequest(
                "Taller Demo", "Lema Demo", "20123456789", "Calle 1", "999888777", null, null
        ));
        assertThat(recreated.quoteValidityDays()).isEqualTo(7);
    }

    @Test
    void customerAndVehicleValidation_coversAllBranches() {
        assertThat(customerVehicleService.listCustomers(null)).isNotEmpty();
        assertThat(customerVehicleService.listCustomers("Paredes")).isNotEmpty();
        assertThat(customerVehicleService.searchVehiclesForPos(null)).isNotEmpty();

        // Missing Ley 29733 consent
        assertThatThrownBy(() -> customerVehicleService.createCustomer(
                new CustomerRequest(DocumentType.DNI, "12345678", "Test", "999111222", null, null, false)
        )).isInstanceOf(BusinessRuleException.class);

        // Invalid DNI & RUC formats
        assertThatThrownBy(() -> customerVehicleService.createCustomer(
                new CustomerRequest(DocumentType.DNI, "1234", "Test", "999111222", null, null, true)
        )).isInstanceOf(BusinessRuleException.class);

        assertThatThrownBy(() -> customerVehicleService.createCustomer(
                new CustomerRequest(DocumentType.RUC, "30123456789", "Empresa", "999111222", null, null, true)
        )).isInstanceOf(BusinessRuleException.class);

        // Duplicate DNI
        assertThatThrownBy(() -> customerVehicleService.createCustomer(
                new CustomerRequest(DocumentType.DNI, "44821903", "Duplicado", "999111222", null, null, true)
        )).isInstanceOf(DuplicateResourceException.class);

        // Create & update customer
        var created = customerVehicleService.createCustomer(
                new CustomerRequest(DocumentType.DNI, "76543210", "Ana Torres", "988776655", "", "", true)
        );
        var updated = customerVehicleService.updateCustomer(created.id(),
                new CustomerRequest(DocumentType.DNI, "76543211", "Ana Torres M.", "988776655", "ana@test.pe", "Surco", true)
        );
        assertThat(updated.documentNumber()).isEqualTo("76543211");

        assertThatThrownBy(() -> customerVehicleService.updateCustomer(created.id(),
                new CustomerRequest(DocumentType.DNI, "44821903", "Colision", "988776655", null, null, true)
        )).isInstanceOf(DuplicateResourceException.class);

        assertThatThrownBy(() -> customerVehicleService.updateCustomer(9999L,
                new CustomerRequest(DocumentType.DNI, "76543211", "NoExiste", "988776655", null, null, true)
        )).isInstanceOf(ResourceNotFoundException.class);

        // Vehicle validations: invalid plate, special plate, mileage regression & override
        assertThatThrownBy(() -> customerVehicleService.createVehicle(
                new VehicleRequest(created.id(), "INVALIDPLATE", false, null, "Yaris", 2021, "1.5", 10000, false)
        )).isInstanceOf(BusinessRuleException.class);

        assertThatThrownBy(() -> customerVehicleService.createVehicle(
                new VehicleRequest(created.id(), "ABC-123", false, null, "Yaris", 2021, "1.5", 10000, false)
        )).isInstanceOf(DuplicateResourceException.class);

        var specialVeh = customerVehicleService.createVehicle(
                new VehicleRequest(created.id(), "DIPL-01", true, "", "LandCruiser", 2024, "VX", 85000, false)
        );
        assertThat(specialVeh.suggestedPreventiveKit()).isNull();

        assertThatThrownBy(() -> customerVehicleService.updateVehicle(specialVeh.id(),
                new VehicleRequest(created.id(), "DIPL-01", true, "VIN123", "LandCruiser", 2024, "VX", 50000, false)
        )).isInstanceOf(BusinessRuleException.class);

        var overriddenVeh = customerVehicleService.updateVehicle(specialVeh.id(),
                new VehicleRequest(created.id(), "DIPL-02", true, "VIN123", "Yaris", 2024, "1.5", 95000, true)
        );
        assertThat(overriddenVeh.currentMileageKm()).isEqualTo(95000);
        assertThat(overriddenVeh.suggestedPreventiveKit()).isNotNull();
    }

    @Test
    void catalogAndQuoteEdgeCases_coverCRUDAndStateGuards() {
        // Search parts with text query, universal fallback & update part
        assertThat(catalogInventoryService.searchParts("04152", "Todos", null)).isNotEmpty();
        assertThat(catalogInventoryService.searchParts("inexistente-xyz", null, null)).isEmpty();

        var createdPart = catalogInventoryService.createPart(new PartRequest(
                "ALT-FIL-999", "04152-YZZA6", PartBrand.ALTERNATIVO, "Filtro Económico",
                "Filtros", "Universal Toyota", new BigDecimal("25.00"), 12, 3, List.of()
        ));
        assertThatThrownBy(() -> catalogInventoryService.createPart(new PartRequest(
                "ALT-FIL-999", "04152-YZZA6", PartBrand.ALTERNATIVO, "Dup",
                "Filtros", "Universal", new BigDecimal("25.00"), 12, 3, null
        ))).isInstanceOf(DuplicateResourceException.class);

        var updatedPart = catalogInventoryService.updatePart(createdPart.id(), new PartRequest(
                "ALT-FIL-998", "04152-YZZA6", PartBrand.ALTERNATIVO, "Filtro Económico V2",
                "Filtros", "Yaris", new BigDecimal("28.00"), 15, 4,
                List.of(new CompatibilitySpec("Yaris", 2018, 2025, ""))
        ));
        assertThat(updatedPart.sku()).isEqualTo("ALT-FIL-998");

        var createdLabor = catalogInventoryService.createLaborRate(new LaborRateRequest(
                "MO-ALIN-3D", "Alineamiento Computarizado 3D", new BigDecimal("0.75"), null, new BigDecimal("70.00")
        ));
        assertThat(createdLabor.code()).isEqualTo("MO-ALIN-3D");
        assertThatThrownBy(() -> catalogInventoryService.createLaborRate(new LaborRateRequest(
                "MO-ALIN-3D", "Dup", new BigDecimal("0.75"), null, new BigDecimal("70.00")
        ))).isInstanceOf(DuplicateResourceException.class);

        var updatedLabor = catalogInventoryService.updateLaborRate(createdLabor.id(), new LaborRateRequest(
                "MO-ALIN-3D-V2", "Alineamiento 3D Actualizado", new BigDecimal("1.00"), 10000, new BigDecimal("85.00")
        ));
        assertThat(updatedLabor.priceIgv()).isEqualByComparingTo("85.00");
        assertThatThrownBy(() -> catalogInventoryService.updateLaborRate(createdLabor.id(), new LaborRateRequest(
                "MO-MANT-10K", "Dup", new BigDecimal("1.00"), null, new BigDecimal("85.00")
        ))).isInstanceOf(DuplicateResourceException.class);
        assertThatThrownBy(() -> catalogInventoryService.updateLaborRate(9999L, new LaborRateRequest(
                "MO-NO-EXISTE", "X", new BigDecimal("1.00"), null, new BigDecimal("85.00")
        ))).isInstanceOf(ResourceNotFoundException.class);

        // Update part preserving existing compatibilities when compatibilities is null
        var preservedPart = catalogInventoryService.updatePart(updatedPart.id(), new PartRequest(
                "ALT-FIL-998", "04152-YZZA6", PartBrand.ALTERNATIVO, "Filtro Económico V3",
                "Filtros", "Yaris", new BigDecimal("32.00"), 15, 4, null
        ));
        assertThat(preservedPart.salePriceIgv()).isEqualByComparingTo("32.00");

        // Quotes listing, filtering, updating PENDIENTE, rejecting PENDIENTE, and state errors
        assertThat(quoteService.listQuotes(QuoteStatus.PENDIENTE, "Yaris")).isNotEmpty();
        assertThat(quoteService.listQuotes(null, "")).isNotEmpty();

        // Vehicle mismatch with customer
        assertThatThrownBy(() -> quoteService.createQuote(new QuoteUpsertRequest(
                1L, 2L, 60000, BigDecimal.ZERO, null,
                List.of(new QuoteItemRequest(QuoteItemType.LABOR, null, 1L, 1))
        ), "recepcion")).isInstanceOf(BusinessRuleException.class);

        // Missing partId / laborRateId guards
        assertThatThrownBy(() -> quoteService.createQuote(new QuoteUpsertRequest(
                1L, 1L, 40000, BigDecimal.ZERO, null,
                List.of(new QuoteItemRequest(QuoteItemType.PART, null, null, 1))
        ), "recepcion")).isInstanceOf(BusinessRuleException.class);

        assertThatThrownBy(() -> quoteService.createQuote(new QuoteUpsertRequest(
                1L, 1L, 40000, BigDecimal.ZERO, null,
                List.of(new QuoteItemRequest(QuoteItemType.LABOR, null, null, 1))
        ), "recepcion")).isInstanceOf(BusinessRuleException.class);

        // Update PENDIENTE quote #5 and then reject it directly from PENDIENTE
        var updatedQuote = quoteService.updateQuote(5L, new QuoteUpsertRequest(
                2L, 2L, 59500, null, "Actualizado en mostrador",
                List.of(new QuoteItemRequest(QuoteItemType.PART, 13L, null, 1))
        ), "admin");
        assertThat(updatedQuote.mileageAtQuote()).isEqualTo(59500);

        // Substitute without autoApprove
        var subNoApprove = quoteService.substitutePartInQuote(5L,
                new SubstitutePartRequest(13L, 15L, false), "admin");
        assertThat(subNoApprove.status()).isEqualTo(QuoteStatus.PENDIENTE);

        assertThatThrownBy(() -> quoteService.substitutePartInQuote(5L,
                new SubstitutePartRequest(999L, 15L, false), "admin"))
                .isInstanceOf(BusinessRuleException.class);

        byte[] pdfNoDiscount = quotePdfService.generateQuotePdf(5L);
        assertThat(pdfNoDiscount).isNotEmpty();

        // Also cover CERRADA PDF fallback when closedAt is null vs populated (#2 is CERRADA)
        var closedEntity = quoteService.getQuoteEntity(2L);
        closedEntity.setClosedAt(null);
        byte[] pdfClosedFallback = quotePdfService.generateQuotePdf(2L);
        assertThat(pdfClosedFallback).isNotEmpty();

        var rejectedPending = quoteService.rejectQuote(5L,
                new QuoteRejectRequest(RejectionReason.TIEMPO_ESPERA, null), "recepcion");
        assertThat(rejectedPending.status()).isEqualTo(QuoteStatus.RECHAZADA);

        // State transition guards on non-PENDIENTE / CERRADA quotes (#2 is CERRADA)
        assertThatThrownBy(() -> quoteService.updateQuote(2L, new QuoteUpsertRequest(
                5L, 5L, 80000, BigDecimal.ZERO, null,
                List.of(new QuoteItemRequest(QuoteItemType.LABOR, null, 1L, 1))
        ), "admin")).isInstanceOf(BusinessRuleException.class);

        assertThatThrownBy(() -> quoteService.approveQuote(2L, "admin"))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> quoteService.rejectQuote(2L,
                new QuoteRejectRequest(RejectionReason.OTRO, "cerrada"), "admin"))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> quoteService.closeQuote(6L))
                .isInstanceOf(BusinessRuleException.class);

        // Forecast dashboard fallback when no runs exist
        demandForecastRepository.deleteAllInBatch();
        mlModelRunRepository.deleteAllInBatch();
        var dashFallback = forecastDashboardService.getDashboard();
        assertThat(dashFallback.modelHealth().executionSource()).isEqualTo("DEFAULT");
    }
}
