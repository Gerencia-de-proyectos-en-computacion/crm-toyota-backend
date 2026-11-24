package com.tricore.crmtoyota.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tricore.crmtoyota.domain.entity.Customer;
import com.tricore.crmtoyota.domain.entity.Quote;
import com.tricore.crmtoyota.domain.entity.QuoteItem;
import com.tricore.crmtoyota.domain.entity.Vehicle;
import com.tricore.crmtoyota.domain.entity.WorkshopSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class QuotePdfService {

    private static final Color TOYOTA_RED = new Color(220, 38, 38);
    private static final Color SLATE_DARK = new Color(15, 23, 42);
    private static final Color SLATE_LIGHT = new Color(241, 245, 249);
    private static final Color BORDER_GRAY = new Color(203, 213, 225);

    private final QuoteService quoteService;
    private final WorkshopSettingsService workshopSettingsService;

    public QuotePdfService(QuoteService quoteService, WorkshopSettingsService workshopSettingsService) {
        this.quoteService = quoteService;
        this.workshopSettingsService = workshopSettingsService;
    }

    @Transactional(readOnly = true)
    public byte[] generateQuotePdf(Long quoteId) {
        Quote quote = quoteService.getQuoteEntity(quoteId);
        WorkshopSettings ws = workshopSettingsService.getEntity();
        Customer customer = quote.getCustomer();
        Vehicle vehicle = quote.getVehicle();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, SLATE_DARK);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Font badgeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, SLATE_DARK);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, SLATE_DARK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, SLATE_DARK);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, TOYOTA_RED);

        // 1. Header White-Label + Quote Number Box
        PdfPTable header = new PdfPTable(new float[]{65f, 35f});
        header.setWidthPercentage(100);

        PdfPCell leftHeader = new PdfPCell();
        leftHeader.setBorder(Rectangle.NO_BORDER);
        leftHeader.addElement(new Paragraph(ws.getBusinessName(), titleFont));
        leftHeader.addElement(new Paragraph(ws.getSlogan(), subtitleFont));
        leftHeader.addElement(new Paragraph("RUC: " + ws.getRuc() + " | Tel: " + ws.getPhone(), subtitleFont));
        leftHeader.addElement(new Paragraph(ws.getAddress(), subtitleFont));
        header.addCell(leftHeader);

        PdfPCell rightHeader = new PdfPCell();
        rightHeader.setBackgroundColor(TOYOTA_RED);
        rightHeader.setBorderColor(TOYOTA_RED);
        rightHeader.setPadding(10f);
        rightHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph pType = new Paragraph("PROFORMA COMERCIAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE));
        pType.setAlignment(Element.ALIGN_CENTER);
        Paragraph pNum = new Paragraph(quote.getQuoteNumber(), badgeFont);
        pNum.setAlignment(Element.ALIGN_CENTER);
        Paragraph pStat = new Paragraph("Estado: " + quote.getStatus().name(), FontFactory.getFont(FontFactory.HELVETICA, 8, Color.WHITE));
        pStat.setAlignment(Element.ALIGN_CENTER);
        rightHeader.addElement(pType);
        rightHeader.addElement(pNum);
        rightHeader.addElement(pStat);
        header.addCell(rightHeader);

        doc.add(header);
        doc.add(Chunk.NEWLINE);

        // 2. Customer & Vehicle Grid
        PdfPTable meta = new PdfPTable(new float[]{50f, 50f});
        meta.setWidthPercentage(100);

        PdfPCell custBox = new PdfPCell();
        custBox.setBackgroundColor(SLATE_LIGHT);
        custBox.setBorderColor(BORDER_GRAY);
        custBox.setPadding(8f);
        custBox.addElement(new Paragraph("DATOS DEL CLIENTE", sectionFont));
        custBox.addElement(new Paragraph("Titular: " + customer.getFullName(), bodyFont));
        custBox.addElement(new Paragraph(customer.getDocumentType() + ": " + customer.getDocumentNumber(), bodyFont));
        custBox.addElement(new Paragraph("Teléfono: " + customer.getPhone(), bodyFont));
        meta.addCell(custBox);

        PdfPCell vehBox = new PdfPCell();
        vehBox.setBackgroundColor(SLATE_LIGHT);
        vehBox.setBorderColor(BORDER_GRAY);
        vehBox.setPadding(8f);
        vehBox.addElement(new Paragraph("VEHÍCULO TOYOTA & VIGENCIA", sectionFont));
        vehBox.addElement(new Paragraph("Placa: " + vehicle.getLicensePlate() + " | Toyota " + vehicle.getModel() + " " + vehicle.getTrimVersion() + " (" + vehicle.getManufactureYear() + ")", bodyFont));
        vehBox.addElement(new Paragraph("Kilometraje registrado: " + String.format("%,d km", quote.getMileageAtQuote()), bodyFont));
        vehBox.addElement(new Paragraph("Válida hasta: " + quote.getValidUntil().format(DateTimeFormatter.ISO_LOCAL_DATE) + " (7 días calendario)", boldFont));
        meta.addCell(vehBox);

        doc.add(meta);
        doc.add(Chunk.NEWLINE);

        // 3. Items Table
        PdfPTable table = new PdfPTable(new float[]{18f, 44f, 10f, 14f, 14f});
        table.setWidthPercentage(100);
        addHeaderCell(table, "CÓDIGO", boldFont);
        addHeaderCell(table, "DESCRIPCIÓN DE REPUESTO / MANO DE OBRA", boldFont);
        addHeaderCell(table, "CANT.", boldFont);
        addHeaderCell(table, "P. UNIT (S/)", boldFont);
        addHeaderCell(table, "IMPORTE (S/)", boldFont);

        for (QuoteItem item : quote.getItems()) {
            addBodyCell(table, item.getItemCode(), bodyFont, Element.ALIGN_LEFT);
            addBodyCell(table, item.getDescription(), bodyFont, Element.ALIGN_LEFT);
            addBodyCell(table, String.valueOf(item.getQuantity()), bodyFont, Element.ALIGN_CENTER);
            addBodyCell(table, formatMoney(item.getUnitPriceIgv()), bodyFont, Element.ALIGN_RIGHT);
            addBodyCell(table, formatMoney(item.getLineTotalIgv()), boldFont, Element.ALIGN_RIGHT);
        }
        doc.add(table);
        doc.add(Chunk.NEWLINE);

        // 4. Totals Box (IGV 18% Incluido Desglosado Informativamente)
        PdfPTable totalsWrapper = new PdfPTable(new float[]{55f, 45f});
        totalsWrapper.setWidthPercentage(100);

        PdfPCell legalCell = new PdfPCell();
        legalCell.setBorder(Rectangle.NO_BORDER);
        legalCell.setPaddingRight(12f);
        legalCell.addElement(new Paragraph("Atendido por: " + quote.getCreatedBy().getFullName(), subtitleFont));
        if (quote.getNotes() != null && !quote.getNotes().isBlank()) {
            legalCell.addElement(new Paragraph("Observaciones: " + quote.getNotes(), subtitleFont));
        }
        legalCell.addElement(Chunk.NEWLINE);
        legalCell.addElement(new Paragraph(
                "AVISO LEGAL: Proforma comercial válida por 7 días calendario y sujeta a disponibilidad física al momento de su aprobación. "
                        + "Los precios expresados en Soles (S/) incluyen el 18% de IGV. Documento informativo sin valor tributario — No reemplaza comprobante de pago electrónico SUNAT.",
                subtitleFont
        ));
        totalsWrapper.addCell(legalCell);

        PdfPTable totals = new PdfPTable(new float[]{60f, 40f});
        addTotalRow(totals, "Importe Bruto (inc. IGV):", "S/ " + formatMoney(quote.getGrossTotalIgv()), bodyFont);
        if (quote.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(totals, "Descuento (" + quote.getDiscountPercent() + "%):", "- S/ " + formatMoney(quote.getDiscountAmount()), bodyFont);
        }
        addTotalRow(totals, "Op. Gravada (Base Imponible):", "S/ " + formatMoney(quote.getTaxableSubtotal()), bodyFont);
        addTotalRow(totals, "IGV (18% incluido):", "S/ " + formatMoney(quote.getIgvAmount()), bodyFont);
        addTotalRow(totals, "TOTAL A PAGAR:", "S/ " + formatMoney(quote.getFinalTotalIgv()), totalFont);

        PdfPCell totalsContainer = new PdfPCell(totals);
        totalsContainer.setBorderColor(BORDER_GRAY);
        totalsContainer.setPadding(6f);
        totalsWrapper.addCell(totalsContainer);

        doc.add(totalsWrapper);
        doc.close();
        return out.toByteArray();
    }

    private void addHeaderCell(PdfPTable table, String label, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(label, font));
        cell.setBackgroundColor(SLATE_LIGHT);
        cell.setBorderColor(BORDER_GRAY);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorderColor(BORDER_GRAY);
        cell.setPadding(6f);
        cell.setHorizontalAlignment(align);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell l = new PdfPCell(new Phrase(label, font));
        l.setBorder(Rectangle.NO_BORDER);
        l.setPadding(4f);
        table.addCell(l);

        PdfPCell r = new PdfPCell(new Phrase(value, font));
        r.setBorder(Rectangle.NO_BORDER);
        r.setPadding(4f);
        r.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(r);
    }

    private String formatMoney(BigDecimal val) {
        return String.format("%,.2f", val);
    }
}
