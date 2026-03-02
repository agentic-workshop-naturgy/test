package com.naturgy.gas.domain;

import com.naturgy.gas.domain.enums.TipoLectura;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "gas_readings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"cups", "fecha"}))
public class GasReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cups", nullable = false, length = 50)
    @NotNull
    private String cups;

    @Column(name = "fecha", nullable = false)
    @NotNull
    private LocalDate fecha;

    @Column(name = "lectura_m3", nullable = false, precision = 12, scale = 3)
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal lecturaM3;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    @NotNull
    private TipoLectura tipo;

    public GasReading() {}

    public GasReading(String cups, LocalDate fecha, BigDecimal lecturaM3, TipoLectura tipo) {
        this.cups = cups;
        this.fecha = fecha;
        this.lecturaM3 = lecturaM3;
        this.tipo = tipo;
    }

    public Long getId() { return id; }

    public String getCups() { return cups; }
    public void setCups(String cups) { this.cups = cups; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getLecturaM3() { return lecturaM3; }
    public void setLecturaM3(BigDecimal lecturaM3) { this.lecturaM3 = lecturaM3; }

    public TipoLectura getTipo() { return tipo; }
    public void setTipo(TipoLectura tipo) { this.tipo = tipo; }
}
