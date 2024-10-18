package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountConcil;
import com.inter.proyecto_intergrupo.model.parametric.AccountEventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountEventMatrixRepository extends JpaRepository<AccountEventMatrix,Integer> {
    AccountEventMatrix findAllById(int id);
    List<AccountEventMatrix> findByEstado(boolean estado);
    AccountEventMatrix findByMatrizEvento(EventMatrix matriz);
    AccountEventMatrix findByMatrizEventoAndTipo(EventMatrix matriz, String tipo);
}
