package com.inter.proyecto_intergrupo.repository.temporal;

import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.temporal.YntpSocietyTemporal;
import org.hibernate.annotations.SelectBeforeUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SelectBeforeUpdate(value=false)
public interface YntpSocietyTemporalRepository extends JpaRepository<YntpSocietyTemporal,String> {
    List<YntpSocietyTemporal> findAll();
    YntpSociety findByYntp(String id);
    YntpSociety findBySociedadDescripcionCorta(String id);
}
