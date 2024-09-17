package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountConcil;
import com.inter.proyecto_intergrupo.model.parametric.CloseDateSourceSystem;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.SourceSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloseDateSourceSystemRepository extends JpaRepository<CloseDateSourceSystem,Integer> {
    CloseDateSourceSystem findAllById(int id);
    List<CloseDateSourceSystem> findByEstado(boolean estado);

    List<CloseDateSourceSystem> findByEstadoAndSistemaFuente(boolean estado, SourceSystem sistemaFuente);

}
