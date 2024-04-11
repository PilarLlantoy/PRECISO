package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.reports.ConciliationComer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ComerConciliationStatusListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ConciliationComer> comerList;

    public ComerConciliationStatusListReport(List<ConciliationComer> comerList){
        this.comerList = comerList;
        workbook = new XSSFWorkbook();
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

    private void writeCompleteHeaderLine(){
        sheet = workbook.createSheet("Conciliación Comercializadora");
        Row row = sheet.createRow(0);

        XSSFCellStyle style = workbook.createCellStyle();
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
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        createCell(row, 0, "Cuenta Banco", style);
        createCell(row,	1, "Cuenta Comercializadora", style);
        createCell(row,	2, "Nombre Cuenta Banco", style);
        createCell(row,	3, "Importe Comercializadora", style);
        createCell(row,	4, "Calculo Prorrata de IVA", style);
        createCell(row,	5, "Total Comercializadora", style);
        createCell(row,	6, "Importe Real", style);
        createCell(row,	7, "Importe Provision", style);
        createCell(row,	8, "Total GPS", style);
        createCell(row,	9, "Diferencias Totales", style);
        createCell(row,	10, "Importe Base Fiscal", style);
        createCell(row,	11, "Importe Balance", style);
        createCell(row,	12, "Diferencia de pagos reales", style);
        createCell(row,	13, "Diferencia Total GPS y Balance", style);
        createCell(row,	14, "Periodo", style);
    }

    private void writeCompleteDataLines(){
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

        for(ConciliationComer comer: comerList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            if(!comer.getCuentaBanco().equals("TOTALES")){
                createCell(row,columnCount++,comer.getCuentaBanco(),style);
                createCell(row,columnCount++,comer.getCuentaComercializadora(),style);
                createCell(row,columnCount++,comer.getNombreCuentaBanco(),style);
                createCell(row,columnCount++,(double)comer.getImporteComercializadora(),style1);
                createCell(row,columnCount++, (double) comer.getProrrataIva(),style1);
                createCell(row,columnCount++,(double) comer.getTotalComer(),style1);
                createCell(row,columnCount++,(double) comer.getImporteReal(),style1);
                createCell(row,columnCount++,(double) comer.getImporteProvisiones(),style1);
                createCell(row,columnCount++,(double) comer.getTotalGps(),style1);
                createCell(row,columnCount++,(double) comer.getDiferenciasTot(),style1);
                createCell(row,columnCount++,(double) comer.getImporteBaseFiscal(),style1);
                createCell(row,columnCount++,(double) comer.getImporteBalance(),style1);
                createCell(row,columnCount++,(double) comer.getDiferenciaPagosReales(),style1);
                createCell(row,columnCount++,(double) comer.getDiferenciaTotalGpsBalance(),style1);
                createCell(row,columnCount++,comer.getPeriodo(),style);
            } else{
                createCell(row,columnCount++,comer.getCuentaBanco(),style2);
                createCell(row,columnCount++,comer.getCuentaComercializadora(),style2);
                createCell(row,columnCount++,comer.getNombreCuentaBanco(),style2);
                createCell(row,columnCount++,(double)comer.getImporteComercializadora(),style3);
                createCell(row,columnCount++, (double) comer.getProrrataIva(),style3);
                createCell(row,columnCount++,(double) comer.getTotalComer(),style3);
                createCell(row,columnCount++,(double) comer.getImporteReal(),style3);
                createCell(row,columnCount++,(double) comer.getImporteProvisiones(),style3);
                createCell(row,columnCount++,(double) comer.getTotalGps(),style3);
                createCell(row,columnCount++,(double) comer.getDiferenciasTot(),style3);
                createCell(row,columnCount++,(double) comer.getImporteBaseFiscal(),style3);
                createCell(row,columnCount++,(double) comer.getImporteBalance(),style3);
                createCell(row,columnCount++,(double) comer.getDiferenciaPagosReales(),style3);
                createCell(row,columnCount++,(double) comer.getDiferenciaTotalGpsBalance(),style3);
                createCell(row,columnCount++,comer.getPeriodo(),style2);
            }


        }
    }

    public void exportConciliation(HttpServletResponse response) throws IOException {
        writeCompleteHeaderLine();
        writeCompleteDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Reporte Concilizacion Comer");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Cuenta banco", style);
        createCell(row,	1, "Cuenta Comercializadora", style);
        createCell(row,	2, "Diferencia Pagos Reales", style);
        createCell(row,	3, "Diferencia Total GPS", style);
        createCell(row,	4, "Estatus", style);
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

        for(ConciliationComer object: comerList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,object.getCuentaBanco(),style);
            createCell(row,columnCount++,object.getCuentaComercializadora(),style);
            createCell(row,columnCount++,(double) object.getDiferenciaPagosReales(),style1);
            createCell(row,columnCount++,(double) object.getDiferenciaPagosReales(),style1);
            createCell(row,columnCount++,object.getDiferenciaTotalGpsBalance(),style1);
        }
    }

    public void exportResume(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}
