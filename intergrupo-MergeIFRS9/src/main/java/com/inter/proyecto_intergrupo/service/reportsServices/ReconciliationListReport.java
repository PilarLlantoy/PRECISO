package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReconciliationListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<IntergrupoV1> intergrupoV1List;

    public ReconciliationListReport(List<IntergrupoV1> IntergrupoV1List, int i){
        this.intergrupoV1List = IntergrupoV1List;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Intergrupo V1");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "YNTP Empresa Reportante", style);
        createCell(row, 1, "YNTP", style);
        createCell(row, 2, "Periodo", style);
        createCell(row, 3, "NIT", style);
        createCell(row, 4, "Divisa", style);
        createCell(row, 5, "Cuenta Local", style);
        createCell(row, 6, "Contrato", style);
        createCell(row, 7, "Código Neocon", style);
        createCell(row, 8, "Código País", style);
        createCell(row, 9, "Sociedad YNTP", style);
        createCell(row, 10, "Valor", style);

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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(IntergrupoV1 intergrupoV1: intergrupoV1List){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,intergrupoV1.getYntpReportante() ,style);
            createCell(row,columnCount++,intergrupoV1.getYntp() ,style);
            createCell(row,columnCount++,intergrupoV1.getPeriodo() ,style);
            createCell(row,columnCount++,intergrupoV1.getNit() ,style);
            createCell(row,columnCount++,intergrupoV1.getDivisa() ,style);
            createCell(row,columnCount++,intergrupoV1.getCuentaLocal() ,style);
            createCell(row,columnCount++,intergrupoV1.getContrato() ,style);
            createCell(row,columnCount++,intergrupoV1.getCodNeocon() ,style);
            createCell(row,columnCount++,intergrupoV1.getCodPais() ,style);
            createCell(row,columnCount++,intergrupoV1.getSociedadYntp() ,style);
            createCell(row,columnCount++,intergrupoV1.getValor() ,style);
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

        createCell(row1, 0, "YNTP Empresa Reportante", style);
        createCell(row1, 1, "YNTP", style);
        createCell(row1, 2, "Periodo", style);
        createCell(row1, 3, "NIT", style);
        createCell(row1, 4, "Divisa", style);
        createCell(row1, 5, "Cuenta Local", style);
        createCell(row1, 6, "Contrato", style);
        createCell(row1, 7, "Código Neocon", style);
        createCell(row1, 8, "Código País", style);
        createCell(row1, 9, "Sociedad YNTP", style);
        createCell(row1, 10, "Valor", style);

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
