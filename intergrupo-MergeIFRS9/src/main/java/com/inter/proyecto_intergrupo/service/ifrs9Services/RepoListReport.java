package com.inter.proyecto_intergrupo.service.ifrs9Services;

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

public class RepoListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Object[]> repoList;

    public RepoListReport(List<Object[]> repoList){
        this.repoList = repoList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Carga masiva repos");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "Centro de costo", style);
        createCell(row,	1, "Cuenta", style);
        createCell(row,	2, "Divisa", style);
        createCell(row,	3, "Contrato", style);
        createCell(row,	4, "Referencia de cruce", style);
        createCell(row,	5, "Importe", style);
        createCell(row,	6, "Descripción", style);
        createCell(row,	7, "Fecha", style);
        createCell(row,	8, "Tipo de documento", style);
        createCell(row,	9, "Numero de documento", style);
        createCell(row,	10, "Digito de verificación", style);
        createCell(row,	11, "Tipo de perdida", style);
        createCell(row,	12, "Clase de riesgo", style);
        createCell(row,	13, "Tipo de riesgo", style);
        createCell(row,	14, "Producto", style);
        createCell(row,	15, "Proceso", style);
        createCell(row,	16, "Linea operativa", style);
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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(Object[] repo: repoList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            createCell(row,columnCount++,	repo[0].toString()	,style);
            createCell(row,columnCount++,	repo[1].toString()	,style);
            createCell(row,columnCount++,	repo[2].toString()	,style);
            createCell(row,columnCount++,	""	,style);
            createCell(row,columnCount++,	repo[3].toString()	,style);
            createCell(row,columnCount++,	repo[4].toString()	,style);
            createCell(row,columnCount++,	"",style);
            createCell(row,columnCount++,	repo[5].toString(),style);
            createCell(row,columnCount++,	repo[6].toString(),style);
            createCell(row,columnCount++,	repo[7].toString()	,style);
            createCell(row,columnCount++,	""	,style);
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
