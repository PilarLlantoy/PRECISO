package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AccountConcil;
import com.inter.proyecto_intergrupo.model.parametric.AccountEventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountConcilRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountEventMatrixRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class AccountEventMatrixService {

    @Autowired
    private final AccountEventMatrixRepository accountEventMatrixRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public AccountEventMatrixService(AccountEventMatrixRepository accountEventMatrixRepository) {
        this.accountEventMatrixRepository = accountEventMatrixRepository;
    }

    public List <AccountEventMatrix> findAll(){return accountEventMatrixRepository.findAll();}
    public List<AccountEventMatrix> findAllActive() {
        return accountEventMatrixRepository.findByEstado(true);
    }

    public AccountEventMatrix findById(int id){
        return accountEventMatrixRepository.findAllById(id);
    }

    public List<AccountEventMatrix> findByMatrizEvento(EventMatrix matriz){
        return accountEventMatrixRepository.findByMatrizEvento(matriz);
    }

    public AccountEventMatrix findByMatrizEventoTipo1(EventMatrix matriz){
        return accountEventMatrixRepository.findByMatrizEventoAndTipo(matriz, "1");
    }

    public AccountEventMatrix findByMatrizEventoTipo2(EventMatrix matriz){
        return accountEventMatrixRepository.findByMatrizEventoAndTipo(matriz, "2");
    }

    public AccountEventMatrix modificar(AccountEventMatrix cuenta){
        accountEventMatrixRepository.save(cuenta);
       return cuenta;
    }


}
