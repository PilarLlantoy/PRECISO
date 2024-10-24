package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
import com.inter.proyecto_intergrupo.model.parametric.LogAccountingLoad;
import com.inter.proyecto_intergrupo.model.parametric.LogInventoryLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LogInventoryLoadRepository extends JpaRepository<LogInventoryLoad,Long> {
    List<LogInventoryLoad> findAllByOrderByFechaCargueAsc();
    List<LogInventoryLoad> findAllByIdCRAndFechaCargueOrderByIdDesc(ConciliationRoute conciliationRoute, Date fechaCargue);
    List<LogInventoryLoad> findAllByFechaCargueOrderByIdDesc(Date fechaCargue);
    List<LogInventoryLoad> findAllByUsuario(String usuario);
}
