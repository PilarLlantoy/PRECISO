package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reports.DescontabilizacionReport;
import com.inter.proyecto_intergrupo.model.reports.DescontabilizacionTemplate;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.ControlPanelJobsRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelJobsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DescontabilizacionReportService {

    @Autowired
    private ControlPanelJobsRepository controlPanelJobsRepository;

    @Autowired
    private ControlPanelJobsService controlPanelJobsService;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public DescontabilizacionReportService(ControlPanelJobsRepository controlPanelJobsRepository, AuditRepository auditRepository, JdbcTemplate jdbcTemplate) {
        this.controlPanelJobsRepository = controlPanelJobsRepository;
        this.auditRepository = auditRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean generateDesconFile(String period){
        Query delete = entityManager.createNativeQuery("DELETE FROM nexco_descontabilizacion_report WHERE periodo = ?");
        delete.setParameter(1,period);
        delete.executeUpdate();

        try {
            Query insertInfo = entityManager.createNativeQuery("INSERT INTO nexco_descontabilizacion_report (fuente_informacion,cuenta,centro,divisa,contrato,concepto,saldo,contrapartida,contrato_generico, periodo) \n" +
                    "SELECT 'Provisión general Intereses',prov.cuenta,centro,divisa,prov.contrato,con.concepto,(SUM(importe)*con.saldo) as saldo,con.cuenta,con.contrato, :period  \n" +
                    "FROM \n" +
                    "(SELECT TRIM(NUCTA) AS NUCTA,DECTA,CODICONS46,EMPRESA FROM dbo.nexco_provisiones as prov \n" +
                    "INNER JOIN (SELECT nucta, decta, codicons46, EMPRESA FROM cuentas_puc WHERE empresa = '0013') \n" +
                    "as puc ON puc.CODICONS46 = CONVERT(varchar,prov.cuenta_neocon) \n" +
                    "WHERE ifrs9 = 'CV') as perimetro\n" +
                    "LEFT JOIN\n" +
                    "nexco_anexo_8_prov_gen_int as prov  ON perimetro.NUCTA = prov.cuenta\n" +
                    "INNER JOIN \n" +
                    "nexco_counterparty_generic_contracts as con ON con.fuente_informacion = 'Provisión general Intereses'\n" +
                    "WHERE prov.fecha_origen LIKE :period2 \n" +
                    "GROUP BY prov.cuenta,centro,divisa,prov.contrato, con.concepto, con.cuenta,con.contrato, con.saldo\n" +
                    "UNION ALL\n" +
                    "SELECT 'Provisión general Capitales',prov.cuenta,centro,divisa,prov.contrato,con.concepto,(SUM(importe)*con.saldo) as saldo,con.cuenta,con.contrato, :period \n" +
                    "FROM \n" +
                    "(SELECT TRIM(NUCTA) AS NUCTA,DECTA,CODICONS46,EMPRESA FROM dbo.nexco_provisiones as prov \n" +
                    "INNER JOIN (SELECT nucta, decta, codicons46, EMPRESA FROM cuentas_puc WHERE empresa = '0013') \n" +
                    "as puc ON puc.CODICONS46 = CONVERT(varchar,prov.cuenta_neocon) \n" +
                    "WHERE ifrs9 = 'CV') as perimetro\n" +
                    "LEFT JOIN\n" +
                    "nexco_anexo_8_porc_cal as prov ON perimetro.NUCTA = prov.cuenta\n" +
                    "INNER JOIN \n" +
                    "nexco_counterparty_generic_contracts as con ON con.fuente_informacion = 'Provisión general Capitales'\n" +
                    "WHERE prov.fecha_origen LIKE :period2  \n" +
                    "GROUP BY prov.cuenta,centro,divisa,prov.contrato, con.concepto, con.cuenta,con.contrato, con.saldo\n" +
                    "UNION ALL\n" +
                    "SELECT 'Manuales (Anexos 8)',prov.cuenta_puc,centro,divisa,prov.contrato,con.concepto,(ROUND(SUM(importe),2)*con.saldo) as saldo,con.cuenta,con.contrato, :period \n" +
                    "FROM \n" +
                    "(SELECT TRIM(NUCTA) AS NUCTA,DECTA,CODICONS46,EMPRESA FROM dbo.nexco_provisiones as prov \n" +
                    "INNER JOIN (SELECT nucta, decta, codicons46, EMPRESA FROM cuentas_puc WHERE empresa = '0013') \n" +
                    "as puc ON puc.CODICONS46 = CONVERT(varchar,prov.cuenta_neocon) \n" +
                    "WHERE ifrs9 = 'CV') as perimetro\n" +
                    "LEFT JOIN\n" +
                    "nexco_manuales_anexo as prov ON perimetro.NUCTA = prov.cuenta_puc\n" +
                    "INNER JOIN \n" +
                    "nexco_counterparty_generic_contracts as con ON con.fuente_informacion = 'Manuales (Anexos 8)'\n" +
                    "WHERE prov.periodo = :period \n" +
                    "GROUP BY prov.cuenta_puc,centro,divisa,prov.contrato, con.concepto, con.cuenta,con.contrato, con.saldo\n" +
                    "UNION ALL\n" +
                    "SELECT 'Rechazos descontabilización',prov.cuenta,centro,divisa,prov.contrato,con.concepto,(ROUND(SUM(prov.saldo),2)*con.saldo),con.cuenta,con.contrato, :period \n" +
                    "FROM \n" +
                    "(SELECT TRIM(NUCTA) AS NUCTA,DECTA,CODICONS46,EMPRESA FROM dbo.nexco_provisiones as prov \n" +
                    "INNER JOIN (SELECT nucta, decta, codicons46, EMPRESA FROM cuentas_puc WHERE empresa = '0013') \n" +
                    "as puc ON puc.CODICONS46 = CONVERT(varchar,prov.cuenta_neocon) \n" +
                    "WHERE ifrs9 = 'CV') as perimetro\n" +
                    "LEFT JOIN\n" +
                    "nexco_rechazos_descontabilizacion as prov ON perimetro.NUCTA = prov.cuenta\n" +
                    "INNER JOIN \n" +
                    "nexco_counterparty_generic_contracts as con ON con.fuente_informacion = 'Rechazos descontabilización'\n" +
                    "WHERE prov.periodo = :period\n" +
                    "GROUP BY prov.cuenta,centro,divisa,prov.contrato, con.concepto, con.cuenta,con.contrato, con.saldo \n" +
                    "UNION ALL\n" +
                    "SELECT 'Diferencias conciliación',prov.cuenta,centro,'COP',prov.contrato,con.concepto,(ROUND(SUM(prov.valor_diferencia),2)*con.saldo)*-1,con.cuenta,con.contrato , :period \n" +
                    "FROM \n" +
                    "(SELECT TRIM(NUCTA) AS NUCTA,DECTA,CODICONS46,EMPRESA FROM dbo.nexco_provisiones as prov \n" +
                    "INNER JOIN (SELECT nucta, decta, codicons46, EMPRESA FROM cuentas_puc WHERE empresa = '0013') \n" +
                    "as puc ON puc.CODICONS46 = CONVERT(varchar,prov.cuenta_neocon) \n" +
                    "WHERE ifrs9 = 'CV') as perimetro\n" +
                    "LEFT JOIN\n" +
                    "nexco_diferencias as prov ON perimetro.NUCTA = prov.cuenta\n" +
                    "INNER JOIN \n" +
                    "nexco_counterparty_generic_contracts as con ON con.fuente_informacion = 'Diferencias conciliación'\n" +
                    "WHERE prov.periodo = :period \n" +
                    "GROUP BY prov.cuenta,centro,prov.contrato, con.concepto, con.cuenta,con.contrato, con.saldo ");

            insertInfo.setParameter("period", period);
            insertInfo.setParameter("period2", period.replace("-", "/") + "%");
            insertInfo.executeUpdate();

            Query update = entityManager.createNativeQuery("UPDATE nexco_descontabilizacion_report \n" +
                    "SET concepto = '1-DESCONT_CTAS_RENTA_FIJA' \n" +
                    "WHERE cuenta LIKE '13%' AND periodo = :period ");
            update.setParameter("period", period);
            update.executeUpdate();

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> validateDescon(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT * FROM nexco_descontabilizacion_report WHERE periodo = ?;");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public List<DescontabilizacionReport> getDescontabilizacionReport(String periodo) {
        List<DescontabilizacionReport> toReturn = new ArrayList<>();

        Query getQuery = entityManager.createNativeQuery("select * from nexco_descontabilizacion_report WHERE periodo = ?", DescontabilizacionReport.class);
        getQuery.setParameter(1, periodo);

        if(!getQuery.getResultList().isEmpty()){
            toReturn = getQuery.getResultList();
        }

        return toReturn;
    }

    public List<Object[]> getDescontabilizacionReportTxt(String periodo) {
        List<Object[]> toReturn = new ArrayList<>();

        Query getQuery = entityManager.createNativeQuery("SELECT centro,cuenta,divisa,contrato,concepto,round(convert(numeric(18,2),saldo),2) saldo\n" +
                "FROM nexco_descontabilizacion_report WHERE periodo = ? and round(convert(numeric(18,2),saldo),2) <> 0.00\n" +
                "UNION ALL\n" +
                "SELECT centro,contrapartida,divisa,contrato_generico,concepto,SUM(round(convert(numeric(18,2), saldo)*(-1), 2)) as saldo \n" +
                "FROM nexco_descontabilizacion_report WHERE periodo = ?\n" +
                "GROUP BY centro,contrapartida,divisa,contrato_generico, concepto \n" +
                "HAVING SUM(round(convert(numeric(18,2), saldo)*(-1), 2)) <> 0.00 \n" +
                ";");
        getQuery.setParameter(1, periodo);
        getQuery.setParameter(2, periodo);

        if(!getQuery.getResultList().isEmpty()){
            toReturn = getQuery.getResultList();
        }

        return toReturn;
    }

    public List<Object[]> getDesconFilter(String period){
        List<Object[]> toReturn = new ArrayList<>();

        Query getData = entityManager.createNativeQuery("SELECT fuente_informacion,cuenta,divisa,concepto, SUM(saldo), contrapartida, contrato_generico  FROM nexco_descontabilizacion_report  WHERE periodo = ? \n" +
                "GROUP BY fuente_informacion,cuenta, divisa,concepto, contrapartida, contrato_generico");
        getData.setParameter(1,period);

        if(!getData.getResultList().isEmpty()){
            toReturn = getData.getResultList();
        }

        return toReturn;
    }

    public List<Object[]> getDescontabilizacionTemplate(String periodo){
        Query getQuery = entityManager.createNativeQuery("select * from nexco_inventario_descontabilizacion WHERE periodo = ?");
        getQuery.setParameter(1,periodo);
        return getQuery.getResultList();
    }

    public void clearDescontabilizacionReport(User user, String period){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_descontabilizacion_report WHERE periodo = ?", DescontabilizacionReport.class);
        query.setParameter(1,period);
        query.executeUpdate();
    }

    public void clearDescontabilizacionTemplate(User user, String period){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_inventario_descontabilizacion WHERE periodo = ?", DescontabilizacionTemplate.class);
        query.setParameter(1,period);
        query.executeUpdate();
    }


}
