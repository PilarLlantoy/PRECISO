package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.ValIFRS9;
import com.inter.proyecto_intergrupo.model.ifrs9.ValQueryEEFF;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ValidationIFRS9ListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet2;
    private XSSFSheet sheet3;
    private XSSFSheet sheet4;
    private XSSFSheet sheet5;
    private XSSFSheet sheet6;
    private XSSFSheet sheet7;
    private XSSFSheet sheet8;
    private List<ValIFRS9> repoList;
    private List<Object[]> queryLocList;
    private List<Object[]> queryIFRS9List;
    private List<Object[]> eeffLocList;
    private List<Object[]> eeffIFRS9List;
    private List<Object[]> divisasList;
    private List<Object[]> planosList;
    private List<Object[]> intergrupoList;

    public ValidationIFRS9ListReport(List<ValIFRS9> cuadreList, List<Object[]> queryLoc, List<Object[]> queryIFRS9, List<Object[]> eeffLoc, List<Object[]> eeffIFRS9, List<Object[]> planos, List<Object[]> divisas,List<Object[]> intergrupoList){
        this.repoList = cuadreList;
        this.queryLocList = queryLoc;
        this.queryIFRS9List = queryIFRS9;
        this.eeffLocList = eeffLoc;
        this.eeffIFRS9List = eeffIFRS9;
        this.divisasList = divisas;
        this.planosList = planos;
        this.intergrupoList = intergrupoList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Cuadre Plantillas");
        Row row = sheet.createRow(0);
        sheet2 = workbook.createSheet("EEFF Local");
        Row row2 = sheet2.createRow(0);
        sheet3 = workbook.createSheet("EEFF IFRS9");
        Row row3 = sheet3.createRow(0);
        sheet4 = workbook.createSheet("Query Local");
        Row row4 = sheet4.createRow(0);
        sheet5 = workbook.createSheet("Query IFRS9");
        Row row5 = sheet5.createRow(0);
        sheet6 = workbook.createSheet("Planos");
        Row row6 = sheet6.createRow(0);
        sheet7 = workbook.createSheet("Divisas");
        Row row7 = sheet7.createRow(0);
        sheet8 = workbook.createSheet("Planos Intergrupo");
        Row row8 = sheet8.createRow(0);


        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "Código Consolidación", style);
        createCell(row,	1, "Divisa", style);
        createCell(row,	2, "Perímetro", style);
        createCell(row,	3, "Saldo Provisión", style);
        createCell(row,	4, "Saldo Reclasificación", style);
        createCell(row,	5, "Saldo Primera Vez", style);
        createCell(row,	6, "Saldo Reclasificaciones", style);
        createCell(row,	7, "Saldo Ajustes Manuales", style);
        createCell(row,	8, "Total Plantilla", style);
        createCell(row,	9, "Naturaleza Total", style);
        createCell(row,	10, "EEFF Local", style);
        createCell(row,	11, "Query Local", style);
        createCell(row,	12, "Diferencia Local", style);
        createCell(row,	13, "Nuevo Saldo", style);
        createCell(row,	14, "EEFF IFRS9", style);
        createCell(row,	15, "Query IFRS9", style);
        createCell(row,	16, "Diferencia IFRS9", style);
        createCell(row,	17, "Diferencia Query", style);
        createCell(row,	18, "Diferencia EEFF", style);
        createCell(row,	19, "Fecha", style);


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

        createCell(row6,0, "Cuenta Local", style);
        createCell(row6,1, "Código Consolidación", style);
        createCell(row6,2, "Divisa", style);
        createCell(row6,3, "Divisa España", style);
        createCell(row6,4, "Valor Miles", style);
        createCell(row6,5, "Tasa", style);
        createCell(row6,6, "Observación", style);
        createCell(row6,7, "Nombre Fichero", style);

        createCell(row7,0, "Fecha", style);
        createCell(row7,1, "Divisa", style);
        createCell(row7,2, "Valor", style);

        createCell(row8,0, "Contrato", style);
        createCell(row8,1, "Cuenta", style);
        createCell(row8,2, "Divisa", style);
        createCell(row8,3, "Empresa", style);
        createCell(row8,4, "Fecha Contable", style);
        createCell(row8,5, "Fecha Proceso", style);
        createCell(row8,6, "Importe Crédito", style);
        createCell(row8,7, "Importe Débito", style);
        createCell(row8,8, "Importe Crédito Divisa", style);
        createCell(row8,9, "Importe Débito Divisa", style);
        createCell(row8,10, "Importe Crédito Divisa Expresado", style);
        createCell(row8,11, "Importe Débito Divisa Expresado", style);
        createCell(row8,12, "Importe Total", style);
        createCell(row8,13, "Tipo", style);
        createCell(row8,14, "Codicons", style);
        createCell(row8,15, "Yntp", style);
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
        CellStyle style2 = workbook.createCellStyle();

        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style2.setFont(font);

        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(ValIFRS9 valQueryEEFF: repoList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            BigDecimal totalPlatillaMiles = valQueryEEFF.getSdoTotalPlantilla().divide(BigDecimal.valueOf(1000));

            createCell(row,columnCount++,	valQueryEEFF.getCodicons(),style);
            createCell(row,columnCount++,	valQueryEEFF.getDivisa(),style);
            createCell(row,columnCount++,	valQueryEEFF.getPerimetro(),style);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoProv().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoRec().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoPri().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoRecla().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoAj().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoTotalPlantilla().toString())	,style1);
            createCell(row,columnCount++,	valQueryEEFF.getNaturalezaTotal()	,style);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoEeffLoc().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoQueryLoc().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoDiffLoc().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoNuevo().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoEeffIfrs9().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoQueryIfrs9().toString())	,style1);
            /*if(valQueryEEFF.getCodicons().substring(0,1).equals("2") || valQueryEEFF.getCodicons().substring(0,1).equals("4") || valQueryEEFF.getCodicons().substring(0,1).equals("5"))
                createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoQueryIfrs9().toString())+Double.parseDouble(valQueryEEFF.getSdoEeffIfrs9().toString())	,style1);
            else*/
                createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoQueryIfrs9().toString())-Double.parseDouble(valQueryEEFF.getSdoEeffIfrs9().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoDiffQuery().toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(valQueryEEFF.getSdoDiffEeff().toString())	,style1);
            createCell(row,columnCount++,	valQueryEEFF.getPeriodo()	,style);
        }

        rowCount = 1;

        for(Object[] eeff: eeffLocList){
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

        for(Object[] eeff: eeffIFRS9List) {
            Row row = sheet3.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, eeff[0], style);
            createCell(row, columnCount++, eeff[1], style);
            createCell(row, columnCount++, eeff[2], style);
            createCell(row, columnCount++, eeff[3], style);
            createCell(row, columnCount++, eeff[4], style);
            createCell(row, columnCount++, eeff[5], style);
            createCell(row, columnCount++, eeff[6], style);
            createCell(row, columnCount++, eeff[7], style);
            createCell(row, columnCount++, Double.parseDouble(eeff[8].toString()), style1);

        }

        rowCount = 1;

        for(Object[] query: queryLocList){
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

        for(Object[] query: queryIFRS9List){
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

        for(Object[] query: planosList){
            Row row = sheet6.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++, query[0], style);
            createCell(row,columnCount++, query[1], style);
            createCell(row,columnCount++, query[2], style);
            createCell(row,columnCount++, query[3], style);
            createCell(row,columnCount++, Double.parseDouble(query[4].toString()), style1);
            createCell(row,columnCount++, Double.parseDouble(query[5].toString()), style1);
            createCell(row,columnCount++, query[6], style);
            createCell(row,columnCount++, query[7], style);

        }

        rowCount = 1;

        for(Object[] query: divisasList){
            Row row = sheet7.createRow(rowCount++);
            int columnCount = 0;

            style2.setFont(font);
            style2.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy;@"));

            createCell(row,columnCount++, query[0], style2);
            createCell(row,columnCount++, query[1], style);
            createCell(row,columnCount++, Double.parseDouble(query[2].toString()), style1);


        }

        rowCount = 1;

        for(Object[] query: intergrupoList){
            Row row = sheet8.createRow(rowCount++);
            int columnCount = 0;

            style2.setFont(font);
            style2.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy;@"));

            createCell(row,columnCount++, query[0], style);
            createCell(row,columnCount++, query[1], style);
            createCell(row,columnCount++, query[2], style);
            createCell(row,columnCount++, query[3], style);
            createCell(row,columnCount++, query[4], style);
            createCell(row,columnCount++, query[5], style);
            createCell(row,columnCount++, Double.parseDouble(query[6].toString()), style1);
            createCell(row,columnCount++, Double.parseDouble(query[7].toString()), style1);
            createCell(row,columnCount++, Double.parseDouble(query[8].toString()), style1);
            createCell(row,columnCount++, Double.parseDouble(query[9].toString()), style1);
            createCell(row,columnCount++, Double.parseDouble(query[10].toString()), style1);
            createCell(row,columnCount++, Double.parseDouble(query[11].toString()), style1);
            createCell(row,columnCount++, Double.parseDouble(query[12].toString()), style1);
            createCell(row,columnCount++, query[13], style);
            createCell(row,columnCount++, query[14], style);
            createCell(row,columnCount++, query[15], style);


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
