package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.ValQueryEEFF;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ValidacionAcQueryEEFFListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Object[]> repoList;

    public ValidacionAcQueryEEFFListReport(List<Object[]> cuadreList){
        this.repoList = cuadreList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Validación Query EEFF");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "Código consolidación", style);
        createCell(row,	1, "Mensaje", style);

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
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);

        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] valQueryEEFF: repoList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            createCell(row,columnCount++,	valQueryEEFF[0]	,style);
            createCell(row,columnCount++,	valQueryEEFF[1] ,style);
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
