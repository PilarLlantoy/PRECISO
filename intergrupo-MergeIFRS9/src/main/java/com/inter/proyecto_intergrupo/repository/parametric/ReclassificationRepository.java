package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Indicators;
import com.inter.proyecto_intergrupo.model.parametric.Reclassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclassificationRepository extends JpaRepository<Reclassification,Long> {
    List<Reclassification> findAll();
    Reclassification findByNitContraparte(Long nit);

}
