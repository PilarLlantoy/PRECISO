package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.PyG;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PyGListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<PyG> pygList;

    public PyGListReport(List<PyG> pygList){
        this.pygList = pygList;
        workbook = new XSSFWorkbook();
    }
    
    private void writeHeaderLine(){
        sheet = workbook.createSheet("Parametría PyG");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Año", style);
        createCell(row, 1, "Descripción", style);
        createCell(row, 2, "Cuenta D", style);
        createCell(row, 3, "Nombre Cuenta D", style);
        createCell(row, 4, "Cuenta H", style);
        createCell(row, 5, "Nombre Cuenta H", style);
        createCell(row, 6, "Tipo", style);
        createCell(row, 7, "Stage", style);
        createCell(row, 8, "Divisa", style);
        createCell(row, 9, "Valor", style);
        createCell(row, 10, "Naturaleza", style);

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

        for(PyG pyg: pygList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,pyg.getAnio(),style);
            createCell(row,columnCount++,pyg.getDescripcion(),style);
            createCell(row,columnCount++,pyg.getCuenta(),style);
            createCell(row,columnCount++,pyg.getNombreCuenta(),style);
            createCell(row,columnCount++,pyg.getCuentaH(),style);
            createCell(row,columnCount++,pyg.getNombreCuentaH(),style);
            createCell(row,columnCount++,pyg.getTipo(),style);
            createCell(row,columnCount++,pyg.getStage(),style);
            createCell(row,columnCount++,pyg.getDivisa(),style);
            createCell(row,columnCount++,pyg.getValor(),style1);
            createCell(row,columnCount++,pyg.getNaturaleza(),style);
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

        createCell(row1, 0, "Fila", style);
        createCell(row1, 1, "Columna", style);
        createCell(row1, 2, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        for(String[] third: lista){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,third[0],style);
            createCell(row,columnCount++,third[1],style);
            if(third[3]!=null)
            createCell(row,columnCount++,third[3],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}