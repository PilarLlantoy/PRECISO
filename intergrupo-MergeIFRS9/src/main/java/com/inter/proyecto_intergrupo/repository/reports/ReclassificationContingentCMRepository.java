package com.inter.proyecto_intergrupo.repository.reports;

import com.inter.proyecto_intergrupo.model.reports.ReclassificationContingent;
import com.inter.proyecto_intergrupo.model.reports.ReclassificationContingentCM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclassificationContingentCMRepository extends JpaRepository<ReclassificationContingentCM,Long> {
    List<ReclassificationContingentCM> findAll();
    ReclassificationContingentCM findByIdCargaMasiva(Long id);
}
