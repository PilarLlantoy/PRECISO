package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YntpSocietyRepository extends JpaRepository<YntpSociety,String> {
    List<YntpSociety> findAll();
    YntpSociety findByYntp(String id);
    YntpSociety findBySociedadDescripcionCorta(String id);
}
