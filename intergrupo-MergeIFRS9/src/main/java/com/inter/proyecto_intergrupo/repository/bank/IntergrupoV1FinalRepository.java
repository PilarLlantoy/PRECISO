package com.inter.proyecto_intergrupo.repository.bank;

import com.inter.proyecto_intergrupo.model.bank.TaxBaseLoad;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1Final;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntergrupoV1FinalRepository extends JpaRepository<IntergrupoV1Final,Long> {
    List<IntergrupoV1Final> findAll();
}
