package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountConcil;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConciliationRouteRepository extends JpaRepository<ConciliationRoute,Integer> {

    ConciliationRoute findAllById(int id);
    List<ConciliationRoute> findByEstado(boolean estado);
    ConciliationRoute findAllByDetalle(String detalle);

    List<ConciliationRoute> findByEstadoAndFichero(boolean estado, boolean fichero);
}
