package com.inter.proyecto_intergrupo.repository.bank;

import com.inter.proyecto_intergrupo.model.reports.IntergrupoV3Final;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV3Valida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntergrupoV3ValidaRepository extends JpaRepository<IntergrupoV3Valida,Long> {
    List<IntergrupoV3Valida> findAll();
}
