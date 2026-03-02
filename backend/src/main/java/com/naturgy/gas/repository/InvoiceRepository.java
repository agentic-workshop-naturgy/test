package com.naturgy.gas.repository;

import com.naturgy.gas.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByCupsAndPeriodoInicio(String cups, LocalDate periodoInicio);

    long countByPeriodoInicio(LocalDate periodoInicio);

    /** Eagerly load invoice with its lines in a single query (avoids LazyInitializationException). */
    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.lines WHERE i.id = :id")
    Optional<Invoice> findByIdWithLines(@Param("id") Long id);

    /** List all invoices with lines eagerly loaded. */
    @Query("SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.lines")
    List<Invoice> findAllWithLines();
}
