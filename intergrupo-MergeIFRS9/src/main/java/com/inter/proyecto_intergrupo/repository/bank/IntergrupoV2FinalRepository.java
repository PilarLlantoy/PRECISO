package com.inter.proyecto_intergrupo.repository.bank;

import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1Final;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV2Final;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntergrupoV2FinalRepository extends JpaRepository<IntergrupoV2Final,Long> {
    List<IntergrupoV2Final> findAll();
}
