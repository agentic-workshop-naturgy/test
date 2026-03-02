package com.naturgy.gas.controller;

import com.naturgy.gas.domain.GasConversionFactor;
import com.naturgy.gas.repository.GasConversionFactorRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/gas/factors")
public class GasConversionFactorController {

    private final GasConversionFactorRepository repo;

    public GasConversionFactorController(GasConversionFactorRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<GasConversionFactor> findAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public GasConversionFactor findById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("GasConversionFactor not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GasConversionFactor create(@RequestBody @Valid GasConversionFactor factor) {
        return repo.save(factor);
    }

    @PutMapping("/{id}")
    public GasConversionFactor update(@PathVariable Long id,
                                      @RequestBody @Valid GasConversionFactor factor) {
        if (!repo.existsById(id)) {
            throw new NoSuchElementException("GasConversionFactor not found: " + id);
        }
        return repo.save(factor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new NoSuchElementException("GasConversionFactor not found: " + id);
        }
        repo.deleteById(id);
    }
}
