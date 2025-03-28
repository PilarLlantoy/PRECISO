package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.SourceParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.StructureParametroReportes;
import com.inter.proyecto_intergrupo.model.parametric.ValidationParametroReportes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationParametroReportesRepository extends JpaRepository<ValidationParametroReportes,Integer> {
    ValidationParametroReportes findAllById(int id);
    List<ValidationParametroReportes> findByEstado(boolean estado);
    List<ValidationParametroReportes> findByFuenteIdAndParametroReportesId(int fuenteId, int parametroId);

    void deleteByFuente(ValidationParametroReportes fuente);
}
