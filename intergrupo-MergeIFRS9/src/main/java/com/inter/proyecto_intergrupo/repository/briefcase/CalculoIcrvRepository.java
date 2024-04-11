package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.CalculoIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculoIcrvRepository extends JpaRepository<CalculoIcrv,Long> {
    List<CalculoIcrv> findAll();
    CalculoIcrv findByIdCalculo(Long id);
    void deleteByPeriodo(String periodo);
    List<CalculoIcrv> findByValoracionAndPeriodo(String valoracion, String periodo);
    List<CalculoIcrv> findByNitAndPeriodo(String nit, String periodo);
    List<CalculoIcrv> findByEmpresaAndPeriodo(String empresa, String periodo);
    List<CalculoIcrv> findByDvAndPeriodo(String dv, String periodo);
    List<CalculoIcrv> findByIsinAndPeriodo(String isin, String periodo);
    List<CalculoIcrv> findByParticipacionAndPeriodo(Double participacion, String periodo);
    List<CalculoIcrv> findByVrAccionAndPeriodo(Double vrAccion, String periodo);
    List<CalculoIcrv> findByNoAccionesAndPeriodo(Integer noAcciones, String periodo);
    List<CalculoIcrv> findByValorNominalAndPeriodo(Double valorNominal, String periodo);
    List<CalculoIcrv> findByPrecioAndPeriodo(Double precio, String periodo);
    List<CalculoIcrv> findByVrPatrimonioAndPeriodo(Double vrPatrimonio, String periodo);
    List<CalculoIcrv> findByVrMercadoAndPeriodo(Double vrMercado, String periodo);
    List<CalculoIcrv> findBySaldoLibrosValoracionAndPeriodo(Double saldoLibrosValoracion, String periodo);
    List<CalculoIcrv> findByAjusteAndPeriodo(Double ajuste, String periodo);
    List<CalculoIcrv> findByPeriodo(String periodo);
}
