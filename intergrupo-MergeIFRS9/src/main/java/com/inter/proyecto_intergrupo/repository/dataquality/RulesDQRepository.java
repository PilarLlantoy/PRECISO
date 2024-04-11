package com.inter.proyecto_intergrupo.repository.dataquality;

import com.inter.proyecto_intergrupo.model.dataquality.RulesDQ;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RulesDQRepository extends JpaRepository<RulesDQ,Long> {
    List<RulesDQ> findAll();
}
