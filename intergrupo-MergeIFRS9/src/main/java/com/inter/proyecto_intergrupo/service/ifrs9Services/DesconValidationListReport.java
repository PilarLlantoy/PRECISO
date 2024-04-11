package com.inter.proyecto_intergrupo.service.ifrs9Services;


import com.inter.proyecto_intergrupo.model.ifrs9.DesconValidationUpload;
import com.inter.proyecto_intergrupo.model.ifrs9.IFRS9ConcilAccount;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class DesconValidationListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private List<DesconValidationUpload> repoList;

    public DesconValidationListReport(List<DesconValidationUpload> cuadreList){
        this.repoList = cuadreList;
        workbook = new SXSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Tanque Descon.");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        short size = 11;
        font.setFontHeightInPoints(size);
        style.setFont(font);
        int i = 0;

        createCell(row, i++, "Neocon", style);
        createCell(row, i++, "Cuenta", style);
        createCell(row,	i++, "Divisa", style);
        createCell(row,	i++, "Sald", style);
        createCell(row,	i++, "Salc", style);
        createCell(row,	i++, "Sald Div.", style);
        createCell(row,	i++, "Salc Div.", style);
        createCell(row,	i++, "Variación ML", style);
        createCell(row,	i++, "Variación ME", style);
        createCell(row,	i++, "Variación Total", style);
        createCell(row,	i++, "Variación Miles", style);
        createCell(row,	i, "Observación", style);

    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
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

        Font font = workbook.createFont();
        short size = 10;
        font.setFontHeightInPoints(size);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(DesconValidationUpload valConcil: repoList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,	valConcil.getCodNeocon(),style);
            createCell(row,columnCount++,	valConcil.getCuenta(),style);
            createCell(row,columnCount++,	valConcil.getDiv(),style);
            createCell(row,columnCount++,	valConcil.getSald(),style1);
            createCell(row,columnCount++,	valConcil.getSalc(),style1);
            createCell(row,columnCount++,	valConcil.getSaldDiv(),style1);
            createCell(row,columnCount++,	valConcil.getSalcDiv(),style1);
            createCell(row,columnCount++,	valConcil.getVrMl(),style1);
            createCell(row,columnCount++,	valConcil.getVrMe(),style1);
            createCell(row,columnCount++,	valConcil.getVrTotal(),style1);
            createCell(row,columnCount++,	valConcil.getVrMiles(),style1);
            createCell(row,columnCount,	valConcil.getObservacion(),style);

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
