package com.naturgy.gas.controller;

import com.naturgy.gas.domain.GasTariff;
import com.naturgy.gas.repository.GasTariffRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/gas/tariffs")
public class GasTariffController {

    private final GasTariffRepository repo;

    public GasTariffController(GasTariffRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<GasTariff> findAll() {
        return repo.findAll();
    }

    @GetMapping("/{tarifa}")
    public GasTariff findById(@PathVariable String tarifa) {
        return repo.findById(tarifa)
                .orElseThrow(() -> new NoSuchElementException("GasTariff not found: " + tarifa));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GasTariff create(@RequestBody @Valid GasTariff tariff) {
        if (repo.existsById(tariff.getTarifa())) {
            throw new IllegalArgumentException("GasTariff already exists: " + tariff.getTarifa());
        }
        return repo.save(tariff);
    }

    @PutMapping("/{tarifa}")
    public GasTariff update(@PathVariable String tarifa,
                            @RequestBody @Valid GasTariff tariff) {
        if (!repo.existsById(tarifa)) {
            throw new NoSuchElementException("GasTariff not found: " + tarifa);
        }
        tariff.setTarifa(tarifa);
        return repo.save(tariff);
    }

    @DeleteMapping("/{tarifa}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String tarifa) {
        if (!repo.existsById(tarifa)) {
            throw new NoSuchElementException("GasTariff not found: " + tarifa);
        }
        repo.deleteById(tarifa);
    }
}
