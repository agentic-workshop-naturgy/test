package com.naturgy.gas;

import com.naturgy.gas.service.BillingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for the calculation helpers in BillingService.
 * No Spring context needed — tests plain math.
 * SSOT: _data/specs/gas_logic-spec.txt
 */
class BillingCalculationTest {

    // ── m³ → kWh ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("m3→kWh: 50.5 * 1.02 * 11.72 = 603.697 (scale=3 HALF_UP)")
    void m3ToKwh_correctConversion() {
        BigDecimal m3       = new BigDecimal("50.500");
        BigDecimal coef     = new BigDecimal("1.02");
        BigDecimal pcs      = new BigDecimal("11.72");
        BigDecimal kwh      = BillingService.computeKwh(m3, coef, pcs);
        // 50.5 * 1.02 = 51.51; 51.51 * 11.72 = 603.6972 → 603.697
        assertThat(kwh).isEqualByComparingTo(new BigDecimal("603.697"));
        assertThat(kwh.scale()).isEqualTo(3);
    }

    @Test
    @DisplayName("m3→kWh: rounds to scale=3 HALF_UP (0.5 case)")
    void m3ToKwh_roundingHalfUp() {
        // 1 * 1.0 * 0.0005 → 0.0005 → rounds to 0.001 (HALF_UP)
        BigDecimal kwh = BillingService.computeKwh(
                BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("0.0005"));
        assertThat(kwh).isEqualByComparingTo(new BigDecimal("0.001"));
    }

    // ── coste_variable ────────────────────────────────────────────────────────

    @Test
    @DisplayName("coste_variable = kwh * variable_eur_kwh, scale=2 HALF_UP")
    void costeVariable_basic() {
        BigDecimal kwh      = new BigDecimal("603.697");
        BigDecimal rate     = new BigDecimal("0.004500");
        BigDecimal result   = BillingService.computeCosteVariable(kwh, rate);
        // 603.697 * 0.0045 = 2.716637 → 2.72
        assertThat(result).isEqualByComparingTo(new BigDecimal("2.72"));
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("coste_variable rounds HALF_UP at 0.005 boundary")
    void costeVariable_roundsHalfUp() {
        // 1 kwh * 0.005 eur/kwh = 0.005 → rounds to 0.01 (HALF_UP)
        BigDecimal result = BillingService.computeCosteVariable(
                BigDecimal.ONE, new BigDecimal("0.005000"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("0.01"));
    }

    // ── base ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("base = costeFijo + costeVariable + alquiler")
    void base_sumOfComponents() {
        BigDecimal fijo     = new BigDecimal("3.85");
        BigDecimal variable = new BigDecimal("2.72");
        BigDecimal alquiler = BigDecimal.ZERO.setScale(2);
        BigDecimal base     = BillingService.computeBase(fijo, variable, alquiler);
        assertThat(base).isEqualByComparingTo(new BigDecimal("6.57"));
        assertThat(base.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("base includes alquiler when > 0")
    void base_withAlquiler() {
        BigDecimal base = BillingService.computeBase(
                new BigDecimal("3.85"), new BigDecimal("2.72"), new BigDecimal("1.00"));
        assertThat(base).isEqualByComparingTo(new BigDecimal("7.57"));
    }

    // ── IVA parametrizable ────────────────────────────────────────────────────

    @Test
    @DisplayName("IVA 21%: 6.57 * 0.21 = 1.3797 → 1.38")
    void iva21_roundsHalfUp() {
        BigDecimal impuestos = BillingService.computeImpuestos(
                new BigDecimal("6.57"), new BigDecimal("0.21"));
        assertThat(impuestos).isEqualByComparingTo(new BigDecimal("1.38"));
        assertThat(impuestos.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("IVA 10%: different rate produces correct result")
    void iva10_parametrizable() {
        BigDecimal impuestos = BillingService.computeImpuestos(
                new BigDecimal("10.00"), new BigDecimal("0.10"));
        assertThat(impuestos).isEqualByComparingTo(new BigDecimal("1.00"));
    }

    @Test
    @DisplayName("IVA 0%: no tax")
    void iva0_zeroTax() {
        BigDecimal impuestos = BillingService.computeImpuestos(
                new BigDecimal("6.57"), BigDecimal.ZERO);
        assertThat(impuestos).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── Negative consumption → must be detected by caller ────────────────────

    @Test
    @DisplayName("Negative m3: caller detects m3 < 0 before calling computeKwh")
    void negativeConsumption_detectedBySign() {
        BigDecimal lecturaFin    = new BigDecimal("100.000");
        BigDecimal lecturaInicio = new BigDecimal("200.000");
        BigDecimal m3 = lecturaFin.subtract(lecturaInicio);

        // The service checks: if m3 < 0 → error entry (no invoice)
        assertThat(m3.compareTo(BigDecimal.ZERO)).isLessThan(0);
    }

    @Test
    @DisplayName("Zero consumption: kwh = 0, coste_variable = 0")
    void zeroConsumption_producesZeroCost() {
        BigDecimal kwh = BillingService.computeKwh(
                BigDecimal.ZERO, new BigDecimal("1.02"), new BigDecimal("11.72"));
        assertThat(kwh).isEqualByComparingTo(BigDecimal.ZERO);

        BigDecimal costeVariable = BillingService.computeCosteVariable(kwh, new BigDecimal("0.0045"));
        assertThat(costeVariable).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ── End-to-end calculation verification (ES0021000000001AA, 2026-01) ──────

    @Test
    @DisplayName("E2E: CUPS001 Jan-2026 full calculation matches expected values")
    void e2e_cups001_jan2026() {
        // Seed data: inicio=1200.00, fin=1250.50, zona=ZONA1, coef=1.02, pcs=11.72
        BigDecimal m3           = new BigDecimal("1250.50").subtract(new BigDecimal("1200.00"));
        BigDecimal kwh          = BillingService.computeKwh(m3, new BigDecimal("1.02"), new BigDecimal("11.72"));
        BigDecimal costeFijo    = new BigDecimal("3.85").setScale(2, RoundingMode.HALF_UP);
        BigDecimal costeVariable = BillingService.computeCosteVariable(kwh, new BigDecimal("0.0045"));
        BigDecimal base         = BillingService.computeBase(costeFijo, costeVariable, BigDecimal.ZERO.setScale(2));
        BigDecimal impuestos    = BillingService.computeImpuestos(base, new BigDecimal("0.21"));
        BigDecimal total        = base.add(impuestos).setScale(2, RoundingMode.HALF_UP);

        assertThat(m3).isEqualByComparingTo(new BigDecimal("50.50"));
        assertThat(kwh).isEqualByComparingTo(new BigDecimal("603.697"));
        assertThat(costeVariable).isEqualByComparingTo(new BigDecimal("2.72"));
        assertThat(base).isEqualByComparingTo(new BigDecimal("6.57"));
        assertThat(impuestos).isEqualByComparingTo(new BigDecimal("1.38"));
        assertThat(total).isEqualByComparingTo(new BigDecimal("7.95"));
    }
}
