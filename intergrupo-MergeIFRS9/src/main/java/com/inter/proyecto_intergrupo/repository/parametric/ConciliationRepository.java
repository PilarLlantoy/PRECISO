package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConciliationRepository extends JpaRepository<Conciliation,Integer> {
    List<Conciliation> findAllByOrderByNombreAsc();
    Conciliation findAllById(int id);
    List<Conciliation> findByEstado(boolean estado);
    Conciliation findAllByNombre(String nombre);
}
