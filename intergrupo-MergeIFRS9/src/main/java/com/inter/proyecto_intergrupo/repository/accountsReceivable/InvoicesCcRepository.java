package com.inter.proyecto_intergrupo.repository.accountsReceivable;

import com.inter.proyecto_intergrupo.model.accountsReceivable.AccountCc;
import com.inter.proyecto_intergrupo.model.accountsReceivable.InvoicesCc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoicesCcRepository extends JpaRepository<InvoicesCc,Long> {
    List<InvoicesCc> findAll();
    List<InvoicesCc> findAllByPeriodo(String periodo);
    InvoicesCc findByIdFactura(Long idFactura);
    List<InvoicesCc> findByLote(String lote);
    void deleteByIdFactura(Long idFactura);
}
