package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.ValidationME;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationMERepository extends JpaRepository<ValidationME,Integer> {
    ValidationME findAllById(int id);
    List<ValidationME> findByEstado(boolean estado);

    List<ValidationME> findByMatrizEvento(EventMatrix matriz);
}
