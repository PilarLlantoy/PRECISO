package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreation;
import com.inter.proyecto_intergrupo.model.ifrs9.Centers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountCreationRepository extends JpaRepository<AccountCreation,Long> {
    List<AccountCreation> findAll();
    List<AccountCreation> findByIdCreacionCuentas(Long id);
}