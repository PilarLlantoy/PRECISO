package com.inter.proyecto_intergrupo.repository.dataquality;

import com.inter.proyecto_intergrupo.model.dataquality.PointRulesDQ;
import com.inter.proyecto_intergrupo.model.dataquality.RulesDQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointRulesDQRepository extends JpaRepository<PointRulesDQ,Long> {
    List<PointRulesDQ> findAll();
    void deleteByPeriodoAndNombreFisicoObjeto(String periodo,String nombreFisicoObjeto);
    void deleteByPeriodo(String periodo);
}
