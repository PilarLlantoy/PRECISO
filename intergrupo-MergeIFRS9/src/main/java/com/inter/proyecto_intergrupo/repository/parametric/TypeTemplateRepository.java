package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Ciiu;
import com.inter.proyecto_intergrupo.model.parametric.TypeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeTemplateRepository extends JpaRepository<TypeTemplate,String> {
    List<TypeTemplate> findAll();
    List<TypeTemplate> findByNombreArchivo(String nombreArchivo);
}
