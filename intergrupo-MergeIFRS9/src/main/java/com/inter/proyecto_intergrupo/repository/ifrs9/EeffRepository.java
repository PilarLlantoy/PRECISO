package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.Eeff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EeffRepository extends JpaRepository<Eeff,String>{
    List<Eeff> findAll();
    List<Eeff> findAllByPeriodo(String periodo);
}
