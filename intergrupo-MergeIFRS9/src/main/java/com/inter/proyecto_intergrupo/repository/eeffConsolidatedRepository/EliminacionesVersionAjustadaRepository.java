package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionAjustada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EliminacionesVersionAjustadaRepository extends JpaRepository<EliminacionesVersionAjustada,Long> {

    List<EliminacionesVersionAjustadaRepository> findByPeriodo(String periodo);

    void deleteByPeriodo(String periodo);
}
