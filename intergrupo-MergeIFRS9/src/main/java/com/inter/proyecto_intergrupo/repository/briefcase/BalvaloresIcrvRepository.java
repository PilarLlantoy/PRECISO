package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.BalvaloresIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PrecioIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalvaloresIcrvRepository extends JpaRepository<BalvaloresIcrv,Long> {
    List<BalvaloresIcrv> findAll();
    BalvaloresIcrv findByIdBal(Long idBal);
    void deleteByPeriodo(String periodo);
    void deleteByIdBal(Long idBal);

    List<BalvaloresIcrv> findByMesAndPeriodo(String mes, String periodo);
    List<BalvaloresIcrv> findByCuentaNiifAndPeriodo(String cuentaNiif, String periodo);
    List<BalvaloresIcrv> findByDescripcionAndPeriodo(String descripcion, String periodo);
    List<BalvaloresIcrv> findByMonedaTotalAndPeriodo(Double monedaTotal, String periodo);

    List<BalvaloresIcrv> findByPeriodo(String periodo);
}
