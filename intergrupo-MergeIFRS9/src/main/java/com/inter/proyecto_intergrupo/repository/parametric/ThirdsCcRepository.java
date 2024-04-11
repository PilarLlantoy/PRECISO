package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.ThirdsCc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface ThirdsCcRepository extends JpaRepository<ThirdsCc,String> {
    List<ThirdsCc> findAll();
    ThirdsCc findByNit(String nit);
    void deleteByNit(String nit);
    List<ThirdsCc> findByNitContainingIgnoreCase(String nit);
    List<ThirdsCc> findByNombreContainingIgnoreCase(String nombre);
    List<ThirdsCc> findByImpuestoContainingIgnoreCase(String nombre);
    List<ThirdsCc> findByTelefonoContainingIgnoreCase(String telefono);
    List<ThirdsCc> findByCorreoContainingIgnoreCase(String correo);
    List<ThirdsCc> findByCorreoAlternoContainingIgnoreCase(String correo);
    List<ThirdsCc> findByCorreoAlterno2ContainingIgnoreCase(String correo);
    List<ThirdsCc> findByDireccionContainingIgnoreCase(String direccion);
    List<ThirdsCc> findByCorreoCopia1ContainingIgnoreCase(String correo);
    List<ThirdsCc> findByCorreoCopia2ContainingIgnoreCase(String correo);
}
