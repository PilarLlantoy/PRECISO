package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.GarantiasActivasIcrf;
import com.inter.proyecto_intergrupo.model.briefcase.ReportIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarantiasActivasIcrfRepository extends JpaRepository<GarantiasActivasIcrf,Long> {
    List<GarantiasActivasIcrf> findAll();
    GarantiasActivasIcrf findByIdReport(Long id);
    void deleteByPeriodo(String periodo);
    List<GarantiasActivasIcrf> findByPeriodo(String periodo);
}
