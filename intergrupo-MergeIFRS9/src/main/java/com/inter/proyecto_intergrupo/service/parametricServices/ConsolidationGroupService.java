package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.ConsolidationGroup;
import com.inter.proyecto_intergrupo.repository.parametric.ConsolidationGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ConsolidationGroupService {
    private final ConsolidationGroupRepository consolidationGroupRepository;

    @Autowired
    public ConsolidationGroupService(ConsolidationGroupRepository consolidationGroupRepository) {
        this.consolidationGroupRepository = consolidationGroupRepository;
    }

    public List <ConsolidationGroup> findAll(){return consolidationGroupRepository.findAll();}
    public ConsolidationGroup findConsolidationGroup(String group){
        return consolidationGroupRepository.findAllById(group);
    }
}
