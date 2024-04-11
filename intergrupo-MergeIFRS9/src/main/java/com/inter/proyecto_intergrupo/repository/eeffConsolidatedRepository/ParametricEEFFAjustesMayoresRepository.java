package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricAjustesMayoresEEFF;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricAjustesMinimosEEFF;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametricEEFFAjustesMayoresRepository extends JpaRepository<ParametricAjustesMayoresEEFF,Long> {

    List<ParametricAjustesMayoresEEFF> findAll();
    List<ParametricAjustesMayoresEEFF> findByPeriodo(String periodo);
    ParametricAjustesMayoresEEFF findByIdTipoParametro(Long id);
    void deleteByIdTipoParametro(Long idTipoParametro);
    List<ParametricAjustesMayoresEEFF> findBycuentaContainingIgnoreCase(String cuenta);
    List<ParametricAjustesMayoresEEFF> findByMonedaContainingIgnoreCase(String moneda);
}
