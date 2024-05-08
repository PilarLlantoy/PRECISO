package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.SourceSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceSystemRepository extends JpaRepository<SourceSystem,Integer> {
    List<SourceSystem> findAllByOrderByNombreAsc();
    SourceSystem findAllById(int id);
    List<SourceSystem> findByEstado(boolean estado);
    SourceSystem findAllByNombre(String id);
}
