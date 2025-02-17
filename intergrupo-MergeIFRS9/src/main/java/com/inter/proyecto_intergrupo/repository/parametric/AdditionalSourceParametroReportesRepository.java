package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AdditionalSourceParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.SourceParametroReportes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdditionalSourceParametroReportesRepository extends JpaRepository<AdditionalSourceParametroReportes,Integer> {
    AdditionalSourceParametroReportes findAllById(int id);
    List<AdditionalSourceParametroReportes> findByEstado(boolean estado);
}
