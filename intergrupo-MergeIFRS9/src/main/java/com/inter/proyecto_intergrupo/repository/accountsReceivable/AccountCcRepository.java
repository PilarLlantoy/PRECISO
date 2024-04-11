package com.inter.proyecto_intergrupo.repository.accountsReceivable;

import com.inter.proyecto_intergrupo.model.accountsReceivable.AccountCc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountCcRepository extends JpaRepository<AccountCc,Long> {
    List<AccountCc> findAll();
    AccountCc findByIdCuentas(Long id);
    void deleteByIdCuentas(Long idCuenta);
    List<AccountCc> findByConceptoContainingIgnoreCase(String concepto);
    List<AccountCc> findByCuentaContainingIgnoreCase(String cuenta);
    List<AccountCc> findByCentroContainingIgnoreCase(String centro);
    List<AccountCc> findByNaturalezaContainingIgnoreCase(String naturaleza);
    List<AccountCc> findByImpuestoContainingIgnoreCase(String impuesto);
    List<AccountCc> findByEventoContainingIgnoreCase(String evento);
}
