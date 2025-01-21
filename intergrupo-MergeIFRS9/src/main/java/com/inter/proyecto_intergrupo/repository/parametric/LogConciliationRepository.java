package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.model.parametric.LogConciliation;
import com.inter.proyecto_intergrupo.model.parametric.LogInformationCrossing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LogConciliationRepository extends JpaRepository<LogConciliation,Long> {
    List<LogConciliation> findAllByOrderByFechaProcesoAsc();
    List<LogConciliation> findAllByIdConciliacionAndFechaProceso(
            Conciliation conciliacion, Date fecha);
    List<LogConciliation> findAllByUsuario(String usuario);

    // Ordenar por fechaProceso de forma descendente (más reciente primero)
    List<LogConciliation> findAllByIdConciliacionAndFechaProcesoOrderByIdDesc(
            Conciliation conciliacion, Date fecha);
}