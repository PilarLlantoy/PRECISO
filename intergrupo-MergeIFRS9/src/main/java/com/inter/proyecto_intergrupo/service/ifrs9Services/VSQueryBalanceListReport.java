package com.inter.proyecto_intergrupo.service.ifrs9Services;

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

public class VSQueryBalanceListReport {

    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private List<Object[]> dataResume;
    private List<Object[]> dataComplete;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public VSQueryBalanceListReport (List<Object[]> dataComplete, List<Object[]> dataResume){
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
        sheet = workbook.createSheet("VS-CC-Query");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        short short1 = 11;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(short1);
        style.setFont(font);

        createCell(row, 0, "Instrumento", style);
        createCell(row, 1, "Subproducto", style);
        createCell(row, 2, "Neocon", style);
        createCell(row, 3, "Cuenta", style);
        createCell(row, 4, "Nombre Cuenta", style);
        createCell(row, 5, "Divisa", style);
        createCell(row, 6, "Tasa", style);
        createCell(row, 7, "VS Importe Saldo Local", style);
        createCell(row, 8, "QL Saldo 0013", style);
        createCell(row, 9, "QL Saldo 0064", style);
        createCell(row, 10, "QL Saldo 0060", style);
        createCell(row, 11, "Fecha QL", style);
        createCell(row, 12, "QI Saldo 0013", style);
        createCell(row, 13, "QI Saldo 0064", style);
        createCell(row, 14, "QI Saldo 0060", style);
        createCell(row, 15, "Fecha QI", style);
        createCell(row, 16, "CC Saldo Aplicativo", style);
        createCell(row, 17, "CC Saldo Contabilidad", style);
        createCell(row, 18, "CC Anexo 8", style);
        createCell(row, 19, "CC Diferencia", style);
        createCell(row, 20, "Dif. VS - QL 0013", style);
        createCell(row, 21, "Dif. VS - QI 0060", style);
        createCell(row, 22, "Dif. VS - Concil", style);
    }

    private void writeHeaderLineResume(){
        sheet = workbook.createSheet("VS-CC-Query");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        short short1 = 11;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(short1);
        style.setFont(font);

        createCell(row, 0, "Instrumento", style);
        createCell(row, 1, "Subproducto", style);
        createCell(row, 2, "Neocon", style);
        createCell(row, 3, "Cuenta", style);
        createCell(row, 4, "Nombre Cuenta", style);
        createCell(row, 5, "Divisa", style);
        createCell(row, 6, "Dif. VS - QL 0013", style);
        createCell(row, 7, "Dif. VS - QI 0060", style);
        createCell(row, 8, "Dif. VS - Concil", style);
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

            createCell(row,columnCount++,data[0],style);
            createCell(row,columnCount++,data[1],style);
            createCell(row,columnCount++,data[2],style);
            createCell(row,columnCount++,data[3],style);
            createCell(row,columnCount++,data[4],style);
            createCell(row,columnCount++,data[5],style);
            createCell(row,columnCount++,Double.parseDouble(data[6].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[7].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[8].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[9].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[10].toString()),style1);
            createCell(row,columnCount++,data[11],style);
            createCell(row,columnCount++,Double.parseDouble(data[12].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[13].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[14].toString()),style1);
            createCell(row,columnCount++,data[15],style);
            createCell(row,columnCount++,Double.parseDouble(data[16].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[17].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[18].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[19].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[20].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[21].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[22].toString()),style1);

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

            createCell(row,columnCount++,data[0],style);
            createCell(row,columnCount++,data[1],style);
            createCell(row,columnCount++,data[2],style);
            createCell(row,columnCount++,data[3],style);
            createCell(row,columnCount++,data[4],style);
            createCell(row,columnCount++,data[5],style);
            createCell(row,columnCount++,Double.parseDouble(data[6].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[7].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(data[8].toString()),style1);

        }
    }

    public void export(HttpServletResponse response) throws IOException {
        if(dataComplete.size()>0) {
            writeHeaderLine();
            writeDataLines();
        }
        else
        {
            sheet = workbook.createSheet("VS-CC-Query");
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
