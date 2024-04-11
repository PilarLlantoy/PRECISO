package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.GarantiasActivasIcrf;
import com.inter.proyecto_intergrupo.model.briefcase.ReposIcrf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReposIcrfRepository extends JpaRepository<ReposIcrf,Long> {
    List<ReposIcrf> findAll();
    ReposIcrf findByIdReport(Long id);
    void deleteByPeriodo(String periodo);
    List<ReposIcrf> findByPeriodo(String periodo);
}
