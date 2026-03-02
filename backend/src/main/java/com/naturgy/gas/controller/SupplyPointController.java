package com.naturgy.gas.controller;

import com.naturgy.gas.domain.SupplyPoint;
import com.naturgy.gas.repository.SupplyPointRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/gas/supply-points")
public class SupplyPointController {

    private final SupplyPointRepository repo;

    public SupplyPointController(SupplyPointRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<SupplyPoint> findAll() {
        return repo.findAll();
    }

    @GetMapping("/{cups}")
    public SupplyPoint findById(@PathVariable String cups) {
        return repo.findById(cups)
                .orElseThrow(() -> new NoSuchElementException("SupplyPoint not found: " + cups));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplyPoint create(@RequestBody @Valid SupplyPoint supplyPoint) {
        if (repo.existsById(supplyPoint.getCups())) {
            throw new IllegalArgumentException("SupplyPoint already exists: " + supplyPoint.getCups());
        }
        return repo.save(supplyPoint);
    }

    @PutMapping("/{cups}")
    public SupplyPoint update(@PathVariable String cups,
                              @RequestBody @Valid SupplyPoint supplyPoint) {
        if (!repo.existsById(cups)) {
            throw new NoSuchElementException("SupplyPoint not found: " + cups);
        }
        supplyPoint.setCups(cups);
        return repo.save(supplyPoint);
    }

    @DeleteMapping("/{cups}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String cups) {
        if (!repo.existsById(cups)) {
            throw new NoSuchElementException("SupplyPoint not found: " + cups);
        }
        repo.deleteById(cups);
    }
}
