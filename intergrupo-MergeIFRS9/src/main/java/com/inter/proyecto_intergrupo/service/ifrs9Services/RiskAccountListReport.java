package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.RiskAccount;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RiskAccountListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;
    private List<RiskAccount> riskAccountList;
    private List<Object[]> riskAccountListObject;

    public RiskAccountListReport(List<RiskAccount> riskAccountList){
        this.riskAccountList = riskAccountList;
        workbook = new XSSFWorkbook();

    }

    public RiskAccountListReport(List<Object[]> riskAccountListObject, int number){
        this.riskAccountListObject = riskAccountListObject;
        workbook = new XSSFWorkbook();

    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Contabilización Riesgos");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Contrato", style);
        createCell(row,	1, "Código Familia Inicial", style);
        createCell(row,	2, "Código Familia Final", style);
        createCell(row,	3, "Código Cliente", style);
        createCell(row,	4, "Stage Inicial", style);
        createCell(row,	5, "Stage Final", style);
        createCell(row,	6, "EAD Inicial", style);
        createCell(row,	7, "EAD Final", style);
        createCell(row,	8, "EAD Y01 Inicial", style);
        createCell(row,	9, "EAD Y01 Final", style);
        createCell(row,	10, "Importe Inicial", style);
        createCell(row,	11, "Importe Final", style);
        createCell(row,	12, "Valor Ajuste Provisión", style);
        createCell(row,	13, "Número Caso", style);
        createCell(row,	14, "IMP SDFUBA", style);
        createCell(row,	15, "IMP RACREG", style);
        //createCell(row,	16, "Familia", style);
        createCell(row,	16, "Cambia Provision", style);
        createCell(row,	17, "Cambio de Segmento", style);
        createCell(row,	18, "Válida", style);

        sheet1 = workbook.createSheet("Fichero Ajustes Riesgos");
        Row row1 = sheet1.createRow(0);

        createCell(row1,	0, "Periodo", style);
        createCell(row1,	1, "Válida", style);
        createCell(row1,	2, "Importe Inicial", style);
        createCell(row1,	3, "Importe Final", style);
        createCell(row1,	4, "Valor Ajuste Provisión", style);
    }

    private void writeHeaderLineResume(){
        sheet = workbook.createSheet("Contabilización Riesgos");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,	0, "Válida", style);
        createCell(row,	1, "Cambia Stage", style);
        createCell(row,	2, "Cambia Segmento", style);
        createCell(row,	3, "Cambia Provision", style);
        createCell(row,	4, "Contrato", style);
    }

    private void writeHeaderLineResumeLoad(){
        sheet = workbook.createSheet("Contabilización Riesgos");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,	0, "Periodo", style);
        createCell(row,	1, "Válida", style);
        createCell(row,	2, "Importe Inicial", style);
        createCell(row,	3, "Importe Final", style);
        createCell(row,	4, "Valor Ajuste Provisión", style);
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

    private void writeDataLinesResume(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] account: riskAccountListObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,	account[0].toString().toUpperCase()	,style);
            createCell(row,columnCount++,	account[1].toString()	,style);
            createCell(row,columnCount++,	account[2].toString()	,style);
            createCell(row,columnCount++,	Double.parseDouble(account[3].toString())	,style1);
            createCell(row,columnCount++,	account[4].toString()	,style);
        }
    }

    private void writeDataLinesResumeLoad(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] account: riskAccountListObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,	account[0]	,style);
            createCell(row,columnCount++,	account[1]	,style);
            createCell(row,columnCount++,	Double.parseDouble(account[2].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(account[3].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(account[4].toString())	,style1);
        }
    }

    private void writeDataLines(List<Object[]> riskAccountListResumen){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(RiskAccount account: riskAccountList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,	account.getContrato()	,style);
            createCell(row,columnCount++,	account.getCodigoFamiliaInicial()	,style);
            createCell(row,columnCount++,	account.getCodigoFamiliaFinal()	,style);
            createCell(row,columnCount++,	account.getCodigoCliente()	,style);
            createCell(row,columnCount++,	account.getStageInicial()	,style);
            createCell(row,columnCount++,	account.getStageFinal()	,style);
            createCell(row,columnCount++,	account.getEadInicial()	,style1);
            createCell(row,columnCount++,	account.getEadFinal()	,style1);
            createCell(row,columnCount++,	account.getEadY01Inicial()	,style1);
            createCell(row,columnCount++,	account.getEadY01Final()	,style1);
            createCell(row,columnCount++,	account.getImporteInicial()	,style1);
            createCell(row,columnCount++,	account.getImporteFinal()	,style1);
            createCell(row,columnCount++,	account.getValorAjusteProvisión()	,style1);
            createCell(row,columnCount++,	account.getNumeroCaso()	,style);
            createCell(row,columnCount++,	account.getImpSdfuba()	,style1);
            createCell(row,columnCount++,	account.getImpRacreg()	,style1);
            //createCell(row,columnCount++,	account.getFamilia()	,style);
            createCell(row,columnCount++,	account.getCambiaProvision()	,style1);
            createCell(row,columnCount++,	account.getCambioDeSegmento()	,style);
            createCell(row,columnCount++,	account.getValida()	,style);
        }

        rowCount = 1;

        for(Object[] account: riskAccountListResumen){
            Row row = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,	account[0]	,style);
            createCell(row,columnCount++,	account[1]	,style);
            createCell(row,columnCount++,	Double.parseDouble(account[2].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(account[3].toString())	,style1);
            createCell(row,columnCount++,	Double.parseDouble(account[4].toString())	,style1);
        }
    }

    public void export(HttpServletResponse response,List<Object[]> riskAccountListResumen) throws IOException {
        writeHeaderLine();
        writeDataLines(riskAccountListResumen);

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

    public void exportResumeLoad(HttpServletResponse response) throws IOException {
        writeHeaderLineResumeLoad();
        writeDataLinesResumeLoad();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLog(HttpServletResponse response, List<String[]> lista)
    {
        int position=0;
        String[] temporalListNames =lista.get(lista.size()-1);
        List<String> list=new ArrayList<>();

        try {
            exportSubLog(response, lista,position);
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(response.getOutputStream());
            workbook.close();
            outputStream.flush();
            outputStream.close();
        }
        catch (IOException e){

        }
    }

    public void exportSubLog(HttpServletResponse response, List<String[]> lista, int position) throws IOException
    {
        sheet = workbook.createSheet("Log");
        Row row1 = sheet.createRow(1);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "Fila", style);
        createCell(row1, 1, "Columna", style);
        createCell(row1, 2, "Estado", style);

        Row row2 = sheet.createRow(0);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for(int i =0; i<lista.size()-1 ; i++)
        {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,lista.get(i)[0],style);
            createCell(row,columnCount++,lista.get(i)[1],style);
            createCell(row,columnCount++,lista.get(i)[3],style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

    }
}
