package com.naturgy.gas.domain;

import com.naturgy.gas.domain.enums.EstadoSupplyPoint;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "supply_points")
public class SupplyPoint {

    @Id
    @Column(name = "cups", length = 50)
    @NotBlank
    private String cups;

    @Column(name = "zona", nullable = false, length = 50)
    @NotBlank
    private String zona;

    @Column(name = "tarifa", nullable = false, length = 20)
    @NotBlank
    private String tarifa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    @NotNull
    private EstadoSupplyPoint estado;

    public SupplyPoint() {}

    public SupplyPoint(String cups, String zona, String tarifa, EstadoSupplyPoint estado) {
        this.cups = cups;
        this.zona = zona;
        this.tarifa = tarifa;
        this.estado = estado;
    }

    public String getCups() { return cups; }
    public void setCups(String cups) { this.cups = cups; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public String getTarifa() { return tarifa; }
    public void setTarifa(String tarifa) { this.tarifa = tarifa; }

    public EstadoSupplyPoint getEstado() { return estado; }
    public void setEstado(EstadoSupplyPoint estado) { this.estado = estado; }
}
