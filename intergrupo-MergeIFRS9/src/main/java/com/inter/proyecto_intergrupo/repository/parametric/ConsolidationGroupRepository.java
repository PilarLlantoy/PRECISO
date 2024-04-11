package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.ConsolidationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsolidationGroupRepository extends JpaRepository<ConsolidationGroup,String> {
    List<ConsolidationGroup> findAll();
    ConsolidationGroup findAllById(String id);
}
