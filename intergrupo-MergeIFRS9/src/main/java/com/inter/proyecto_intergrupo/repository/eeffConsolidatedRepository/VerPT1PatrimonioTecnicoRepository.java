package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.VerPT;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.VerPT1;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerPT1PatrimonioTecnicoRepository extends JpaRepository<VerPT1,Long> {

    VerPT1 findByIdDato1(Long id);
}
