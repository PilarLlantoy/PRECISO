package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesDetalleVersionAjustada;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionAjustada;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionInicialDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EliminacionesVersionCuadreGeneralAjustadaRepository extends JpaRepository<EliminacionesDetalleVersionAjustada,Long> {

    List<EliminacionesDetalleVersionAjustada> findByPeriodo(String periodo);

    void deleteByPeriodo(String periodo);
}
