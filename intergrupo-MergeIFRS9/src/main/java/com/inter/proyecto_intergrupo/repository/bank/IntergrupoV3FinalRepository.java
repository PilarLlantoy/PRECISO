package com.inter.proyecto_intergrupo.repository.bank;

import com.inter.proyecto_intergrupo.model.reports.IntergrupoV2Final;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV3Final;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntergrupoV3FinalRepository extends JpaRepository<IntergrupoV3Final,Long> {
    List<IntergrupoV3Final> findAll();
}
