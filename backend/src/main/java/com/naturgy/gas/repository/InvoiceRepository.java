package com.naturgy.gas.repository;

import com.naturgy.gas.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByCupsAndPeriodoInicio(String cups, LocalDate periodoInicio);

    long countByPeriodoInicio(LocalDate periodoInicio);
}
