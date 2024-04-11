package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.BalvaloresIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.ValoresIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValoresIcrvRepository extends JpaRepository<ValoresIcrv,Long> {
    List<ValoresIcrv> findAll();
    ValoresIcrv findByIdValores(Long idBal);
    void deleteByPeriodo(String periodo);
    List<ValoresIcrv> findByPeriodo(String periodo);
}
