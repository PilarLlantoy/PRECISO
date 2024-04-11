package com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.GenericsParametric;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Transactional
public class GenericsParametricService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public ArrayList<String[]> saveFileDB(InputStream file) throws IOException {
        ArrayList<String[]> list = new ArrayList<String[]>();

        if (file != null) {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet = wb.getSheetAt(0);
                rows = sheet.iterator();
                rows1 = sheet.iterator();

                list = validateParametric(rows);
                String[] result = list.get(0);

                if (result[2].equals("true")) {
                    insertParametric(rows1);
                }
            } catch (Exception e) {
                String[] log = new String[3];
                log[2] = "Documento Invalido";
                list.add(log);
            }
        }

        return list;
    }

    public ArrayList<String[]> validateParametric(Iterator<Row> rows) {
        ArrayList<String[]> list = new ArrayList<String[]>();
        ArrayList<String> validateDuplicates = new ArrayList<>();
        ArrayList<String[]> logDup = new ArrayList<String[]>();
        XSSFRow row;

        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                DataFormatter formatter = new DataFormatter();

                String fuenteInfo = formatter.formatCellValue(row.getCell(0));
                String tp = formatter.formatCellValue(row.getCell(1));
                String indicador = formatter.formatCellValue(row.getCell(2));
                String cartera = formatter.formatCellValue(row.getCell(3));
                String clase = formatter.formatCellValue(row.getCell(4));
                String calificacion = formatter.formatCellValue(row.getCell(5));
                String empresa = formatter.formatCellValue(row.getCell(6));
                String cuenta = formatter.formatCellValue(row.getCell(7));
                String uno = formatter.formatCellValue(row.getCell(8));
                String dos = formatter.formatCellValue(row.getCell(9));
                String tres = formatter.formatCellValue(row.getCell(10));
                String cuatro = formatter.formatCellValue(row.getCell(11));
                String cinco = formatter.formatCellValue(row.getCell(12));
                String seis = formatter.formatCellValue(row.getCell(13));
                String nombreCuenta = formatter.formatCellValue(row.getCell(14));
                String porcentajeCalc = formatter.formatCellValue(row.getCell(15));
                String codigoIfrs9 = formatter.formatCellValue(row.getCell(16));

                String key = fuenteInfo.trim()+";"+tp.trim()+";"+indicador.trim()+";"+cartera.trim()+";"+calificacion.trim()+";"+codigoIfrs9+";"+clase.trim();

                if (fuenteInfo.trim().isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El campo fuente de infomarción está vacío ";
                    list.add(log1);
                }

                if (tp.trim().isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "El campo TP está vacío ";
                    list.add(log1);
                }

                if (cartera.trim().isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El campo cartera está vacío ";
                    list.add(log1);
                }

                if (calificacion.trim().isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El campo calificación está vacío ";
                    list.add(log1);
                }

                if (nombreCuenta.trim().isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(14);
                    log1[2] = "El campo nombre cuenta está vacío ";
                    list.add(log1);
                }

                if (porcentajeCalc.trim().isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(15);
                    log1[2] = "El campo porcentaje calculado no debe estar vacío";
                    list.add(log1);
                }

                if (empresa.trim().isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El empresa está vacío ";
                    list.add(log1);
                } else if (empresa.trim().length() > 4) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El campo empresa debe tener máximo 4 posiciones";
                    list.add(log1);
                }

                if (cuenta.trim().isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El campo cuenta está vacío ";
                    list.add(log1);
                } else if (!validateWithPUC(cuenta)) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El campo cuenta no existe en el PUC";
                    list.add(log1);
                }

                if (tp.equals("PYG")) {
                    if (indicador.trim().isEmpty()) {
                        String[] log1 = new String[3];
                        log1[0] = String.valueOf(row.getRowNum() + 1);
                        log1[1] = CellReference.convertNumToColString(6);
                        log1[2] = "El indicador está vacío ";
                        list.add(log1);
                    } else if (indicador.trim().length() > 1 || !indicador.trim().matches("[+]|[-]")) {
                        String[] log1 = new String[3];
                        log1[0] = String.valueOf(row.getRowNum() + 1);
                        log1[1] = CellReference.convertNumToColString(2);
                        log1[2] = "El campo indicador solo puede contener los caracteres [+] ó [-]";
                        list.add(log1);
                    }
                }

                if (validateDuplicates.contains(key)) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "Los campos Fuente,TP, Indicador, Cartera, Calificación, código IFRS9 y Clase están duplicados.";
                    logDup.add(log1);
                }

                validateDuplicates.add(key);

            }
        }

        String[] log = new String[3];
        log[2] = "true";
        list.add(log);
        logDup.add(log);

        if (!list.get(0)[2].equals("true")) {
            return list;
        } else {
            return logDup;
        }
    }

    public void insertParametric(Iterator<Row> rows) {
        XSSFRow row;
        ArrayList<GenericsParametric> toInsert = new ArrayList<>();

        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                DataFormatter formatter = new DataFormatter();

                String fuenteInfo = formatter.formatCellValue(row.getCell(0));
                String tp = formatter.formatCellValue(row.getCell(1));
                String indicador = formatter.formatCellValue(row.getCell(2));
                String cartera = formatter.formatCellValue(row.getCell(3));
                String clase = formatter.formatCellValue(row.getCell(4));
                String calificacion = formatter.formatCellValue(row.getCell(5));
                String empresa = formatter.formatCellValue(row.getCell(6));
                String cuenta = formatter.formatCellValue(row.getCell(7));
                String uno = formatter.formatCellValue(row.getCell(8));
                String dos = formatter.formatCellValue(row.getCell(9));
                String tres = formatter.formatCellValue(row.getCell(10));
                String cuatro = formatter.formatCellValue(row.getCell(11));
                String cinco = formatter.formatCellValue(row.getCell(12));
                String seis = formatter.formatCellValue(row.getCell(13));
                String nombreCuenta = formatter.formatCellValue(row.getCell(14));
                String porcentajeCalc = formatter.formatCellValue(row.getCell(15));
                String codigoIfrs9 = formatter.formatCellValue(row.getCell(16));

                GenericsParametric generics = new GenericsParametric();
                generics.setFuenteInfo(fuenteInfo);
                generics.setTp(tp);
                generics.setIndicador(indicador);
                generics.setCartera(cartera);
                generics.setClase(clase);
                generics.setCalificacion(calificacion);
                generics.setEmpresa(StringUtils.leftPad(empresa, 4, "0"));
                generics.setCuenta(cuenta);
                generics.setUno(uno);
                generics.setDos(dos);
                generics.setTres(tres);
                generics.setCuatro(cuatro);
                generics.setCinco(cinco);
                generics.setSeis(seis);
                generics.setNombreCuenta(nombreCuenta);
                if (porcentajeCalc.trim().isEmpty()) {
                    generics.setPorcentajeCalc(0.0);
                } else {
                    if (porcentajeCalc.contains("%")) {
                        String realNum = porcentajeCalc.replace("%", "").trim();
                        double val = Double.parseDouble(realNum) / 100;
                        generics.setPorcentajeCalc(val);
                    } else {
                        generics.setPorcentajeCalc(Double.parseDouble(porcentajeCalc));
                    }
                }
                generics.setCodigoIfrs9(codigoIfrs9);


                toInsert.add(generics);
            }
        }

        insertGenerics(toInsert);
    }

    public void insertGenerics(List<GenericsParametric> generics) {
        dropTable();

        jdbcTemplate.batchUpdate(
                "insert into nexco_parametrica_genericas (fuente_info,tp,indicador,cartera,clase,calificacion,empresa,cuenta,uno,dos,tres,cuatro,cinco,seis,nombre_cuenta,porcentaje_calc,codigo_ifrs9) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, generics.get(i).getFuenteInfo());
                        ps.setString(2, generics.get(i).getTp());
                        ps.setString(3, generics.get(i).getIndicador());
                        ps.setString(4, generics.get(i).getCartera());
                        ps.setString(5, generics.get(i).getClase());
                        ps.setString(6, generics.get(i).getCalificacion());
                        ps.setString(7, generics.get(i).getEmpresa());
                        ps.setString(8, generics.get(i).getCuenta());
                        ps.setString(9, generics.get(i).getUno());
                        ps.setString(10, generics.get(i).getDos());
                        ps.setString(11, generics.get(i).getTres());
                        ps.setString(12, generics.get(i).getCuatro());
                        ps.setString(13, generics.get(i).getCinco());
                        ps.setString(14, generics.get(i).getSeis());
                        ps.setString(15, generics.get(i).getNombreCuenta());
                        ps.setDouble(16, generics.get(i).getPorcentajeCalc());
                        ps.setString(17, generics.get(i).getCodigoIfrs9());
                    }

                    public int getBatchSize() {
                        return generics.size();
                    }
                });

    }

    private void dropTable() {
        Query delete = entityManager.createNativeQuery("TRUNCATE TABLE nexco_parametrica_genericas");
        delete.executeUpdate();
    }

    public boolean validateWithPUC(String cuenta) {

        boolean result = false;

        Query search = entityManager.createNativeQuery("SELECT NUCTA FROM CUENTAS_PUC WHERE NUCTA = ? AND EMPRESA = '0013'");
        search.setParameter(1, cuenta);

        result = !search.getResultList().isEmpty();

        return result;
    }

    public ArrayList<GenericsParametric> getGenerics() {

        ArrayList<GenericsParametric> toReturn = new ArrayList<>();

        Query getInfo = entityManager.createNativeQuery("SELECT * FROM nexco_parametrica_genericas ORDER BY fuente_info", GenericsParametric.class);

        if (!getInfo.getResultList().isEmpty()) {
            toReturn = (ArrayList<GenericsParametric>) getInfo.getResultList();
        }

        return toReturn;
    }

}
