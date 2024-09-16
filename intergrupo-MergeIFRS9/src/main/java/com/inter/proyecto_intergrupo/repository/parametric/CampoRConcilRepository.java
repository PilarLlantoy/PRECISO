package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.CampoRConcil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampoRConcilRepository extends JpaRepository<CampoRConcil,Integer> {
    List<CampoRConcil> findAllByOrderByNombreAsc();
    CampoRConcil findAllById(int id);
    List<CampoRConcil> findByEstado(boolean estado);
    CampoRConcil findAllByNombre(String nombre);
}
