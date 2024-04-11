package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.Equivalences;
import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RejectionIdP2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquivalencesRepository extends JpaRepository<Equivalences,String> {
    List<Equivalences> findAll();
    Equivalences findByCuentaContable(String id);
}
