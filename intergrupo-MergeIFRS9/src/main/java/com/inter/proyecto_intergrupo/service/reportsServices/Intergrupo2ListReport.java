package com.inter.proyecto_intergrupo.service.reportsServices;


import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1Final;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV2;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV2Final;
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

public class Intergrupo2ListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<IntergrupoV2> interList;

    public Intergrupo2ListReport(List<IntergrupoV2> interList) {
        workbook = new XSSFWorkbook();
        this.interList = interList;
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Integrupo V2");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "YNTP Empresa Reportante", style);
        createCell(row, 1, "Cod Neocon", style);
        createCell(row, 2, "Divisa", style);
        createCell(row, 3, "Yntp", style);
        createCell(row, 4, "Sociedad YNTP", style);
        createCell(row, 5, "Contrato", style);
        createCell(row, 6, "NIT Contraparte", style);
        createCell(row, 7, "Valor", style);
        createCell(row, 8, "Cod País", style);
        createCell(row, 9, "País", style);
        createCell(row, 10, "Cuenta Local", style);
        createCell(row, 11, "Periodo", style);
        createCell(row, 12, "Fuente", style);
        createCell(row, 13, "Input", style);
        createCell(row, 14, "Componente", style);

    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
        Cell cell = row.createCell(columCount);

        if(value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if(value instanceof Integer){
            cell.setCellValue((Integer) value);
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        } else if(value instanceof String){
            cell.setCellValue((String) value);
        }else if(value instanceof Long){
            cell.setCellValue((Long) value);
        }else if(value instanceof Double){
            cell.setCellValue((Double) value);
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


        for(IntergrupoV2 inter : interList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, inter.getYntpReportante(),style);
            createCell(row,columnCount++, inter.getCodNeocon(),style);
            createCell(row,columnCount++, inter.getDivisa(),style);
            createCell(row,columnCount++, inter.getYntp(),style);
            createCell(row,columnCount++, inter.getSociedadYntp(),style);
            createCell(row,columnCount++, inter.getContrato(),style);
            createCell(row,columnCount++, inter.getNit(),style);
            createCell(row,columnCount++, inter.getValor(),style1);
            createCell(row,columnCount++, inter.getCodPais(),style);
            createCell(row,columnCount++, inter.getPais(),style);
            createCell(row,columnCount++, inter.getCuentaLocal(),style);
            createCell(row,columnCount++, inter.getPeriodo(),style);
            createCell(row,columnCount++, inter.getFuente(),style);
            createCell(row,columnCount++, inter.getInput(),style);
            createCell(row,columnCount++, inter.getComponente(),style);
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

    private void writeDataLinesFinal(List<IntergrupoV2Final> intList){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));


        for(IntergrupoV2Final inter : intList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, inter.getYntpReportante(),style);
            createCell(row,columnCount++, inter.getCodNeocon(),style);
            createCell(row,columnCount++, inter.getDivisa(),style);
            createCell(row,columnCount++, inter.getYntp(),style);
            createCell(row,columnCount++, inter.getSociedadYntp(),style);
            createCell(row,columnCount++, inter.getContrato(),style);
            createCell(row,columnCount++, inter.getNit(),style);
            createCell(row,columnCount++, inter.getValor(),style1);
            createCell(row,columnCount++, inter.getCodPais(),style);
            createCell(row,columnCount++, inter.getPais(),style);
            createCell(row,columnCount++, inter.getCuentaLocal(),style);
            createCell(row,columnCount++, inter.getPeriodo(),style);
            createCell(row,columnCount++, inter.getFuente(),style);
            createCell(row,columnCount++, inter.getInput(),style);
            createCell(row,columnCount++, inter.getComponente(),style);
        }
    }

    public void exportFinal(HttpServletResponse response,List<IntergrupoV2Final> intList) throws IOException {
        writeHeaderLine();
        writeDataLinesFinal(intList);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
    private void writeHeaderLineLog(){
        sheet = workbook.createSheet("Log Intergrupo V2");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Fila", style);
        createCell(row, 1, "Columna", style);
        createCell(row, 2, "Comentario", style);

    }

    private void writeDataLinesLog(List<String[]> list){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(Object[] listpart: list){
            if(listpart[2].equals("COMPLETE") || listpart[2].equals("FAIL") || listpart[2].equals("FAILED"))
            {
                style = workbook.createCellStyle();
                font = workbook.createFont();
                font.setBold(true);
                font.setFontHeight(11);
                style.setFont(font);

                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;
                createCell(row,columnCount++,"# Exitosos",style);
                createCell(row,columnCount++,"# Fallidos",style);
                createCell(row,columnCount++,"Estado Final",style);
            }

            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,listpart[0],style);
            createCell(row,columnCount++,listpart[1],style);
            createCell(row,columnCount++,listpart[2],style);
        }
    }

    public void exportLog(HttpServletResponse response, List<String[]> list) throws IOException {
        writeHeaderLineLog();
        writeDataLinesLog(list);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
