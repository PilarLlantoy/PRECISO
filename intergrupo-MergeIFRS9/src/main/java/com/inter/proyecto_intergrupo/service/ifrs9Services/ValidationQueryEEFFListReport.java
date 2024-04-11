package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.ValQueryEEFF;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ValidationQueryEEFFListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet2;
    private XSSFSheet sheet3;
    private XSSFSheet sheet4;
    private XSSFSheet sheet5;
    private XSSFSheet sheet6;
    private XSSFSheet sheet7;
    private XSSFSheet sheet8;
    private XSSFSheet sheet9;

    private List<Object[]> repoList;
    private List<Object[]> eefflL;
    private List<Object[]> eeffiL;
    private List<Object[]> querylL;
    private List<Object[]> queryiL;
    private List<Object[]> intlL;
    private List<Object[]> intiL;
    private List<Object[]> ajush;
    private List<Object[]> ajusm;

    public ValidationQueryEEFFListReport(List<Object[]> cuadreList, List<Object[]> eeffl, List<Object[]> eeffi, List<Object[]> queryl, List<Object[]> queryi,List<Object[]> intl,List<Object[]> inti,List<Object[]> ajush,List<Object[]> ajusm){
        this.repoList = cuadreList;
        this.eefflL = eeffl;
        this.eeffiL = eeffi;
        this.querylL = queryl;
        this.queryiL = queryi;
        this.intlL = intl;
        this.intiL = inti;
        this.ajush = ajush;
        this.ajusm = ajusm;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Cuadre Query EEFF");
        Row row = sheet.createRow(0);

        sheet2 = workbook.createSheet("EEFF Local");
        Row row2 = sheet2.createRow(0);

        sheet3 = workbook.createSheet("EEFF IFRS9");
        Row row3 = sheet3.createRow(0);

        sheet4 = workbook.createSheet("Query Local");
        Row row4 = sheet4.createRow(0);

        sheet5 = workbook.createSheet("Query IFRS9");
        Row row5 = sheet5.createRow(0);

        sheet6 = workbook.createSheet("Intergrupo Local");
        Row row6 = sheet6.createRow(0);

        sheet7 = workbook.createSheet("Intergrupo IFRS9");
        Row row7 = sheet7.createRow(0);

        sheet8 = workbook.createSheet("Ajustes Homogeneización");
        Row row8 = sheet8.createRow(0);

        sheet9 = workbook.createSheet("Ajustes Manuales");
        Row row9 = sheet9.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "Fecha Proceso Local", style);
        createCell(row,1, "Fecha Proceso IFRS9", style);
        createCell(row,2, "Código Consolidación", style);
        createCell(row,3, "Descripción", style);
        createCell(row,4, "Tipo Cuenta", style);
        createCell(row,5, "Perímetro", style);
        createCell(row,6, "Intergrupo", style);
        createCell(row,7, "Sociedad IC", style);
        createCell(row,8, "Nombre Sociedad IC", style);
        createCell(row,9, "Divisa", style);
        createCell(row,10, "EEFF local", style);
        createCell(row,11, "EEFF IFRS9", style);
        createCell(row,12, "Query Local", style);
        createCell(row,13, "Query IFRS9", style);
        createCell(row,14, "Intergrupo V2", style);
        createCell(row,15, "Intergrupo IFRS9", style);
        createCell(row,16, "Diferencia Local", style);
        createCell(row,17, "Diferencia IFRS9", style);
        createCell(row,18, "Diferencia EEFF Inter. Local", style);
        createCell(row,19, "Diferencia EEFF Inter. IFRS9", style);
        createCell(row,20, "Saldo Ajustes Manuales", style);
        createCell(row,21, "Saldo Ajustes Homogeneización", style);
        createCell(row,22, "Periodo", style);

        createCell(row2,0, "Descripción", style);
        createCell(row2,1, "Denominación Cuenta", style);
        createCell(row2,2, "Tipo Cuenta", style);
        createCell(row2,3, "Cuenta", style);
        createCell(row2,4, "Sociedad IC", style);
        createCell(row2,5, "Descripción IC", style);
        createCell(row2,6, "Desgloce", style);
        createCell(row2,7, "Divisa", style);
        createCell(row2,8, "Saldo", style);

        createCell(row3,0, "Descripción", style);
        createCell(row3,1, "Denominación Cuenta", style);
        createCell(row3,2, "Tipo Cuenta", style);
        createCell(row3,3, "Cuenta", style);
        createCell(row3,4, "Sociedad IC", style);
        createCell(row3,5, "Descripción IC", style);
        createCell(row3,6, "Desgloce", style);
        createCell(row3,7, "Divisa", style);
        createCell(row3,8, "Saldo", style);

        createCell(row4,0, "Empresa", style);
        createCell(row4,1, "Cuenta Local", style);
        createCell(row4,2, "Código Consolidación", style);
        createCell(row4,3, "Fecha Contable", style);
        createCell(row4,4, "Divisa", style);
        createCell(row4,5, "Divisa España", style);
        createCell(row4,6, "Saldo", style);
        createCell(row4,7, "Fecha Proceso", style);

        createCell(row5,0, "Empresa", style);
        createCell(row5,1, "Cuenta Local", style);
        createCell(row5,2, "Código Consolidación", style);
        createCell(row5,3, "Fecha Contable", style);
        createCell(row5,4, "Divisa", style);
        createCell(row5,5, "Divisa España", style);
        createCell(row5,6, "Saldo", style);
        createCell(row5,7, "Fecha Proceso", style);

        createCell(row6,0, "YNTP Reportante", style);
        createCell(row6,1, "Código Consolidación", style);
        createCell(row6,2, "Divisa", style);
        createCell(row6,3, "Divisa España", style);
        createCell(row6,4, "YNTP", style);
        createCell(row6,5, "Sociedad YNTP", style);
        createCell(row6,6, "Contrato", style);
        createCell(row6,7, "NIT", style);
        createCell(row6,8, "Cód. País", style);
        createCell(row6,9, "País", style);
        createCell(row6,10, "Cuenta Local", style);
        createCell(row6,11, "Saldo", style);

        createCell(row7,0, "YNTP Reportante", style);
        createCell(row7,1, "Código Consolidación", style);
        createCell(row7,2, "Divisa", style);
        createCell(row7,3, "Divisa España", style);
        createCell(row7,4, "YNTP", style);
        createCell(row7,5, "Sociedad YNTP", style);
        createCell(row7,6, "Contrato", style);
        createCell(row7,7, "NIT", style);
        createCell(row7,8, "Cód. País", style);
        createCell(row7,9, "País", style);
        createCell(row7,10, "Cuenta Local", style);
        createCell(row7,11, "Saldo", style);

        createCell(row8,0, "Tipo Asiento", style);
        createCell(row8,1, "Descripción Asiento", style);
        createCell(row8,2, "Código Consolidación", style);
        createCell(row8,3, "Sociedad IC", style);
        createCell(row8,4, "Descripción IC", style);
        createCell(row8,5, "Divisa", style);
        createCell(row8,6, "Debe 1", style);
        createCell(row8,7, "Debe 2", style);
        createCell(row8,8, "Haber 1", style);
        createCell(row8,9, "Haber 2", style);
        createCell(row8,10, "Saldo Total", style);

        createCell(row9,0, "Código Consolidación", style);
        createCell(row9,1, "Divisa", style);
        createCell(row9,2, "Saldo", style);

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
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);

        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] valQueryEEFF: repoList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,	valQueryEEFF[5]	,style);
            createCell(row,columnCount++,	valQueryEEFF[4]	,style);
            createCell(row,columnCount++,	valQueryEEFF[0]	,style);
            createCell(row,columnCount++,	valQueryEEFF[1]	,style);
            createCell(row,columnCount++,	valQueryEEFF[19]	,style);
            createCell(row,columnCount++,	valQueryEEFF[7]	,style);
            createCell(row,columnCount++,	valQueryEEFF[6]	,style);
            createCell(row,columnCount++,	valQueryEEFF[18]	,style);
            createCell(row,columnCount++,	valQueryEEFF[2]	,style);
            createCell(row,columnCount++,	valQueryEEFF[3]	,style);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[13].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[12].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[17].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[16].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[15].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[14].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[11].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[10].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[9].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[8].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[21].toString().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF[20].toString())	,style1);
            createCell(row,columnCount++,	valQueryEEFF[22]	,style);

        }

        rowCount = 1;

        for(Object[] eeff: eefflL){
            Row row = sheet2.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, eeff[0], style);
            createCell(row,columnCount++, eeff[1], style);
            createCell(row,columnCount++, eeff[2], style);
            createCell(row,columnCount++, eeff[3], style);
            createCell(row,columnCount++, eeff[4], style);
            createCell(row,columnCount++, eeff[5], style);
            createCell(row,columnCount++, eeff[6], style);
            createCell(row,columnCount++, eeff[7], style);
            createCell(row,columnCount++, Double.parseDouble(eeff[8].toString()), style1);
        }

        rowCount = 1;

        for(Object[] eeff: eeffiL){
            Row row = sheet3.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, eeff[0], style);
            createCell(row,columnCount++, eeff[1], style);
            createCell(row,columnCount++, eeff[2], style);
            createCell(row,columnCount++, eeff[3], style);
            createCell(row,columnCount++, eeff[4], style);
            createCell(row,columnCount++, eeff[5], style);
            createCell(row,columnCount++, eeff[6], style);
            createCell(row,columnCount++, eeff[7], style);
            createCell(row,columnCount++, Double.parseDouble(eeff[8].toString()), style1);

        }

        rowCount = 1;

        for(Object[] query: querylL){
            Row row = sheet4.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, query[0], style);
            createCell(row,columnCount++, query[1], style);
            createCell(row,columnCount++, query[2], style);
            createCell(row,columnCount++, query[3], style);
            createCell(row,columnCount++, query[4], style);
            createCell(row,columnCount++, query[5], style);
            createCell(row,columnCount++, Double.parseDouble(query[6].toString()), style1);
            createCell(row,columnCount++, query[7], style);

        }

        rowCount = 1;

        for(Object[] query: queryiL){
            Row row = sheet5.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, query[0], style);
            createCell(row,columnCount++, query[1], style);
            createCell(row,columnCount++, query[2], style);
            createCell(row,columnCount++, query[3], style);
            createCell(row,columnCount++, query[4], style);
            createCell(row,columnCount++, query[5], style);
            createCell(row,columnCount++, Double.parseDouble(query[6].toString()), style1);
            createCell(row,columnCount++, query[7], style);

        }

        rowCount = 1;

        for(Object[] inter: intlL) {
            Row row = sheet6.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, inter[0], style);
            createCell(row, columnCount++, inter[1], style);
            createCell(row, columnCount++, inter[2], style);
            createCell(row, columnCount++, inter[3], style);
            createCell(row, columnCount++, inter[4], style);
            createCell(row, columnCount++, inter[5], style);
            createCell(row, columnCount++, inter[6], style);
            createCell(row, columnCount++, inter[7], style);
            createCell(row, columnCount++, inter[8], style);
            createCell(row, columnCount++, inter[9], style);
            createCell(row, columnCount++, inter[10], style);
            createCell(row, columnCount++, Double.parseDouble(inter[11].toString()), style1);

        }

        rowCount = 1;

        for(Object[] inter: intiL){
            Row row = sheet7.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, inter[0], style);
            createCell(row,columnCount++, inter[1], style);
            createCell(row,columnCount++, inter[2], style);
            createCell(row,columnCount++, inter[3], style);
            createCell(row,columnCount++, inter[4], style);
            createCell(row,columnCount++, inter[5], style);
            createCell(row,columnCount++, inter[6], style);
            createCell(row,columnCount++, inter[7], style);
            createCell(row,columnCount++, inter[8], style);
            createCell(row,columnCount++, inter[9], style);
            createCell(row,columnCount++, inter[10], style);
            createCell(row,columnCount++, Double.parseDouble(inter[11].toString()), style1);

        }

        rowCount = 1;

        for(Object[] adj: ajush){
            Row row = sheet8.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, adj[0], style);
            createCell(row,columnCount++, adj[1], style);
            createCell(row,columnCount++, adj[2], style);
            createCell(row,columnCount++, adj[3], style);
            createCell(row,columnCount++, adj[4], style);
            createCell(row,columnCount++, adj[5], style);
            createCell(row,columnCount++, Double.parseDouble(adj[6].toString()), style1);
            createCell(row,columnCount++, Double.parseDouble(adj[7].toString()), style1);
            createCell(row,columnCount++, Double.parseDouble(adj[8].toString()), style1);
            createCell(row,columnCount++, Double.parseDouble(adj[9].toString()), style1);
            createCell(row,columnCount++, Double.parseDouble(adj[10].toString()), style1);

        }

        rowCount = 1;

        for(Object[] adj: ajusm){
            Row row = sheet9.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, adj[0], style);
            createCell(row,columnCount++, adj[1], style);
            createCell(row,columnCount++, Double.parseDouble(adj[2].toString()), style1);

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
