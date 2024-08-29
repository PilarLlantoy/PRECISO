package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Campo;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampoRCRepository extends JpaRepository<CampoRC,Integer> {
    List<CampoRC> findAllByOrderByNombreAsc();
    CampoRC findAllById(int id);
    List<CampoRC> findByEstado(boolean estado);
    CampoRC findAllByNombre(String nombre);
}
