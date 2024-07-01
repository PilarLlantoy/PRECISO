package com.inter.proyecto_intergrupo.service.parametricServices;


import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.EventMatrixRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class EventMatrixService {

    @Autowired
    private final EventMatrixRepository eventMatrixRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public EventMatrixService(EventMatrixRepository eventMatrixRepository) {
        this.eventMatrixRepository = eventMatrixRepository;
    }

    public List<EventMatrix> findAllActive() {
        return eventMatrixRepository.findByEstado(true);
    }

    public EventMatrix findById(int id){
        return eventMatrixRepository.findAllById(id);
    }

    public EventMatrix modificar(EventMatrix eventMatrix){
        eventMatrixRepository.save(eventMatrix);
       return eventMatrix;
    }

}
