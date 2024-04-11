package com.inter.proyecto_intergrupo.repository.reports;

import com.inter.proyecto_intergrupo.model.reports.ContingentTemplate;
import com.inter.proyecto_intergrupo.model.reports.ReclassificationContingent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclassificationContingentRepository extends JpaRepository<ReclassificationContingent,Long> {
    List<ReclassificationContingent> findAll();
    ReclassificationContingent findByIdReclasificacion(Long id);
}
