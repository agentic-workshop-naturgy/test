package com.naturgy.gas.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.naturgy.gas.domain.Invoice;
import com.naturgy.gas.domain.InvoiceLine;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Generates PDF invoice using iText 7.
 * Layout mirrors factura_gas_demo.pdf structure:
 *   header, invoice data, consumption summary, line breakdown, totals.
 */
@Service
public class InvoicePdfService {

    public byte[] generatePdf(Invoice invoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
             Document doc = new Document(pdfDoc)) {

            // ── Header ────────────────────────────────────────────────────────
            doc.add(new Paragraph("FACTURA DE GAS NATURAL")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Naturgy Energy Group, S.A.")
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY));
            doc.add(new Paragraph(" "));

            // ── Invoice data table ────────────────────────────────────────────
            Table info = new Table(UnitValue.createPercentArray(new float[]{3, 5}))
                    .setWidth(UnitValue.createPercentValue(100));

            addInfoRow(info, "Número de factura:", invoice.getNumeroFactura());
            addInfoRow(info, "CUPS:", invoice.getCups());
            addInfoRow(info, "Período de facturación:",
                    invoice.getPeriodoInicio() + "  →  " + invoice.getPeriodoFin());
            addInfoRow(info, "Fecha de emisión:", invoice.getFechaEmision().toString());
            doc.add(info);
            doc.add(new Paragraph(" "));

            // ── Lines breakdown table ─────────────────────────────────────────
            doc.add(new Paragraph("Desglose de conceptos")
                    .setFontSize(13)
                    .setBold());

            Table lines = new Table(UnitValue.createPercentArray(new float[]{5, 2, 2, 2}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Header row
            addHeaderCell(lines, "Descripción");
            addHeaderCell(lines, "Cantidad");
            addHeaderCell(lines, "Precio unit.");
            addHeaderCell(lines, "Importe (€)");

            // Data rows
            for (InvoiceLine line : invoice.getLines()) {
                lines.addCell(new Cell().add(new Paragraph(line.getDescripcion())));
                lines.addCell(new Cell().add(
                        new Paragraph(line.getCantidad().toPlainString())
                                .setTextAlignment(TextAlignment.RIGHT)));
                lines.addCell(new Cell().add(
                        new Paragraph(fmt6(line.getPrecioUnitario()))
                                .setTextAlignment(TextAlignment.RIGHT)));
                lines.addCell(new Cell().add(
                        new Paragraph(fmt2(line.getImporte()))
                                .setTextAlignment(TextAlignment.RIGHT)));
            }
            doc.add(lines);
            doc.add(new Paragraph(" "));

            // ── Totals ────────────────────────────────────────────────────────
            Table totals = new Table(UnitValue.createPercentArray(new float[]{6, 2}))
                    .setWidth(UnitValue.createPercentValue(60))
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

            addTotalRow(totals, "Base imponible:", fmt2(invoice.getBase()));
            addTotalRow(totals, "Impuestos (IVA):", fmt2(invoice.getImpuestos()));

            // Bold total row
            Cell totalLabel = new Cell().add(new Paragraph("TOTAL A PAGAR:").setBold());
            Cell totalValue = new Cell().add(
                    new Paragraph(fmt2(invoice.getTotal()) + " €").setBold()
                            .setTextAlignment(TextAlignment.RIGHT));
            totals.addCell(totalLabel);
            totals.addCell(totalValue);

            doc.add(totals);
            doc.add(new Paragraph(" "));

            // ── Footer ────────────────────────────────────────────────────────
            doc.add(new Paragraph("Este documento es una factura oficial. Conserve este documento.")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF for invoice " + invoice.getNumeroFactura(), e);
        }

        return baos.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addInfoRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(value)).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
    }

    private void addHeaderCell(Table table, String text) {
        table.addHeaderCell(new Cell().add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY));
    }

    private void addTotalRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)));
        table.addCell(new Cell().add(new Paragraph(value + " €")
                .setTextAlignment(TextAlignment.RIGHT)));
    }

    private String fmt2(BigDecimal bd) {
        return bd == null ? "0.00" : bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String fmt6(BigDecimal bd) {
        if (bd == null) return "0.000000";
        // Strip trailing zeros but keep at least 4 decimal places
        String plain = bd.stripTrailingZeros().toPlainString();
        if (!plain.contains(".")) return plain;
        String[] parts = plain.split("\\.");
        String decimals = parts[1];
        while (decimals.length() < 4) decimals += "0";
        return parts[0] + "." + decimals;
    }
}
