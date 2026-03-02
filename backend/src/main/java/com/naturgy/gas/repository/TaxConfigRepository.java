package com.naturgy.gas.repository;

import com.naturgy.gas.domain.TaxConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TaxConfigRepository extends JpaRepository<TaxConfig, String> {

    @Query("SELECT t FROM TaxConfig t WHERE t.taxCode = :taxCode AND t.vigenciaDesde <= :date ORDER BY t.vigenciaDesde DESC LIMIT 1")
    Optional<TaxConfig> findEffectiveTax(@Param("taxCode") String taxCode, @Param("date") LocalDate date);
}
