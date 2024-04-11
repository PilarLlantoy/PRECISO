package com.inter.proyecto_intergrupo.repository.reports;

import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.model.reports.Anexo8Finrep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Anexo8FinrepRepository extends JpaRepository<Anexo8Finrep,String> {
    List<Anexo8Finrep> findAll();
    Anexo8Finrep findByCuenta(String cuenta);
    void deleteByCuenta(String cuenta);
    List<Anexo8Finrep> findByCuentaContainingIgnoreCase(String cuenta);
}
