package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.CampoRConcil;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
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

public class InventoryLoadListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<CampoRConcil> colCroutes;
    private List<Object[]> aroutes;
    private ConciliationRoute cr;

    public InventoryLoadListReport(List<Object[]> aroutes, List<CampoRConcil> colCroutes, ConciliationRoute cr){
        this.aroutes = aroutes;
        this.colCroutes = colCroutes;
        this.cr = cr;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet(cr.getNombreArchivo().replace(" ","_"));
        Row row = sheet.createRow(0);
        int count = 0;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        for (CampoRConcil campo :colCroutes) {
            createCell(row, count++, campo.getNombre(), style);
        }
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

        for(Object[] data: aroutes){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            for (Object part:data)
            {
                if(part!=null)
                    createCell(row,columnCount++,part.toString(),style);
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