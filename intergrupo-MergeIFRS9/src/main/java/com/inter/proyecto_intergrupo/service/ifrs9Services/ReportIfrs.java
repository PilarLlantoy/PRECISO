package com.inter.proyecto_intergrupo.service.ifrs9Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class ReportIfrs {

    @PersistenceContext
    EntityManager entityManager;

    public void QueryAndPerimeter(String periodo){
        Query getQuery = entityManager.createNativeQuery("  select q.empresa,q.codicons,q.nucta,q.coddiv,q.salmes from nexco_query q join nexco_perimeter n on q.nucta = n.cuenta;");
        List<Object[]> result =getQuery.getResultList();
    }

}
