package com.naturgy.gas.controller;

import com.naturgy.gas.domain.Invoice;
import com.naturgy.gas.repository.InvoiceRepository;
import com.naturgy.gas.service.InvoicePdfService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * GET /api/gas/invoices
 * GET /api/gas/invoices/{id}
 * GET /api/gas/invoices/{id}/pdf
 */
@RestController
@RequestMapping("/api/gas/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepo;
    private final InvoicePdfService pdfService;

    public InvoiceController(InvoiceRepository invoiceRepo, InvoicePdfService pdfService) {
        this.invoiceRepo = invoiceRepo;
        this.pdfService  = pdfService;
    }

    /**
     * List invoices with optional filters:
     *   ?cups=ES0021000000001AA
     *   ?period=2026-01           (matches periodoInicio year+month)
     *   ?fechaEmision=2026-01-15
     */
    @GetMapping
    public List<Invoice> findAll(
            @RequestParam(required = false) String cups,
            @RequestParam(required = false) String period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEmision) {

        List<Invoice> all = invoiceRepo.findAllWithLines();

        if (cups != null && !cups.isBlank()) {
            all = all.stream().filter(i -> cups.equals(i.getCups())).collect(Collectors.toList());
        }
        if (period != null && !period.isBlank()) {
            YearMonth ym = parsePeriodOrThrow(period);
            all = all.stream()
                    .filter(i -> {
                        LocalDate pi = i.getPeriodoInicio();
                        return pi.getYear() == ym.getYear() && pi.getMonth() == ym.getMonth();
                    })
                    .collect(Collectors.toList());
        }
        if (fechaEmision != null) {
            all = all.stream()
                    .filter(i -> fechaEmision.equals(i.getFechaEmision()))
                    .collect(Collectors.toList());
        }
        return all;
    }

    @GetMapping("/{id}")
    public Invoice findById(@PathVariable Long id) {
        return invoiceRepo.findByIdWithLines(id)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found: " + id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        Invoice invoice = invoiceRepo.findByIdWithLines(id)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found: " + id));

        byte[] pdfBytes = pdfService.generatePdf(invoice);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"factura-" + invoice.getNumeroFactura() + ".pdf\"")
                .body(pdfBytes);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private YearMonth parsePeriodOrThrow(String period) {
        try {
            return YearMonth.parse(period);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid period format '" + period + "'. Expected YYYY-MM");
        }
    }
}
