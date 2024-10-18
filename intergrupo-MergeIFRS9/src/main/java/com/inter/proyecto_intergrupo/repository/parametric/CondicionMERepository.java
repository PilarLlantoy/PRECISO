package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.CondicionEventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.CondicionRC;
import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CondicionMERepository extends JpaRepository<CondicionEventMatrix,Integer> {
    CondicionEventMatrix findAllById(int id);
    List<CondicionEventMatrix> findByEstado(boolean estado);
    List<CondicionEventMatrix> findByMatrizEvento(EventMatrix matriz);
}
