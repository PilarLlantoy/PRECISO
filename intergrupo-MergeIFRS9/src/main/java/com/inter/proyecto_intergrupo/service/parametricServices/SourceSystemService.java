package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.SourceSystem;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SourceSystemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SourceSystemService {

    @Autowired
    private final SourceSystemRepository sourceSystemRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public SourceSystemService(SourceSystemRepository sourceSystemRepository) {
        this.sourceSystemRepository = sourceSystemRepository;
    }

    public SourceSystem findById(int id){
        return sourceSystemRepository.findAllById(id);
    }

    public List <SourceSystem> findAll(){return sourceSystemRepository.findAllByOrderByNombreAsc();}
    public List<SourceSystem> findAllActive() {
        return sourceSystemRepository.findByEstado(true);
    }

    public SourceSystem findSourceSystemById(int id){
        return sourceSystemRepository.findAllById(id);
    }

    public SourceSystem modificarSourceSystem(SourceSystem sf){
       sourceSystemRepository.save(sf);
       return sf;
    }
    public SourceSystem findByNombre(String name){return sourceSystemRepository.findAllByNombre(name);}


    public Page<SourceSystem> getAll(Pageable pageable){

        return sourceSystemRepository.findAll(pageable);
    }

    public List<SourceSystem> findByFilter(String value, String filter) {
        List<SourceSystem> list=new ArrayList<SourceSystem>();
        switch (filter)
        {
            case "Código":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_sistema_fuente as em " +
                        "WHERE em.id_sf LIKE ?", SourceSystem.class);
                query.setParameter(1, value );
                list= query.getResultList();
                break;
            case "Nombre":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM preciso_sistema_fuente as em " +
                        "WHERE em.nombre_sf LIKE ? ", SourceSystem.class);
                query0.setParameter(1, value);
                list = query0.getResultList();
                break;
            case "Sigla":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM preciso_sistema_fuente as em " +
                        "WHERE em.sigla_sf LIKE ? ", SourceSystem.class);
                query1.setParameter(1, value);
                list = query1.getResultList();
                break;
            case "Aplica Festivo":
                boolean valor = true;
                if("no aplica".equalsIgnoreCase(value))
                    valor = false;
                Query query2 = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_sistema_fuente as em WHERE em.festivo = ?", SourceSystem.class);
                query2.setParameter(1, valor);
                list = query2.getResultList();
                break;
            case "Código País":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM preciso_sistema_fuente as em " +
                        "WHERE em.id_pais LIKE ? ", SourceSystem.class);
                query3.setParameter(1, value);
                list = query3.getResultList();
                break;
            case "País":
                Query query4 = entityManager.createNativeQuery("SELECT pa.* FROM preciso_paises as em \n" +
                        "inner join preciso_sistema_fuente as pa on pa.id_pais = em.id_pais\n" +
                        "WHERE em.nombre_pais LIKE ? ", SourceSystem.class);
                query4.setParameter(1, value);
                list = query4.getResultList();
                break;
            case "Estado":
                boolean valor1 = true;
                if("inactivo".equalsIgnoreCase(value))
                    valor1 = false;
                Query quer = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_sistema_fuente as em WHERE em.activo = ?", SourceSystem.class);
                quer.setParameter(1, valor1);
                list = quer.getResultList();
                break;
            default:
                break;
        }
        return list;
    }
}
