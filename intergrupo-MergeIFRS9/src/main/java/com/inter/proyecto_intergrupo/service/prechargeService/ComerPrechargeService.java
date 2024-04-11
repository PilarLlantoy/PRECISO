package com.inter.proyecto_intergrupo.service.prechargeService;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.precharges.ComerPrecharge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;

@Service
@Transactional
public class ComerPrechargeService {

    @Autowired
    EntityManager entityManager;

    public ArrayList<ComerPrecharge> getInformation(String periodo){
        ArrayList<ComerPrecharge> finalData;

        ArrayList<String> months = new ArrayList<>();

        int month = Integer.parseInt(periodo.substring(5,7)) < 10 ? Integer.parseInt(periodo.substring(5,7).replace("0","")) :  Integer.parseInt(periodo.substring(5,7));
        String year = periodo.substring(0,4);
        for(int i = month; i>0 ; i--){
            String m;
            if(i<10){
                m = year + "-0"+ i;
            } else {
                m = year +"-"+ i;
            }
            months.add(m);
        }

        Query getData = entityManager.createNativeQuery("SELECT * FROM nexco_precarga_comer WHERE periodo IN (:months) " +
                "ORDER BY periodo desc", ComerPrecharge.class);
        getData.setParameter("months",months);

        if(!getData.getResultList().isEmpty()){
            finalData = (ArrayList<ComerPrecharge>) getData.getResultList();
        } else {
            finalData = new ArrayList<>();
        }

        return finalData;
    }

}
