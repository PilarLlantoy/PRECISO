package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AccountConcil;
import com.inter.proyecto_intergrupo.model.parametric.CloseDateSourceSystem;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.SourceSystem;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountConcilRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CloseDateSourceSystemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class CloseDateSourceSystemService {

    @Autowired
    private final CloseDateSourceSystemRepository closeDateSourceSystemRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public CloseDateSourceSystemService(CloseDateSourceSystemRepository closeDateSourceSystemRepository) {
        this.closeDateSourceSystemRepository = closeDateSourceSystemRepository;
    }

    public List <CloseDateSourceSystem> findAll(){return closeDateSourceSystemRepository.findAll();}
    public List<CloseDateSourceSystem> findAllActive() {
        return closeDateSourceSystemRepository.findByEstado(true);
    }

    public CloseDateSourceSystem findById(int id){
        return closeDateSourceSystemRepository.findAllById(id);
    }

    public void deleteById(int id){
        closeDateSourceSystemRepository.deleteById(id);
    }

    public List <CloseDateSourceSystem> findByEstadoAndSistemaFuente(SourceSystem sistemaFuente){return closeDateSourceSystemRepository.findByEstadoAndSistemaFuente(true, sistemaFuente);}

    public CloseDateSourceSystem modificar(CloseDateSourceSystem fecha){
        closeDateSourceSystemRepository.save(fecha);
       return fecha;
    }


}
