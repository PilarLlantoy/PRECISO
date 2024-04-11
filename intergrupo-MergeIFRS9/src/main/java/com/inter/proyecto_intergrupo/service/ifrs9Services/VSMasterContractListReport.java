package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.IncurredLoss;
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

public class VSMasterContractListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private List<Object[]> dataResume;
    private List<Object[]> dataComplete;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public VSMasterContractListReport(List<Object[]> dataComplete, List<Object[]> dataResume){
        this.dataResume = dataResume;
        this.dataComplete = dataComplete;
        workbook = new SXSSFWorkbook();
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

    private void writeHeaderLine(){
        sheet = workbook.createSheet("VS - MC Comparativa");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        short short1 = 11;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(short1);
        style.setFont(font);

        createCell(row, 0, "Empresa", style);
        createCell(row, 1, "Instrumento", style);
        createCell(row, 2, "Contrato", style);
        createCell(row, 3, "Stage", style);
        createCell(row, 4, "V Saldo Local", style);
        createCell(row, 5, "V Importe Provision", style);
        createCell(row, 6, "V Importe EAD", style);
        createCell(row, 7, "M Saldo Local", style);
        createCell(row, 8, "M Importe Provision", style);
        createCell(row, 9, "M Importe EAD", style);
        createCell(row, 10, "Dif Saldo Local", style);
        createCell(row, 11, "Dif Importe Provision", style);
        createCell(row, 12, "Dif Importe EAD", style);
    }

    private void writeHeaderLineResume(){
        sheet = workbook.createSheet("VS - MC Comparativa");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        short short1 = 11;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(short1);
        style.setFont(font);

        createCell(row, 0, "Empresa", style);
        createCell(row, 1, "Instrumento", style);
        createCell(row, 2, "Stage", style);
        createCell(row, 3, "V Saldo Local", style);
        createCell(row, 4, "V Importe Provision", style);
        createCell(row, 5, "V Importe EAD", style);
        createCell(row, 6, "M Saldo Local", style);
        createCell(row, 7, "M Importe Provision", style);
        createCell(row, 8, "M Importe EAD", style);
        createCell(row, 9, "Dif Saldo Local", style);
        createCell(row, 10, "Dif Importe Provision", style);
        createCell(row, 11, "Dif Importe EAD", style);
    }

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        short short1 = 10;
        short short2 = 10;
        Font font = workbook.createFont();
        Font font1 = workbook.createFont();
        font.setFontHeightInPoints(short1);
        font1.setFontHeightInPoints(short2);
        style.setFont(font);
        style1.setFont(font1);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] data: dataComplete){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,data[1],style);
            createCell(row,columnCount++,data[2],style);
            createCell(row,columnCount++,data[3],style);
            createCell(row,columnCount++,data[4],style);
            createCell(row,columnCount++,Double.parseDouble(data[5].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[6].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[7].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[8].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[9].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[10].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[11].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[12].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[13].toString()),style1);

        }
    }

    private void writeDataLinesResume(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        short short1 = 10;
        short short2 = 10;
        Font font = workbook.createFont();
        Font font1 = workbook.createFont();
        font.setFontHeightInPoints(short1);
        font1.setFontHeightInPoints(short2);
        style.setFont(font);
        style1.setFont(font1);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] data: dataResume){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,data[1],style);
            createCell(row,columnCount++,data[2],style);
            createCell(row,columnCount++,data[3],style);
            createCell(row,columnCount++,Double.parseDouble(data[4].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[5].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[6].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[7].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[8].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[9].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[10].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[11].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[12].toString()),style1);

        }
    }

    public void export(HttpServletResponse response) throws IOException {
        if(dataComplete.size()>0) {
            writeHeaderLine();
            writeDataLines();
        }
        else
        {
            sheet = workbook.createSheet("VS-MC Comparativa");
            sheet.setRandomAccessWindowSize(1000);
            Row row = sheet.createRow(0);

            short short1 = 11;
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints(short1);
            style.setFont(font);

            createCell(row, 0, "La cantidad de registros de descarga no es soportado por Excel ", style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportResume(HttpServletResponse response) throws IOException {
        writeHeaderLineResume();
        writeDataLinesResume();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
