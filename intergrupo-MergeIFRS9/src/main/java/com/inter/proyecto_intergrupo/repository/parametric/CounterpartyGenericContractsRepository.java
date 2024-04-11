package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.CounterpartyGenericContracts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounterpartyGenericContractsRepository extends JpaRepository<CounterpartyGenericContracts, String> {
    List<CounterpartyGenericContracts> findAll();
    List<CounterpartyGenericContracts> findByCuenta(String cuenta);
}