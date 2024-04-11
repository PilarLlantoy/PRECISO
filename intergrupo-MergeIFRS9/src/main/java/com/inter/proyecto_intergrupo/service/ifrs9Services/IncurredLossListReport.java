package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.IncurredLoss;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IncurredLossListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private List<IncurredLoss> lossList;
    private List<Object[]> lossListObject;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public IncurredLossListReport(List<IncurredLoss> lossList,List<Object[]> lossListObject){
        this.lossList = lossList;
        this.lossListObject = lossListObject;
        workbook = new SXSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Perdida Incurrida");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        short short1 = 11;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(short1);
        style.setFont(font);

        createCell(row, 0, "Cuenta", style);
        createCell(row, 1, "Centro", style);
        createCell(row, 2, "Contrato", style);
        createCell(row, 3, "Segmento", style);
        createCell(row, 4, "Stage", style);
        createCell(row, 5, "Indicador contrato", style);
        createCell(row, 6, "Saldo", style);
        createCell(row, 7, "Código consolidación", style);
    }

    private void writeHeaderLineResume(){
        sheet = workbook.createSheet("Perdida Incurrida");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        short short1 = 11;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(short1);
        style.setFont(font);

        createCell(row, 0, "Cuenta", style);
        createCell(row, 1, "Centro", style);
        createCell(row, 2, "Saldo", style);
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
        short short1 = 10;
        short short2 = 10;
        Font font = workbook.createFont();
        Font font1 = workbook.createFont();
        font.setFontHeightInPoints(short1);
        font1.setFontHeightInPoints(short2);
        style.setFont(font);
        style1.setFont(font1);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(IncurredLoss loss: lossList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,loss.getCuenta(),style);
            createCell(row,columnCount++,loss.getCentro(),style);
            createCell(row,columnCount++,loss.getContrato(),style);
            createCell(row,columnCount++,loss.getSegmento(),style);
            createCell(row,columnCount++,loss.getStage(),style);
            createCell(row,columnCount++,loss.getIndicadorcontrato(),style);
            createCell(row,columnCount++,loss.getSaldo(),style1);
            createCell(row,columnCount++,loss.getCodigoconso(),style);
        }
    }

    private void writeDataLinesResume(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        short short1 = 10;
        short short2 = 10;
        Font font = workbook.createFont();
        Font font1 = workbook.createFont();
        font.setFontHeightInPoints(short1);
        font1.setFontHeightInPoints(short2);
        style.setFont(font);
        style1.setFont(font1);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] loss: lossListObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,loss[0].toString(),style);
            createCell(row,columnCount++,loss[1].toString(),style);
            createCell(row,columnCount++,Double.parseDouble(loss[2].toString()),style1);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        if(lossList.size()>0) {
            writeHeaderLine();
            writeDataLines();
        }
        else
        {
            sheet = workbook.createSheet("Perdida Incurrida");
            sheet.setRandomAccessWindowSize(1000);
            Row row = sheet.createRow(0);

            short short1 = 11;
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints(short1);
            style.setFont(font);

            createCell(row, 0, "La cantidad de registros de descarga no es soportado por Excel ", style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportResume(HttpServletResponse response) throws IOException {
        writeHeaderLineResume();
        writeDataLinesResume();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }


}
