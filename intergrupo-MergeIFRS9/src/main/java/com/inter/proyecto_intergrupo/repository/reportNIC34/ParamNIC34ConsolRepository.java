package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.ParamNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamNIC34Consol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParamNIC34ConsolRepository extends JpaRepository<ParamNIC34Consol,Long> {
    List<ParamNIC34Consol> findAll();
    ParamNIC34Consol findByIdNic34(Long idNic34);
    void deleteByIdNic34(Long idMda);
    List<ParamNIC34Consol> findByL6Like(String l6);
    List<ParamNIC34Consol> findByCuentaLike(String cuenta);
    List<ParamNIC34Consol> findByIdGrupoLike(String idGrupo);
    List<ParamNIC34Consol> findByGrupoLike(String grupo);
    List<ParamNIC34Consol> findByAplicaLike(String aplica);
    List<ParamNIC34Consol> findByIdNotaLike(String idNota);
    List<ParamNIC34Consol> findByNotaLike(String nota);
    List<ParamNIC34Consol> findByIdSubnotaLike(String idSubnota);
    List<ParamNIC34Consol> findBySubnotaLike(String subnota);
    List<ParamNIC34Consol> findByIdCampoLike(String idCampo);
    List<ParamNIC34Consol> findByCampoLike(String campo);
    List<ParamNIC34Consol> findByMonedaLike(String moneda);
    List<ParamNIC34Consol> findByResponsableLike(String responsable);
}
