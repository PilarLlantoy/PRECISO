package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.OperationAccount;
import com.inter.proyecto_intergrupo.model.parametric.ResponsibleAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationAccountRepository extends JpaRepository<OperationAccount,String> {
    List<OperationAccount> findAll();
    OperationAccount findByCuentaLocal(String id);
}
