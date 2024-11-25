package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.model.parametric.GeneralParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneralParamRepository extends JpaRepository<GeneralParam,Long> {
    List<GeneralParam> findAllByOrderByUnidadPrincipalAsc();
    GeneralParam findAllById(Long id);
    List<GeneralParam> findByUnidadSecundaria(String unidadSecundaria);
    List<GeneralParam> findByUnidadPrincipal(String unidadPrincipal);
}
