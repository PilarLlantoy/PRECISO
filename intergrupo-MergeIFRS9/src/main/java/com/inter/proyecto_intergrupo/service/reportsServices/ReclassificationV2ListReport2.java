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
import org.hibernate.tool.schema.extract.spi.ExtractionContext;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReclassificationV2ListReport2 {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    private List<ContingentTemplate> contingentTemplateList;
    private List<ReclassificationContingent> contingentTemplateListObject;
    private List<ReclassificationContingentCom> contingentTemplateListObjectCom;
    private List<planoreclasificacion> planoreclasificacionList;
    private List<ContingentIntergroup> contingentIntergroupList;
    private List<Corep> corepList;
    private List<YntpSociety> bankList;
    private List<GarantBank> bancoList;
    private List<Object[]> listResume;

    public ReclassificationV2ListReport2(List<Object[]> listResume){
        this.listResume = listResume;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Reclasificaciones");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "Centro", style);
        createCell(row,	1, "Cuenta Contable", style);
        createCell(row,	2, "Divisa", style);
        createCell(row,	3, "Contrato", style);
        createCell(row,	4, "Nombre Banco", style);
        createCell(row,	5, "Importe", style);
        createCell(row,	6, "Descripción", style);
        createCell(row,	7, "Número Documento", style);

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

        for(Object[] list: listResume){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            createCell(row,columnCount++,	list[0]	,style);
            createCell(row,columnCount++,	list[1]	,style);
            createCell(row,columnCount++,	list[2]	,style);
            createCell(row,columnCount++,	list[3]	,style);
            createCell(row,columnCount++,	list[4]	,style1);
            createCell(row,columnCount++,	list[5]	,style);
            createCell(row,columnCount++,	list[6]	,style);

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
        sheet = workbook.createSheet("Reclasificaciones");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "Centro", style);
        createCell(row,	1, "Cuenta Neocon", style);
        createCell(row,	2, "Cuenta Contable", style);
        createCell(row,	3, "Divisa", style);
        createCell(row,	4, "Contrato", style);
        createCell(row,	5, "Importe", style);
        createCell(row,	6, "Descripción", style);
        createCell(row,	7, "Documento", style);
        createCell(row,	8, "Fecha", style);
        createCell(row,	9, "Valor Base", style);
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

        for(Object[] list: listResume){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            Double doubleVal = Double.parseDouble(list[5].toString());

            String SdoubleVal = String.format("%.2f", doubleVal);

            createCell(row,columnCount++,	list[0]	,style);
            createCell(row,columnCount++,	list[1]	,style);
            createCell(row,columnCount++,	list[2]	,style);
            createCell(row,columnCount++,	list[3]	,style);
            createCell(row,columnCount++,	list[4]	,style);
            createCell(row,columnCount++,	Double.parseDouble(list[5].toString())	,style1);
            createCell(row,columnCount++,	list[6]	,style);
            createCell(row,columnCount++,	list[7]	,style);
            createCell(row,columnCount++,	list[8]	,style);
            createCell(row,columnCount++, Double.parseDouble("0.00")	,style1);

        }
    }


    public void exportTipoAval(HttpServletResponse response, List<String[]> lista, int position) throws IOException
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

        for(int i =0; i<lista.size() && !lista.get(i)[0].equals("Tipo Aval") && !lista.get(i)[0].equals("Avales CRED"); i++)
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
