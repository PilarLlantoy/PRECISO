package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Perimeter;
import com.inter.proyecto_intergrupo.model.ifrs9.ValQueryEEFF;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
@Service
@Transactional
public class PerimeterService {@PersistenceContext
EntityManager entityManager;
    
    @Autowired
    private AuditRepository auditRepository;

    public List<Perimeter> calcularPerimetro(String periodo, String opcionQ, String opcionE, String empresa) {
        List<Perimeter> list = new ArrayList<Perimeter>();
        String[] arrayPeriodo = periodo.split("-");
        String anio = arrayPeriodo[0];
        String mes = arrayPeriodo[1];
        try {

            Query query0 = entityManager.createNativeQuery("DELETE FROM nexco_perimetereeff WHERE periodo = ?");
            query0.setParameter(1,periodo);
            query0.executeUpdate();

            Query query1 = entityManager.createNativeQuery("SELECT p.cuenta_neocon as cuenta, p.sector, p.producto_espana as producto " +
                    "INTO perimetro_ifrs9_parcial " +
                    "FROM nexco_provisiones p " +
                    "WHERE p.ifrs9 = 'CV'");
            query1.executeUpdate();

            Query query2 = entityManager.createNativeQuery("SELECT cuenta, divisa, SUM(saldo) AS saldo \n" +
                    "INTO eeff_aux \n" +
                    "FROM (SELECT C.cuenta, C.Divisa_espana as divisa, \n" +
                    "CASE WHEN LTRIM(RTRIM(C.saldo)) = '' THEN CAST(0.00 AS DECIMAL(18,2)) \n" +
                    "ELSE CAST(REPLACE(TRIM(C.saldo),',','.') AS DECIMAL(18,2)) END AS saldo \n" +
                    "FROM nexco_eeff AS C \n" +
                    "LEFT JOIN nexco_cuentas_neocon D \n" +
                    "on c.cuenta = d.cuenta \n" +
                    "WHERE saldo <> '' and descripcion_ic = '' and d.entrada = 'S' and upper(tipo) = ? and period = ?) AS A \n" +
                    "GROUP BY cuenta, divisa");
            query2.setParameter(1, opcionE);
            query2.setParameter(2, periodo);
            query2.executeUpdate();

            Query query3 = entityManager.createNativeQuery("SELECT q.codicons as cuenta, d.divisa_neocon as divisa, ROUND(SUM(q.saldo)/1000, 0) AS saldo, q.fecha_proceso " +
                    "INTO query_aux " +
                    "FROM " +
                    "(SELECT codicons, divisa, " +
                    "CAST(case when substring(nucta, 1,1) in ('2', '3', '4') or substring(nucta, 1, 2) in ('62', '63', '82', '83') then saldoquery*(-1) else saldoquery end AS DECIMAL(18,2)) AS saldo, convert(varchar, fechproce, 23) fecha_proceso " +
                    "FROM nexco_query_marcados " +
                    "WHERE origen = ? AND YEAR(fecont) = ? AND MONTH(fecont) = ? AND empresa = ?) " +
                    "AS q INNER JOIN " +
                    "nexco_divisas as d ON " +
                    "q.divisa = d.divisa_neocon " +
                    "GROUP BY q.codicons, d.divisa_neocon, q.fecha_proceso");
            query3.setParameter(1,opcionQ);
            query3.setParameter(2,anio);
            query3.setParameter(3,mes);
            query3.setParameter(4,empresa);
            query3.executeUpdate();

            Query query4 = entityManager.createNativeQuery("INSERT INTO nexco_perimetereeff (segmento, subproducto, codigo_consolidacion, divisa, saldo_query, saldo_eeff, diferencia_saldos, fecha_proceso, periodo) \n" +
                    "SELECT per.sector as sector, \n" +
                    "per.producto as producto, \n" +
                    "per.cuenta as codigo_consolidacion, \n" +
                    "ISNULL(e.divisa, q.divisa) as divisa, \n" +
                    "ISNULL(CONVERT(DECIMAL(18,2), q.saldo),0) AS saldo_query, \n" +
                    "ISNULL(e.saldo,0) AS saldo_eeff, \n" +
                    "abs(ISNULL(CONVERT(DECIMAL(18,2), q.saldo),0)) - abs(ISNULL(e.saldo,0)) AS saldo_dif, \n" +
                    "q.fecha_proceso as fecha_proceso, ? periodo \n" +
                    "FROM perimetro_ifrs9_parcial per LEFT JOIN \n" +
                    "query_aux AS q \n" +
                    "ON per.cuenta = q.cuenta LEFT JOIN \n" +
                    "eeff_aux AS e \n" +
                    "ON per.cuenta = e.cuenta \n" +
                    ";");
            query4.setParameter(1,periodo);
            query4.executeUpdate();

            Query query5 = entityManager.createNativeQuery(
                    "SELECT * " +
                    "FROM nexco_perimetereeff " +
                    "WHERE convert(decimal(18,2), diferencia_saldos) <> 0  and periodo = ?",Perimeter.class);
            query5.setParameter(1,periodo);
            list= query5.getResultList();

            Query query7 = entityManager.createNativeQuery("DROP TABLE perimetro_ifrs9_parcial");
            query7.executeUpdate();

            Query query8 = entityManager.createNativeQuery("DROP TABLE eeff_aux");
            query8.executeUpdate();

            Query query9 = entityManager.createNativeQuery("DROP TABLE query_aux");
            query9.executeUpdate();

            return list;

        } catch (NoResultException e) {
            return list;
        }
    }

