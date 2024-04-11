package com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricEEFF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametricEEFFRepository  extends JpaRepository<ParametricEEFF,Long> {

    List<ParametricEEFF> findAll();
    ParametricEEFF findByIdTipoParametro(Long id);
    void deleteByIdTipoParametro(Long idTipoParametro);
    List<ParametricEEFF> findByParametroContainingIgnoreCase(String parametro);
    List<ParametricEEFF> findByConceptoContainingIgnoreCase(String concepto);
    List<ParametricEEFF> findByCuentaContainingIgnoreCase(String cuenta);

    List<ParametricEEFF> findByCuenta2ContainingIgnoreCase(String cuenta2);
    List<ParametricEEFF> findByPorcentajeGreaterThanEqual(Double porcentaje);

}
