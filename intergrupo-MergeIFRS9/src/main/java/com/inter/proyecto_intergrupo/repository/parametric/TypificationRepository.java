package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Typification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TypificationRepository extends JpaRepository<Typification,Integer> {
    List<Typification> findAll();
    Typification findAllById(int id);
    List<Typification> findByEstado(boolean estado);
    List<Typification> findByAplicaConcil(boolean aplicaConcil);
    List<Typification> findAllByDetalleIgnoreCase(String detalle);

}
