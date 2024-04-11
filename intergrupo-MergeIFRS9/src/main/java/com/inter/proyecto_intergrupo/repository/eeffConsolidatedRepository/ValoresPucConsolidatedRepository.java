package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaPucFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ValoresPucFiliales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValoresPucConsolidatedRepository extends JpaRepository<ValoresPucFiliales,String> {

    void deleteByPeriodo(String periodo);

    List<ValoresPucFiliales> findByPeriodo(String periodo);
}
