package com.naturgy.gas.repository;

import com.naturgy.gas.domain.SupplyPoint;
import com.naturgy.gas.domain.enums.EstadoSupplyPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplyPointRepository extends JpaRepository<SupplyPoint, String> {
    List<SupplyPoint> findByEstado(EstadoSupplyPoint estado);
}
