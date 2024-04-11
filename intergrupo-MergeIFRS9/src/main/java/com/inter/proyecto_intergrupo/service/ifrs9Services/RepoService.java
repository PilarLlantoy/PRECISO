package com.inter.proyecto_intergrupo.service.ifrs9Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class RepoService {

    @Autowired
    MarketRiskService marketRiskService;

    @PersistenceContext
    EntityManager entityManager;

    public List<Object[]> findCtasAjuste(String periodo){
        Query queryGet = entityManager.createNativeQuery("select r.cnta_cntble_1_conciliacion,r.tipo_entidad,r.centro_contable_alt,r.moneda,r.numero_papeleta,r.numero_ident,r.causacion_hoy," +
                "m.valor_presente,r.cnta_cntble_1_conciliacion as cuenta_balance,(r.causacion_hoy + m.valor_presente)*-1 as ajuste,cnta_cntble_1_pyg,(r.causacion_hoy + m.valor_presente) as ajuste_pyg from nexco_rys_conciliacion r " +
                "join nexco_riesgo_mercado m on m.numero_papeleta = r.numero_papeleta where SUBSTRING(r.fecha,1,7) = ? and m.fecha = ?;");
        queryGet.setParameter(1,periodo);
        queryGet.setParameter(2,periodo);
        List<Object[]> result = queryGet.getResultList();
        return result;
    }

    public List<Object[]> cargaMasivaFormat(String periodo){
        Query queryGet = entityManager.createNativeQuery("select r.centro_contable_alt,  r.cnta_cntble_1_conciliacion,r.moneda, r.numero_papeleta, " +
                "(r.causacion_hoy + m.valor_presente)*-1 as ajuste, r.fecha , SUBSTRING(r.numero_ident,1,2) as tipo_documento, SUBSTRING(r.numero_ident,4,13) as num_documento from nexco_rys_conciliacion " +
                "r join nexco_riesgo_mercado m on m.numero_papeleta = r.numero_papeleta where SUBSTRING(r.fecha,1,7) = ? and m.fecha = ?");
        queryGet.setParameter(1,periodo);
        queryGet.setParameter(2,periodo);
        List<Object[]> result = queryGet.getResultList();
        return result;
    }
}
