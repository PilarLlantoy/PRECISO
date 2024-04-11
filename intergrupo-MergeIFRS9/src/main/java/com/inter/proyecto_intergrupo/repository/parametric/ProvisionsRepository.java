package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Provisions;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProvisionsRepository extends JpaRepository<Provisions,String> {
    List<Provisions> findAll();
    Provisions findByCuentaNeocon(String id);

}
