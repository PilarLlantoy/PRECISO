package com.inter.proyecto_intergrupo.service.bankServices;

import com.inter.proyecto_intergrupo.model.bank.TaxBase;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class TaxBaseListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private ArrayList<TaxBase> finalTaxBase;
    private ArrayList<Object[]> noMatch;
    private static final short size = 10;

    public TaxBaseListReport(ArrayList<TaxBase> finalTaxBase, ArrayList<Object[]> noMatch) {
        this.finalTaxBase = finalTaxBase;
        this.noMatch = noMatch;
        workbook = new SXSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Base Fiscal");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size);
        style.setFont(font);

        createCell(row, 0, "COD NEOCON", style);
        createCell(row, 1, "DIVISA", style);
        createCell(row, 2, "YNTP", style);
        createCell(row, 3, "SOCIEDADES YNTP", style);
        createCell(row, 4, "CONTRATO", style);
        createCell(row, 5, "NIT Contraparte", style);
        createCell(row, 6, "VALOR", style);
        createCell(row, 7, "COD. PAÍS", style);
        createCell(row, 8, "PAÍS", style);
        createCell(row, 9, "CUENTA LOCAL", style);
        createCell(row, 10, "PERIODO", style);
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

    private void writeDataLinesMatch(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontHeightInPoints(size);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(TaxBase tax: finalTaxBase){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++, tax.getCodNeocon(),style);
            createCell(row,columnCount++, tax.getDivisa(),style);
            createCell(row,columnCount++, tax.getYntp(),style);
            createCell(row,columnCount++, tax.getSociedadYntp(),style);
            createCell(row,columnCount++, tax.getContrato(),style);
            createCell(row,columnCount++, tax.getNitContraparte(),style);
            createCell(row,columnCount++, tax.getValor(),style1);
            createCell(row,columnCount++, tax.getCodPais(),style);
            createCell(row,columnCount++, tax.getPais(),style);
            createCell(row,columnCount++, tax.getCuentaLocal(),style);
            createCell(row,columnCount++, tax.getPeriodo(),style);
        }
    }

    private void writeDataLinesNoMatch(){

        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();


        Font font = workbook.createFont();
        font.setFontHeightInPoints(size);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] tax: noMatch){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,tax[0].toString(),style);
            createCell(row,columnCount++,tax[1].toString(),style);
            createCell(row,columnCount++,tax[2].toString(),style);
            createCell(row,columnCount++,tax[3].toString(),style);
            createCell(row,columnCount++,tax[4].toString(),style);
            createCell(row,columnCount++,tax[5].toString(),style);
            createCell(row,columnCount++,Double.parseDouble(tax[6].toString()),style1);
            createCell(row,columnCount++,tax[7].toString(),style);
            createCell(row,columnCount++,tax[8].toString(),style);
            createCell(row,columnCount++,tax[9].toString(),style);
        }
    }

    public void exportMatch(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLinesMatch();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportNoMatch(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLinesNoMatch();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLog(HttpServletResponse response, ArrayList<String[]> lista) throws IOException
    {
        sheet = workbook.createSheet("Log");
        sheet.setRandomAccessWindowSize(100);
        Row row1 = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size);
        style.setFont(font);

        createCell(row1, 0, "GOF INPUT", style);
        createCell(row1, 1, "NIT Tercero", style);
        createCell(row1, 2, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight((short) 10);

        for(String[] tax: lista){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,tax[0],style);
            createCell(row,columnCount++,tax[2],style);
            createCell(row,columnCount++,tax[1],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
