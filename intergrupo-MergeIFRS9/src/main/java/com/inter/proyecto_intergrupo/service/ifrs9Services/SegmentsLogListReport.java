package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.bank.IntergrupoSubsidiariesTemplate;
import com.inter.proyecto_intergrupo.model.bank.SubsidiariesTemplate;
import com.inter.proyecto_intergrupo.model.parametric.Ciiu;
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

public class SegmentsLogListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> subsidiariesList;

    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public SegmentsLogListReport(List<String[]> list) {
        this.subsidiariesList = list;
        workbook = new XSSFWorkbook();
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


    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Plantilla");
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

        subsidiariesList.remove(subsidiariesList.size()-1);
        for(String[] log: subsidiariesList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style);
            createCell(row,columnCount++,log[1],style);
            createCell(row,columnCount++,log[2],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}