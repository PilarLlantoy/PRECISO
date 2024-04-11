package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamNIC34;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParamNIC34Repository extends JpaRepository<ParamNIC34,Long> {
    List<ParamNIC34> findAll();
    ParamNIC34 findByIdNic34(Long idMda);
    void deleteByIdNic34(Long idMda);
    List<ParamNIC34> findByL6Like(String l6);
    List<ParamNIC34> findByCuentaLike(String cuenta);
    List<ParamNIC34> findByIdGrupoLike(String idGrupo);
    List<ParamNIC34> findByGrupoLike(String grupo);
    List<ParamNIC34> findByAplicaLike(String aplica);
    List<ParamNIC34> findByIdNotaLike(String idNota);
    List<ParamNIC34> findByNotaLike(String nota);
    List<ParamNIC34> findByIdSubnotaLike(String idSubnota);
    List<ParamNIC34> findBySubnotaLike(String subnota);
    List<ParamNIC34> findByIdCampoLike(String idCampo);
    List<ParamNIC34> findByCampoLike(String campo);
    List<ParamNIC34> findByMonedaLike(String moneda);
    List<ParamNIC34> findByResponsableLike(String responsable);
}
