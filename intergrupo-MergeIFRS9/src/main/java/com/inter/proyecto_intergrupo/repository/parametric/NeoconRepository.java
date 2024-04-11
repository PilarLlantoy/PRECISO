package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Indicators;
import com.inter.proyecto_intergrupo.model.parametric.Neocon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeoconRepository extends JpaRepository<Neocon,Long> {
    List<Neocon> findAll();
    Neocon findByCuenta(Long id);

}
