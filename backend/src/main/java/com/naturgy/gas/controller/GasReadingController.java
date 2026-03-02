package com.naturgy.gas.controller;

import com.naturgy.gas.domain.GasReading;
import com.naturgy.gas.repository.GasReadingRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/gas/readings")
public class GasReadingController {

    private final GasReadingRepository repo;

    public GasReadingController(GasReadingRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<GasReading> findAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public GasReading findById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("GasReading not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GasReading create(@RequestBody @Valid GasReading reading) {
        return repo.save(reading);
    }

    @PutMapping("/{id}")
    public GasReading update(@PathVariable Long id,
                             @RequestBody @Valid GasReading reading) {
        if (!repo.existsById(id)) {
            throw new NoSuchElementException("GasReading not found: " + id);
        }
        // Reflect the path id onto the body to prevent mismatch
        return repo.save(reading);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new NoSuchElementException("GasReading not found: " + id);
        }
        repo.deleteById(id);
    }
}
