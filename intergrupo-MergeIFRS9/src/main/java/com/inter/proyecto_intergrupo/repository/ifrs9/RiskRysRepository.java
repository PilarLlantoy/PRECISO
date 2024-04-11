package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.RiskRys;
import com.inter.proyecto_intergrupo.model.ifrs9.RysConcil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskRysRepository extends JpaRepository<RiskRys,String> {
    List<RiskRys> findAll();
    List<RiskRys> findByNumeroPapeleta(String numero);
}
