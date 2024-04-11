package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.bank.planoreclasificacion;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.*;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReclassificationV2ListReport3 {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;

    private List<Object[]> Error1;
    private List<Object[]> Error2;

    public ReclassificationV2ListReport3(List<Object[]> error1, List<Object[]> error2){
        this.Error1 = error1;
        this.Error2 = error2;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Error Reclasificaciones");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "Cod. Neocon", style);
        createCell(row,	1, "Cuenta Contable", style);
        createCell(row,	2, "Mensaje", style);

        //

        sheet1 = workbook.createSheet("Error Reclasificaciones 2");
        Row row1 = sheet1.createRow(0);

        createCell(row1,	0, "Cuenta Contable", style);
        createCell(row1,	1, "Tipo Entidad", style);
        createCell(row1,	2, "Producto", style);
        createCell(row1,	3, "Tipo", style);
        createCell(row1,	4, "Stage", style);
        createCell(row1,	5, "Mensaje", style);

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

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(Object[] list: Error1){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,	list[0]	,style);
            createCell(row,columnCount++,	list[1]	,style);
            createCell(row,columnCount++,	list[2]	,style);

        }

        rowCount = 1;

        for(Object[] list: Error2){
            Row row = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,	list[1]	,style);
            createCell(row,columnCount++,	list[2]	,style);
            createCell(row,columnCount++,	list[4]	,style);
            createCell(row,columnCount++,	list[5]	,style);
            createCell(row,columnCount++,	list[6]	,style);
            createCell(row,columnCount++,	list[7]	,style);

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
