package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.LogAccountingLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LogAccountingLoadRepository extends JpaRepository<LogAccountingLoad,Long> {
    List<LogAccountingLoad> findAllByOrderByFechaCargueAsc();
    List<LogAccountingLoad> findAllByIdRcAndFechaCargueOrderByIdDesc(AccountingRoute accountingRoute, Date fechaCargue);
    List<LogAccountingLoad> findAllByUsuario(String usuario);
}
