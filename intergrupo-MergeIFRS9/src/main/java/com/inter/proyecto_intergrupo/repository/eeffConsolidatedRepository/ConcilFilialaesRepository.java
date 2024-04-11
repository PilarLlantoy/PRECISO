package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ConcilFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.DatesLoadEeFF;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaeeffFiliales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcilFilialaesRepository extends JpaRepository<ConcilFiliales,String> {


    List<ConcilFiliales> findByPeriodo(String periodo);

}
