package com.inter.proyecto_intergrupo.repository.reports;

import com.inter.proyecto_intergrupo.model.reports.ContingentTemplate;
import com.inter.proyecto_intergrupo.model.reports.Rp21;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContingentTemplateRepository extends JpaRepository<ContingentTemplate,Long> {
    List<ContingentTemplate> findAll();
    ContingentTemplate findByIdPlantilla(Long id);
    List<ContingentTemplate> findAllByContrato(String contrato);
    void deleteByPeriodo(String period);
}
