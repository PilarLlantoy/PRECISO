package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.ConsolidationMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsolidationMethodRepository extends JpaRepository<ConsolidationMethod,String> {
    List<ConsolidationMethod> findAll();
    ConsolidationMethod findAllById(String id);
}
