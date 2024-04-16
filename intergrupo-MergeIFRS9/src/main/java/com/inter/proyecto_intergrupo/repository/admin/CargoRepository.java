package com.inter.proyecto_intergrupo.repository.admin;

import com.inter.proyecto_intergrupo.model.admin.Cargo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoRepository extends CrudRepository<Cargo,Integer> {
    Cargo findByNombre(String nombre);
    Cargo findById(int id);
    List<Cargo> findAll();
    List<Cargo> findByEstado(boolean estado);
}
