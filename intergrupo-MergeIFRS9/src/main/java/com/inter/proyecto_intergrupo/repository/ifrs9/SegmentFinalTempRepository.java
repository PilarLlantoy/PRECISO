package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.Segmentos;
import com.inter.proyecto_intergrupo.model.ifrs9.SegmentosFinalTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentFinalTempRepository extends JpaRepository<SegmentosFinalTemp,Long> {
    List<SegmentosFinalTemp> findAll();
    List<SegmentosFinalTemp> findByNumeroCliente(String numeroCliente);
}
