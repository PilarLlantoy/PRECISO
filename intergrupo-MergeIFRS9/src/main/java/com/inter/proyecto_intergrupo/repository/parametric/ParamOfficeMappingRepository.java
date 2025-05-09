package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.MasterInvent;
import com.inter.proyecto_intergrupo.model.parametric.ParamOfficeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParamOfficeMappingRepository extends JpaRepository<ParamOfficeMapping,Long> {
    ParamOfficeMapping findAllById(Long id);
}
