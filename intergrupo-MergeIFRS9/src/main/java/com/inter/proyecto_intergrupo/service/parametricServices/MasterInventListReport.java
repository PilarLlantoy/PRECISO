package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.model.parametric.MasterInvent;
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

public class MasterInventListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<MasterInvent> listData;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public MasterInventListReport(List<MasterInvent> listData) {
        this.listData = listData;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Maestro Inventarios");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Conciliación", style);
        createCell(row, 1, "Fecha Conciliación", style);
        createCell(row, 2, "Contable", style);
        createCell(row, 3, "Fecha Contable", style);
        createCell(row, 4, "Estado Conciliación", style);
        createCell(row, 5, "Estado Contable", style);
        createCell(row, 6, "Aplica Semana", style);
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

    private void writeDataLines(List<Object[]> data1){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style2.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style2.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));

        for(Object[] data: data1){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data[0].toString(),style);
            createCell(row,columnCount++,data[1].toString(),style2);
            createCell(row,columnCount++,data[2].toString(),style);
            createCell(row,columnCount++,data[3].toString(),style2);
            createCell(row,columnCount++,data[4].toString().equalsIgnoreCase("1") ? "Si" : "No",style);
            createCell(row,columnCount++,data[5].toString().equalsIgnoreCase("1") ? "Si" : "No",style);
            createCell(row,columnCount++,data[6].toString().equalsIgnoreCase("1") ? "Si" : "No",style);
        }
    }

    public void export(HttpServletResponse response,List<Object[]> data1) throws IOException {
        writeHeaderLine();
        writeDataLines(data1);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
