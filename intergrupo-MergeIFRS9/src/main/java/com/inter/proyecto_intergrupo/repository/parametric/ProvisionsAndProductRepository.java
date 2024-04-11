package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.ProvisionsAndProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProvisionsAndProductRepository extends JpaRepository<ProvisionsAndProduct, String> {
    List<ProvisionsAndProduct> findAll();
    List<ProvisionsAndProduct> findByCuenta(String cuenta);
}
