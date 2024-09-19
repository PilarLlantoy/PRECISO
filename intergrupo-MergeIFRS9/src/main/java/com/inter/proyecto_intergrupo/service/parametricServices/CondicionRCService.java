package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.model.parametric.CondicionRC;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRCRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CondicionRCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class CondicionRCService {

    @Autowired
    private final CondicionRCRepository condicionRCRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public CondicionRCService(CondicionRCRepository condicionRCRepository) {
        this.condicionRCRepository = condicionRCRepository;
    }

    public List <CondicionRC> findAll(){return condicionRCRepository.findAll();}
    public List<CondicionRC> findAllActive() {
        return condicionRCRepository.findByEstado(true);
    }

    public CondicionRC findById(int id){
        return condicionRCRepository.findAllById(id);
    }


    public CondicionRC modificar(CondicionRC campo){
        condicionRCRepository.save(campo);
       return campo;
    }

    public void deleteById(int id){
        condicionRCRepository.deleteById(id);
    }


}
