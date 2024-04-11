package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaPucFiliales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FiduciariaPucConsolidatedFiduciariaRepository extends JpaRepository<FiduciariaPucFiliales,String> {

     List<FiduciariaPucFiliales> findByPeriodo(String periodo);

    void deleteByPeriodo(String periodo);

}