    public List<Perimeter> getPerimetro(String periodo){

        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_perimetereeff WHERE periodo = ?",Perimeter.class);
        query.setParameter(1,periodo);
        return query.getResultList();
    }

    public List<Object[]> getCompany(String periodo){

        Query companies = entityManager.createNativeQuery(
                "select distinct empresa from nexco_query_marcados where substring(fecont, 1, 7) = ? \n");
        companies.setParameter(1,periodo);
        List<Object[]> companiesList = companies.getResultList();

        if(companiesList.isEmpty()){

            Query companies2 = entityManager.createNativeQuery(
                    "select distinct empresa from nexco_query where substring(fecont, 1, 7) = ? \n");
            companies.setParameter(1,periodo);
            List<Object[]> companiesList2 = companies.getResultList();

            return companiesList2;

        } else {

            return companiesList;

        }
    }


    public void clearPerimetro(User user, String periodo){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_perimetereeff WHERE periodo = ?", Perimeter.class);
        query.setParameter(1,periodo);
        query.executeUpdate();
    }

    public List<Perimeter> findByFilter(String value, String filter, String periodo) {

        List<Perimeter> list=new ArrayList<Perimeter>();
        switch (filter)
        {
            case "Segmento":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_perimetereeff as em " +
                        "WHERE em.segmento LIKE ? AND periodo = ? ", Perimeter.class);
                query.setParameter(1, value);
                query.setParameter(2, periodo);

                list= query.getResultList();

                break;
            case "Subproducto":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_perimetereeff as em " +
                        "WHERE em.subproducto LIKE ? AND periodo = ?", Perimeter.class);
                query0.setParameter(1, value);
                query0.setParameter(2, periodo);

                list= query0.getResultList();
                break;
            case "Codigo Consolidacion":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_perimetereeff as em " +
                        "WHERE em.codigo_consolidacion LIKE ? AND periodo = ?", Perimeter.class);
                query1.setParameter(1, value);
                query1.setParameter(2, periodo);

                list= query1.getResultList();
                break;

            case "Divisa":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_perimetereeff as em " +
                        "WHERE em.divisa LIKE ? AND periodo = ?", Perimeter.class);
                query2.setParameter(1, value);
                query2.setParameter(2,periodo);

                list= query2.getResultList();
                break;
            default:
                break;
        }
        return list;
    }
    
}
