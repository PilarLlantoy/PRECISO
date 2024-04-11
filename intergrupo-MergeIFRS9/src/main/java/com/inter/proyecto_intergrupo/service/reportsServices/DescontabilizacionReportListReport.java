package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.reports.DescontabilizacionReport;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DescontabilizacionReportListReport {
    private final SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private List<DescontabilizacionReport> descontabilizacionReportList;
    private static final short SIZE = 10;
    private static final short SIZE2 = 11;

    public DescontabilizacionReportListReport(List<DescontabilizacionReport> descontabilizacionReportList){
        this.descontabilizacionReportList = descontabilizacionReportList;
        workbook = new SXSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Descontabilización");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(SIZE2);
        style.setFont(font);

        createCell(row,0, "Fuente", style);
        createCell(row,	1, "Cuenta", style);
        createCell(row,	2, "Centro", style);
        createCell(row,	3, "Divisa", style);
        createCell(row,	4, "Contrato", style);
        createCell(row,	5, "Concepto", style);
        createCell(row,	6, "Saldo", style);
        createCell(row,	7, "Contrapartida", style);
        createCell(row,	8, "Contrato genérico", style);
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
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
        font.setFontHeightInPoints(SIZE);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(DescontabilizacionReport descon: descontabilizacionReportList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,descon.getFuenteInformacion() ,style);
            createCell(row,columnCount++,descon.getCuenta(),style);
            createCell(row,columnCount++,descon.getCentro(),style);
            createCell(row,columnCount++,descon.getDivisa(),style);
            createCell(row,columnCount++,descon.getContrato(),style);
            createCell(row,columnCount++,descon.getConcepto(),style);
            createCell(row,columnCount++,descon.getSaldo(),style1);
            createCell(row,columnCount++,descon.getContrapartida(),style);
            createCell(row,columnCount++,descon.getContratoGenerico(),style);
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