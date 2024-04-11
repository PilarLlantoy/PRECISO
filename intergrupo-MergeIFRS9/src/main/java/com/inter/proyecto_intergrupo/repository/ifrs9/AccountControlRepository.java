package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.AccountControl;
import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.Equivalences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountControlRepository extends JpaRepository<AccountControl,String> {
    List<AccountControl> findAll();
    AccountControl findByCUENTA(String id);
}
