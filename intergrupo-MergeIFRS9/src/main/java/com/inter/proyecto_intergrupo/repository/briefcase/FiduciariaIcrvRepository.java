package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.FiduciariaIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.ValoresIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FiduciariaIcrvRepository extends JpaRepository<FiduciariaIcrv,Long> {
    List<FiduciariaIcrv> findAll();
    FiduciariaIcrv findByIdFiduciaria(Long idBal);
    void deleteByPeriodo(String periodo);
    List<FiduciariaIcrv> findByPeriodo(String periodo);
}
