package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.ReposYSimultaneas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReposYSimultaneasRepository extends JpaRepository<ReposYSimultaneas, String> {
    List<ReposYSimultaneas> findAll();
    List<ReposYSimultaneas> findByCuenta(String cuenta);
}
