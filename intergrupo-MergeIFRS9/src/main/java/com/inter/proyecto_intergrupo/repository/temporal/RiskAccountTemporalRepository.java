package com.inter.proyecto_intergrupo.repository.temporal;

import com.inter.proyecto_intergrupo.model.ifrs9.RiskAccount;
import com.inter.proyecto_intergrupo.model.temporal.RiskAccountTemporal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskAccountTemporalRepository extends JpaRepository<RiskAccountTemporal,Long> {
    List<RiskAccountTemporal> findAll();
    RiskAccountTemporal findByIdCriesgos(Long id);
}
