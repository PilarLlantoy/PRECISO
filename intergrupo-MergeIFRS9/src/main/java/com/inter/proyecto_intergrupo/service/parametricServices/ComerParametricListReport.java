package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AvalTypes;
import com.inter.proyecto_intergrupo.model.parametric.ComerParametric;
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

public class ComerParametricListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private ArrayList<String[]> comerList;
    private List<ComerParametric> comerAllList;

    public ComerParametricListReport(ArrayList<String[]> comerList,List<ComerParametric> comerAllList){
        this.comerList = comerList;
        this.comerAllList = comerAllList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Metodología_Comercializadora");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Cuenta Local", style);
        createCell(row, 1, "Clase", style);
        createCell(row, 2, "Nombre Clase", style);
        createCell(row, 3, "Doc Compr", style);
        createCell(row, 4, "Prorrata de IVA", style);
        createCell(row, 5, "Tipo de importe", style);
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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(ComerParametric comer : comerAllList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,comer.getCuentaLocal(),style);
            createCell(row,columnCount++,comer.getClase(),style);
            createCell(row,columnCount++,comer.getNombreClase(),style);
            createCell(row,columnCount++,comer.getDocCompr(),style);
            createCell(row,columnCount++,comer.getProIva(),style);
            createCell(row,columnCount++,comer.getImporte(),style);
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

    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Inserción");
        Row row1 = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        if(!comerList.isEmpty() && comerList.get(0)[2].equals("true"))
        {
            createCell(row1, 0, "Cuenta - Clase", style);
            createCell(row1, 1, "Estado", style);
        }
        else if(!comerList.isEmpty())
        {
            createCell(row1, 0, "Fila", style);
            createCell(row1, 1, "Columna", style);
            createCell(row1, 2, "Estado", style);
        }
        else
        {
            createCell(row1, 0, "Documento cargado está vacío", style);
        }

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        if(comerList.size()>0 && comerList.get(0)[2].equals("true"))
            comerList.remove(comerList.size()-1);
        for(String[] log: comerList){

            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style);
            createCell(row,columnCount++,log[1],style);
            if(!log[2].equals("true"))
                createCell(row,columnCount++,log[2],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}
