package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.ParamInforme;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParamInformeRepository extends JpaRepository<ParamInforme,Long> {
    List<ParamInforme> findAll();
    ParamInforme findByIdNic34(Long idNic34);
    void deleteByIdNic34(Long idNic34);
    List<ParamInforme> findByAgrupa1ContainingIgnoreCase(String agrupa1);
    List<ParamInforme> findByAplicaQueryContainingIgnoreCase(String aplicaQuery);
    List<ParamInforme> findByAgrupa2ContainingIgnoreCase(String agrupa2);
    List<ParamInforme> findByIdGContainingIgnoreCase(String idG);
    List<ParamInforme> findByMonedaContainingIgnoreCase(String moneda);
    List<ParamInforme> findByConceptoContainingIgnoreCase(String concepto);
    List<ParamInforme> findByCondicionContainingIgnoreCase(String condicion);
    List<ParamInforme> findByNotasContainingIgnoreCase(String nota);
    List<ParamInforme> findByAplicaContainingIgnoreCase(String aplica);
}
