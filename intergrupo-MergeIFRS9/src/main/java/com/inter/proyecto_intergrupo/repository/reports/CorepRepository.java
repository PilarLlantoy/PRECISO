package com.inter.proyecto_intergrupo.repository.reports;

import com.inter.proyecto_intergrupo.model.reports.ContingentTemplate;
import com.inter.proyecto_intergrupo.model.reports.Corep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorepRepository extends JpaRepository<Corep,Long> {
    List<Corep> findAll();
    Corep findByIdCorep(Long id);
}
