package com.inter.proyecto_intergrupo.service.dataqualityServices;

import com.inter.proyecto_intergrupo.model.dataquality.PointRulesDQ;
import com.inter.proyecto_intergrupo.model.dataquality.RulesDQ;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class PointRulesDQListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> rulesList;
    private List<PointRulesDQ> listRules;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public PointRulesDQListReport(List<String[]> rulesList, List<PointRulesDQ> listRules) {
        this.rulesList = rulesList;
        this.listRules = listRules;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Reglas DQ");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "FECHA DE CIERRE", style);
        createCell(row, 1, "FECHA DE EJECUCIÓN REGLA DE CALIDAD", style);
        createCell(row, 2, "IDENTIFICADOR DEL PAIS DEL DATA SYSTEM MODELO GLOBAL", style);
        createCell(row, 3, "IDENTIFICADOR UUAA", style);
        createCell(row, 4, "NOMBRE DEL DATA SYSTEM", style);
        createCell(row, 5, "DESCRIPCIÓN DESGLOSE REGLA DE CALIDAD", style);
        createCell(row, 6, "IDENTIFICADOR SECUENCIAL REGLA DE CALIDAD LEGACY", style);
        createCell(row, 7, "TIPO DE PRINCIPIO REGLA DE CALIDAD MODELO GLOBAL", style);
        createCell(row, 8, "TIPO DE REGLA DE CALIDAD MODELO GLOBAL", style);
        createCell(row, 9, "NOMBRE FÍSICO OBJETO", style);
        createCell(row, 10, "NOMBRE FÍSICO CAMPO", style);
        createCell(row, 11, "PORCENTAJE CUMPLIMIENTO DE REGLA DE CALIDAD", style);
        createCell(row, 12, "NUMERADOR DE CUMPLIMIENTO DE REGLA DE CALIDAD", style);
        createCell(row, 13, "DENOMINADOR DE CUMPLIMIENTO DE REGLA DE CALIDAD", style);
        createCell(row, 14, "TIPO FRECUENCIA EJECUCION DE REGLA DE CALIDAD MODELO GLOBAL", style);
        createCell(row, 15, "PORCENTAJE UMBRAL MINIMO DE LA REGLA DE CALIDAD", style);
        createCell(row, 16, "PORCENTAJE UMBRAL OBJETIVO DE LA REGLA DE CALIDAD", style);
        createCell(row, 17, "NOMBRE CAMPO IMPORTE REGLA DE CALIDAD", style);
        createCell(row, 18, "PORCENTAJE CUMPLIMIENTO SALDO EN UNA REGLA DE CALIDAD", style);
        createCell(row, 19, "IMPORTE NUMERADOR REGLA DE CALIDAD", style);
        createCell(row, 20, "IMPORTE DENOMINADOR REGLA DE CALIDAD", style);
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
        Cell cell = row.createCell(columCount);

        if(value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if(value instanceof Integer){
            cell.setCellValue((Integer) value);
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        }else if(value instanceof Long){
            cell.setCellValue((Long) value);
        }else if(value instanceof Double){
            cell.setCellValue((Double) value);
        }else if(value instanceof String){
            cell.setCellValue((String) value);
        }

        cell.setCellStyle(style);
    }

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style2.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style2.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy;@"));

        for(PointRulesDQ rule: listRules){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,rule.getFechaCierre(),style2);
            createCell(row,columnCount++,rule.getFechaEjecucion(),style2);
            createCell(row,columnCount++,rule.getIdentificadorPais(),style);
            createCell(row,columnCount++,rule.getIdentificadorUuaa(),style);
            createCell(row,columnCount++,rule.getNombreDataSystem(),style);
            createCell(row,columnCount++,rule.getDescripcionDesglose(),style);
            createCell(row,columnCount++,rule.getIdentificadorSecuencialLegacy(),style);
            createCell(row,columnCount++,rule.getTipoPrincipio(),style);
            createCell(row,columnCount++,rule.getTipoRegla(),style);
            createCell(row,columnCount++,rule.getNombreFisicoObjeto(),style);
            createCell(row,columnCount++,rule.getNombreFisicoCampo(),style);
            createCell(row,columnCount++,rule.getPorcentajeCumplimiento(),style1);
            createCell(row,columnCount++,rule.getNumeradorCumplimiento(),style1);
            createCell(row,columnCount++,rule.getDenominadorCumplimiento(),style1);
            createCell(row,columnCount++,rule.getTipoFrecuenciaEjecucion(),style);
            createCell(row,columnCount++,rule.getPorcentajeUmbralMinimo(),style1);
            createCell(row,columnCount++,rule.getPorcentajeUmbralObjetivo(),style1);
            createCell(row,columnCount++,rule.getNombreCampoImporte(),style);
            createCell(row,columnCount++,rule.getPorcentajeCumplimientoSaldo(),style1);
            createCell(row,columnCount++,rule.getImporteNumerador(),style1);
            createCell(row,columnCount++,rule.getImporteDenominador(),style1);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Data_Quality");
        Row row = sheet.createRow(0);
        Row row1 = sheet.createRow(1);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font1 = workbook.createFont();
        font1.setBold(false);
        font1.setFontHeight(10);
        style1.setFont(font1);

        createCell(row, 0, "#Exitosos", style);
        createCell(row, 1, "#Fallidos", style);
        createCell(row, 2, "Estado Final", style);

        createCell(row1, 0, rulesList.get(rulesList.size()-1)[0], style1);
        createCell(row1, 1, rulesList.get(rulesList.size()-1)[1], style1);
        createCell(row1, 2, rulesList.get(rulesList.size()-1)[2], style1);

        if(rulesList.size()>1)
        {
            rulesList.remove(rulesList.size()-1);
            Row row2 = sheet.createRow(2);
            createCell(row2, 0, "Fila", style);
            createCell(row2, 1, "Columna", style);
            createCell(row2, 2, "Estado", style);

            int rowCount = 3;
            for (String[] log : rulesList)
            {
                Row row3 = sheet.createRow(rowCount++);
                int columnCount = 0;
                createCell(row3, columnCount++, log[0], style1);
                createCell(row3, columnCount++, log[1], style1);
                createCell(row3, columnCount++, log[2], style1);
            }
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
