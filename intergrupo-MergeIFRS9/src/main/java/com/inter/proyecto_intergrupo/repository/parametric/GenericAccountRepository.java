package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.GenericAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenericAccountRepository extends JpaRepository<GenericAccount, String> {
    List<GenericAccount> findAll();
    List<GenericAccount> findByCuenta(String cuenta);
}
