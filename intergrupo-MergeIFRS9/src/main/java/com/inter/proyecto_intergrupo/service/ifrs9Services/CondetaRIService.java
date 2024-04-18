package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.CondetaRI;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.CenterRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.CondetaRIRepository;
import groovy.lang.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@Transactional
public class CondetaRIService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    CondetaRIRepository condetaRIRepository;

    @Autowired
    CenterRepository centerRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private AuditRepository auditRepository;

    @Scheduled(cron = "0 0 11 ? * * ")
    public boolean loadFileQuery0() throws IOException, ParseException {
        try {
            //String path = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\InfoContable\\01-NEXCO\\";
            String path = "\\\\co.igrupobbva\\svrfilesystem\\TX\\Recepcion_host\\Financiera\\Conciliacion\\";
            String fileNameStart = "GOF.S0011.HAAD721.";
            String fileNameEnd = ".TD001.CONDETA.RV";

            File pathToSearch = new File(path);

            File[] listFiles = pathToSearch.listFiles(file -> {
                String name = file.getName();
                if(name.contains(fileNameStart) && name.contains(fileNameEnd)) {
                    return name.contains(fileNameStart);
                }else {
                    return false;
                }
            });

            if(listFiles != null && listFiles.length>0){
                String finalPath = listFiles[listFiles.length-1].toString();
                System.out.println(finalPath);
                bulkFileFromPath(finalPath);
                auditCode("Ejecución Exitosa Job Depositos",null);
                return true;
            }else {
                auditCode("Ejecución Fallida Job Depositos",null);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            auditCode("Ejecución Fallida Job Depositos",null);
            return false;
        }
    }
    @Scheduled(cron = "0 0 12 ? * * ")
    public boolean loadFileQuery1() throws IOException, ParseException {
        try {
            //String path = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\InfoContable\\01-NEXCO\\";
            String path = "\\\\co.igrupobbva\\svrfilesystem\\TX\\Recepcion_host\\Financiera\\Conciliacion\\";
            String fileNameStart = "GOF.S0011.HAAD721.";
            String fileNameEnd = ".TD001.CONDETA.RV";

            File pathToSearch = new File(path);

            File[] listFiles = pathToSearch.listFiles(file -> {
                String name = file.getName();
                if(name.contains(fileNameStart) && name.contains(fileNameEnd)) {
                    return name.contains(fileNameStart);
                }else {
                    return false;
                }
            });

            if(listFiles != null && listFiles.length>0){
                String finalPath = listFiles[listFiles.length-1].toString();
                bulkFileFromPath(finalPath);
                auditCode("Ejecución Exitosa Job Depositos",null);
                return true;
            }else {
                auditCode("Ejecución Fallida Job Depositos",null);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            auditCode("Ejecución Fallida Job Depositos",null);
            return false;
        }
    }
    @Scheduled(cron = "0 0 14 ? * * ")
    public boolean loadFileQuery2() throws IOException, ParseException {
        try {
            //String path = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\InfoContable\\01-NEXCO\\";
            String path = "\\\\co.igrupobbva\\svrfilesystem\\TX\\Recepcion_host\\Financiera\\Conciliacion\\";
            String fileNameStart = "GOF.S0011.HAAD721.";
            String fileNameEnd = ".TD001.CONDETA.RV";

            File pathToSearch = new File(path);

            File[] listFiles = pathToSearch.listFiles(file -> {
                String name = file.getName();
                if(name.contains(fileNameStart) && name.contains(fileNameEnd)) {
                    return name.contains(fileNameStart);
                }else {
                    return false;
                }
            });

            if(listFiles != null && listFiles.length>0){
                String finalPath = listFiles[listFiles.length-1].toString();
                bulkFileFromPath(finalPath);
                auditCode("Ejecución Exitosa Job Depositos",null);
                return true;
            }else {
                auditCode("Ejecución Fallida Job Depositos",null);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            auditCode("Ejecución Fallida Job Depositos",null);
            return false;
        }
    }

    public boolean loadFileQueryManual(User user) throws IOException, ParseException {
        try {
            //String path = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\InfoContable\\01-NEXCO\\";
            String path = "\\\\co.igrupobbva\\svrfilesystem\\TX\\Recepcion_host\\Financiera\\Conciliacion\\";
            String fileNameStart = "GOF.S0011.HAAD721.";
            String fileNameEnd = ".TD001.CONDETA.RV";

            File pathToSearch = new File(path);

            File[] listFiles = pathToSearch.listFiles(file -> {
                String name = file.getName();
                System.out.println(name);
                if(name.contains(fileNameStart) && name.contains(fileNameEnd)) {
                    return name.contains(fileNameStart);
                }else {
                    return false;
                }
            });

            if(listFiles != null && listFiles.length>0){
                String finalPath = listFiles[listFiles.length-1].toString();
                System.out.println("FIN: "+ finalPath);
                bulkFileFromPath(finalPath);
                auditCode("Ejecución Manual Exitosa Job Depositos",user);
                return true;
            }else {
                auditCode("Ejecución Manual Fallida Job Depositos",user);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            auditCode("Ejecución Manual Fallida Job Depositos",user);
            return false;
        }
    }

    public void bulkFileFromPath(String path) throws ParseException {
        Query dropTable = entityManager.createNativeQuery("DROP TABLE IF EXISTS nexco_condetari_temporal \n");
        dropTable.executeUpdate();

        Query createTemporalTable = entityManager.createNativeQuery("CREATE TABLE nexco_condetari_temporal(\n" +
                "\tempresa VARCHAR(255),\n" +
                "\taplicativo VARCHAR(255),\n" +
                "\tfecha1 VARCHAR(255),\n" +
                "\tfecha2 VARCHAR(255),\n" +
                "\tcuenta VARCHAR(255),\n" +
                "\tdivisa VARCHAR(255),\n" +
                "\tcentro VARCHAR(255),\n" +
                "\tcontrato VARCHAR(255),\n" +
                "\ttipo_identificacion VARCHAR(255),\n" +
                "\tidentificacion VARCHAR(255),\n" +
                "\tdigito_verificacion VARCHAR(255),\n" +
                "\tvalor_aplicativo VARCHAR(255),\n" +
                "\tvalor_contable VARCHAR(255),\n" +
                "\tvalor_diferencia VARCHAR(255),\n" +
                "\tvalor_variacion VARCHAR(255)\n" +
                ") \n");
        createTemporalTable.executeUpdate();

        Query bulkInfo = entityManager.createNativeQuery("BULK INSERT nexco_condetari_temporal \n" +
                "FROM '"+path+"' \n" +
                "WITH(FIELDTERMINATOR= ';') \n");
        bulkInfo.executeUpdate();

        Query updateTemp = entityManager.createNativeQuery("UPDATE nexco_condetari_temporal \n" +
                "SET \n" +
                "empresa = TRIM(empresa),\n" +
                "aplicativo = TRIM(aplicativo),\n" +
                "fecha1 = TRIM(fecha1),\n" +
                "fecha2 = TRIM(fecha1),\n" +
                "cuenta = TRIM(cuenta),\n" +
                "divisa = TRIM(divisa),\n" +
                "centro = TRIM(centro),\n" +
                "contrato = TRIM(contrato),\n" +
                "tipo_identificacion = TRIM(tipo_identificacion),\n" +
                "identificacion = TRIM(identificacion),\n" +
                "digito_verificacion = TRIM(digito_verificacion),\n" +
                "valor_aplicativo = REPLACE(TRIM(REPLACE(valor_aplicativo, '+','')),',',''),\n" +
                "valor_contable = REPLACE(TRIM(REPLACE(valor_contable, '+','')),',',''),\n" +
                "valor_diferencia = REPLACE(TRIM(REPLACE(valor_diferencia, '+','')),',',''),\n" +
                "valor_variacion = REPLACE(TRIM(REPLACE(REPLACE(valor_variacion, '+',''),';','')),',','') \n");

        updateTemp.executeUpdate();

        Date todayString = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        String realDate = formatter.format(todayString);

        Date dateMinusMonth = formatter.parse(realDate);
        Calendar c = Calendar.getInstance();
        c.setTime(dateMinusMonth);
        c.add(Calendar.MONTH,-1);
        Date resultAfter  = c.getTime();

        String finalDate = formatter.format(resultAfter);

        Query deleteFromFinal = entityManager.createNativeQuery("DELETE FROM nexco_condetari where fecha1 like ? OR fecha2 like ? \n");
        deleteFromFinal.setParameter(1,finalDate+"%");
        deleteFromFinal.setParameter(2,finalDate+"%");
        deleteFromFinal.executeUpdate();

        System.out.println(finalDate);

        Query finalInsert = entityManager.createNativeQuery("INSERT INTO nexco_condetari (empresa,aplicativo,fecha1,fecha2,cuenta,divisa,centro,contrato,\n" +
                "tipo_identificacion,identificacion,digito_verificacion,valor_aplicativo, valor_contable, valor_diferencia, valor_variacion)\n" +
                "SELECT temp.* FROM\n" +
                "(SELECT empresa,aplicativo,fecha1,fecha2,temp.cuenta,divisa,centro,contrato,tipo_identificacion,\n" +
                "identificacion,digito_verificacion,\n" +
                "(convert(numeric(16,2),valor_aplicativo)) as valor_aplicativo," +
                "(convert(numeric(16,2),valor_contable)) as valor_contable," +
                "(convert(numeric(16,2),valor_aplicativo))-(convert(numeric(16,2),valor_contable)) as valor_diferencia, " +
                "(convert(numeric(16,2),valor_variacion)) as valor_variacion  FROM nexco_condetari_temporal as temp \n" +
                "WHERE (temp.fecha1 like ? OR temp.fecha2 like ?) AND ((convert(numeric(16,2),valor_aplicativo))-(convert(numeric(16,2),valor_contable))) <> 0) AS temp\n" +
                "INNER JOIN \n" +
                "(SELECT DISTINCT cuenta from nexco_h140 where diferencia <> 0 AND fecha like ?) as h140 ON  h140.cuenta = temp.cuenta \n");
        finalInsert.setParameter(1,finalDate+"%");
        finalInsert.setParameter(2,finalDate+"%");
        finalInsert.setParameter(3,finalDate+"%");
        finalInsert.executeUpdate();

    }

    public void bulkInsert(File file) throws IOException {
        List<CondetaRI> listCondeta = new ArrayList<>();
        Query query = entityManager.createNativeQuery("SELECT centro,cuenta FROM nexco_h140 WHERE centro <> '9999' and diferencia <> 0 group by centro,cuenta");
        List<Object[]> h140 = query.getResultList();
        HashMap<Tuple2<String, String>, Object[]> list = new HashMap<>();
        for (Object[] h : h140)
            list.put(new Tuple2<>(h[0].toString(), h[1].toString()), h);
        Scanner scan = new Scanner(file);
        int cont = 0;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            line = line.replaceAll("\\s+", "");
            if (line.indexOf(';') >= 0) {
                String[] data = line.split(";");
                if (list.get(new Tuple2<>(data[6], data[4])) != null) {
                    CondetaRI condetaRI = new CondetaRI();
                    condetaRI.setEmpresa(data[0]);
                    condetaRI.setAplicativo(data[1]);
                    condetaRI.setFecha1(data[2]);
                    condetaRI.setFecha2(data[3]);
                    condetaRI.setCuenta(data[4]);
                    condetaRI.setDivisa(data[5]);
                    condetaRI.setCentro(data[6]);
                    condetaRI.setContrato(data[7]);
                    condetaRI.setTipo_identificacion(data[8]);
                    condetaRI.setIdentificacion(data[9]);
                    condetaRI.setDigito_verificacion(data[10]);
                    //condetaRI.setValor_aplicativo(new BigDecimal(data[11].replace(",","")));
                    //condetaRI.setValor_contable(new BigDecimal(data[12].replace(",","")));
                    //condetaRI.setValor_diferencial(new BigDecimal(data[13].replace(",","")));
                    //condetaRI.setValor_variacion(new BigDecimal(data[14].replace(",","")));
                    if (listCondeta.size() >= 200) {
                        condetaRIRepository.saveAll(listCondeta);
                        listCondeta.removeAll(listCondeta);
                    } else {
                        listCondeta.add(condetaRI);
                    }
                }
            }
            cont++;
        }
        if (listCondeta.size() != 0) {
            condetaRIRepository.saveAll(listCondeta);
        }

    }

    public void auditCode (String info, User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setComponente("CONDETARI");
        insert.setFecha(today);
        insert.setInput("CONDETARI");
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
