package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.EventTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;

@Service
@Transactional
public class EventTypeService {

    @Autowired
    private final EventTypeRepository eventTypeRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public EventTypeService(EventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    public List<EventType> findAll(){return eventTypeRepository.findAllByOrderByNombreAsc();}
    public List<EventType> findAllActive() {
        return eventTypeRepository.findByEstado(true);
    }


    public EventType findByName(String nombre){
        return eventTypeRepository.findAllByNombre(nombre);
    }


    public EventType findAllById(int id){
        return eventTypeRepository.findAllById(id);
    }

    public EventType modificar(EventType pais){
        eventTypeRepository.save(pais);
       return pais;
    }

    public Page<EventType> getAll(Pageable pageable){
        return eventTypeRepository.findAll(pageable);
    }


}
