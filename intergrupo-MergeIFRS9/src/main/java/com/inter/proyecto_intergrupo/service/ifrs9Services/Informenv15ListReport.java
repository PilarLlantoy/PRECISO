package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.Desconnv15;
import com.inter.proyecto_intergrupo.model.ifrs9.Informenv15;
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

public class Informenv15ListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Informenv15> desconnv15List;

    public Informenv15ListReport(List<Informenv15> desconnv15List){
        this.desconnv15List = desconnv15List;
        workbook = new XSSFWorkbook();
    }

    //TODO
    private void writeHeaderLine(){
        sheet = workbook.createSheet("Desconnv15");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0,  "Cuenta", style);
        createCell(row, 1,  "Divisa", style);
        createCell(row, 2,  "Sald", style);
        createCell(row, 3,  "Salc", style);
        createCell(row, 4,  "Sald Div", style);
        createCell(row, 5,  "Salc Div", style);
        createCell(row, 6,  "Observaciones", style);
        createCell(row, 7,  "Tp", style);
        createCell(row, 8,  "Tp1", style);
        createCell(row, 9,  "Fuente información", style);

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

        for(Informenv15 infnv15: desconnv15List){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,infnv15.getCuenta(),style);
            createCell(row,columnCount++,infnv15.getDivisa(),style);
            createCell(row,columnCount++,infnv15.getSald().toString(),style1);
            createCell(row,columnCount++,infnv15.getSalc().toString(),style1);
            createCell(row,columnCount++,infnv15.getSaldDiv().toString(),style1);
            createCell(row,columnCount++,infnv15.getSalcDiv().toString(),style1);
            createCell(row,columnCount++,infnv15.getObservaciones(),style);
            createCell(row,columnCount++,infnv15.getTp(),style);
            createCell(row,columnCount++,infnv15.getTp1(),style);
            createCell(row,columnCount,infnv15.getFuenteInformacion(),style);
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
