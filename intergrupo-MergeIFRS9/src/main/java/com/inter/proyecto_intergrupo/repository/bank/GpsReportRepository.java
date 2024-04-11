package com.inter.proyecto_intergrupo.repository.bank;

import com.inter.proyecto_intergrupo.model.bank.GpsReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GpsReportRepository extends JpaRepository<GpsReport,Long> {
    List<GpsReport> findAll();
    List<GpsReport> findByEjercicioMes(String periodo);
}
