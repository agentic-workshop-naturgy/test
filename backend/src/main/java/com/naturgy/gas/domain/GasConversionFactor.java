package com.naturgy.gas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "gas_conversion_factors",
       uniqueConstraints = @UniqueConstraint(columnNames = {"zona", "mes"}))
public class GasConversionFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zona", nullable = false, length = 50)
    @NotBlank
    private String zona;

    @Column(name = "mes", nullable = false, length = 7)
    @NotBlank
    private String mes;

    @Column(name = "coef_conv", nullable = false, precision = 10, scale = 5)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal coefConv;

    @Column(name = "pcs_kwh_m3", nullable = false, precision = 10, scale = 5)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pcsKwhM3;

    public GasConversionFactor() {}

    public GasConversionFactor(String zona, String mes, BigDecimal coefConv, BigDecimal pcsKwhM3) {
        this.zona = zona;
        this.mes = mes;
        this.coefConv = coefConv;
        this.pcsKwhM3 = pcsKwhM3;
    }

    public Long getId() { return id; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public String getMes() { return mes; }
    public void setMes(String mes) { this.mes = mes; }

    public BigDecimal getCoefConv() { return coefConv; }
    public void setCoefConv(BigDecimal coefConv) { this.coefConv = coefConv; }

    public BigDecimal getPcsKwhM3() { return pcsKwhM3; }
    public void setPcsKwhM3(BigDecimal pcsKwhM3) { this.pcsKwhM3 = pcsKwhM3; }
}
