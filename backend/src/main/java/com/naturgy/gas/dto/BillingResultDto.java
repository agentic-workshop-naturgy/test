package com.naturgy.gas.dto;

import java.util.List;

public class BillingResultDto {
    private String period;
    private List<BillingSuccessEntry> successes;
    private List<BillingErrorEntry> errors;

    public BillingResultDto() {}

    public BillingResultDto(String period,
                            List<BillingSuccessEntry> successes,
                            List<BillingErrorEntry> errors) {
        this.period = period;
        this.successes = successes;
        this.errors = errors;
    }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public List<BillingSuccessEntry> getSuccesses() { return successes; }
    public void setSuccesses(List<BillingSuccessEntry> successes) { this.successes = successes; }

    public List<BillingErrorEntry> getErrors() { return errors; }
    public void setErrors(List<BillingErrorEntry> errors) { this.errors = errors; }
}
