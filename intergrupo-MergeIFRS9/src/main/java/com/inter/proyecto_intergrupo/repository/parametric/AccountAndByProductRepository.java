package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountAndByProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountAndByProductRepository extends JpaRepository<AccountAndByProduct,String> {
    List<AccountAndByProduct> findAll();
    List<AccountAndByProduct> findByCuenta(String cuenta);
}