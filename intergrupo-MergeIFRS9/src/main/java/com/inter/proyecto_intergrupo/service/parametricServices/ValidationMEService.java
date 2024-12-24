package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.CondicionEventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.ValidationME;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRC;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ValidationMERepository;
import com.inter.proyecto_intergrupo.repository.parametric.ValidationRCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class ValidationMEService {

    @Autowired
    private final ValidationMERepository validationMERepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public ValidationMEService(ValidationMERepository validationMERepository) {
        this.validationMERepository = validationMERepository;
    }

    public List <ValidationME> findAll(){return validationMERepository.findAll();}
    public List<ValidationME> findAllActive() {
        return validationMERepository.findByEstado(true);
    }
    public List <ValidationME> findByEventMatrix(EventMatrix em){return validationMERepository.findByMatrizEvento(em);}

    public ValidationME findById(int id){
        return validationMERepository.findAllById(id);
    }


    public ValidationME modificar(ValidationME validationME){
        validationMERepository.save(validationME);
       return validationME;
    }

    public void deleteById(int id){
        validationMERepository.deleteById(id);
    }


}
