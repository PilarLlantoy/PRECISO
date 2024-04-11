package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricAjustesMinimosEEFF;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricEEFF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametricEEFFAjustesMinimosRepository extends JpaRepository<ParametricAjustesMinimosEEFF,Long> {

    List<ParametricAjustesMinimosEEFF> findAll();
    ParametricAjustesMinimosEEFF findByIdTipoParametro(Long id);
    void deleteByIdTipoParametro(Long idTipoParametro);
    List<ParametricAjustesMinimosEEFF> findByCuentaOrigenContainingIgnoreCase(String cuentaOrigen);
    List<ParametricAjustesMinimosEEFF> findByEmpresaOrigenContainingIgnoreCase(String empresaOrigen);
    List<ParametricAjustesMinimosEEFF> findByCuentaDestinoGreaterThanEqual(String cuentaDestino);
    List<ParametricAjustesMinimosEEFF> findByEmpresaDestinoContainingIgnoreCase(String empresaDestino);
}
