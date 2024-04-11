package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.TypeEntity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TypeEntityListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<TypeEntity> typeEntityList;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public TypeEntityListReport(List<TypeEntity> typeEntityList){
        this.typeEntityList = typeEntityList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Tipo Entidades");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Tipo Contraparte", style);
        createCell(row, 1, "NIT", style);
        createCell(row, 2, "Contraparte", style);
        createCell(row, 3, "Intergrupo", style);
        createCell(row, 4, "Tipo Entidad", style);
        createCell(row, 5, "Eliminación", style);
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

        for(TypeEntity type: typeEntityList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,type.getTipoContraparte(),style);
            createCell(row,columnCount++,type.getNit(),style);
            createCell(row,columnCount++,type.getContraparte(),style);
            if(type.getIntergrupo()!=null &&type.getIntergrupo()==true) {
                createCell(row, columnCount++, "SI", style);
            }
            else if(type.getIntergrupo()!=null)
            {
                createCell(row, columnCount++, "NO", style);
            }
            else
            {
                createCell(row, columnCount++, "", style);
            }
            createCell(row,columnCount++,type.getTipoEntidad(),style);
            if(type.getEliminacion()!=null &&type.getEliminacion()==true) {
                createCell(row, columnCount++, "SI", style);
            }
            else if(type.getEliminacion()!=null)
            {
                createCell(row, columnCount++, "NO", style);
            }
            else
            {
                createCell(row, columnCount++, "", style);
            }
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
        sheet = workbook.createSheet("LOG_TIPO_ENTIDAD");
        Row row1 = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "Tipo Contraparte - Tipo Entidad", style);
        createCell(row1, 1, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        for(String[] third: lista){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,third[0],style);
            createCell(row,columnCount++,third[1],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
