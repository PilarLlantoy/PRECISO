package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.ValidationRC;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRConcil;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ValidationRConcilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class ValidationRConcilService {

    @Autowired
    private final ValidationRConcilRepository validationRCRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public ValidationRConcilService(ValidationRConcilRepository validationRCRepository) {
        this.validationRCRepository = validationRCRepository;
    }

    public List <ValidationRConcil> findAll(){return validationRCRepository.findAll();}
    public List<ValidationRConcil> findAllActive() {
        return validationRCRepository.findByEstado(true);
    }
    public ValidationRConcil findById(int id){
        return validationRCRepository.findAllById(id);
    }

    public ValidationRConcil modificar(ValidationRConcil campo){
        validationRCRepository.save(campo);
       return campo;
    }

}
