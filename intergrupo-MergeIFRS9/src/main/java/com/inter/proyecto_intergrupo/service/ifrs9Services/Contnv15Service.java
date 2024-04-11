package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.Contnv15;
import com.inter.proyecto_intergrupo.model.ifrs9.Desconnv15;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class Contnv15Service {
    @PersistenceContext
    EntityManager entityManager;

    public boolean loadCont(String period) {
        try {

            boolean leido = false;

            Query deleteTable = entityManager.createNativeQuery("DROP TABLE IF EXISTS nexco_con_nv15_temp");
            deleteTable.executeUpdate();

            Query query = entityManager.createNativeQuery("CREATE TABLE nexco_con_nv15_temp( " +
                    "cuenta varchar(255), " +
                    "divisa varchar(255), " +
                    "sald varchar(255), " +
                    "salc varchar(255), " +
                    "sald_div varchar(255), " +
                    "salc_div varchar(255), " +
                    "observaciones varchar(255), " +
                    "tp varchar(255), " +
                    "tp1 varchar(255))");
            query.executeUpdate();

            Query query0 = entityManager.createNativeQuery("DELETE FROM nexco_con_nv15 WHERE periodo = ?");
            query0.setParameter(1,period);
            query0.executeUpdate();

            File f1 = new File("\\\\co.igrupobbva\\svrfilesystem\\TX\\Recepcion_host\\Financiera\\PLAN_00\\CONTAB_NIV15_PROV_PLAN00.TXT");
            File f2 = new File("\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\infocontable\\01-NEXCO\\CONTAB_NIV15_PROV_PLAN00.TXT");

            if(f1.exists() && !f1.isDirectory()) {
                Query query1 = entityManager.createNativeQuery("BULK INSERT nexco_con_nv15_temp FROM " +
                        "'\\\\co.igrupobbva\\svrfilesystem\\TX\\Recepcion_host\\Financiera\\PLAN_00\\CONTAB_NIV15_PROV_PLAN00.TXT' WITH (FIELDTERMINATOR= ';')");
                query1.executeUpdate();

                leido = true;
            }
            else{
                if(f2.exists() && !f2.isDirectory()) {
                    Query query1 = entityManager.createNativeQuery("BULK INSERT nexco_con_nv15_temp FROM " +
                            "'\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\infocontable\\01-NEXCO\\CONTAB_NIV15_PROV_PLAN00.TXT' WITH (FIELDTERMINATOR= ';')");
                    query1.executeUpdate();

                    leido = true;
                }
            }


            if(leido){
                Query query2 = entityManager.createNativeQuery("UPDATE nexco_con_nv15_temp\n" +
                        "SET cuenta = TRIM(cuenta),\n" +
                        "divisa = TRIM(divisa),\n" +
                        "sald = TRIM(REPLACE(REPLACE(sald,'+',''),',','')),\n" +
                        "salc = TRIM(REPLACE(REPLACE(salC,'+',''),',','')),\n" +
                        "sald_div = TRIM(REPLACE(REPLACE(sald_div,'+',''),',','')),\n" +
                        "salc_div = TRIM(REPLACE(REPLACE(salc_div,'+',''),',','')),\n" +
                        "observaciones = TRIM(observaciones),\n" +
                        "tp = TRIM(tp),\n" +
                        "tp1 = TRIM(tp1)");
                query2.executeUpdate();

                Query query3 = entityManager.createNativeQuery("INSERT INTO nexco_con_nv15 (cuenta,divisa,sald,salc,sald_div,salc_div,observaciones,tp,tp1,fuente_informacion,periodo)\n" +
                        "SELECT cuenta,divisa,sald,salc,sald_div,salc_div,observaciones,tp,tp1,'CONTAN15', ?\n" +
                        "FROM nexco_con_nv15_temp");
                query3.setParameter(1,period);
                query3.executeUpdate();

                Query query4 = entityManager.createNativeQuery("UPDATE nexco_con_nv15\n" +
                        "SET diferencia = sald-salc\n" +
                        "WHERE periodo = ?");
                query4.setParameter(1,period);
                query4.executeUpdate();

                return true;
            }
            return false;
        } catch (NoResultException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Contnv15> getcontnv15(String periodo){
        Query getQuery = entityManager.createNativeQuery("select * from nexco_con_nv15 WHERE periodo = ?", Contnv15.class);
        getQuery.setParameter(1,periodo);
        return getQuery.getResultList();
    }

    public List<Contnv15> findByFilter(String value, String filter, String periodo) {
        List<Contnv15> list=new ArrayList<>();
        switch (filter)
        {
            case "Cuenta":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_con_nv15 as em " +
                        "WHERE em.cuenta LIKE ? AND periodo = ?", Contnv15.class);
                query1.setParameter(1, value);
                query1.setParameter(2, periodo);
                list= query1.getResultList();

                break;
            case "Divisa":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_con_nv15 as em " +
                        "WHERE em.divisa LIKE ? AND periodo = ?", Contnv15.class);
                query2.setParameter(1, value);
                query2.setParameter(2, periodo);
                list= query2.getResultList();

                break;
            case "Observaciones":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_con_nv15 as em " +
                        "WHERE em.observaciones LIKE ? AND periodo = ?", Contnv15.class);
                query3.setParameter(1, value);
                query3.setParameter(2, periodo);
                list= query3.getResultList();

                break;
            case "Tp":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_con_nv15 as em " +
                        "WHERE em.tp LIKE ? AND periodo = ?", Contnv15.class);
                query4.setParameter(1, value);
                query4.setParameter(2, periodo);
                list= query4.getResultList();

                break;
            case "Tp1":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_con_nv15 as em " +
                        "WHERE em.tp1 LIKE ? AND periodo = ?", Contnv15.class);
                query5.setParameter(1, value);
                query5.setParameter(2, periodo);
                list= query5.getResultList();

                break;
            default:
                break;
        }
        return list;
    }
}
