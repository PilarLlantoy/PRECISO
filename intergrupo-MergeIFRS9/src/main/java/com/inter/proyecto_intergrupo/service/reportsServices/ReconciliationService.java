package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.parametric.Subsidiaries;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1;
import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class ReconciliationService {

    @Autowired
    EntityManager entityManager;

    public List<Object[]> reconciliationIntergroupV1(String periodo, String company, String query, String inter, String tipo){

        String filterDiff = "";
        String table = "";

        if(inter.equals("v1")){
            table = "/*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def";
        }else if(inter.equals("v2")){
            table = "/*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def";
        }else{
            table = "/*nexco_intergrupo_v3*/ nexco_intergrupo_v3_def";
        }

        if(tipo.equals("diff")){
            filterDiff = "where isnull(abs(a.salmes), 0) < isnull(CONVERT(numeric(20,2), CAST(abs(b.valor) AS FLOAT)), 0) and isnull(abs(a.salmes), 0)-isnull(CONVERT(numeric(20,2), CAST(abs(b.valor) AS FLOAT)), 0) not between -5 and 5";
        }

        Query result = entityManager.createNativeQuery("SELECT a.empresa, b.cuenta_local, b.divisa, isnull(abs(a.salmes), 0), isnull(CONVERT(numeric(20,2), CAST(abs(b.valor) AS FLOAT)), 0) valor, isnull(abs(a.salmes), 0)-isnull(CONVERT(numeric(20,2), CAST(abs(b.valor) AS FLOAT)), 0) diff\n" +
                "FROM (select sum(a.valor) valor, a.cuenta_local, a.divisa from (\n" +
                "select z.valor, z.cuenta_local, z.divisa from "+table+" z\n" +
                "INNER JOIN (select distinct cuenta_local from nexco_cuentas_responsables) y\n" +
                "on z.cuenta_local like convert(varchar, y.cuenta_local)+'%'\n" +
                "where periodo = ?\n" +
                "group by z.valor, z.cuenta_local, z.divisa, z.id_reporte) a\n" +
                "group by a.cuenta_local, a.divisa) b\n" +
                "LEFT JOIN (select empresa, nucta, case when coddiv = 'COD' then CASE WHEN substring(nucta, 1, 1) not in ('4', '5') THEN 'COP' ELSE coddiv END else coddiv end coddiv, sum(salmes) salmes from nexco_query_marcados where substring(fecont, 1, 7) = ? and empresa = ? and origen = ? group by empresa, nucta, case when coddiv = 'COD' then CASE WHEN substring(nucta, 1, 1) not in ('4', '5') THEN 'COP' ELSE coddiv END else coddiv end) a \n" +
                "ON a.nucta = b.cuenta_local and b.divisa = a.coddiv \n" +
                ""+filterDiff+"; \n");

        result.setParameter(1,periodo);
        result.setParameter(2,periodo);
        result.setParameter(3,company);
        result.setParameter(4,query);

        return result.getResultList();

    }

    public List<Object[]> reconciliationIntergroupV1Neocon(String periodo, String company, String query, String inter, String tipo){

        String filterDiff = "";
        String table = "";

        if(inter.equals("v1")){
            table = "/*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def";
        }else if(inter.equals("v2")){
            table = "/*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def";
        }else{
            table = "/*nexco_intergrupo_v3*/ nexco_intergrupo_v3_def";
        }

        if(tipo.equals("diff")){
            filterDiff = "where isnull(abs(a.salmes), 0) < isnull(CONVERT(numeric(20,2), CAST(abs(b.valor) AS FLOAT)), 0) and isnull(abs(a.salmes), 0)-isnull(CONVERT(numeric(20,2), CAST(abs(b.valor) AS FLOAT)), 0) not between -5 and 5";
        }

        Query result = entityManager.createNativeQuery("SELECT a.empresa, b.cod_neocon, b.divisa, isnull(abs(a.salmes), 0), isnull(CONVERT(numeric(20,2), CAST(b.valor AS FLOAT)), 0) valor, isnull(abs(a.salmes), 0)-isnull(CONVERT(numeric(20,2), CAST(abs(b.valor) AS FLOAT)), 0) diff\n" +
                "FROM (select sum(a.valor) valor, a.cod_neocon, a.divisa from (\n" +
                "select z.valor, z.cod_neocon, z.divisa from "+table+" z\n" +
                "INNER JOIN (select distinct cuenta_local from nexco_cuentas_responsables) y\n" +
                "on z.cuenta_local like convert(varchar, y.cuenta_local)+'%'\n" +
                "where periodo = ?\n" +
                "group by z.valor, z.cod_neocon, z.divisa, z.id_reporte) a\n" +
                "group by a.cod_neocon, a.divisa) b\n" +
                "LEFT JOIN (select empresa, codicons, case when coddiv = 'COD' then CASE WHEN substring(nucta, 1, 1) not in ('4', '5') THEN 'COP' ELSE coddiv END else coddiv end coddiv, sum(salmes) salmes from nexco_query_marcados \n" +
                "where substring(fecont, 1, 7) = ? and empresa = ? and origen = ?  \n" +
                "group by empresa, codicons, case when coddiv = 'COD' then CASE WHEN substring(nucta, 1, 1) not in ('4', '5') THEN 'COP' ELSE coddiv END else coddiv end) a \n" +
                "ON a.codicons = b.cod_neocon and b.divisa = a.coddiv \n" +
                ""+filterDiff+";");

        result.setParameter(1,periodo);
        result.setParameter(2,periodo);
        result.setParameter(3,company);
        result.setParameter(4,query);

        return result.getResultList();
        //return data;

    }

    public void reconciliationIntergroupUpdateNeocon(String periodo){

        Query result = entityManager.createNativeQuery("update a\n" +
                "set a.cod_neocon = b.CODICONS46\n" +
                "from nexco_intergrupo_v1 a\n" +
                "inner join (select nucta, CODICONS46 from CUENTAS_PUC where EMPRESA = '0013') b\n" +
                "on a.cuenta_local = b.nucta\n" +
                "where a.periodo = ?\n" +
                ";");
        result.setParameter(1,periodo);
        result.executeUpdate();

    }

    public List<Object[]> getCompany(String periodo){

        Query companies = entityManager.createNativeQuery(
                "select distinct empresa from nexco_query_marcados where substring(fecont, 1, 7) = ? \n");

        companies.setParameter(1,periodo);

        return companies.getResultList();

    }

    public List<IntergrupoV1> getIntergroupV1(String periodo){

        Query resultInt = entityManager.createNativeQuery("SELECT * FROM /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def where periodo = ?", IntergrupoV1.class);
        resultInt.setParameter(1,periodo);
        List<IntergrupoV1> data = resultInt.getResultList();
        return data;
    }

    public List<Object[]> getCdM(String periodo){

        Query resultInt = entityManager.createNativeQuery("SELECT componente, semaforo_componente\n" +
                "FROM preciso_administracion_cuadro_mando \n" +
                "where fecha_reporte  = ? \n" +
                "group by componente, semaforo_componente");
        resultInt.setParameter(1,periodo);
        return resultInt.getResultList();
    }

    public void reconciliationIntergroupV1Fil(String periodo, String version){

        String table = "";

        if(version.equals("v1")){
            table = "/*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def";
        }else if (version.equals("v2")){
            table = "/*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def";
        }else{
            table = "/*nexco_intergrupo_v3*/ nexco_intergrupo_v3_def";
        }

        Query deleteInfoInter = entityManager.createNativeQuery("truncate table nexco_intergrupo_v1_temp;");
        deleteInfoInter.executeUpdate();

        Query deleteInfoFil = entityManager.createNativeQuery("truncate table nexco_intergrupo_filial_temp;");
        deleteInfoFil.executeUpdate();

        Query deleteInfo1 = entityManager.createNativeQuery("truncate table nexco_intergrupo_conc_tm1;");
        deleteInfo1.executeUpdate();

        Query deleteInfo2 = entityManager.createNativeQuery("truncate table nexco_intergrupo_conc_tm2;");
        deleteInfo2.executeUpdate();

        Query deleteInfo3 = entityManager.createNativeQuery("truncate table nexco_intergrupo_conc_tm3;");
        deleteInfo3.executeUpdate();

        Query deleteInfo5 = entityManager.createNativeQuery("truncate table nexco_intergrupo_conc_tm5;");
        deleteInfo5.executeUpdate();

        Query deleteInfo6 = entityManager.createNativeQuery("truncate table nexco_intergrupo_conc_tm6;");
        deleteInfo6.executeUpdate();

        Query deleteInfo7 = entityManager.createNativeQuery("delete from nexco_intergrupo_recon where periodo = ?");
        deleteInfo7.setParameter(1,periodo);
        deleteInfo7.executeUpdate();

        Query deleteInfo8 = entityManager.createNativeQuery("delete from nexco_intergrupo_recon_just where periodo = ?");
        deleteInfo8.setParameter(1,periodo);
        deleteInfo8.executeUpdate();

        Query insertInt = entityManager.createNativeQuery("insert into nexco_intergrupo_v1_temp (cod_neocon, cod_pais, contrato, cuenta_local, divisa, fuente, nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante, componente, input, valida)\n" +
                "select cod_neocon, cod_pais, contrato, cuenta_local, divisa, fuente, nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante, componente, input, 'N' from "+table+" where periodo = ?\n" +
                "and yntp in ('00560', '00561', '00565', '00566', '00570', '70054') ;");
        insertInt.setParameter(1,periodo);
        insertInt.executeUpdate();

        Query insertFil = entityManager.createNativeQuery("insert into nexco_intergrupo_filial_temp (cod_neocon, cod_pais, contrato, cuenta_local, divisa, nit_contraparte, observaciones, pais, periodo, sociedad_yntp, usuario, valor, yntp, yntp_reportante, valida)\n" +
                "select cod_neocon, cod_pais, contrato, cuenta_local, divisa, nit_contraparte, observaciones, pais, periodo, sociedad_yntp, usuario, valor, yntp, yntp_reportante, 'N' from nexco_filiales_intergrupo where periodo = ? \n" +
                "and yntp_reportante in ('00560', '00561', '00565', '00566', '00570', '70054') and yntp = '00548';");
        insertFil.setParameter(1,periodo);
        insertFil.executeUpdate();

        Query insertRec1 = entityManager.createNativeQuery("insert into nexco_intergrupo_conc_tm1 (cuenta_local, contrato_banco, contrato_filial, yntp_empresa, cuenta_filial, concepto, valor_banco, valor_filial)\n" +
                "select a.*, sum(isnull(b.valor_banco, 0)) valor_banco, sum(isnull(c.valor_filial, 0)) valor_filial from \n" +
                "(select distinct a.cuenta_local, a.contrato_banco, a.contrato_filial, yntp_empresa, a.cuenta_filial, a.conceptos from nexco_filiales a\n" +
                "left join (select nucta, INDIC from CUENTAS_PUC where empresa = '0013') b\n" +
                "on a.cuenta_local = b.NUCTA\n" +
                "where b.INDIC = 'I' and (a.cuenta_local is not null or a.cuenta_filial is not null)) a\n" +
                "left join (select yntp, cuenta_local, contrato, sum(valor) valor_banco from nexco_intergrupo_v1_temp group by cuenta_local, contrato, yntp) b\n" +
                "on a.yntp_empresa = b.yntp and a.cuenta_local = b.cuenta_local and a.contrato_banco = b.contrato\n" +
                "left join (select yntp_reportante yntp_filial, contrato, cuenta_local cuenta_filial, sum(valor) valor_filial from nexco_intergrupo_filial_temp group by cuenta_local, contrato, yntp_reportante) c\n" +
                "on a.yntp_empresa = c.yntp_filial and a.cuenta_filial = c.cuenta_filial and a.contrato_filial = c.contrato\n" +
                "group by a.cuenta_local, a.contrato_banco, a.yntp_empresa, a.cuenta_filial,a.contrato_filial, a.conceptos\n" +
                ";");
        //insertRec1.setParameter(1,period);
        insertRec1.executeUpdate();

        Query UpdateInt1 = entityManager.createNativeQuery("update a\n" +
                "set a.valida = '1'\n" +
                "from nexco_intergrupo_v1_temp a\n" +
                "inner join nexco_intergrupo_conc_tm1 b\n" +
                "on a.yntp = b.yntp_empresa and a.cuenta_local = b.cuenta_local and a.contrato = b.contrato_banco\n" +
                "where b.valor_banco <> 0\n" +
                ";");
        //UpdateInt1.setParameter(1,period);
        UpdateInt1.executeUpdate();

        Query UpdateFil1 = entityManager.createNativeQuery("update a\n" +
                "set a.valida = '1'\n" +
                "from nexco_intergrupo_filial_temp a\n" +
                "inner join nexco_intergrupo_conc_tm1 b\n" +
                "on a.yntp_reportante = b.yntp_empresa and a.cuenta_local = b.cuenta_filial and a.contrato = b.contrato_filial\n" +
                "where b.valor_filial <> 0\n" +
                ";");
        //UpdateFil1.setParameter(1,period);
        UpdateFil1.executeUpdate();

        Query insertRec2 = entityManager.createNativeQuery("insert into nexco_intergrupo_conc_tm2 (cuenta_local, yntp_empresa, cuenta_filial, concepto, valor_banco, valor_filial)\n" +
                "select a.*, sum(isnull(b.valor_banco, 0)) valor_banco, sum(isnull(c.valor_filial, 0)) valor_filial from \n" +
                "(select distinct a.cuenta_local, yntp_empresa, a.cuenta_filial, a.conceptos from nexco_filiales a\n" +
                "left join (select nucta, INDIC from CUENTAS_PUC where empresa = '0013') b\n" +
                "on a.cuenta_local = b.NUCTA\n" +
                "where b.INDIC = 'I' and (a.cuenta_local is not null or a.cuenta_filial is not null)) a\n" +
                "left join (select yntp, cuenta_local, contrato, sum(valor) valor_banco from nexco_intergrupo_v1_temp where valida = 'N' group by cuenta_local, contrato, yntp) b\n" +
                "on a.yntp_empresa = b.yntp and a.cuenta_local = b.cuenta_local\n" +
                "left join (select yntp_reportante yntp_filial, cuenta_local cuenta_filial, sum(valor) valor_filial from nexco_intergrupo_filial_temp where valida = 'N' group by cuenta_local, yntp_reportante) c\n" +
                "on a.yntp_empresa = c.yntp_filial and a.cuenta_filial = c.cuenta_filial\n" +
                "group by a.cuenta_local, a.yntp_empresa, a.cuenta_filial, a.conceptos\n" +
                ";");
        //insertRec2.setParameter(1,period);
        insertRec2.executeUpdate();

        Query UpdateInt2 = entityManager.createNativeQuery("update a\n" +
                "set a.valida = '2'\n" +
                "from nexco_intergrupo_v1_temp a\n" +
                "inner join nexco_intergrupo_conc_tm2 b\n" +
                "on a.yntp = b.yntp_empresa and a.cuenta_local = b.cuenta_local\n" +
                "where b.valor_banco <> 0 and a.valida = 'N'\n" +
                ";");
        //UpdateInt2.setParameter(1,period);
        UpdateInt2.executeUpdate();

        Query UpdateFil2 = entityManager.createNativeQuery("update a\n" +
                "set a.valida = '2'\n" +
                "from nexco_intergrupo_filial_temp a\n" +
                "inner join nexco_intergrupo_conc_tm2 b\n" +
                "on a.yntp_reportante = b.yntp_empresa and a.cuenta_local = b.cuenta_filial\n" +
                "where b.valor_filial <> 0 and a.valida = 'N'\n" +
                ";");
        //UpdateFil2.setParameter(1,period);
        UpdateFil2.executeUpdate();

        Query insertRec3 = entityManager.createNativeQuery("insert into nexco_intergrupo_conc_tm3 (cuenta_local, yntp_empresa, cuenta_filial, concepto, valor_banco, valor_filial)\n" +
                "select  isnull(b.cuenta_local, a.cuenta_local) cuenta_local, a.yntp_empresa, isnull(c.cuenta_filial, a.cuenta_filial) cuenta_filial, a.conceptos, \n" +
                "sum(isnull(b.valor_banco, 0)) valor_banco, \n" +
                "sum(isnull(c.valor_filial, 0)) valor_filial from \n" +
                "(select distinct a.cuenta_local, yntp_empresa, a.cuenta_filial, a.conceptos from nexco_filiales a\n" +
                "left join (select nucta, INDIC from CUENTAS_PUC where empresa = '0013') b\n" +
                "on a.cuenta_local = b.NUCTA\n" +
                "where b.INDIC = 'L' and (a.cuenta_local is not null or a.cuenta_filial is not null)) a\n" +
                "left join (select yntp, cuenta_local, sum(valor) valor_banco from nexco_intergrupo_v1_temp where valida = 'N' group by cuenta_local, yntp) b\n" +
                "on b.cuenta_local like case when a.cuenta_local = '' then null else a.cuenta_local end+'%' and a.yntp_empresa = b.yntp\n" +
                "left join (select yntp_reportante yntp_filial, cuenta_local cuenta_filial, sum(valor) valor_filial from nexco_intergrupo_filial_temp where valida = 'N' group by yntp_reportante, cuenta_local) c\n" +
                "on a.cuenta_filial = c.cuenta_filial and a.yntp_empresa = c.yntp_filial\n" +
                "group by isnull(b.cuenta_local, a.cuenta_local), a.yntp_empresa, isnull(c.cuenta_filial, a.cuenta_filial), a.conceptos \n" +
                ";");
        //insertRec3.setParameter(1,period);
        insertRec3.executeUpdate();


        Query UpdateInt3 = entityManager.createNativeQuery("update a\n" +
                "set a.valida = '3'\n" +
                "from nexco_intergrupo_v1_temp a\n" +
                "inner join nexco_intergrupo_conc_tm3 b\n" +
                "on a.yntp = b.yntp_empresa and a.cuenta_local like case when b.cuenta_local = '' then null else b.cuenta_local end+'%'\n" +
                "where b.valor_banco <> 0 and a.valida = 'N'\n" +
                ";");
        //UpdateInt3.setParameter(1,period);
        UpdateInt3.executeUpdate();

        Query UpdateFil3 = entityManager.createNativeQuery("update a\n" +
                "set a.valida = '3'\n" +
                "from nexco_intergrupo_filial_temp a\n" +
                "inner join nexco_intergrupo_conc_tm3 b\n" +
                "on a.yntp_reportante = b.yntp_empresa and a.cuenta_local = b.cuenta_filial\n" +
                "where b.valor_filial <> 0 and a.valida = 'N'\n" +
                ";");
        //UpdateFil3.setParameter(1,period);
        UpdateFil3.executeUpdate();


        Query insertRec5 = entityManager.createNativeQuery("insert into nexco_intergrupo_conc_tm5 (cuenta_local, yntp_empresa, cuenta_filial, concepto, valor_banco, valor_filial)\n" +
                "select a.cuenta_local, a.yntp_empresa, isnull(c.cuenta_filial, a.cuenta_filial) cuenta_filial, a.conceptos, \n" +
                "sum(isnull(b.valor_banco, 0)) valor_banco, sum(isnull(c.valor_filial, 0)) valor_filial from \n" +
                "(select distinct a.cuenta_local, yntp_empresa, a.cuenta_filial, a.conceptos from nexco_filiales a\n" +
                "left join (select nucta, INDIC from CUENTAS_PUC where empresa = '0013') b\n" +
                "on a.cuenta_local = b.NUCTA\n" +
                "where b.INDIC = 'I' and (a.cuenta_local is not null or a.cuenta_filial is not null)) a\n" +
                "left join (select yntp, cuenta_local, sum(valor) valor_banco from nexco_intergrupo_v1_temp where valida = 'N' group by cuenta_local, yntp) b\n" +
                "on b.cuenta_local = a.cuenta_local and a.yntp_empresa = b.yntp\n" +
                "left join (select yntp_reportante yntp_filial, cuenta_local cuenta_filial, sum(valor) valor_filial from nexco_intergrupo_filial_temp where valida = 'N' group by yntp_reportante, cuenta_local) c\n" +
                "on c.cuenta_filial like case when a.cuenta_filial = '' then null else a.cuenta_filial end +'%' and a.yntp_empresa = c.yntp_filial\n" +
                "group by a.cuenta_local, a.yntp_empresa, isnull(c.cuenta_filial, a.cuenta_filial), a.conceptos\n" +
                ";");
        //insertRec5.setParameter(1,period);
        insertRec5.executeUpdate();

        Query UpdateInt5 = entityManager.createNativeQuery("update a\n" +
                "set a.valida = '5'\n" +
                "from nexco_intergrupo_v1_temp a\n" +
                "inner join nexco_intergrupo_conc_tm5 b\n" +
                "on a.yntp = b.yntp_empresa and a.cuenta_local = b.cuenta_local\n" +
                "where b.valor_banco <> 0 and a.valida = 'N'\n" +
                ";");
        //UpdateInt5.setParameter(1,period);
        UpdateInt5.executeUpdate();

        Query UpdateFil5 = entityManager.createNativeQuery("update a\n" +
                "set a.valida = '5'\n" +
                "from nexco_intergrupo_filial_temp a \n" +
                "inner join nexco_intergrupo_conc_tm5 b \n" +
                "on a.yntp_reportante = b.yntp_empresa and a.cuenta_local like case when b.cuenta_filial = '' then null else b.cuenta_filial end+'%' \n" +
                "where b.valor_filial <> 0 and a.valida = 'N' \n" +
                ";");
        //UpdateFil5.setParameter(1,period);
        UpdateFil5.executeUpdate();


        Query insertRec6 = entityManager.createNativeQuery("insert into nexco_intergrupo_conc_tm6 (cuenta_local, yntp_empresa, cuenta_filial, concepto, valor_banco, valor_filial)\n" +
                "select isnull(b.cuenta_local, a.cuenta_local) cuenta_local, a.yntp_empresa, isnull(c.cuenta_filial, a.cuenta_filial) cuenta_filial, a.conceptos, " +
                "sum(isnull(b.valor_banco, 0)) valor_banco, sum(isnull(c.valor_filial, 0)) valor_filial from \n" +
                "(select distinct a.cuenta_local, yntp_empresa, a.cuenta_filial, a.conceptos from nexco_filiales a\n" +
                "left join (select nucta, INDIC from CUENTAS_PUC where empresa = '0013') b\n" +
                "on a.cuenta_local = b.NUCTA\n" +
                "where b.INDIC = 'L' and (a.cuenta_local is not null or a.cuenta_filial is not null)) a\n" +
                "left join (select yntp, cuenta_local, sum(valor) valor_banco from nexco_intergrupo_v1_temp where valida = 'N' group by cuenta_local, yntp) b\n" +
                "on b.cuenta_local like case when a.cuenta_local = '' then null else a.cuenta_local end+'%' and a.yntp_empresa = b.yntp\n" +
                "left join (select yntp_reportante yntp_filial, cuenta_local cuenta_filial, sum(valor) valor_filial from nexco_intergrupo_filial_temp where valida = 'N' group by yntp_reportante, cuenta_local) c\n" +
                "on c.cuenta_filial like case when a.cuenta_filial = '' then null else a.cuenta_filial end+'%' and a.yntp_empresa = c.yntp_filial\n" +
                "group by isnull(b.cuenta_local, a.cuenta_local), a.yntp_empresa, isnull(c.cuenta_filial, a.cuenta_filial), a.conceptos \n" +
                ";\n");
        //insertRec6.setParameter(1,period);
        insertRec6.executeUpdate();

        Query UpdateInt6 = entityManager.createNativeQuery("update a\n" +
                "set a.valida = '6'\n" +
                "from nexco_intergrupo_v1_temp a\n" +
                "inner join nexco_intergrupo_conc_tm6 b\n" +
                "on a.yntp = b.yntp_empresa and a.cuenta_local like case when b.cuenta_local = '' then null else b.cuenta_local end+'%'\n" +
                "where b.valor_banco <> 0 and a.valida = 'N'\n" +
                ";");
        //UpdateInt6.setParameter(1,period);
        UpdateInt6.executeUpdate();

        Query UpdateFil6 = entityManager.createNativeQuery("update a\n" +
                "set a.valida = '6'\n" +
                "from nexco_intergrupo_filial_temp a\n" +
                "inner join nexco_intergrupo_conc_tm6 b\n" +
                "on a.yntp_reportante = b.yntp_empresa and a.cuenta_local like case when b.cuenta_filial = '' then null else b.cuenta_filial end+'%'\n" +
                "where b.valor_filial <> 0 and a.valida = 'N'\n" +
                ";");
        //UpdateFil6.setParameter(1,period);
        UpdateFil6.executeUpdate();

        Query insertRecF = entityManager.createNativeQuery("insert into nexco_intergrupo_recon (cuenta_local, contrato_banco, contrato_filial, yntp_empresa, cuenta_filial, valor_banco, valor_filial, periodo, concepto, valida)\n" +
                "select cuenta_local, contrato_banco, contrato_filial, yntp_empresa, cuenta_filial, valor_banco, valor_filial, ?, concepto, '1' valida \n" +
                "from nexco_intergrupo_conc_tm1 \n" +
                "--where valor_banco <> 0 or valor_filial <>0\n" +
                "union all\n" +
                "select cuenta_local, null as contrato_banco, null as contrato_filial, yntp_empresa, cuenta_filial, valor_banco, valor_filial, ?, concepto, '2' valida \n" +
                "from nexco_intergrupo_conc_tm2 \n" +
                "--where valor_banco <> 0 or valor_filial <>0\n" +
                "union all\n" +
                "select cuenta_local, null as contrato_banco, null as contrato_filial, yntp_empresa, cuenta_filial, valor_banco, valor_filial, ?, concepto, '3' valida \n" +
                "from nexco_intergrupo_conc_tm3 \n" +
                "--where valor_banco <> 0 or valor_filial <>0\n" +
                "union all\n" +
                "select cuenta_local, null as contrato_banco, null as contrato_filial, yntp_empresa, cuenta_filial, valor_banco, valor_filial, ?, concepto, '5' valida \n" +
                "from nexco_intergrupo_conc_tm5\n" +
                "--where valor_banco <> 0 or valor_filial <>0\n" +
                "union all\n" +
                "select cuenta_local, null as contrato_banco, null as contrato_filial, yntp_empresa, cuenta_filial, valor_banco, valor_filial, ?, concepto, '6' valida \n" +
                "from nexco_intergrupo_conc_tm6\n" +
                "--where valor_banco <> 0 or valor_filial <>0\n" +
                ";");
        insertRecF.setParameter(1,periodo);
        insertRecF.setParameter(2,periodo);
        insertRecF.setParameter(3,periodo);
        insertRecF.setParameter(4,periodo);
        insertRecF.setParameter(5,periodo);
        insertRecF.executeUpdate();

        Query insertNewAccounts = entityManager.createNativeQuery("insert into nexco_filiales (cuenta_local, yntp_empresa, conceptos)\n" +
                "select distinct a.cuenta_local, a.yntp, 'pdte' from nexco_intergrupo_v1_temp a\n" +
                "left join (select id, yntp_empresa, cuenta_local from nexco_filiales) b on a.yntp = b.yntp_empresa and a.cuenta_local = b.cuenta_local\n" +
                "where a.valida = 'N' and b.id is null;");
        //insertNewAccounts.setParameter(1,periodo);
        insertNewAccounts.executeUpdate();

        Query insertNewAccountsFil = entityManager.createNativeQuery("insert into nexco_filiales (cuenta_filial, yntp_empresa, conceptos)\n" +
                "select distinct a.cuenta_local, yntp_reportante, 'pdte' from nexco_intergrupo_filial_temp a\n" +
                "left join (select id, yntp_empresa, cuenta_filial from nexco_filiales) b on a.yntp_reportante = b.yntp_empresa and a.cuenta_local = b.cuenta_filial\n" +
                "where a.valida = 'N' and b.id is null;");
        //insertNewAccounts.setParameter(1,periodo);
        insertNewAccountsFil.executeUpdate();

        Query resultRec = entityManager.createNativeQuery("insert into nexco_intergrupo_recon_just (yntp_empresa, concepto, cuenta_local, cuenta_filial, contrato_banco, valor_banco, valor_filial, diferencia, periodo, tipo_registro)\n" +
                "select yntp_empresa, concepto, cuenta_local, '' cuenta_filial, contrato_banco,\n" +
                "round(valor_banco,0) as valor_banco, 0 valor_filial, 0 diferencia, ? periodo, '' as tipo\n" +
                "from nexco_intergrupo_recon\n" +
                "where 1=1\n" +
                "and periodo = ?\n" +
                "and (valor_banco <> 0 or valor_filial <> 0)\n" +
                "group by valor_banco, cuenta_local, yntp_empresa, contrato_banco, concepto\n" +
                "union all\n" +
                "select yntp_empresa, concepto, '' cuenta_local, cuenta_filial, contrato_banco,\n" +
                "0 valor_banco, round(valor_filial,0) as valor_filial, 0 diferencia, ? periodo, '' as tipo\n" +
                "from nexco_intergrupo_recon\n" +
                "where 1=1\n" +
                "and periodo = ?\n" +
                "and (valor_banco <> 0 or valor_filial <> 0)\n" +
                "group by cuenta_filial, yntp_empresa, contrato_banco, concepto, valor_filial\n" +
                "union all\n" +
                "select isnull(a.yntp_empresa, b.yntp_empresa), isnull(a.concepto, b.concepto)+' Total', '' cuenta_local,\n" +
                "'' cuenta_filial, '' contrato_banco, \n" +
                "round(sum(isnull(a.valor_banco, 0)),0) valor_banco, round(sum(isnull(b.valor_filial, 0)),0) valor_filial, round(sum(isnull(a.valor_banco, 0))-sum(isnull(b.valor_filial, 0)),0) diferencia, ? periodo, 'TOTAL' tipo from\n" +
                "(select x.yntp_empresa, x.concepto, sum(x.valor_banco) valor_banco from\n" +
                "(select yntp_empresa, concepto, valor_banco\n" +
                "from nexco_intergrupo_recon\n" +
                "where valor_banco <> 0\n" +
                "and periodo = ? \n" +
                "group by yntp_empresa, cuenta_local, concepto, valor_banco, contrato_banco) x\n" +
                "group by x.yntp_empresa, x.concepto) a\n" +
                "full outer join \n" +
                "(select z.yntp_empresa, z.concepto, sum(z.valor_filial) valor_filial from\n" +
                "(select yntp_empresa, concepto, valor_filial\n" +
                "from nexco_intergrupo_recon\n" +
                "where valor_filial <> 0\n" +
                "and periodo = ? \n" +
                "group by yntp_empresa, cuenta_filial, concepto, valor_filial, contrato_banco) z\n" +
                "group by z.yntp_empresa, z.concepto) b \n" +
                "on a.yntp_empresa = b.yntp_empresa and a.concepto = b.concepto \n" +
                "group by a.yntp_empresa, a.concepto, b.yntp_empresa, b.concepto \n" +
                "order by 1, 2, 5, 3, 4 \n" +
                ";");

        resultRec.setParameter(1,periodo);
        resultRec.setParameter(2,periodo);
        resultRec.setParameter(3,periodo);
        resultRec.setParameter(4,periodo);
        resultRec.setParameter(5,periodo);
        resultRec.setParameter(6,periodo);
        resultRec.setParameter(7,periodo);
        resultRec.executeUpdate();
    }

    public List<Object[]> reconciliationIntergroupV1FilDownloadDiff() {
        Query resultRec = entityManager.createNativeQuery("select distinct yntp_empresa, isnull(cuenta_local, cuenta_filial) cuenta, case when cuenta_filial is null then 'Banco' else 'Filial' end fuente\n" +
                "from nexco_filiales where conceptos = 'pdte';");
        return resultRec.getResultList();
    }

    public List<Object[]> getYNTPFil() {
        Query result = entityManager.createNativeQuery("select right('00000'+convert(varchar, yntp), 5) yntp, substring(sociedad_corta, 1, 18) sociedad_corta from nexco_sociedades_yntp\n" +
                "where yntp in ('00560', '00561', '00565', '00566', '00570', '70054');");
        return result.getResultList();
    }

    public void updateReconJust(String id, String just, String periodo) {

        Query result = entityManager.createNativeQuery("select tipo_registro, concepto, yntp_empresa from nexco_intergrupo_recon_just \n" +
                "where id = ? ;");
        result.setParameter(1,id);
        List<Object[]> resultId = result.getResultList();

        String tipoRegistro = resultId.get(0)[0].toString();
        String concepto = resultId.get(0)[1].toString();
        String yntp = resultId.get(0)[2].toString();

        if(tipoRegistro.equals("TOTAL")){
            Query update = entityManager.createNativeQuery("update nexco_intergrupo_recon_just \n" +
                    "set justificacion = ? " +
                    "where concepto = ? and periodo = ? and yntp_empresa = ?;");
            update.setParameter(1,just);
            update.setParameter(2,concepto);
            update.setParameter(3,periodo);
            update.setParameter(4,yntp);
            update.executeUpdate();
        }else{
            Query update = entityManager.createNativeQuery("update nexco_intergrupo_recon_just \n" +
                    "set justificacion = ? " +
                    "where id = ? ;");
            update.setParameter(1,just);
            update.setParameter(2,id);
            update.executeUpdate();
        }

    }

    public List<Object[]> reconciliationIntergroupV1FilDownload(String periodo, String yntp, String level) {

        if(yntp.equals("ALL")) {

            if(!level.equals("D")) {

                Query resultFinal = entityManager.createNativeQuery("select yntp_empresa, concepto, case when tipo_registro = 'TOTAL' then a.cuenta_local else substring(a.cuenta_local, 1, "+level+") end cuenta_local, \n" +
                        "case when tipo_registro = 'TOTAL' then cuenta_filial else substring(cuenta_filial, 1, "+level+") end cuenta_filial, sum(valor_banco) valor_banco, sum(valor_filial) valor_filial, sum(abs(valor_banco))-sum(abs(valor_filial)) diferencia, case when tipo_registro = 'TOTAL' then justificacion else '' end justificacion, tipo_registro\n" +
                        ", b.CODICONS46 cod_neocon_banco, c.cod_neocon cod_neocon_filial \n" +
                        "from nexco_intergrupo_recon_just a\n" +
                        "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                        "on a.cuenta_local = b.NUCTA\n" +
                        "left join (select distinct cod_neocon, cuenta_local from nexco_filiales_intergrupo where periodo = ?) c\n" +
                        "on a.cuenta_filial = c.cuenta_local\n" +
                        "where periodo = ?\n" +
                        "group by yntp_empresa, concepto, case when tipo_registro = 'TOTAL' then a.cuenta_local else substring(a.cuenta_local, 1, "+level+") end, case when tipo_registro = 'TOTAL' then cuenta_filial else substring(cuenta_filial, 1, "+level+") end, case when tipo_registro = 'TOTAL' then justificacion else '' end, tipo_registro\n" +
                        ", b.CODICONS46, c.cod_neocon \n" +
                        "--having sum(valor_banco) <> 0 or sum(valor_filial) <> 0 \n" +
                        "order by 1, 2, 5, 3, 4 \n" +
                        ";");
                resultFinal.setParameter(1, periodo);
                resultFinal.setParameter(2, periodo);
                return resultFinal.getResultList();

            }else{

                Query resultFinal = entityManager.createNativeQuery("select a.yntp_empresa, a.concepto, a.cuenta_local, a.cuenta_filial, a.contrato_banco, a.valor_banco valor_banco, " +
                        "a.valor_filial valor_filial, abs(a.valor_banco)-abs(a.valor_filial) diferencia, a.tipo_registro, a.id, a.justificacion \n" +
                        ", b.CODICONS46 cod_neocon_banco, c.cod_neocon cod_neocon_filial \n" +
                        "from nexco_intergrupo_recon_just a \n" +
                        "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                        "on a.cuenta_local = b.NUCTA\n" +
                        "left join (select distinct cod_neocon, cuenta_local, yntp_reportante from nexco_filiales_intergrupo where periodo = ?) c\n" +
                        "on a.cuenta_filial = c.cuenta_local and a.yntp_empresa = c.yntp_reportante \n" +
                        "where periodo = ? and (valor_banco <> 0 or valor_filial <> 0) \n" +
                        "order by 1, 2, 5, 3, 4 \n" +
                        ";");
                resultFinal.setParameter(1, periodo);
                resultFinal.setParameter(2, periodo);
                return resultFinal.getResultList();

            }

        }else{

            if(!level.equals("D")){

                Query resultFinal = entityManager.createNativeQuery("select a.yntp_empresa, a.concepto, case when tipo_registro = 'TOTAL' then a.cuenta_local else substring(a.cuenta_local, 1, "+level+") end cuenta_local, \n" +
                        "case when tipo_registro = 'TOTAL' then cuenta_filial else substring(cuenta_filial, 1, "+level+") end cuenta_filial, sum(valor_banco) valor_banco, sum(valor_filial) valor_filial, sum(abs(valor_banco))-sum(abs(valor_filial)) diferencia, case when tipo_registro = 'TOTAL' then justificacion else '' end justificacion, tipo_registro \n" +
                        ", b.CODICONS46 cod_neocon_banco, c.cod_neocon cod_neocon_filial \n" +
                        "from nexco_intergrupo_recon_just a \n" +
                        "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                        "on a.cuenta_local = b.NUCTA\n" +
                        "left join (select distinct cod_neocon, cuenta_local from nexco_filiales_intergrupo where periodo = ?) c\n" +
                        "on a.cuenta_filial = c.cuenta_local \n" +
                        "where periodo = ? and yntp_empresa = ? \n" +
                        "group by yntp_empresa, concepto, case when tipo_registro = 'TOTAL' then a.cuenta_local else substring(a.cuenta_local, 1, "+level+") end, case when tipo_registro = 'TOTAL' then cuenta_filial else substring(cuenta_filial, 1, "+level+") end, case when tipo_registro = 'TOTAL' then justificacion else '' end, tipo_registro \n" +
                        ", b.CODICONS46, c.cod_neocon \n" +
                        "--having sum(valor_banco) <> 0 or sum(valor_filial) <> 0 \n" +
                        "order by 1, 2, 5, 3, 4 \n" +
                        ";");

                resultFinal.setParameter(1,periodo);
                resultFinal.setParameter(2,periodo);
                resultFinal.setParameter(3,yntp);
                return resultFinal.getResultList();

            }else{


                Query resultFinal = entityManager.createNativeQuery("select a.yntp_empresa, a.concepto, a.cuenta_local, a.cuenta_filial, a.contrato_banco, a.valor_banco valor_banco, " +
                        "a.valor_filial valor_filial, abs(a.valor_banco)-abs(a.valor_filial) diferencia, a.tipo_registro, a.id, a.justificacion \n" +
                        ", b.CODICONS46 cod_neocon_banco, c.cod_neocon cod_neocon_filial \n" +
                        "from nexco_intergrupo_recon_just a \n" +
                        "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                        "on a.cuenta_local = b.NUCTA\n" +
                        "left join (select distinct cod_neocon, cuenta_local, yntp_reportante from nexco_filiales_intergrupo where periodo = ?) c\n" +
                        "on a.cuenta_filial = c.cuenta_local and a.yntp_empresa = c.yntp_reportante \n" +
                        "where periodo = ? and yntp_empresa = ? and (valor_banco <> 0 or valor_filial <> 0) \n" +
                        "order by 1, 2, 5, 3, 4 \n" +
                        ";");

                resultFinal.setParameter(1,periodo);
                resultFinal.setParameter(2,periodo);
                resultFinal.setParameter(3,yntp);
                return resultFinal.getResultList();

            }
        }

    }

    public List<Subsidiaries> reconciliationIntergroupV1FilDownloadR(String periodo) {
                Query resultFinal = entityManager.createNativeQuery("select * from nexco_filiales where cuenta_filial is not null and cuenta_local is null", Subsidiaries.class);
                return resultFinal.getResultList();
            }
}
