package com.inter.proyecto_intergrupo.service.ifrs9Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class VSMasterContractService {

    @Autowired
    EntityManager entityManager;

    public void createResumeWithoutAdjust(String period){

        String validPeriod = period.replace("-","");

        Query createVertical = entityManager.createNativeQuery("\tif object_id('tempdb..##VERTICAL_BASE') is not null\n" +
                "\t   drop table ##VERTICAL_BASE\n" +
                "\t\n" +
                "\tSELECT a.cves_fec_datos\n" +
                "\t     , a.cves_cod_ent_iuc\n" +
                "\t     , a.cves_cod_ccontr\n" +
                "\t\t , a.cves_cod_ctacont\n" +
                "\t     , a.cves_ind_stage_final\n" +
                "\t     , a.cves_imp_saldo_loc  \n" +
                "\t     , a.cves_imp_prov_final \n" +
                "\t     , a.cves_imp_ead_actual \n" +
                "\tINTO   ##VERTICAL_BASE\n" +
                "\tFROM   [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_vertical_saldos_"+validPeriod+" AS a\n" +
                "\tWHERE  a.cves_cod_ctacont is not null \n" +
                "\tAND    a.cves_cod_ent_iuc = '0013' ");

        createVertical.executeUpdate();

        Query updateV = entityManager.createNativeQuery("UPDATE ##VERTICAL_BASE\n" +
                "\tSET cves_imp_prov_final = 0\n" +
                "\tWHERE cves_imp_prov_final like '999999999999999.9%' ");

        updateV.executeUpdate();

        Query verticalPuc = entityManager.createNativeQuery("\tif object_id('tempdb..##VERTICAL') is not null\n" +
                "\t   drop table ##VERTICAL\n" +
                "\t\n" +
                "\tSELECT a.cves_fec_datos as 'fecha'\n" +
                "\t     , min(prov.instrumento) instrumento\n" +
                "\t     , a.cves_cod_ent_iuc as 'v empresa'\n" +
                "\t     , a.cves_cod_ccontr as 'contrato'\n" +
                "\t     , a.cves_ind_stage_final as 'v stage'\n" +
                "\t     , SUM(a.cves_imp_saldo_loc) as 'v saldo local'\n" +
                "\t     , MAX(a.cves_imp_prov_final) as 'v importe provision'\n" +
                "\t     , MAX(a.cves_imp_ead_actual) as 'v importe EAD'\n" +
                "\tINTO   ##VERTICAL\n" +
                "\tFROM   ##VERTICAL_BASE AS a\n" +
                "\t       LEFT JOIN (   SELECT NUCTA\n" +
                "\t                          , CODICONS46\n" +
                "\t                          , DECTA \n" +
                "\t                     FROM   CUENTAS_PUC \n" +
                "\t                     WHERE  empresa = '0013'\n" +
                "\t                 ) AS puc ON puc.NUCTA = CONVERT(VARCHAR,a.cves_cod_ctacont) COLLATE Modern_Spanish_CI_AS\n" +
                "\t       LEFT JOIN (   SELECT cuenta_neocon\n" +
                "\t                          , instrumento \n" +
                "\t                     FROM nexco_provisiones\n" +
                "\t                 ) AS prov ON prov.cuenta_neocon = puc.CODICONS46\n" +
                "\tGROUP BY a.cves_fec_datos\n" +
                "\t       , a.cves_cod_ent_iuc\n" +
                "\t       , a.cves_cod_ccontr\n" +
                "\t       , a.cves_ind_stage_final");

        verticalPuc.executeUpdate();

        Query createMaster = entityManager.createNativeQuery("if object_id('tempdb..##MAESTRO') is not null\n" +
                "\t   drop table ##MAESTRO\n" +
                "\t\n" +
                "\tselect a.cmco_cod_ccontr as 'contrato'\n" +
                "\t     , a.cmco_cod_ent_iuc\n" +
                "\t     , a.cmco_ind_stage_final as 'm stage'\n" +
                "\t     , SUM(a.cmco_imp_saldisac_con                \n" +
                "\t         + a.cmco_imp_invirreg_con                \n" +
                "\t         + a.cmco_imp_exced_con    \n" +
                "\t         + a.cmco_imp_morre_con                \n" +
                "\t         + a.cmco_imp_morsb_con                \n" +
                "\t         + a.cmco_imp_morar_con                \n" +
                "\t         + a.cmco_imp_mosus_con                \n" +
                "\t         + a.cmco_imp_sdfuba_con                \n" +
                "\t         + a.cmco_imp_dbledu_con                \n" +
                "\t         + a.cmco_imp_ressald_con                \n" +
                "\t         + a.cmco_imp_mtm_con                \n" +
                "\t         + a.cmco_imp_grs                \n" +
                "\t         + a.cmco_imp_mst                \n" +
                "\t         + a.cmco_imp_cre                \n" +
                "\t         + a.cmco_imp_crg                \n" +
                "\t         + a.cmco_imp_psu) as 'm saldo local'\n" +
                "\t     , MAX(a.cmco_imp_prov_final) as 'm importe provision'\n" +
                "\t     , MAX(a.cmco_imp_ead_actual) as 'm importe ead'                \n" +
                "\tINTO   ##MAESTRO\n" +
                "\tfrom   [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_maestro_contratos_"+validPeriod+" as a\n" +
                "\tWHERE  a.cmco_cod_ent_iuc = '0013'\n" +
                "\tGROUP BY a.cmco_cod_ccontr\n" +
                "\t       , a.cmco_ind_stage_final\n" +
                "\t       , a.cmco_cod_ent_iuc");

        createMaster.executeUpdate();

        Query updateM = entityManager.createNativeQuery("UPDATE ##MAESTRO\n" +
                "\tSET [m importe provision] = 0\n" +
                "\tWHERE [m importe provision] like '999999999999999.9%' ");

        updateM.executeUpdate();
    }

    public void createResumeAdjust(String period){

        String validPeriod = period.replace("-","");

        Query createVertical = entityManager.createNativeQuery("\tif object_id('tempdb..##VERTICAL_BASE') is not null\n" +
                "\t   drop table ##VERTICAL_BASE\n" +
                "\t\n" +
                "\tSELECT a.cves_fec_datos\n" +
                "\t     , a.cves_cod_ent_iuc\n" +
                "\t     , a.cves_cod_ccontr\n" +
                "\t\t , a.cves_cod_ctacont\n" +
                "\t     , a.cves_ind_stage_final\n" +
                "\t     , a.cves_imp_saldo_loc  \n" +
                "\t     , a.cves_imp_prov_final \n" +
                "\t     , a.cves_imp_ead_actual \n" +
                "\tINTO   ##VERTICAL_BASE\n" +
                "\tFROM   [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_vertical_saldos_ajustado_"+validPeriod+" AS a\n" +
                "\tWHERE  a.cves_cod_ctacont is not null \n" +
                "\tAND    a.cves_cod_ent_iuc = '0013' ");

        createVertical.executeUpdate();

        Query updateV = entityManager.createNativeQuery("UPDATE ##VERTICAL_BASE\n" +
                "\tSET cves_imp_prov_final = 0\n" +
                "\tWHERE cves_imp_prov_final like '999999999999999.9%' ");

        updateV.executeUpdate();

        Query verticalPuc = entityManager.createNativeQuery("\tif object_id('tempdb..##VERTICAL') is not null\n" +
                "\t   drop table ##VERTICAL\n" +
                "\t\n" +
                "\tSELECT a.cves_fec_datos as 'fecha'\n" +
                "\t     , min(prov.instrumento) instrumento\n" +
                "\t     , a.cves_cod_ent_iuc as 'v empresa'\n" +
                "\t     , a.cves_cod_ccontr as 'contrato'\n" +
                "\t     , a.cves_ind_stage_final as 'v stage'\n" +
                "\t     , SUM(a.cves_imp_saldo_loc) as 'v saldo local'\n" +
                "\t     , MAX(a.cves_imp_prov_final) as 'v importe provision'\n" +
                "\t     , MAX(a.cves_imp_ead_actual) as 'v importe EAD'\n" +
                "\tINTO   ##VERTICAL\n" +
                "\tFROM   ##VERTICAL_BASE AS a\n" +
                "\t       LEFT JOIN (   SELECT NUCTA\n" +
                "\t                          , CODICONS46\n" +
                "\t                          , DECTA \n" +
                "\t                     FROM   CUENTAS_PUC \n" +
                "\t                     WHERE  empresa = '0013'\n" +
                "\t                 ) AS puc ON puc.NUCTA = CONVERT(VARCHAR,a.cves_cod_ctacont) COLLATE Modern_Spanish_CI_AS\n" +
                "\t       LEFT JOIN (   SELECT cuenta_neocon\n" +
                "\t                          , instrumento \n" +
                "\t                     FROM nexco_provisiones\n" +
                "\t                 ) AS prov ON prov.cuenta_neocon = puc.CODICONS46\n" +
                "\tGROUP BY a.cves_fec_datos\n" +
                "\t       , a.cves_cod_ent_iuc\n" +
                "\t       , a.cves_cod_ccontr\n" +
                "\t       , a.cves_ind_stage_final");

        verticalPuc.executeUpdate();

        Query createMaster = entityManager.createNativeQuery("if object_id('tempdb..##MAESTRO') is not null\n" +
                "\t   drop table ##MAESTRO\n" +
                "\t\n" +
                "\tselect a.cmco_cod_ccontr as 'contrato'\n" +
                "\t     , a.cmco_cod_ent_iuc\n" +
                "\t     , a.cmco_ind_stage_final as 'm stage'\n" +
                "\t     , SUM(a.cmco_imp_saldisac_con                \n" +
                "\t         + a.cmco_imp_invirreg_con                \n" +
                "\t         + a.cmco_imp_exced_con    \n" +
                "\t         + a.cmco_imp_morre_con                \n" +
                "\t         + a.cmco_imp_morsb_con                \n" +
                "\t         + a.cmco_imp_morar_con                \n" +
                "\t         + a.cmco_imp_mosus_con                \n" +
                "\t         + a.cmco_imp_sdfuba_con                \n" +
                "\t         + a.cmco_imp_dbledu_con                \n" +
                "\t         + a.cmco_imp_ressald_con                \n" +
                "\t         + a.cmco_imp_mtm_con                \n" +
                "\t         + a.cmco_imp_grs                \n" +
                "\t         + a.cmco_imp_mst                \n" +
                "\t         + a.cmco_imp_cre                \n" +
                "\t         + a.cmco_imp_crg                \n" +
                "\t         + a.cmco_imp_psu) as 'm saldo local'\n" +
                "\t     , MAX(a.cmco_imp_prov_final) as 'm importe provision'\n" +
                "\t     , MAX(a.cmco_imp_ead_actual) as 'm importe ead'                \n" +
                "\tINTO   ##MAESTRO\n" +
                "\tfrom   [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_maestro_contratos_ajustado_"+validPeriod+" as a\n" +
                "\tWHERE  a.cmco_cod_ent_iuc = '0013'\n" +
                "\tGROUP BY a.cmco_cod_ccontr\n" +
                "\t       , a.cmco_ind_stage_final\n" +
                "\t       , a.cmco_cod_ent_iuc");

        createMaster.executeUpdate();

        Query updateM = entityManager.createNativeQuery("UPDATE ##MAESTRO\n" +
                "\tSET [m importe provision] = 0\n" +
                "\tWHERE [m importe provision] like '999999999999999.9%' ");

        updateM.executeUpdate();
    }


    public List<Object[]> getResume(){

        Query getResume = entityManager.createNativeQuery("\tSELECT vertical.fecha\n" +
                "\t     , vertical.[v empresa]\n" +
                "\t     , vertical.instrumento\n" +
                "\t     , vertical.[v stage]\n" +
                "\t     , sum(vertical.[v saldo local]) [v saldo local]\n" +
                "\t     , sum(vertical.[v importe provision]) [v importe provision]\n" +
                "\t     , sum(vertical.[v importe EAD]) [v importe EAD]\n" +
                "\t     , sum(maestro.[m saldo local]) [m saldo local]\n" +
                "\t     , sum(maestro.[m importe provision]) [m importe provision]\n" +
                "\t     , sum(maestro.[m importe ead]) [m importe ead]\n" +
                "\t     , (sum(maestro.[m saldo local])       - sum(vertical.[v saldo local])) as 'Dif. Saldo Local'\n" +
                "\t     , (sum(maestro.[m importe provision]) - sum(vertical.[v importe provision])) as 'Dif. Importe Prov'\n" +
                "\t     , (sum(maestro.[m importe ead])       - sum(vertical.[v importe ead])) as 'Dif. Importe EAD'\n" +
                "\tFROM   ##VERTICAL vertical\n" +
                "\t       LEFT JOIN ##MAESTRO maestro ON vertical.contrato = maestro.contrato\n" +
                "\tgroup by vertical.fecha\n" +
                "\t       , vertical.[v empresa]\n" +
                "\t       , vertical.instrumento\n" +
                "\t       , vertical.[v stage]\n" +
                "\tORDER BY instrumento\n" +
                "\t       , vertical.[v stage]");

        return getResume.getResultList();
    }

    public List<Object[]> getComplete(){

        Query getData = entityManager.createNativeQuery("SELECT vertical.fecha\n" +
                "\t     , vertical.[v empresa]\n" +
                "\t     , vertical.instrumento\n" +
                "\t\t , vertical.contrato\n" +
                "\t     , vertical.[v stage]\n" +
                "\t     , sum(vertical.[v saldo local]) [v saldo local]\n" +
                "\t     , sum(vertical.[v importe provision]) [v importe provision]\n" +
                "\t     , sum(vertical.[v importe EAD]) [v importe EAD]\n" +
                "\t     , sum(maestro.[m saldo local]) [m saldo local]\n" +
                "\t     , sum(maestro.[m importe provision]) [m importe provision]\n" +
                "\t     , sum(maestro.[m importe ead]) [m importe ead]\n" +
                "\t     , (sum(maestro.[m saldo local])       - sum(vertical.[v saldo local])) as 'Dif. Saldo Local'\n" +
                "\t     , (sum(maestro.[m importe provision]) - sum(vertical.[v importe provision])) as 'Dif. Importe Prov'\n" +
                "\t     , (sum(maestro.[m importe ead])       - sum(vertical.[v importe ead])) as 'Dif. Importe EAD'\n" +
                "\tFROM   ##VERTICAL vertical\n" +
                "\t       LEFT JOIN ##MAESTRO maestro ON vertical.contrato = maestro.contrato\n" +
                "\tgroup by vertical.fecha\n" +
                "\t\t   , vertical.contrato\n" +
                "\t       , vertical.[v empresa]\n" +
                "\t       , vertical.instrumento\n" +
                "\t       , vertical.[v stage]\n" +
                "\tORDER BY instrumento\n" +
                "\t       , vertical.[v stage]");

        return getData.getResultList();
    }

/*    public List<Object[]> getResumeAdjust(String period){

        String validPeriod = period.replace("-","");

        Query getData = entityManager.createNativeQuery("SELECT  \n" +
                "vertical.fecha,\n" +
                "vertical.[v empresa],\n" +
                "'' as instrumento,\n " +
                "--vertical.[v contrato],\n" +
                "vertical.[v stage],\n" +
                "vertical.[v saldo local],\n" +
                "vertical.[v importe provision],\n" +
                "vertical.[v importe EAD],\n" +
                "maestro.[m saldo local],\n" +
                "maestro.[m importe provision],\n" +
                "maestro.[m importe ead],\n" +
                "(maestro.[m saldo local] - vertical.[v saldo local]) as 'Dif. Saldo Local',\n" +
                "(maestro.[m importe provision] - vertical.[v importe provision]) as 'Dif. Importe Prov',\n" +
                "(maestro.[m importe ead] - vertical.[v importe ead]) as 'Dif. Importe EAD' \n" +
                "FROM\n" +
                "(SELECT a.cves_fec_datos as 'fecha',\n" +
                "a.cves_cod_ent_iuc as 'v empresa',\n" +
                "a.cves_ind_stage_final as 'v stage',\n" +
                "--a.cves_cod_ccontr as 'v contrato',\n" +
                "SUM(a.cves_imp_saldo_loc) as 'v saldo local',\n" +
                "MAX(a.cves_imp_prov_final) as 'v importe provision',\n" +
                "MAX(a.cves_imp_ead_actual) as 'v importe EAD'\n" +
                "FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_vertical_saldos_ajustado_"+validPeriod+" AS a\n" +
                "WHERE a.cves_cod_ctacont is not null AND a.cves_cod_ent_iuc = '0013'\n" +
                "GROUP BY \n" +
                "a.cves_fec_datos,\n" +
                "a.cves_cod_ent_iuc,\n" +
                "a.cves_ind_stage_final) vertical\n" +
                "LEFT JOIN\n" +
                "(select\t--a.cmco_cod_ccontr as 'm contrato',\n" +
                "a.cmco_ind_stage_final as 'm stage',\n" +
                "SUM(a.cmco_imp_saldisac_con\t\t\t\t\n " +
                "+ a.cmco_imp_invirreg_con\t\t\t\t\n" +
                "+ a.cmco_imp_exced_con\t\t\t\t\n" +
                "+ a.cmco_imp_morre_con\t\t\t\t\n" +
                "+ a.cmco_imp_morsb_con\t\t\t\t\n" +
                "+ a.cmco_imp_morar_con\t\t\t\t\n" +
                "+ a.cmco_imp_mosus_con\t\t\t\t\n" +
                "+ a.cmco_imp_sdfuba_con\t\t\t\t\n" +
                "+ a.cmco_imp_dbledu_con\t\t\t\t\n" +
                "+ a.cmco_imp_ressald_con\t\t\t\t\n" +
                "+ a.cmco_imp_mtm_con\t\t\t\t\n" +
                "+ a.cmco_imp_grs\t\t\t\t\n" +
                "+ a.cmco_imp_mst\t\t\t\t\n" +
                "+ a.cmco_imp_cre\t\t\t\t\n" +
                "+ a.cmco_imp_crg\t\t\t\t\n" +
                "+ a.cmco_imp_psu) as 'm saldo local',\t\t\t\n" +
                "MAX(a.cmco_imp_prov_final) as 'm importe provision',\t\t\t\t\n" +
                "MAX(a.cmco_imp_ead_actual) as 'm importe ead'\t\t\t\t\n" +
                "from [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_maestro_contratos_ajustado_"+validPeriod+" as a \n" +
                "WHERE a.cmco_cod_ent_iuc = '0013'\n "+
                "GROUP BY\n" +
                "a.cmco_ind_stage_final) maestro\n" +
                "ON vertical.[v stage] = maestro.[m stage]\n"+
                "ORDER BY vertical.[v stage]");

        return getData.getResultList();
    }

    public List<Object[]> getCompleteWithoutAdjust(String period){
        String validPeriod = period.replace("-","");

        Query getData = entityManager.createNativeQuery("SELECT  \n" +
                "vertical.fecha,\n" +
                "vertical.[v empresa],\n" +
                "'' as instrumento,\n" +
                "vertical.[v contrato],\n" +
                "vertical.[v stage],\n" +
                "vertical.[v saldo local],\n" +
                "vertical.[v importe provision],\n" +
                "vertical.[v importe EAD],\n" +
                "maestro.[m saldo local],\n" +
                "maestro.[m importe provision],\n" +
                "maestro.[m importe ead],\n" +
                "(maestro.[m saldo local] - vertical.[v saldo local]) as 'Dif. Saldo Local',\n" +
                "(maestro.[m importe provision] - vertical.[v importe provision]) as 'Dif. Importe Prov',\n" +
                "(maestro.[m importe ead] - vertical.[v importe ead]) as 'Dif. Importe EAD'\n" +
                "FROM\n" +
                "(SELECT a.cves_fec_datos as 'fecha',\n" +
                "a.cves_cod_ent_iuc as 'v empresa',\n" +
                "a.cves_ind_stage_final as 'v stage',\n" +
                "a.cves_cod_ccontr as 'v contrato',\n" +
                "SUM(a.cves_imp_saldo_loc) as 'v saldo local',\n" +
                "MAX(a.cves_imp_prov_final) as 'v importe provision',\n" +
                "MAX(a.cves_imp_ead_actual) as 'v importe EAD'\n" +
                "FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_vertical_saldos_"+validPeriod+" AS a\n" +
                "WHERE a.cves_cod_ctacont is not null AND a.cves_cod_ent_iuc = '0013' \n" +
                "GROUP BY \n" +
                "a.cves_fec_datos,\n" +
                "a.cves_cod_ent_iuc,\n" +
                "a.cves_ind_stage_final,\n" +
                "a.cves_cod_ccontr) vertical\n" +
                "LEFT JOIN\n" +
                "(select\ta.cmco_cod_ccontr as 'm contrato',\t \n" +
                "(a.cmco_imp_saldisac_con\t\t\t\t\n" +
                "+ a.cmco_imp_invirreg_con\t\t\t\t\n" +
                "+ a.cmco_imp_exced_con\t\t\t\t\n" +
                "+ a.cmco_imp_morre_con\t\t\t\t\n" +
                "+ a.cmco_imp_morsb_con\t\t\t\t\n" +
                "+ a.cmco_imp_morar_con\t\t\t\t\n" +
                "+ a.cmco_imp_mosus_con\t\t\t\t\n" +
                "+ a.cmco_imp_sdfuba_con\t\t\t\t\n" +
                "+ a.cmco_imp_dbledu_con\t\t\t\t\n" +
                "+ a.cmco_imp_ressald_con\t\t\t\t\n" +
                "+ a.cmco_imp_mtm_con\t\t\t\t\n" +
                "+ a.cmco_imp_grs\t\t\t\t\n" +
                "+ a.cmco_imp_mst\t\t\t\t\n" +
                "+ a.cmco_imp_cre\t\t\t\t\n" +
                "+ a.cmco_imp_crg\t\t\t\t\n" +
                "+ a.cmco_imp_psu) as 'm saldo local',\t\t\t\n" +
                "a.cmco_imp_prov_final as 'm importe provision',\t\t\t\t\n" +
                "a.cmco_imp_ead_actual as 'm importe ead'\t\t\t\t\n" +
                "from [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_maestro_contratos_"+validPeriod+" as a \n" +
                "WHERE a.cmco_cod_ent_iuc = '0013') maestro\n" +
                "ON vertical.[v contrato] = maestro.[m contrato]\n" +
                "ORDER BY vertical.[v stage]");

        return getData.getResultList();
    }

    public List<Object[]> getCompleteAdjust(String period){
        String validPeriod = period.replace("-","");

        Query getData = entityManager.createNativeQuery("SELECT  \n" +
                "vertical.fecha,\n" +
                "vertical.[v empresa],\n" +
                "'' as instrumento,\n" +
                "vertical.[v contrato],\n" +
                "vertical.[v stage],\n" +
                "vertical.[v saldo local],\n" +
                "vertical.[v importe provision],\n" +
                "vertical.[v importe EAD],\n" +
                "maestro.[m saldo local],\n" +
                "maestro.[m importe provision],\n" +
                "maestro.[m importe ead],\n" +
                "(maestro.[m saldo local] - vertical.[v saldo local]) as 'Dif. Saldo Local',\n" +
                "(maestro.[m importe provision] - vertical.[v importe provision]) as 'Dif. Importe Prov',\n" +
                "(maestro.[m importe ead] - vertical.[v importe ead]) as 'Dif. Importe EAD'\n" +
                "FROM\n" +
                "(SELECT a.cves_fec_datos as 'fecha',\n" +
                "a.cves_cod_ent_iuc as 'v empresa',\n" +
                "a.cves_ind_stage_final as 'v stage',\n" +
                "a.cves_cod_ccontr as 'v contrato',\n" +
                "SUM(a.cves_imp_saldo_loc) as 'v saldo local',\n" +
                "MAX(a.cves_imp_prov_final) as 'v importe provision',\n" +
                "MAX(a.cves_imp_ead_actual) as 'v importe EAD'\n" +
                "FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_vertical_saldos_ajustado_"+validPeriod+" AS a\n" +
                "WHERE a.cves_cod_ctacont is not null AND a.cves_cod_ent_iuc = '0013'\n" +
                "GROUP BY \n" +
                "a.cves_fec_datos,\n" +
                "a.cves_cod_ent_iuc,\n" +
                "a.cves_ind_stage_final,\n" +
                "a.cves_cod_ccontr) vertical\n" +
                "LEFT JOIN\n" +
                "(select\ta.cmco_cod_ccontr as 'm contrato',\t \n" +
                "(a.cmco_imp_saldisac_con\t\t\t\t\n" +
                "+ a.cmco_imp_invirreg_con\t\t\t\t\n" +
                "+ a.cmco_imp_exced_con\t\t\t\t\n" +
                "+ a.cmco_imp_morre_con\t\t\t\t\n" +
                "+ a.cmco_imp_morsb_con\t\t\t\t\n" +
                "+ a.cmco_imp_morar_con\t\t\t\t\n" +
                "+ a.cmco_imp_mosus_con\t\t\t\t\n" +
                "+ a.cmco_imp_sdfuba_con\t\t\t\t\n" +
                "+ a.cmco_imp_dbledu_con\t\t\t\t\n" +
                "+ a.cmco_imp_ressald_con\t\t\t\t\n" +
                "+ a.cmco_imp_mtm_con\t\t\t\t\n" +
                "+ a.cmco_imp_grs\t\t\t\t\n" +
                "+ a.cmco_imp_mst\t\t\t\t\n" +
                "+ a.cmco_imp_cre\t\t\t\t\n" +
                "+ a.cmco_imp_crg\t\t\t\t\n" +
                "+ a.cmco_imp_psu) as 'm saldo local',\t\t\t\n" +
                "a.cmco_imp_prov_final as 'm importe provision',\t\t\t\t\n" +
                "a.cmco_imp_ead_actual as 'm importe ead'\t\t\t\t\n" +
                "from [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_maestro_contratos_ajustado_"+validPeriod+" as a \n"+
                "WHERE a.cmco_cod_ent_iuc = '0013') maestro\n"+
                "ON vertical.[v contrato] = maestro.[m contrato]\n" +
                "ORDER BY vertical.[v stage]");

        return getData.getResultList();
    }*/





}
