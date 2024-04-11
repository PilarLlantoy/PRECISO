package com.inter.proyecto_intergrupo.repository.bank;

import com.inter.proyecto_intergrupo.model.bank.TaxBaseLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxBaseLoadRepository extends JpaRepository<TaxBaseLoad,Long> {
    List<TaxBaseLoad> findAll();
    TaxBaseLoad findByIdCarga(Long id);
    List<TaxBaseLoad> findAllByEstado(String estado);
    void deleteByIdCarga(Long id);
    void deleteByFecha(String fecha);
}
