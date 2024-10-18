package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.CondicionEventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.CondicionRC;
import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CondicionMERepository;
import com.inter.proyecto_intergrupo.repository.parametric.CondicionRCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class CondicionMEService {

    @Autowired
    private final CondicionMERepository condicionMERepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public CondicionMEService(CondicionMERepository condicionMERepository) {
        this.condicionMERepository = condicionMERepository;
    }

    public List <CondicionEventMatrix> findAll(){return condicionMERepository.findAll();}
    public List<CondicionEventMatrix> findAllActive() {
        return condicionMERepository.findByEstado(true);
    }
    public List<CondicionEventMatrix> findByMatrizEvento(EventMatrix matriz) {
        return condicionMERepository.findByMatrizEvento(matriz);
    }

    public CondicionEventMatrix findById(int id){
        return condicionMERepository.findAllById(id);
    }


    public CondicionEventMatrix modificar(CondicionEventMatrix condicion){
        condicionMERepository.save(condicion);
       return condicion;
    }

    public void deleteById(int id){
        condicionMERepository.deleteById(id);
    }


}
