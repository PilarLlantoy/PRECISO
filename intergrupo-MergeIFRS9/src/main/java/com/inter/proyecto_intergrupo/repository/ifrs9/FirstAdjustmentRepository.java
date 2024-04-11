package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.bank.GpsReport;
import com.inter.proyecto_intergrupo.model.ifrs9.FirstAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FirstAdjustmentRepository  extends JpaRepository<FirstAdjustment,String>{
    List<FirstAdjustment> findAll();

}
