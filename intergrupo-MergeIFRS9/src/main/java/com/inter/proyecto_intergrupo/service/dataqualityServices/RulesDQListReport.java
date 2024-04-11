package com.inter.proyecto_intergrupo.service.dataqualityServices;

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

public class RulesDQListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> rulesList;
    private List<RulesDQ> listRules;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public RulesDQListReport(List<String[]> rulesList,List<RulesDQ> listRules) {
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

        createCell(row, 0, "Tipo Principio", style);
        createCell(row, 1, "Tipo Regla", style);
        createCell(row, 2, "Tabla", style);
        createCell(row, 3, "Columna", style);
        createCell(row, 4, "Formato", style);
        createCell(row, 5, "Longitud", style);
        createCell(row, 6, "Identificador", style);
        createCell(row, 7, "Fichero", style);
        createCell(row, 8, "Contraparte", style);
        createCell(row, 9, "Campo", style);
        createCell(row, 10, "Valor", style);
        createCell(row, 11, "Umbral Mínimo", style);
        createCell(row, 12, "Umbral Objetivo", style);
        createCell(row, 13, "Descripción Funcional", style);
        createCell(row, 14, "Variación Minima", style);
        createCell(row, 15, "Variación Maxima", style);
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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(RulesDQ rule: listRules){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,rule.getTipoPrincipio(),style);
            createCell(row,columnCount++,rule.getTipoRegla(),style);
            createCell(row,columnCount++,rule.getTabla(),style);
            createCell(row,columnCount++,rule.getColumna(),style);
            createCell(row,columnCount++,rule.getFormato(),style);
            createCell(row,columnCount++,rule.getLongitud(),style);
            createCell(row,columnCount++,rule.getIdentificador(),style);
            createCell(row,columnCount++,rule.getFichero(),style);
            createCell(row,columnCount++,rule.getContraparte(),style);
            createCell(row,columnCount++,rule.getCampo(),style);
            createCell(row,columnCount++,rule.getValor(),style);
            createCell(row,columnCount++,rule.getUmbralMinimo(),style1);
            createCell(row,columnCount++,rule.getUmbralObjetivo(),style1);
            createCell(row,columnCount++,rule.getDescripcion(),style);
            createCell(row,columnCount++,rule.getVariacionMin(),style1);
            createCell(row,columnCount++,rule.getVariacionMax(),style1);
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
