package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.CondicionRC;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRC;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CondicionRCRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ValidationRCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class ValidationRCService {

    @Autowired
    private final ValidationRCRepository validationRCRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public ValidationRCService(ValidationRCRepository validationRCRepository) {
        this.validationRCRepository = validationRCRepository;
    }

    public List <ValidationRC> findAll(){return validationRCRepository.findAll();}
    public List<ValidationRC> findAllActive() {
        return validationRCRepository.findByEstado(true);
    }

    public ValidationRC findById(int id){
        return validationRCRepository.findAllById(id);
    }


    public ValidationRC modificar(ValidationRC campo){
        validationRCRepository.save(campo);
       return campo;
    }

    public void deleteById(int id){
        validationRCRepository.deleteById(id);
    }


}
