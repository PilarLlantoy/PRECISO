package com.inter.proyecto_intergrupo.service.reportsServices;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Rp21DiffListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Object[]> DiffList;

    public Rp21DiffListReport(List<Object[]> diffList, int i){
        this.DiffList = diffList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Diferencias");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Fecha Contable", style);
        createCell(row, 1, "Cuenta", style);
        createCell(row, 2, "Divisa", style);
        createCell(row, 3, "Tipo de Operación", style);
        createCell(row, 4, "Saldo RP21", style);
        createCell(row, 5, "Saldo Mes", style);
        createCell(row, 6, "Diferencia", style);
        createCell(row, 7, "Fecha Proceso Query", style);

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

        for(Object[] difference: DiffList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,difference[0], style);
            createCell(row,columnCount++,difference[1], style);
            createCell(row,columnCount++,difference[2], style);
            createCell(row,columnCount++,difference[3], style);
            createCell(row,columnCount++,difference[4], style1);
            createCell(row,columnCount++,difference[5], style1);
            createCell(row,columnCount++,difference[6], style1);
            if(difference[7]!=null)
                createCell(row,columnCount++,difference[7].toString().replace("00:00:00.0",""), style);
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

    public void exportLog(HttpServletResponse response, ArrayList<String[]> lista) throws IOException
    {
        sheet = workbook.createSheet("Log");
        Row row1 = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(14);
        style.setFont(font);

        createCell(row1, 0, "Fecha Contable", style);
        createCell(row1, 1, "Cuenta", style);
        createCell(row1, 2, "Divisa", style);
        createCell(row1, 3, "Tipo de Operación", style);
        createCell(row1, 4, "Saldo RP21", style);
        createCell(row1, 5, "Saldo Mes", style);
        createCell(row1, 6, "Diferencia", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(13);

        for(String[] currency: lista){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,currency[0],style);
            createCell(row,columnCount++,currency[1],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}
