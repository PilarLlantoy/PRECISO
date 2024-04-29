package com.inter.proyecto_intergrupo.repository.admin;

import com.inter.proyecto_intergrupo.model.admin.TipoDocumento;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoDocumentoRepository extends CrudRepository<TipoDocumento,Integer> {

    TipoDocumento findById(int id);
    List<TipoDocumento> findAll();
    List<TipoDocumento> findByEstado(boolean estado);
    TipoDocumento findByNombre(String nombre);
}
