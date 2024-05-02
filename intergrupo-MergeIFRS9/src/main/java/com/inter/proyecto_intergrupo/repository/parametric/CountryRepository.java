package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country,Integer> {
    List<Country> findAllByOrderByNombreAsc();
    Country findAllById(int id);
    List<Country> findByEstado(boolean estado);
    Country findAllByNombre(String id);
}
