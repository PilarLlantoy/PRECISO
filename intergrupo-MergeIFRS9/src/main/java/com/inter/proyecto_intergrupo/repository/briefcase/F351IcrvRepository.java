package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.CalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.F351Icrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface F351IcrvRepository extends JpaRepository<F351Icrv,Long> {
    List<F351Icrv> findAll();
    F351Icrv findByIdF(Long id);
    void deleteByPeriodo(String periodo);
    List<F351Icrv> findByPeriodo(String periodo);
}