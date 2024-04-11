package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RejectionIdP1;
import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RejectionIdP2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RejectionIdP2Repository extends JpaRepository<RejectionIdP2,String> {
    List<RejectionIdP2> findAll();
    RejectionIdP2 findByLineaProducto(String id);
}
