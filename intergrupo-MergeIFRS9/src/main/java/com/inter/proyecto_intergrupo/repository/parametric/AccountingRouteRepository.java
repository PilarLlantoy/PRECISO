package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountingRouteRepository extends JpaRepository<AccountingRoute,Integer> {
    List<AccountingRoute> findAllByOrderByNombreAsc();
    AccountingRoute findAllById(int id);
    List<AccountingRoute> findByEstado(boolean estado);
    AccountingRoute findAllByNombre(String nombre);
}