package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.DesconValidationUpload;
import com.inter.proyecto_intergrupo.model.temporal.TaxBaseTemporal;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.coyote.http11.filters.IdentityOutputFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@Transactional
public class DesconValidationService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EntityManager entityManager;

    public void uploadFiles(String period, MultipartFile[] files){

        ArrayList<DesconValidationUpload> toInsert = new ArrayList<>();

        Arrays.stream(files).forEach(multipartFile -> {
            if(!multipartFile.isEmpty()){

                BufferedReader br;
                String line;
                try {
                    InputStream is = multipartFile.getInputStream();
                    br = new BufferedReader(new InputStreamReader(is));
                    while ((line = br.readLine()) != null){
                        String[] data = line.split(";");
                        DesconValidationUpload upload = new DesconValidationUpload();

                        upload.setCuenta(data[0].trim());
                        upload.setDiv(data[1].trim());
                        upload.setSald(Double.parseDouble(data[2].trim()
                                .replace("+","")
                                .replace(",","")));
                        upload.setSalc(Double.parseDouble(data[3].trim()
                                .replace("+","")
                                .replace(",","")));
                        upload.setSaldDiv(Double.parseDouble(data[4].trim()
                                .replace("+","")
                                .replace(",","")));
                        upload.setSalcDiv(Double.parseDouble(data[5].trim()
                                .replace("+","")
                                .replace(",","")));
                        upload.setObservacion(data[6].trim());
                        upload.setPeriodo(period);
                        upload.setNombreReal(multipartFile.getOriginalFilename());

                        toInsert.add(upload);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if(!toInsert.isEmpty()){
            Query delete = entityManager.createNativeQuery("DELETE FROM nexco_validacion_descon_carga WHERE periodo = ?");
            delete.setParameter(1,period);
            delete.executeUpdate();

            batchInsert(toInsert);

            Query update = entityManager.createNativeQuery("UPDATE nexco_validacion_descon_carga\n" +
                    "SET vr_ml = ROUND(sald - salc,2) \n" +
                    "WHERE periodo = ?");
            update.setParameter(1,period);
            update.executeUpdate();

            Query update2 = entityManager.createNativeQuery("UPDATE nexco_validacion_descon_carga\n" +
                    "SET vr_me = ROUND((sald_div-salc_div) * val.valor,2)\n" +
                    "FROM\n" +
                    "nexco_validacion_descon_carga\n" +
                    "INNER JOIN nexco_divisas_valor as val ON val.divisa = div \n" +
                    "WHERE val.fecha LIKE ? AND periodo = ?");
            update2.setParameter(1,period+"%");
            update2.setParameter(2,period);
            update2.executeUpdate();

            Query update3 = entityManager.createNativeQuery("UPDATE nexco_validacion_descon_carga \n" +
                    "SET vr_total = ROUND(vr_ml + vr_me,2) \n" +
                    "WHERE periodo = ?");
            update3.setParameter(1,period);
            update3.executeUpdate();

            Query update4 = entityManager.createNativeQuery("UPDATE nexco_validacion_descon_carga \n" +
                    "SET vr_miles = ROUND(vr_total/1000,2) \n" +
                    "WHERE periodo = ?");
            update4.setParameter(1,period);
            update4.executeUpdate();

            Query update5 = entityManager.createNativeQuery("UPDATE nexco_validacion_descon_carga\n" +
                    "SET cod_neocon = puc.CODICONS46\n" +
                    "FROM\n" +
                    "nexco_validacion_descon_carga\n" +
                    "INNER JOIN (SELECT NUCTA, CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') AS puc ON cuenta = puc.NUCTA\n" +
                    "WHERE periodo = ?");
            update5.setParameter(1,period);
            update5.executeUpdate();

            Query update6 = entityManager.createNativeQuery("UPDATE nexco_validacion_descon_carga \n" +
                    "SET tasa_conversion = val.valor\n" +
                    "FROM\n" +
                    "nexco_validacion_descon_carga\n" +
                    "INNER JOIN nexco_divisas_valor as val ON val.divisa = div \n" +
                    "WHERE val.fecha LIKE ? AND periodo = ?");
            update6.setParameter(1,period+"%");
            update6.setParameter(2,period);
            update6.executeUpdate();
        }
    }

    public List<Object[]> validateDescon(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT * FROM nexco_validacion_descon_carga WHERE periodo = ?;");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public void batchInsert(List<DesconValidationUpload> temporal) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_validacion_descon_carga (cuenta, div, observacion, salc, salc_div, sald, sald_div, periodo, nombre_real) values (?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getCuenta());
                        ps.setString(2, temporal.get(i).getDiv());
                        ps.setString(3, temporal.get(i).getObservacion());
                        ps.setDouble(4, temporal.get(i).getSalc());
                        ps.setDouble(5, temporal.get(i).getSalcDiv());
                        ps.setDouble(6, temporal.get(i).getSald());
                        ps.setDouble(7, temporal.get(i).getSaldDiv());
                        ps.setString(8, temporal.get(i).getPeriodo());
                        ps.setString(9, temporal.get(i).getNombreReal());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public List<DesconValidationUpload> getInfo(String period){
        List<DesconValidationUpload> toReturn = new ArrayList<>();

        Query getData = entityManager.createNativeQuery("SELECT * FROM nexco_validacion_descon_carga WHERE periodo = ?", DesconValidationUpload.class);
        getData.setParameter(1, period);

        if(!getData.getResultList().isEmpty()){
            toReturn = getData.getResultList();
        }

        return toReturn;
    }

}
