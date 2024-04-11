package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.CalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PduIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PduIcrvRepository extends JpaRepository<PduIcrv,Long> {
    List<PduIcrv> findAll();
    PduIcrv findByIdPdu(Long id);
    void deleteByPeriodo(String periodo);
    List<PduIcrv> findByNoisinAndPeriodo(String noisin, String periodo);
    List<PduIcrv> findByGrupoAndPeriodo(String grupo, String periodo);
    List<PduIcrv> findByEntidadAndPeriodo(String entidad, String periodo);
    List<PduIcrv> findByPorcentajeAndPeriodo(Double porcentaje, String periodo);
    List<PduIcrv> findByFechaAsambleaAndPeriodo(String fechaAsamblea, String periodo);
    List<PduIcrv> findByFechaCausacionAndPeriodo(String fechaCausacion, String periodo);
    List<PduIcrv> findByUtilidadDelEjercicioAndPeriodo(Double utilidadDelEjercicio, String periodo);
    List<PduIcrv> findByReservaNoDistribuidaAndPeriodo(Double reservaNoDistribuida, String periodo);
    List<PduIcrv> findByUtilidadDistribuirAndPeriodo(Double utilidadDistribuir, String periodo);
    List<PduIcrv> findByDividendosRecibidosAndPeriodo(Double dividendosRecibidos, String periodo);
    List<PduIcrv> findByPorcentajeEfectivoAndPeriodo(Double porcentajeEfectivo, String periodo);
    List<PduIcrv> findByEfectivoAndPeriodo(Double efectivo, String periodo);
    List<PduIcrv> findByPorcentajeAccionAndPeriodo(Double porcentajeAccion, String periodo);
    List<PduIcrv> findByAccionAndPeriodo(Double accion, String periodo);
    List<PduIcrv> findByTotalAndPeriodo(Double total, String periodo);
    List<PduIcrv> findByValidacionAndPeriodo(Double validacion, String periodo);
    List<PduIcrv> findByAplicaRetfuenteAndPeriodo(String aplicaRetfuente, String periodo);
    List<PduIcrv> findByRetencionEnFuenteAndPeriodo(Double retencionEnFuente, String periodo);
    List<PduIcrv> findByValorRecibirAndPeriodo(Double valorRecibir, String periodo);
    List<PduIcrv> findByFechasDePago1AndPeriodo(String fechasDePago1, String periodo);
    List<PduIcrv> findByFechasDePago2AndPeriodo(String fechasDePago2, String periodo);
    List<PduIcrv> findByFechasDePago3AndPeriodo(String fechasDePago3, String periodo);
    List<PduIcrv> findByValorDividendosPago1AndPeriodo(Double valorDividendosPago1, String periodo);
    List<PduIcrv> findByValorDividendosPago2AndPeriodo(Double valorDividendosPago2, String periodo);
    List<PduIcrv> findByValorDividendosPago3AndPeriodo(Double valorDividendosPago3, String periodo);
    List<PduIcrv> findByCorreoAndPeriodo(Double correo, String periodo);
    List<PduIcrv> findByPeriodo(String periodo);
}
