package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.CampoParamReportes;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampoParametroReportesRepository extends JpaRepository<CampoParamReportes,Integer> {
    List<CampoParamReportes> findAllByOrderByDetalleAsc();
    CampoParamReportes findAllById(int id);
    List<CampoParamReportes> findByEstado(boolean estado);
    CampoParamReportes findAllByDetalle(String detalle);
}
