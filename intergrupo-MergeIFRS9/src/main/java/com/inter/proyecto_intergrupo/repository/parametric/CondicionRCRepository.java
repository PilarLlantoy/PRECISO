package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.model.parametric.CondicionRC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CondicionRCRepository extends JpaRepository<CondicionRC,Integer> {
    CondicionRC findAllById(int id);
    List<CondicionRC> findByEstado(boolean estado);
}
