package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.AccountControl;
import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RysParametric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RysParametricRepository extends JpaRepository<RysParametric,Long> {
    List<RysParametric> findAll();
    RysParametric findByIdRys(Long id);
    RysParametric findByCuenta(String id);
}
