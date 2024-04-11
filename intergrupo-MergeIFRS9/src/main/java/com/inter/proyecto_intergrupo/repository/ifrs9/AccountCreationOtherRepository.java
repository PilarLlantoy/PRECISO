package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.accountsReceivable.InvoicesCc;
import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreationOther;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountCreationOtherRepository extends JpaRepository<AccountCreationOther,Long> {
    List<AccountCreationOther> findAll();
    List<AccountCreationOther> findAllByPeriodo(String periodo);
    AccountCreationOther findByIdCuentas(Long idCuentas);
}
