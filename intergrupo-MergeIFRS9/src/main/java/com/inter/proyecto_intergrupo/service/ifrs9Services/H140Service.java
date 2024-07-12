package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelJobs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.H140;
import com.inter.proyecto_intergrupo.model.ifrs9.H140Comp;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.ControlPanelJobsRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelJobsService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class H140Service {

    Logger log = LoggerFactory.getLogger(H140Service.class);
    ThreadLocal<String> threadLocalValue = ThreadLocal.withInitial(() -> "Name star");

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private ControlPanelJobsService controlPanelJobsService;

    @Autowired
    private ControlPanelJobsRepository controlPanelJobsRepository;

    @Autowired
    private AuditRepository auditRepository;

    final String PRIMARY_SC = "C:\\Users\\CE66390\\Documents\\BBVA Intergrupo\\Pantallazos Base Fiscal\\temporalH140.txt";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 5 * * * ")
    //@Scheduled(fixedDelay = 60000*1)
    public void validateRoutineTax() throws IOException, ParseException {
        Query query1 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_cuadro_mando_jobs as em WHERE em.nombre = ? AND em.estado = ?", ControlPanelJobs.class);
        query1.setParameter(1, "HADT140");
        query1.setParameter(2, true);
        List<ControlPanelJobs> listTemporal = query1.getResultList();
        if (listTemporal.size() != 0) {
            loadH140(null);
            ControlPanelJobs update = listTemporal.get(0);
            update.setFechaEjecucion(new Date());
            update.setFechaEjecucionExitosa(new Date());
            controlPanelJobsRepository.save(update);
            auditCode("Ejecución Exitosa Job HADT140",null);
        }

        ControlPanelJobs job = controlPanelJobsService.findByIdJob(2);
        job.setFechaEjecucion(new Date());
        controlPanelJobsService.save(job);
    }

    public boolean loadH140(User user) throws FileNotFoundException, ParseException {
        try {
            //String ruta = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\InfoContable\\01-NEXCO\\";
            String ruta = "\\\\co.igrupobbva\\svrfilesystem\\TX\\RECEPCION_HOST\\XC\\CONSOLIDACION\\";
            String nameFile = "GOF.S0011.HAAM642.F";
            String endNameFile = ".TD001.H140MES.txt";
            /*Getting current date time*/
            Date date = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, -1);
            Date after = c.getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
            String today = formatter.format(after);


            nameFile = nameFile + today + endNameFile;
            System.out.println(nameFile);
            Path path = Paths.get(ruta + nameFile); // se deberia cambiar a ruta + nameFile
            if (Files.exists(path)) {
                File file = new File(ruta + nameFile); // se deberia cambiar a ruta + nameFile
                loadFile(file);
                cargarNombre(nameFile);
                threadLocalValue.remove();
                threadLocalValue.set(nameFile);
                if(user!=null)
                    auditCode("Ejecución Manual Exitosa Job HADT140",user);
                return true;
            } else {
                if(user!=null)
                    auditCode("Ejecución Manual Fallida Job HADT140",user);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(user!=null)
                auditCode("Ejecución Manual Fallida Job HADT140",user);
            return false;
        }
    }

    public void cargarNombre(String nombre)
    {
        Query dataD = entityManager.createNativeQuery("DELETE FROM  nexco_h140_gof WHERE date_gof = '0000-00' ");
        dataD.executeUpdate();

        Query data = entityManager.createNativeQuery("INSERT INTO nexco_h140_gof (name_gof,date_gof) values (?,?)");
        data.setParameter(1,nombre);
        data.setParameter(2,"0000-00");
        data.executeUpdate();

    }

    public boolean loadH140File(InputStream file) throws FileNotFoundException, ParseException {
        try {

            File f1 = new File(PRIMARY_SC);
            try (OutputStream output = new FileOutputStream(f1)) {
                file.transferTo(output);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            loadFile(f1);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<String> getPerimeter() throws ParseException {
        ArrayList<String> result = new ArrayList<>();

        Query data = entityManager.createNativeQuery("SELECT DISTINCT TRIM(c1.nucta) FROM nexco_provisiones p1 \n" +
                "INNER JOIN \n" +
                "(SELECT NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') c1 ON CONVERT(varchar,p1.cuenta_neocon) = c1.codicons46 \n" +
                "WHERE p1.ifrs9 = 'CV'");
        if(!data.getResultList().isEmpty()){
            data.getResultList().forEach( e -> {
                result.add(e.toString());
            });
        }

        return result;
    }

    public void loadFile(File file) throws FileNotFoundException, ParseException {
        Scanner scan = new Scanner(file);

        ArrayList<H140> toInsert = new ArrayList<>();
        ArrayList<H140Comp> toInsertComp = new ArrayList<>();

        Date todayString = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        String realDate = formatter.format(todayString);

        Date dateMinusMonth = formatter.parse(realDate);
        Calendar c = Calendar.getInstance();
        c.setTime(dateMinusMonth);
        c.add(Calendar.MONTH,-1);
        Date resultAfter  = c.getTime();

        String finalDate = formatter.format(resultAfter);

        ArrayList<String> perimeter = getPerimeter();


        Query truncate1 = entityManager.createNativeQuery("DELETE FROM nexco_h140 where fecha like ?");
        truncate1.setParameter(1, finalDate+"%");
        truncate1.executeUpdate();

        Query truncate = entityManager.createNativeQuery("DELETE FROM nexco_h140_completa where fecha like ?");
        truncate.setParameter(1, finalDate+"%");
        truncate.executeUpdate();

        while(scan.hasNextLine()){
            String line = scan.nextLine().trim();

            String empresa = StringUtils.leftPad(line.substring(0,2).trim(),4,"0");
            line = line.substring(3).trim();
            String fecha = line.substring(0,10).trim();
            line = line.substring(11);
            String centro = StringUtils.leftPad(line.substring(0,4).trim(),4,"0");
            line = line.substring(5).trim();
            String cuenta = line.substring(0,15).trim();
            line = line.substring(15);
            String divisa = line.substring(0,3).trim();
            line = line.substring(30);
            String aplicativo = line.substring(0,2).trim();
            line = line.substring(3);
            double saldoAplicativo = Double.parseDouble(line.substring(0,18).trim());
            line = line.substring(19);
            double saldoContable = Double.parseDouble(line.substring(0,18).trim());
            double diferencia = Math.round((saldoAplicativo - saldoContable)* 100.0)/100.0;

            if((!Objects.equals(centro, "9999") && !divisa.isEmpty()) && aplicativo.equals("RI") && perimeter.contains(cuenta)){
                H140 data = new H140();
                data.setEmpresa(empresa);
                data.setFecha(fecha);
                data.setCentro(centro);
                data.setCuenta(cuenta);
                data.setDivisa(divisa);
                data.setAplicativo(aplicativo);
                data.setSaldo_aplicativo(saldoAplicativo);
                data.setSaldo_contable(saldoContable);
                data.setDiferencia(diferencia);

                toInsert.add(data);
                if(toInsert.size() == 100000){
                    batchInsert(toInsert);
                    toInsert.clear();
                }
            }

            if((!Objects.equals(centro, "9999") && !divisa.isEmpty())){
                H140Comp data = new H140Comp();
                data.setEmpresa(empresa);
                data.setFecha(fecha);
                data.setCentro(centro);
                data.setCuenta(cuenta);
                data.setDivisa(divisa);
                data.setAplicativo(aplicativo);
                data.setSaldo_aplicativo(saldoAplicativo);
                data.setSaldo_contable(saldoContable);
                data.setDiferencia(diferencia);

                toInsertComp.add(data);
                if(toInsertComp.size() == 100000){
                    batchInsertComp(toInsertComp);
                    toInsertComp.clear();
                }
            }
        }
        batchInsert(toInsert);
        toInsert.clear();

        batchInsertComp(toInsertComp);
        toInsertComp.clear();

        System.out.println("terminó");
    }

    public void batchInsert(ArrayList<H140> temporal) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_H140 (empresa, fecha, centro, cuenta, divisa, aplicativo, saldo_aplicativo, saldo_contable, diferencia) values (?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getEmpresa());
                        ps.setString(2, temporal.get(i).getFecha());
                        ps.setString(3, temporal.get(i).getCentro());
                        ps.setString(4, temporal.get(i).getCuenta());
                        ps.setString(5, temporal.get(i).getDivisa());
                        ps.setString(6, temporal.get(i).getAplicativo());
                        ps.setDouble(7, temporal.get(i).getSaldo_aplicativo());
                        ps.setDouble(8, temporal.get(i).getSaldo_contable());
                        ps.setDouble(9, temporal.get(i).getDiferencia());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public void batchInsertComp(ArrayList<H140Comp> temporal) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_h140_completa (empresa, fecha, centro, cuenta, divisa, aplicativo, saldo_aplicativo, saldo_contable, diferencia) values (?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getEmpresa());
                        ps.setString(2, temporal.get(i).getFecha());
                        ps.setString(3, temporal.get(i).getCentro());
                        ps.setString(4, temporal.get(i).getCuenta());
                        ps.setString(5, temporal.get(i).getDivisa());
                        ps.setString(6, temporal.get(i).getAplicativo());
                        ps.setDouble(7, temporal.get(i).getSaldo_aplicativo());
                        ps.setDouble(8, temporal.get(i).getSaldo_contable());
                        ps.setDouble(9, temporal.get(i).getDiferencia());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public List<H140> findAllH140() {
        javax.persistence.Query queryGet = entityManager.createNativeQuery("SELECT * FROM nexco_h140", H140.class);
        List<H140> result = queryGet.getResultList();
        return result;
    }

    public String getNameFile() {
        DateFormat formatter = new SimpleDateFormat("yyMMdd");
        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("SELECT TOP 1 fecha from nexco_h140");
        if (lastUpdate.getResultList().size() > 0) {
            Date myDate = (Date) lastUpdate.getResultList().get(0);
            return "GOF.S0011.HAAM642.F" + formatter.format(myDate) + ".TD001.H140MES.TXT";
        } else return "No hay registro";
    }

    public void auditCode (String info, User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setComponente("HADT140");
        insert.setFecha(today);
        insert.setInput("HADT140");
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
