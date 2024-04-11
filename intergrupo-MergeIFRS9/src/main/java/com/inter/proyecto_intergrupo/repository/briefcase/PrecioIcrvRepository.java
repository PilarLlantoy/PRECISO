package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.BaseIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PrecioIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrecioIcrvRepository extends JpaRepository<PrecioIcrv,Long> {
    List<PrecioIcrv> findAll();
    PrecioIcrv findByIdPrecio(Long id);
    void deleteByIdPrecio(Long idPrecio);
    void deleteByPeriodo(String periodo);
    List<PrecioIcrv> findByMetodoAndPeriodo(String metodo, String periodo);
    List<PrecioIcrv> findByFechaContableAndPeriodo(String fechaContable, String periodo);
    List<PrecioIcrv> findByEmpresaAndPeriodo(String empresa, String periodo);
    List<PrecioIcrv> findByPrecioValoracionAndPeriodo(Double precioValoracion, String periodo);
    List<PrecioIcrv> findByPatrimonioAndPeriodo(Double patrimonio, String periodo);
    List<PrecioIcrv> findByAccionesAndPeriodo(Double acciones, String periodo);
    List<PrecioIcrv> findByVrIntrinsecoAndPeriodo(Double vrIntrinseco, String periodo);
    List<PrecioIcrv> findByOriAndPeriodo(Double ori, String periodo);
    List<PrecioIcrv> findByFechaReciboAndPeriodo(String fechaRecibo, String periodo);
    List<PrecioIcrv> findByFechaActualizacionAndPeriodo(String fechaActualizacion, String periodo);
    List<PrecioIcrv> findByResultadoAndPeriodo(String resultado, String periodo);
    List<PrecioIcrv> findByIsinAndPeriodo(String isin, String periodo);

    List<PrecioIcrv> findByPeriodo(String periodo);
}
