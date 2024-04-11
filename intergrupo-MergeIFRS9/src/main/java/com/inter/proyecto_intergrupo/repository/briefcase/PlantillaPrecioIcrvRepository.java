package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.PlantillaPrecioIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PrecioIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaPrecioIcrvRepository extends JpaRepository<PlantillaPrecioIcrv,Long> {
    List<PlantillaPrecioIcrv> findAll();
    PlantillaPrecioIcrv findByIdPrecio(Long id);
    void deleteByIdPrecio(Long idPrecio);
    List<PlantillaPrecioIcrv> findByMetodo(String metodo);
    List<PlantillaPrecioIcrv> findByEmpresa(String empresa);
}
