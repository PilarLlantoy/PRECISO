package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ConcilFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionInicialCuadreGeneral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EliminacionesVersionIncialRepository extends JpaRepository<EliminacionesVersionInicialCuadreGeneral,Long> {

    List<EliminacionesVersionInicialCuadreGeneral> findByPeriodo(String periodo);
    void deleteByPeriodo(String periodo);
}
