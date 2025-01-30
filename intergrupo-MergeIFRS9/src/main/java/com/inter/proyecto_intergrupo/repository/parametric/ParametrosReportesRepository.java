package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.ParametrosReportes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametrosReportesRepository extends JpaRepository<ParametrosReportes,Integer> {
    List<ParametrosReportes> findAllByOrderByNombreAsc();
    ParametrosReportes findAllById(int id);
    List<ParametrosReportes> findByActivo(boolean estado);
    ParametrosReportes findAllByNombre(String nombre);
}
