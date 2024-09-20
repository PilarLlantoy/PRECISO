package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.CrossesConcilRoute;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRConcil;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CrossesConcilRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ValidationRConcilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class CrossesConcilRouteService {

    @Autowired
    private final CrossesConcilRouteRepository crossesRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public CrossesConcilRouteService(CrossesConcilRouteRepository crossesRepository) {
        this.crossesRepository = crossesRepository;
    }

    public List <CrossesConcilRoute> findAll(){return crossesRepository.findAll();}
    public List<CrossesConcilRoute> findAllActive() {
        return crossesRepository.findByEstado(true);
    }
    public CrossesConcilRoute findById(int id){
        return crossesRepository.findAllById(id);
    }

    public CrossesConcilRoute modificar(CrossesConcilRoute campo){
        crossesRepository.save(campo);
       return campo;
    }

    public void deleteById(int id){
        crossesRepository.deleteById(id);
    }



}
