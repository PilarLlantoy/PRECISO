package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.ConsolidationMethod;
import com.inter.proyecto_intergrupo.repository.parametric.ConsolidationMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ConsolidationMethodService {
    private final ConsolidationMethodRepository consolidationMethodRepository;

    @Autowired
    public ConsolidationMethodService(ConsolidationMethodRepository consolidationMethodRepository) {
        this.consolidationMethodRepository = consolidationMethodRepository;
    }

    public List <ConsolidationMethod> findAll(){return consolidationMethodRepository.findAll();}
    public ConsolidationMethod findConsolidationGroup(String method){
        return consolidationMethodRepository.findAllById(method);
    }
}
