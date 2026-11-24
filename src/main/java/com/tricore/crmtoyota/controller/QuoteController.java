package com.tricore.crmtoyota.controller;

import com.tricore.crmtoyota.domain.enums.QuoteStatus;
import com.tricore.crmtoyota.dto.QuoteDtos.*;
import com.tricore.crmtoyota.service.QuotePdfService;
import com.tricore.crmtoyota.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quotes")
@PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
@Tag(name = "05. Cotizador Express POS y Proforma PDF", description = "Máquina de estados atómica (@Version), sustitución OEM, WhatsApp y OpenPDF < 3s")
public class QuoteController {

    private final QuoteService quoteService;
    private final QuotePdfService quotePdfService;

    public QuoteController(QuoteService quoteService, QuotePdfService quotePdfService) {
        this.quoteService = quoteService;
        this.quotePdfService = quotePdfService;
    }

    @GetMapping
    @Operation(summary = "Listar cotizaciones filtrando por estado y búsqueda rápida")
    public List<QuoteResponse> listQuotes(
            @RequestParam(required = false) QuoteStatus status,
            @RequestParam(required = false) String q
    ) {
        return quoteService.listQuotes(status, q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle completo de una proforma")
    public QuoteResponse getQuote(@PathVariable Long id) {
        return quoteService.getQuote(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Emitir nueva cotización en estado PENDIENTE")
    public QuoteResponse createQuote(@Valid @RequestBody QuoteUpsertRequest request, Authentication auth) {
        return quoteService.createQuote(request, auth.getName());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar cotización existente (solo si permanece PENDIENTE)")
    public QuoteResponse updateQuote(
            @PathVariable Long id,
            @Valid @RequestBody QuoteUpsertRequest request,
            Authentication auth
    ) {
        return quoteService.updateQuote(id, request, auth.getName());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprobar cotización descontando stock atómicamente (409 con equivalentes si falta stock)")
    public QuoteResponse approveQuote(@PathVariable Long id, Authentication auth) {
        return quoteService.approveQuote(id, auth.getName());
    }

    @PostMapping("/{id}/substitute-part")
    @Operation(summary = "Sustituir en 1 clic un repuesto sin stock por un equivalente compatible y aprobar")
    public QuoteResponse substitutePart(
            @PathVariable Long id,
            @Valid @RequestBody SubstitutePartRequest request,
            Authentication auth
    ) {
        return quoteService.substitutePartInQuote(id, request, auth.getName());
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rechazar cotización registrando motivo (si estaba APROBADA reintegra el stock atómicamente)")
    public QuoteResponse rejectQuote(
            @PathVariable Long id,
            @Valid @RequestBody QuoteRejectRequest request,
            Authentication auth
    ) {
        return quoteService.rejectQuote(id, request, auth.getName());
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Cerrar comercialmente una cotización previamente APROBADA")
    public QuoteResponse closeQuote(@PathVariable Long id) {
        return quoteService.closeQuote(id);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Descargar proforma comercial en PDF (< 3s) con marca blanca y desglose IGV")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        QuoteResponse quote = quoteService.getQuote(id);
        byte[] pdfBytes = quotePdfService.generateQuotePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(quote.quoteNumber() + ".pdf")
                .build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
