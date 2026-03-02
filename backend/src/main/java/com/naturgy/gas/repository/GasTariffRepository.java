package com.naturgy.gas.repository;

import com.naturgy.gas.domain.GasTariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface GasTariffRepository extends JpaRepository<GasTariff, String> {

    @Query("SELECT t FROM GasTariff t WHERE t.tarifa = :tarifa AND t.vigenciaDesde <= :date ORDER BY t.vigenciaDesde DESC LIMIT 1")
    Optional<GasTariff> findEffectiveTariff(@Param("tarifa") String tarifa, @Param("date") LocalDate date);
}
