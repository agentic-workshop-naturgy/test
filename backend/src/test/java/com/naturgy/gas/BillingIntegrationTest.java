package com.naturgy.gas;

import com.naturgy.gas.domain.Invoice;
import com.naturgy.gas.dto.BillingResultDto;
import com.naturgy.gas.repository.InvoiceRepository;
import com.naturgy.gas.service.BillingService;
import com.naturgy.gas.service.InvoicePdfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: seed → run billing for 2026-01 → verify invoices → verify PDF bytes.
 * SSOT: _data/specs/gas_logic-spec.txt
 *
 * @Transactional ensures the Hibernate session stays open so lazy collections
 * (invoice.lines) can be accessed within the same test transaction.
 * Each test method rolls back after completion; seed data (from DataSeeder) persists
 * for the lifetime of the shared application context.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BillingIntegrationTest {

    @Autowired
    private BillingService billingService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoicePdfService invoicePdfService;

    @Test
    void billingRun_createsInvoices() {
        BillingResultDto result = billingService.runBilling("2026-01");

        // All 3 ACTIVO CUPS in seed data have readings for 2026-01
        assertThat(result.getSuccesses())
                .as("Should have at least 1 successful invoice")
                .isNotEmpty();

        assertThat(result.getErrors())
                .as("No billing errors expected for 2026-01 seed data")
                .isEmpty();

        // Verify invoices are persisted
        List<Invoice> saved = invoiceRepository.findAll();
        assertThat(saved).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void billingRun_invoicesHaveLines() {
        billingService.runBilling("2026-01");

        List<Invoice> invoices = invoiceRepository.findAllWithLines();
        assertThat(invoices).isNotEmpty();

        Invoice first = invoices.get(0);
        assertThat(first.getLines())
                .as("Invoice must have at least 3 lines (FIJO, VARIABLE, IVA)")
                .hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void billingRun_invoiceFieldsPopulated() {
        billingService.runBilling("2026-01");

        List<Invoice> invoices = invoiceRepository.findAll();
        Invoice inv = invoices.get(0);

        assertThat(inv.getNumeroFactura()).startsWith("GAS-202601-");
        assertThat(inv.getCups()).isNotBlank();
        assertThat(inv.getPeriodoInicio()).isNotNull();
        assertThat(inv.getPeriodoFin()).isNotNull();
        assertThat(inv.getBase()).isPositive();
        assertThat(inv.getImpuestos()).isPositive();
        assertThat(inv.getTotal()).isPositive();
        assertThat(inv.getFechaEmision()).isNotNull();
    }

    @Test
    void billingRun_idempotent_noDuplicates() {
        billingService.runBilling("2026-01");
        long countAfterFirst = invoiceRepository.count();

        // Run again — should replace, not add
        billingService.runBilling("2026-01");
        long countAfterSecond = invoiceRepository.count();

        assertThat(countAfterSecond).isEqualTo(countAfterFirst);
    }

    @Test
    void pdfGeneration_returnsBytesForInvoice() {
        billingService.runBilling("2026-01");

        // Use JOIN FETCH query so lines are eagerly loaded (avoids LazyInitializationException)
        Invoice invoice = invoiceRepository.findAllWithLines().get(0);
        byte[] pdf = invoicePdfService.generatePdf(invoice);

        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(0);

        // Basic PDF magic bytes check: %PDF
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }
}
