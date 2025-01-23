package com.inter.proyecto_intergrupo.repository.parametric;
import com.inter.proyecto_intergrupo.model.parametric.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface LogInformationCrossingRepository extends JpaRepository<LogInformationCrossing,Long> {
    List<LogInformationCrossing> findAllByOrderByFechaProcesoAsc();
    List<LogInformationCrossing> findAllByIdConciliacionAndFechaProcesoAndIdEventoOrderByIdDesc(
            Conciliation conciliacion, Date fecha, EventType evento);
    List<LogInformationCrossing> findAllByUsuario(String usuario);
}