package com.naturgy.gas.controller;

import com.naturgy.gas.dto.BillingResultDto;
import com.naturgy.gas.service.BillingService;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * POST /api/gas/billing/{period}   e.g. period = "2026-01"
 */
@RestController
@RequestMapping("/api/gas/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/{period}")
    public BillingResultDto runBilling(@PathVariable String period) {
        validatePeriod(period);
        return billingService.runBilling(period);
    }

    private void validatePeriod(String period) {
        try {
            YearMonth.parse(period);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid period format '" + period + "'. Expected YYYY-MM (e.g. 2026-01)");
        }
    }
}
