package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.bank.GpsReport;
import com.inter.proyecto_intergrupo.model.parametric.ComerParametric;
import com.inter.proyecto_intergrupo.model.reports.ConciliationComer;
import com.inter.proyecto_intergrupo.repository.bank.GpsReportRepository;
import groovy.lang.Tuple2;
import groovy.lang.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class ConciliationComerService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    GpsReportRepository gpsReportRepository;

    public List<ConciliationComer> getConciliation(String periodo){

        Query getResult= entityManager.createNativeQuery("SELECT * FROM nexco_conciliacion_comer as con " +
                "WHERE con.periodo = ?",ConciliationComer.class);
        getResult.setParameter(1,periodo);

        List<ConciliationComer> result = getResult.getResultList();
        return result;
    }

    public void insertComerConciliation(String periodo){

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

        Query delete = entityManager.createNativeQuery("DELETE FROM nexco_conciliacion_comer WHERE periodo = ?");
        delete.setParameter(1,periodo);
        delete.executeUpdate();

        Query conciliate = entityManager.createNativeQuery("INSERT INTO nexco_conciliacion_comer(cuenta_banco, cuenta_comercializadora,nombre_cuenta_banco,importe_comercializadora,calculo_prorrata_iva,total_comercializadora,importe_real,importe_provisiones,total_gps,diferencias_totales,importe_base_fiscal,importe_balance, diferencia_pagos_reales, diferencia_total_gps_balance,periodo) \n" +
                "SELECT resp.cuenta_local as 'Cuenta Banco',\n" +
                "CASE WHEN paramFil.cuenta_filial IS NULL THEN '' ELSE paramFil.cuenta_filial END as 'Cuenta Comer',\n" +
                "puc.DECTA as 'Nombre Cuenta Banco',\n" +
                "CASE WHEN filiales.valor IS NULL THEN 0 ELSE filiales.valor END as 'Importe Comer',\n" +
                "CAST(paramComer.prorrata_iva AS float) * CAST(CASE WHEN filiales.valor IS NULL THEN 0 ELSE filiales.valor END AS float) as 'Calculo Prorrata Iva',\n" +
                "(CASE WHEN filiales.valor IS NULL THEN 0 ELSE filiales.valor END) + (CAST(paramComer.prorrata_iva AS float) * CAST(CASE WHEN filiales.valor IS NULL THEN 0 ELSE filiales.valor END AS float)) as 'Total Comer',\n" +
                "CASE WHEN comer.[Importe real] IS NULL THEN 0 ELSE comer.[Importe real] END AS 'Importe Real',\n" +
                "CASE WHEN comer.[Importe Provision] IS NULL THEN 0 ELSE comer.[Importe Provision] END AS 'Importe Provision',\n" +
                "CASE WHEN comer.[Total GPS] IS NULL THEN 0 ELSE comer.[Total GPS] END AS 'Total GPS',\n" +
                "(CASE WHEN filiales.valor IS NULL THEN 0 ELSE filiales.valor END + (CAST(paramComer.prorrata_iva AS float) * CAST(CASE WHEN filiales.valor IS NULL THEN 0 ELSE filiales.valor END AS float))) - (CASE WHEN comer.[Total GPS] IS NULL THEN 0 ELSE comer.[Total GPS] END) as 'Diferencias Totales',\n" +
                "CASE WHEN base.valor IS NULL THEN 0 ELSE base.valor END AS 'Importe Base Fiscal',\n" +
                "ISNULL(CAST(query.valor AS bigint),0) AS 'Importe Balance',\n" +
                "ISNULL(comer.[Importe real],0) - CASE WHEN base.valor IS NULL THEN 0 ELSE base.valor END  as 'Diferencia Pagos Reales',\n" +
                "ISNULL(comer.[Total GPS],0) - ISNULL(CAST(query.valor AS bigint),0) as 'Diferencia Total GPS y Balance',\n" +
                "'"+periodo+"' \n" +
                "FROM\n" +
                "(SELECT cuenta_local FROM nexco_cuentas_responsables WHERE aplica_metodologia = 1) as resp\n" +
                "LEFT JOIN  \n" +
                "(SELECT ISNULL(comer_real.cuenta_local, comer_prov.cuenta_local) as cuenta_local ,ISNULL(comer_real.valor,0) as 'Importe real', ISNULL(comer_prov.valor,0) as 'Importe Provision', ISNULL(comer_real.valor,0) + ISNULL(comer_prov.valor,0) as 'Total GPS' FROM\n" +
                "(SELECT comer.cuenta_local as cuenta_local, SUM(comer.valor) as valor\n" +
                "FROM nexco_precarga_comer as comer\n" +
                "WHERE comer.observaciones = 'Pagos Reales' AND periodo IN (:months) \n" +
                "GROUP BY cuenta_local) AS comer_real\n" +
                "FULL OUTER JOIN (SELECT comer.cuenta_local as cuenta_local, SUM(comer.valor) as valor\n" +
                "FROM nexco_precarga_comer as comer\n" +
                "WHERE comer.observaciones = 'Provisiones' AND periodo IN (:months)\n" +
                "GROUP BY cuenta_local) AS comer_prov\n" +
                "ON comer_real.cuenta_local = comer_prov.cuenta_local) as comer ON comer.cuenta_local = resp.cuenta_local\n" +
                "LEFT JOIN nexco_filiales as paramFil ON resp.cuenta_local = paramFil.cuenta_local\n" +
                "LEFT JOIN (SELECT cuenta_local, valor FROM nexco_plantilla_filiales as filiales WHERE filiales.yntp_reportante = '00570' AND periodo = '"+periodo+"') as filiales ON filiales.cuenta_local = paramFil.cuenta_filial\n" +
                "LEFT JOIN (SELECT NUCTA, DECTA FROM CUENTAS_PUC as puc where EMPRESA = '0013' GROUP BY DECTA, NUCTA) as puc ON resp.cuenta_local = puc.NUCTA\n" +
                "LEFT JOIN (SELECT prorrata_iva , nombre_clase, cuenta_local FROM nexco_parametrica_metodo_comer as comer GROUP BY nombre_clase, cuenta_local, prorrata_iva) as paramComer ON resp.cuenta_local = paramComer.cuenta_local\n" +
                "LEFT JOIN (SELECT cuenta_local, SUM(bf.valor) as valor FROM nexco_base_fiscal AS bf WHERE bf.nit_contraparte = '900249297' AND periodo IN (:months) GROUP BY cuenta_local) as base ON base.cuenta_local = resp.cuenta_local\n" +
                "LEFT JOIN (SELECT SUM(saldoquery) as valor, nucta FROM nexco_query_marcados as q  WHERE q.origen = 'LOCAL' AND q.empresa = '0013' AND q.fecont LIKE '"+periodo+"%' GROUP BY nucta) as query ON resp.cuenta_local = query.nucta\n" +
                "GROUP BY resp.cuenta_local, comer.[Importe real], comer.[Importe Provision], comer.[Total GPS], paramFil.cuenta_filial, puc.DECTA, filiales.valor, base.cuenta_local,base.valor, query.nucta, query.valor, paramComer.prorrata_iva \n" +
                "ORDER BY resp.cuenta_local");

        conciliate.setParameter("months",months);

        conciliate.executeUpdate();
    }

    public List<ConciliationComer> getConciliationWithTotal(String periodo){

        Query getResult= entityManager.createNativeQuery("SELECT id_comer,cuenta_banco,cuenta_comercializadora,nombre_cuenta_banco, importe_comercializadora, \n" +
                "calculo_prorrata_iva, total_comercializadora, importe_real, importe_provisiones, total_gps, diferencias_totales,\n" +
                "importe_base_fiscal, importe_balance, diferencia_pagos_reales, diferencia_total_gps_balance,\n" +
                "periodo\n" +
                "FROM nexco_conciliacion_comer \n" +
                "WHERE periodo = :period\n" +
                "UNION ALL \n" +
                "SELECT 0,'TOTALES','','',\n" +
                "SUM(importe_comercializadora) as tc, SUM(calculo_prorrata_iva), SUM (total_comercializadora),\n" +
                "SUM(importe_real), SUM(importe_provisiones), SUM(total_gps), SUM(diferencias_totales), SUM(importe_base_fiscal),\n" +
                "SUM(importe_balance), SUM(diferencia_pagos_reales), SUM(diferencia_total_gps_balance), ''\n" +
                "FROM nexco_conciliacion_comer \n" +
                "WHERE periodo = :period \n"+
                "ORDER BY cuenta_banco ",ConciliationComer.class);
        getResult.setParameter("period",periodo);

        List<ConciliationComer> result = getResult.getResultList();
        return result;
    }

}
