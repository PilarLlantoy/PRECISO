package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.AdjustmentsHom;
import com.inter.proyecto_intergrupo.model.ifrs9.ManualAdjustments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdjustmentsHomRepository extends JpaRepository<AdjustmentsHom,Long> {
    List<AdjustmentsHom> findAll();
    //List<ApuntesRiesgos> findByNumeroCliente(String numeroCliente);
}
