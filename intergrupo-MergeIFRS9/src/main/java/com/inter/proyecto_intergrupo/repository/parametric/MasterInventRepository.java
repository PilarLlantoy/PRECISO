package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.MasterInvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterInventRepository extends JpaRepository<MasterInvent,Long> {
    MasterInvent findAllById(Long id);
}
