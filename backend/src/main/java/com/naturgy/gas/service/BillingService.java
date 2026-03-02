package com.naturgy.gas.service;

import com.naturgy.gas.domain.*;
import com.naturgy.gas.domain.enums.EstadoSupplyPoint;
import com.naturgy.gas.domain.enums.TipoLinea;
import com.naturgy.gas.dto.BillingErrorEntry;
import com.naturgy.gas.dto.BillingResultDto;
import com.naturgy.gas.dto.BillingSuccessEntry;
import com.naturgy.gas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core billing logic.  SSOT: _data/specs/gas_logic-spec.txt
 */
@Service
public class BillingService {

    private final SupplyPointRepository supplyPointRepo;
    private final GasReadingRepository gasReadingRepo;
    private final GasTariffRepository gasTariffRepo;
    private final GasConversionFactorRepository conversionFactorRepo;
    private final TaxConfigRepository taxConfigRepo;
    private final InvoiceRepository invoiceRepo;

    public BillingService(SupplyPointRepository supplyPointRepo,
                          GasReadingRepository gasReadingRepo,
                          GasTariffRepository gasTariffRepo,
                          GasConversionFactorRepository conversionFactorRepo,
                          TaxConfigRepository taxConfigRepo,
                          InvoiceRepository invoiceRepo) {
        this.supplyPointRepo = supplyPointRepo;
        this.gasReadingRepo = gasReadingRepo;
        this.gasTariffRepo = gasTariffRepo;
        this.conversionFactorRepo = conversionFactorRepo;
        this.taxConfigRepo = taxConfigRepo;
        this.invoiceRepo = invoiceRepo;
    }

