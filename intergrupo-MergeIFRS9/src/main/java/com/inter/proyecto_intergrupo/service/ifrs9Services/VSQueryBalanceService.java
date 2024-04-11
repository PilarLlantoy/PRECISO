package com.inter.proyecto_intergrupo.service.ifrs9Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class VSQueryBalanceService {
    @Autowired
    EntityManager entityManager;

    public List<Object[]> getBalanceResume(String period){

        String periodValid = period.replace("-","");

        Query getData = entityManager.createNativeQuery("SELECT \n" +
                "prov.instrumento,\n" +
                "sub.subproducto, \n" +
                "puc.CODICONS46,\n" +
                "ISNULL(perimetro.cuenta, vertical.cuenta),\n" +
                "puc.DECTA,\n" +
                "CASE WHEN perimetro.divisa IS NOT NULL THEN perimetro.divisa ELSE CONVERT(VARCHAR,vertical.divisa) COLLATE Modern_Spanish_CI_AS END as divisa, \n" +
                "ISNULL(ISNULL(vertical.saldo,0.00) - local13.saldoquery,0.00) AS 'Diferencia VS QL13',\n" +
                "ISNULL(ISNULL(vertical.saldo,0.00) - ifrs960.saldoquery,0.00) AS 'Diferencia VS QI60',\n" +
                "ISNULL(((ISNULL(vertical.saldo,0.00) - local13.saldoquery) - (CAST(CAST(ISNULL(h140a.saldo_aplicativo,0.00) * div.valor AS DECIMAL(16,2)) - CAST(ISNULL(h140c.saldo_contable,0.00)* div.valor AS DECIMAL(16,2)) - CASE WHEN sicc.importe IS NOT NULL THEN sicc.importe ELSE 0 END AS DECIMAL(18,2)))),0.00) AS 'Diferencia VS Concil' \n" +
                "FROM \n" +
                "(SELECT q.nucta as cuenta , q.coddiv as divisa\n" +
                "FROM nexco_provisiones AS prov \n" +
                "INNER JOIN nexco_query AS q ON q.codicons = prov.cuenta_neocon \n" +
                "WHERE prov.ifrs9 = 'PR' AND q.fecont like :period2 AND q.empresa = '0060') perimetro\n" +
                "FULL OUTER JOIN\n" +
                "(SELECT\n" +
                "vertical.cves_cod_ent_iuc,\n" +
                "vertical.cves_cod_ctacont AS 'cuenta',\n" +
                "vertical.cves_cod_divisa_con AS 'divisa', \n" +
                "SUM(vertical.cves_imp_saldo_loc) AS 'saldo'\n" +
                "FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_vertical_saldos_"+periodValid+" AS vertical \n" +
                "WHERE  vertical.cves_cod_ctacont IS NOT NULL AND vertical.cves_cod_ent_iuc = '0013' \n" +
                "GROUP BY \n" +
                "vertical.cves_cod_ent_iuc,\n" +
                "vertical.cves_cod_divisa_con,\n" +
                "vertical.cves_cod_ctacont\n" +
                ") vertical \n" +
                "ON CONVERT(VARCHAR,vertical.cuenta) COLLATE Modern_Spanish_CI_AS = perimetro.cuenta\n" +
                "AND perimetro.divisa = CONVERT(VARCHAR,vertical.divisa) COLLATE Modern_Spanish_CI_AS\n" +
                "LEFT JOIN (SELECT NUCTA, CODICONS46, DECTA FROM CUENTAS_PUC WHERE empresa = '0013') AS puc\n" +
                "ON puc.NUCTA = ISNULL(perimetro.cuenta,vertical.cuenta) \n" +
                "LEFT JOIN (SELECT cuenta_neocon, instrumento FROM nexco_provisiones) AS prov\n" +
                "ON prov.cuenta_neocon = puc.CODICONS46\n" +
                "LEFT JOIN (SELECT * FROM nexco_cuenta_subproducto_local) AS sub \n" +
                "ON sub.cuenta = SUBSTRING(puc.NUCTA,1,6)\n" +
                "LEFT JOIN (select * from nexco_divisas_valor WHERE fecha like :period2) as div\n" +
                "ON ISNULL(perimetro.divisa,vertical.divisa) = div.divisa\n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, SUBSTRING(CAST(fechproce as varchar),1,10) fecont , coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'LOCAL' AND fecont like :period2 AND empresa = '0013' GROUP BY nucta, fechproce ,coddiv) AS local13 \n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = local13.nucta \n" +
                "AND local13.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'LOCAL' AND fecont like :period2 AND empresa = '0064' GROUP BY nucta, coddiv) AS local64 \n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = local64.nucta\n" +
                "AND local64.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'LOCAL' AND fecont like :period2 AND empresa = '0060' GROUP BY nucta, coddiv) AS local60\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = local60.nucta\n" +
                "AND local60.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, SUBSTRING(CAST(fechproce as varchar),1,10) fecont , coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'IFRS9' AND fecont like :period2 AND empresa = '0013' GROUP BY nucta, fechproce , coddiv) AS ifrs913\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = ifrs913.nucta \n" +
                "AND ifrs913.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'IFRS9' AND fecont like :period2 AND empresa = '0064' GROUP BY nucta, coddiv) AS ifrs964\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = ifrs964.nucta\n" +
                "AND ifrs964.divisa = ISNULL(perimetro.divisa, vertical.divisa) \n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'IFRS9' AND fecont like :period2 AND empresa = '0060' GROUP BY nucta, coddiv) AS ifrs960\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = ifrs960.nucta\n" +
                "AND ifrs960.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT cuenta, SUM(saldo_aplicativo) saldo_aplicativo, divisa FROM nexco_h140_completa_ifrs9 WHERE fecha like :period2 GROUP BY cuenta, divisa) AS h140a\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = h140a.cuenta\n" +
                "AND h140a.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT cuenta, SUM(saldo_contable) saldo_contable, divisa FROM nexco_h140_completa_ifrs9 WHERE fecha like :period2 GROUP BY cuenta, divisa) AS h140c\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = h140c.cuenta\n" +
                "AND h140C.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT cuenta, SUM(importe) importe, divisa FROM Cargas_Anexos_SICC_"+periodValid+" GROUP BY cuenta, divisa) sicc\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = sicc.cuenta\n" +
                "AND sicc.divisa = ISNULL(perimetro.divisa, vertical.divisa) \n" +
                "GROUP BY \n" +
                "div.valor,\n" +
                "prov.instrumento,\n" +
                "sub.subproducto,\n" +
                "puc.CODICONS46,\n" +
                "perimetro.cuenta,\n" +
                "puc.DECTA,\n" +
                "perimetro.divisa,\n" +
                "vertical.saldo,\n" +
                "local13.saldoquery,\n" +
                "local64.saldoquery,\n" +
                "local60.saldoquery,\n" +
                "local13.fecont,\n" +
                "ifrs913.saldoquery,\n" +
                "ifrs964.saldoquery,\n" +
                "ifrs960.saldoquery,\n" +
                "ifrs913.fecont,\n" +
                "h140a.saldo_aplicativo,\n" +
                "h140c.saldo_contable,\n" +
                "sicc.importe, \n"+
                "vertical.divisa, \n"+
                "vertical.cuenta");
        
        getData.setParameter("period2", period.concat("%"));

        return getData.getResultList();
    }

    public List<Object[]> getBalance(String period){

        String periodValid = period.replace("-","");
        
        Query getData = entityManager.createNativeQuery("SELECT \n" +
                "prov.instrumento,\n" +
                "sub.subproducto, \n" +
                "puc.CODICONS46,\n" +
                "ISNULL(perimetro.cuenta, vertical.cuenta),\n" +
                "puc.DECTA,\n" +
                "CASE WHEN perimetro.divisa IS NOT NULL THEN perimetro.divisa ELSE CONVERT(VARCHAR,vertical.divisa) COLLATE Modern_Spanish_CI_AS END as divisa,\n" +
                "div.valor AS tasa,\n" +
                "ISNULL(vertical.saldo,0.00) AS 'Saldo Vertical',\n" +
                "ISNULL(local13.saldoquery,0.00) AS 'Saldo Query Local 13',\n" +
                "ISNULL(local64.saldoquery,0.00) AS 'Saldo Query Local 64',\n" +
                "ISNULL(local60.saldoquery,0.00) AS 'Saldo Query Local 60',\n" +
                "local13.fecont as 'Fecha Query Local',\n" +
                "ISNULL(ifrs913.saldoquery,0.00) AS 'Saldo Query IFRS9 13',\n" +
                "ISNULL(ifrs964.saldoquery,0.00) AS 'Saldo Query IFRS9 64', \n" +
                "ISNULL(ifrs960.saldoquery,0.00) AS 'Saldo Query IFRS9 60',\n" +
                "ifrs913.fecont as 'Fecha Query IFRS9',\n" +
                "CAST(ISNULL(h140a.saldo_aplicativo,0.00) * div.valor AS DECIMAL(16,2)) AS 'H140 Saldo Aplicativo',\n" +
                "CAST(ISNULL(h140c.saldo_contable,0.00)* div.valor AS DECIMAL(16,2)) AS 'H140 Saldo Contable',\n" +
                "ISNULL(sicc.importe,0.00) 'Saldo SICC',\n" +
                "ISNULL((CAST(CAST(ISNULL(h140a.saldo_aplicativo,0.00) * div.valor AS DECIMAL(16,2)) - CAST(ISNULL(h140c.saldo_contable,0.00)* div.valor AS DECIMAL(16,2)) - CASE WHEN sicc.importe IS NOT NULL THEN sicc.importe ELSE 0 END AS DECIMAL(18,2))),0.00) AS 'Diferencia H140',\n" +
                "ISNULL(ISNULL(vertical.saldo,0.00) - local13.saldoquery,0.00) AS 'Diferencia VS QL13',\n" +
                "ISNULL(ISNULL(vertical.saldo,0.00) - ifrs960.saldoquery,0.00) AS 'Diferencia VS QI60',\n" +
                "ISNULL(((ISNULL(vertical.saldo,0.00) - local13.saldoquery) - (CAST(CAST(ISNULL(h140a.saldo_aplicativo,0.00) * div.valor AS DECIMAL(16,2)) - CAST(ISNULL(h140c.saldo_contable,0.00)* div.valor AS DECIMAL(16,2)) - CASE WHEN sicc.importe IS NOT NULL THEN sicc.importe ELSE 0 END AS DECIMAL(18,2)))),0.00) AS 'Diferencia VS Concil' \n" +
                "FROM \n" +
                "(SELECT q.nucta as cuenta , q.coddiv as divisa\n" +
                "FROM nexco_provisiones AS prov \n" +
                "INNER JOIN nexco_query AS q ON q.codicons = prov.cuenta_neocon \n" +
                "WHERE prov.ifrs9 = 'PR' AND q.fecont like :period2 AND q.empresa = '0060') perimetro\n" +
                "FULL OUTER JOIN\n" +
                "(SELECT\n" +
                "vertical.cves_cod_ent_iuc,\n" +
                "vertical.cves_cod_ctacont AS 'cuenta',\n" +
                "vertical.cves_cod_divisa_con AS 'divisa', \n" +
                "SUM(vertical.cves_imp_saldo_loc) AS 'saldo'\n" +
                "FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_vertical_saldos_"+periodValid+" AS vertical \n" +
                "WHERE  vertical.cves_cod_ctacont IS NOT NULL AND vertical.cves_cod_ent_iuc = '0013' \n" +
                "GROUP BY \n" +
                "vertical.cves_cod_ent_iuc,\n" +
                "vertical.cves_cod_divisa_con,\n" +
                "vertical.cves_cod_ctacont\n" +
                ") vertical \n" +
                "ON CONVERT(VARCHAR,vertical.cuenta) COLLATE Modern_Spanish_CI_AS = perimetro.cuenta\n" +
                "AND perimetro.divisa = CONVERT(VARCHAR,vertical.divisa) COLLATE Modern_Spanish_CI_AS\n" +
                "LEFT JOIN (SELECT NUCTA, CODICONS46, DECTA FROM CUENTAS_PUC WHERE empresa = '0013') AS puc\n" +
                "ON puc.NUCTA = ISNULL(perimetro.cuenta,vertical.cuenta) \n" +
                "LEFT JOIN (SELECT cuenta_neocon, instrumento FROM nexco_provisiones) AS prov\n" +
                "ON prov.cuenta_neocon = puc.CODICONS46\n" +
                "LEFT JOIN (SELECT * FROM nexco_cuenta_subproducto_local) AS sub \n" +
                "ON sub.cuenta = SUBSTRING(puc.NUCTA,1,6)\n" +
                "LEFT JOIN (select * from nexco_divisas_valor WHERE fecha like :period2) as div\n" +
                "ON ISNULL(perimetro.divisa,vertical.divisa) = div.divisa\n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, SUBSTRING(CAST(fechproce as varchar),1,10) fecont, coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'LOCAL' AND fecont like :period2 AND empresa = '0013' GROUP BY nucta, fechproce ,coddiv) AS local13 \n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = local13.nucta \n" +
                "AND local13.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'LOCAL' AND fecont like :period2 AND empresa = '0064' GROUP BY nucta, coddiv) AS local64 \n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = local64.nucta\n" +
                "AND local64.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'LOCAL' AND fecont like :period2 AND empresa = '0060' GROUP BY nucta, coddiv) AS local60\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = local60.nucta\n" +
                "AND local60.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, SUBSTRING(CAST(fechproce as varchar),1,10) fecont, coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'IFRS9' AND fecont like :period2 AND empresa = '0013' GROUP BY nucta, fechproce, coddiv) AS ifrs913\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = ifrs913.nucta \n" +
                "AND ifrs913.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'IFRS9' AND fecont like :period2 AND empresa = '0064' GROUP BY nucta, coddiv) AS ifrs964\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = ifrs964.nucta\n" +
                "AND ifrs964.divisa = ISNULL(perimetro.divisa, vertical.divisa) \n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS saldoquery, coddiv as divisa FROM nexco_query_marcados \n" +
                "WHERE origen = 'IFRS9' AND fecont like :period2 AND empresa = '0060' GROUP BY nucta, coddiv) AS ifrs960\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = ifrs960.nucta\n" +
                "AND ifrs960.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT cuenta, SUM(saldo_aplicativo) saldo_aplicativo, divisa FROM nexco_h140_completa_ifrs9 WHERE fecha like :period2 GROUP BY cuenta, divisa) AS h140a\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = h140a.cuenta\n" +
                "AND h140a.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT cuenta, SUM(saldo_contable) saldo_contable, divisa FROM nexco_h140_completa_ifrs9 WHERE fecha like :period2 GROUP BY cuenta, divisa) AS h140c\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = h140c.cuenta\n" +
                "AND h140C.divisa = ISNULL(perimetro.divisa, vertical.divisa)\n" +
                "LEFT JOIN (SELECT cuenta, SUM(importe) importe, divisa FROM Cargas_Anexos_SICC_"+periodValid+" GROUP BY cuenta, divisa) sicc\n" +
                "ON ISNULL(perimetro.cuenta,vertical.cuenta) = sicc.cuenta\n" +
                "AND sicc.divisa = ISNULL(perimetro.divisa, vertical.divisa) \n" +
                "GROUP BY \n" +
                "div.valor,\n" +
                "prov.instrumento,\n" +
                "sub.subproducto,\n" +
                "puc.CODICONS46,\n" +
                "perimetro.cuenta,\n" +
                "puc.DECTA,\n" +
                "perimetro.divisa,\n" +
                "vertical.saldo,\n" +
                "local13.saldoquery,\n" +
                "local64.saldoquery,\n" +
                "local60.saldoquery,\n" +
                "local13.fecont,\n" +
                "ifrs913.saldoquery,\n" +
                "ifrs964.saldoquery,\n" +
                "ifrs960.saldoquery,\n" +
                "ifrs913.fecont,\n" +
                "h140a.saldo_aplicativo,\n" +
                "h140c.saldo_contable,\n" +
                "sicc.importe, \n"+
                "vertical.divisa, \n"+
                "vertical.cuenta");
        
        getData.setParameter("period2", period.concat("%"));

        return getData.getResultList();
    }
}
