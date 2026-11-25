package com.tricore.crmtoyota;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.tricore.crmtoyota.domain.enums.*;
import com.tricore.crmtoyota.dto.AuthDtos.LoginRequest;
import com.tricore.crmtoyota.dto.CatalogDtos.*;
import com.tricore.crmtoyota.dto.CustomerVehicleDtos.*;
import com.tricore.crmtoyota.dto.QuoteDtos.*;
import com.tricore.crmtoyota.dto.WorkshopSettingsDtos.WorkshopSettingsUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CrmToyotaFullFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    @Test
    void endToEndMostradorLifecycle_stockConflict_substitute_approve_pdf_revert_and_mlDashboard() throws Exception {
        String adminToken = loginAndGetToken("admin", "Toyota2026!");
        String recepToken = loginAndGetToken("recepcion", "Toyota2026!");
        String almacenToken = loginAndGetToken("almacen", "Toyota2026!");

        // 1. Sliding refresh & bad credentials
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxDiscountPercent").value(5));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("recepcion", "WrongPass"))))
                .andExpect(status().isUnauthorized());

        // 2. White-label settings public GET + ADMIN PUT + RECEPCIONISTA 403
        mockMvc.perform(get("/api/v1/workshop-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("[Nombre del Taller Automotriz]"));

        var updateWs = new WorkshopSettingsUpdateRequest(
                "Taller Piloto Toyota Lima",
                "Especialistas Postventa Toyota",
                "20601234567",
                "Av. República de Panamá 4120, Surquillo",
                "+51 987 654 321",
                "contacto@pilototoyota.pe",
                7
        );
        mockMvc.perform(put("/api/v1/workshop-settings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWs)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/workshop-settings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWs)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Taller Piloto Toyota Lima"));

        // 3. Customer Ley 29733 + RUC + Vehicle + Preventive Kit Suggestion
        var newCustomer = new CustomerRequest(
                DocumentType.RUC, "20609988771", "Flota Corporativa Pacífico SAC",
                "994556677", "flota@pacifico.pe", "Av. Javier Prado 1200", true
        );
        MvcResult custRes = mockMvc.perform(post("/api/v1/customers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isCreated())
                .andReturn();
        Long customerId = objectMapper.readTree(custRes.getResponse().getContentAsString()).get("id").asLong();

        var newVehicle = new VehicleRequest(
                customerId, "T9X-712", false, "MR2B29F30M9988776",
                "Yaris", 2022, "1.5 CVT", 39100, false
        );
        MvcResult vehRes = mockMvc.perform(post("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newVehicle)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.suggestedPreventiveKit.code").value("KIT-YARIS-40K"))
                .andReturn();
        Long vehicleId = objectMapper.readTree(vehRes.getResponse().getContentAsString()).get("id").asLong();

        // 4. Search POS vehicles, parts compatible with Yaris, labor rates & kits
        mockMvc.perform(get("/api/v1/vehicles").param("q", "T9X-712")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].licensePlate").value("T9X-712"));

        mockMvc.perform(get("/api/v1/parts")
                        .param("vehicleId", String.valueOf(vehicleId))
                        .param("category", "Frenos")
                        .param("q", "Pastillas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].oemReferenceCode").value("04465-0D150"));

        mockMvc.perform(get("/api/v1/labor-rates")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/maintenance-kits")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/maintenance-kits/suggest")
                        .param("model", "Yaris")
                        .param("mileageKm", "39850")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("KIT-YARIS-40K"));

        // 5. Create Quote with out-of-stock part #6 (AIS-PAD-0D1, stock 0) + discount cap test
        var overDiscountReq = new QuoteUpsertRequest(
                customerId, vehicleId, 39200, new BigDecimal("10.00"), "Intento exceso descuento",
                List.of(new QuoteItemRequest(QuoteItemType.PART, 1L, null, 1))
        );
        mockMvc.perform(post("/api/v1/quotes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overDiscountReq)))
                .andExpect(status().isBadRequest());

        var validQuoteReq = new QuoteUpsertRequest(
                customerId, vehicleId, 39200, new BigDecimal("5.00"), "Revisión frenos y aceite",
                List.of(
                        new QuoteItemRequest(QuoteItemType.PART, 6L, null, 1), // AIS-PAD-0D1 stock = 0
                        new QuoteItemRequest(QuoteItemType.LABOR, null, 5L, 1)
                )
        );
        MvcResult quoteRes = mockMvc.perform(post("/api/v1/quotes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validQuoteReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDIENTE"))
                .andExpect(jsonPath("$.whatsappUrl").isNotEmpty())
                .andReturn();
        Long quoteId = objectMapper.readTree(quoteRes.getResponse().getContentAsString()).get("id").asLong();

        // 6. Attempt Approve -> Must return HTTP 409 Conflict with compatibleEquivalents AND stay PENDIENTE
        mockMvc.perform(post("/api/v1/quotes/" + quoteId + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.conflicts[0].sku").value("AIS-PAD-0D1"))
                .andExpect(jsonPath("$.conflicts[0].compatibleEquivalents").isNotEmpty());

        mockMvc.perform(get("/api/v1/quotes/" + quoteId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDIENTE"));

        // 7. Substitute missing part #6 with equivalent #8 (DEN-PAD-523, stock 6) + autoApprove = true!
        var subReq = new SubstitutePartRequest(6L, 8L, true);
        mockMvc.perform(post("/api/v1/quotes/" + quoteId + "/substitute-part")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROBADA"));

        // Verify PDF generation (< 3 seconds, non-empty PDF bytes, NOT showing closed date while APROBADA)
        long startMs = System.currentTimeMillis();
        MvcResult pdfRes = mockMvc.perform(get("/api/v1/quotes/" + quoteId + "/pdf")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();
        long elapsedMs = System.currentTimeMillis() - startMs;
        assertThat(elapsedMs).isLessThan(3000L);
        byte[] approvedPdfBytes = pdfRes.getResponse().getContentAsByteArray();
        assertThat(approvedPdfBytes.length).isGreaterThan(1000);
        try (PdfReader approvedReader = new PdfReader(approvedPdfBytes)) {
            String approvedPdfText = new PdfTextExtractor(approvedReader).getTextFromPage(1);
            assertThat(approvedPdfText).contains("Válida hasta:");
            assertThat(approvedPdfText).doesNotContain("Fecha de atención / cierre:");
        }

        // 8. Revert APROBADA -> RECHAZADA (atomically restores stock to part #8 back to 6)
        var rejectReq = new QuoteRejectRequest(RejectionReason.PRECIO, "Cliente decidió postergar servicio");
        mockMvc.perform(post("/api/v1/quotes/" + quoteId + "/reject")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECHAZADA"))
                .andExpect(jsonPath("$.rejectionReason").value("PRECIO"));

        // 9. Close an approved seed quote (#4 COT-2026-00040 -> CERRADA) and verify closedAt + PDF attention date
        mockMvc.perform(post("/api/v1/quotes/4/close")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CERRADA"))
                .andExpect(jsonPath("$.closedAt").isNotEmpty());

        MvcResult closedPdfRes = mockMvc.perform(get("/api/v1/quotes/4/pdf")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + recepToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();
        try (PdfReader closedReader = new PdfReader(closedPdfRes.getResponse().getContentAsByteArray())) {
            String closedPdfText = new PdfTextExtractor(closedReader).getTextFromPage(1);
            assertThat(closedPdfText).contains("Estado: CERRADA");
            assertThat(closedPdfText).contains("Atendida:");
            assertThat(closedPdfText).contains("Fecha de atención / cierre:");
        }

        // 10. Warehouse Stock Replenishment (EXT04) + Movements + ML Forecast Dashboard & Recalculate
        var repReq = new StockReplenishmentRequest(
                6L, 25, "Importaciones Nipón SAC", "FAC-F002-00891", new BigDecimal("110.00")
        );
        mockMvc.perform(post("/api/v1/inventory/replenishments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + almacenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stockAfter").value(25));

        mockMvc.perform(get("/api/v1/inventory/movements")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + almacenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].referenceDoc").value("FAC-F002-00891"));

        mockMvc.perform(get("/api/v1/forecasts/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + almacenToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelHealth.r2").value(0.86));

        mockMvc.perform(post("/api/v1/forecasts/recalculate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelHealth.executionSource").value("MOCK_FALLBACK"));
    }
}
