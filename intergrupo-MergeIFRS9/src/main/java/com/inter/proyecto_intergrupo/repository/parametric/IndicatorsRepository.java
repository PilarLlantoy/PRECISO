package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Indicators;
import com.inter.proyecto_intergrupo.model.parametric.Subsidiaries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndicatorsRepository extends JpaRepository<Indicators,String> {
    List<Indicators> findAll();
    Indicators findByCuentaNeocon(String id);

}
