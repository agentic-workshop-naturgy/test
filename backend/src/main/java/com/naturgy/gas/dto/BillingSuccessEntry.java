package com.naturgy.gas.dto;

import java.math.BigDecimal;

public class BillingSuccessEntry {
    private String cups;
    private String numeroFactura;
    private BigDecimal total;

    public BillingSuccessEntry() {}

    public BillingSuccessEntry(String cups, String numeroFactura, BigDecimal total) {
        this.cups = cups;
        this.numeroFactura = numeroFactura;
        this.total = total;
    }

    public String getCups() { return cups; }
    public void setCups(String cups) { this.cups = cups; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
