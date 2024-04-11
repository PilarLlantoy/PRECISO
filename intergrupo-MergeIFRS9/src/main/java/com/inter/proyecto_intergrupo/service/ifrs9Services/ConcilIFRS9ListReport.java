package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.IFRS9ConcilAccount;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ConcilIFRS9ListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private List<IFRS9ConcilAccount> repoList;
    private final short SIZE = 10;
    private final short SIZE2 = 11;


    public ConcilIFRS9ListReport(List<IFRS9ConcilAccount> cuadreList){
        this.repoList = cuadreList;
        workbook = new SXSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Conciliación IFRS9");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(SIZE2);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        byte[] rgb = new byte[3];
        rgb[0] = (byte) 0; // red
        rgb[1] = (byte) 68; // green
        rgb[2] = (byte) 129; // blue

        XSSFColor color = new XSSFColor(rgb,new DefaultIndexedColorMap());
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        int i = 0;

        createCell(row, i++, "Empresa", style);
        createCell(row,	i++, "Cuenta", style);
        createCell(row,	i++, "Descripción", style);
        createCell(row,	i++, "Codicons", style);
        createCell(row,	i++, "Saldo mes", style);
        createCell(row,	i++, "Saldo pérdida incurrida", style);
        createCell(row,	i++, "Saldo ajustes primera vez", style);
        createCell(row,	i++, "Saldo diferencias automáticas", style);
        createCell(row,	i++, "Saldo manuales", style);
        createCell(row,	i++, "Saldo Prov. Gen. Int.", style);
        createCell(row,	i++, "Saldo 1%", style);
        createCell(row,	i++, "Saldo desc nivel 15", style);
        createCell(row,	i++, "Saldo rechazos", style);
        createCell(row,	i++, "Diferencias Totales", style);
        createCell(row,	i++, "Validacion", style);
        createCell(row,	i, "Observación", style);
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

        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFCellStyle style1 = (XSSFCellStyle) workbook.createCellStyle();
        XSSFCellStyle style2 = (XSSFCellStyle) workbook.createCellStyle();
        XSSFCellStyle style3 = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        Font font2 = workbook.createFont();
        Font font3 = workbook.createFont();

        font.setFontHeightInPoints(SIZE);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        font2.setFontHeightInPoints(SIZE);
        font2.setBold(true);
        font2.setColor(IndexedColors.WHITE.getIndex());
        style2.setFont(font2);

        byte[] rgb = new byte[3];
        rgb[0] = (byte) 0; // red
        rgb[1] = (byte) 68; // green
        rgb[2] = (byte) 129; // blue

        XSSFColor color = new XSSFColor(rgb,new DefaultIndexedColorMap());
        style2.setFillForegroundColor(color);
        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        font3.setFontHeightInPoints(SIZE);
        font3.setBold(true);
        font3.setColor(IndexedColors.WHITE.getIndex());
        style3.setFont(font3);
        style3.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style3.setFillForegroundColor(color);
        style3.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for(IFRS9ConcilAccount valConcil: repoList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            if(!valConcil.getEmpresa().equals("TOTALES")){
                createCell(row,columnCount++,	valConcil.getEmpresa()	,style);
                createCell(row,columnCount++,	valConcil.getCuenta()	,style);
                createCell(row,columnCount++,	valConcil.getDescripcion()	,style);
                createCell(row,columnCount++,	valConcil.getCodicons()	,style);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoS2().toString())	,style1);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoPerInc().toString())	,style1);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoPrimeraVez().toString())	,style1);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoDifConc().toString())	,style1);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoManuales().toString())	,style1);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoProvGenint().toString())	,style1);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoPorcCal().toString())	,style1);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoDescAuto().toString())	,style1);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoRechazosAut().toString())	,style1);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getDiferencias().toString())	,style1);
                createCell(row,columnCount++,	valConcil.getValidacion()	,style);
                createCell(row,columnCount,	valConcil.getObservacion()	,style);
            }else{
                createCell(row,columnCount++,	valConcil.getEmpresa()	,style2);
                createCell(row,columnCount++,	valConcil.getCuenta()	,style2);
                createCell(row,columnCount++,	valConcil.getDescripcion()	,style2);
                createCell(row,columnCount++,	valConcil.getCodicons()	,style2);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoS2().toString())	,style3);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoPerInc().toString())	,style3);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoPrimeraVez().toString())	,style3);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoDifConc().toString())	,style3);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoManuales().toString())	,style3);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoProvGenint().toString())	,style3);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoPorcCal().toString())	,style3);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoDescAuto().toString())	,style3);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getSaldoRechazosAut().toString())	,style3);
                createCell(row,columnCount++,	Double.parseDouble(valConcil.getDiferencias().toString())	,style3);
                createCell(row,columnCount++,	valConcil.getValidacion()	,style2);
                createCell(row,columnCount,	valConcil.getObservacion()	,style2);
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
