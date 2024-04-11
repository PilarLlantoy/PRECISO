package com.inter.proyecto_intergrupo.service.prechargeService;

import com.inter.proyecto_intergrupo.model.bank.TaxBase;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.precharges.ComerPrecharge;
import com.inter.proyecto_intergrupo.repository.parametric.ThirdRepository;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComerPrechargeListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private ArrayList<ComerPrecharge> finalPrecharge;
    private ThirdService thirdService;
    private static final short size = 10;

    public ComerPrechargeListReport(ArrayList<ComerPrecharge> finalPrecharge) {
        this.finalPrecharge = finalPrecharge;
        workbook = new SXSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Precarga Comer");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size);
        style.setFont(font);

        createCell(row, 0, "Yntp Reportante", style);
        createCell(row, 1, "Cod Neocon", style);
        createCell(row, 2, "Divisa", style);
        createCell(row, 3, "Yntp", style);
        createCell(row, 4, "Sociedad Yntp", style);
        createCell(row, 5, "Contrato", style);
        createCell(row, 6, "Nit Contraparte", style);
        createCell(row, 7, "Valor", style);
        createCell(row, 8, "Cod. País", style);
        createCell(row, 9, "País", style);
        createCell(row, 10, "Cuenta Local", style);
        createCell(row, 11, "Observación", style);
        createCell(row, 12, "Periodo", style);
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

        for(ComerPrecharge pre: finalPrecharge){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++, pre.getYntpReportante(),style);
            createCell(row,columnCount++, pre.getCodNeocon(),style);
            createCell(row,columnCount++, pre.getDivisa(),style);
            createCell(row,columnCount++, pre.getYntp(),style);
            createCell(row,columnCount++, pre.getSociedadYntp(),style);
            createCell(row,columnCount++, pre.getContrato(),style);
            createCell(row,columnCount++, pre.getNit(),style);
            createCell(row,columnCount++, pre.getValor(),style1);
            createCell(row,columnCount++, pre.getCodPais(),style);
            createCell(row,columnCount++, pre.getPais(),style);
            createCell(row,columnCount++, pre.getCuentaLocal(),style);
            createCell(row,columnCount++, pre.getObservaciones(),style);
            createCell(row,columnCount++, pre.getPeriodo(),style);
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

/*    public void exportLog(HttpServletResponse response, ArrayList<String[]> lista) throws IOException
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
    }*/
}

