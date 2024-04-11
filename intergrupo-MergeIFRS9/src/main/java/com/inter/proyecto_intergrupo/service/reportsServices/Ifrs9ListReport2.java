package com.inter.proyecto_intergrupo.service.reportsServices;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Ifrs9ListReport2 {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private String monthPrev;
    private String month;
    private String type;

    private List<String[]> list;

    public Ifrs9ListReport2(List<String[]> list, String monthPrev, String month, String type){
        this.list = list;
        this.monthPrev = monthPrev;
        this.month = month;
        this.type = type;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Cruce Inicial");
        Row row = sheet.createRow(0);
        Row row2 = sheet.createRow(1);

        CellStyle style = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        XSSFFont font2 = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        font2.setBold(true);
        font2.setFontHeight(11);
        style.setFont(font);
        style2.setFont(font2);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        byte[] rgb = new byte[3];
        rgb[0] = (byte) 0; // red
        rgb[1] = (byte) 68; // green
        rgb[2] = (byte) 129; // blue

        IndexedColorMap colorMap = workbook.getStylesSource().getIndexedColors();
        XSSFColor color = new XSSFColor(rgb, colorMap);
        ((XSSFCellStyle) style).setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        if(type.equals("A")){
            createCell(row,0, "Vertical Saldos Ajustado", style2);
        }else if(type.equals("SA")){
            createCell(row,0, "Vertical Saldos Sin Ajuste", style2);
        }else if(type.equals("HA")){
            createCell(row,0, "Vertical Saldos Ajustado Holding", style2);
        }else if(type.equals("HSA")){
            createCell(row,0, "Vertical Saldos Sin Ajuste Holding", style2);
        }else{
            createCell(row,0, "", style2);
        }
        createCell(row,1, "", style2);
        createCell(row,2, "", style2);
        createCell(row,3, monthPrev.substring(0,1).toUpperCase()+monthPrev.substring(1), style);
        createCell(row,4, "", style);
        createCell(row,5, "", style);
        createCell(row,6, "", style);
        createCell(row,7, month.substring(0,1).toUpperCase()+month.substring(1), style);
        createCell(row,8, "", style);
        createCell(row,9, "", style);
        createCell(row,10, "", style);
        createCell(row,11, "", style2);

        createCell(row2,0, "Consulta", style);
        createCell(row2,1, "Instrumento", style);
        createCell(row2,2, "Subproducto", style);
        createCell(row2,3, "Stage 1", style);
        createCell(row2,4, "Stage 2", style);
        createCell(row2,5, "Stage 3", style);
        createCell(row2,6, "Total", style);
        createCell(row2,7, "Stage 1", style);
        createCell(row2,8, "Stage 2", style);
        createCell(row2,9, "Stage 3", style);
        createCell(row2,10, "Total", style);
        createCell(row2,11, "Variación", style);

    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
        //sheet.autoSizeColumn(columCount);
        Cell cell = row.createCell(columCount);

        if(value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if(value instanceof Integer){
            cell.setCellValue((Integer) value);
        } else if(value instanceof Double){
            cell.setCellValue((Double) value);
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        } else if(value instanceof Long){
            cell.setCellValue((Long) value);
        }else if(value instanceof String){
            cell.setCellValue((String) value);
        }

        cell.setCellStyle(style);
    }

    private void writeDataLines(){
        int rowCount = 2;

        CellStyle style = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();

        CellStyle style3 = workbook.createCellStyle();
        CellStyle style4 = workbook.createCellStyle();

        XSSFFont font = workbook.createFont();
        XSSFFont font2 = workbook.createFont();
        font.setFontHeight(10);
        font2.setFontHeight(10);
        font2.setBold(true);
        style.setFont(font);
        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        style3.setFont(font2);
        style4.setFont(font2);
        style4.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] list: list){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            if(list[1].toString().substring(0, 5).equals("TOTAL")) {
                createCell(row, columnCount++, "", style3);
                createCell(row, columnCount++, list[1], style3);
                createCell(row, columnCount++, list[2], style3);
                createCell(row, columnCount++, Double.parseDouble(list[3].toString()), style4);
                createCell(row, columnCount++, Double.parseDouble(list[4].toString()), style4);
                createCell(row, columnCount++, Double.parseDouble(list[5].toString()), style4);
                createCell(row, columnCount++, Double.parseDouble(list[6].toString()), style4);
                createCell(row, columnCount++, Double.parseDouble(list[7].toString()), style4);
                createCell(row, columnCount++, Double.parseDouble(list[8].toString()), style4);
                createCell(row, columnCount++, Double.parseDouble(list[9].toString()), style4);
                createCell(row, columnCount++, Double.parseDouble(list[10].toString()), style4);
                createCell(row, columnCount++, Double.parseDouble(list[11].toString()), style4);

            }else{

                createCell(row, columnCount++, list[0], style);
                createCell(row, columnCount++, list[1], style);
                createCell(row, columnCount++, list[2], style);
                createCell(row, columnCount++, Double.parseDouble(list[3].toString()), style2);
                createCell(row, columnCount++, Double.parseDouble(list[4].toString()), style2);
                createCell(row, columnCount++, Double.parseDouble(list[5].toString()), style2);
                createCell(row, columnCount++, Double.parseDouble(list[6].toString()), style2);
                createCell(row, columnCount++, Double.parseDouble(list[7].toString()), style2);
                createCell(row, columnCount++, Double.parseDouble(list[8].toString()), style2);
                createCell(row, columnCount++, Double.parseDouble(list[9].toString()), style2);
                createCell(row, columnCount++, Double.parseDouble(list[10].toString()), style2);
                createCell(row, columnCount++, Double.parseDouble(list[11].toString()), style2);

            }

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

}
