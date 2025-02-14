package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Typification;
import com.inter.proyecto_intergrupo.model.parametric.TypificationConcil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypificationConcilRepository extends JpaRepository<TypificationConcil,Long> {
    List<TypificationConcil> findAll();
    Typification findAllById(Long id);

    void deleteByTipificacion (Typification typification);

}
