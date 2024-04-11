package com.inter.proyecto_intergrupo.service.adminServices;

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

public class ControlPanelAccounts {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Object[]> accounts;

    public ControlPanelAccounts(List<Object[]> accounts) {
        this.accounts = accounts;
        workbook = new XSSFWorkbook();
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

    public void exportAccount(HttpServletResponse response,String fuente) throws IOException {
        writeHeaderLineAccount(fuente);
        writeDataLinesAccount(fuente);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
    private void writeHeaderLineAccount(String fuente){
        sheet = workbook.createSheet("Validación Query");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);
        int columnCount = 0;

        createCell(row, columnCount++, "Cuenta Contable", style);
        if(fuente.equals("IFRS9"))
            createCell(row, columnCount++, "Código de Consolidación", style);
        createCell(row, columnCount++, "Divisa", style);
        createCell(row, columnCount++, "Saldo Divisa Contingentes", style);
        createCell(row, columnCount++, "Saldo Pesos Contingentes", style);
        createCell(row, columnCount++, "Saldo Divisa Query", style);
        createCell(row, columnCount++, "Saldo Pesos Query", style);
        createCell(row, columnCount++, "Diferencia Divisa", style);
        createCell(row, columnCount++, "Diferencia Pesos", style);
        createCell(row, columnCount++, "Fecha proceso Query", style);

    }
    private void writeDataLinesAccount(String fuente){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] acc: accounts){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,acc[0].toString(),style);
            if(acc[1]!=null)
                createCell(row,columnCount++,acc[1].toString(),style);
            else
                createCell(row,columnCount++,"",style);
            if(fuente.equals("IFRS9"))
                createCell(row,columnCount++,acc[2].toString(),style);
            else
                createCell(row,columnCount++,Double.parseDouble(acc[2].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(acc[3].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(acc[4].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(acc[5].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(acc[6].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(acc[7].toString()),style1);
            if(fuente.equals("IFRS9")) {
                createCell(row, columnCount++, Double.parseDouble(acc[8].toString()), style1);
            }
            else if(acc.length >8 && acc[8]!=null) {
                createCell(row, columnCount++, acc[8].toString().replace("00:00:00.0", ""), style);
            }
            if (acc.length >9 && acc[9]!=null)
                createCell(row, columnCount++, acc[9].toString().replace("00:00:00.0", ""), style);

        }
    }
}
