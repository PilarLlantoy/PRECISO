package com.inter.proyecto_intergrupo.repository.reports;

import com.inter.proyecto_intergrupo.model.reports.Rp21;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Rp21Repository extends JpaRepository<Rp21,Long> {
    List<Rp21> findAll();
    Rp21 findByIdReporte(Long id);
}
