package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaeeffFiliales;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FiduciariaEeffConsolidatedRepository extends JpaRepository<FiduciariaeeffFiliales,String> {
    void deleteByPeriodo(String periodo);

    List<FiduciariaeeffFiliales> findByPeriodo(String periodo);

}
