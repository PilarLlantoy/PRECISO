package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.ControlPanelJobs;
import com.inter.proyecto_intergrupo.model.bank.GpsReport;
import com.inter.proyecto_intergrupo.model.ifrs9.CondetaRI;
import com.inter.proyecto_intergrupo.model.ifrs9.IncurredLoss;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.temporal.TaxBaseTemporal;
import com.inter.proyecto_intergrupo.repository.admin.ControlPanelJobsRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.IncurredLossRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class IncurredLossService {


    @Autowired
    private final IncurredLossRepository incurredLossRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    public IncurredLossService(IncurredLossRepository incurredLossRepository) {
        this.incurredLossRepository = incurredLossRepository;
    }

    public void batchInsert(List<IncurredLoss> temporal) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_perdidaincurrida_temp (cuenta, centro, contrato, segmento, stage, indicador_contrato, saldo, codigo_consolidacion) values (?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getCuenta());
                        ps.setString(2, temporal.get(i).getCentro());
                        ps.setString(3, temporal.get(i).getContrato());
                        ps.setString(4, temporal.get(i).getSegmento());
                        ps.setString(5, temporal.get(i).getStage());
                        ps.setString(6, temporal.get(i).getIndicadorcontrato());
                        ps.setDouble(7, temporal.get(i).getSaldo());
                        ps.setString(8, temporal.get(i).getCodigoconso());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public ArrayList loadQueryDatabase(InputStream file) throws FileNotFoundException, ParseException {
        XSSFRow row;
        ArrayList lista = new ArrayList();
        ArrayList<IncurredLoss> toInsert = new ArrayList<>();
        clearPerdida();
        Scanner scan = new Scanner(file);
        int cont = 0;
        Date lastFECHPROCE = new Date();
        while (scan.hasNextLine()) {
            String[] log = new String[3];
            log[2] = "false";
            String line = scan.nextLine();
            if(cont >= 0){
                com.inter.proyecto_intergrupo.model.ifrs9.IncurredLoss incurredloss = new IncurredLoss();
                String[] data = line.split(";");
                incurredloss.setCuenta(data[0].trim());
                incurredloss.setCentro(data[1].trim());
                incurredloss.setContrato(data[2].trim());
                incurredloss.setSegmento(data[3].trim());
                incurredloss.setStage(data[4].trim());
                incurredloss.setIndicadorcontrato(data[5].trim());
                incurredloss.setSaldo(Double.parseDouble(data[6].trim().replace(",","").replace("+","")));
                incurredloss.setCodigoconso("");
                toInsert.add(incurredloss);

                if (toInsert.size() == 1000000) {
                    batchInsert(toInsert);
                    toInsert.clear();
                }
                log[0] = data[0].trim();
                log[1] = "Registro actualizado exitosamente.";
                log[2] = "true";
                lista.add(log);
            }
            cont++;
        }
        if (!toInsert.isEmpty()) {
            batchInsert(toInsert);
            toInsert.clear();
            completeCodicons();
        }
        return lista;
    }

    public void completeCodicons() {
        Query firstQuery = entityManager.createNativeQuery("INSERT INTO nexco_perdidaincurrida (centro,codigo_consolidacion,contrato,cuenta,indicador_contrato,saldo,segmento,stage)\n" +
                "SELECT A.centro,B.CODICONS46,A.contrato, A.cuenta,A.indicador_contrato,A.saldo,A.segmento,A.stage FROM nexco_perdidaincurrida_temp A\n" +
                "LEFT JOIN (SELECT NUCTA, CODICONS46 FROM CUENTAS_PUC WHERE empresa = '0013')B ON A.cuenta = B. NUCTA");
        firstQuery.executeUpdate();

        Query finalQuery = entityManager.createNativeQuery("DELETE FROM nexco_perdidaincurrida_temp");
        finalQuery.executeUpdate();
    }

    public void clearPerdida() {
        javax.persistence.Query firstQuery = entityManager.createNativeQuery("DELETE FROM nexco_perdidaincurrida");
        firstQuery.executeUpdate();
    }

    public List<Object[]> getLostResume()
    {
        Query firstQuery = entityManager.createNativeQuery("SELECT cuenta, centro, sum(saldo)  FROM nexco_perdidaincurrida GROUP BY cuenta, centro");
        return firstQuery.getResultList();
    }

    public boolean countTable(){
        Query query = entityManager.createNativeQuery("SELECT count(*) FROM nexco_perdidaincurrida");
        List<String> list= query.getResultList();
        if(Long.parseLong(list.get(0))<850000)
            return true;
        else
            return false;
    }

    public List<IncurredLoss> findAll(){
        return incurredLossRepository.findAll();
    }

    public List<IncurredLoss> findAllDinamic(Set<Map.Entry<String, Object>> params)
    {
        String partsQuery = "";
        for (Map.Entry<String, Object> entry : params) {
            if(entry.getValue()!=null && !entry.getValue().toString().isEmpty() && !entry.getValue().toString().isBlank())
            {
                partsQuery = partsQuery+entry.getKey()+" LIKE '"+entry.getValue()+"' AND ";
            }
        }
        if(partsQuery.length()>0)
        {
            partsQuery = "WHERE "+partsQuery.substring(0,partsQuery.length()-4);
        }
        Query firstQuery = entityManager.createNativeQuery("SELECT * FROM nexco_perdidaincurrida "+partsQuery,IncurredLoss.class);
        return firstQuery.getResultList();
    }

    public List<IncurredLoss> findByFilter(String value, String filter)
    {
        List<IncurredLoss> list=new ArrayList<IncurredLoss>();
        switch (filter)
        {
            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_perdidaincurrida as em " +
                        "WHERE em.cuenta LIKE ?", IncurredLoss.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Centro":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_perdidaincurrida as em " +
                        "WHERE em.centro LIKE ?", IncurredLoss.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Contrato":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_perdidaincurrida as em " +
                        "WHERE em.contrato LIKE ?", IncurredLoss.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Segmento":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_perdidaincurrida as em " +
                        "WHERE em.segmento LIKE ?", IncurredLoss.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Stage":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_perdidaincurrida as em " +
                        "WHERE em.stage LIKE ?", IncurredLoss.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "Indicador contrato":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_perdidaincurrida as em " +
                        "WHERE em.indicador_contrato LIKE ?", IncurredLoss.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            case "Saldo":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_perdidaincurrida as em " +
                        "WHERE em.saldo LIKE ?", IncurredLoss.class);
                query5.setParameter(1, value);

                list= query5.getResultList();
                break;
            case "Código consolidación":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM nexco_perdidaincurrida as em " +
                        "WHERE em.codigo_consolidacion LIKE ?", IncurredLoss.class);
                query6.setParameter(1, value);

                list= query6.getResultList();
                break;
            default:
                break;
        }
        return list;
    }
}

