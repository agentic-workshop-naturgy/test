package com.naturgy.gas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tax_config")
public class TaxConfig {

    @Id
    @Column(name = "tax_code", length = 20)
    @NotBlank
    private String taxCode;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal taxRate;

    @Column(name = "vigencia_desde", nullable = false)
    @NotNull
    private LocalDate vigenciaDesde;

    public TaxConfig() {}

    public TaxConfig(String taxCode, BigDecimal taxRate, LocalDate vigenciaDesde) {
        this.taxCode = taxCode;
        this.taxRate = taxRate;
        this.vigenciaDesde = vigenciaDesde;
    }

    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public LocalDate getVigenciaDesde() { return vigenciaDesde; }
    public void setVigenciaDesde(LocalDate vigenciaDesde) { this.vigenciaDesde = vigenciaDesde; }
}
