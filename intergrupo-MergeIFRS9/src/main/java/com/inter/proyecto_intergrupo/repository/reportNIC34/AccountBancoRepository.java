package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.AccountBanco;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountBancoRepository extends JpaRepository<AccountBanco,String> {
    List<AccountBanco> findAll();
    AccountBanco findByCuenta(String cuenta);
    void deleteByCuenta(String cuenta);
    List<AccountBanco> findByCuentaContainingIgnoreCase(String cuenta);
    List<AccountBanco> findByNaturalezaContainingIgnoreCase(String naturaleza);
}
