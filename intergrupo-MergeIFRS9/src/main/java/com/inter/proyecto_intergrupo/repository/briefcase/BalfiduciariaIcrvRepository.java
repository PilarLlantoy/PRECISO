package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.BalfiduciariaIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalfiduciariaIcrvRepository extends JpaRepository<BalfiduciariaIcrv,Long> {
    List<BalfiduciariaIcrv> findAll();
    BalfiduciariaIcrv findByIdBal(Long idBal);
    void deleteByPeriodo(String periodo);
    void deleteByIdBal(Long idBal);
    List<BalfiduciariaIcrv> findByCorteAndPeriodo(String corte, String periodo);
    List<BalfiduciariaIcrv> findByCodigoContabilidadAndPeriodo(String codigoContabilidad, String periodo);
    List<BalfiduciariaIcrv> findByNombreFideicomisoAndPeriodo(String nombreFideicomiso, String periodo);
    List<BalfiduciariaIcrv> findByAnoAndPeriodo(String ano, String periodo);
    List<BalfiduciariaIcrv> findByMesAndPeriodo(String mes, String periodo);
    List<BalfiduciariaIcrv> findByCodigoPucAndPeriodo(String codigoPuc, String periodo);
    List<BalfiduciariaIcrv> findByCodigoCuentaNiifAndPeriodo(String codigoCuentaNiif, String periodo);
    List<BalfiduciariaIcrv> findByCodigoPucLocalAndPeriodo(String codigoPucLocal, String periodo);
    List<BalfiduciariaIcrv> findByNombreAndPeriodo(String nombre, String periodo);
    List<BalfiduciariaIcrv> findByNivelAndPeriodo(String nivel, String periodo);
    List<BalfiduciariaIcrv> findByCodiconsAndPeriodo(String codicons, String periodo);
    List<BalfiduciariaIcrv> findByCodigestAndPeriodo(String codigest, String periodo);
    List<BalfiduciariaIcrv> findByL4AndPeriodo(String l4, String periodo);
    List<BalfiduciariaIcrv> findBySaldoFinalAndPeriodo(Double saldoFinal, String periodo);
    List<BalfiduciariaIcrv> findByPeriodo(String periodo);
}
