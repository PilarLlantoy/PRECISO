package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.MarketRisk;
import com.inter.proyecto_intergrupo.repository.ifrs9.MarketRiskRepository;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class MarketRiskService {

    @Autowired
    MarketRiskRepository marketRiskRepository;

    @PersistenceContext
    EntityManager entityManager;

    public List<MarketRisk>  findByPeriodo(String periodo){
        Query getMarketRisk = entityManager.createNativeQuery("SELECT * FROM nexco_riesgo_mercado WHERE SUBSTRING(fecha,1,7) = ?",MarketRisk.class);
        getMarketRisk.setParameter(1,periodo);
        List<MarketRisk> result = getMarketRisk.getResultList();
        return result;
    }

    public ArrayList<String[]> saveFileBD(InputStream file,String periodo) throws IOException, ParseException {
        ArrayList<String[]> list = new ArrayList<>();
        if (file != null) {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            //list = validarPlantilla(rows, periodo);
            //String[] temporal = list.get(0);
            if (true) {
                list = getRows(rows1);
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows,String periodo) {
        ArrayList lista = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        Boolean validaFecha = false;
        String[] log = new String[3];
        log[0] = "0";
        log[1] = "0";
        log[2] = "false";
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            //validando fecha
            if (firstRow == 2) {
                DataFormatter formatter = new DataFormatter();
                String cellFecha = formatter.formatCellValue(row.getCell(1));
                String [] fecha = cellFecha.split("/");
                if (fecha[2] + "-" + fecha[2] == periodo) {
                    validaFecha = true;
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "false";
                    break;
                }
            }
            firstRow ++;
        }
        lista.add(log);
        return lista;
    }

    public ArrayList<String[]> getRows(Iterator<Row> rows) throws ParseException {
        XSSFRow row;
        int firstRow = 1;
        ArrayList lista = new ArrayList();
        while (rows.hasNext()) {
            String[] log = new String[3];
            log[2] = "false";
            row = (XSSFRow) rows.next();
            if (firstRow == 2) {

            }
            if (firstRow > 5) {
                DataFormatter formatter = new DataFormatter();
                String cellNumeroPapeleta = formatter.formatCellValue(row.getCell(0));
                String cellCodNombre = formatter.formatCellValue(row.getCell(1));
                String cellCodPuc = formatter.formatCellValue(row.getCell(2));
                String cellFecha = formatter.formatCellValue(row.getCell(3));
                String cellFechaFinal = formatter.formatCellValue(row.getCell(4));
                String cellValorTotal = formatter.formatCellValue(row.getCell(5));
                String cellValorIntereses = formatter.formatCellValue(row.getCell(6));
                String cellCausacionHoy = formatter.formatCellValue(row.getCell(7));
                String cellMoneda = formatter.formatCellValue(row.getCell(8));
                String cellFechaCorte = formatter.formatCellValue(row.getCell(9));
                String cellExposicion = formatter.formatCellValue(row.getCell(10));
                String cellDiasVto = formatter.formatCellValue(row.getCell(11));
                String cellTasaDescuento = formatter.formatCellValue(row.getCell(12));
                String cellFd = formatter.formatCellValue(row.getCell(13));
                String cellValorPresente = formatter.formatCellValue(row.getCell(14));
                String cellDiferencia = formatter.formatCellValue(row.getCell(15));
                log[0] = String.valueOf(row.getRowNum());
                if ((!cellNumeroPapeleta.isEmpty() || !cellNumeroPapeleta.isBlank()) && (!cellCodNombre.isEmpty() || !cellCodNombre.isBlank()) && (!cellCodPuc.isEmpty() || !cellCodPuc.isBlank())
                        && (!cellFecha.isEmpty() || !cellFecha.isBlank()) && (!cellFechaFinal.isEmpty() || !cellFechaFinal.isBlank()) && (!cellValorTotal.isEmpty() || !cellValorTotal.isBlank())
                        && (!cellValorIntereses.isEmpty() || !cellValorIntereses.isBlank()) && (!cellCausacionHoy.isEmpty() || !cellCausacionHoy.isBlank()) && (!cellMoneda.isBlank() || !cellMoneda.isEmpty()
                        && (!cellFechaCorte.isEmpty() || !cellFechaCorte.isBlank()) && (!cellExposicion.isEmpty() || !cellExposicion.isBlank())) && (!cellDiasVto.isEmpty() || !cellDiasVto.isBlank())
                        && (!cellTasaDescuento.isEmpty() || !cellTasaDescuento.isBlank()) && (!cellFd.isEmpty() || !cellFd.isBlank()) && (!cellValorPresente.isEmpty() || !cellValorPresente.isBlank())
                        && (!cellDiferencia.isEmpty() || !cellDiferencia.isBlank())) {
                    MarketRisk marketRisk = new MarketRisk();
                    marketRisk.setNumeroPapeleta(cellNumeroPapeleta);
                    marketRisk.setCodNombre(cellCodNombre);
                    marketRisk.setCodPuc(cellCodPuc);
                    String[] fecha = cellFecha.split("/");
                    marketRisk.setFecha("20"+fecha[2]+"-"+String.format("%02d", Integer.parseInt(fecha[0])));
                    marketRisk.setFechaFinal(cellFechaFinal);
                    marketRisk.setValorTotal(Long.parseLong(cellValorTotal.replace(".","").replaceAll("\\s+", "")));
                    marketRisk.setIntereses(cellValorIntereses);
                    marketRisk.setCausacionHoy(Long.parseLong(cellCausacionHoy.replace(".","").replaceAll("\\s+", "")));
                    marketRisk.setMoneda(cellMoneda);
                    marketRisk.setExposicion(Long.parseLong(cellExposicion.replace(".","").replaceAll("\\s+", "")));
                    marketRisk.setFechaCorte(cellFechaCorte);
                    marketRisk.setDiasVto(cellDiasVto);
                    marketRisk.setTasaDescuento(cellTasaDescuento);
                    marketRisk.setFd(Double.parseDouble(cellFd.replace(",",".").replaceAll("\\s+", "")));
                    marketRisk.setValorPresente(Long.parseLong(cellValorPresente.replace(".","").replaceAll("\\s+", "")));
                    marketRisk.setDiferencia(Long.parseLong(cellDiferencia.replace(".","").replaceAll("\\s+", "")));
                    marketRiskRepository.save(marketRisk);
                }
                else {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "Revisar que la fila tenga todos los datos completos";
                }
            }
            lista.add(log);
            firstRow ++;
        }
        return lista;
    }
}
