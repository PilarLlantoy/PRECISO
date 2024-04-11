package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.information.ChangeAccountQuery;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChangeAccountQueryListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ChangeAccountQuery> changeAccountQueryList;
    private List<Object[]> changeAccountQueryListSub;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ChangeAccountQueryListReport(List<ChangeAccountQuery> changeAccountQueryList, List<Object[]> changeAccountQueryListSub){
        this.changeAccountQueryList = changeAccountQueryList;
        this.changeAccountQueryListSub=changeAccountQueryListSub;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Cambio Cuenta");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Empresa", style);
        createCell(row, 1, "Cuenta", style);
        createCell(row, 2, "Código de consolidación anterior", style);
        createCell(row, 3, "Código de consolidación nuevo", style);
        createCell(row, 4, "Fecha de corte", style);
        createCell(row, 5, "Perímetro IFRS9", style);
        createCell(row, 6, "Observación", style);

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

        for(ChangeAccountQuery change: changeAccountQueryList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,change.getEmpresa(),style);
            createCell(row,columnCount++,change.getCuenta(),style);
            createCell(row,columnCount++,change.getCodiconsAnterior(),style);
            createCell(row,columnCount++,change.getCodiconsNuevo(),style);
            createCell(row,columnCount++,change.getFechaCorte(),style);
            createCell(row,columnCount++,change.getPerimetroIfrs9(),style);
            createCell(row,columnCount++,change.getObservacion(),style);
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

    public void exportExtra(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public static String completeWithZero(String nit){

        int numZeros = 15 - nit.length();

        String zeros = "";
        for (int i = 0; i < numZeros; i++){
            zeros = zeros.concat("0");
        }
        return zeros+ nit;
    }

    public void exportLog(HttpServletResponse response, ArrayList<String[]> lista) throws IOException
    {
        sheet = workbook.createSheet("LOG_TERCEROS");
        Row row1 = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "NIT Contraparte", style);
        createCell(row1, 1, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        for(String[] third: lista){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,third[0],style);
            createCell(row,columnCount++,third[1],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportCodicons(HttpServletResponse response) throws IOException {

        sheet = workbook.createSheet("CambioCodicons");
        Row row = sheet.createRow(0);

        XSSFFont font1 = workbook.createFont();
        font1.setBold(false);
        font1.setFontHeight(11);

        CellStyle style = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);
        style2.setFont(font1);

        createCell(row, 0, "EMPRESA", style);
        createCell(row, 1, "CUENTA", style);
        createCell(row, 2, "CÓDIGO CONSOLIDACIÓN ANTERIOR", style);
        createCell(row, 3, "CÓDIGO CONSOLIDACIÓN NUEVO", style);
        createCell(row, 4, "FECHA CORTE", style);
        createCell(row, 5, "PERÍMETRO IFRS9", style);
        createCell(row, 6, "CAMBIO", style);
        createCell(row, 7, "FECHA PROCESO", style);

        int rowCount = 1;

        CellStyle style1 = workbook.createCellStyle();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] query: changeAccountQueryListSub){
            Row row1 = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row1,columnCount++,query[0],style2);
            createCell(row1,columnCount++,query[1],style2);
            createCell(row1,columnCount++,query[2],style2);
            createCell(row1,columnCount++,query[3],style2);
            createCell(row1,columnCount++,query[4],style2);
            createCell(row1,columnCount++,query[5],style2);
            createCell(row1,columnCount++,query[6],style2);
            if(query[7]!=null)
                createCell(row1,columnCount++,query[7].toString().replace("00:00:00.0",""),style2);

        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
