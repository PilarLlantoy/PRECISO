package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.AccountControl;
import com.inter.proyecto_intergrupo.model.ifrs9.PlaneRistras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaneRistrasRepository extends JpaRepository<PlaneRistras,Long> {
    List<PlaneRistras> findAll();
    PlaneRistras findByIdPlano(Long id);
}
