package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ConcilFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricEEFF;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.VerPT;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerPTValorRiesgoRepository extends JpaRepository<VerPT,Long> {

    VerPT findByIdDato(Long id);
}
