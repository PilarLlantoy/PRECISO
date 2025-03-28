package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.ResultingFieldParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.SourceParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.StructureParametroReportes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultingFieldsParametroReportesRepository
        extends JpaRepository<ResultingFieldParametroReportes,Integer> {
    ResultingFieldParametroReportes findAllById(int id);
    List<ResultingFieldParametroReportes> findByEstado(boolean estado);
    List<ResultingFieldParametroReportes> findByFuenteIdAndParametroReportesId(int fuenteId, int parametroId);

    void deleteByFuente(SourceParametroReportes fuente);
}
