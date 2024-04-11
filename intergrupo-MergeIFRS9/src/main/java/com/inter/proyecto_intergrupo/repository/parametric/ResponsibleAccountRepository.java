package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.ResponsibleAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponsibleAccountRepository extends JpaRepository<ResponsibleAccount,Long> {
    List<ResponsibleAccount> findAll();
    ResponsibleAccount findByCuentaLocal(Long id);
}
