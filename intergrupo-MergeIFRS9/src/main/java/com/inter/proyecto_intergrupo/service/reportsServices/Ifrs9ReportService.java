package com.inter.proyecto_intergrupo.service.reportsServices;


import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.GenericsParametric;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Provisions;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.model.reports.Ifrs9Report;
import com.inter.proyecto_intergrupo.model.reports.ReportPrimary;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.repository.reports.Ifrs9ReportRepository;
import com.inter.proyecto_intergrupo.service.parametricServices.ProvisionsService;
import com.inter.proyecto_intergrupo.service.parametricServices.SubProductService;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class Ifrs9ReportService {

    @Autowired
    SubProductService subProductService;

    @Autowired
    ProvisionsService provisionsService;

    @Autowired
    Ifrs9ReportRepository ifrs9ReportRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    SendEmailService sendEmailService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    Logger log = LoggerFactory.getLogger(Ifrs9ReportService.class);

    public void loadIfrs9Report(String period, String valAjuste) throws ParseException {

        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM");

        Date actDate = formatter2.parse(period);
        Calendar act = Calendar.getInstance();
        act.setTime(actDate);

        Calendar prev = Calendar.getInstance();
        prev.setTime(actDate);
        prev.add(Calendar.MONTH,-1);
        Date resultPrev = prev.getTime();

        String periodoPrev = formatter2.format(resultPrev);
        String actPrev = formatter2.format(actDate);

        Query deleteVert = entityManager.createNativeQuery("delete from nexco_reporte_ifrs9 where periodo = ?;");
        deleteVert.setParameter(1,period);
        deleteVert.executeUpdate();

        Query deleteVert1 = entityManager.createNativeQuery("delete from nexco_reporte_ifrs9_final where periodo = ?;");
        deleteVert1.setParameter(1,period);
        deleteVert1.executeUpdate();

        getInfoMISSA(period);
        if(valAjuste.equals("A"))
        {
            getInfoMISA(period);
        }

        /*Query udpateVert1 = entityManager.createNativeQuery("update a\n" +
                "set a.codicons = b.CODICONS46\n" +
                "from nexco_reporte_ifrs9 a\n" +
                "inner join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on a.CVES_COD_CTACONT = b.nucta\n" +
                "where a.periodo = ? \n" +
                ";");
        udpateVert1.setParameter(1,period);
        udpateVert1.executeUpdate();*/

        Query udpateVert2 = entityManager.createNativeQuery("update a\n" +
                "set a.subproducto = b.subproducto\n" +
                "from nexco_reporte_ifrs9 a\n" +
                "inner join nexco_cuenta_subproducto_local b\n" +
                "on a.largo6 = b.cuenta\n" +
                "where a.periodo = ?  AND a.subproducto is null\n" +
                ";");
        udpateVert2.setParameter(1,period);
        udpateVert2.executeUpdate();

        //updateProvisones(period);
        //updateProvProc(period);

        /*Query udpateVert3 = entityManager.createNativeQuery("update a\n" +
                "set a.instrumento = b.instrumento, a.sector = b.sector, a.stage_espana = b.stage_espana, \n" +
                "a.producto_espana = b.producto_espana, a.signo = b.signo\n" +
                "from nexco_reporte_ifrs9 a\n" +
                "inner join nexco_provisiones b\n" +
                "on a.codicons = b.cuenta_neocon\n" +
                "where a.periodo = ? \n" +
                ";");
        udpateVert3.setParameter(1,period);
        udpateVert3.executeUpdate();*/

        //Sin Ajuste
        Query getResult1= entityManager.createNativeQuery("insert into nexco_reporte_ifrs9_final (consulta, instrumento, subproducto, stage1_ant, stage2_ant, stage3_ant, total_general_ant, stage1, stage2, stage3, total_general, variacion, periodo, type, local, tipo_registro) \n" +
                "select 'SALDO' origen, z.*, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'SA', 'S', '' \n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "group by isnull(y.origen, x.origen), z.instrumento, z.subproducto\n" +
                "UNION ALL\n" +
                "select 'SALDO' origen, 'TOTAL RIESGOS' instrumento, '' subproducto, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'SA', 'S', 'TOTAL' \n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "UNION ALL\n" +
                "select 'PROV' origen, z.*, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'SA', 'S', '' \n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "group by isnull(y.origen, x.origen), z.instrumento, z.subproducto\n" +
                "UNION ALL\n" +
                "select 'PROV' origen, 'TOTAL PROVISIÓN' instrumento, '' subproducto, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'SA', 'S', 'TOTAL'\n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "order by 1 desc, 2, 3\n" +
                ";");
        getResult1.setParameter(1,periodoPrev);
        getResult1.setParameter(2,"SA");
        getResult1.setParameter(3,period);
        getResult1.setParameter(4,"SA");
        getResult1.setParameter(5,periodoPrev);
        getResult1.setParameter(6,"SA");
        getResult1.setParameter(7,period);
        getResult1.setParameter(8, "SA");
        getResult1.setParameter(9, periodoPrev);
        getResult1.setParameter(10,"SA");
        getResult1.setParameter(11,period);
        getResult1.setParameter(12,"SA");
        getResult1.setParameter(13,periodoPrev);
        getResult1.setParameter(14,"SA");
        getResult1.setParameter(15,period);
        getResult1.setParameter(16,"SA");
        getResult1.executeUpdate();
        //

        //Ajuste
        Query getResult2= entityManager.createNativeQuery("insert into nexco_reporte_ifrs9_final (consulta, instrumento, subproducto, stage1_ant, stage2_ant, stage3_ant, total_general_ant, stage1, stage2, stage3, total_general, variacion, periodo, type, local, tipo_registro) \n" +
                "select 'SALDO' origen, z.*, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'A', 'S', '' \n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "group by isnull(y.origen, x.origen), z.instrumento, z.subproducto\n" +
                "UNION ALL\n" +
                "select 'SALDO' origen, 'TOTAL RIESGOS' instrumento, '' subproducto, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'A', 'S', 'TOTAL' \n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "UNION ALL\n" +
                "select 'PROV' origen, z.*, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'A', 'S', '' \n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "group by isnull(y.origen, x.origen), z.instrumento, z.subproducto\n" +
                "UNION ALL\n" +
                "select 'PROV' origen, 'TOTAL PROVISIÓN' instrumento, '' subproducto, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'A', 'S', 'TOTAL'\n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.cves_ind_stage_final = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, cves_ind_stage_final) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "order by 1 desc, 2, 3\n" +
                ";");
        getResult2.setParameter(1,periodoPrev);
        getResult2.setParameter(2,"A");
        getResult2.setParameter(3,period);
        getResult2.setParameter(4,"A");
        getResult2.setParameter(5,periodoPrev);
        getResult2.setParameter(6,"A");
        getResult2.setParameter(7,period);
        getResult2.setParameter(8, "A");
        getResult2.setParameter(9, periodoPrev);
        getResult2.setParameter(10,"A");
        getResult2.setParameter(11,period);
        getResult2.setParameter(12,"A");
        getResult2.setParameter(13,periodoPrev);
        getResult2.setParameter(14,"A");
        getResult2.setParameter(15,period);
        getResult2.setParameter(16,"A");
        getResult2.executeUpdate();
        //

        //HOLDING SA
        Query getResult3= entityManager.createNativeQuery("insert into nexco_reporte_ifrs9_final (consulta, instrumento, subproducto, stage1_ant, stage2_ant, stage3_ant, total_general_ant, stage1, stage2, stage3, total_general, variacion, periodo, type, local, tipo_registro) \n" +
                "select 'SALDO' origen, z.*, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'SA', 'N', ''\n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.stage_espana) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, stage_espana) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "group by isnull(y.origen, x.origen), z.instrumento, z.subproducto\n" +
                "UNION ALL\n" +
                "select 'SALDO' origen, 'TOTAL RIESGOS' instrumento, '' subproducto, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)))-(sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0))) variacion, '"+period+"', 'SA', 'N', 'TOTAL'\n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.stage_espana) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, stage_espana) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "UNION ALL\n" +
                "select 'PROV' origen, z.*, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'SA', 'N', ''\n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.stage_espana) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, stage_espana) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "group by isnull(y.origen, x.origen), z.instrumento, z.subproducto\n" +
                "UNION ALL\n" +
                "select 'PROV' origen, 'TOTAL PROVISIÓN' instrumento, '' subproducto, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'SA', 'N', 'TOTAL'\n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.stage_espana) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, stage_espana) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "order by 1 desc, 2, 3\n" +
                ";");
        getResult3.setParameter(1,periodoPrev);
        getResult3.setParameter(2,"SA");
        getResult3.setParameter(3,period);
        getResult3.setParameter(4,"SA");
        getResult3.setParameter(5,periodoPrev);
        getResult3.setParameter(6,"SA");
        getResult3.setParameter(7,period);
        getResult3.setParameter(8, "SA");
        getResult3.setParameter(9, periodoPrev);
        getResult3.setParameter(10,"SA");
        getResult3.setParameter(11,period);
        getResult3.setParameter(12,"SA");
        getResult3.setParameter(13,periodoPrev);
        getResult3.setParameter(14,"SA");
        getResult3.setParameter(15,period);
        getResult3.setParameter(16,"SA");
        getResult3.executeUpdate();
        //

        //HOLDING AJUSTE
        Query getResult4= entityManager.createNativeQuery("insert into nexco_reporte_ifrs9_final (consulta, instrumento, subproducto, stage1_ant, stage2_ant, stage3_ant, total_general_ant, stage1, stage2, stage3, total_general, variacion, periodo, type, local, tipo_registro) \n" +
                "select 'SALDO' origen, z.*, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'A', 'N', ''\n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.stage_espana) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, stage_espana) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "group by isnull(y.origen, x.origen), z.instrumento, z.subproducto\n" +
                "UNION ALL\n" +
                "select 'SALDO' origen, 'TOTAL RIESGOS' instrumento, '' subproducto, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)))-(sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0))) variacion, '"+period+"', 'A', 'N', 'TOTAL'\n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.stage_espana) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'SALDO'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, stage_espana) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "UNION ALL\n" +
                "select 'PROV' origen, z.*, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'A', 'N', ''\n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.stage_espana) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, stage_espana) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "group by isnull(y.origen, x.origen), z.instrumento, z.subproducto\n" +
                "UNION ALL\n" +
                "select 'PROV' origen, 'TOTAL PROVISIÓN' instrumento, '' subproducto, sum(isnull(y.stage1, 0)) stage1a, sum(isnull(y.stage2, 0)) stage2a, sum(isnull(y.stage3, 0)) stage3a, sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0)) totala \n" +
                ", sum(isnull(x.stage1, 0)) stage1b, sum(isnull(x.stage2, 0)) stage2b, sum(isnull(x.stage3, 0)) stage3b, sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)) totalb\n" +
                ", (sum(isnull(x.stage1, 0)+isnull(x.stage2, 0)+isnull(x.stage3, 0)))-(sum(isnull(y.stage1, 0)+isnull(y.stage2, 0)+isnull(y.stage3, 0))) variacion, '"+period+"', 'A', 'N', 'TOTAL'\n" +
                "from \n" +
                "(SELECT distinct a.instrumento, c.subproducto FROM nexco_provisiones a\n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on convert(varchar, a.cuenta_neocon) = b.CODICONS46\n" +
                "inner join nexco_cuenta_subproducto_local c\n" +
                "on substring(b.NUCTA, 1, 6) = c.cuenta) z\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, nri.stage_espana) a\n" +
                "group by origen, instrumento, subproducto) y\n" +
                "on z.instrumento = y.instrumento and z.subproducto = y.subproducto\n" +
                "left join \n" +
                "(\n" +
                "select  origen, instrumento, subproducto, SUM(stage1) stage1, SUM(stage2) stage2, SUM(stage3) stage3 from \n" +
                "(SELECT nri.origen, nri.instrumento, nri.subproducto, \n" +
                "CASE WHEN nri.stage_espana = '1' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage1, \n" +
                "CASE WHEN nri.stage_espana = '2' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage2, \n" +
                "CASE WHEN nri.stage_espana = '3' then CAST(SUM(nri.saldo) AS bigint) else 0 end stage3 \n" +
                "FROM nexco_reporte_ifrs9 AS nri \n" +
                "WHERE nri.periodo = ? AND nri.type = ? and nri.origen = 'PROV'\n" +
                "GROUP BY nri.origen, nri.instrumento, nri.subproducto, stage_espana) a\n" +
                "group by origen, instrumento, subproducto) x\n" +
                "on z.instrumento = x.instrumento and z.subproducto = x.subproducto\n" +
                "order by 1 desc, 2, 3\n" +
                ";");
        getResult4.setParameter(1,periodoPrev);
        getResult4.setParameter(2,"A");
        getResult4.setParameter(3,period);
        getResult4.setParameter(4,"A");
        getResult4.setParameter(5,periodoPrev);
        getResult4.setParameter(6,"A");
        getResult4.setParameter(7,period);
        getResult4.setParameter(8, "A");
        getResult4.setParameter(9, periodoPrev);
        getResult4.setParameter(10,"A");
        getResult4.setParameter(11,period);
        getResult4.setParameter(12,"A");
        getResult4.setParameter(13,periodoPrev);
        getResult4.setParameter(14,"A");
        getResult4.setParameter(15,period);
        getResult4.setParameter(16,"A");
        getResult4.executeUpdate();
        //


        Date today = new Date();
        String input = "VERTICAL-SALDOS";

        StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo(input, period);

        if (validateStatus == null) {
            StatusInfo status = new StatusInfo();
            status.setInput(input);
            status.setPeriodo(period);
            status.setFecha(today);
            StatusInfoRepository.save(status);
        } else {
            validateStatus.setFecha(today);
            StatusInfoRepository.save(validateStatus);
        }

        Query deleteVertAll = entityManager.createNativeQuery("delete from nexco_reporte_ifrs9 where periodo NOT IN (?,?);");
        deleteVertAll.setParameter(1,periodoPrev);
        deleteVertAll.setParameter(2,actPrev);
        deleteVertAll.executeUpdate();

        Query emails = entityManager.createNativeQuery("SELECT distinct A.* FROM preciso_administracion_usuarios A, preciso_administracion_perfiles B, preciso_administracion_user_rol C \n" +
                "WHERE A.usuario = C.usuario AND C.id_perfil = B.id_perfil AND B.nombre_perfil = 'Correo Vertical' and a.estado=1", User.class);
        List<User>listEmails = emails.getResultList();
        for (User u : listEmails)
        {
            sendEmail(u,periodoPrev,actPrev);
        }

    }

    public void updateProvProc(String periodo){
        Query query = entityManager.createNativeQuery("EXEC dbo.NEXCO_ACTUALIZAR_VERTICAL @periodo = ?");
        query.setParameter(1,periodo);
        query.executeUpdate();
    }

    public void updateProvisones(String periodo) {
        jdbcTemplate.batchUpdate(
                "update a \n" +
                "set a.instrumento = b.instrumento, a.sector = b.sector, a.stage_espana = b.stage_espana, \n" +
                        "a.producto_espana = b.producto_espana, a.signo = b.signo\n" +
                        "from nexco_reporte_ifrs9 a\n" +
                        "inner join nexco_provisiones b\n" +
                        "on a.codicons = b.cuenta_neocon\n" +
                        "where a.periodo = ? \n" +
                        ";",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, periodo);
                    }

                    public int getBatchSize() {
                        return 1;
                    }
                });

    }

    public void sendEmail(User userRecipientEmail, String periodo1,String periodo2) {
        String subject = "Notificación Ejecución Job Reducción Tabla Vertical de Saldos";

        String content = "<p>Se realizo la limpieza del histórico y solo se mantuvieron los periodos "+periodo1+" y "+periodo2+".</p>" +
                "<br>" +
                "<table>" +
                "   <thead>" +
                "       <tr>" +
                "           <th>Usuario</th>" +
                "           <th>Componente</th>" +
                "           <th>Input</th>" +
                "           <th>Periodo</th>" +
                "           <th>Detalle</th>" +
                "       </tr>" +
                "   </thead>" +
                "   <tbody>" +
                "       <td>SYSTEM JOB</td>" +
                "       <td>Conciliación IFRS9</td>" +
                "       <td>Vertical de Saldos</td>" +
                "       <td>"+periodo2+"</td>" +
                "       <td>Se ejecutó el proceso de limpieza de la tabla de vetical de saldos de forma exitosa.</td>" +
                "   </tbody>" +
                "</table>";

        sendEmailService.sendEmail(userRecipientEmail.getCorreo(), subject, content);
    }

    public List vaidateDataAjuste(String period)
    {
        try
        {
            Query udpateVert1Validation = entityManager.createNativeQuery("SELECT TOP 1 * FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_VERTICAL_SALDOS_AJUSTADO_" + period.replace("-", ""));
            return udpateVert1Validation.getResultList();
        }
        catch(Exception e)
        {
            return new ArrayList();
        }
    }
    public void getInfoMISSA(String period){

        try{

            /*Query insertVert = entityManager.createNativeQuery("INSERT INTO nexco_reporte_ifrs9 (centro, cves_cod_ctacont, cves_ind_stage_final, cves_cod_segm_finrep, cves_cod_ent_iuc, cves_cod_ccontr, saldo, periodo, type, largo6, origen)\n" +
                    "select cves_cod_cofici_con, CVES_COD_CTACONT, CVES_IND_STAGE_FINAL, CVES_COD_SEGM_FINREP, CVES_COD_ENT_IUC,\n" +
                    "CVES_COD_CCONTR, SUM(CONVERT(MONEY,CVES_IMP_SALDO_LOC)) SALDO, ?, 'SA', SUBSTRING(CVES_COD_CTACONT, 1, 6), 'SALDO'\n" +
                    "from [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_VERTICAL_SALDOS_"+period.replace("-","")+" \n" +
                    "WHERE  CVES_COD_CTACONT is not null AND CVES_COD_ENT_IUC = '0013'\n" +
                    "GROUP BY cves_cod_cofici_con, CVES_COD_CTACONT, CVES_IND_STAGE_FINAL, CVES_COD_SEGM_FINREP, CVES_COD_ENT_IUC, CVES_COD_CCONTR\n" +
                    ";");
            insertVert.setParameter(1,period);
            insertVert.executeUpdate();

            Query insertVertProv = entityManager.createNativeQuery("INSERT INTO nexco_reporte_ifrs9 (centro, cves_cod_ctacont, cves_ind_stage_final, cves_cod_segm_finrep, cves_cod_ent_iuc, cves_cod_ccontr, saldo, periodo, type, largo6, origen)\n" +
                    "select cves_cod_cofici_con, CVES_COD_CTACONT, CVES_IND_STAGE_FINAL, CVES_COD_SEGM_FINREP, CVES_COD_ENT_IUC,\n" +
                    "CVES_COD_CCONTR, SUM(CONVERT(MONEY,CVES_IMP_provrep_dl) ) PROV, ?, 'SA', SUBSTRING(CVES_COD_CTACONT, 1, 6), 'PROV'\n" +
                    "from [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_VERTICAL_SALDOS_"+period.replace("-","")+" \n" +
                    "WHERE CVES_IMP_PROVREP_DL not like '999999999999999.9%' AND CVES_COD_CTACONT is not null AND CVES_COD_ENT_IUC = '0013'\n" +
                    "GROUP BY cves_cod_cofici_con, CVES_COD_CTACONT, CVES_IND_STAGE_FINAL, CVES_COD_SEGM_FINREP, CVES_COD_ENT_IUC, CVES_COD_CCONTR\n" +
                    ";");
            insertVertProv.setParameter(1,period);
            insertVertProv.executeUpdate();*/

            Query insertVert = entityManager.createNativeQuery("INSERT INTO nexco_reporte_ifrs9 (centro, cves_cod_ctacont, cves_ind_stage_final, cves_cod_segm_finrep, cves_cod_ent_iuc, cves_cod_ccontr, saldo, periodo, type, largo6, origen, codicons,instrumento,producto_espana,sector,signo,stage_espana, subproducto)\n" +
                    "select A.cves_cod_cofici_con, A.CVES_COD_CTACONT, A.CVES_IND_STAGE_FINAL, A.CVES_COD_SEGM_FINREP, A.CVES_COD_ENT_IUC,\n" +
                    "A.CVES_COD_CCONTR, SUM(CONVERT(MONEY,A.CVES_IMP_SALDO_LOC)) SALDO, ?, 'SA', SUBSTRING(A.CVES_COD_CTACONT, 1, 6), 'SALDO',\n" +
                    "C.CODICONS46, B.instrumento, B.producto_espana, B.sector, B.signo, B.stage_espana, D.subproducto \n" +
                    "from [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_VERTICAL_SALDOS_"+period.replace("-","")+" A \n" +
                    "LEFT JOIN (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') C ON A.CVES_COD_CTACONT = C.NUCTA COLLATE Modern_Spanish_CI_AS \n" +
                    "LEFT JOIN (select cuenta, subproducto from nexco_cuenta_subproducto_local) D ON SUBSTRING(A.CVES_COD_CTACONT, 1, 6) = D.cuenta COLLATE Modern_Spanish_CI_AS \n" +
                    "LEFT JOIN (select distinct cuenta_neocon, instrumento, producto_espana, sector, signo, stage_espana from nexco_provisiones) B ON C.CODICONS46 = B.cuenta_neocon \n" +
                    "WHERE  A.CVES_COD_CTACONT is not null AND A.CVES_COD_ENT_IUC = '0013'\n" +
                    "GROUP BY A.cves_cod_cofici_con, A.CVES_COD_CTACONT, A.CVES_IND_STAGE_FINAL, A.CVES_COD_SEGM_FINREP, A.CVES_COD_ENT_IUC, A.CVES_COD_CCONTR, C.CODICONS46, B.instrumento, B.producto_espana, B.sector, B.signo, B.stage_espana, D.subproducto ;");
            insertVert.setParameter(1,period);
            insertVert.executeUpdate();

            Query insertVertProv = entityManager.createNativeQuery("INSERT INTO nexco_reporte_ifrs9 (centro, cves_cod_ctacont, cves_ind_stage_final, cves_cod_segm_finrep, cves_cod_ent_iuc, cves_cod_ccontr, saldo, periodo, type, largo6, origen, codicons,instrumento,producto_espana,sector,signo,stage_espana, subproducto)\n" +
                    "select cves_cod_cofici_con, CVES_COD_CTACONT, CVES_IND_STAGE_FINAL, CVES_COD_SEGM_FINREP, CVES_COD_ENT_IUC,\n" +
                    "CVES_COD_CCONTR, SUM(CONVERT(MONEY,CVES_IMP_provrep_dl) ) PROV, ?, 'SA', SUBSTRING(CVES_COD_CTACONT, 1, 6), 'PROV',\n" +
                    "C.CODICONS46, B.instrumento, B.producto_espana, B.sector, B.signo, B.stage_espana, D.subproducto\n" +
                    "from [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_VERTICAL_SALDOS_"+period.replace("-","")+" A \n" +
                    "LEFT JOIN (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') C ON A.CVES_COD_CTACONT = C.NUCTA COLLATE Modern_Spanish_CI_AS \n" +
                    "LEFT JOIN (select cuenta, subproducto from nexco_cuenta_subproducto_local) D ON SUBSTRING(A.CVES_COD_CTACONT, 1, 6) = D.cuenta COLLATE Modern_Spanish_CI_AS \n" +
                    "LEFT JOIN (select distinct cuenta_neocon, instrumento, producto_espana, sector, signo, stage_espana from nexco_provisiones) B ON C.CODICONS46 = B.cuenta_neocon\n" +
                    "WHERE CVES_IMP_PROVREP_DL not like '999999999999999.9%' AND CVES_COD_CTACONT is not null AND CVES_COD_ENT_IUC = '0013'\n" +
                    "GROUP BY A.cves_cod_cofici_con, A.CVES_COD_CTACONT, A.CVES_IND_STAGE_FINAL, A.CVES_COD_SEGM_FINREP, A.CVES_COD_ENT_IUC, A.CVES_COD_CCONTR, C.CODICONS46, B.instrumento, B.producto_espana, B.sector, B.signo, B.stage_espana,D.subproducto;");
            insertVertProv.setParameter(1,period);
            insertVertProv.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void getInfoMISA(String period){
        //try{

            /*Query insertVertA = entityManager.createNativeQuery("INSERT INTO nexco_reporte_ifrs9 (centro, cves_cod_ctacont, cves_ind_stage_final, cves_cod_segm_finrep, cves_cod_ent_iuc, cves_cod_ccontr, saldo, periodo, type, largo6, origen)\n" +
                    "select cves_cod_cofici_con, CVES_COD_CTACONT, CVES_IND_STAGE_FINAL, CVES_COD_SEGM_FINREP_NEW, CVES_COD_ENT_IUC, CVES_COD_CCONTR, SUM(CONVERT(MONEY,CVES_IMP_SALDO_LOC) ) SALDO,\n" +
                    "?, 'A', SUBSTRING(CVES_COD_CTACONT, 1, 6), 'SALDO' \n" +
                    "from [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_VERTICAL_SALDOS_AJUSTADO_"+period.replace("-","")+" \n" +
                    "WHERE  CVES_COD_CTACONT is not null AND CVES_COD_ENT_IUC = '0013'\n" +
                    "GROUP BY cves_cod_cofici_con, CVES_COD_CTACONT, CVES_IND_STAGE_FINAL, CVES_COD_SEGM_FINREP_NEW, CVES_COD_ENT_IUC, CVES_COD_CCONTR\n" +
                    ";");
            insertVertA.setParameter(1,period);
            insertVertA.executeUpdate();

            Query insertVertProvA = entityManager.createNativeQuery("INSERT INTO nexco_reporte_ifrs9 (centro, cves_cod_ctacont, cves_ind_stage_final, cves_cod_segm_finrep, cves_cod_ent_iuc, cves_cod_ccontr, saldo, periodo, type, largo6, origen)\n" +
                    "select cves_cod_cofici_con, CVES_COD_CTACONT, CVES_IND_STAGE_FINAL, CVES_COD_SEGM_FINREP_NEW, CVES_COD_ENT_IUC, CVES_COD_CCONTR, SUM(CONVERT(MONEY,CVES_IMP_provrep_dl) ) PROV,\n" +
                    "?, 'A', SUBSTRING(CVES_COD_CTACONT, 1, 6), 'PROV' \n" +
                    "from [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_VERTICAL_SALDOS_AJUSTADO_"+period.replace("-","")+" \n" +
                    "WHERE CVES_IMP_PROVREP_DL not like '999999999999999.9%' AND CVES_COD_CTACONT is not null AND CVES_COD_ENT_IUC = '0013'\n" +
                    "GROUP BY cves_cod_cofici_con, CVES_COD_CTACONT, CVES_IND_STAGE_FINAL, CVES_COD_SEGM_FINREP_NEW, CVES_COD_ENT_IUC, CVES_COD_CCONTR\n" +
                    ";");
            insertVertProvA.setParameter(1,period);
            insertVertProvA.executeUpdate();*/

        Query insertVertA = entityManager.createNativeQuery("INSERT INTO nexco_reporte_ifrs9 (centro, cves_cod_ctacont, cves_ind_stage_final, cves_cod_segm_finrep, cves_cod_ent_iuc, cves_cod_ccontr, saldo, periodo, type, largo6, origen, codicons,instrumento,producto_espana,sector,signo,stage_espana)\n" +
                "select A.cves_cod_cofici_con, A.CVES_COD_CTACONT, A.CVES_IND_STAGE_FINAL, A.CVES_COD_SEGM_FINREP_NEW, A.CVES_COD_ENT_IUC, A.CVES_COD_CCONTR, SUM(CONVERT(MONEY,A.CVES_IMP_SALDO_LOC) ) SALDO,\n" +
                "?, 'A', SUBSTRING(A.CVES_COD_CTACONT, 1, 6), 'SALDO', C.CODICONS46, B.instrumento, B.producto_espana, B.sector, B.signo, B.stage_espana \n" +
                "from [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_VERTICAL_SALDOS_AJUSTADO_"+period.replace("-","")+" A \n" +
                "LEFT JOIN (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') C ON A.CVES_COD_CTACONT = C.NUCTA COLLATE Modern_Spanish_CI_AS\n" +
                "LEFT JOIN (select distinct cuenta_neocon, instrumento, producto_espana, sector, signo, stage_espana from nexco_provisiones) B ON C.CODICONS46 = B.cuenta_neocon\n" +
                "WHERE A.CVES_COD_CTACONT is not null AND A.CVES_COD_ENT_IUC = '0013'\n" +
                "GROUP BY A.cves_cod_cofici_con, A.CVES_COD_CTACONT, A.CVES_IND_STAGE_FINAL, A.CVES_COD_SEGM_FINREP_NEW, A.CVES_COD_ENT_IUC, A.CVES_COD_CCONTR, C.CODICONS46, B.instrumento, B.producto_espana, B.sector, B.signo, B.stage_espana ;");
        insertVertA.setParameter(1,period);
        insertVertA.executeUpdate();

        Query insertVertProvA = entityManager.createNativeQuery("INSERT INTO nexco_reporte_ifrs9 (centro, cves_cod_ctacont, cves_ind_stage_final, cves_cod_segm_finrep, cves_cod_ent_iuc, cves_cod_ccontr, saldo, periodo, type, largo6, origen, codicons,instrumento,producto_espana,sector,signo,stage_espana)\n" +
                "select A.cves_cod_cofici_con, A.CVES_COD_CTACONT, A.CVES_IND_STAGE_FINAL, A.CVES_COD_SEGM_FINREP_NEW, A.CVES_COD_ENT_IUC, A.CVES_COD_CCONTR, SUM(CONVERT(MONEY,A.CVES_IMP_provrep_dl) ) PROV,\n" +
                "?, 'A', SUBSTRING(A.CVES_COD_CTACONT, 1, 6), 'PROV', C.CODICONS46, B.instrumento, B.producto_espana, B.sector, B.signo, B.stage_espana \n" +
                "from [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_VERTICAL_SALDOS_AJUSTADO_"+period.replace("-","")+" A \n" +
                "LEFT JOIN (select NUCTA, CODICONS46 from CUENTAS_PUC where empresa = '0013') C ON A.CVES_COD_CTACONT = C.NUCTA COLLATE Modern_Spanish_CI_AS\n" +
                "LEFT JOIN (select distinct cuenta_neocon, instrumento, producto_espana, sector, signo, stage_espana from nexco_provisiones) B ON C.CODICONS46 = B.cuenta_neocon\n" +
                "WHERE A.CVES_IMP_PROVREP_DL not like '999999999999999.9%' AND A.CVES_COD_CTACONT is not null AND A.CVES_COD_ENT_IUC = '0013'\n" +
                "GROUP BY A.cves_cod_cofici_con, A.CVES_COD_CTACONT, A.CVES_IND_STAGE_FINAL, A.CVES_COD_SEGM_FINREP_NEW, A.CVES_COD_ENT_IUC, A.CVES_COD_CCONTR, C.CODICONS46, B.instrumento, B.producto_espana, B.sector, B.signo, B.stage_espana ;");
        insertVertProvA.setParameter(1,period);
        insertVertProvA.executeUpdate();

    }

    /*public List<String[]> getIfrs9ReportPrimary(String periodo, String type, String local){

        Query queryValidate = entityManager.createNativeQuery("SELECT consulta, instrumento, subproducto, stage1_ant, stage2_ant, stage3_ant, total_general_ant, stage1, stage2, stage3, total_general, variacion, periodo, tipo_registro FROM nexco_reporte_ifrs9_final WHERE periodo = ? and type = ? and local = ?;");
        queryValidate.setParameter(1, periodo);
        queryValidate.setParameter(2, type);
        queryValidate.setParameter(3, local);

        return queryValidate.getResultList();
    }*/

    public List<String[]> getIfrs9ReportPrimary(String periodo, String type, String local){

        String periodo2 = "";
        try {
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM");
            Date fecha = formato.parse(periodo);
            Calendar c = Calendar.getInstance();
            c.setTime(fecha);
            c.add(Calendar.MONTH,-1);
            periodo2 = formato.format(c.getTime());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Query queryValidate = entityManager.createNativeQuery("SELECT A.consulta, A.instrumento, A.subproducto, ISNULL(B.stage1,0) AS totals1, ISNULL(B.stage2,0) AS totals2, ISNULL(B.stage3,0) AS totals3, ISNULL(B.total_general,0) AS totalG, A.stage1, A.stage2, A.stage3, A.total_general, A.total_general-ISNULL(B.total_general,0) as VARIA, A.periodo, A.tipo_registro \n" +
                "FROM (SELECT * FROM nexco_reporte_ifrs9_final WHERE periodo = ? and type = ? and local = ?) A\n" +
                "LEFT JOIN (SELECT * FROM nexco_reporte_ifrs9_final WHERE periodo = ? and type = ? and local = ?) B ON A.consulta = B.consulta and A.instrumento = B.instrumento and A.subproducto = B.subproducto");
        queryValidate.setParameter(1, periodo);
        queryValidate.setParameter(2, type);
        queryValidate.setParameter(3, local);
        queryValidate.setParameter(4, periodo2);
        queryValidate.setParameter(5, type);
        queryValidate.setParameter(6, local);

        return queryValidate.getResultList();
    }


    public List<Object[]> findAllExport(String type, String period){
        javax.persistence.Query getResult= entityManager.createNativeQuery("select cves_cod_ctacont, cves_cod_ent_iuc, cves_cod_segm_finrep, cves_ind_stage_final, largo6, type, \n" +
                "codicons, instrumento, origen, periodo, producto_espana, sector, signo, stage_espana, subproducto, sum(saldo) saldo\n" +
                "from nexco_reporte_ifrs9 \n" +
                "where periodo = ? and type = ?\n" +
                "group by cves_cod_ctacont, cves_cod_ent_iuc, cves_cod_segm_finrep, cves_ind_stage_final, largo6, type, \n" +
                "codicons, instrumento, origen, periodo, producto_espana, sector, signo, stage_espana, subproducto");
        getResult.setParameter(1,period);
        getResult.setParameter(2,type);
        return getResult.getResultList();
    }

    public List<Object[]> findAllExportFilter(String type, String period, String center){
        javax.persistence.Query getResult= entityManager.createNativeQuery("select centro, cves_cod_ccontr, cves_cod_ctacont, cves_cod_ent_iuc, cves_cod_segm_finrep, cves_ind_stage_final, largo6, type, \n" +
                "codicons, instrumento, origen, periodo, producto_espana, sector, signo, stage_espana, subproducto, saldo\n" +
                "from nexco_reporte_ifrs9 \n" +
                "where periodo = ? and type = ? and centro = ?\n" +
                ";");

        getResult.setParameter(1,period);
        getResult.setParameter(2,type);
        getResult.setParameter(3,center);
        return getResult.getResultList();
    }

    public List validateTableVert(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT top 1 id_reporte FROM nexco_reporte_ifrs9_final WHERE periodo = ?;");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

}
