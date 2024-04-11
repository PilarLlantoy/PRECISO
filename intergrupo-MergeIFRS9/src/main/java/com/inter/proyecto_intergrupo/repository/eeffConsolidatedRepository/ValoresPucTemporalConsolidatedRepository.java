package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.ValoresPucFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ValoresPucTemporalFiliales;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValoresPucTemporalConsolidatedRepository extends JpaRepository<ValoresPucTemporalFiliales,String> {

    void deleteByPeriodo(String periodo);

}
