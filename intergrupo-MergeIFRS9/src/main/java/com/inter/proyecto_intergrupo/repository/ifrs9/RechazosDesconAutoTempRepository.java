package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.AccountControl;
import com.inter.proyecto_intergrupo.model.ifrs9.RechazosDesconAutoTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RechazosDesconAutoTempRepository extends JpaRepository<RechazosDesconAutoTemp,Long> {
    List<RechazosDesconAutoTemp> findAll();
    RechazosDesconAutoTemp findByTempId(Long id);
}
