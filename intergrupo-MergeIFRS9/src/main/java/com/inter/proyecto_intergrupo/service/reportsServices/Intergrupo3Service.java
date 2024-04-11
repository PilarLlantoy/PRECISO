package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.PlainIFRS9Intergroup;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.bank.IntergrupoV1FinalRepository;
import com.inter.proyecto_intergrupo.repository.bank.IntergrupoV3FinalRepository;
import com.inter.proyecto_intergrupo.repository.bank.IntergrupoV3ValidaRepository;
import com.inter.proyecto_intergrupo.repository.parametric.*;
import com.inter.proyecto_intergrupo.repository.reports.ContingentTemplateRepository;
import com.inter.proyecto_intergrupo.repository.temporal.ReclassificationInterDepRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class Intergrupo3Service {

    @Autowired
    private ContingentTemplateRepository contingentTemplateRepository;

    @Autowired
    private ReclassificationInterDepRepository reclassificationInterDepRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private YntpSocietyRepository yntpSocietyRepository;

    @Autowired
    IntergrupoV3FinalRepository intergrupoV3FinalRepository;

    @Autowired
    IntergrupoV3ValidaRepository intergrupoV3ValidaRepository;

    @PersistenceContext
    EntityManager entityManager;

    public List<IntergrupoV3> getAllFromV3(String periodo) {
        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v3 WHERE periodo = ?", IntergrupoV3.class);
        result.setParameter(1, periodo);
        List<IntergrupoV3> data = result.getResultList();

        return data;
    }

    public List<IntergrupoV3Temp> getAllFromV3Cruce(String periodo) {
        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v3_temp WHERE periodo = ?", IntergrupoV3Temp.class);
        result.setParameter(1, periodo);
        List<IntergrupoV3Temp> data = result.getResultList();

        return data;
    }

    public List<PlainIFRS9Intergroup> getAllFromV3Planos(String periodo) {
        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_plano_ifrs9_intergrupo WHERE periodo = ?", PlainIFRS9Intergroup.class);
        result.setParameter(1, periodo);
        List<PlainIFRS9Intergroup> data = result.getResultList();

        return data;
    }

    public List<Object[]> getAllFromV3PlanosObject(String periodo) {
        Query result = entityManager.createNativeQuery("SELECT a.empresa,a.fecha_contable,a.cuenta,a.contrato,a.divisa,a.importe_c,a.importe_d,a.importe_cd,a.importe_dd,a.importe_cd_exp,a.importe_dd_exp,a.importe_total,a.observacion,a.origen,a.periodo,b.CODICONS46,ISNULL(c.yntp,' ') YNTP FROM nexco_plano_ifrs9_intergrupo_origin a\n" +
                "LEFT JOIN (SELECT NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE empresa= '0013') b ON a.cuenta = b.NUCTA\n" +
                "LEFT JOIN (select contrato, yntp from nexco_intergrupo_v2 where periodo = ? group by contrato, yntp) c ON a.contrato=c.contrato\n" +
                "WHERE a.periodo = ?");
        result.setParameter(1, periodo);
        result.setParameter(2, periodo);
        return result.getResultList();
    }

    public List<IntergrupoV3> findByFilter(String value, String filter, String period) {

        ArrayList<IntergrupoV3> toReturn;

        switch (filter) {

            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v3 WHERE cuenta_local LIKE ? and periodo LIKE ?", IntergrupoV3.class);
                query.setParameter(1, value);
                query.setParameter(2, period);
                if (query.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV3>) query.getResultList();
                }
                break;

            case "Contrato":
                Query query1 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v3 WHERE contrato LIKE ? and periodo LIKE ?", IntergrupoV3.class);
                query1.setParameter(1, value);
                query1.setParameter(2, period);
                if (query1.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV3>) query1.getResultList();
                }
                break;

            case "Nit":
                Query query2 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v3 WHERE nit LIKE ? and periodo LIKE ?", IntergrupoV3.class);
                query2.setParameter(1, value);
                query2.setParameter(2, period);
                if (query2.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV3>) query2.getResultList();
                }
                break;

            case "Cod Neocon":
                Query query3 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v3 WHERE cod_neocon LIKE ? and periodo LIKE ?", IntergrupoV3.class);
                query3.setParameter(1, value);
                query3.setParameter(2, period);
                if (query3.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV3>) query3.getResultList();
                }
                break;

            case "Divisa":
                Query query4 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v3 WHERE divisa LIKE ? and periodo LIKE ?", IntergrupoV3.class);
                query4.setParameter(1, value);
                query4.setParameter(2, period);
                if (query4.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV3>) query4.getResultList();
                }
                break;

            default:
                toReturn = new ArrayList<>();
        }


        return toReturn;
    }

    public void insertV3(String period, User user){

        Query deleteInfo = entityManager.createNativeQuery("truncate table nexco_intergrupo_v3_temp;");
        deleteInfo.executeUpdate();

        Query deleteInfo1 = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v3 where periodo = ?");
        deleteInfo1.setParameter(1,period);
        deleteInfo1.executeUpdate();


        //Exclusión regla de efectivo por parametrica de nexco_provisiones
        Query insert = entityManager.createNativeQuery("insert into nexco_intergrupo_v3_temp (cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input,\n" +
                "nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante, cuenta_plano, valor_rec, valor_prov,perimetro, intergrupo, elimina)\n" +
                "select y.cod_neocon, y.cod_pais, y.componente, y.contrato, y.cuenta_local, y.divisa, y.fuente, y.input,\n" +
                "y.nit, y.pais, y.periodo, y.sociedad_yntp, y.valor, y.yntp, y.yntp_empresa_reportante, ISNULL(z.cuenta, x.cuenta) cuenta_plano, z.importe valor_recla, \n" +
                "x.importe valor_prov, y.perimetro, case when t.yntp is not null then 'S' else 'N' end intergrupo,\n" +
                "case when (y.perimetro in ('CV', 'PR') and y.instrumento != 'EFECTIVO' and t.yntp is not null and z.importe is null and x.importe is null) or (substring(ISNULL(z.cuenta, x.cuenta),1, 1) = '1' and z.importe_d = 0) then case when Y.componente = 'DIVIDENDOS' then 'N' else 'S' end else 'N' end elimina\n" +
                "from (select a.*, b.ifrs9 perimetro, b.instrumento from /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def a\n" +
                "left join nexco_provisiones b\n" +
                "on a.cod_neocon = b.cuenta_neocon\n" +
                "where periodo = ?) y\n" +
                "left join (select contrato, cuenta, divisa, sum(case when substring(cuenta,1, 1) = '1' then importe_d else importe_c end) importe,sum(importe_d) importe_d, sum(importe_c) importe_c\n" +
                "from nexco_plano_ifrs9_intergrupo a\n" +
                "where LEN(cuenta) > 12 and origen = 'INTERGRUPO_RECLAS.TXT' and periodo = ?\n" +
                "group by contrato, cuenta, divisa) z\n" +
                "on y.contrato = z.contrato and SUBSTRING(y.cuenta_local, 1, 2) = SUBSTRING(z.cuenta, 1, 2) and y.perimetro = 'PR'\n" +
                "left join (select contrato, cuenta, divisa, sum(importe_total) importe\n" +
                "from nexco_plano_ifrs9_intergrupo a\n" +
                "where LEN(cuenta) > 12 and importe_c <> 0 and origen = 'INTERGRUPO_PROVIS.TXT' and periodo = ?\n" +
                "group by contrato, cuenta, divisa) x\n" +
                "on y.contrato = x.contrato and SUBSTRING(y.cuenta_local, 1, 2) = SUBSTRING(x.cuenta, 1, 2) and y.perimetro = 'CV'\n" +
                "left join (select distinct yntp from nexco_terceros) t\n" +
                "on y.yntp = t.yntp\n" +
                ";");

        insert.setParameter(1,period);
        insert.setParameter(2,period);
        insert.setParameter(3,period);
        insert.executeUpdate();

        Query insert2 = entityManager.createNativeQuery("insert into nexco_intergrupo_v3 (cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input,\n" +
                "nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante)\n" +
                "select cod_neocon, cod_pais, componente, contrato, CASE WHEN cuenta_plano is null THEN cuenta_local ELSE cuenta_plano END Cuenta_Final,\n" +
                "divisa, fuente, input,\n" +
                "nit, pais, periodo, sociedad_yntp, CASE WHEN valor_prov is not null AND valor_prov != 0 THEN valor_prov WHEN valor_rec is not null AND valor_rec != 0 THEN valor_rec ELSE valor END Valor_final,\n" +
                "yntp, yntp_empresa_reportante from nexco_intergrupo_v3_temp\n" +
                "where elimina = 'N'\n" +
                "GROUP BY cod_neocon, cod_pais, componente, contrato, CASE WHEN cuenta_plano is null THEN cuenta_local ELSE cuenta_plano END,\n" +
                "cuenta_local, divisa, fuente, input,\n" +
                "nit, pais, periodo, sociedad_yntp, CASE WHEN valor_prov is not null AND valor_prov != 0 THEN valor_prov WHEN valor_rec is not null AND valor_rec != 0 THEN valor_rec ELSE valor END, \n" +
                "yntp, yntp_empresa_reportante" +
                ";");

        insert2.executeUpdate();

        Query insert3 = entityManager.createNativeQuery("INSERT INTO nexco_intergrupo_v3 (cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input,nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante)\n" +
                "SELECT y.CODICONS46, x.cod_pais,'INTERGRUPO - IFRS9' COMP,z.contrato,z.cuenta,z.divisa,'INTERGRUPO - IFRS9' FUE,'INTERGRUPO - IFRS9' INP,x.nit,x.pais,? PER,x.sociedad_yntp,z.importe_total,x.yntp,x.yntp_empresa_reportante\n" +
                "FROM (select a.* from nexco_plano_ifrs9_intergrupo a where a.periodo = ? and SUBSTRING(a.cuenta,1,2) = '28') z\n" +
                "LEFT JOIN (SELECT NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA='0013')y ON z.cuenta = y.NUCTA\n" +
                "LEFT JOIN (SELECT contrato,nit,yntp,sociedad_yntp,cod_pais,pais,yntp_empresa_reportante FROM /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def WHERE periodo = ? GROUP BY contrato,nit,yntp,sociedad_yntp,cod_pais,pais,yntp_empresa_reportante) x ON z.contrato = x.contrato");
        insert3.setParameter(1,period);
        insert3.setParameter(2,period);
        insert3.setParameter(3,period);
        insert3.executeUpdate();

        Query update1 = entityManager.createNativeQuery("update a\n" +
                "set a.cod_neocon = b.CODICONS46\n" +
                "from nexco_intergrupo_v3 a\n" +
                "inner join (select nucta, CODICONS46 from CUENTAS_PUC where EMPRESA = '0013') b\n" +
                "on a.cuenta_local = b.nucta\n" +
                "where a.periodo = ?");
        update1.setParameter(1,period);
        update1.executeUpdate();

        saveLog(user,"Generación Intergrupo V3");

        Query search = entityManager.createNativeQuery("\n" +
                "SELECT H.cod_neocon,H.yntp,H.valor,H.contrato,U.cuenta FROM \n" +
                "(SELECT distinct A.* FROM (SELECT cod_neocon,yntp,valor,periodo,contrato\n" +
                "FROM nexco_intergrupo_v3 WHERE periodo=? and contrato != ''\n" +
                "GROUP BY cod_neocon,yntp,valor,periodo,contrato HAVING count(*)>1)A\n" +
                "LEFT JOIN (SELECT * FROM nexco_intergrupo_v3\n" +
                "WHERE periodo = ?) B ON A.cod_neocon = B.cod_neocon and A.contrato = B.contrato and A.yntp = B.yntp)H\n" +
                "INNER JOIN (select Z.contrato,Y.CODICONS46,Z.cuenta from nexco_plano_ifrs9_intergrupo Z\n" +
                "LEFT JOIN (select NUCTA, CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') Y ON Y.NUCTA = Z.cuenta\n" +
                "where periodo=? and importe_d = 0 GROUP BY Z.contrato,Y.CODICONS46, Z.cuenta) U ON H.contrato = U.contrato and H.cod_neocon = U.CODICONS46\n");
        search.setParameter(1,period);
        search.setParameter(2,period);
        search.setParameter(3,period);
        List<Object[]> listTemporal = search.getResultList();
        for (Object[] part: listTemporal) {
            Query delete1 = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v3\n" +
                    "WHERE periodo = ? AND cod_neocon = ? AND contrato = ? AND yntp = ? AND valor = ? AND cuenta_local = ? ");
            delete1.setParameter(1,period);
            delete1.setParameter(2,part[0].toString());
            delete1.setParameter(3,part[3].toString());
            delete1.setParameter(4,part[1].toString());
            delete1.setParameter(5,Double.parseDouble(part[2].toString()));
            delete1.setParameter(6,part[4].toString());
            delete1.executeUpdate();
        }


    }

    public void insertV3P1(String period, User user){

        Query deleteInfo = entityManager.createNativeQuery("truncate table nexco_intergrupo_v3_temp;");
        deleteInfo.executeUpdate();

        //Exclusión regla de efectivo por parametrica de nexco_provisiones
        Query insert = entityManager.createNativeQuery("insert into nexco_intergrupo_v3_temp (cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input,\n" +
                "nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante, cuenta_plano, valor_rec, valor_prov,perimetro, intergrupo, elimina)\n" +
                "select y.cod_neocon, y.cod_pais, y.componente, y.contrato, y.cuenta_local, y.divisa, y.fuente, y.input,\n" +
                "y.nit, y.pais, y.periodo, y.sociedad_yntp, y.valor, y.yntp, y.yntp_empresa_reportante, ISNULL(z.cuenta, x.cuenta) cuenta_plano, z.importe valor_recla, \n" +
                "x.importe valor_prov, y.perimetro, case when t.yntp is not null then 'S' else 'N' end intergrupo,\n" +
                "case when (y.perimetro in ('CV', 'PR') and y.instrumento != 'EFECTIVO' and t.yntp is not null and z.importe is null and x.importe is null) or (substring(ISNULL(z.cuenta, x.cuenta),1, 1) = '1' and z.importe_d = 0) then case when Y.componente = 'DIVIDENDOS' then 'N' else 'S' end else 'N' end elimina\n" +
                "from (select a.*, b.ifrs9 perimetro, b.instrumento from /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def a\n" +
                "left join nexco_provisiones b\n" +
                "on a.cod_neocon = b.cuenta_neocon\n" +
                "where periodo = ?) y\n" +
                "left join (select contrato, cuenta, divisa, sum(case when substring(cuenta,1, 1) = '1' then importe_d else importe_c end) importe,sum(importe_d) importe_d, sum(importe_c) importe_c\n" +
                "from nexco_plano_ifrs9_intergrupo a\n" +
                "where LEN(cuenta) > 12 and origen = 'INTERGRUPO_RECLAS.TXT' and periodo = ?\n" +
                "group by contrato, cuenta, divisa) z\n" +
                "on y.contrato = z.contrato and SUBSTRING(y.cuenta_local, 1, 2) = SUBSTRING(z.cuenta, 1, 2) and y.perimetro = 'PR'\n" +
                "left join (select contrato, cuenta, divisa, sum(importe_total) importe\n" +
                "from nexco_plano_ifrs9_intergrupo a\n" +
                "where LEN(cuenta) > 12 and importe_c <> 0 and origen = 'INTERGRUPO_PROVIS.TXT' and periodo = ?\n" +
                "group by contrato, cuenta, divisa) x\n" +
                "on y.contrato = x.contrato and SUBSTRING(y.cuenta_local, 1, 2) = SUBSTRING(x.cuenta, 1, 2) and y.perimetro = 'CV'\n" +
                "left join (select distinct yntp from nexco_terceros) t\n" +
                "on y.yntp = t.yntp\n" +
                ";");

        insert.setParameter(1,period);
        insert.setParameter(2,period);
        insert.setParameter(3,period);
        insert.executeUpdate();

        saveLog(user,"Generación Intergrupo Validación V3");

    }

    public void insertV3P2(String period, User user){

        Query deleteInfo1 = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v3 where periodo = ?");
        deleteInfo1.setParameter(1,period);
        deleteInfo1.executeUpdate();

        Query insert2 = entityManager.createNativeQuery("insert into nexco_intergrupo_v3 (cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input,\n" +
                "nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante)\n" +
                "select cod_neocon, cod_pais, componente, contrato, CASE WHEN (cuenta_plano is null or cuenta_plano = '') THEN cuenta_local ELSE cuenta_plano END Cuenta_Final,\n" +
                "divisa, fuente, input,\n" +
                "nit, pais, periodo, sociedad_yntp, CASE WHEN valor_prov is not null AND valor_prov != 0 THEN valor_prov WHEN valor_rec is not null AND valor_rec != 0 THEN valor_rec ELSE valor END Valor_final,\n" +
                "yntp, yntp_empresa_reportante from nexco_intergrupo_v3_valida\n" +
                "where elimina = 'N'\n" +
                "GROUP BY cod_neocon, cod_pais, componente, contrato, CASE WHEN (cuenta_plano is null or cuenta_plano = '') THEN cuenta_local ELSE cuenta_plano END,\n" +
                "cuenta_local, divisa, fuente, input,\n" +
                "nit, pais, periodo, sociedad_yntp, CASE WHEN valor_prov is not null AND valor_prov != 0 THEN valor_prov WHEN valor_rec is not null AND valor_rec != 0 THEN valor_rec ELSE valor END, \n" +
                "yntp, yntp_empresa_reportante" +
                ";");

        insert2.executeUpdate();

        Query insert3 = entityManager.createNativeQuery("INSERT INTO nexco_intergrupo_v3 (cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input,nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante)\n" +
                "SELECT y.CODICONS46, x.cod_pais,'INTERGRUPO - IFRS9' COMP,z.contrato,z.cuenta,z.divisa,'INTERGRUPO - IFRS9' FUE,'INTERGRUPO - IFRS9' INP,x.nit,x.pais,? PER,x.sociedad_yntp,z.importe_total,x.yntp,x.yntp_empresa_reportante\n" +
                "FROM (select a.* from nexco_plano_ifrs9_intergrupo a where a.periodo = ? and SUBSTRING(a.cuenta,1,2) = '28') z\n" +
                "LEFT JOIN (SELECT NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA='0013')y ON z.cuenta = y.NUCTA\n" +
                "LEFT JOIN (SELECT contrato,nit,yntp,sociedad_yntp,cod_pais,pais,yntp_empresa_reportante FROM /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def WHERE periodo = ? GROUP BY contrato,nit,yntp,sociedad_yntp,cod_pais,pais,yntp_empresa_reportante) x ON z.contrato = x.contrato");
        insert3.setParameter(1,period);
        insert3.setParameter(2,period);
        insert3.setParameter(3,period);
        insert3.executeUpdate();

        Query update1 = entityManager.createNativeQuery("update a\n" +
                "set a.cod_neocon = b.CODICONS46\n" +
                "from nexco_intergrupo_v3 a\n" +
                "inner join (select nucta, CODICONS46 from CUENTAS_PUC where EMPRESA = '0013') b\n" +
                "on a.cuenta_local = b.nucta\n" +
                "where a.periodo = ?");
        update1.setParameter(1,period);
        update1.executeUpdate();

        saveLog(user,"Generación Intergrupo V3");

        Query search = entityManager.createNativeQuery("\n" +
                "SELECT H.cod_neocon,H.yntp,H.valor,H.contrato,U.cuenta FROM \n" +
                "(SELECT distinct A.* FROM (SELECT cod_neocon,yntp,valor,periodo,contrato\n" +
                "FROM nexco_intergrupo_v3 WHERE periodo=? and contrato != ''\n" +
                "GROUP BY cod_neocon,yntp,valor,periodo,contrato HAVING count(*)>1)A\n" +
                "LEFT JOIN (SELECT * FROM nexco_intergrupo_v3\n" +
                "WHERE periodo = ?) B ON A.cod_neocon = B.cod_neocon and A.contrato = B.contrato and A.yntp = B.yntp)H\n" +
                "INNER JOIN (select Z.contrato,Y.CODICONS46,Z.cuenta from nexco_plano_ifrs9_intergrupo Z\n" +
                "LEFT JOIN (select NUCTA, CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') Y ON Y.NUCTA = Z.cuenta\n" +
                "where periodo=? and importe_d = 0 GROUP BY Z.contrato,Y.CODICONS46, Z.cuenta) U ON H.contrato = U.contrato and H.cod_neocon = U.CODICONS46\n");
        search.setParameter(1,period);
        search.setParameter(2,period);
        search.setParameter(3,period);
        List<Object[]> listTemporal = search.getResultList();
        for (Object[] part: listTemporal) {
            Query delete1 = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v3\n" +
                    "WHERE periodo = ? AND cod_neocon = ? AND contrato = ? AND yntp = ? AND valor = ? AND cuenta_local = ? ");
            delete1.setParameter(1,period);
            delete1.setParameter(2,part[0].toString());
            delete1.setParameter(3,part[3].toString());
            delete1.setParameter(4,part[1].toString());
            delete1.setParameter(5,Double.parseDouble(part[2].toString()));
            delete1.setParameter(6,part[4].toString());
            delete1.executeUpdate();
        }


    }

    public List<Object[]> getNeoconInter(String period) {

        Query resultRec = entityManager.createNativeQuery("select distinct b.grscing from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def a\n" +
                "left join nexco_cuentas_neocon b\n" +
                "on a.cod_neocon = b.cuenta\n" +
                "where a.periodo = ?");
        resultRec.setParameter(1,period);

        return resultRec.getResultList();
    }

    public List<Object> getHabilDay(String period) {

        Query result = entityManager.createNativeQuery("select * from\n" +
                "(select top 1 FechaHabil\n" +
                "from fechas_habiles\n" +
                "where FechaHabil like ?+'%'\n" +
                "order by NumColumn desc) a\n" +
                "union all\n" +
                "select * from\n" +
                "(select top 1 FechaHabil\n" +
                "from fechas_habiles\n" +
                "where FechaHabil like substring(convert(varchar, DATEADD(d, 1, EOMONTH(convert(date, ?+'-01'))), 23), 1, 7)+'%'\n" +
                "order by NumColumn asc) b");
        result.setParameter(1,period);
        result.setParameter(2,period);

        return result.getResultList();
    }

    public List<IntergrupoV2> findIntergrupo2(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.id_reporte, em.cod_neocon, em.cod_pais, em.componente, em.contrato, \n" +
                "em.cuenta_local, em.divisa, em.fuente, em.input, em.nit, em.pais, em.periodo, em.sociedad_yntp, convert(numeric(20,2), em.valor) valor, em.yntp, em.yntp_empresa_reportante \n" +
                "FROM /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def as em \n" +
                "WHERE em.id_reporte = ?",IntergrupoV2.class);

        query.setParameter(1, id);
        return query.getResultList();
    }

    public List<Currency> getDivisas(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_divisas as em ",Currency.class);

        return query.getResultList();
    }

    public List<Country> getPaises(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_paises as em ",Country.class);

        return query.getResultList();
    }


    public List<Object[]> getCuenta(String cuenta){
        Query query = entityManager.createNativeQuery("select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013' and nucta = ?");
        query.setParameter(1, cuenta);
        return query.getResultList();
    }

    public List<Object[]> getCodiCons(String codicons){
        Query query = entityManager.createNativeQuery("select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013' and CODICONS46 = ?");
        query.setParameter(1, codicons);
        return query.getResultList();
    }

    public List validateTableIntProv(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT top 1 origen FROM nexco_plano_ifrs9_intergrupo \n" +
                " WHERE periodo = ? and origen = 'INTERGRUPO_PROVIS.TXT';");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public List validateTableIntRec(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT top 1 origen FROM nexco_plano_ifrs9_intergrupo \n" +
                "WHERE periodo = ? and origen = 'INTERGRUPO_RECLAS.TXT';");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public List validateTableInterV3Val(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT top 1 * FROM nexco_intergrupo_v3_valida \n" +
                "WHERE periodo = ? ;");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public List validateTableInterV3Aju(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT top 1 * FROM nexco_intergrupo_v3_final \n" +
                "WHERE periodo = ? ;");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public void saveLog(User user,String accion)
    {
        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion(accion);
        insert.setCentro(user.getCentro());
        insert.setComponente("Intergrupo");
        insert.setFecha(today);
        insert.setInput("Intergrupo V3 Banco");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<IntergrupoV3Final> getAllFromV3Final(String periodo) {

        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v3_final WHERE periodo = ?", IntergrupoV3Final.class);
        result.setParameter(1, periodo);
        List<IntergrupoV3Final> data = result.getResultList();

        return data;
    }

    public void processAjuInterV3(String period){
        Query queryValidate = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v3_def \n" +
                "WHERE periodo = ? ;");
        queryValidate.setParameter(1, period);
        queryValidate.executeUpdate();

        Query result = entityManager.createNativeQuery("Insert into nexco_intergrupo_v3_def (yntp_empresa_reportante,cod_neocon,divisa,yntp,sociedad_yntp,contrato,\n" +
                "nit,valor,cod_pais,pais,cuenta_local,periodo,fuente,input,componente) SELECT base.yntp_empresa_reportante,base.cod_neocon,base.divisa,base.yntp,base.sociedad_yntp,base.contrato,\n" +
                "base.nit,case when ajuste.valor is null then base.valor else (base.valor+ajuste.valor) end as valor,base.cod_pais,\n" +
                "base.pais,base.cuenta_local,base.periodo,base.fuente,base.input,base.componente\n" +
                "FROM (SELECT * FROM nexco_intergrupo_v3 WHERE periodo = ?) AS base LEFT JOIN\n" +
                "(SELECT cod_neocon,cod_pais,componente,contrato,cuenta_local,divisa,fuente,input,nit,pais,periodo,sociedad_yntp,yntp,yntp_empresa_reportante,sum(valor) as valor FROM nexco_intergrupo_v3_final WHERE periodo = ? \n" +
                "group by cod_neocon,cod_pais,componente,contrato,cuenta_local,divisa,fuente,input,nit,pais,periodo,sociedad_yntp,yntp,yntp_empresa_reportante) AS ajuste ON  \n" +
                "base.cod_neocon=ajuste.cod_neocon and base.cod_pais=ajuste.cod_pais and\n" +
                "base.contrato=ajuste.contrato and base.cuenta_local=ajuste.cuenta_local and base.divisa=ajuste.divisa and\n" +
                "base.nit=ajuste.nit and\n" +
                "base.pais=ajuste.pais and base.periodo=ajuste.periodo and base.sociedad_yntp=ajuste.sociedad_yntp and\n" +
                "base.yntp=ajuste.yntp and base.yntp_empresa_reportante=ajuste.yntp_empresa_reportante ");
        result.setParameter(1, period);
        result.setParameter(2, period);
        result.executeUpdate();

        Query queryValidate2 = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v3_def \n" +
                "WHERE periodo = ? and valor=0;");
        queryValidate2.setParameter(1, period);
        queryValidate2.executeUpdate();
    }

    public void auditCode (String info, User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setComponente("Intergrupo");
        insert.setFecha(today);
        insert.setInput("Versión 3");
        if(user!=null)
        {
            insert.setNombre(user.getNombre());
            insert.setUsuario(user.getUsuario());
            insert.setCentro(user.getCentro());
        }
        else
        {
            insert.setNombre("SYSTEM JOB");
        }
        auditRepository.save(insert);
    }

    public List<IntergrupoV3> getAllFromV3FinalAju(String periodo) {

        Query result = entityManager.createNativeQuery("SELECT base.id_reporte,base.yntp_empresa_reportante,base.cod_neocon,base.divisa,base.yntp,base.sociedad_yntp,base.contrato,\n" +
                "base.nit,base.valor,base.cod_pais,\n" +
                "base.pais,base.cuenta_local,base.periodo,base.fuente,base.input,base.componente\n" +
                "FROM nexco_intergrupo_v3_def as base WHERE periodo = ?", IntergrupoV3.class);
        result.setParameter(1, periodo);
        List<IntergrupoV3> data = result.getResultList();

        return data;
    }

    public List<IntergrupoV3Valida> getAllInterValidacionV3(String periodo) {

        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v3_valida WHERE periodo = ?", IntergrupoV3Valida.class);
        result.setParameter(1, periodo);
        List<IntergrupoV3Valida> data = result.getResultList();

        return data;
    }

    public List<IntergrupoV3Valida> getAllInterValidacionV3Temp(String periodo) {

        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v3_temp WHERE periodo = ?", IntergrupoV3Valida.class);
        result.setParameter(1, periodo);
        List<IntergrupoV3Valida> data = result.getResultList();

        return data;
    }

    public ArrayList<String[]> saveFileValidateIntergrupoV3(InputStream file, User user, String period) throws IOException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows;
            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet = wb.getSheetAt(0);
                rows = sheet.iterator();
                list = validarValInterV3(rows, user, period);
            }
            catch (Exception e) {
                String[] error = new String[3];
                error[0] = "0";
                error[1] = "1";
                error[2] = "FAIL";
                list.add(error);
                e.printStackTrace();
            }
        }
        return list;
    }

    public ArrayList<String[]> validarValInterV3(Iterator<Row> rows, User user, String period) {

        ArrayList<String[]> lista = new ArrayList();
        ArrayList<IntergrupoV3Valida> interList = new ArrayList();
        XSSFRow row;
        int firstRow = 1;

        Query deleteAp1 = entityManager.createNativeQuery("delete from nexco_intergrupo_v3_valida where periodo = ?;");
        deleteAp1.setParameter(1,period);
        deleteAp1.executeUpdate();

        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (firstRow != 1 && row != null) {
                DataFormatter formatter = new DataFormatter();

                String cellYNTPEmpresaReportante = formatter.formatCellValue(row.getCell(0)).trim();
                String cellCodNeocon = formatter.formatCellValue(row.getCell(1)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(2)).trim();
                String cellYntp = formatter.formatCellValue(row.getCell(3)).trim();
                String cellSociedadYNTP = formatter.formatCellValue(row.getCell(4)).trim();
                String cellContrato = formatter.formatCellValue(row.getCell(5)).trim();
                String cellNITContraparte = formatter.formatCellValue(row.getCell(6)).trim();
                String cellValor = formatter.formatCellValue(row.getCell(7)).trim();
                String cellCodPaís = formatter.formatCellValue(row.getCell(8)).trim();
                String cellPaís = formatter.formatCellValue(row.getCell(9)).trim();
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(10)).trim();
                String cellPeriodo = formatter.formatCellValue(row.getCell(11)).trim();
                String cellFuente = formatter.formatCellValue(row.getCell(12)).trim();
                String cellInput = formatter.formatCellValue(row.getCell(13)).trim();
                String cellComponente = formatter.formatCellValue(row.getCell(14)).trim();
                String cellCuentaPlano = formatter.formatCellValue(row.getCell(15)).trim();
                String cellValorProv = formatter.formatCellValue(row.getCell(16)).trim();
                String cellValorRec = formatter.formatCellValue(row.getCell(17)).trim();
                String cellIntergrupo = formatter.formatCellValue(row.getCell(18)).trim();
                String cellPerimetro = formatter.formatCellValue(row.getCell(19)).trim();
                String cellElimina = formatter.formatCellValue(row.getCell(20)).trim();

                if (cellYNTPEmpresaReportante.trim().length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El YNTP Empresa Reportante debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellCodNeocon.trim().length() !=5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El CodNeocon debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellDivisa.trim().length() !=3) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "La Divisa debe estar diligenciado a 3 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellYntp.trim().length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El YNTP debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellSociedadYNTP.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "La Sociedad YNTP no puede estar vacía.";
                    lista.add(log1);
                }
                /*if (cellContrato.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El Contrato no puede estar vacío.";
                    lista.add(log1);
                }*/
                if (cellNITContraparte.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El NIT Contraparte no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellCodPaís.trim().length() != 2) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(8);
                    log1[2] = "El Cod País debe estar diligenciado a 2 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellPaís.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(9);
                    log1[2] = "El País no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellCuentaLocal.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(10);
                    log1[2] = "La Cuenta Local no puede estar vacía.";
                    lista.add(log1);
                }
                if (!cellPeriodo.trim().equals(period)) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(11);
                    log1[2] = "El periodo reportado no es el mismo al cual se desea cargar.";
                    lista.add(log1);
                }
                if (cellFuente.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(12);
                    log1[2] = "El Fuente reportada no puede estar vacía.";
                    lista.add(log1);
                }
                /*if (cellInput.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(13);
                    log1[2] = "El Input reportado no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellComponente.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(14);
                    log1[2] = "El Componente reportado no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellCuentaPlano.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(15);
                    log1[2] = "La Cuenta Plano no puede estar vacía.";
                    lista.add(log1);
                }*/
                if (cellPerimetro.trim().length() > 0 && cellPerimetro.trim().length() != 2) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(19);
                    log1[2] = "El Perimetro no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellIntergrupo.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(18);
                    log1[2] = "El Intergrupo no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellElimina.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(20);
                    log1[2] = "El Campo Elimina no puede estar vacío.";
                    lista.add(log1);
                }


                Double valapp;
                Double valappProv;
                Double valappRec;
                try{
                    valapp = !cellValor.isEmpty()?Double.parseDouble(cellValor.trim().replace(".","").replace(",",".").replace("(","-").replace(")","")):.0;
                }catch (Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El Valor debe ser informado númerico";
                    lista.add(log1);
                    valapp = .0;
                }
                try{
                    valappProv = !cellValorProv.isEmpty()?Double.parseDouble(cellValorProv.trim().replace(".","").replace(",",".").replace("(","-").replace(")","")):.0;
                }catch (Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El Valor Prov debe ser informado númerico";
                    lista.add(log1);
                    valappProv = .0;
                }
                try{
                    valappRec = !cellValorRec.isEmpty()?Double.parseDouble(cellValorRec.trim().replace(".","").replace(",",".").replace("(","-").replace(")","")):.0;
                }catch (Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El Valor Rec debe ser informado númerico";
                    lista.add(log1);
                    valappRec = .0;
                }

                IntergrupoV3Valida temporalInter = new IntergrupoV3Valida();
                temporalInter.setYntpEmpresaReportante(cellYNTPEmpresaReportante);
                temporalInter.setCodNeocon(cellCodNeocon);
                temporalInter.setDivisa(cellDivisa);
                temporalInter.setYntp(cellYntp);
                temporalInter.setSociedadYntp(cellSociedadYNTP);
                temporalInter.setContrato(cellContrato);
                temporalInter.setNit(cellNITContraparte);
                temporalInter.setValor(valapp);
                temporalInter.setCodPais(cellCodPaís);
                temporalInter.setPais(cellPaís);
                temporalInter.setCuentaLocal(cellCuentaLocal);
                temporalInter.setPeriodo(period);
                temporalInter.setFuente(cellFuente);
                temporalInter.setInput(cellInput);
                temporalInter.setComponente(cellComponente);
                temporalInter.setCuentaPlano(cellCuentaPlano);
                temporalInter.setValorProv(valappProv);
                temporalInter.setValorRec(valappRec);
                temporalInter.setIntergrupo(cellIntergrupo);
                temporalInter.setPerimetro(cellPerimetro);
                temporalInter.setElimina(cellElimina);
                interList.add(temporalInter);

            } else {
                firstRow = 0;
            }
        }
        String[] log = new String[3];
        if(interList.size()-lista.size()<0)
            log[0] = String.valueOf(0);
        else
            log[0] = String.valueOf(interList.size()-lista.size());
        log[1] = String.valueOf(lista.size());
        if(lista.isEmpty())
        {
            intergrupoV3ValidaRepository.saveAll(interList);
            log[2] = "COMPLETE";
        }
        else
        {
            log[2] = "FAILED";
        }
        lista.add(log);

        return lista;
    }

    public ArrayList<String[]> saveFileFinalIntergrupo(InputStream file, User user, String period) throws IOException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows;
            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet = wb.getSheetAt(0);
                rows = sheet.iterator();
                list = validarPlantillaFinal(rows, user, period);
            }
            catch (Exception e) {
                String[] error = new String[3];
                error[0] = "0";
                error[1] = "1";
                error[2] = "FAIL";
                list.add(error);
                e.printStackTrace();
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantillaFinal(Iterator<Row> rows, User user, String period) {

        ArrayList<String[]> lista = new ArrayList();
        ArrayList<IntergrupoV3Final> interList = new ArrayList();
        XSSFRow row;
        int firstRow = 1;

        Query deleteAp1 = entityManager.createNativeQuery("delete from nexco_intergrupo_v3_final where periodo = ?;");
        deleteAp1.setParameter(1,period);
        deleteAp1.executeUpdate();

        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (firstRow != 1 && row != null) {
                DataFormatter formatter = new DataFormatter();

                String cellYNTPEmpresaReportante = formatter.formatCellValue(row.getCell(0)).trim();
                String cellCodNeocon = formatter.formatCellValue(row.getCell(1)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(2)).trim();
                String cellYntp = formatter.formatCellValue(row.getCell(3)).trim();
                String cellSociedadYNTP = formatter.formatCellValue(row.getCell(4)).trim();
                String cellContrato = formatter.formatCellValue(row.getCell(5)).trim();
                String cellNITContraparte = formatter.formatCellValue(row.getCell(6)).trim();
                String cellValor = formatter.formatCellValue(row.getCell(7)).trim();
                String cellCodPaís = formatter.formatCellValue(row.getCell(8)).trim();
                String cellPaís = formatter.formatCellValue(row.getCell(9)).trim();
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(10)).trim();
                String cellPeriodo = formatter.formatCellValue(row.getCell(11)).trim();
                String cellFuente = formatter.formatCellValue(row.getCell(12)).trim();
                String cellInput = formatter.formatCellValue(row.getCell(13)).trim();
                String cellComponente = formatter.formatCellValue(row.getCell(14)).trim();

                if (cellYNTPEmpresaReportante.trim().length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El YNTP Empresa Reportante debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellCodNeocon.trim().length() !=5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El CodNeocon debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellDivisa.trim().length() !=3) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "La Divisa debe estar diligenciado a 3 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellYntp.trim().length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El YNTP debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellSociedadYNTP.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "La Sociedad YNTP no puede estar vacía.";
                    lista.add(log1);
                }
                /*if (cellContrato.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El Contrato no puede estar vacío.";
                    lista.add(log1);
                }*/
                if (cellNITContraparte.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El NIT Contraparte no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellCodPaís.trim().length() != 2) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(8);
                    log1[2] = "El Cod País debe estar diligenciado a 2 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellPaís.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(9);
                    log1[2] = "El País no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellCuentaLocal.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(10);
                    log1[2] = "La Cuenta Local no puede estar vacía.";
                    lista.add(log1);
                }
                if (!cellPeriodo.trim().equals(period)) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(11);
                    log1[2] = "El periodo reportado no es el mismo al cual se desea cargar.";
                    lista.add(log1);
                }
                if (cellFuente.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(12);
                    log1[2] = "El Fuente reportada no puede estar vacía.";
                    lista.add(log1);
                }
                /*if (cellInput.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(13);
                    log1[2] = "El Input reportado no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellComponente.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(14);
                    log1[2] = "El Componente reportado no puede estar vacío.";
                    lista.add(log1);
                }*/

                Double valapp;
                try{
                    System.out.println(cellValor);
                    valapp = !cellValor.isEmpty()?Double.parseDouble(cellValor.replace(" ","").replace(".","").replace(",",".").replace("(","-").replace(")","")):.0;
                }catch (Exception e){
                    e.printStackTrace();
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El Valor debe ser informado númerico";
                    lista.add(log1);
                    valapp = .0;
                }

                IntergrupoV3Final temporalInter = new IntergrupoV3Final();
                temporalInter.setYntpReportante(cellYNTPEmpresaReportante);
                temporalInter.setCodNeocon(cellCodNeocon);
                temporalInter.setDivisa(cellDivisa);
                temporalInter.setYntp(cellYntp);
                temporalInter.setSociedadYntp(cellSociedadYNTP);
                temporalInter.setContrato(cellContrato);
                temporalInter.setNit(cellNITContraparte);
                temporalInter.setValor(valapp);
                temporalInter.setCodPais(cellCodPaís);
                temporalInter.setPais(cellPaís);
                temporalInter.setCuentaLocal(cellCuentaLocal);
                temporalInter.setPeriodo(period);
                temporalInter.setFuente(cellFuente);
                temporalInter.setInput(cellInput);
                temporalInter.setComponente(cellComponente);
                interList.add(temporalInter);

            } else {
                firstRow = 0;
            }
        }
        String[] log = new String[3];
        log[0] = String.valueOf(interList.size()-lista.size());
        log[1] = String.valueOf(lista.size());
        if(lista.isEmpty())
        {
            intergrupoV3FinalRepository.saveAll(interList);
            log[2] = "COMPLETE";
        }
        else
        {
            log[2] = "FAILED";
        }
        lista.add(log);

        return lista;
    }
}
