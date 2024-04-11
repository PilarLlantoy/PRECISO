package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.ParamFechas;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParamFechasRepository extends JpaRepository<ParamFechas,Long> {
    List<ParamFechas> findAll();
    ParamFechas findByIdFecha(Long idFecha);
    void deleteByIdFecha(Long idFecha);
    List<ParamFechas> findByAnoContainingIgnoreCase(String ano);
    List<ParamFechas> findByMesContainingIgnoreCase(String mes);
    List<ParamFechas> findByBalanceContainingIgnoreCase(String balance);
    List<ParamFechas> findByPygContainingIgnoreCase(String pyg);
    List<ParamFechas> findByQaplicaContainingIgnoreCase(String qaplica);
}
