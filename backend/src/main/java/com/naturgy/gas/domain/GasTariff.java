package com.naturgy.gas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "gas_tariffs")
public class GasTariff {

    @Id
    @Column(name = "tarifa", length = 20)
    @NotBlank
    private String tarifa;

    @Column(name = "fijo_mes_eur", nullable = false, precision = 10, scale = 4)
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal fijoMesEur;

    @Column(name = "variable_eur_kwh", nullable = false, precision = 10, scale = 6)
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal variableEurKwh;

    @Column(name = "vigencia_desde", nullable = false)
    @NotNull
    private LocalDate vigenciaDesde;

    public GasTariff() {}

    public GasTariff(String tarifa, BigDecimal fijoMesEur, BigDecimal variableEurKwh, LocalDate vigenciaDesde) {
        this.tarifa = tarifa;
        this.fijoMesEur = fijoMesEur;
        this.variableEurKwh = variableEurKwh;
        this.vigenciaDesde = vigenciaDesde;
    }

    public String getTarifa() { return tarifa; }
    public void setTarifa(String tarifa) { this.tarifa = tarifa; }

    public BigDecimal getFijoMesEur() { return fijoMesEur; }
    public void setFijoMesEur(BigDecimal fijoMesEur) { this.fijoMesEur = fijoMesEur; }

    public BigDecimal getVariableEurKwh() { return variableEurKwh; }
    public void setVariableEurKwh(BigDecimal variableEurKwh) { this.variableEurKwh = variableEurKwh; }

    public LocalDate getVigenciaDesde() { return vigenciaDesde; }
    public void setVigenciaDesde(LocalDate vigenciaDesde) { this.vigenciaDesde = vigenciaDesde; }
}
