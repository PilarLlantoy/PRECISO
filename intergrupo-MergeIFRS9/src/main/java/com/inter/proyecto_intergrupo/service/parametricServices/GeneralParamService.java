package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.model.parametric.GeneralParam;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.EventTypeRepository;
import com.inter.proyecto_intergrupo.repository.parametric.GeneralParamRepository;
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
public class GeneralParamService {

    @Autowired
    private final GeneralParamRepository generalParamRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public GeneralParamService(GeneralParamRepository generalParamRepository) {
        this.generalParamRepository = generalParamRepository;
    }

    public List<GeneralParam> findAll(){return generalParamRepository.findAllByOrderByUnidadPrincipalAsc();}

    public List<GeneralParam> findByUnidadPrincipal(String unidad){
        return generalParamRepository.findByUnidadPrincipal(unidad);
    }

    public List<GeneralParam> findByUnidadSecundaria(String unidad){
        return generalParamRepository.findByUnidadSecundaria(unidad);
    }

    public GeneralParam findAllById(Long id){
        return generalParamRepository.findAllById(id);
    }

    public GeneralParam modificar(GeneralParam parametro){
        generalParamRepository.save(parametro);
       return parametro;
    }
}
