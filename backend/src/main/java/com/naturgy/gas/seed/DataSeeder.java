package com.naturgy.gas.seed;

import com.naturgy.gas.domain.*;
import com.naturgy.gas.domain.enums.EstadoSupplyPoint;
import com.naturgy.gas.domain.enums.TipoLectura;
import com.naturgy.gas.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Value("${gas.seed.samples-dir:_data/db/samples}")
    private String samplesDir;

    private final SupplyPointRepository supplyPointRepo;
    private final GasTariffRepository gasTariffRepo;
    private final GasConversionFactorRepository conversionFactorRepo;
    private final TaxConfigRepository taxConfigRepo;
    private final GasReadingRepository gasReadingRepo;

    public DataSeeder(SupplyPointRepository supplyPointRepo,
                      GasTariffRepository gasTariffRepo,
                      GasConversionFactorRepository conversionFactorRepo,
                      TaxConfigRepository taxConfigRepo,
                      GasReadingRepository gasReadingRepo) {
        this.supplyPointRepo = supplyPointRepo;
        this.gasTariffRepo = gasTariffRepo;
        this.conversionFactorRepo = conversionFactorRepo;
        this.taxConfigRepo = taxConfigRepo;
        this.gasReadingRepo = gasReadingRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting data seed from {}", samplesDir);
        seedSupplyPoints();
        seedGasTariffs();
        seedGasConversionFactors();
        seedTaxes();
        seedGasReadings();
        log.info("Data seed complete. supply_points={}, tariffs={}, factors={}, taxes={}, readings={}",
                supplyPointRepo.count(), gasTariffRepo.count(),
                conversionFactorRepo.count(), taxConfigRepo.count(), gasReadingRepo.count());
    }

    private void seedSupplyPoints() {
        Path file = Paths.get(samplesDir, "supply-points.csv");
        if (!Files.exists(file)) {
            log.warn("supply-points.csv not found at {}; skipping.", file);
            return;
        }
        int loaded = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.isBlank()) continue;
                String[] cols = line.split(",", -1);
                if (cols.length < 4) throw new IllegalArgumentException("supply-points.csv malformed row: " + line);
                String cups = cols[0].trim();
                String zona = cols[1].trim();
                String tarifa = cols[2].trim();
                EstadoSupplyPoint estado = EstadoSupplyPoint.valueOf(cols[3].trim());
                if (!supplyPointRepo.existsById(cups)) {
                    supplyPointRepo.save(new SupplyPoint(cups, zona, tarifa, estado));
                    loaded++;
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed supply-points.csv", e);
        }
        log.info("supply-points: {} rows inserted (idempotent)", loaded);
    }

    private void seedGasTariffs() {
        Path file = Paths.get(samplesDir, "gas-tariffs.csv");
        if (!Files.exists(file)) {
            log.warn("gas-tariffs.csv not found at {}; skipping.", file);
            return;
        }
        int loaded = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.isBlank()) continue;
                String[] cols = line.split(",", -1);
                if (cols.length < 4) throw new IllegalArgumentException("gas-tariffs.csv malformed row: " + line);
                String tarifa = cols[0].trim();
                BigDecimal fijo = new BigDecimal(cols[1].trim());
                BigDecimal variable = new BigDecimal(cols[2].trim());
                LocalDate vigencia = LocalDate.parse(cols[3].trim());
                if (!gasTariffRepo.existsById(tarifa)) {
                    gasTariffRepo.save(new GasTariff(tarifa, fijo, variable, vigencia));
                    loaded++;
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed gas-tariffs.csv", e);
        }
        log.info("gas-tariffs: {} rows inserted (idempotent)", loaded);
    }

    private void seedGasConversionFactors() {
        Path file = Paths.get(samplesDir, "gas-conversion-factors.csv");
        if (!Files.exists(file)) {
            log.warn("gas-conversion-factors.csv not found at {}; skipping.", file);
            return;
        }
        int loaded = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.isBlank()) continue;
                String[] cols = line.split(",", -1);
                if (cols.length < 4) throw new IllegalArgumentException("gas-conversion-factors.csv malformed row: " + line);
                String zona = cols[0].trim();
                String mes = cols[1].trim();
                BigDecimal coef = new BigDecimal(cols[2].trim());
                BigDecimal pcs = new BigDecimal(cols[3].trim());
                if (!conversionFactorRepo.existsByZonaAndMes(zona, mes)) {
                    conversionFactorRepo.save(new GasConversionFactor(zona, mes, coef, pcs));
                    loaded++;
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed gas-conversion-factors.csv", e);
        }
        log.info("gas-conversion-factors: {} rows inserted (idempotent)", loaded);
    }

    private void seedTaxes() {
        Path file = Paths.get(samplesDir, "taxes.csv");
        if (!Files.exists(file)) {
            log.warn("taxes.csv not found at {}; skipping.", file);
            return;
        }
        int loaded = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.isBlank()) continue;
                String[] cols = line.split(",", -1);
                if (cols.length < 3) throw new IllegalArgumentException("taxes.csv malformed row: " + line);
                String taxCode = cols[0].trim();
                BigDecimal taxRate = new BigDecimal(cols[1].trim());
                LocalDate vigencia = LocalDate.parse(cols[2].trim());
                if (!taxConfigRepo.existsById(taxCode)) {
                    taxConfigRepo.save(new TaxConfig(taxCode, taxRate, vigencia));
                    loaded++;
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed taxes.csv", e);
        }
        log.info("taxes: {} rows inserted (idempotent)", loaded);
    }

    private void seedGasReadings() {
        Path file = Paths.get(samplesDir, "gas-readings.csv");
        if (!Files.exists(file)) {
            log.warn("gas-readings.csv not found at {}; skipping.", file);
            return;
        }
        int loaded = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.isBlank()) continue;
                String[] cols = line.split(",", -1);
                if (cols.length < 4) throw new IllegalArgumentException("gas-readings.csv malformed row: " + line);
                String cups = cols[0].trim();
                LocalDate fecha = LocalDate.parse(cols[1].trim());
                BigDecimal lecturaM3 = new BigDecimal(cols[2].trim());
                TipoLectura tipo = TipoLectura.valueOf(cols[3].trim());
                if (!gasReadingRepo.existsByCupsAndFecha(cups, fecha)) {
                    gasReadingRepo.save(new GasReading(cups, fecha, lecturaM3, tipo));
                    loaded++;
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed gas-readings.csv", e);
        }
        log.info("gas-readings: {} rows inserted (idempotent)", loaded);
    }
}
