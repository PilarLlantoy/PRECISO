package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelJobs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.OnePercent;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.ControlPanelJobsRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelJobsService;
import com.inter.proyecto_intergrupo.service.informationServices.OnePercentDatesServices;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.xmlgraphics.util.DateFormatUtil;
import org.codehaus.groovy.transform.SourceURIASTTransformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.swing.text.DateFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class OnePercentService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    OnePercentDatesServices onePercentDatesServices;

    @Autowired
    ControlPanelJobsRepository controlPanelJobsRepository;

    @Autowired
    ControlPanelJobsService controlPanelJobsService;

    @Scheduled(cron = "0 30 10 * * * ")
    public void executeJobMAuto() throws ParseException {

        Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando_jobs as em WHERE em.nombre = 'Calculo 1%' AND em.estado = 1", ControlPanelJobs.class);
        if(!query1.getResultList().isEmpty()) {
            Date today = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", new Locale("es", "ES"));
            String todayDate = formatter.format(today);

            Calendar prev = Calendar.getInstance();
            prev.setTime(today);
            prev.add(Calendar.MONTH, -1);
            Date resultPrev = prev.getTime();

            String lateDate = formatter.format(resultPrev);

            Query searchDate = entityManager.createNativeQuery("SELECT fecha_corte, version FROM nexco_fechas_porc WHERE fechas = ?");
            searchDate.setParameter(1, todayDate);

            ControlPanelJobs job = controlPanelJobsService.findByIdJob(8);
            job.setFechaEjecucion(new Date());

            if(searchDate.getResultList().isEmpty())
            {
                onePercentDatesServices.generateDates(todayDate.substring(0,7));
                onePercentDatesServices.generateDates(lateDate.substring(0,7));
            }
            if(!searchDate.getResultList().isEmpty()){
                createOrUpdateTable(null);
                auditCode("Ejecuciòn exitosa Job Calculo 1%",null);
                job.setFechaEjecucionExitosa(new Date());
            }
            controlPanelJobsService.save(job);
        }
    }

    @Scheduled(cron = "0 30 17 * * * ")
    public void executeJobMAuto2() throws ParseException {

        Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando_jobs as em WHERE em.nombre = 'Calculo 1%' AND em.estado = 1", ControlPanelJobs.class);
        if(!query1.getResultList().isEmpty()) {
            Date today = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", new Locale("es", "ES"));
            String todayDate = formatter.format(today);

            Calendar prev = Calendar.getInstance();
            prev.setTime(today);
            prev.add(Calendar.MONTH, -1);
            Date resultPrev = prev.getTime();

            String lateDate = formatter.format(resultPrev);

            Query searchDate = entityManager.createNativeQuery("SELECT fecha_corte, version FROM nexco_fechas_porc WHERE fechas = ?");
            searchDate.setParameter(1, todayDate);

            ControlPanelJobs job = controlPanelJobsService.findByIdJob(8);
            job.setFechaEjecucion(new Date());

            if(searchDate.getResultList().isEmpty())
            {
                onePercentDatesServices.generateDates(todayDate.substring(0,7));
                onePercentDatesServices.generateDates(lateDate.substring(0,7));
            }
            if(!searchDate.getResultList().isEmpty()){
                createOrUpdateTable(null);
                auditCode("Ejecuciòn exitosa Job Calculo 1%",null);
                job.setFechaEjecucionExitosa(new Date());
            }
            controlPanelJobsService.save(job);
        }
    }

    public boolean executeJobManual(User user) throws ParseException {

        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd",new Locale("es", "ES"));
        String todayDate = formatter.format(today);

        Calendar prev = Calendar.getInstance();
        prev.setTime(today);
        prev.add(Calendar.MONTH,-1);
        Date resultPrev = prev.getTime();

        String lateDate = formatter.format(resultPrev);

        Query searchDate = entityManager.createNativeQuery("SELECT fecha_corte, version FROM nexco_fechas_porc WHERE fechas = ?");
        searchDate.setParameter(1,todayDate);

        if(searchDate.getResultList().isEmpty())
        {
            onePercentDatesServices.generateDates(todayDate.substring(0,7));
            onePercentDatesServices.generateDates(lateDate.substring(0,7));
        }
        if(!searchDate.getResultList().isEmpty()){
            createOrUpdateTable(null);
            ControlPanelJobs job = controlPanelJobsService.findByIdJob(8);
            job.setFechaEjecucion(new Date());
            job.setFechaEjecucionExitosa(new Date());
            controlPanelJobsService.save(job);
            auditCode("Ejecución forzada exitosa Job Calulo 1%",user);
            return true;
        }
        else
        {
            ControlPanelJobs job = controlPanelJobsService.findByIdJob(8);
            job.setFechaEjecucion(new Date());
            controlPanelJobsService.save(job);
            auditCode("Ejecución forzada Job Calulo 1%",user);
            return false;
        }
    }


    public boolean createOrUpdateTable(User user) throws ParseException {
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd",new Locale("es", "ES"));
        String todayDate = formatter.format(today);

        String periodoContable = "";
        String version = "";

        Query searchDate = entityManager.createNativeQuery("SELECT fecha_corte, version FROM nexco_fechas_porc WHERE fechas = ?");
        searchDate.setParameter(1,todayDate);

        if(!searchDate.getResultList().isEmpty()){
            Object[] result = (Object[]) searchDate.getResultList().get(0);

            periodoContable = result[0].toString();
            version = result[1].toString();
        }
        else{
            return false;
        }

        if(!periodoContable.isEmpty() && !version.isEmpty()){
            if(version.equals("1")){
                loadTable(periodoContable.substring(0,7));
            } else{
                updateTable(periodoContable.substring(0,7), version);
            }
            auditCode("Ejecución generar versión "+version+" Calulo 1%",user);
        }
        return true;

    }

    public void updateTable(String periodo, String version) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM-yy",new Locale("es", "ES"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");

        Date originalDate = dateFormat.parse(periodo);

        String currentMonth = formatter.format(originalDate);

        String mesActual = "";

        if(currentMonth.substring(0,4).equals("sept")) {
            currentMonth = currentMonth.replace("t", "");
        }

        Query getColumns = entityManager.createNativeQuery("SELECT Mes FROM FECHA_EQUIVALENTE WHERE REPLACE(LOWER(EQUIV),' ','') = ?");
        getColumns.setParameter(1,currentMonth);

        if(!getColumns.getResultList().isEmpty()){
            mesActual = getColumns.getResultList().get(0).toString().toLowerCase();
        }

        String ver = "version_"+version.trim();
        int versionInt = Integer.parseInt(version);

        //Crear Tabla Temporal
        Query dropTemporal = entityManager.createNativeQuery("DROP TABLE nexco_porcentaje_calculado_temp2");
        dropTemporal.executeUpdate();

        Query createTemporal = entityManager.createNativeQuery("SELECT puc.CODICONS46 as cod_neocon, a.cartera as cartera,a.epigrafe as epigrafe, a.calificacion as calificacion, a.nucta as nucta, a.oficina as oficina, \n" +
                "a.mes1 as "+ver+",(a.porcentaje_calc/100) as porcentaje_calculado, 'Provisión General Capitales' as fuente_info, :periodo as fecha_corte \n" +
                "INTO nexco_porcentaje_calculado_temp2 \n" +
                "FROM \n" +
                "(SELECT * FROM (SELECT  "+mesActual+" as mes1 ,TRIM(nucta) as nucta, epigrafe as epigrafe, oficina as oficina FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.S2_MES_P3 WHERE TipoRegistro = '001') as s2 \n" +
                "INNER JOIN (SELECT * FROM nexco_parametrica_genericas WHERE fuente_info = 'PROV_GENE' AND clase = 'CAPITAL') as gen \n" +
                "ON s2.nucta collate Modern_Spanish_CI_AS like gen.cuenta+'%') as a \n" +
                "INNER JOIN (SELECT NUCTA, CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') as puc ON puc.nucta = a.nucta collate Modern_Spanish_CI_AS ");

        createTemporal.setParameter("periodo",periodo.trim());
        createTemporal.executeUpdate();


        //Actualizar Datos

        Query updateTable = entityManager.createNativeQuery("MERGE nexco_porcentaje_calculado AS TARGET\n" +
                "USING nexco_porcentaje_calculado_temp2 AS SOURCE\n" +
                "ON (TARGET.nucta = SOURCE.nucta collate Modern_Spanish_CI_AS AND TARGET.oficina = SOURCE.oficina collate Modern_Spanish_CI_AS AND TARGET.fecha_corte = SOURCE.fecha_corte)\n" +
                "WHEN MATCHED THEN \n" +
                "\tUPDATE \n" +
                "\tSET TARGET."+ver+" = SOURCE."+ver+", \n" +
                "\tTARGET.oficina = SOURCE.oficina\n" +
                "WHEN NOT MATCHED BY TARGET THEN\n" +
                "\tINSERT (cod_neocon, cartera, epigrafe, calificacion, nucta, oficina, "+ver+", porcentaje_calculado, fuente_info, fecha_corte) \n" +
                "\tVALUES (SOURCE.cod_neocon, SOURCE.cartera, SOURCE.epigrafe, SOURCE.calificacion, SOURCE.nucta, SOURCE.oficina, SOURCE."+ver+", SOURCE.porcentaje_calculado, SOURCE.fuente_info, SOURCE.fecha_corte);");

        updateTable.executeUpdate();

        //Actualizar Calculos

            Query updateActualBalance = entityManager.createNativeQuery("UPDATE nexco_porcentaje_calculado\n" +
                "SET version_"+versionInt+" = 0 \n" +
                "WHERE version_"+versionInt+" IS NULL AND fecha_corte = :periodo");
        updateActualBalance.setParameter("periodo",periodo);
        updateActualBalance.executeUpdate();


        StringBuilder condition = new StringBuilder("1=1");
        StringBuilder update = new StringBuilder();
        for(int i = 1;i <=versionInt-1; i++){
            if(i == 1){
                condition = new StringBuilder("version_0 IS NULL AND version_1 IS NULL AND variacion_1 IS NULL AND calculo_1 IS NULL \n");
            }else{
                condition
                        .append(" AND version_").append(i)
                        .append(" IS NULL AND variacion_").append(i)
                        .append(" IS NULL AND calculo_").append(i)
                        .append(" IS NULL \n");
            }
        }

        for(int i = 1;i <=versionInt-1; i++){
            if(i == 1){
                update = new StringBuilder("version_0 = 0,  version_1 = 0, variacion_1 = 0, calculo_1 = 0 \n");
            }else{
                update
                        .append(",version_").append(i)
                        .append(" = 0 ,variacion_").append(i)
                        .append(" = 0 ,calculo_").append(i)
                        .append(" = 0 ");
            }
        }

        if(update.isEmpty()==false) {
            Query updateBalance = entityManager.createNativeQuery("UPDATE nexco_porcentaje_calculado\n" +
                    "SET " + update + " \n" +
                    "WHERE " + condition + " AND fecha_corte = :periodo");
            updateBalance.setParameter("periodo", periodo);
            updateBalance.executeUpdate();
        }

        Query updateDiference = entityManager.createNativeQuery("UPDATE nexco_porcentaje_calculado\n" +
                "SET variacion_"+versionInt+" = (version_"+versionInt+" - version_"+(versionInt-1)+") " +
                "WHERE fecha_corte = :periodo");
        updateDiference.setParameter("periodo", periodo);
        updateDiference.executeUpdate();

        Query updateCalc = entityManager.createNativeQuery("UPDATE nexco_porcentaje_calculado\n" +
                "SET calculo_"+versionInt+" = ROUND(variacion_"+versionInt+" * porcentaje_calculado,2) " +
                "WHERE fecha_corte = :periodo");
        updateCalc.setParameter("periodo", periodo);
        updateCalc.executeUpdate();

        Query updateAccounts = entityManager.createNativeQuery("UPDATE nexco_porcentaje_calculado\n" +
                "SET cuenta_balance = genBal.cuenta,\n" +
                "cuenta_pyg = CASE WHEN calculo_"+versionInt+" >= 0 THEN genProvPos.cuenta ELSE genProvNeg.cuenta END \n" +
                "FROM\n" +
                "nexco_porcentaje_calculado AS porc\n" +
                "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'BALANCE' AND gen.fuente_info = 'PROV_GENE' AND clase = 'PROVISION') as genBal ON genBal.cartera = porc.cartera AND genBal.calificacion = porc.calificacion\n" +
                "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'PYG' AND indicador ='+' AND gen.fuente_info = 'PROV_GENE' AND clase = 'PROVISION') as genProvPos ON porc.cartera = genProvPos.cartera AND porc.calificacion = genProvPos.calificacion\n" +
                "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'PYG' AND indicador ='-' AND gen.fuente_info = 'PROV_GENE' AND clase = 'PROVISION') as genProvNeg ON porc.cartera = genProvNeg.cartera AND porc.calificacion = genProvNeg.calificacion\n" +
                "WHERE (porc.cuenta_balance IS NULL OR porc.cuenta_pyg IS NULL) AND porc.fecha_corte = :periodo");
        updateAccounts.setParameter("periodo", periodo);
        updateAccounts.executeUpdate();

        Date today= new Date();
        String fechaFinal = DateFormatUtils.format(today,"yyyy-MM-dd");

        Query updateActualBalance3 = entityManager.createNativeQuery("UPDATE nexco_porcentaje_calculado\n" +
                "SET fecha_creacion = :fechaFinal \n" +
                "WHERE fecha_corte = :periodo");
        updateActualBalance3.setParameter("periodo",periodo);
        updateActualBalance3.setParameter("fechaFinal",fechaFinal);
        updateActualBalance3.executeUpdate();

    }

    public void loadTable(String periodo) throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("MMM-yy",new Locale("es", "ES"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");

        Date originalDate = dateFormat.parse(periodo);

        Calendar c = Calendar.getInstance();
        c.setTime(originalDate);
        c.add(Calendar.MONTH,-1);
        Date resultAfter  = c.getTime();

        String currentMonth = formatter.format(originalDate);
        String lastMonth = formatter.format(resultAfter);

        String mesActual = "";
        String mesAnterior = "";

        if(currentMonth.substring(0,4).equals("sept")) {
            currentMonth = currentMonth.replace("t", "");
        }

        if(lastMonth.substring(0,4).equals("sept")) {
            lastMonth = lastMonth.replace("t", "");
        }
        System.out.println(currentMonth);
        System.out.println(lastMonth);

        Query getColumns = entityManager.createNativeQuery("SELECT Mes FROM FECHA_EQUIVALENTE WHERE REPLACE(LOWER(EQUIV),' ','') = ? OR REPLACE(LOWER(EQUIV),' ','') = ? order by mes");
        getColumns.setParameter(1,currentMonth);
        getColumns.setParameter(2,lastMonth);

        if(!getColumns.getResultList().isEmpty()){
            mesActual = getColumns.getResultList().get(0).toString().toLowerCase();
            mesAnterior = getColumns.getResultList().get(1).toString().toLowerCase();
            System.out.println("MES ACTUAL: "+mesActual);
            System.out.println("MES ANTERIOR: "+mesAnterior);
        }

        Query dropTemporal = entityManager.createNativeQuery("DROP TABLE IF EXISTS nexco_porcentaje_calculado_temp");
        dropTemporal.executeUpdate();

        Query insertIntoTemporal = entityManager.createNativeQuery("SELECT puc.CODICONS46 as codicons, a.cartera as cartera,a.epigrafe as epigrafe, a.calificacion as calificacion, a.nucta as cuenta, a.oficina as centro, \n" +
                "a.mes2 as 'version0', a.mes1 as 'version1', ROUND((a.mes1- a.mes2),2) as 'variacion1', \n" +
                "ROUND((a.mes1-a.mes2) * (a.porcentaje_calc/100),2) as 'calculo1',(a.porcentaje_calc/100) as 'porcentaje'\n" +
                "INTO nexco_porcentaje_calculado_temp\n" +
                "FROM\n" +
                "(SELECT * FROM (SELECT "+mesActual+" as mes1, "+mesAnterior+" as mes2, TRIM(nucta) as nucta, epigrafe as epigrafe, oficina as oficina FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.S2_MES_P3 WHERE TipoRegistro = '001') as s2\n" +
                "INNER JOIN (SELECT * FROM nexco_parametrica_genericas WHERE fuente_info = 'PROV_GENE' AND clase = 'CAPITAL') as gen\n" +
                "ON s2.nucta collate Modern_Spanish_CI_AS like gen.cuenta+'%') as a\n" +
                "INNER JOIN (SELECT NUCTA, CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') as puc ON puc.nucta = a.nucta collate Modern_Spanish_CI_AS");
        insertIntoTemporal.executeUpdate();

        Query deleteFromFinal = entityManager.createNativeQuery("DELETE FROM nexco_porcentaje_calculado WHERE fecha_corte = ?");
        deleteFromFinal.setParameter(1,periodo);
        deleteFromFinal.executeUpdate();

        Date today= new Date();
        String parserString = DateFormatUtils.format(today,"yyyy-MM-dd");

        Query insertIntoFinal = entityManager.createNativeQuery("INSERT INTO nexco_porcentaje_calculado \n" +
                "(cod_neocon,cartera,epigrafe,calificacion,nucta,oficina,version_0,version_1,variacion_1,calculo_1,porcentaje_calculado,cuenta_balance,cuenta_pyg,fuente_info,fecha_corte,fecha_creacion)\n" +
                "SELECT  \n" +
                "temp.codicons,temp.cartera,temp.epigrafe,temp.calificacion,temp.cuenta,temp.centro,temp.version0,temp.version1,temp.variacion1,temp.calculo1,temp.porcentaje,\n" +
                "genBal.cuenta 'Cuenta Balance',\n" +
                "CASE WHEN temp.calculo1 >= 0 THEN genProvPos.cuenta ELSE genProvNeg.cuenta END 'Cuenta PYG',\n" +
                "'Provisión General Capitales',\n" +
                "'"+periodo+"','"+parserString+"'\n" +
                "FROM \n" +
                "nexco_porcentaje_calculado_temp as temp\n" +
                "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'BALANCE' AND gen.fuente_info = 'PROV_GENE' AND clase = 'PROVISION') as genBal ON genBal.cartera = temp.cartera AND genBal.calificacion = temp.calificacion\n" +
                "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'PYG' AND indicador ='+' AND gen.fuente_info = 'PROV_GENE' AND clase = 'PROVISION') as genProvPos ON temp.cartera = genProvPos.cartera AND temp.calificacion = genProvPos.calificacion\n" +
                "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'PYG' AND indicador ='-' AND gen.fuente_info = 'PROV_GENE' AND clase = 'PROVISION') as genProvNeg ON temp.cartera = genProvNeg.cartera AND temp.calificacion = genProvNeg.calificacion");

        insertIntoFinal.executeUpdate();
    }

    public ArrayList<OnePercent> getData (String periodo){
        ArrayList<OnePercent> toReturn = new ArrayList<>();

        Query getData = entityManager.createNativeQuery("SELECT * FROM nexco_porcentaje_calculado WHERE fecha_corte = ?", OnePercent.class);
        getData.setParameter(1,periodo);

        if(!getData.getResultList().isEmpty()){
            toReturn = (ArrayList<OnePercent>) getData.getResultList();
        }

        return toReturn;
    }

    public ArrayList<OnePercent> getTopRegisters (String periodo){
        ArrayList<OnePercent> toReturn = new ArrayList<>();

        Query getData = entityManager.createNativeQuery("SELECT TOP 1000 * FROM nexco_porcentaje_calculado WHERE fecha_corte = ?", OnePercent.class);
        getData.setParameter(1,periodo);

        if(!getData.getResultList().isEmpty()){
            toReturn = (ArrayList<OnePercent>) getData.getResultList();
        }

        return toReturn;
    }

    public List<Object[]> generateMassiveCharge(String period, String version){

        /*Query updatCenters = entityManager.createNativeQuery("UPDATE a \n" +
                "SET a.oficina = b.OFINEGOCIO\n" +
                "FROM nexco_porcentaje_calculado a, [82.255.50.134].DB_FINAN_NUEVA.dbo.OFI_GRUPO b\n" +
                "WHERE a.oficina = b.OFICINA COLLATE SQL_Latin1_General_CP1_CI_AS and a.fecha_corte = ? and b.FECHA_CIERRE is not null and b.FECHA_CIERRE!='' and b.OFINEGOCIO !='0000'");
        updatCenters.setParameter(1,period);
        updatCenters.executeUpdate();*/

        List<Object[]> result = new ArrayList<>();

        Query getData = entityManager.createNativeQuery("SELECT ISNULL(A.OFINEGOCIO, porc.oficina) oficinaA,cuenta_balance, 'COP' as divisa, '001302039000000103' as contrato, '' as referencia,\n" +
                "CONVERT(numeric(18,2),SUM(calculo_"+version+"*-1)) as valor, UPPER(cartera+'-'+calificacion+'-prov gen cap') AS DESCRIPCION,\n" +
                "(SELECT REPLACE(MAX(FechaHabil),'-','') FROM FECHAS_HABILES WHERE FechaHabil LIKE :fecha2 AND DiaHabil <> 0) as PERIODO, '3' as tp, '100100112' as nit, '9' as dv,\n" +
                "' ' as TIPOPERDIDA,' ' as CLASERIESGO,' ' as TIPOMOVIMIENTO,' ' as PRODUCTO,' ' as PROCESO,' ' as LINEAOPERATIVA\n" +
                "FROM nexco_porcentaje_calculado as porc " +
                "LEFT JOIN (SELECT * FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.OFI_GRUPO b WHERE b.FECHA_CIERRE is not null and b.FECHA_CIERRE!='' and b.OFINEGOCIO !='0000') A ON porc.oficina= A.OFICINA collate SQL_Latin1_General_CP1_CI_AS \n" +
                "WHERE ROUND(calculo_"+version+"*-1,2) <> 0 AND porc.fecha_corte = :fecha\n" +
                "GROUP BY ISNULL(A.OFINEGOCIO, porc.oficina),cuenta_balance,cartera,calificacion \n" +
                "UNION ALL\n" +
                "SELECT ISNULL(A.OFINEGOCIO, porc.oficina) oficinaA,cuenta_pyg, 'COP' as divisa, '001302039000000103' as contrato, '' as referencia,\n" +
                "CONVERT(numeric(18,2),SUM(calculo_"+version+")) as valor, UPPER(cartera+'-'+calificacion+'-prov gen cap') AS DESCRIPCION,\n" +
                "(SELECT REPLACE(MAX(FechaHabil),'-','') FROM FECHAS_HABILES WHERE FechaHabil LIKE :fecha2 AND DiaHabil <> 0) as PERIODO, '3' as tp, '100100112' as nit, '9' as dv,\n" +
                "' ' as TIPOPERDIDA,' ' as CLASERIESGO,' ' as TIPOMOVIMIENTO,' ' as PRODUCTO,' ' as PROCESO,' ' as LINEAOPERATIVA\n" +
                "FROM nexco_porcentaje_calculado as porc " +
                "LEFT JOIN (SELECT * FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.OFI_GRUPO b WHERE b.FECHA_CIERRE is not null and b.FECHA_CIERRE!='' and b.OFINEGOCIO !='0000') A ON porc.oficina= A.OFICINA COLLATE SQL_Latin1_General_CP1_CI_AS \n" +
                "WHERE ROUND(calculo_"+version+",2) <> 0 AND porc.fecha_corte = :fecha \n"+
                "GROUP BY ISNULL(A.OFINEGOCIO, porc.oficina),cuenta_pyg,cartera,calificacion ");

        getData.setParameter("fecha", period);
        getData.setParameter("fecha2", period+"%");

        if(!getData.getResultList().isEmpty()){
            result = getData.getResultList();
        }

        return result;
    }

    public List<Object[]> generateAnexo8(String period){
        List<Object[]> result = new ArrayList<>();
        ArrayList<String> maxCol = new ArrayList<>();

        for(int i = 0 ; i<=8 ; i++){
            Query getBalance = entityManager.createNativeQuery("SELECT ISNULL(SUM(version_"+i+"),0) FROM nexco_porcentaje_calculado WHERE fecha_corte = ?");
            getBalance.setParameter(1, period);

            String res = getBalance.getResultList().get(0).toString();
            if(!res.equals("0.0")){
                maxCol.add("version_"+i);
            }
        }

        String lastCol = maxCol.get(maxCol.size()-1);

        insertIntoAnexo(period,lastCol);

        Query getData = entityManager.createNativeQuery("SELECT oficina,\n" +
                "cuenta_balance, \n" +
                "'COP' as divisa,\n" +
                "FORMAT(SUM(("+lastCol+"*-1)*(porc.porcentaje_calculado)),'#,##0.00') as valor,\n" +
                "CONVERT(VARCHAR,CONVERT(datetime,(SELECT REPLACE(MAX(FechaHabil),'-','/') FROM FECHAS_HABILES WHERE FechaHabil LIKE :fecha2 AND DiaHabil <> 0)),103) as 'Fecha Origen',\n" +
                "'' as 'Fecha Cierre',\n" +
                "'3' as tp, \n" +
                "'100100112' as nit, \n" +
                "'9' as dv,\n" +
                "'PROVISIONES MANUALES' AS 'Razon Social',\n" +
                "'001302039000000103' as contrato, \n" +
                "UPPER(cartera+'-'+calificacion+'-prov gen cap') AS 'Observacion', \n" +
                "'' as 'Cuenta Provision',\n" +
                "'' as 'Valor Provision',\n" +
                "'' as 'Importe Original',\n" +
                "'NO'\n" +
                "FROM nexco_porcentaje_calculado as porc\n" +
                "WHERE ROUND("+lastCol+"*-1,2) <> 0 AND porc.fecha_corte = :fecha \n" +
                "GROUP BY oficina,cuenta_balance,cartera,calificacion");
        getData.setParameter("fecha", period);
        getData.setParameter("fecha2", period+"%");

        if(!getData.getResultList().isEmpty()){
            result = getData.getResultList();
        }

        return result;
    }

    public void insertIntoAnexo(String period, String version){

        Query delete = entityManager.createNativeQuery("DELETE FROM nexco_anexo_8_porc_cal WHERE fecha_origen LIKE ?");
        delete.setParameter(1,period.replace("-","/")+"%");
        delete.executeUpdate();

        Query insert = entityManager.createNativeQuery("INSERT INTO nexco_anexo_8_porc_cal(centro,cuenta,divisa,importe,fecha_origen,fecha_cierre,tp,identificacion,dv,razon_social,contrato,observacion,\n" +
                "cuenta_provision,valor_provision,importe_moneda_original, prob_recup)\n" +
                "SELECT oficina,\n" +
                "cuenta_balance, \n" +
                "'COP' as divisa,\n" +
                "ROUND(SUM(("+version+"*-1)*(porc.porcentaje_calculado)),2) as valor,\n" +
                "(SELECT REPLACE(MAX(FechaHabil),'-','/') FROM FECHAS_HABILES WHERE FechaHabil LIKE :fecha2 AND DiaHabil <> 0) as 'Fecha Origen',\n" +
                "'' as 'Fecha Cierre',\n" +
                "'3' as tp, \n" +
                "'100100112' as nit, \n" +
                "'9' as dv,\n" +
                "'PROVISIONES MANUALES' AS 'Razon Social',\n" +
                "'001302039000000103' as contrato, \n" +
                "UPPER(cartera+'-'+calificacion+'-prov gen cap') AS 'Observacion', \n" +
                "'' as 'Cuenta Provision',\n" +
                "'' as 'Valor Provision',\n" +
                "'' as 'Importe Original',\n" +
                "'NO'\n" +
                "FROM nexco_porcentaje_calculado as porc\n" +
                "WHERE ROUND("+version+"*-1,2) <> 0 AND porc.fecha_corte = :fecha \n" +
                "GROUP BY oficina,cuenta_balance,cartera,calificacion");
        insert.setParameter("fecha", period);
        insert.setParameter("fecha2", period+"%");
        insert.executeUpdate();
    }

    public void auditCode (String info, User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setComponente("Provisiones");
        insert.setFecha(today);
        insert.setInput("Calculo 1%");
        if(user!=null)
        {
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            insert.setCentro(user.getCentro());
        }
        else
        {
            insert.setNombre("SYSTEM JOB");
        }
        auditRepository.save(insert);
    }

}
