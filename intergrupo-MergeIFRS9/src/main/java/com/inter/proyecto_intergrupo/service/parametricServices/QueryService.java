package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelJobs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.information.ChangeAccountQuery;
import com.inter.proyecto_intergrupo.model.parametric.MarcacionConcil;
import com.inter.proyecto_intergrupo.model.parametric.Query;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.parametric.QueryMark;
import com.inter.proyecto_intergrupo.model.temporal.TaxBaseTemporal;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CurrencyRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelJobsService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Transactional
public class QueryService {

    //SaldoQuery es el campo que se multiplica por -1

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private ControlPanelJobsService controlPanelJobsService;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    //@Scheduled(cron = "0 30 5 * * ?")
    @Scheduled(fixedRate  = 7200000)
    public void loadFileQuery() throws ParseException {
        String ruta = "\\\\co.igrupobbva\\svrfilesystem\\TX\\RECEPCION_HOST\\xc\\consolidacion\\";
        String nameFile = "GOF.S0011.AHA.T024.";
        String endNameFile = ".D03.HAQL9905.txt";
        /*Getting current date time*/
        ZoneId defaultZoneId = ZoneId.systemDefault();
        LocalDate date = LocalDate.now().minusDays(1);
        Date correctDate = Date.from(date.atStartOfDay(defaultZoneId).toInstant());
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        //SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM");
        String today = formatter.format(correctDate);
        String todayConvert = formatter2.format(correctDate);

        Date dateMinusMonth = formatter2.parse(todayConvert);
        Calendar c = Calendar.getInstance();
        c.setTime(dateMinusMonth);
        c.add(Calendar.MONTH,-1);
        Date resultAfter  = c.getTime();

        String realDate = formatter2.format(resultAfter);

        try
        {
            nameFile = nameFile + "F" + today + endNameFile;
            System.out.println(nameFile);
            Path path = Paths.get(ruta + nameFile); // se deberia cambiar a ruta + nameFile

            ControlPanelJobs job =controlPanelJobsService.findByIdJob(3);
            Calendar fechaUltima = Calendar.getInstance();
            fechaUltima.setTime(job.getFechaEjecucionExitosa());

            Calendar fechaHoy = Calendar.getInstance();
            fechaHoy.setTime(new Date());

            if(fechaUltima.get(Calendar.DAY_OF_MONTH) != fechaHoy.get(Calendar.DAY_OF_MONTH) && job.getEstado()==true) {
                if (Files.exists(path)) {
                    File file = new File(ruta + nameFile); // se deberia cambiar a ruta + nameFile
                    saveAyer();
                    loadQueryDatabase(file, "View", realDate);


                    job.setFechaEjecucion(new Date());
                    job.setFechaEjecucionExitosa(new Date());
                    controlPanelJobsService.save(job);
                    auditCode("Ejecución Exitosa Job Query",null);
                } else {
                    job.setFechaEjecucion(new Date());
                    controlPanelJobsService.save(job);
                    auditCode("Ejecución Fallida Job Query",null);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public boolean loadFileQuerySub(User user) throws ParseException {
        String ruta = "\\\\co.igrupobbva\\svrfilesystem\\TX\\RECEPCION_HOST\\xc\\consolidacion\\";
        String nameFile = "GOF.S0011.AHA.T024.";
        String endNameFile = ".D03.HAQL9905.txt";
        /*Getting current date time*/
        ZoneId defaultZoneId = ZoneId.systemDefault();
        LocalDate date = LocalDate.now().minusDays(1);
        Date correctDate = Date.from(date.atStartOfDay(defaultZoneId).toInstant());
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        //SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM");
        String today = formatter.format(correctDate);
        String todayConvert = formatter2.format(correctDate);

        Date dateMinusMonth = formatter2.parse(todayConvert);
        Calendar c = Calendar.getInstance();
        c.setTime(dateMinusMonth);
        c.add(Calendar.MONTH,-1);
        Date resultAfter  = c.getTime();

        String realDate = formatter2.format(resultAfter);

        /* Building the nameFile*/
        try {
            nameFile = nameFile + "F" + today + endNameFile;
            Path path = Paths.get(ruta + nameFile); // se deberia cambiar a ruta + nameFile

            ControlPanelJobs job =controlPanelJobsService.findByIdJob(3);
            Calendar fechaUltima = Calendar.getInstance();
            fechaUltima.setTime(job.getFechaEjecucionExitosa());

            Calendar fechaHoy = Calendar.getInstance();
            fechaHoy.setTime(new Date());

            if(fechaUltima.get(Calendar.DAY_OF_MONTH) != fechaHoy.get(Calendar.DAY_OF_MONTH) && job.getEstado()==true)
            {
                if (Files.exists(path)) {
                    File file = new File(ruta + nameFile); // se deberia cambiar a ruta + nameFile

                    loadQueryDatabase(file, "View", realDate);
                    auditCode("Ejecución manual exitosa Job Query",user);
                    return true;
                } else {
                    auditCode("Ejecución manual fallida Job Query",user);
                    return false;
                }
            }
            else {
                auditCode("Ejecución manual fallida Job Query",user);
                return false;
            }
        }catch(Exception e){
            auditCode("Ejecución manual fallida Job Query",user);
            return false;
        }

    }

    public String[] saveFileBDManual(InputStream  fileIn, String tipo, String period, User user) throws IOException, InvalidFormatException, ParseException {

        File f1 = new File("C:\\Users\\CE66390\\Documents\\BBVA Intergrupo\\Pruebas\\Query\\queryTemporal.txt");
        String [] respuesta = new String[2];

        try (OutputStream output = new FileOutputStream(f1)) {
            fileIn.transferTo(output);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Path path = Paths.get(f1.getPath()); // se deberia cambiar a ruta + nameFile
        if (Files.exists(path)) {
            File file = new File(f1.getPath()); // se deberia cambiar a ruta + nameFile
            respuesta = loadQueryDatabase(file,tipo,period);

            Date today = new Date();
            Audit insert = new Audit();
            insert.setAccion("Inserción archivo Query");
            insert.setCentro(user.getCentro());
            insert.setComponente("Parametricas");
            insert.setFecha(today);
            insert.setInput("Query");
            insert.setNombre(user.getNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }else{
            Date today = new Date();
            Audit insert = new Audit();
            insert.setAccion("Falla inserción archivo Query");
            insert.setCentro(user.getCentro());
            insert.setComponente("Parametricas");
            insert.setFecha(today);
            insert.setInput("Query");
            insert.setNombre(user.getNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);

        }
        return  respuesta;
    }

    public String[] loadQueryDatabase(File file, String tipo,String period) throws FileNotFoundException{
        List<Query> queryOriginalList= new ArrayList<>();
        List<Query> queryMarkList= new ArrayList<>();

        Scanner scan = new Scanner(file);

        String respuesta="";
        int cont = 0;
        if(tipo.equals("View")){
            clearQuery();
        }
        else if((tipo.equals("LOCAL")||tipo.equals("IFRS9"))){
            clearQueryMacados(tipo,period);
        }
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            System.out.println(line);
            line = line.replaceAll("\\s+", ";");
            System.out.println(line);
            if (line.indexOf(';') >= 0 && cont > 0) {
                String[] data = line.split(";");
                //String [] temporalFecont = data[3].split("-");
                String finalFecont = data[3];
                System.out.println(finalFecont);
                respuesta = finalFecont.substring(0,7);
                Query query = new Query();
                query.setEmpresa(data[1]);
                query.setNUCTA(data[2]);
                query.setFECONT(finalFecont);
                if (data[4].matches(".*[A-Z].*")) {
                    query.setCODDIV(data[4]);
                    query.setSALMES(new BigDecimal(data[5].replaceAll(",", "")));
                    query.setSALMESD(new BigDecimal(data[6].replaceAll(",", "")));
                    query.setSALMED(new BigDecimal(data[7].replaceAll(",", "")));
                    query.setSALMEDD(new BigDecimal(data[8].replaceAll(",", "")));
                    query.setCODIGEST(data[9]);
                    query.setCODICONS(data[10]);

                    java.sql.Date date = java.sql.Date.valueOf(data[11]);
                    query.setFECHPROCE(date);
                    //query.setFECHPROCE(new SimpleDateFormat("yyyy-MM-dd").parse(data[11]));
                } else {
                    query.setCODDIV("COP");
                    query.setSALMES(new BigDecimal(data[4].replaceAll(",", "")));
                    query.setSALMESD(new BigDecimal(data[5].replaceAll(",", "")));
                    query.setSALMED(new BigDecimal(data[6].replaceAll(",", "")));
                    query.setSALMEDD(new BigDecimal(data[7].replaceAll(",", "")));
                    query.setCODIGEST(data[8]);
                    query.setCODICONS(data[9]);
                    java.sql.Date date = java.sql.Date.valueOf(data[10]);
                    query.setFECHPROCE(date);
                    //query.setFECHPROCE(new SimpleDateFormat("yyyy-MM-dd").parse(data[10]));
                }
                query.setSALDOQUERYDIVISA(BigDecimal.valueOf(query.getSALMESD().doubleValue()*-1));
                query.setSALDOQUERY(BigDecimal.valueOf(query.getSALMES().doubleValue()*-1));
                //query.setDIVISA(getDivisa(query.getCODDIV()));
                //query.setDIVISA(query.getCODDIV());

                /* SAVE OBJECT IN BD*/
                if(tipo.equals("View"))
                {
                    queryOriginalList.add(query);
                }
                else if(tipo.equals("LOCAL") || tipo.equals("IFRS9"))
                {
                    queryMarkList.add(query);
                }
                //queryComplete.add(query);

            }
            cont++;
        }

        String [] toReturn = new String[2];
        
        String fechaValidada;

        if(respuesta.equals(period)){
            fechaValidada = "true";
            String result = "";

            if(tipo.equals("View")){
                insertQuery(queryOriginalList);
                result = insertDivisaNeocon(respuesta, tipo);
            }
            else if(tipo.equals("LOCAL")||tipo.equals("IFRS9"))
            {
                insertQueryMark(queryMarkList, tipo);
                result = insertDivisaNeocon(respuesta, tipo);
            }
            toReturn[1] = result;
        } else{
            fechaValidada = "El Query no coincide con el periodo";
        }
        
        
        
        toReturn[0] = fechaValidada;
        

        queryOriginalList.clear();
        queryMarkList.clear();

        return toReturn;
    }

    public void insertQuery(List<Query> temporal) {

        jdbcTemplate.batchUpdate(
                "INSERT INTO nexco_query(empresa,NUCTA,FECONT,CODDIV,SALMES,SALMESD,SALMED,SALMEDD,CODIGEST,CODICONS,FECHPROCE,SALDOQUERYDIVISA,SALDOQUERY,DIVISA) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getEmpresa());
                        ps.setString(2, temporal.get(i).getNUCTA());
                        ps.setString(3, temporal.get(i).getFECONT());
                        ps.setString(4, temporal.get(i).getCODDIV());
                        ps.setBigDecimal(5, temporal.get(i).getSALMES());
                        ps.setBigDecimal(6, temporal.get(i).getSALMESD());
                        ps.setBigDecimal(7, temporal.get(i).getSALMED());
                        ps.setBigDecimal(8, temporal.get(i).getSALMEDD());
                        ps.setString(9, temporal.get(i).getCODIGEST());
                        ps.setString(10, temporal.get(i).getCODICONS());
                        ps.setString(11, temporal.get(i).getFECHPROCE().toString());
                        ps.setBigDecimal(12, temporal.get(i).getSALDOQUERYDIVISA());
                        ps.setBigDecimal(13, temporal.get(i).getSALDOQUERY());
                        ps.setString(14, temporal.get(i).getDIVISA());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public void insertQueryMark(List<Query> temporal, String tipo) {

        jdbcTemplate.batchUpdate(
                "INSERT INTO nexco_query_marcados (empresa,NUCTA,FECONT,CODDIV,SALMES,SALMESD,SALMED,SALMEDD,CODIGEST,CODICONS,FECHPROCE,SALDOQUERYDIVISA,SALDOQUERY,DIVISA,ORIGEN) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getEmpresa());
                        ps.setString(2, temporal.get(i).getNUCTA());
                        ps.setString(3, temporal.get(i).getFECONT());
                        ps.setString(4, temporal.get(i).getCODDIV());
                        ps.setBigDecimal(5, temporal.get(i).getSALMES());
                        ps.setBigDecimal(6, temporal.get(i).getSALMESD());
                        ps.setBigDecimal(7, temporal.get(i).getSALMED());
                        ps.setBigDecimal(8, temporal.get(i).getSALMEDD());
                        ps.setString(9, temporal.get(i).getCODIGEST());
                        ps.setString(10, temporal.get(i).getCODICONS());
                        ps.setString(11, temporal.get(i).getFECHPROCE().toString());
                        ps.setBigDecimal(12, temporal.get(i).getSALDOQUERYDIVISA());
                        ps.setBigDecimal(13, temporal.get(i).getSALDOQUERY());
                        ps.setString(14, temporal.get(i).getDIVISA());
                        ps.setString(15, tipo);
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public String insertDivisaNeocon(String periodo, String tipo){
        String result = "";

        if(tipo.equals("View")){
            result = "true";
            javax.persistence.Query updateNeocon = entityManager.createNativeQuery("UPDATE nexco_query \n" +
                    "SET divisa = CASE WHEN div.divisa_neocon IS NULL THEN '' ELSE div.divisa_neocon END\n" +
                    "FROM \n" +
                    "nexco_divisas as div \n" +
                    "RIGHT JOIN\n" +
                    "nexco_query as q \n" +
                    "ON div.id_divisa = q.coddiv");
            updateNeocon.executeUpdate();

            javax.persistence.Query getNeocon = entityManager.createNativeQuery("SELECT coddiv FROM nexco_query WHERE divisa = '' ");
            if(!getNeocon.getResultList().isEmpty()){
                String div = getNeocon.getResultList().get(0).toString();
                result = "La divisa "+div+" no está parametrizada en la tabla de divisas";
            }
        } else if(tipo.equals("LOCAL")||tipo.equals("IFRS9")){
            result = "true";
            javax.persistence.Query updateNeocon = entityManager.createNativeQuery("UPDATE nexco_query_marcados \n" +
                    "SET divisa = CASE WHEN div.divisa_neocon IS NULL THEN '' ELSE div.divisa_neocon END\n" +
                    "FROM\n" +
                    "nexco_divisas as div \n" +
                    "RIGHT JOIN\n" +
                    "nexco_query_marcados as q \n" +
                    "ON div.id_divisa = q.coddiv \n"+
                    "WHERE q.fecont LIKE (:fecont+'%') AND q.origen =(:tipo) ");

            updateNeocon.setParameter("fecont",periodo);
            updateNeocon.setParameter("tipo",tipo);
            updateNeocon.executeUpdate();


            javax.persistence.Query getNeocon = entityManager.createNativeQuery("SELECT coddiv FROM nexco_query_marcados WHERE divisa = '' AND fecont LIKE (:fecont+'%') AND origen = :origen ");
            getNeocon.setParameter("fecont",periodo);
            getNeocon.setParameter("origen",tipo);
            if(!getNeocon.getResultList().isEmpty()){
                String div = getNeocon.getResultList().get(0).toString();
                result = "La divisa "+div+" no está parametrizada en la tabla de divisas";
            }
        }

        return result;
    }
    public void clearQuery() {
        javax.persistence.Query query = entityManager.createNativeQuery("DELETE FROM nexco_query");
        query.executeUpdate();
    }
    public void clearQueryMacados(String tipo,String period) {
        javax.persistence.Query lastDelete = entityManager.createNativeQuery("DELETE FROM nexco_query_marcados WHERE origen = ? AND fecont LIKE ? ");
        lastDelete.setParameter(1,tipo);
        lastDelete.setParameter(2,period+"%");
        lastDelete.executeUpdate();
    }

    public String getLastUpdate(){
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT TOP 1 fechproce from nexco_query");
        if(lastUpdate.getResultList().size() > 0){
            Date myDate = (Date)lastUpdate.getResultList().get(0);
            return formatter.format(myDate);
        }
        else return "No hay registro";
    }

    public String[] getActualMonthCharge() throws ParseException {
        DateFormat queryFormatter = new SimpleDateFormat("yyyy-MM");
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date date = queryFormatter.parse(calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH));
        String fecont = queryFormatter.format(date);
        String[] response = new String[2];
        response[0] = fecont;

        javax.persistence.Query getActualMonth = entityManager.createNativeQuery("SELECT TOP 1 fechproce FROM nexco_query WHERE fecont like ?");
        getActualMonth.setParameter(1,"%"+fecont+"%");

        if(getActualMonth.getResultList().size() > 0){
            Date myDate = (Date) getActualMonth.getResultList().get(0);
            response[1] = formatter.format(myDate);
        }else {
            response[1] = "No se ha encontrado fecha de carga";
        }
        return response;
    }

    public String[] getByMonth(String period){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String [] response = new String[2];
        response[1] = period;

        javax.persistence.Query getByPeriod = entityManager.createNativeQuery("SELECT TOP 1 fechproce FROM nexco_query WHERE fecont like ?");
        getByPeriod.setParameter(1,"%"+period+"%");

        if(getByPeriod.getResultList().size() > 0){
            Date myDate = (Date) getByPeriod.getResultList().get(0);
            response[1] = formatter.format(myDate);
        }else {
            response[1] = "No se ha encontrado fecha de carga";
        }
        return response;
    }

    public List<Query> getAllHoy(){

        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT * FROM nexco_query", Query.class);
        return lastUpdate.getResultList();
    }

    public List<Query> getAllAyer(){

        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT * FROM nexco_query_anterior", Query.class);
        return lastUpdate.getResultList();
    }

    public void validateCodicons(String anterior, String nuevo){
        String[] anteriorParts = anterior.split("-");
        String[] nuevoParts = nuevo.split("-");
        javax.persistence.Query delete = entityManager.createNativeQuery("DELETE FROM nexco_cambios_cuentas WHERE fecha_corte LIKE ?");
        delete.setParameter(1,nuevo+"%");
        delete.executeUpdate();
        javax.persistence.Query insert = entityManager.createNativeQuery("INSERT INTO nexco_cambios_cuentas (empresa,cuenta,codicons_anterior,codicons_nuevo,fecha_corte,perimetro_ifrs9,cambio) \n" +
                "SELECT A.empresa, A.nucta,B.codicons, A.codicons, A.fecont, ISNULL(C.ifrs9,'NO') perimetro, CASE WHEN A.codicons = B.codicons THEN 'NO' ELSE 'SI' END \n" +
                "FROM (SELECT * FROM nexco_query_marcados WHERE MONTH(fecont) = ? AND YEAR(fecont) = ? ) A \n" +
                "LEFT JOIN (SELECT * FROM nexco_query_marcados WHERE MONTH(fecont) = ? AND YEAR(fecont) = ? ) B ON A.nucta = B.nucta \n" +
                "LEFT JOIN nexco_provisiones C ON A.codicons = C.cuenta_neocon \n" +
                "GROUP BY A.empresa, A.nucta,A.codicons, B.codicons, A.fecont, ISNULL(C.ifrs9,'NO'), CASE WHEN A.codicons = B.codicons THEN 'NO' ELSE 'SI' END");
        insert.setParameter(1,nuevoParts[1]);
        insert.setParameter(2,nuevoParts[0]);
        insert.setParameter(3,anteriorParts[1]);
        insert.setParameter(4,anteriorParts[0]);
        insert.executeUpdate();
    }

    public List<Query> getAllMarcado(String marca,String periodo){

        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT * FROM nexco_query_marcados AS nq WHERE nq.ORIGEN = ? AND nq.fecont LIKE ?", Query.class);
        lastUpdate.setParameter(1,marca);
        lastUpdate.setParameter(2,periodo+"%");
        return lastUpdate.getResultList();
    }

    public List<MarcacionConcil> getAllConciliacionMarcado(String periodo){
        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT * FROM nexco_h140_completa_ifrs9 WHERE fecha LIKE ? ", MarcacionConcil.class);
        lastUpdate.setParameter(1,periodo+"%");
        return lastUpdate.getResultList();
    }

    public List<String[]> getDates(){

        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT CONVERT(varchar, nq.fechproce,10)AS [MM-DD-YY] FROM nexco_query AS nq GROUP BY nq.fechproce ORDER BY nq.fechproce");
        return lastUpdate.getResultList();
    }

    public void markLocal(String tipo, String periodo){

        String[] partDate = periodo.split("-");
        if(tipo.equals("0"))
        {
            javax.persistence.Query lastDelete = entityManager.createNativeQuery("DELETE FROM nexco_query_marcados WHERE origen = ? and MONTH(fecont) = ? and YEAR(fecont) = ?");
            lastDelete.setParameter(1,"LOCAL");
            lastDelete.setParameter(2,partDate[1]);
            lastDelete.setParameter(3,partDate[0]);
            lastDelete.executeUpdate();

            javax.persistence.Query lastUpdate = entityManager.createNativeQuery("INSERT INTO nexco_query_marcados (empresa, nucta,fecont,coddiv,salmes,salmesd, salmed,salmedd,codigest,codicons,fechproce,saldoquery,saldoquerydivisa,divisa,origen) " +
                    "(SELECT empresa, nucta,fecont,coddiv,salmes,salmesd, salmed,salmedd,codigest,codicons,fechproce,saldoquery,saldoquerydivisa,divisa,'LOCAL' FROM nexco_query)");
            lastUpdate.executeUpdate();
        }
        else if(tipo.equals("1"))
        {
            javax.persistence.Query lastDelete = entityManager.createNativeQuery("DELETE FROM nexco_query_marcados WHERE origen = ? and MONTH(fecont) = ? and YEAR(fecont) = ?");
            lastDelete.setParameter(1,"LOCAL");
            lastDelete.setParameter(2,partDate[1]);
            lastDelete.setParameter(3,partDate[0]);
            lastDelete.executeUpdate();

            javax.persistence.Query lastUpdate = entityManager.createNativeQuery("INSERT INTO nexco_query_marcados (empresa, nucta,fecont,coddiv,salmes,salmesd, salmed,salmedd,codigest,codicons,fechproce,saldoquery,saldoquerydivisa,divisa,origen) " +
                    "(SELECT empresa, nucta,fecont,coddiv,salmes,salmesd, salmed,salmedd,codigest,codicons,fechproce,saldoquery,saldoquerydivisa,divisa,'LOCAL' FROM nexco_query_anterior)");
            lastUpdate.executeUpdate();
        }
    }

    public void markIfrs9(String tipo, String periodo){

        String[] partDate = periodo.split("-");
        if(tipo.equals("0"))
        {
            javax.persistence.Query lastDelete = entityManager.createNativeQuery("DELETE FROM nexco_query_marcados WHERE origen = ? and MONTH(fecont) = ? and YEAR(fecont) = ?");
            lastDelete.setParameter(1,"IFRS9");
            lastDelete.setParameter(2,partDate[1]);
            lastDelete.setParameter(3,partDate[0]);
            lastDelete.executeUpdate();

            javax.persistence.Query lastUpdate = entityManager.createNativeQuery("INSERT INTO nexco_query_marcados (empresa, nucta,fecont,coddiv,salmes,salmesd, salmed,salmedd,codigest,codicons,fechproce,saldoquery,saldoquerydivisa,divisa,origen) " +
                    "(SELECT empresa, nucta,fecont,coddiv,salmes,salmesd, salmed,salmedd,codigest,codicons,fechproce,saldoquery,saldoquerydivisa,divisa,'IFRS9' FROM nexco_query)");
            lastUpdate.executeUpdate();
        }
        else if(tipo.equals("1"))
        {
            javax.persistence.Query lastDelete = entityManager.createNativeQuery("DELETE FROM nexco_query_marcados WHERE origen = ? and MONTH(fecont) = ? and YEAR(fecont) = ?");
            lastDelete.setParameter(1,"IFRS9");
            lastDelete.setParameter(2,partDate[1]);
            lastDelete.setParameter(3,partDate[0]);
            lastDelete.executeUpdate();

            javax.persistence.Query lastUpdate = entityManager.createNativeQuery("INSERT INTO nexco_query_marcados (empresa, nucta,fecont,coddiv,salmes,salmesd, salmed,salmedd,codigest,codicons,fechproce,saldoquery,saldoquerydivisa,divisa,origen) " +
                    "(SELECT empresa, nucta,fecont,coddiv,salmes,salmesd, salmed,salmedd,codigest,codicons,fechproce,saldoquery,saldoquerydivisa,divisa,'IFRS9' FROM nexco_query_anterior)");
            lastUpdate.executeUpdate();
        }
    }

    public void desmarkIfrs9(String tipo, String periodo){

        String[] partDate = periodo.split("-");
        if(tipo.equals("0"))
        {
            javax.persistence.Query lastDelete = entityManager.createNativeQuery("DELETE FROM nexco_query_marcados WHERE origen = ? and MONTH(fecont) = ? and YEAR(fecont) = ?");
            lastDelete.setParameter(1,"LOCAL");
            lastDelete.setParameter(2,partDate[1]);
            lastDelete.setParameter(3,partDate[0]);
            lastDelete.executeUpdate();
        }
        else if(tipo.equals("1"))
        {
            javax.persistence.Query lastDelete = entityManager.createNativeQuery("DELETE FROM nexco_query_marcados WHERE origen = ? and MONTH(fecont) = ? and YEAR(fecont) = ?");
            lastDelete.setParameter(1,"IFRS9");
            lastDelete.setParameter(2,partDate[1]);
            lastDelete.setParameter(3,partDate[0]);
            lastDelete.executeUpdate();
        }
    }

    public void desmarkIfrs9Concil(String periodo){

        javax.persistence.Query lastDelete = entityManager.createNativeQuery("DELETE FROM nexco_h140_completa_ifrs9 WHERE fecha LIKE ? ");
        lastDelete.setParameter(1,periodo+"%");
        lastDelete.executeUpdate();

        javax.persistence.Query lastDeleteGof = entityManager.createNativeQuery("DELETE FROM nexco_h140_gof WHERE date_gof LIKE ? ");
        lastDeleteGof.setParameter(1,periodo+"%");
        lastDeleteGof.executeUpdate();
    }

    public String getUltimoGof()
    {
        javax.persistence.Query data = entityManager.createNativeQuery("SELECT top 1 name_gof FROM nexco_h140_gof WHERE date_gof = '0000-00' ");
        return data.getResultList().get(0).toString();
    }

    public void markIfrs9Concil(String periodo, String nombre){

        desmarkIfrs9Concil(periodo);
        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("INSERT INTO nexco_h140_completa_ifrs9 (aplicativo,centro,cuenta,diferencia,divisa,empresa,fecha,saldo_aplicativo,saldo_contable) " +
                " (SELECT aplicativo,centro,cuenta,diferencia,divisa,empresa,fecha,saldo_aplicativo,saldo_contable FROM nexco_h140_completa WHERE fecha LIKE ? " +
                ")");
        lastUpdate.setParameter(1,periodo+"%");
        lastUpdate.executeUpdate();

        javax.persistence.Query data = entityManager.createNativeQuery("INSERT INTO nexco_h140_gof (name_gof,date_gof) values (?,?)");
        data.setParameter(1,nombre);
        data.setParameter(2,periodo);
        data.executeUpdate();
    }

    public void saveAyer(){

        javax.persistence.Query lastDelete = entityManager.createNativeQuery("DELETE FROM nexco_query_anterior");
        lastDelete.executeUpdate();

        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("INSERT INTO nexco_query_anterior (empresa, nucta,fecont,coddiv,salmes,salmesd, salmed,salmedd,codigest,codicons,fechproce,saldoquery,saldoquerydivisa,divisa) " +
                " (SELECT empresa, nucta,fecont,coddiv,salmes,salmesd, salmed,salmedd,codigest,codicons,fechproce,saldoquery,saldoquerydivisa,divisa FROM nexco_query " +
                ")");
        lastUpdate.executeUpdate();
    }

    public List<String[]> getFechas(String periodo){
        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT hoy.fecha_hoy, ayerp.fecha_ayer, malp.fecha_local, maip.fecha_ifrs9 FROM \n" +
                "(SELECT '1' as id) AS temp \n" +
                "LEFT JOIN \n" +
                "(SELECT TOP 1 '1' as id, fecont as fecha_hoy FROM nexco_query) as HOY ON temp.id = HOY.id \n" +
                "LEFT JOIN \n" +
                "(SELECT TOP 1 '1' as id, fecont as fecha_ayer FROM nexco_query_anterior) AS AYERP ON temp.id = AYERP.id \n" +
                "LEFT JOIN \n" +
                "(SELECT TOP 1 '1' as id, fecont as fecha_local FROM nexco_query_marcados WHERE origen = 'LOCAL' AND fecont LIKE ?) AS MALP ON temp.id = MALP.id \n" +
                "LEFT JOIN \n" +
                "(SELECT TOP 1 '1' as id, fecont as fecha_ifrs9 FROM nexco_query_marcados WHERE origen = 'IFRS9' AND fecont LIKE ? ) AS MAIP ON temp.id = MAIP.id");
        lastUpdate.setParameter(1,periodo+"%");
        lastUpdate.setParameter(2,periodo+"%");
        return lastUpdate.getResultList();
    }

    public List<String[]> getFechasProceso(String periodo){
        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT hoy.fecha_hoy, ayerp.fecha_ayer, malp.fecha_local, maip.fecha_ifrs9 FROM \n" +
                "(SELECT '1' as id) AS temp \n" +
                "LEFT JOIN \n" +
                "(SELECT TOP 1 '1' as id, CONVERT(varchar, fechproce,23) as fecha_hoy FROM nexco_query) as HOY ON temp.id = HOY.id \n" +
                "LEFT JOIN \n" +
                "(SELECT TOP 1 '1' as id, CONVERT(varchar, fechproce,23) as fecha_ayer FROM nexco_query_anterior) AS AYERP ON temp.id = AYERP.id \n" +
                "LEFT JOIN \n" +
                "(SELECT TOP 1 '1' as id, CONVERT(varchar, fechproce,23) as fecha_local FROM nexco_query_marcados WHERE origen = 'LOCAL' AND fecont LIKE ? ) AS MALP ON temp.id = MALP.id \n" +
                "LEFT JOIN \n" +
                "(SELECT TOP 1 '1' as id, CONVERT(varchar, fechproce,23) as fecha_ifrs9 FROM nexco_query_marcados WHERE origen = 'IFRS9' AND fecont LIKE ? ) AS MAIP ON temp.id = MAIP.id");
        lastUpdate.setParameter(1,periodo+"%");
        lastUpdate.setParameter(2,periodo+"%");
        return lastUpdate.getResultList();
    }

    public List<String> getNombreGof(String periodo){
        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT TOP 1 name_gof FROM nexco_h140_gof WHERE date_gof LIKE ? ");
        lastUpdate.setParameter(1,periodo+"%");
        return lastUpdate.getResultList();
    }

    public List<String> getFechasProcesoConcil(String periodo){
        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT TOP 1 fecha FROM nexco_h140_completa_ifrs9 WHERE fecha LIKE ? ");
        lastUpdate.setParameter(1,periodo+"%");
        return lastUpdate.getResultList();
    }

    public List<String> getFechasConcil(String periodo){
        //javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT TOP 1 fecha FROM nexco_h140_completa_ifrs9 WHERE fecha LIKE ? ");
        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT MAX(FechaHabil) FROM FECHAS_HABILES WHERE FechaHabil LIKE ? AND DiaHabil = 1");
        lastUpdate.setParameter(1,periodo+"%");
        return lastUpdate.getResultList();
    }

    public List<ChangeAccountQuery> getAccountsCodicons(String period) {
        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("select * from nexco_cambios_cuentas WHERE fecha_corte LIKE ? ",ChangeAccountQuery.class);
        lastUpdate.setParameter(1,period+"%");
        return lastUpdate.getResultList();
    }

    public void auditCode (String info, User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setComponente("Informacionales");
        insert.setFecha(today);
        insert.setInput("Query");
        if(user!=null)
        {
            insert.setNombre(user.getNombre());
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
