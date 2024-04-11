package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.BalvaloresIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.UVRIcrf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UVRIcrfRepository extends JpaRepository<UVRIcrf, Date> {
    List<UVRIcrf> findAll();
    UVRIcrf findByFecha(Date fecha);
    void deleteByPeriodo(String periodo);
    void deleteByFecha(Date fecha);

    List<UVRIcrf> findByFechaAndPeriodo(Date fecha, String periodo);
    List<UVRIcrf> findByPesoCopUvrAndPeriodo(Double pesoCopUvr, String periodo);
    List<UVRIcrf> findByVariacionAnualAndPeriodo(Double variacionAnual, String periodo);

    List<UVRIcrf> findByPeriodo(String periodo);
}
