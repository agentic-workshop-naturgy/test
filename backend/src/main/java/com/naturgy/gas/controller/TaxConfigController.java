package com.naturgy.gas.controller;

import com.naturgy.gas.domain.TaxConfig;
import com.naturgy.gas.repository.TaxConfigRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/gas/taxes")
public class TaxConfigController {

    private final TaxConfigRepository repo;

    public TaxConfigController(TaxConfigRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<TaxConfig> findAll() {
        return repo.findAll();
    }

    @GetMapping("/{taxCode}")
    public TaxConfig findById(@PathVariable String taxCode) {
        return repo.findById(taxCode)
                .orElseThrow(() -> new NoSuchElementException("TaxConfig not found: " + taxCode));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaxConfig create(@RequestBody @Valid TaxConfig tax) {
        if (repo.existsById(tax.getTaxCode())) {
            throw new IllegalArgumentException("TaxConfig already exists: " + tax.getTaxCode());
        }
        return repo.save(tax);
    }

    @PutMapping("/{taxCode}")
    public TaxConfig update(@PathVariable String taxCode,
                            @RequestBody @Valid TaxConfig tax) {
        if (!repo.existsById(taxCode)) {
            throw new NoSuchElementException("TaxConfig not found: " + taxCode);
        }
        tax.setTaxCode(taxCode);
        return repo.save(tax);
    }

    @DeleteMapping("/{taxCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String taxCode) {
        if (!repo.existsById(taxCode)) {
            throw new NoSuchElementException("TaxConfig not found: " + taxCode);
        }
        repo.deleteById(taxCode);
    }
}
