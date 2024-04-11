package com.inter.proyecto_intergrupo.service.reportsServices;


import com.inter.proyecto_intergrupo.model.reports.IntergrupoV2;
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

public class Intergrupo2VerListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Object[]> interList;

    public Intergrupo2VerListReport(List<Object[]> interList) {
        workbook = new XSSFWorkbook();
        this.interList = interList;
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Integrupo");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "YNTP Empresa Reportante", style);
        createCell(row, 1, "Cod. Neocon", style);
        createCell(row, 2, "Divisa", style);
        createCell(row, 3, "Divisa Neocon", style);
        createCell(row, 4, "YNTP", style);
        createCell(row, 5, "Sociedad YNTP", style);
        createCell(row, 6, "Contrato", style);
        createCell(row, 7, "NIT", style);
        createCell(row, 8, "Signo Motores", style);
        createCell(row, 9, "Valor Motores", style);
        createCell(row, 10, "Valor", style);
        createCell(row, 11, "Cod. País", style);
        createCell(row, 12, "País", style);
        createCell(row, 13, "País XX", style);
        createCell(row, 14, "Cuenta Local", style);
        createCell(row, 15, "Fecha", style);
        createCell(row, 16, "Fuente", style);
        createCell(row, 17, "Input", style);
        createCell(row, 18, "Componente", style);

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


        for(Object[] inter : interList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, inter[0],style);
            createCell(row,columnCount++, inter[1],style);
            createCell(row,columnCount++, inter[2],style);
            createCell(row,columnCount++, inter[3],style);
            createCell(row,columnCount++, inter[4],style);
            createCell(row,columnCount++, inter[5],style);
            createCell(row,columnCount++, inter[6],style);
            createCell(row,columnCount++, inter[7],style);
            createCell(row,columnCount++, inter[8],style);
            createCell(row,columnCount++, Double.parseDouble(inter[9].toString()),style1);
            createCell(row,columnCount++, Double.parseDouble(inter[10].toString()),style1);
            createCell(row,columnCount++, inter[11],style);
            createCell(row,columnCount++, inter[12],style);
            createCell(row,columnCount++, inter[13],style);
            createCell(row,columnCount++, inter[14],style);
            createCell(row,columnCount++, inter[15],style);
            createCell(row,columnCount++, inter[16],style);
            createCell(row,columnCount++, inter[17],style);
            createCell(row,columnCount++, inter[18],style);
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

    public void exportErrors(HttpServletResponse response) throws IOException {
        writeHeaderLineErrors();
        writeDataLinesErrors();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    private void writeHeaderLineErrors(){
        sheet = workbook.createSheet("Errores Generación Plano");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Código", style);
        createCell(row, 1, "Mensaje", style);

    }

    private void writeDataLinesErrors(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);


        for(Object[] inter : interList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, inter[0],style);
            createCell(row,columnCount++, inter[1],style);
        }
    }

}
