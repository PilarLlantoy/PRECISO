package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.IFRS9ConcilAccount;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class ConcilIFRS9Service {
    @PersistenceContext
    EntityManager entityManager;

    public void createIFRS9Conciliation(String period) {
        Query delete = entityManager.createNativeQuery("DELETE FROM nexco_conciliacion_scope_ifrs9 WHERE periodo = ?");
        delete.setParameter(1, period);
        delete.executeUpdate();

        Query insert = entityManager.createNativeQuery("INSERT INTO nexco_conciliacion_scope_ifrs9(empresa,cuenta,descripcion,codicons,saldo_s2,saldo_per_inc,saldo_primera_vez,\n" +
                "saldo_manuales,saldo_prov_gen_int,saldo_porc_calc,saldo_desc_auto, saldo_rechazos_aut, saldo_dif_conc, observacion, periodo)\n" +
                "SELECT \n" +
                "perimetro.EMPRESA,\n" +
                "perimetro.NUCTA,\n" +
                "perimetro.DECTA,\n" +
                "perimetro.CODICONS46,\n" +
                "ROUND(ISNULL(s2.mes1,0.00),2) as 'Saldo S2',\n" +
                "ROUND(ISNULL(perIn.saldo,0.00),2) as 'Saldo Perdida Incurrida',\n" +
                "ROUND(ISNULL(aju.saldo,0.00),2) as 'Saldo Primera Vez',\n" +
                "ROUND(ISNULL(manuales.saldo,0),2) as 'Saldo Manuales',\n" +
                "ROUND(ISNULL(prov.saldo,0.00),2) as 'Saldo Prov Gen Int',\n" +
                "ROUND(ISNULL(porc.saldo,0.00),2) as 'Saldo 1%',\n" +
                "ROUND(ISNULL(nv15.saldo,0.00),2) as 'Saldo Nivel 15',\n" +
                "ROUND(ISNULL(rechazos.saldo,0.00),2) as 'Saldo Rechazos Descontabilizacion',\n" +
                "ROUND(ISNULL(diferencias.saldo,0.00),2) as 'Saldo Diferencias Conciliacion',\n" +
                "'' AS 'Justificacion', \n" +
                ":period as Periodo \n" +
                "FROM\n" +
                "(SELECT TRIM(NUCTA) AS NUCTA,DECTA,CODICONS46,EMPRESA FROM dbo.nexco_provisiones as prov \n" +
                "INNER JOIN (SELECT nucta, decta, codicons46, EMPRESA FROM cuentas_puc WHERE empresa = '0013') \n" +
                "as puc ON puc.CODICONS46 = CONVERT(varchar,prov.cuenta_neocon) \n" +
                "WHERE ifrs9 = 'CV') as perimetro\n" +
                "LEFT JOIN\n" +
                "(SELECT nucta as nucta , sum(mes1) as mes1 FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.S2_MES_P3 WHERE TipoRegistro = '001' GROUP BY NUCTA) AS S2 ON S2.nucta COLLATE Modern_Spanish_CI_AS = perimetro.NUCTA\n" +
                "LEFT JOIN \n" +
                "(SELECT cuenta, sum(saldo) as saldo FROM nexco_perdidaincurrida GROUP BY cuenta) AS perIn ON perIn.cuenta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(diferencia_pesos) as saldo FROM nexco_ajuste_primera_vez GROUP BY cuenta) AS aju ON aju.cuenta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta_puc, sum(importe) as saldo FROM nexco_manuales_anexo WHERE periodo = :period GROUP BY cuenta_puc) as manuales ON manuales.cuenta_puc = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(importe) as saldo FROM nexco_anexo_8_prov_gen_int WHERE fecha_origen LIKE :period3 GROUP BY cuenta) as prov ON prov.cuenta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(importe) as saldo FROM nexco_anexo_8_porc_cal WHERE fecha_origen LIKE :period3 GROUP BY cuenta) as porc ON porc.cuenta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(diferencia) as saldo FROM nexco_descon_nv15 WHERE periodo = :period  AND observaciones = 'PROVISIONES PLAN00 DESCONTAB' GROUP BY cuenta) as nv15 ON nv15.cuenta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(saldo) as saldo FROM nexco_rechazos_descontabilizacion WHERE periodo = :period GROUP BY cuenta) as rechazos ON rechazos.cuenta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(valor_diferencia) as saldo FROM nexco_diferencias WHERE periodo = :period GROUP BY cuenta) as diferencias ON diferencias.cuenta = perimetro.NUCTA\n" +
                "GROUP BY perimetro.NUCTA, perimetro.DECTA,perimetro.CODICONS46,perimetro.EMPRESA,s2.mes1,perIn.saldo, aju.saldo, manuales.saldo, prov.saldo, porc.saldo, nv15.saldo, rechazos.saldo, diferencias.saldo\n" +
                "ORDER BY ISNULL(s2.mes1,0) DESC");

        insert.setParameter("period", period);
        insert.setParameter("period3", period.replace("-", "/") + "%");
        insert.executeUpdate();

        Query update = entityManager.createNativeQuery("UPDATE nexco_conciliacion_scope_ifrs9 \n" +
                "SET saldo_manuales = saldo_manuales *-1, \n" +
                "saldo_prov_gen_int = saldo_prov_gen_int*-1, \n" +
                "saldo_porc_calc = saldo_porc_calc *-1, \n" +
                "saldo_rechazos_aut = saldo_rechazos_aut *-1\n" +
                "WHERE periodo = :period ");
        update.setParameter("period", period);
        update.executeUpdate();

        Query update2 = entityManager.createNativeQuery("UPDATE nexco_conciliacion_scope_ifrs9 \n" +
                "SET diferencias = ROUND(saldo_s2 + saldo_per_inc - saldo_per_inc + saldo_primera_vez + saldo_manuales + saldo_prov_gen_int + saldo_porc_calc + saldo_desc_auto + saldo_rechazos_aut + saldo_dif_conc,2) \n " +
                "WHERE periodo = :period");
        update2.setParameter("period", period);
        update2.executeUpdate();

        Query update3 = entityManager.createNativeQuery("UPDATE nexco_conciliacion_scope_ifrs9 \n" +
                "SET validacion = CASE WHEN diferencias <> 0 THEN 'REVISAR' ELSE 'OK' END\n " +
                "WHERE periodo = :period");
        update3.setParameter("period", period);
        update3.executeUpdate();

    }

    public ArrayList<IFRS9ConcilAccount> getConciliation(String period) {
        ArrayList<IFRS9ConcilAccount> toReturn = new ArrayList<>();

        Query getData = entityManager.createNativeQuery("SELECT * FROM nexco_conciliacion_scope_ifrs9 WHERE periodo = ? order by cuenta", IFRS9ConcilAccount.class);
        getData.setParameter(1, period);

        if (!getData.getResultList().isEmpty()) {
            toReturn = (ArrayList<IFRS9ConcilAccount>) getData.getResultList();
        }

        return toReturn;
    }

    public ArrayList<IFRS9ConcilAccount> getConciliationAccum(String period) {
        ArrayList<IFRS9ConcilAccount> toReturn = new ArrayList<>();

        Query getData = entityManager.createNativeQuery("SELECT \n" +
                "id_carga, empresa, cuenta, descripcion, codicons, saldo_s2, saldo_per_inc, saldo_primera_vez, saldo_dif_conc, saldo_manuales,\n" +
                "saldo_prov_gen_int, saldo_porc_calc, saldo_desc_auto, saldo_rechazos_aut, saldo_dif_conc, validacion, observacion,diferencias, periodo\n" +
                "FROM nexco_conciliacion_scope_ifrs9 WHERE periodo = :period \n" +
                "UNION ALL\n" +
                "SELECT 0, 'TOTALES', '','','', SUM(saldo_s2), SUM(saldo_per_inc), SUM(saldo_primera_vez), SUM(saldo_dif_conc), SUM(saldo_manuales),\n" +
                "SUM(saldo_prov_gen_int), SUM(saldo_porc_calc), SUM(saldo_desc_auto), SUM(saldo_rechazos_aut), SUM(saldo_dif_conc),'', '', SUM(diferencias), ''\n" +
                "FROM nexco_conciliacion_scope_ifrs9 WHERE periodo = :period \n" +
                "ORDER BY 2,3,4 ", IFRS9ConcilAccount.class);
        getData.setParameter("period", period);

        if (!getData.getResultList().isEmpty()) {
            toReturn = (ArrayList<IFRS9ConcilAccount>) getData.getResultList();
        }

        return toReturn;
    }

}
