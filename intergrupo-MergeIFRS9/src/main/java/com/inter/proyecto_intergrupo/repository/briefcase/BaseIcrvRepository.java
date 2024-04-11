package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.BaseIcrv;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaseIcrvRepository extends JpaRepository<BaseIcrv,Long> {
    List<BaseIcrv> findAll();
    BaseIcrv findByIdBase(Long id);
    void deleteByIdBase(Long idBase);
    List<BaseIcrv> findByCuentaContainingIgnoreCase(String cuenta);
    List<BaseIcrv> findByEmpresaContainingIgnoreCase(String empresa);
    List<BaseIcrv> findByNaturalezaContainingIgnoreCase(String naturaleza);
    List<BaseIcrv> findByEventoContainingIgnoreCase(String evento);
    List<BaseIcrv> findByConceptoContainingIgnoreCase(String concepto);
    List<BaseIcrv> findByNoAsignadoContainingIgnoreCase(String noAsignado);
    List<BaseIcrv> findByCodiconsContainingIgnoreCase(String codicons);
    List<BaseIcrv> findByEpigrafeContainingIgnoreCase(String epigrafe);
    List<BaseIcrv> findByDescripcionPlanoContainingIgnoreCase(String descripcionPlano);
    List<BaseIcrv> findByCtaContainingIgnoreCase(String cta);
    List<BaseIcrv> findByDescripcionCtaContainingIgnoreCase(String descripcionCta);
}
