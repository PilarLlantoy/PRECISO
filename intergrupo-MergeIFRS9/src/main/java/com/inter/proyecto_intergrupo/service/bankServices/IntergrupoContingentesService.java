package com.inter.proyecto_intergrupo.service.bankServices;

import com.inter.proyecto_intergrupo.model.reports.Corep;
import com.inter.proyecto_intergrupo.model.temporal.CorepTeporalH;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class IntergrupoContingentesService {

    @PersistenceContext
    EntityManager entityManager;

    public void sqlinsert(String periodo)
    {
        try {
            Query query = entityManager.createNativeQuery("INSERT INTO nexco_corep_obligatorios " +
                    " SELECT am.cuenta_contable		AS CTA_CONTA" +
                    "     ,CONCAT(am.cuenta_contable,'_',am.divisa) AS CTA_CONTA_MONEDA" +
                    "	  ,am.divisa				AS MONEDA" +
                    "	  ,CAST(REPLACE(am.saldo_pesos,',','.') AS FLOAT)/CAST(gm.valor AS FLOAT) AS SALDODIVISA" +
                    "	  ,am.saldo_pesos			AS SALDOPESOS" +
                    "	  ,CAST(am.fecha_alta as varchar)			AS FEC_ALTA" +
                    "	  ,CAST(am.fecha_vencimiento as varchar)		AS FEC_VENCI" +
                    "	  ,CAST(am.fecha_vencimiento as varchar)		AS FEC_CIERRE" +
                    "	  ,am.nit					AS NIT" +
                    "	  ,am.CONTRATO				AS CONTRATO" +
                    "	  ,''						AS NOMBRE_CLIENTE" +
                    "	  ,am.nombre_banco			AS NOMBRE_BANCO" +
                    "	  ,am.pais_banco			AS PAIS_BANCO" +
                    "	  ,am.intergrupo			AS INTERGRUPO" +
                    "	  ,hm.CODIGO_LIQUIDEZ 		AS TCLIENTE" +
                    "	  ,''						AS Vencimiento_Rel" +
                    "	  ,''						AS Vencimiento_Residul" +
                    "	  ,''						AS Vto_Rel" +
                    "	  ,''						AS Vto_Residul" +
                    "	  ,''						AS CCF" +
                    "	  ,''						AS RW" +
                    "	  ,dm.CODICONS46			AS CODCONSOLc" +
                    "	  ,bm.tipo_aval				AS Tipo_Avl" +
                    "	  ,dm1.CODICONS46			AS NUEVO_CODIGO" +
                    "	  ,CAST(am.saldo_pesos as varchar)	AS Provision" +
                    "	  ,am.cta_contable_60		AS CUENTAPROVISION" +
                    "	  ,''						AS ISO" +
                    "	  ,''						AS ISO_GARANTIA" +
                    "  FROM  nexco_reclasificacion_contingentes as am LEFT JOIN" +
                    "        nexco_contratos			 as bm" +
                    "	ON (am.CONTRATO = bm.id_contrato)	LEFT JOIN" +
                    "		nexco_paises			 as cm" +
                    "	ON (bm.id_pais = cm.id_pais)		LEFT JOIN" +
                    "		CUENTAS_PUC				 as dm" +
                    "	ON (am.cuenta_contable = dm.NUCTA)	LEFT JOIN" +
                    "		CUENTAS_PUC				 as dm1" +
                    "	ON (am.cta_contable_60 = dm.NUCTA)	LEFT JOIN" +
                    "		nexco_terceros			 as em" +
                    "   ON (am.nit = em.nit_contraparte) LEFT JOIN" +
                    "       nexco_divisas_valor as gm" +
                    "   ON (am.DIVISA = gm.DIVISA COLLATE SQL_Latin1_General_CP1_CI_AS ) LEFT JOIN" +
                    "       [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_FINREP_CALCULADA_"+periodo+" as hm"  +
                    "   ON (hm.IDENTIFICACION COLLATE SQL_Latin1_General_CP1_CI_AS = am.nit)");
            query.executeUpdate();
        } catch (NoResultException e)
        {
            e.printStackTrace();
        }
    }

    public List<Object[]> getTemporalCorepSecond(String periodo)
    {
        Query queryData = entityManager.createNativeQuery("SELECT EMPRESA, FECHA, CENTRO, NUCTA, DATO1, FECHA_PROC,CONTRATO,APLICATIVO,RISTRA,SUM(CAST(TRIM(REPLACE(REPLACE(VALOR,'+',''),',','')) AS NUMERIC(18,2))) FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.CONT_H141MES_"+periodo.replace("-","_")+" WHERE EMPRESA = '0064' AND SUBSTRING(NUCTA,1,4) IN ('6295') AND LEN(NUCTA)>=15 AND CONTRATO COLLATE SQL_Latin1_General_CP1_CI_AS NOT IN (select contrato from nexco_corep_obligatorios group by contrato) GROUP BY EMPRESA, FECHA, CENTRO, NUCTA, DATO1, FECHA_PROC,CONTRATO,APLICATIVO,RISTRA");
        return queryData.getResultList();
    }

    public List<Object[]> getTemporalCorep(String periodo)
    {
        List<Object[]> listFinal = new ArrayList<>();

        Query queryData1 = entityManager.createNativeQuery("SELECT A.EMPRESA , A.FECHA, A.CENTRO, A.NUCTA, A.DATO1, A.FECHA_PROC, A.CONTRATO,A.APLICATIVO, A.RISTRA, SUM(CAST(TRIM(REPLACE(REPLACE( A.VALOR,'+',''),',','')) AS NUMERIC(18,2))) FROM nexco_corep_obligatorios B\n" +
                "INNER JOIN ( SELECT * FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.CONT_H141MES_"+periodo.replace("-","_")+" WHERE EMPRESA = '0064' ) A ON B.contrato COLLATE Modern_Spanish_CI_AS = A.CONTRATO AND B.cuenta COLLATE Modern_Spanish_CI_AS = A.NUCTA\n" +
                "GROUP BY A.EMPRESA, A.FECHA, A.CENTRO, A.NUCTA, A.DATO1, A.FECHA_PROC, A.CONTRATO, A.APLICATIVO, A.RISTRA");
        listFinal.addAll(queryData1.getResultList());

        Query queryData = entityManager.createNativeQuery("SELECT A.EMPRESA , A.FECHA, A.CENTRO, A.NUCTA, A.DATO1, A.FECHA_PROC, A.CONTRATO,A.APLICATIVO, A.RISTRA, SUM(CAST(TRIM(REPLACE(REPLACE( A.VALOR,'+',''),',','')) AS NUMERIC(18,2))) FROM nexco_corep_obligatorios B\n" +
                "INNER JOIN ( SELECT * FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.CONT_H141MES_"+periodo.replace("-","_")+" WHERE EMPRESA = '0064' ) A ON B.contrato COLLATE Modern_Spanish_CI_AS = A.CONTRATO AND B.cuentaprovision COLLATE Modern_Spanish_CI_AS = A.NUCTA\n" +
                "GROUP BY A.EMPRESA, A.FECHA, A.CENTRO, A.NUCTA, A.DATO1, A.FECHA_PROC, A.CONTRATO, A.APLICATIVO, A.RISTRA");
        listFinal.addAll(queryData.getResultList());

        return listFinal;
    }

    public List<Corep> sqlinsertCorep(String periodo, String omitir, String incluirR, String incluirP,String lTcliente)
    {

        List<Corep> lista=new ArrayList<Corep>();
        List<String> listI=new ArrayList<String>();
        List<String> listIP=new ArrayList<String>();
        List<String> listIR=new ArrayList<String>();
        List<String> listO=new ArrayList<String>();
        List<String> listCl=new ArrayList<String>();
        if(omitir.trim().length()>0)
        {
            if(!omitir.contains(","))
            {
                omitir=omitir.replace(" ","");
                listO.add(omitir);
            }
            else
            {
                try{
                    omitir=omitir.replace(" ","");
                    listO = Arrays.asList(omitir.split(","));
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        if(incluirP.trim().length()>0)
        {
            if(!incluirP.contains(","))
            {
                incluirP=incluirP.replace(" ","");
                listI.add(incluirP);
                listIP.add(incluirP);
            }
            else
            {
                try{
                    incluirP=incluirP.replace(" ","");
                    listI = Arrays.asList(incluirP.split(","));
                    listIP = Arrays.asList(incluirP.split(","));
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        if(incluirR.trim().length()>0)
        {
            if(!incluirR.contains(","))
            {
                incluirR=incluirR.replace(" ","");
                listI.add(incluirR);
                listIR.add(incluirR);
            }
            else
            {
                try{
                    incluirR=incluirR.replace(" ","");
                    listI.addAll(Arrays.asList(incluirR.split(",")));
                    listIR.addAll(Arrays.asList(incluirR.split(",")));
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        if(lTcliente.trim().length()>0)
        {
            if(!lTcliente.contains(","))
            {
                lTcliente=lTcliente.replace(" ","");
                listCl.add(String.format(lTcliente));
            }
            else
            {
                try{
                    lTcliente=lTcliente.replace(" ","");
                    listCl = Arrays.asList(lTcliente.split(","));
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        Query queryDelete2 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_corep_temporal");
        queryDelete2.executeUpdate();
        Query queryData = entityManager.createNativeQuery("INSERT INTO nexco_corep_temporal ([contrato],[nucta],[valor])\n" +
                    "SELECT CONTRATO, NUCTA,SUM(CAST(TRIM(REPLACE(REPLACE(VALOR,'+',''),',','')) AS NUMERIC(18,2))) AS VALOR FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.CONT_H141MES_"+periodo.replace("-","_")+" WHERE EMPRESA = '0064' AND SUBSTRING(NUCTA,1,4) IN :listI AND SUBSTRING(NUCTA,1,1)NOT IN :listO AND LEN(NUCTA)>=15 GROUP BY CONTRATO, NUCTA ");
        queryData.setParameter("listO",listO);
        queryData.setParameter("listI",listI);
        queryData.executeUpdate();
        Query queryDelete = entityManager.createNativeQuery("TRUNCATE TABLE nexco_corep_obligatorios");
        queryDelete.executeUpdate();
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_corep_obligatorios ([cuenta],[cta_conta_moneda],[divisa],[saldodivisa],[saldopesos],[fec_alta],[fec_venci],[fec_cierre],[nit],[contrato],[nombre_cliente]\n" +
                ",[nombre_banco],[cod_pais],[pais_banco],[intergrupo],[tcliente],[vencimiento_rel],[vencimiento_residul],[vto_rel],[vto_residul],[ccf],[rw],[codconsol],[tipo_avl],[nuevo_codigo],[provision]\n" +
                ",[cuentaprovision],[iso],[iso_garantia],[segmento_finrep]) \n" +
                "SELECT am.cta_contable_60 AS CTA_CONTA \n" +
                ",CASE WHEN hs.NUCTA IS NOT NULL THEN CONCAT(hs.NUCTA COLLATE SQL_Latin1_General_CP1_CI_AS,'_',am.divisa) END AS CTA_CONTA_MONEDA \n" +
                ",am.divisa AS MONEDA \n" +
                ",ABS(am.saldo_divisa) AS SALDODIVISA \n" +
                ",ABS(am.saldo_pesos) AS SALDOPESOS \n" +
                ",am.fecha_alta  AS FEC_ALTA \n" +
                ",am.fecha_vencimiento  AS FEC_VENCI \n" +
                ",am.fecha_cierre  AS FEC_CIERRE \n" +
                ",am.nit AS NIT \n" +
                ",am.CONTRATO AS CONTRATO \n" +
                ",am.nombre_cliente AS NOMBRE_CLIENTE \n" +
                ",CASE WHEN am.nombre_banco IS NOT NULL THEN am.nombre_banco END AS NOMBRE_BANCO \n" +
                ",CASE WHEN cm.id_pais IS NULL THEN 'CO' ELSE cm.id_pais END AS COD_PAIS \n" +
                ",CASE WHEN cm.nombre_pais IS NULL THEN 'COLOMBIA' ELSE cm.nombre_pais END AS PAIS_BANCO \n" +
                ",am.intergrupo AS INTERGRUPO \n" +
                ",''/*CASE WHEN hm.CODIGO_LIQUIDEZ IN :listCl THEN 'INSTITUCIÓN' \n" +
                "ELSE 'EMPRESA' END*/ AS TCLIENTE \n" +
                ",'' AS Vencimiento_Rel \n" +
                ",'' AS Vencimiento_Residul \n" +
                ",'' AS Vto_Rel \n" +
                ",'' AS Vto_Residul \n" +
                ",'' AS CCF \n" +
                ",'' AS RW \n" +
                ",dm1.CODICONS46 AS CODCONSOLC\n" +
                ",CONCAT('AVAL ',nta.aval_origen) AS Tipo_Avl \n" +
                ",dm2.CODICONS46 AS NUEVO_CODIGO\n" +
                ",ABS(hsp.VALOR) AS Provision \n" +
                ",hsp.NUCTA AS CUENTAPROVISION \n" +
                ",CASE WHEN cm.id_pais IS NULL THEN 'CO' ELSE cm.id_pais END AS ISO \n" +
                ",CASE WHEN cm.id_pais IS NULL THEN 'CO' ELSE cm.id_pais END AS ISO_GARANTIA \n" +
                ",''/*hm.CODIGO_LIQUIDEZ*/ AS SEGMENTO_FINREP \n" +
                "FROM  (SELECT * FROM  nexco_reclasificacion_contingentes_com where SUBSTRING(cuenta_contable,1,4) IN :listI AND SUBSTRING(cuenta_contable,1,1)NOT IN :listO)  am \n" +
                "INNER JOIN nexco_contratos as bm ON (am.CONTRATO = bm.id_contrato) \n" +
                "LEFT JOIN (SELECT * FROM nexco_corep_temporal WHERE SUBSTRING(NUCTA,1,4) IN :listIR ) as hs \n" +
                "ON (bm.id_contrato = hs.CONTRATO COLLATE SQL_Latin1_General_CP1_CI_AS) \n" +
                "LEFT JOIN (SELECT * FROM nexco_corep_temporal WHERE SUBSTRING(NUCTA,1,4) IN :listIP ) as hsp \n" +
                "ON (bm.id_contrato = hsp.CONTRATO COLLATE SQL_Latin1_General_CP1_CI_AS) \n" +
                "LEFT JOIN nexco_paises  as cm ON (am.pais_banco = cm.id_pais) \n" +
                "LEFT JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0060') as dm1 ON (am.cta_contable_60 = dm1.NUCTA) \n" +
                "LEFT JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0060') as dm2 ON (hs.NUCTA COLLATE SQL_Latin1_General_CP1_CI_AS = dm2.NUCTA) \n" +
                "LEFT JOIN nexco_terceros  as em ON (am.nit = em.nit_contraparte) \n" +
                "LEFT JOIN nexco_tipo_aval as nta ON (am.tipo_aval = nta.id_tipo_aval) \n" +
                "LEFT JOIN nexco_divisas_valor as gm ON (am.DIVISA = gm.DIVISA COLLATE SQL_Latin1_General_CP1_CI_AS ) \n" +
                //"LEFT JOIN [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_FINREP_CALCULADA_"+periodo.replace("-","")+" as hm ON (RIGHT('000000000000000'+Ltrim(Rtrim(SUBSTRING(am.nit,2,LEN(am.nit)-2))),15) = hm.IDENTIFICACION COLLATE SQL_Latin1_General_CP1_CI_AS) \n" +
                "WHERE am.periodo_origen = :periodo \n" +
                "GROUP BY am.CONTRATO \n" +
                ",hs.NUCTA \n" +
                ",hsp.NUCTA \n" +
                ",am.cta_contable_60 \n" +
                ",am.divisa \n" +
                ",am.nit \n" +
                ",am.nombre_cliente \n" +
                ",CASE WHEN cm.id_pais IS NULL THEN 'CO' ELSE cm.id_pais END\n" +
                ",CASE WHEN am.nombre_banco IS NOT NULL THEN am.nombre_banco END \n" +
                ",CASE WHEN cm.nombre_pais IS NULL THEN 'COLOMBIA' ELSE cm.nombre_pais END \n" +
                ",am.intergrupo \n" +
                //",CASE WHEN hm.CODIGO_LIQUIDEZ IN :listCl THEN 'INSTITUCIÓN' \n" +
                //"ELSE 'EMPRESA' END \n" +
                ",dm1.CODICONS46 \n" +
                ",dm2.CODICONS46\n" +
                ",CONCAT('AVAL ',nta.aval_origen)\n" +
                ",am.pais_banco \n" +
                ",cm.nombre_pais \n" +
                //",hm.CODIGO_LIQUIDEZ\n" +
                ",am.fecha_alta\n" +
                ",am.fecha_vencimiento\n" +
                ",am.fecha_cierre\n" +
                ",ABS(am.saldo_divisa)\n" +
                ",ABS(am.saldo_pesos)\n" +
                ",ABS(hs.VALOR)\n" +
                ",ABS(hsp.VALOR)");

            query.setParameter("listO",listO);
            query.setParameter("listI",listI);
            query.setParameter("listIR",listIR);
            query.setParameter("listIP",listIP);
            query.setParameter("periodo",periodo);
            //query.setParameter("listCl",listCl);
            query.executeUpdate();
            Query queryLoad = entityManager.createNativeQuery("SELECT * FROM nexco_corep_obligatorios ORDER BY cta_conta_moneda ",Corep.class);
            lista=queryLoad.getResultList();
        return lista;
    }
}
