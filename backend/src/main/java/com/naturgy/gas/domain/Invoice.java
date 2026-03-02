package com.naturgy.gas.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices",
       uniqueConstraints = @UniqueConstraint(columnNames = {"cups", "periodo_inicio"}))
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_factura", nullable = false, unique = true, length = 60)
    @NotBlank
    private String numeroFactura;

    @Column(name = "cups", nullable = false, length = 50)
    @NotBlank
    private String cups;

    @Column(name = "periodo_inicio", nullable = false)
    @NotNull
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    @NotNull
    private LocalDate periodoFin;

    @Column(name = "base", nullable = false, precision = 12, scale = 2)
    @NotNull
    private BigDecimal base;

    @Column(name = "impuestos", nullable = false, precision = 12, scale = 2)
    @NotNull
    private BigDecimal impuestos;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    @NotNull
    private BigDecimal total;

    @Column(name = "fecha_emision", nullable = false)
    @NotNull
    private LocalDate fechaEmision;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLine> lines = new ArrayList<>();

    public Invoice() {}

    public Long getId() { return id; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public String getCups() { return cups; }
    public void setCups(String cups) { this.cups = cups; }

    public LocalDate getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(LocalDate periodoInicio) { this.periodoInicio = periodoInicio; }

    public LocalDate getPeriodoFin() { return periodoFin; }
    public void setPeriodoFin(LocalDate periodoFin) { this.periodoFin = periodoFin; }

    public BigDecimal getBase() { return base; }
    public void setBase(BigDecimal base) { this.base = base; }

    public BigDecimal getImpuestos() { return impuestos; }
    public void setImpuestos(BigDecimal impuestos) { this.impuestos = impuestos; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public List<InvoiceLine> getLines() { return lines; }
    public void setLines(List<InvoiceLine> lines) { this.lines = lines; }
}
