package com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class RechazosDescontabilizacionListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private SXSSFSheet sheet1;
    private List<Object[]> rechazosDescontabilizacionList;
    String opcion;
    private static final short size = 10;
    private static final short size2 = 11;

    public RechazosDescontabilizacionListReport(List<Object[]> rechazosDescontabilizacionList, String opcion){
        this.rechazosDescontabilizacionList = rechazosDescontabilizacionList;
        workbook = new SXSSFWorkbook();
        this.opcion = opcion;
    }

    private void writeHeaderLine(){

        if(!opcion.equals("CV")) {
            sheet = workbook.createSheet("RechazosDescontabilizacion");
            sheet.setRandomAccessWindowSize(1000);
            Row row = sheet.createRow(0);

            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints(size2);
            style.setFont(font);

            createCell(row, 0, "Cuenta", style);
            createCell(row, 1, "Centro", style);
            createCell(row, 2, "Divisa", style);
            createCell(row, 3, "Contrato", style);
            createCell(row, 4, "Saldo", style);
            createCell(row, 5, "Periodo", style);
            createCell(row, 6, "Descripción", style);
        }
        else
        {
            sheet = workbook.createSheet("Coincide");
            sheet.setRandomAccessWindowSize(1000);
            Row row0 = sheet.createRow(0);

            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints(size2);
            style.setFont(font);

            createCell(row0, 0, "CUENTA", style);
            createCell(row0, 1, "DIFERENCIA", style);
            createCell(row0, 2, "REC_COD_PROCESO", style);
            createCell(row0, 3, "REC_COD_CCONTR", style);
            createCell(row0, 4, "REC_COD_CTACONT", style);
            createCell(row0, 5, "REC_COD_SALDO", style);
            createCell(row0, 6, "SEGMENT", style);

            sheet1 = workbook.createSheet("No Coincide");
            sheet1.setRandomAccessWindowSize(1000);
            Row row = sheet1.createRow(0);

            createCell(row, 0, "CUENTA", style);
            createCell(row, 1, "DIFERENCIA", style);
            createCell(row, 2, "REC_COD_PROCESO", style);
            createCell(row, 3, "REC_COD_CCONTR", style);
            createCell(row, 4, "REC_COD_CTACONT", style);
            createCell(row, 5, "REC_COD_SALDO", style);
            createCell(row, 6, "SEGMENT", style);
        }
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
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();

        Font font = workbook.createFont();
        short size = 10;
        font.setFontHeightInPoints(size);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        if(opcion.equals("Pre-Carga")) {
            for (Object[] rechazoDescontabilizacion : rechazosDescontabilizacionList) {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;


                createCell(row, columnCount++, rechazoDescontabilizacion[3].toString(), style);
                createCell(row, columnCount++, rechazoDescontabilizacion[1].toString(), style);
                createCell(row, columnCount++, rechazoDescontabilizacion[4].toString(), style);
                createCell(row, columnCount++, rechazoDescontabilizacion[2].toString(), style);
                createCell(row, columnCount++, Double.parseDouble(rechazoDescontabilizacion[7].toString()), style1);
                createCell(row, columnCount++, rechazoDescontabilizacion[6].toString(), style);
                if(rechazoDescontabilizacion.length>8 &&rechazoDescontabilizacion[8]!= null && rechazoDescontabilizacion[8].toString()!=null)
                    createCell(row, columnCount, rechazoDescontabilizacion[8].toString(), style);
            }
        }else if(opcion.equals("CV")) {
            int rowCountA1 = 1;
            for (Object[] rechazoDescontabilizacion : rechazosDescontabilizacionList) {
                if(rechazoDescontabilizacion[2] != null) {
                    Row row = sheet.createRow(rowCount++);
                    int columnCount = 0;
                    createCell(row, columnCount++, rechazoDescontabilizacion[0].toString(), style);
                    createCell(row, columnCount++, Double.parseDouble(rechazoDescontabilizacion[1].toString()), style1);
                    createCell(row, columnCount++, rechazoDescontabilizacion[2].toString(), style);
                    createCell(row, columnCount++, rechazoDescontabilizacion[3].toString(), style);
                    createCell(row, columnCount++, rechazoDescontabilizacion[4].toString(), style);
                    createCell(row, columnCount++, rechazoDescontabilizacion[5].toString(), style);
                    createCell(row, columnCount++, rechazoDescontabilizacion[6].toString(), style);
                }
                else {
                    Row row = sheet1.createRow(rowCountA1++);
                    int columnCount = 0;
                    createCell(row, columnCount++, rechazoDescontabilizacion[0].toString(), style);
                    createCell(row, columnCount++, Double.parseDouble(rechazoDescontabilizacion[1].toString()), style1);
                    /*createCell(row, columnCount++, rechazoDescontabilizacion[2].toString(), style);
                    createCell(row, columnCount++, rechazoDescontabilizacion[3].toString(), style);
                    createCell(row, columnCount++, rechazoDescontabilizacion[4].toString(), style);
                    createCell(row, columnCount++, rechazoDescontabilizacion[5].toString(), style);
                    createCell(row, columnCount++, rechazoDescontabilizacion[6].toString(), style);*/
                }
            }
        }
        else {
            for (Object[] rechazoDescontabilizacion : rechazosDescontabilizacionList) {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;


                createCell(row, columnCount++, rechazoDescontabilizacion[3].toString(), style);
                createCell(row, columnCount++, rechazoDescontabilizacion[1].toString(), style);
                createCell(row, columnCount++, rechazoDescontabilizacion[4].toString(), style);
                createCell(row, columnCount++, rechazoDescontabilizacion[2].toString(), style);
                createCell(row, columnCount++, Double.parseDouble(rechazoDescontabilizacion[6].toString()), style1);
                createCell(row, columnCount++, rechazoDescontabilizacion[5].toString(), style);
                if(rechazoDescontabilizacion.length>7 &&rechazoDescontabilizacion[7]!= null && rechazoDescontabilizacion[7].toString()!=null)
                createCell(row, columnCount, rechazoDescontabilizacion[7].toString(), style);
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
