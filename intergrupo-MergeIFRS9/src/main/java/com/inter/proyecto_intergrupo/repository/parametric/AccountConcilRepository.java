package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountConcil;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountConcilRepository extends JpaRepository<AccountConcil,Integer> {
    AccountConcil findAllById(int id);
    List<AccountConcil> findByEstado(boolean estado);

    List<AccountConcil> findByEstadoAndConciliacion(boolean estado, Conciliation conciliacion);

}
