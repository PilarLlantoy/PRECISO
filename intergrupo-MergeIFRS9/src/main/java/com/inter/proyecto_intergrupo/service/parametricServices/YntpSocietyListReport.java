package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class YntpSocietyListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;
    private XSSFSheet sheet2;
    private List<YntpSociety> yntpList;
    private List<Object[]> yntpListSub;

    public YntpSocietyListReport(List<YntpSociety> yntpList){
        this.yntpList = yntpList;
        workbook = new XSSFWorkbook();
    }
    public YntpSocietyListReport(List<YntpSociety> yntpList,List<Object[]> yntpListSub){
        this.yntpListSub = yntpListSub;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Sociedades Yntp");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Código Sociedad", style);
        createCell(row, 1, "Descripción Larga Sociedad", style);
        createCell(row, 2, "Descripción Corta Sociedad", style);
        createCell(row, 3, "Código país", style);
        createCell(row, 4, "País", style);
        createCell(row, 5, "Código Divisa", style);
        createCell(row, 6, "Divisa", style);
        createCell(row, 7, "Código Mtdo. Consolidación IFRS\"", style);
        createCell(row, 8, "Mtdo. Consolidación IFRS\"", style);
        createCell(row, 9, "Código Grupo Consolidación IFRS", style);
        createCell(row, 10, "Grupo Consolidación IFRS", style);
        createCell(row, 11, "Tipo Entidad", style);
    }

    private void writeHeaderLineConvert(){
        sheet = workbook.createSheet("Sociedades Yntp");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row, 0, "Código Sociedad", style);
        createCell(row, 1, "Descripción Larga Sociedad", style);
        createCell(row, 2, "Descripción Corta Sociedad", style);
        createCell(row, 3, "Código país", style);
        createCell(row, 4, "Código Divisa", style);
        createCell(row, 5, "Código Mtdo. Consolidación IFRS\"", style);
        createCell(row, 6, "Código Grupo Consolidación IFRS", style);
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

        for(YntpSociety yntp: yntpList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            String temporalYntp=String.valueOf(yntp.getYntp());

            for (int i=0;i< 5-(String.valueOf(yntp.getYntp()).length());i++)
            {
                temporalYntp="0"+temporalYntp;
            }

            createCell(row,columnCount++,temporalYntp,style);
            createCell(row,columnCount++,yntp.getSociedadDescripcionLarga(),style);
            createCell(row,columnCount++,yntp.getSociedadDescripcionCorta(),style);
            createCell(row,columnCount++,yntp.getPais().getId(),style);
            createCell(row,columnCount++,yntp.getPais().getNombre(),style);
            createCell(row,columnCount++,yntp.getDivisa().getId(),style);
            createCell(row,columnCount++,yntp.getDivisa().getNombre(),style);
            createCell(row,columnCount++,yntp.getMetodo().getId(),style);
            createCell(row,columnCount++,yntp.getMetodo().getNombre(),style);
            createCell(row,columnCount++,yntp.getGrupo().getId(),style);
            createCell(row,columnCount++,yntp.getGrupo().getNombre(),style);
            createCell(row,columnCount++,yntp.getTipoEntidad(),style);
        }
    }

    private void writeDataLinesConvert(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(Object[] yntp: yntpListSub){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,yntp[0],style);
            createCell(row,columnCount++,yntp[2],style);
            createCell(row,columnCount++,yntp[1],style);
            createCell(row,columnCount++,yntp[6],style);
            createCell(row,columnCount++,yntp[3],style);
            createCell(row,columnCount++,yntp[5],style);
            createCell(row,columnCount++,yntp[4],style);
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
    public void exportConvert(HttpServletResponse response) throws IOException {
        writeHeaderLineConvert();
        writeDataLinesConvert();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLog(HttpServletResponse response, ArrayList<String[]> lista) throws IOException
    {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        sheet = workbook.createSheet("Log Yntp");
        Row row1 = sheet.createRow(0);
        createCell(row1, 0, "Yntp", style);
        createCell(row1, 1, "Estado", style);

        int rowCount = 1;
        int position =0;

        font.setBold(false);
        font.setFontHeight(10);

        for(int i =0;i<lista.size() && !lista.get(i)[0].equals("DIVISA") ;i++){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,lista.get(i)[0],style);
            createCell(row,columnCount++,lista.get(i)[1],style);
            position++;
        }

        position++;

        sheet1 = workbook.createSheet("Log Divisa");
        Row row2 = sheet1.createRow(0);
        createCell(row2, 0, "Código", style);
        createCell(row2, 1, "Estado", style);

        rowCount = 1;

        for(int i =position;i<lista.size() && !lista.get(i)[0].equals("PAIS") ;i++){
            Row row = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,lista.get(i)[0],style);
            createCell(row,columnCount++,lista.get(i)[1],style);
            position++;
        }
        position++;

        sheet2 = workbook.createSheet("Log País");
        Row row3 = sheet2.createRow(0);
        createCell(row3, 0, "Código", style);
        createCell(row3, 1, "Estado", style);

        rowCount = 1;

        for(int i =position;i<lista.size() ;i++){
            Row row = sheet2.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,lista.get(i)[0],style);
            createCell(row,columnCount++,lista.get(i)[1],style);
            position++;
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
