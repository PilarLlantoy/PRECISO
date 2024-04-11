package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.Centers;
import com.inter.proyecto_intergrupo.model.ifrs9.RysConcil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RysConcilRepository extends JpaRepository<RysConcil,String> {
    List<RysConcil> findAll();
    List<RysConcil> findByNumeroPapeleta(String numero);
}
