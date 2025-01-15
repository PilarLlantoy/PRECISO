package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.*;
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

public class AccountingLoadListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<CampoRC> colAroutes;
    private List<Object[]> aroutes;
    private AccountingRoute ac;

    public AccountingLoadListReport(List<Object[]> aroutes, List<CampoRC> colAroutes, AccountingRoute ac){
        this.aroutes = aroutes;
        this.colAroutes = colAroutes;
        this.ac = ac;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet(ac.getNombre().replace(" ","_"));
        Row row = sheet.createRow(0);
        int count = 0;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        List<String> camposVal = new ArrayList<>();

        for (CampoRC campo :colAroutes) {
            camposVal.add(campo.getNombre().toUpperCase());
            createCell(row, count++, campo.getNombre().toUpperCase().replace("PERIODO_PRECISO","FECHA CONTABLE"), style);
        }
        if(!camposVal.contains("PERIODO_PRECISO"))
            createCell(row, count++, "FECHA CONTABLE", style);
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

        CellStyle style1 = workbook.createCellStyle();
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        CellStyle style2 = workbook.createCellStyle();
        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        CellStyle style3 = workbook.createCellStyle();
        style3.setFont(font);
        style3.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));

        for(Object[] data: aroutes){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            for (Object part:data)
            {
                if(part!=null) {
                    try{
                        if(data.length==(columnCount+1)) {
                            createCell(row, columnCount++, part, style3);
                        }
                        else if(colAroutes.get(columnCount).getTipo().equalsIgnoreCase("Float"))
                            createCell(row, columnCount++, Double.parseDouble(part.toString()), style1);
                        else if(colAroutes.get(columnCount).getTipo().equalsIgnoreCase("Integer") || colAroutes.get(columnCount).getTipo().equalsIgnoreCase("Bigint"))
                            createCell(row, columnCount++, Long.parseLong(part.toString()), style2);
                        else
                            createCell(row, columnCount++, part.toString(), style);
                    }
                    catch (Exception e)
                    {
                        createCell(row, columnCount++, part.toString(), style);
                    }
                }
                else
                    createCell(row,columnCount++,"",style);
                if(columnCount == 1000000)
                    break;
            }
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush(); // Asegúrate de que todos los datos se envíen
        outputStream.close();
    }
}
