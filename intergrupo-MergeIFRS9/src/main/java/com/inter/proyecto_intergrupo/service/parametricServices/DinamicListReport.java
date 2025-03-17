package com.inter.proyecto_intergrupo.service.parametricServices;

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

public class DinamicListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Object[]> dataList;
    private List<Object[]> headerList;
    private String hoja;
    private String ruta;

    public DinamicListReport(List<Object[]> dataList, String hoja,String ruta){
        this.dataList = dataList;
        this.hoja = hoja;
        this.ruta = ruta;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Matrices Eventos");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0,"Conciliacion", style);
        createCell(row, 1,"Inventario", style);
        createCell(row, 2,"Matriz", style);
        createCell(row, 3,"Tipo Evento", style);
        createCell(row, 4,"Cuenta 1", style);
        createCell(row, 5,"Cuenta 2", style);
        createCell(row, 6,"Homologa Centros", style);
        createCell(row, 7,"PYG", style);
        createCell(row, 8,"Estado", style);

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

        for(Object[] data: dataList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,(data[0]!=null ? data[0].toString():""),style);
            createCell(row,columnCount++,(data[1]!=null ? data[1].toString():""),style);
            createCell(row,columnCount++,(data[2]!=null ? data[2].toString():""),style);
            createCell(row,columnCount++,(data[3]!=null ? data[3].toString():""),style);
            createCell(row,columnCount++,(data[4]!=null ? data[4].toString():""),style);
            createCell(row,columnCount++,(data[5]!=null ? data[5].toString():""),style);
            createCell(row,columnCount++,((boolean)data[6] ? "Activo":"Inactivo"),style);
            createCell(row,columnCount++,((boolean)data[7] ? "Activo":"Inactivo"),style);
            createCell(row,columnCount++,((boolean)data[8] ? "Activo":"Inactivo"),style);

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
