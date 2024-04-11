package com.inter.proyecto_intergrupo.service.bankServices;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.IntergrupoSubsidiariesTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class IntergrupoSubsidiariesTemplateService {

    @Autowired
    EntityManager entityManager;

    public List<IntergrupoSubsidiariesTemplate> getIntergrupoSubsidiaries(String fecont, User user){
        List<IntergrupoSubsidiariesTemplate> subsidiaries = new ArrayList<>();

        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_filiales_intergrupo WHERE periodo = ?", IntergrupoSubsidiariesTemplate.class);
        result.setParameter(1,fecont);

        if(!result.getResultList().isEmpty()){
            subsidiaries = result.getResultList();
        }

        return subsidiaries;
    }

}
