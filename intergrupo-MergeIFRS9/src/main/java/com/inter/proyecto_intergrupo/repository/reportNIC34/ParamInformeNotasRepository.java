package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.ParamInforme;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamInformeNotas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParamInformeNotasRepository extends JpaRepository<ParamInformeNotas,Long> {
    List<ParamInformeNotas> findAll();
    ParamInformeNotas findByIdNic34(Long idNic34);
    void deleteByIdNic34(Long idNic34);
    List<ParamInformeNotas> findByAgrupa1ContainingIgnoreCase(String agrupa1);
    List<ParamInformeNotas> findByAplicaQueryContainingIgnoreCase(String aplicaQuery);
    List<ParamInformeNotas> findByAgrupa2ContainingIgnoreCase(String agrupa2);
    List<ParamInformeNotas> findByIdGContainingIgnoreCase(String idG);
    List<ParamInformeNotas> findByMonedaContainingIgnoreCase(String moneda);
    List<ParamInformeNotas> findByConceptoContainingIgnoreCase(String concepto);
    List<ParamInformeNotas> findByCondicionContainingIgnoreCase(String condicion);
    List<ParamInformeNotas> findByNotasContainingIgnoreCase(String nota);
    List<ParamInformeNotas> findByAplicaContainingIgnoreCase(String aplica);
}
