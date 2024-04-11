package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.ApuntesRiesgos;
import com.inter.proyecto_intergrupo.model.ifrs9.ManualAdjustments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManualAdjustmentsRepository extends JpaRepository<ManualAdjustments,Long> {
    List<ManualAdjustments> findAll();
    //List<ApuntesRiesgos> findByNumeroCliente(String numeroCliente);
}
