package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.DesconValConcil;
import com.inter.proyecto_intergrupo.model.ifrs9.DesconValDif;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DesconValidationConcilService {

    @Autowired
    EntityManager entityManager;

    public void createDifAndConcil(String period, String empresa, String origen) {
        Query delete = entityManager.createNativeQuery("DELETE FROM nexco_validacion_descon_dif WHERE periodo = ?");
        delete.setParameter(1, period);
        delete.executeUpdate();

        Query delete2 = entityManager.createNativeQuery("DELETE FROM nexco_validacion_descon_final WHERE periodo = ?");
        delete2.setParameter(1, period);
        delete2.executeUpdate();

        Query insert = entityManager.createNativeQuery("INSERT INTO nexco_validacion_descon_dif(cod_neocon,cuenta,saldo_plano,saldo_concil,diferencia,periodo)\n" +
                "SELECT \n" +
                "perimeter.CODICONS46 as 'Cod. Neocon',\n" +
                "perimeter.NUCTA as 'Cuenta',\n" +
                "ISNULL(host.saldo,0) as 'Saldo Host',\n" +
                "ISNULL(concil.saldo,0) as 'Saldo Concil',\n" +
                "ISNULL(ROUND(host.saldo-concil.saldo,2),0) as 'diferencia', \n" +
                ":period as period \n" +
                "FROM \n" +
                "(SELECT TRIM(NUCTA) AS NUCTA, CODICONS46 FROM dbo.nexco_provisiones as prov \n" +
                "INNER JOIN (SELECT nucta, decta, codicons46 FROM cuentas_puc WHERE empresa = '0013') \n" +
                "as puc ON puc.CODICONS46 = CONVERT(varchar,prov.cuenta_neocon) \n" +
                "WHERE ifrs9 = 'CV') as perimeter\n" +
                "LEFT JOIN (SELECT cuenta, SUM(diferencias) as saldo FROM nexco_conciliacion_scope_ifrs9 WHERE periodo = :period GROUP BY cuenta) as concil ON concil.cuenta = perimeter.NUCTA\n" +
                "LEFT JOIN (SELECT cuenta, SUM(vr_total) as saldo FROM nexco_validacion_descon_carga WHERE periodo = :period GROUP BY cuenta) as host ON perimeter.NUCTA = host.cuenta");
        insert.setParameter("period", period);
        insert.executeUpdate();

        Query insert2 = entityManager.createNativeQuery("INSERT INTO nexco_validacion_descon_final\n" +
                "(empresa,cuenta,descripcion,codicons,saldo_query,saldo_per_inc,saldo_primera_vez,\n" +
                "saldo_dif_conc,saldo_manuales,saldo_prov_gen_int, saldo_porc_calc,saldo_rechazos_aut,diferencias,periodo)\n" +
                "SELECT\n" +
                "perimetro.EMPRESA,\n" +
                "perimetro.NUCTA,\n" +
                "perimetro.DECTA,\n" +
                "perimetro.CODICONS46,\n" +
                "ISNULL(query.saldo,0) as 'Saldo Query',\n" +
                "ISNULL(perIn.saldo,0) as 'Saldo PI',\n" +
                "ISNULL(aju.saldo, 0) as 'Saldo PV',\n" +
                "ISNULL(dif.saldo, 0) as 'Saldo Dif',\n" +
                "ISNULL(man.saldo,0) as 'Saldo Man',\n" +
                "ISNULL(provGen.saldo,0) as 'Saldo Prov',\n" +
                "ISNULL(porCal.saldo,0) as 'Saldo Capitales',\n" +
                "ISNULL(rechazos.saldo,0) as 'Saldo Rechazos',\n" +
                "ISNULL(query.saldo,0)-ISNULL(perIn.saldo,0)-ISNULL(aju.saldo, 0)-ISNULL(dif.saldo, 0)-ISNULL(man.saldo,0)\n" +
                "-ISNULL(provGen.saldo,0)-ISNULL(porCal.saldo,0)-ISNULL(rechazos.saldo,0) AS diferencias, \n" +
                ":period as periodo \n" +
                "FROM\n" +
                "(SELECT TRIM(NUCTA) AS NUCTA,DECTA,CODICONS46,EMPRESA FROM dbo.nexco_provisiones as prov \n" +
                "INNER JOIN (SELECT nucta, decta, codicons46, EMPRESA FROM cuentas_puc WHERE empresa = '0013') \n" +
                "as puc ON puc.CODICONS46 = CONVERT(varchar,prov.cuenta_neocon) \n" +
                "WHERE ifrs9 = 'CV') as perimetro\n" +
                "LEFT JOIN\n" +
                "(SELECT nucta, sum(saldoquery) as saldo FROM nexco_query_marcados WHERE origen = :origen AND empresa = :empresa AND fecont like '2021-11%' GROUP BY nucta) AS query ON query.nucta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(saldo) as saldo FROM nexco_perdidaincurrida GROUP BY cuenta) AS perIn ON perIn.cuenta = perimetro.NUCTA \n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(diferencia_pesos) as saldo FROM nexco_ajuste_primera_vez GROUP BY cuenta) AS aju ON aju.cuenta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(vr_total) as saldo FROM nexco_validacion_descon_carga \n" +
                "WHERE observacion = '2-DESCONT_DESC_CONCIL' AND periodo = :period GROUP BY cuenta) as dif ON dif.cuenta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(vr_total) as saldo FROM nexco_validacion_descon_carga \n" +
                "WHERE observacion = '1-DESCONT_CTAS_MANUALES' AND periodo = :period GROUP BY cuenta) as man ON man.cuenta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(vr_total) as saldo FROM nexco_validacion_descon_carga \n" +
                "WHERE observacion = '1-DESCONTAB_PROV_GENERICA_INTERESES' AND periodo = :period GROUP BY cuenta) as provGen ON provGen.cuenta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(vr_total) as saldo FROM nexco_validacion_descon_carga \n" +
                "WHERE observacion = '1-DESCONTAB_PROV_GENERAL' AND periodo = :period GROUP BY cuenta) as porCal ON porCal.cuenta = perimetro.NUCTA\n" +
                "LEFT JOIN\n" +
                "(SELECT cuenta, sum(vr_total) as saldo FROM nexco_validacion_descon_carga \n" +
                "WHERE observacion = '2-DESCONT_SIN_SCOPE' AND periodo = :period GROUP BY cuenta) as rechazos ON rechazos.cuenta = perimetro.NUCTA\n" +
                "GROUP BY perimetro.EMPRESA,perimetro.NUCTA,perimetro.DECTA,perimetro.CODICONS46, query.saldo, perIn.saldo, aju.saldo, dif.saldo, man.saldo, provGen.saldo, porCal.saldo, rechazos.saldo");
        insert2.setParameter("period", period);
        insert2.setParameter("origen", origen);
        insert2.setParameter("empresa", empresa);
        insert2.executeUpdate();
    }

    public List<Object[]> getCompanies(String period) {
        List<Object[]> toReturn = new ArrayList<>();
        Query getData = entityManager.createNativeQuery("SELECT DISTINCT empresa FROM nexco_query_marcados WHERE fecont LIKE ?");
        getData.setParameter(1, period + "%");

        if (!getData.getResultList().isEmpty()) {
            toReturn = getData.getResultList();
        }
        return toReturn;
    }

    public List<DesconValConcil> getDiff(String period) {
        List<DesconValConcil> toReturn = new ArrayList<>();

        Query getData = entityManager.createNativeQuery("SELECT * FROM nexco_validacion_descon_final WHERE periodo = ?",DesconValConcil.class);
        getData.setParameter(1, period);

        if (!getData.getResultList().isEmpty()) {
            toReturn = getData.getResultList();
        }

        return toReturn;
    }

    public boolean dataConcilU(String period,String nivel, String tipoCuenta){
        try {
            boolean resp = true;
            String nivelDefP = nivel;
            String nivelDefS = nivel;
            String nivelD = nivel;
            if (nivelD.equals("Detalle") /*|| tipoCuenta.equals("neocon")*/) {
                nivelDefP = "LEN(A.cuenta)";
                nivelDefS = "LEN(cuenta)";
                //nivelD = "Detalle";
            }
            Query queryClean = entityManager.createNativeQuery("DELETE FROM nexco_validacion_descon_final " +
                    "WHERE periodo = ?");
            queryClean.setParameter(1, period);
            queryClean.executeUpdate();

            Query queryValidate = entityManager.createNativeQuery("INSERT INTO nexco_validacion_descon_final \n" +
                    "(cuenta,codicons,saldo_dif_conc,saldo_manuales,saldo_prov_gen_int, saldo_porc_calc,saldo_rechazos_aut,periodo,nivel,tipo_cuenta)" +
                    "SELECT SUBSTRING(A.cuenta,1," + nivelDefP + ") AS cuentaF,\n" +
                    "G.CODICONS46, \n" +
                    "ISNULL(sum(F.valor),0) AS DESCON_NIV15_PROV_PLAN00,\n" +
                    "ISNULL(sum(D.valor),0) AS TANQUE_NIV15_PROV_PLAN00,\n" +
                    "ISNULL(sum(B.valor),0) AS CONTAB_NIV15_PROV_PLAN00,\n" +
                    "ISNULL(sum(C.valor),0) AS PATPYG_NIV15_PROV_PLAN00,\n" +
                    "ISNULL(sum(E.valor),0) AS INFORME_NIVEL15_RECLASIFICACION_PLAN00,\n" +
                    ":periodo AS periodo,'"+nivelD+"' AS nivel,'"+tipoCuenta+"' AS TipoCuenta \n" +
                    "FROM (SELECT SUBSTRING(cuenta,1," + nivelDefS + ") cuenta FROM nexco_validacion_descon_carga WHERE periodo = :periodo GROUP BY SUBSTRING(cuenta,1," + nivelDefS + ")) A\n" +
                    "LEFT JOIN (SELECT SUBSTRING(cuenta,1," + nivelDefS + ") cuenta, sum(vr_total) AS valor FROM nexco_validacion_descon_carga WHERE nombre_real = 'CONTAB_NIV15_PROV_PLAN00.TXT' AND  periodo = :periodo GROUP BY SUBSTRING(cuenta,1," + nivelDefS + ")) B ON A.cuenta = B.cuenta\n" +
                    "LEFT JOIN (SELECT SUBSTRING(cuenta,1," + nivelDefS + ") cuenta, sum(vr_total) AS valor FROM nexco_validacion_descon_carga WHERE nombre_real = 'PATPYG_NIV15_PROV_PLAN00.TXT' AND  periodo = :periodo GROUP BY SUBSTRING(cuenta,1," + nivelDefS + ")) C ON A.cuenta = C.cuenta\n" +
                    "LEFT JOIN (SELECT SUBSTRING(cuenta,1," + nivelDefS + ") cuenta, sum(vr_total) AS valor FROM nexco_validacion_descon_carga WHERE nombre_real = 'TANQUE_NIV15_PROV_PLAN00.TXT' AND  periodo = :periodo GROUP BY SUBSTRING(cuenta,1," + nivelDefS + ")) D ON A.cuenta = D.cuenta\n" +
                    "LEFT JOIN (SELECT SUBSTRING(cuenta,1," + nivelDefS + ") cuenta, sum(vr_total) AS valor FROM nexco_validacion_descon_carga WHERE nombre_real = 'INFORME_NIVEL15_RECLASIFICACION_PLAN00.TXT' AND  periodo = :periodo GROUP BY SUBSTRING(cuenta,1," + nivelDefS + ")) E ON A.cuenta = E.cuenta\n" +
                    "LEFT JOIN (SELECT SUBSTRING(cuenta,1," + nivelDefS + ") cuenta, sum(vr_total) AS valor FROM nexco_validacion_descon_carga WHERE nombre_real = 'DESCON_NIV15_PROV_PLAN00.TXT' AND  periodo = :periodo GROUP BY SUBSTRING(cuenta,1," + nivelDefS + ")) F ON A.cuenta = F.cuenta\n" +
                    "LEFT JOIN (SELECT NUCTA, CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013' GROUP BY NUCTA, CODICONS46) G ON SUBSTRING(A.cuenta,1," + nivelDefP + ") = G.NUCTA \n" +
                    "GROUP BY G.CODICONS46,SUBSTRING(A.cuenta,1," + nivelDefP + ")");
            queryValidate.setParameter("periodo", period);
            queryValidate.executeUpdate();

            if(getDiff(period).isEmpty())
                resp = false;

            return resp;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
