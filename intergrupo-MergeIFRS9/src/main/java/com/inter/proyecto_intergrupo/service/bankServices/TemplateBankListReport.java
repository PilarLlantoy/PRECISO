package com.inter.proyecto_intergrupo.service.bankServices;

import com.inter.proyecto_intergrupo.model.bank.TaxBase;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TemplateBankListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> templateBankList;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public TemplateBankListReport(List<String[]> templateBankList) {
        this.templateBankList = templateBankList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Plantilla Banco");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "YNTP EMPRESA", style);
        createCell(row, 1, "COD NEOCON", style);
        createCell(row, 2, "DIVISA", style);
        createCell(row, 3, "YNTP", style);
        createCell(row, 4, "SOCIEDAD YNTP", style);
        createCell(row, 5, "CONTRATO", style);
        createCell(row, 6, "NIT CONTRAPARTE", style);
        createCell(row, 7, "VALOR", style);
        createCell(row, 8, "COD PAÍS", style);
        createCell(row, 9, "PAÍS", style);
        createCell(row, 10, "CUENTA LOCAL", style);
        createCell(row, 11, "OBSERVACIONES", style);
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
        Cell cell = row.createCell(columCount);

        if(value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if(value instanceof Integer){
            cell.setCellValue((Integer) value);
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        }else if(value instanceof Long){
            cell.setCellValue((Long) value);
        }else if(value instanceof Double){
            cell.setCellValue((Double) value);
        }else if(value instanceof String){
            cell.setCellValue((String) value);
        }

        cell.setCellStyle(style);
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

        for(Object[] tax: templateBankList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,tax[0],style);
            createCell(row,columnCount++,tax[1].toString(),style);
            createCell(row,columnCount++,tax[2],style);
            createCell(row,columnCount++,tax[3],style);
            createCell(row,columnCount++,tax[4],style);
            createCell(row,columnCount++,tax[5],style);
            createCell(row,columnCount++,tax[6].toString(),style);
            createCell(row,columnCount++,Double.parseDouble(tax[7].toString()),style1);
            createCell(row,columnCount++,tax[8],style);
            createCell(row,columnCount++,tax[9],style);
            createCell(row,columnCount++,tax[10],style);
            createCell(row,columnCount++,tax[11],style);
        }
    }

    private void writeDataLinesReport(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] tax: templateBankList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,tax[0],style);
            createCell(row,columnCount++,tax[1].toString(),style);
            createCell(row,columnCount++,tax[2],style);
            createCell(row,columnCount++,tax[3],style);
            createCell(row,columnCount++,tax[4],style);
            createCell(row,columnCount++,tax[5],style);
            createCell(row,columnCount++,tax[6].toString(),style);
            createCell(row,columnCount++,Double.parseDouble(tax[7].toString()),style1);
            createCell(row,columnCount++,tax[8],style);
            createCell(row,columnCount++,tax[9],style);
            createCell(row,columnCount++,tax[10],style);
            createCell(row,columnCount++,tax[11],style);
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

    public void exportReport(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLinesReport();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Plantilla");
        Row row1 = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "Fila", style);
        createCell(row1, 1, "Columna", style);
        createCell(row1, 2, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        if(templateBankList.size()>0)
            templateBankList.remove(templateBankList.size()-1);
        for(String[] log: templateBankList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style);
            createCell(row,columnCount++,log[1],style);
            createCell(row,columnCount++,log[2],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLogQuery(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Plantilla");
        Row row1 = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        createCell(row1, 0, "Cuenta", style);
        createCell(row1, 1, "Saldo Banco", style);
        createCell(row1, 2, "Saldo Query", style);
        createCell(row1, 3, "Error", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        if(templateBankList.size()>0)
            templateBankList.remove(templateBankList.size()-1);
        for(String[] log: templateBankList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            boolean resp = false;

            createCell(row,columnCount++,log[0],style);
            createCell(row, columnCount++, Double.parseDouble(log[1]), style1);
            try {
                Double.parseDouble(log[2]);
                resp = true;
            }catch (Exception e){
            }

            if(!resp){
                createCell(row, columnCount++, log[2], style);
            }else{
                createCell(row, columnCount++, Double.parseDouble(log[2]), style1);
            }

            createCell(row,columnCount++,log[3],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLogDatabase(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Plantilla");
        Row row1 = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        createCell(row1, 0, "Cuenta", style);
        createCell(row1, 1, "Error", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        if(templateBankList.size()>0)
            templateBankList.remove(templateBankList.size()-1);
        for(String[] log: templateBankList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style);
            createCell(row,columnCount++,log[1],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLogCodicons(HttpServletResponse response,List<Object[]> validateCodicons) throws IOException
    {
        sheet = workbook.createSheet("Log_Plantilla");

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        Row row0 = sheet.createRow(0);
        createCell(row0, 0, "NOTA:", style);
        createCell(row0, 1, "Se ha realizado el cargue de forma exitosa, el siguiente listado es una validación de naturaleza frente a la parametrica de Naturaleza de cuentas que no cumplieron.", style);

        Row row1 = sheet.createRow(1);
        createCell(row1, 0, "Cuenta", style);
        createCell(row1, 1, "Estado Validación", style);

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for(Object[] log: validateCodicons){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0].toString(),style);
            if(log[1].toString().equals("NO EXIST"))
                createCell(row,columnCount++,"La cuenta no se encuentra parametrizada en la parametrica de cuentas naturaleza.",style);
            else
                createCell(row,columnCount++,"La cuenta se encuentra con la naturaleza contraria a la estipulada en la parametrica.",style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
