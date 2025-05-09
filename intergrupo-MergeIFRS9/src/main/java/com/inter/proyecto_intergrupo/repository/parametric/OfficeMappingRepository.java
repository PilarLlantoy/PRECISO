package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.OfficeMapping;
import com.inter.proyecto_intergrupo.model.parametric.ParamOfficeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficeMappingRepository extends JpaRepository<OfficeMapping,String> {

}
