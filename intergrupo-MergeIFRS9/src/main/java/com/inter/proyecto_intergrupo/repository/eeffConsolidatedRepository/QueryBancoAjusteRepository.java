package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.PlantillaBancoModel;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.QueryBanco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryBancoAjusteRepository extends JpaRepository<PlantillaBancoModel, Long> {


    List<PlantillaBancoModel> findByPeriodo(String periodo);

    void deleteByPeriodo(String periodo);
}