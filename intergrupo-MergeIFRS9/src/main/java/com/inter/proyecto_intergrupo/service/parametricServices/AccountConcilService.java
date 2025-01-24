package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AccountConcil;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRC;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountConcilRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ValidationRCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class AccountConcilService {

    @Autowired
    private final AccountConcilRepository accountConcilRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public AccountConcilService(AccountConcilRepository accountConcilRepository) {
        this.accountConcilRepository = accountConcilRepository;
    }

    public List <AccountConcil> findAll(){return accountConcilRepository.findAll();}
    public List<AccountConcil> findAllActive() {
        return accountConcilRepository.findByEstado(true);
    }

    public AccountConcil findById(int id){
        return accountConcilRepository.findAllById(id);
    }

    public List <AccountConcil> findByEstadoAndConciliacion(Conciliation conciliacion){return accountConcilRepository.findByEstadoAndConciliacion(true, conciliacion);}

    public AccountConcil modificar(AccountConcil cuenta){
        accountConcilRepository.save(cuenta);
       return cuenta;
    }

    public void eliminar(int idCuenta) {
        accountConcilRepository.deleteById(idCuenta);
    }


}
