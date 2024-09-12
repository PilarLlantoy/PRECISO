package com.inter.proyecto_intergrupo.repository.parametric;

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

    /*
    @Query("SELECT MAX(r.rutaId) FROM CutaC r")
    Optional<Integer> findMaxRutaId();
    @Query("SELECT MAX(r.codigo) FROM CutaC r WHERE r.ruta = :ruta")
    Optional<Integer> findMaxCodigoByRuta(@Param("ruta") String ruta);
    @Query("SELECT r.rutaId FROM CutaC r WHERE r.ruta = :ruta")
    Optional<Integer> findRutaIdByRuta(@Param("ruta") String ruta);

     */
}
