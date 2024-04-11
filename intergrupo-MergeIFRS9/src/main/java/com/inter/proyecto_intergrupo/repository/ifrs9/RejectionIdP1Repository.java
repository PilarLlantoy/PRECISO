package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RejectionIdP1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RejectionIdP1Repository extends JpaRepository<RejectionIdP1,String> {
    List<RejectionIdP1> findAll();
    RejectionIdP1 findByInicialCuenta(String id);
}
