package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.CondicionRC;
import com.inter.proyecto_intergrupo.model.parametric.FilterParametroReportes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilterParametroReportesRepository extends JpaRepository<FilterParametroReportes,Integer> {
    FilterParametroReportes findAllById(int id);
    List<FilterParametroReportes> findByEstado(boolean estado);
}
