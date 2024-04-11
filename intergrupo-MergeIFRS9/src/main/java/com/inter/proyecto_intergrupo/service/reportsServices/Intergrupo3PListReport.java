package com.inter.proyecto_intergrupo.service.reportsServices;


import com.inter.proyecto_intergrupo.model.ifrs9.PlainIFRS9Intergroup;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV3Temp;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Intergrupo3PListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Object[]> interList;

    public Intergrupo3PListReport(List<Object[]> interList) {
        workbook = new XSSFWorkbook();
        this.interList = interList;
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Planos Integrupo");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Empresa", style);
        createCell(row, 1, "Fecha Contable", style);
        createCell(row, 2, "Cuenta", style);
        createCell(row, 3, "Contrato", style);
        createCell(row, 4, "Divisa", style);
        createCell(row, 5, "Importe Crédito", style);
        createCell(row, 6, "Importe Débito", style);
        createCell(row, 7, "Importe Crédito Divisa", style);
        createCell(row, 8, "Importe Débito Divisa", style);
        createCell(row, 9, "Importe Crédito Divisa Exp", style);
        createCell(row, 10, "Importe Débito Divisa Exp", style);
        createCell(row, 11, "Importe Total", style);
        createCell(row, 12, "Observación", style);
        createCell(row, 13, "Origen", style);
        createCell(row, 14, "Periodo", style);
        createCell(row, 15, "Codicons", style);
        createCell(row, 16, "Yntp", style);

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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));


        for(Object[] inter : interList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, inter[0],style);
            createCell(row,columnCount++, inter[1],style);
            createCell(row,columnCount++, inter[2],style);
            createCell(row,columnCount++, inter[3],style);
            createCell(row,columnCount++, inter[4],style);
            createCell(row,columnCount++, Double.parseDouble(inter[5].toString()),style1);
            createCell(row,columnCount++, Double.parseDouble(inter[6].toString()),style1);
            createCell(row,columnCount++, Double.parseDouble(inter[7].toString()),style1);
            createCell(row,columnCount++, Double.parseDouble(inter[8].toString()),style1);
            createCell(row,columnCount++, Double.parseDouble(inter[9].toString()),style1);
            createCell(row,columnCount++, Double.parseDouble(inter[10].toString()),style1);
            createCell(row,columnCount++, Double.parseDouble(inter[11].toString()),style1);
            createCell(row,columnCount++, inter[12],style);
            createCell(row,columnCount++, inter[13],style);
            createCell(row,columnCount++, inter[14],style);
            createCell(row,columnCount++, inter[15],style);
            createCell(row,columnCount++, inter[16],style);

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
