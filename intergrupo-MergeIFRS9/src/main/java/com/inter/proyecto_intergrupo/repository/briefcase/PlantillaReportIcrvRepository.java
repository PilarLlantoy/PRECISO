package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.PlantillaReportIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.ReportIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaReportIcrvRepository extends JpaRepository<PlantillaReportIcrv,Long> {
    List<PlantillaReportIcrv> findAll();
    PlantillaReportIcrv findByIdReport(Long id);
}
