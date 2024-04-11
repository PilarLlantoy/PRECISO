package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.accountsReceivable.AccountCc;
import com.inter.proyecto_intergrupo.model.accountsReceivable.InvoicesCc;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParamMDARepository extends JpaRepository<ParamMDA,Long> {
    List<ParamMDA> findAll();
    List<ParamMDA> findAllByFecha(String fecha);
    ParamMDA findByIdMda(Long idMda);
    void deleteByIdMda(Long idMda);
    List<ParamMDA> findByFechaContainingIgnoreCase(String fecha);
    List<ParamMDA> findByDivisaContainingIgnoreCase(String divisa);
    List<ParamMDA> findByMonedaContainingIgnoreCase(String moneda);
}
