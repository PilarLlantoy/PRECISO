package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionInicialCuadreGeneral;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionInicialDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EliminacionesVersionIncialDetalleRepository extends JpaRepository<EliminacionesVersionInicialDetalle,Long> {

    List<EliminacionesVersionInicialDetalle> findByPeriodo(String periodo);
    void deleteByPeriodo(String periodo);
}
