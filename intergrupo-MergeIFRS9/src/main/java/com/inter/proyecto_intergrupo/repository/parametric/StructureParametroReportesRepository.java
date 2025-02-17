package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.FilterParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.SourceParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.StructureParametroReportes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StructureParametroReportesRepository extends JpaRepository<StructureParametroReportes,Integer> {
    StructureParametroReportes findAllById(int id);
    List<StructureParametroReportes> findByEstado(boolean estado);
    List<StructureParametroReportes> findByFuenteIdAndParametroReportesId(int fuenteId, int parametroId);

    void deleteByFuente(SourceParametroReportes fuente);
}
