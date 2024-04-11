package com.inter.proyecto_intergrupo.repository.reports;

import com.inter.proyecto_intergrupo.model.reports.Ifrs9Report;
import com.inter.proyecto_intergrupo.model.reports.Rp21;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Ifrs9ReportRepository extends JpaRepository<Ifrs9Report,Long> {
    List<Ifrs9Report> findAll();
}
