package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.FilterParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.SourceParametroReportes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceParametroReportesRepository extends JpaRepository<SourceParametroReportes,Integer> {
    SourceParametroReportes findAllById(int id);
    List<SourceParametroReportes> findByEstado(boolean estado);
}
