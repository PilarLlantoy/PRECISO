package com.inter.proyecto_intergrupo.service.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.ParamFechas;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
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

public class ParamFechasListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> fechasList;
    private List<ParamFechas> listFechas;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ParamFechasListReport(List<String[]> fechasList, List<ParamFechas> listFechas) {
        this.fechasList = fechasList;
        this.listFechas = listFechas;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Fechas");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int columnCountH = 0;
        createCell(row, columnCountH++, "Año", style);
        createCell(row, columnCountH++, "Mes", style);
        createCell(row, columnCountH++, "Fecont", style);
        createCell(row, columnCountH++, "01-Balance", style);
        createCell(row, columnCountH++, "02-PYG", style);
        createCell(row, columnCountH++, "Q Aplica", style);
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

        for(ParamFechas paramFechas: listFechas){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,paramFechas.getAno(),style);
            createCell(row,columnCount++,paramFechas.getMes(),style);
            createCell(row,columnCount++,paramFechas.getFecont(),style);
            if(paramFechas.getBalance() != null)
                createCell(row,columnCount++,paramFechas.getBalance(),style);
            else
                createCell(row,columnCount++,"",style);
            if(paramFechas.getPyg() != null)
                createCell(row,columnCount++,paramFechas.getPyg(),style);
            else
                createCell(row,columnCount++,"",style);
            createCell(row,columnCount++,paramFechas.getQaplica(),style);
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

    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Cargue");
        Row row = sheet.createRow(0);
        Row row1 = sheet.createRow(1);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font1 = workbook.createFont();
        font1.setBold(false);
        font1.setFontHeight(10);
        style1.setFont(font1);

        createCell(row, 0, "#Exitosos", style);
        createCell(row, 1, "#Fallidos", style);
        createCell(row, 2, "Estado Final", style);

        createCell(row1, 0, fechasList.get(fechasList.size()-1)[0], style1);
        createCell(row1, 1, fechasList.get(fechasList.size()-1)[1], style1);
        createCell(row1, 2, fechasList.get(fechasList.size()-1)[2], style1);

        if(fechasList.size()>1)
        {
            fechasList.remove(fechasList.size()-1);
            Row row2 = sheet.createRow(2);
            createCell(row2, 0, "Fila", style);
            createCell(row2, 1, "Columna", style);
            createCell(row2, 2, "Estado", style);

            int rowCount = 3;
            for (String[] log : fechasList)
            {
                Row row3 = sheet.createRow(rowCount++);
                int columnCount = 0;
                createCell(row3, columnCount++, log[0], style1);
                createCell(row3, columnCount++, log[1], style1);
                createCell(row3, columnCount++, log[2], style1);
            }
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
