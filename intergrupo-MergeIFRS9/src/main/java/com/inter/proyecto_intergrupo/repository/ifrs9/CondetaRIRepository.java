package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.CondetaRI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CondetaRIRepository extends JpaRepository<CondetaRI,String> {
    List<CondetaRI> findByCentroAndAndCuenta(String cuenta, String centro);
}
