package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.bank.planoreclasificacion;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.*;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReclassificationV2ListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;
    private XSSFSheet sheet2;
    private XSSFSheet sheet3;
    private List<ContingentTemplate> contingentTemplateList;
    private List<ReclassificationContingent> contingentTemplateListObject;
    private List<ReclassificationContingentCom> contingentTemplateListObjectCom;
    private List<planoreclasificacion> planoreclasificacionList;
    private List<ContingentIntergroup> contingentIntergroupList;
    private List<Corep> corepList;

    public ReclassificationV2ListReport(){
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Inventario Contingentes");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "Contrato", style);
        createCell(row,	1, "Cuenta Contable", style);
        //createCell(row,	2, "Fecha Alta", style);
        createCell(row,	2, "Fecha Cierre", style);
        //createCell(row,	4, "Fecha Vencimiento", style);
        //createCell(row,	5, "Intergrupo", style);
        createCell(row,	3, "NIT", style);
        createCell(row,	4, "Nombre Banco", style);
        //createCell(row,	8, "Nombre Cliente", style);
        //createCell(row,	9, "Número", style);
        //createCell(row,	10, "País Banco", style);
        createCell(row,	5, "Periodo", style);
        //createCell(row,	12, "Prefijo", style);
        createCell(row,	6, "Saldo Divisa", style);
        createCell(row,	7, "Saldo Pesos", style);
        createCell(row,	8, "Tasa", style);
        createCell(row,	9, "Tipo Moneda", style);
        createCell(row,	10, "Divisa", style);

        sheet1 = workbook.createSheet("Lista Bancos Yntp");
        Row row1 = sheet1.createRow(0);
        createCell(row1, 0, "Yntp", style);
        createCell(row1, 1, "Banco Sociedad Corta", style);

        sheet2 = workbook.createSheet("Lista Bancos Garante");
        Row row2 = sheet2.createRow(0);
        createCell(row2, 0, "NIT", style);
        createCell(row2, 1, "Banco Nombre Similar", style);

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

        for(ContingentTemplate contingentes: contingentTemplateList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            createCell(row,columnCount++,	contingentes.getContrato()	,style);
            createCell(row,columnCount++,	contingentes.getCuentaContable()	,style);
            //createCell(row,columnCount++,	formato.format(contingentes.getFechaAlta())	,style);
            createCell(row,columnCount++,	formato.format(contingentes.getFechaCierre())	,style);
            //createCell(row,columnCount++,	formato.format(contingentes.getFechaVenciemiento())	,style);
            //createCell(row,columnCount++,	contingentes.getIntergrupo()	,style);
            createCell(row,columnCount++,	contingentes.getNit(),style);
            createCell(row,columnCount++,	contingentes.getNombreBanco(),style);
            //createCell(row,columnCount++,	contingentes.getNombreCliente(),style);
            //createCell(row,columnCount++,	contingentes.getNumero()	,style);
            //createCell(row,columnCount++,	contingentes.getPaisBanco()	,style);
            createCell(row,columnCount++,	contingentes.getPeriodo()	,style);
            //createCell(row,columnCount++,	contingentes.getPrefijo()	,style);
            createCell(row,columnCount++,	contingentes.getSaldoDivisa()	,style);
            createCell(row,columnCount++,	contingentes.getSaldoPesos()	,style1);
            createCell(row,columnCount++,	contingentes.getTasa()	,style1);
            createCell(row,columnCount++,	contingentes.getTipoMoneda(),style);
            createCell(row,columnCount++,	contingentes.getDivisa().getId(),style);

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

    public void exportReport(HttpServletResponse response) throws IOException {
        writeHeaderLineReport();
        writeDataLinesReport();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    private void writeHeaderLineReport(){
        sheet = workbook.createSheet("Reporte Rp21");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "Cuenta Contable", style);
        createCell(row,	1, "NIT", style);
        createCell(row,	2, "Divisa", style);
        createCell(row,	3, "Saldo Pesos", style);
        createCell(row,	4, "Saldo Divisa", style);
    }

    private void writeDataLinesReport(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

    }

    public void exportLog(HttpServletResponse response, List<String[]> lista, List<Object[]> diff, List<Object[]> diffRest) throws IOException
    {
        int position=0;
        String[] temporalListNames =lista.get(lista.size()-1);
        List<String> list=new ArrayList<>();

        export(response, lista,position, diff, diffRest);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void export(HttpServletResponse response, List<String[]> lista, int position, List<Object[]> diff, List<Object[]> diffRest) throws IOException
    {
        sheet = workbook.createSheet("Log Carga");
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

        for(int i =0; i<lista.size() && !lista.get(i)[0].equals("Plantilla"); i++)
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

        //Sheet 2

        sheet1 = workbook.createSheet("Dif. Saldos Intergrupo-Depósitos");
        Row row3 = sheet1.createRow(0);
        createCell(row3, 0, "Contrato", style);
        createCell(row3, 1, "Cuenta Contable MIS", style);
        createCell(row3, 2, "Cuenta Contable Intergrupo", style);
        createCell(row3, 3, "Saldo MIS", style);
        createCell(row3, 4, "Saldo Intergrupo", style);
        createCell(row3, 5, "Diferencia", style);

        rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        CellStyle style1 = workbook.createCellStyle();
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] dif: diff){
            Row row = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,dif[0],style);
            createCell(row,columnCount++,dif[1],style);
            createCell(row,columnCount++,dif[2],style);
            createCell(row,columnCount++,dif[3],style1);
            createCell(row,columnCount++,dif[4],style1);
            createCell(row,columnCount++,dif[5],style1);

        }

        //Sheet 3

        sheet2 = workbook.createSheet("Dif. Saldos Intergrupo");
        Row row4 = sheet2.createRow(0);
        createCell(row4, 0, "Contrato", style);
        createCell(row4, 1, "Cuenta Contable Intergrupo", style);
        createCell(row4, 2, "Saldo Intergrupo", style);

        rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        for(Object[] difR: diffRest){
            Row row = sheet2.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,difR[0],style);
            createCell(row,columnCount++,difR[1],style);
            createCell(row,columnCount++,difR[2],style1);
        }

    }

}
