package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Contract;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract,String> {
    List<Contract> findAll();
    Contract findAllByContrato(String id);
    Contract findByContrato(String contrato);
}
