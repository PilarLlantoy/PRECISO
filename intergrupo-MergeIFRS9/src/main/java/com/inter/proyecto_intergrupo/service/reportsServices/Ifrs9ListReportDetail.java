package com.inter.proyecto_intergrupo.service.reportsServices;

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
import java.util.HashMap;
import java.util.List;

public class Ifrs9ListReportDetail {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private int position;
    private HashMap<String, Object> list;
    private List<Object[]> ifrsList;

    public Ifrs9ListReportDetail(List<Object[]> ifrsList){
        this.ifrsList = ifrsList;
        workbook = new XSSFWorkbook();
        position=0;
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Vertical");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "Consulta", style);
        createCell(row,	1, "Instrumento", style);
        createCell(row,	2, "Subproducto", style);
        createCell(row,	3, "1", style);
        createCell(row,	4, "2", style);
        createCell(row,	5, "3", style);
        createCell(row,	6, "Total General", style);
    }

    private void writeHeaderLineAll(){
        sheet = workbook.createSheet("Ifrs9");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row,0, "Centro", style);
        createCell(row,	1, "Contrato", style);
        createCell(row,	2, "Cuenta Contable", style);
        createCell(row,	3, "Empresa", style);
        createCell(row,	4, "Segmento FINREP", style);
        createCell(row,	5, "Stage", style);
        createCell(row,	6, "Cuenta Contable (Largo 6)", style);
        createCell(row,	7, "Tipo", style);
        createCell(row,	8, "Código Neocon", style);
        createCell(row,	9, "Instrumento", style);
        createCell(row,	10, "Origen", style);
        createCell(row,	11, "Periodo", style);
        createCell(row,	12, "Producto", style);
        createCell(row,	13, "Sector", style);
        createCell(row,	14, "Signo", style);
        createCell(row,	15, "Stage Esp", style);
        createCell(row,	16, "Subproducto", style);
        createCell(row,	17, "Saldo", style);
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style) {

        Cell cell = row.createCell(columCount);

        if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        }

        cell.setCellStyle(style);
    }


    private void writeDataLinesAjusteSinAjusteAll(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for (Object[] ifrs9Report: ifrsList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
            createCell(row,columnCount++,ifrs9Report[0],style);
            createCell(row,columnCount++,ifrs9Report[1],style);
            createCell(row,columnCount++,ifrs9Report[2],style);
            createCell(row,columnCount++,ifrs9Report[3],style);
            createCell(row,columnCount++,ifrs9Report[4],style);
            createCell(row,columnCount++,ifrs9Report[5],style);
            createCell(row,columnCount++,ifrs9Report[6],style);
            createCell(row,columnCount++,ifrs9Report[7],style);
            createCell(row,columnCount++,ifrs9Report[8],style);
            createCell(row,columnCount++,ifrs9Report[9],style);
            createCell(row,columnCount++,ifrs9Report[10],style);
            createCell(row,columnCount++,ifrs9Report[11],style);
            createCell(row,columnCount++,ifrs9Report[12],style);
            createCell(row,columnCount++,ifrs9Report[13],style);
            createCell(row,columnCount++,ifrs9Report[14],style);
            createCell(row,columnCount++,ifrs9Report[15],style);
            createCell(row,columnCount++,ifrs9Report[16],style);
            createCell(row,columnCount++,Double.parseDouble(ifrs9Report[17].toString()),style2);
        }
    }

    /*public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLinesAjusteSinAjuste();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }*/

    public void exportAll(HttpServletResponse response) throws IOException {
        writeHeaderLineAll();
        writeDataLinesAjusteSinAjusteAll();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}
