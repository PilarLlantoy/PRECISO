package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface EventMatrixRepository extends JpaRepository<EventMatrix,Integer> {
    EventMatrix findAllById(int id);
    List<EventMatrix> findByEstado(boolean estado);

    List<EventMatrix> findAllByEstadoAndConciliacionAndInventarioConciliacion(
            boolean estado, Conciliation conciliacion, ConciliationRoute inventario
    );
}
