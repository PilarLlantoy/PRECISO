package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.CondicionRC;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationRCRepository extends JpaRepository<ValidationRC,Integer> {
    ValidationRC findAllById(int id);
    List<ValidationRC> findByEstado(boolean estado);
}
