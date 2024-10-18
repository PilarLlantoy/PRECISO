package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.Campo;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class CampoService {

    @Autowired
    private final CampoRepository campoRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public CampoService(CampoRepository campoRepository) {
        this.campoRepository = campoRepository;
    }

    public List <Campo> findAll(){return campoRepository.findAllByOrderByNombreAsc();}
    public List<Campo> findAllActiveCountries() {
        return campoRepository.findByEstado(true);
    }

    public Campo findById(int id){
        return campoRepository.findAllById(id);
    }

    public Campo findByName(String nombre){
        return campoRepository.findAllByNombre(nombre);
    }

    public Campo modificar(Campo campo){
        campoRepository.save(campo);
       return campo;
    }



}
