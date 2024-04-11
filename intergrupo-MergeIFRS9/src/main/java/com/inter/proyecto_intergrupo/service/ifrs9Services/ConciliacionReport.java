package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.CondetaRI;
import com.inter.proyecto_intergrupo.model.ifrs9.Deaccount;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ConciliacionReport {
    private SXSSFWorkbook workbook;
    private Sheet sheetCruza;
    private Sheet sheetNoCruza;
    private Sheet sheetNoCruzaCon;
    private Sheet sheetNoCruzaConProvi;
    private List<Object[]> templateConci;
    private List<Object[]> templateSinAjuste;
    private List<Object[]> templateCon;
    private List<Object[]> templateCon2;
    protected static final String EMPTY_VALUE = " ";

    public ConciliacionReport(List<Object[]> templateConci, List<Object[]> tempalteSinAjuste, List<Object[]> templateCon, List<Object[]> templateCon2) {
        this.templateConci = templateConci;
        this.templateSinAjuste = tempalteSinAjuste;
        this.templateCon = templateCon;
        this.templateCon2 = templateCon2;
        workbook = new SXSSFWorkbook(100);
    }

    private void autoResizeColumns(int listSize) {
        for (int colIndex = 0; colIndex < listSize; colIndex++) {
            sheetCruza.autoSizeColumn(colIndex);
        }
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

    private void writeHeaderLine(){
        sheetCruza = workbook.createSheet("Coincide");
        Row row = sheetCruza.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        createCell(row, 0, "Centro", style);
        createCell(row, 1, "Cuenta", style);
        createCell(row, 2, "Valor Diferencia H140", style);
        createCell(row, 3, "Valor Diferencia Condeta", style);
        createCell(row, 4, "Diferencia", style);

        sheetNoCruza = workbook.createSheet("No Coincide");
        row = sheetNoCruza.createRow(0);

        createCell(row, 0, "Centro", style);
        createCell(row, 1, "Cuenta", style);
        createCell(row, 2, "Valor Diferencia H140", style);
        createCell(row, 3, "Valor Diferencia Condeta", style);
        createCell(row, 4, "Diferencia", style);

        if(templateCon2==null) {

            sheetNoCruzaCon = workbook.createSheet("No Coincide Contratos");
            row = sheetNoCruzaCon.createRow(0);

            createCell(row, 0, "Centro", style);
            createCell(row, 1, "Contrato", style);
            createCell(row, 2, "Cuenta", style);
            createCell(row, 3, "Valor Contable", style);
            createCell(row, 4, "Valor Aplicativo", style);
            createCell(row, 5, "Diferencia", style);

            sheetNoCruzaConProvi = workbook.createSheet("No Coincide Contratos 2");
            row = sheetNoCruzaConProvi.createRow(0);

            createCell(row, 0, "Centro", style);
            createCell(row, 1, "Contrato", style);
            createCell(row, 2, "Cuenta", style);
            createCell(row, 3, "Valor Contable", style);
            createCell(row, 4, "Valor Aplicativo", style);
            createCell(row, 5, "Diferencia", style);

        }else{

            sheetNoCruzaCon = workbook.createSheet("No Coincide Contratos");
            row = sheetNoCruzaCon.createRow(0);

            createCell(row, 0, "Centro", style);
            createCell(row, 1, "Contrato", style);
            createCell(row, 2, "Cuenta", style);
            createCell(row, 3, "Valor Contable", style);
            createCell(row, 4, "Valor Apuntes", style);
            createCell(row, 5, "Valor Aplicativo", style);
            createCell(row, 6, "Diferencia", style);

        }
    }

    private void writeDataLines(){
        int rowCount = 1;
        int maxExcel = 1048575;
        CellStyle style = workbook.createCellStyle();
        Font font= workbook.createFont();
        style.setFont(font);
        CellStyle style1 = workbook.createCellStyle();

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        //List<Object[]> Ajuste = templateSinAjuste.get(0);
        for(Object[] tax: templateConci){
            Row row = sheetCruza.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,tax[0].toString(),style);
            createCell(row,columnCount++,tax[1].toString(),style);
            createCell(row,columnCount++,Double.parseDouble(tax[2].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(tax[3].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(tax[4].toString()),style1);

        }

        rowCount = 1;
        for( Object[] tax: templateSinAjuste){
            Row row = sheetNoCruza.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,tax[0].toString(),style);
            createCell(row,columnCount++,tax[1].toString(),style);
            createCell(row,columnCount++,Double.parseDouble(tax[2].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(tax[3].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(tax[4].toString()),style1);
        }

        rowCount = 1;

        if(templateCon2==null) {

            for (int i =0; i < templateCon.size() && i < maxExcel ;i++)
            {
                Row row = sheetNoCruzaCon.createRow(rowCount++);
                int columnCount = 0;
                createCell(row, columnCount++, templateCon.get(i)[0].toString(), style);
                createCell(row, columnCount++, templateCon.get(i)[1].toString(), style);
                createCell(row, columnCount++, templateCon.get(i)[2].toString(), style);
                createCell(row, columnCount++, Double.parseDouble(templateCon.get(i)[3].toString()), style1);
                createCell(row, columnCount++, Double.parseDouble(templateCon.get(i)[4].toString()), style1);
                createCell(row, columnCount++, Double.parseDouble(templateCon.get(i)[5].toString()), style1);
            }
            if(templateCon.size()>maxExcel)
            {
                rowCount = 1;
                for (int i = maxExcel; i < templateCon.size()  ;i++)
                {
                    Row row1 = sheetNoCruzaConProvi.createRow(rowCount++);
                    int columnCount = 0;
                    createCell(row1, columnCount++, templateCon.get(i)[0].toString(), style);
                    createCell(row1, columnCount++, templateCon.get(i)[1].toString(), style);
                    createCell(row1, columnCount++, templateCon.get(i)[2].toString(), style);
                    createCell(row1, columnCount++, Double.parseDouble(templateCon.get(i)[3].toString()), style1);
                    createCell(row1, columnCount++, Double.parseDouble(templateCon.get(i)[4].toString()), style1);
                    createCell(row1, columnCount++, Double.parseDouble(templateCon.get(i)[5].toString()), style1);
                }
            }

        }else{

            for (Object[] tax : templateCon2) {
                Row row = sheetNoCruzaCon.createRow(rowCount++);
                int columnCount = 0;
                createCell(row, columnCount++, tax[0].toString(), style);
                createCell(row, columnCount++, tax[1].toString(), style);
                createCell(row, columnCount++, tax[2].toString(), style);
                createCell(row, columnCount++, Double.parseDouble(tax[3].toString()), style1);
                createCell(row, columnCount++, Double.parseDouble(tax[4].toString()), style1);
                createCell(row, columnCount++, Double.parseDouble(tax[5].toString()), style1);
                createCell(row, columnCount++, Double.parseDouble(tax[6].toString()), style1);

            }

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
