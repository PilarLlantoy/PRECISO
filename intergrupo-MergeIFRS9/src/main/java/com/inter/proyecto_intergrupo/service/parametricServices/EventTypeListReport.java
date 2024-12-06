package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.model.parametric.ThirdsCc;
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

public class EventTypeListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<EventType> listData;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public EventTypeListReport(List<EventType> listData) {
        this.listData = listData;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Tipos de Eventos");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Nombre Tipo", style);
        createCell(row, 1, "Se Valida Cruce", style);
        createCell(row, 2, "Estado Actvo", style);
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

        for(EventType data: listData){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getNombre(),style);
            createCell(row,columnCount++,data.isValidaCruce()==true ? "Si" : "No",style);
            createCell(row,columnCount++,data.isEstado()==true ? "Si" : "No",style);
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
