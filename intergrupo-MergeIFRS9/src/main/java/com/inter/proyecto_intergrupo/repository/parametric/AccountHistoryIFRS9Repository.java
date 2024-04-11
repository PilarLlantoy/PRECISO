package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountHistoryIFRS9;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountHistoryIFRS9Repository extends JpaRepository<AccountHistoryIFRS9, String> {
    List<AccountHistoryIFRS9> findAll();
    List<AccountHistoryIFRS9> findByCuenta(String cuenta);
}