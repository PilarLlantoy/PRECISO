package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.TypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypeEntityRepository extends JpaRepository<TypeEntity,Long> {
    List<TypeEntity> findAll();
    TypeEntity findByIdTipoEntidad(Long id);
}
