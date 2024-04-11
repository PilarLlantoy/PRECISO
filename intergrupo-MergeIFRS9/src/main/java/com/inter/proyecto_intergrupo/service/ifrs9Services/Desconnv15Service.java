package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.Desconnv15;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class Desconnv15Service {
    @PersistenceContext
    EntityManager entityManager;

    public boolean loadDescon(String period) {
        try {

            boolean leido = false;

            Query deleteTable = entityManager.createNativeQuery("DROP TABLE IF EXISTS nexco_descon_nv15_v1");
            deleteTable.executeUpdate();

            Query query = entityManager.createNativeQuery("CREATE TABLE nexco_descon_nv15_v1( " +
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

            Query query0 = entityManager.createNativeQuery("DELETE FROM nexco_descon_nv15 WHERE periodo = ?");
            query0.setParameter(1, period);
            query0.executeUpdate();

            File f1 = new File("\\\\co.igrupobbva\\svrfilesystem\\TX\\Recepcion_host\\Financiera\\PLAN_00\\DESCON_NIV15_PROV_PLAN00.TXT");
            //File f2 = new File("\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\infocontable\\01-NEXCO\\DESCON_NIV15_PROV_PLAN00.TXT");
            File f2 = new File("C:\\Users\\CE66916\\Documents\\Mario\\DesconNivel15\\DESCON_NIV15_PROV_PLAN00.TXT");

            if (f1.exists() && !f1.isDirectory()) {
                Query query1 = entityManager.createNativeQuery("BULK INSERT nexco_descon_nv15_v1 FROM " +
                        "'\\\\co.igrupobbva\\svrfilesystem\\TX\\Recepcion_host\\Financiera\\PLAN_00\\DESCON_NIV15_PROV_PLAN00.TXT' WITH (FIELDTERMINATOR= ';')");
                query1.executeUpdate();

                leido = true;
            } else {
                if (f2.exists() && !f2.isDirectory()) {
                    Query query1 = entityManager.createNativeQuery("BULK INSERT nexco_descon_nv15_v1 FROM " +
                            "'\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\infocontable\\01-NEXCO\\DESCON_NIV15_PROV_PLAN00.TXT' WITH (FIELDTERMINATOR= ';')");
                    query1.executeUpdate();

                    leido = true;
                }
            }


            if (leido) {
                Query query2 = entityManager.createNativeQuery("UPDATE nexco_descon_nv15_v1\n" +
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

                Query query3 = entityManager.createNativeQuery("INSERT INTO nexco_descon_nv15 (cuenta,divisa,sald,salc,sald_div,salc_div,observaciones,tp,tp1,fuente_informacion,periodo)\n" +
                        "SELECT cuenta,divisa,sald,salc,sald_div,salc_div,observaciones,tp,tp1,'DESCONN15', ?\n" +
                        "FROM nexco_descon_nv15_v1");
                query3.setParameter(1, period);
                query3.executeUpdate();

                Query query4 = entityManager.createNativeQuery("UPDATE nexco_descon_nv15\n" +
                        "SET diferencia = sald-salc\n" +
                        "WHERE periodo = ?");
                query4.setParameter(1, period);
                query4.executeUpdate();

                Query query5 = entityManager.createNativeQuery("UPDATE nexco_descon_nv15\n" +
                        "SET cod_neocon = puc.CODICONS46\n" +
                        "FROM\n" +
                        "nexco_descon_nv15\n" +
                        "INNER JOIN (SELECT NUCTA, CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') as puc ON puc.NUCTA = cuenta\n" +
                        "WHERE periodo = :period    \n" +
                        "\n" +
                        "UPDATE nexco_descon_nv15\n" +
                        "SET val_neocon = CASE WHEN perimetro.CODICONS46 IS NULL THEN 'Cod Neocon no existe en perímetro' ELSE 'Ok' END\n" +
                        "FROM\n" +
                        "nexco_descon_nv15\n" +
                        "LEFT JOIN \n" +
                        "(SELECT CODICONS46 FROM dbo.nexco_provisiones as prov \n" +
                        "INNER JOIN (SELECT trim(nucta) as NUCTA, decta, codicons46, EMPRESA FROM cuentas_puc WHERE empresa = '0013') \n" +
                        "as puc ON puc.CODICONS46 = CONVERT(varchar,prov.cuenta_neocon) \n" +
                        "WHERE ifrs9 = 'CV') as perimetro on perimetro.CODICONS46 = cod_neocon\n" +
                        "WHERE periodo = :period ");
                query5.setParameter("period", period);
                query5.executeUpdate();

                return true;
            }
            return false;
        } catch (NoResultException e) {

            return false;
        }
    }

    public List<Desconnv15> getdesconnv15(String periodo){
        Query getQuery = entityManager.createNativeQuery("select * from nexco_descon_nv15 WHERE periodo = ?", Desconnv15.class);
        getQuery.setParameter(1,periodo);
        return getQuery.getResultList();
    }

    public List<Desconnv15> findByFilter(String value, String filter, String periodo) {
        List<Desconnv15> list=new ArrayList<>();
        switch (filter)
        {
            case "Cuenta":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_descon_nv15 as em " +
                        "WHERE em.cuenta LIKE ? AND periodo = ?", Desconnv15.class);
                query1.setParameter(1, value);
                query1.setParameter(2, periodo);
                list= query1.getResultList();

                break;
            case "Divisa":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_descon_nv15 as em " +
                        "WHERE em.divisa LIKE ? AND periodo = ?", Desconnv15.class);
                query2.setParameter(1, value);
                query2.setParameter(2, periodo);
                list= query2.getResultList();

                break;
            case "Observaciones":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_descon_nv15 as em " +
                        "WHERE em.observaciones LIKE ? AND periodo = ?", Desconnv15.class);
                query3.setParameter(1, value);
                query3.setParameter(2, periodo);
                list= query3.getResultList();

                break;
            case "Tp":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_descon_nv15 as em " +
                        "WHERE em.tp LIKE ? AND periodo = ?", Desconnv15.class);
                query4.setParameter(1, value);
                query4.setParameter(2, periodo);
                list= query4.getResultList();

                break;
            case "Tp1":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_descon_nv15 as em " +
                        "WHERE em.tp1 LIKE ? AND periodo = ?", Desconnv15.class);
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
