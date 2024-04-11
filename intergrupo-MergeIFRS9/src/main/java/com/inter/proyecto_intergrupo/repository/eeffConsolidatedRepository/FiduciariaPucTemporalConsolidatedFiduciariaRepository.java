package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaPucFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaPucTemporalFiliales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FiduciariaPucTemporalConsolidatedFiduciariaRepository extends JpaRepository<FiduciariaPucTemporalFiliales, String> {

    void deleteByPeriodo(String periodo);

    List<FiduciariaPucTemporalFiliales> findByPeriodo(String periodo);
}
