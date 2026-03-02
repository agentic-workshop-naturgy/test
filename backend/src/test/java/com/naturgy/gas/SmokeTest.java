package com.naturgy.gas;

import com.naturgy.gas.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test: boot + seed + basic row counts.
 * The DataSeeder reads CSVs relative to the working directory (_data/db/samples/).
 * When running from backend/ directory, paths resolve correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class SmokeTest {

    @Autowired
    private SupplyPointRepository supplyPointRepo;

    @Autowired
    private GasTariffRepository gasTariffRepo;

    @Autowired
    private GasConversionFactorRepository conversionFactorRepo;

    @Autowired
    private TaxConfigRepository taxConfigRepo;

    @Autowired
    private GasReadingRepository gasReadingRepo;

    @Test
    void contextLoads() {
        // Application context starts successfully
    }

    @Test
    void supplyPointsSeeded() {
        assertThat(supplyPointRepo.count()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void gasTariffsSeeded() {
        assertThat(gasTariffRepo.count()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void gasConversionFactorsSeeded() {
        assertThat(conversionFactorRepo.count()).isGreaterThanOrEqualTo(4);
    }

    @Test
    void taxesSeeded() {
        assertThat(taxConfigRepo.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void gasReadingsSeeded() {
        // 10 rows in gas-readings.csv (including one with unknown cups)
        assertThat(gasReadingRepo.count()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void supplyPointHasCups() {
        assertThat(supplyPointRepo.findById("ES0021000000001AA")).isPresent();
    }

    @Test
    void tariffRL1Exists() {
        assertThat(gasTariffRepo.findById("RL1")).isPresent();
    }

    @Test
    void taxIVAExists() {
        assertThat(taxConfigRepo.findById("IVA")).isPresent();
    }
}
