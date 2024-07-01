package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.TipoDocumento;
import com.inter.proyecto_intergrupo.model.admin.User;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

}
