package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.Campo;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRCRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class CampoRCService {

    @Autowired
    private final CampoRCRepository campoRCRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public CampoRCService(CampoRCRepository campoRCRepository) {
        this.campoRCRepository = campoRCRepository;
    }

    public List <CampoRC> findAll(){return campoRCRepository.findAllByOrderByNombreAsc();}
    public List<CampoRC> findAllActive() {
        return campoRCRepository.findByEstado(true);
    }

    public CampoRC findById(int id){
        return campoRCRepository.findAllById(id);
    }

    public CampoRC findByName(String nombre){
        return campoRCRepository.findAllByNombre(nombre);
    }

    public CampoRC modificar(CampoRC campo){
        campoRCRepository.save(campo);
       return campo;
    }

    public List<String> validatePrincipal(String principal)
    {
        Query validate = entityManager.createNativeQuery("SELECT nv.nombre FROM preciso_campos_rc as nv WHERE " +
                "nv.visualizacion=1 and nv.estado=1 and nv.id_rc = 1");
        //validate.setParameter(1,principal);
        return validate.getResultList();
    }



}
