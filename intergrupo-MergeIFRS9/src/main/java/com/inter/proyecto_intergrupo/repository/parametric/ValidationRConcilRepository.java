package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.ValidationRC;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRConcil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationRConcilRepository extends JpaRepository<ValidationRConcil,Integer> {
    ValidationRConcil findAllById(int id);
    List<ValidationRConcil> findByEstado(boolean estado);
}
