package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventTypeRepository extends JpaRepository<EventType,Integer> {
    List<EventType> findAllByOrderByNombreAsc();
    EventType findAllById(int id);
    List<EventType> findByEstado(boolean estado);
    List<EventType> findAllByNombreIgnoreCase(String id);
}
