package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RechazosDescontabilizacionPreCarga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RechazosDescontabilizacionRepository extends JpaRepository<RechazosDescontabilizacionPreCarga,Integer> {
    List<RechazosDescontabilizacionPreCarga> findAll();
    Optional<RechazosDescontabilizacionPreCarga> findById(Integer id);
}
