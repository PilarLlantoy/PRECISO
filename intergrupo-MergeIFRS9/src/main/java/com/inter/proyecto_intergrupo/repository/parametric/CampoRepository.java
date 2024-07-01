package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Campo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampoRepository extends JpaRepository<Campo,Integer> {
    List<Campo> findAllByOrderByNombreAsc();
    Campo findAllById(int id);
    List<Campo> findByEstado(boolean estado);
    Campo findAllByNombre(String nombre);
}
