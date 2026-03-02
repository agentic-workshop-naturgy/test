package com.naturgy.gas.dto;

public class BillingErrorEntry {
    private String cups;
    private String reason;

    public BillingErrorEntry() {}

    public BillingErrorEntry(String cups, String reason) {
        this.cups = cups;
        this.reason = reason;
    }

    public String getCups() { return cups; }
    public void setCups(String cups) { this.cups = cups; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
