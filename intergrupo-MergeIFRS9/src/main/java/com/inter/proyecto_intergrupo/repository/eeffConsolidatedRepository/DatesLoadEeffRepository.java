package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.DatesLoadEeFF;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaeeffFiliales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatesLoadEeffRepository extends JpaRepository<DatesLoadEeFF,Long> {

    DatesLoadEeFF findByEntidadAndPeriodo(String entidad, String periodo);

    List<DatesLoadEeFF> findByPeriodo(String period);

}
