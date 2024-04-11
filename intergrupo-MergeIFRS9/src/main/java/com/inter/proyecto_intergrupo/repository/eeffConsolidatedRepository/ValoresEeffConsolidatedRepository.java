package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.ValoreseeffFiliales;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValoresEeffConsolidatedRepository extends JpaRepository<ValoreseeffFiliales,Long> {
    void deleteByPeriodo(String periodo);

    List<ValoreseeffFiliales> findByPeriodo(String periodo);
}
