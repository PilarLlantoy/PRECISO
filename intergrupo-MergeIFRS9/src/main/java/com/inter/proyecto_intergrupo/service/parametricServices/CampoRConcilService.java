package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.CampoRConcil;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRCRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRConcilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class CampoRConcilService {

    @Autowired
    private final CampoRConcilRepository campoRCRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public CampoRConcilService(CampoRConcilRepository campoRCRepository) {
        this.campoRCRepository = campoRCRepository;
    }

    public List <CampoRConcil> findAll(){return campoRCRepository.findAllByOrderByNombreAsc();}
    public List<CampoRConcil> findAllActive() {
        return campoRCRepository.findByEstado(true);
    }

    public CampoRConcil findById(int id){
        return campoRCRepository.findAllById(id);
    }

    public CampoRConcil findByName(String nombre){
        return campoRCRepository.findAllByNombre(nombre);
    }

    public CampoRConcil modificar(CampoRConcil campo){
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


    public List<String> validatePrincipal2(String principal)
    {
        Query validate = entityManager.createNativeQuery("SELECT nv.nombre FROM preciso_rutas_contables as nv WHERE " +
                "nv.id_sf=1 and nv.activo=1");
        //validate.setParameter(1,principal);
        return validate.getResultList();
    }


}
