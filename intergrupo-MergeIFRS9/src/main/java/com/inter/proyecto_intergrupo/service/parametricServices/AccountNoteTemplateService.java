package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AccountEventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.AccountNoteTemplate;
import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountEventMatrixRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountNoteTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class AccountNoteTemplateService {

    @Autowired
    private final AccountNoteTemplateRepository accountNoteTemplateRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public AccountNoteTemplateService(AccountNoteTemplateRepository accountNoteTemplateRepository) {
        this.accountNoteTemplateRepository = accountNoteTemplateRepository;
    }

    public List <AccountNoteTemplate> findAll(){return accountNoteTemplateRepository.findAll();}
    public List<AccountNoteTemplate> findAllActive() {
        return accountNoteTemplateRepository.findByEstado(true);
    }

    public AccountNoteTemplate findById(int id){
        return accountNoteTemplateRepository.findAllById(id);
    }


    public AccountNoteTemplate modificar(AccountNoteTemplate cuenta){
        accountNoteTemplateRepository.save(cuenta);
       return cuenta;
    }


}