    /**
     * Run billing for a given period (YYYY-MM).
     * Idempotent: existing invoice for (cups, periodStart) is replaced.
     */
    @Transactional
    public BillingResultDto runBilling(String period) {
        YearMonth ym = YearMonth.parse(period);
        LocalDate periodStart = ym.atDay(1);
        LocalDate periodEnd   = ym.atEndOfMonth();
        String yyyymm         = ym.format(DateTimeFormatter.ofPattern("yyyyMM"));
        int daysInMonth       = ym.lengthOfMonth();

        List<BillingSuccessEntry> successes = new ArrayList<>();
        List<BillingErrorEntry>   errors    = new ArrayList<>();

        List<SupplyPoint> activePoints = supplyPointRepo.findByEstado(EstadoSupplyPoint.ACTIVO);

        for (SupplyPoint sp : activePoints) {
            String cups = sp.getCups();

            // ── 1. Boundary readings ─────────────────────────────────────────
            Optional<GasReading> inicioOpt = gasReadingRepo.findLastReadingBefore(cups, periodStart);
            if (inicioOpt.isEmpty()) {
                errors.add(new BillingErrorEntry(cups,
                        "Missing lectura_inicio: no reading strictly before " + periodStart));
                continue;
            }

            Optional<GasReading> finOpt = gasReadingRepo.findLastReadingOnOrBefore(cups, periodEnd);
            if (finOpt.isEmpty()) {
                errors.add(new BillingErrorEntry(cups,
                        "Missing lectura_fin: no reading on or before " + periodEnd));
                continue;
            }

            GasReading inicio = inicioOpt.get();
            GasReading fin    = finOpt.get();

            // ── 2. m³ consumed ───────────────────────────────────────────────
            BigDecimal m3 = fin.getLecturaM3().subtract(inicio.getLecturaM3());
            if (m3.compareTo(BigDecimal.ZERO) < 0) {
                errors.add(new BillingErrorEntry(cups,
                        "Negative consumption: " + m3 + " m3 (inicio=" + inicio.getLecturaM3()
                        + ", fin=" + fin.getLecturaM3() + ")"));
                continue;
            }

            // ── 3. Tariff ────────────────────────────────────────────────────
            Optional<GasTariff> tariffOpt = gasTariffRepo.findEffectiveTariff(sp.getTarifa(), periodEnd);
            if (tariffOpt.isEmpty()) {
                errors.add(new BillingErrorEntry(cups,
                        "No effective tariff for tarifa=" + sp.getTarifa() + " on " + periodEnd));
                continue;
            }
            GasTariff tariff = tariffOpt.get();

            // ── 4. Conversion factor ─────────────────────────────────────────
            Optional<GasConversionFactor> factorOpt =
                    conversionFactorRepo.findByZonaAndMes(sp.getZona(), period);
            if (factorOpt.isEmpty()) {
                errors.add(new BillingErrorEntry(cups,
                        "No conversion factor for zona=" + sp.getZona() + " mes=" + period));
                continue;
            }
            GasConversionFactor factor = factorOpt.get();

            // ── 5. IVA ───────────────────────────────────────────────────────
            Optional<TaxConfig> taxOpt = taxConfigRepo.findEffectiveTax("IVA", periodEnd);
            if (taxOpt.isEmpty()) {
                errors.add(new BillingErrorEntry(cups,
                        "No effective IVA tax config on " + periodEnd));
                continue;
            }
            TaxConfig tax = taxOpt.get();

            // ── 6. Calculations (SSOT: gas_logic-spec.txt) ───────────────────
            // kWh = m3 * coef_conv * pcs_kwh_m3  (scale=3)
            BigDecimal kwh = m3
                    .multiply(factor.getCoefConv())
                    .multiply(factor.getPcsKwhM3())
                    .setScale(3, RoundingMode.HALF_UP);

            // coste_fijo = fijo_mes_eur * (days_in_period / days_in_month) → scale=2
            long daysInPeriod    = periodStart.until(periodEnd.plusDays(1), java.time.temporal.ChronoUnit.DAYS);
            BigDecimal costeFijo = tariff.getFijoMesEur()
                    .multiply(new BigDecimal(daysInPeriod))
                    .divide(new BigDecimal(daysInMonth), 2, RoundingMode.HALF_UP);

            // coste_variable = kwh * variable_eur_kwh  → scale=2
            BigDecimal costeVariable = kwh
                    .multiply(tariff.getVariableEurKwh())
                    .setScale(2, RoundingMode.HALF_UP);

            // alquiler defaults to 0.00 (workshop default per logic-spec)
            BigDecimal alquiler = BigDecimal.ZERO.setScale(2);

            // base, impuestos, total  → scale=2
            BigDecimal base      = costeFijo.add(costeVariable).add(alquiler)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal impuestos = base.multiply(tax.getTaxRate())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal total     = base.add(impuestos)
                    .setScale(2, RoundingMode.HALF_UP);

            // ── 7. Idempotency: replace existing invoice ──────────────────────
            invoiceRepo.findByCupsAndPeriodoInicio(cups, periodStart).ifPresent(existing -> {
                invoiceRepo.delete(existing);
                invoiceRepo.flush();
            });

            // ── 8. Sequence number ────────────────────────────────────────────
            long count         = invoiceRepo.countByPeriodoInicio(periodStart);
            String seq         = String.format("%03d", count + 1);
            String numeroFactura = "GAS-" + yyyymm + "-" + cups + "-" + seq;

            // ── 9. Build invoice ──────────────────────────────────────────────
            Invoice invoice = new Invoice();
            invoice.setNumeroFactura(numeroFactura);
            invoice.setCups(cups);
            invoice.setPeriodoInicio(periodStart);
            invoice.setPeriodoFin(periodEnd);
            invoice.setBase(base);
            invoice.setImpuestos(impuestos);
            invoice.setTotal(total);
            invoice.setFechaEmision(LocalDate.now());

            List<InvoiceLine> lines = buildLines(invoice, costeFijo, kwh, tariff,
                    alquiler, base, impuestos, tax);
            invoice.setLines(lines);

            invoiceRepo.save(invoice);
            successes.add(new BillingSuccessEntry(cups, numeroFactura, total));
        }

        return new BillingResultDto(period, successes, errors);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Compute kWh from m³, coef_conv, pcs_kwh_m3. Scale=3 HALF_UP. */
    public static BigDecimal computeKwh(BigDecimal m3,
                                         BigDecimal coefConv,
                                         BigDecimal pcsKwhM3) {
        return m3.multiply(coefConv).multiply(pcsKwhM3)
                .setScale(3, RoundingMode.HALF_UP);
    }

    /** Compute coste_variable = kwh * variable_eur_kwh. Scale=2 HALF_UP. */
    public static BigDecimal computeCosteVariable(BigDecimal kwh, BigDecimal variableEurKwh) {
        return kwh.multiply(variableEurKwh).setScale(2, RoundingMode.HALF_UP);
    }

    /** Compute base = coste_fijo + coste_variable + alquiler. Scale=2. */
    public static BigDecimal computeBase(BigDecimal costeFijo,
                                          BigDecimal costeVariable,
                                          BigDecimal alquiler) {
        return costeFijo.add(costeVariable).add(alquiler)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Compute impuestos = base * rate. Scale=2 HALF_UP. */
    public static BigDecimal computeImpuestos(BigDecimal base, BigDecimal rate) {
        return base.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private List<InvoiceLine> buildLines(Invoice invoice,
                                          BigDecimal costeFijo,
                                          BigDecimal kwh,
                                          GasTariff tariff,
                                          BigDecimal alquiler,
                                          BigDecimal base,
                                          BigDecimal impuestos,
                                          TaxConfig tax) {
        List<InvoiceLine> lines = new ArrayList<>();

        // TERMINO_FIJO
        InvoiceLine lFijo = new InvoiceLine();
        lFijo.setInvoice(invoice);
        lFijo.setTipo(TipoLinea.TERMINO_FIJO);
        lFijo.setDescripcion("Término fijo");
        lFijo.setCantidad(BigDecimal.ONE.setScale(3));
        lFijo.setPrecioUnitario(costeFijo.setScale(6, RoundingMode.HALF_UP));
        lFijo.setImporte(costeFijo);
        lines.add(lFijo);

        // TERMINO_VARIABLE
        InvoiceLine lVar = new InvoiceLine();
        lVar.setInvoice(invoice);
        lVar.setTipo(TipoLinea.TERMINO_VARIABLE);
        lVar.setDescripcion("Término variable");
        lVar.setCantidad(kwh);
        lVar.setPrecioUnitario(tariff.getVariableEurKwh().setScale(6, RoundingMode.HALF_UP));
        lVar.setImporte(computeCosteVariable(kwh, tariff.getVariableEurKwh()));
        lines.add(lVar);

        // ALQUILER (only if > 0)
        if (alquiler.compareTo(BigDecimal.ZERO) > 0) {
            InvoiceLine lAlq = new InvoiceLine();
            lAlq.setInvoice(invoice);
            lAlq.setTipo(TipoLinea.ALQUILER);
            lAlq.setDescripcion("Alquiler contador");
            lAlq.setCantidad(BigDecimal.ONE.setScale(3));
            lAlq.setPrecioUnitario(alquiler.setScale(6, RoundingMode.HALF_UP));
            lAlq.setImporte(alquiler);
            lines.add(lAlq);
        }

        // IVA
        int ivaPercent = tax.getTaxRate().multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP).intValue();
        InvoiceLine lIva = new InvoiceLine();
        lIva.setInvoice(invoice);
        lIva.setTipo(TipoLinea.IVA);
        lIva.setDescripcion("IVA " + ivaPercent + "%");
        lIva.setCantidad(tax.getTaxRate().setScale(3, RoundingMode.HALF_UP));
        lIva.setPrecioUnitario(base.setScale(6, RoundingMode.HALF_UP));
        lIva.setImporte(impuestos);
        lines.add(lIva);

        return lines;
    }
}
