package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.FirstAdjustment;
import com.inter.proyecto_intergrupo.model.ifrs9.IncurredLoss;
import com.inter.proyecto_intergrupo.repository.ifrs9.FirstAdjustmentRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;


@Service
@Transactional
public class FirstAdjustmentService {


    @Autowired
    private final FirstAdjustmentRepository firstAdjustmentRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    public FirstAdjustmentService(FirstAdjustmentRepository firstAdjustmentRepository) {
        this.firstAdjustmentRepository = firstAdjustmentRepository;
    }

    public void loadQueryDatabase(InputStream file) throws FileNotFoundException, ParseException {
        XSSFRow row;
        ArrayList lista = new ArrayList();
        clearAjuste();
        Scanner scan = new Scanner(file);
        ArrayList<FirstAdjustment> toInsert = new ArrayList<>();

        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            FirstAdjustment firstAdjustment = new FirstAdjustment();

            String importepesosdebe = StringUtils.stripStart(line.substring(124, 139),"0").equals("") ? "000" : StringUtils.stripStart(line.substring(124, 139),"0");
            importepesosdebe = transformData(importepesosdebe);
            importepesosdebe = new StringBuilder(importepesosdebe).insert(importepesosdebe.length()-2, ".").toString();

            String importepesoshaber = StringUtils.stripStart(line.substring(139, 154),"0").equals("") ? "000" : StringUtils.stripStart(line.substring(139, 154),"0");
            importepesoshaber = transformData(importepesoshaber);
            importepesoshaber = new StringBuilder(importepesoshaber).insert(importepesoshaber.length()-2, ".").toString();

            String importedivisadebe = StringUtils.stripStart(line.substring(154, 169),"0").equals("") ? "000" : StringUtils.stripStart(line.substring(154, 169),"0");
            importedivisadebe = transformData(importedivisadebe);
            importedivisadebe = new StringBuilder(importedivisadebe).insert(importedivisadebe.length()-2, ".").toString();

            String importedivisahaber = StringUtils.stripStart(line.substring(169, 184),"0").equals("") ? "000" : StringUtils.stripStart(line.substring(169, 184),"0");
            importedivisahaber = transformData(importedivisahaber);
            importedivisahaber = new StringBuilder(importedivisahaber).insert(importedivisahaber.length()-2, ".").toString();

            firstAdjustment.setEmpresa(line.substring(0, 4));
            firstAdjustment.setAplicativo(line.substring(4, 7));
            firstAdjustment.setFechacontable(line.substring(7, 15));
            firstAdjustment.setFechaproceso(line.substring(15, 23));
            firstAdjustment.setRistra(line.substring(23, 92));
            firstAdjustment.setCentrooperante(line.substring(98, 102));
            firstAdjustment.setCentroorigen(line.substring(102, 106));
            firstAdjustment.setCentrodestino(line.substring(106, 110));
            firstAdjustment.setNumeromovdebe(line.substring(110, 117));
            firstAdjustment.setNumeromovhaber(line.substring(117, 124));
            firstAdjustment.setImportepesosdebe(Double.parseDouble(importepesosdebe));
            firstAdjustment.setImportepesoshaber(Double.parseDouble(importepesoshaber));
            firstAdjustment.setImportedivisadebe(Double.parseDouble(importedivisadebe));
            firstAdjustment.setImportedivisahaber(Double.parseDouble(importedivisahaber));
            firstAdjustment.setDiferenciaPesos(Math.round((Double.parseDouble(importepesosdebe)-Double.parseDouble(importepesoshaber))*100.0)/100.0);
            firstAdjustment.setCorrectora(line.substring(184, 185));
            firstAdjustment.setReferencia(line.substring(185, 197));
            firstAdjustment.setClaveinterfaz(line.substring(197, 200));
            firstAdjustment.setDescripcion(line.substring(215, 245).trim());
            firstAdjustment.setContrato(line.substring(245, 275).trim());
            firstAdjustment.setStage(line.substring(275, 276));
            firstAdjustment.setSegmento(line.substring(276, 279));
            firstAdjustment.setCeros(line.substring(285, 306));
            firstAdjustment.setCuentaingreso(line.substring(306, 321));
            firstAdjustment.setCuentagasto(line.substring(321, 336));
            String rista = firstAdjustment.getRistra();
            rista = rista.replaceAll("\\s+", ";");
            String[] data = rista.split(";");
            firstAdjustment.setCuenta(data[2]);
            firstAdjustment.setCodigoconsolidacion("");

            toInsert.add(firstAdjustment);

            if (toInsert.size() == 500000) {
                batchInsert(toInsert);
                toInsert.clear();
            }
        }
        if (!toInsert.isEmpty()) {
            batchInsert(toInsert);
        }
        toInsert.clear();
    }

    public String transformData(String num){
        String res = "";

        if(num.length() == 1){
            res = "00"+num;
        }else if(num.length() == 2){
            res = "0"+num;
        }else {
            res = num;
        }

        return res;
    }

    public void batchInsert(List<FirstAdjustment> temporal) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_ajuste_primera_vez (aplicativo,centro_destino,centro_operante,centro_origen,ceros,clave_de_interfaz,codigo_de_consolidacion,contrato,correctora,cuenta,cuenta_gasto,cuenta_ingreso,descripcion,empresa,fecha_contable,fecha_proceso,importe_en_divisa_debe,importe_en_divisa_haber,importe_en_pesos_debe," +
                        "importe_en_pesos_haber,numero_mov_debe,numero_mov_haber,referencia,ristra,segmento,stage, diferencia_pesos) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getAplicativo());
                        ps.setString(2, temporal.get(i).getCentrodestino());
                        ps.setString(3, temporal.get(i).getCentrooperante());
                        ps.setString(4, temporal.get(i).getCentroorigen());
                        ps.setString(5, temporal.get(i).getCeros());
                        ps.setString(6, temporal.get(i).getClaveinterfaz());
                        ps.setString(7, temporal.get(i).getCodigoconsolidacion());
                        ps.setString(8, temporal.get(i).getContrato());
                        ps.setString(9, temporal.get(i).getCorrectora());
                        ps.setString(10, temporal.get(i).getCuenta());
                        ps.setString(11, temporal.get(i).getCuentagasto());
                        ps.setString(12, temporal.get(i).getCuentaingreso());
                        ps.setString(13, temporal.get(i).getDescripcion());
                        ps.setString(14, temporal.get(i).getEmpresa());
                        ps.setString(15, temporal.get(i).getFechacontable());
                        ps.setString(16, temporal.get(i).getFechaproceso());
                        ps.setDouble(17, temporal.get(i).getImportedivisadebe());
                        ps.setDouble(18, temporal.get(i).getImportedivisahaber());
                        ps.setDouble(19, temporal.get(i).getImportepesosdebe());
                        ps.setDouble(20, temporal.get(i).getImportepesoshaber());
                        ps.setString(21, temporal.get(i).getNumeromovdebe());
                        ps.setString(22, temporal.get(i).getNumeromovhaber());
                        ps.setString(23, temporal.get(i).getReferencia());
                        ps.setString(24, temporal.get(i).getRistra());
                        ps.setString(25, temporal.get(i).getSegmento());
                        ps.setString(26, temporal.get(i).getStage());
                        ps.setDouble(27,temporal.get(i).getDiferenciaPesos());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public void clearAjuste() {
        javax.persistence.Query firstQuery = entityManager.createNativeQuery("DELETE FROM nexco_ajuste_primera_vez");
        firstQuery.executeUpdate();
    }

    public ArrayList<Object[]> getFirstAdjustment(){
        ArrayList<Object[]> toReturn = new ArrayList<>();

        Query data = entityManager.createNativeQuery("select cuenta,centro_origen,sum(importe_en_pesos_debe) SPD,sum(importe_en_pesos_haber) SPH,sum(importe_en_divisa_debe) SDD,sum(importe_en_divisa_haber) SDH from nexco_ajuste_primera_vez GROUP BY cuenta,centro_origen");

        if(!data.getResultList().isEmpty()){
            toReturn = (ArrayList<Object[]>) data.getResultList();
        }

        return toReturn;
    }

    public List<FirstAdjustment> findAllDinamic(Set<Map.Entry<String, Object>> params)
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
        Query firstQuery = entityManager.createNativeQuery("SELECT * FROM nexco_ajuste_primera_vez "+partsQuery,FirstAdjustment.class);
        return firstQuery.getResultList();
    }
}


