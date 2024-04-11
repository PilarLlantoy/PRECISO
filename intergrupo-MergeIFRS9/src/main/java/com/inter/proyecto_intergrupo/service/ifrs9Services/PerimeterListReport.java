package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.Perimeter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PerimeterListReport {

    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private List<Perimeter> perimeterList;

    public PerimeterListReport(List<Perimeter> perimeterList){
        this.perimeterList = perimeterList;
        workbook = new SXSSFWorkbook();
    }

    //TODO
    private void writeHeaderLine(){
        sheet = workbook.createSheet("PerimetroIFRS9");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        short size = 11;
        font.setFontHeightInPoints(size);
        style.setFont(font);

        createCell(row, 0, "Segmento", style);
        createCell(row, 1, "Subproducto", style);
        createCell(row, 2, "Código de consolidación", style);
        createCell(row, 3, "Divisa", style);
        createCell(row, 4, "Saldo Query", style);
        createCell(row, 5, "Saldo EEFF", style);
        createCell(row, 6, "Diferencia de saldos", style);
        createCell(row, 7, "Fecha Proceso", style);

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

        Font font = workbook.createFont();
        short size = 10;
        font.setFontHeightInPoints(size);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Perimeter perimeter: perimeterList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,perimeter.getSegmento(),style);
            createCell(row,columnCount++,perimeter.getSubproducto(),style);
            createCell(row,columnCount++,perimeter.getCodConso(),style);
            createCell(row,columnCount++,perimeter.getDivisa(),style);
            createCell(row,columnCount++,Double.parseDouble(perimeter.getSaldoQuery().toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(perimeter.getSaldoEEFF().toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(perimeter.getDifSaldos().toString()),style1);
            createCell(row,columnCount,perimeter.getFechaProceso(),style);
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