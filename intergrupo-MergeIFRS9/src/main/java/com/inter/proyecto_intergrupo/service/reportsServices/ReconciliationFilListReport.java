package com.inter.proyecto_intergrupo.service.reportsServices;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.tools.ant.taskdefs.Java;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReconciliationFilListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet2;
    private List<Object[]> DiffList;
    private List<Object[]> AccList;
    private String Level;


    public ReconciliationFilListReport(List<Object[]> diffList, List<Object[]> accList, String level){
        this.DiffList = diffList;
        this.AccList = accList;
        this.Level = level;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
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

        if(Level.equals("D")) {

            sheet = workbook.createSheet("Conciliación");
            Row row = sheet.createRow(0);
            createCell(row, 0, "YNTP", style);
            createCell(row, 1, "Concepto", style);
            createCell(row, 2, "Cuenta Banco", style);
            createCell(row, 3, "Cuenta Neocon Banco", style);
            createCell(row, 4, "Cuenta Filial", style);
            createCell(row, 5, "Cuenta Neocon Filial", style);
            createCell(row, 6, "Contrato", style);
            createCell(row, 7, "Valor Banco", style);
            createCell(row, 8, "Valor Filial", style);
            createCell(row, 9, "Diferencia", style);
            createCell(row, 10, "Justificación", style);

        }else{

            sheet = workbook.createSheet("Conciliación");
            Row row = sheet.createRow(0);
            createCell(row, 0, "YNTP", style);
            createCell(row, 1, "Concepto", style);
            createCell(row, 2, "Cuenta Banco", style);
            createCell(row, 3, "Cuenta Neocon Banco", style);
            createCell(row, 4, "Cuenta Filial", style);
            createCell(row, 5, "Cuenta Neocon Filial", style);
            createCell(row, 6, "Valor Banco", style);
            createCell(row, 7, "Valor Filial", style);
            createCell(row, 8, "Diferencia", style);
            createCell(row, 9, "Justificación", style);

        }

        sheet2 = workbook.createSheet("Cuentas por parametrizar");
        Row row2 = sheet2.createRow(0);
        createCell(row2, 0, "YNTP", style);
        createCell(row2, 1, "Cuenta", style);
        createCell(row2, 2, "Banco/Filial", style);

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
        CellStyle style2 = workbook.createCellStyle();
        CellStyle style3 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        XSSFFont font2 = workbook.createFont();
        XSSFFont font3 = workbook.createFont();

        font.setFontHeight(10);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        font2.setFontHeight(10);
        font2.setBold(true);
        font2.setColor(IndexedColors.WHITE.getIndex());
        style2.setFont(font2);

        byte[] rgb = new byte[3];
        rgb[0] = (byte) 0; // red
        rgb[1] = (byte) 68; // green
        rgb[2] = (byte) 129; // blue

        IndexedColorMap colorMap = workbook.getStylesSource().getIndexedColors();
        XSSFColor color = new XSSFColor(rgb, colorMap);
        ((XSSFCellStyle) style2).setFillForegroundColor(color);
        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        font3.setFontHeight(10);
        font3.setBold(true);
        font3.setColor(IndexedColors.WHITE.getIndex());
        style3.setFont(font3);
        style3.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        ((XSSFCellStyle) style3).setFillForegroundColor(color);
        style3.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        if(Level.equals("D")) {

            for (Object[] difference : DiffList) {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;

                if (!difference[8].toString().equals("TOTAL")) {
                    createCell(row, columnCount++, difference[0], style);
                    createCell(row, columnCount++, difference[1], style);
                    createCell(row, columnCount++, difference[2], style);
                    createCell(row, columnCount++, difference[11], style);
                    createCell(row, columnCount++, difference[3], style);
                    createCell(row, columnCount++, difference[12], style);
                    createCell(row, columnCount++, difference[4], style);
                    createCell(row, columnCount++, difference[5], style1);
                    createCell(row, columnCount++, difference[6], style1);
                    createCell(row, columnCount++, "", style1);
                    createCell(row, columnCount++, difference[10], style1);
                } else {
                    createCell(row, columnCount++, difference[0], style2);
                    createCell(row, columnCount++, difference[1], style2);
                    createCell(row, columnCount++, difference[2], style2);
                    createCell(row, columnCount++, difference[11], style2);
                    createCell(row, columnCount++, difference[3], style2);
                    createCell(row, columnCount++, difference[12], style2);
                    createCell(row, columnCount++, difference[4], style2);
                    createCell(row, columnCount++, difference[5], style3);
                    createCell(row, columnCount++, difference[6], style3);
                    createCell(row, columnCount++, difference[7], style3);
                    createCell(row, columnCount++, difference[10], style3);
                }
            }

        }else{

            for (Object[] difference : DiffList) {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;

                if (!difference[8].toString().equals("TOTAL")) {
                    createCell(row, columnCount++, difference[0], style);
                    createCell(row, columnCount++, difference[1], style);
                    createCell(row, columnCount++, difference[2], style);
                    createCell(row, columnCount++, difference[9], style);
                    createCell(row, columnCount++, difference[3], style);
                    createCell(row, columnCount++, difference[10], style);
                    createCell(row, columnCount++, difference[4], style1);
                    createCell(row, columnCount++, difference[5], style1);
                    createCell(row, columnCount++, "", style1);
                    createCell(row, columnCount++, difference[7], style1);
                } else {
                    createCell(row, columnCount++, difference[0], style2);
                    createCell(row, columnCount++, difference[1], style2);
                    createCell(row, columnCount++, difference[2], style2);
                    createCell(row, columnCount++, difference[9], style2);
                    createCell(row, columnCount++, difference[3], style2);
                    createCell(row, columnCount++, difference[10], style2);
                    createCell(row, columnCount++, difference[4], style3);
                    createCell(row, columnCount++, difference[5], style3);
                    createCell(row, columnCount++, difference[6], style3);
                    createCell(row, columnCount++, difference[7], style3);
                }
            }

        }

        rowCount = 1;

        for(Object[] difference: AccList){
            Row row = sheet2.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,difference[0] ,style);
            createCell(row,columnCount++,difference[1] ,style);
            createCell(row,columnCount++,difference[2] ,style);

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
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "YNTP", style);
        createCell(row1, 1, "Concepto", style);
        createCell(row1, 1, "Cuenta Local", style);
        createCell(row1, 2, "Cuenta Filial", style);
        createCell(row1, 3, "Contrato", style);
        createCell(row1, 4, "Valor Banco", style);
        createCell(row1, 5, "Valor Filial", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

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
