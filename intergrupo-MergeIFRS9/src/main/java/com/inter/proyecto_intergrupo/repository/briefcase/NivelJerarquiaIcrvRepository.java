package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.FiduciariaIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.NivelJerarquiaIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NivelJerarquiaIcrvRepository extends JpaRepository<NivelJerarquiaIcrv,Long> {
    List<NivelJerarquiaIcrv> findAll();
    NivelJerarquiaIcrv findByIdNivel(Long idBal);
    void deleteByPeriodo(String periodo);
    List<NivelJerarquiaIcrv> findByPeriodo(String periodo);
}
