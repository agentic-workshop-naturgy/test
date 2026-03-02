package com.naturgy.gas.repository;

import com.naturgy.gas.domain.GasReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface GasReadingRepository extends JpaRepository<GasReading, Long> {

    boolean existsByCupsAndFecha(String cups, LocalDate fecha);

    @Query("SELECT r FROM GasReading r WHERE r.cups = :cups AND r.fecha < :date ORDER BY r.fecha DESC LIMIT 1")
    Optional<GasReading> findLastReadingBefore(@Param("cups") String cups, @Param("date") LocalDate date);

    @Query("SELECT r FROM GasReading r WHERE r.cups = :cups AND r.fecha <= :date ORDER BY r.fecha DESC LIMIT 1")
    Optional<GasReading> findLastReadingOnOrBefore(@Param("cups") String cups, @Param("date") LocalDate date);
}
