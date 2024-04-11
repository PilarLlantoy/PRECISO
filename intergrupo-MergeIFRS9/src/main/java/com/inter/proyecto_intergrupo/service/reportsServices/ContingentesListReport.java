package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.bank.planoreclasificacion;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.*;
import com.inter.proyecto_intergrupo.model.temporal.CorepTeporalH;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
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

public class ContingentesListReport {
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
    private List<YntpSociety> bankList;
    private List<GarantBank> bancoList;
    private List<Country> paisList;
    private List<Object[]> listResume;

    public ContingentesListReport(List<ContingentTemplate> contingentTemplateList,List<Object[]> listResume,List<YntpSociety> bankList,List<GarantBank> bancoList,List<ReclassificationContingentCom> contingentTemplateListObjectCom,List<ReclassificationContingent> contingentTemplateListObject, List<Corep> corepList,List<planoreclasificacion> planoreclasificacionList,List<ContingentIntergroup> contingentIntergroupList,List<Country> paisList){
        this.contingentTemplateList = contingentTemplateList;
        this.listResume = listResume;
        this.bankList=bankList;
        this.bancoList=bancoList;
        this.contingentTemplateListObjectCom = contingentTemplateListObjectCom;
        this.contingentTemplateListObject = contingentTemplateListObject;
        this.corepList=corepList;
        this.planoreclasificacionList = planoreclasificacionList;
        this.contingentIntergroupList = contingentIntergroupList;
        this.paisList = paisList;
        workbook = new XSSFWorkbook();
    }

    /*public ContingentesListReport(List<ReclassificationContingent> contingentTemplateListObject, int number,List<Corep> corepList)
    {
        this.contingentTemplateListObject = contingentTemplateListObject;
        this.corepList=corepList;
        workbook = new XSSFWorkbook();
    }

    public ContingentesListReport(List<ReclassificationContingentCom> contingentTemplateListObjectCom){
        this.contingentTemplateListObjectCom = contingentTemplateListObjectCom;
        workbook = new XSSFWorkbook();

    }

    public ContingentesListReport(List<planoreclasificacion> planoreclasificacionList,List<ContingentIntergroup> contingentIntergroupList){
        this.planoreclasificacionList = planoreclasificacionList;
        this.contingentIntergroupList = contingentIntergroupList;
        workbook = new XSSFWorkbook();
    }*/

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Inventario Contingentes");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "FECHA", style);
        createCell(row,	1, "CUENTA_CONTABLE", style);
        createCell(row,	2, "DIVISA", style);
        createCell(row,	3, "SALDO_DIVISA", style);
        createCell(row,	4, "FECHA_ALTA", style);
        createCell(row,	5, "FECHA_DE_VENCIMIENTO", style);
        createCell(row,	6, "NIT", style);
        createCell(row,	7, "NOMBRE_CLIENTE", style);
        createCell(row,	8, "CONTRATO", style);
        createCell(row,	9, "NOMBRE_BANCO", style);
        createCell(row,	10, "PAIS_BANCO", style);
        createCell(row,	11, "INTERGRUPO", style);
        createCell(row,	12, "TASA", style);
        createCell(row,	13, "PREFIJO", style);
        createCell(row,	14, "NUMERO", style);
        createCell(row,	15, "NIT BANCO", style);
        createCell(row,	16, "PERIODO", style);
        createCell(row,	17, "SALDO_PESOS", style);
        createCell(row,	18, "TIPO MONEDA", style);

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

            createCell(row,columnCount++,	formato.format(contingentes.getFechaCierre())	,style);
            createCell(row,columnCount++,	contingentes.getCuentaContable()	,style);
            if(contingentes.getDivisa()!=null)
                createCell(row,columnCount++,	contingentes.getDivisa().getId(),style);
            else
                createCell(row,columnCount++,	"",style);
            createCell(row,columnCount++,	contingentes.getSaldoDivisa()	,style1);
            createCell(row,columnCount++,	formato.format(contingentes.getFechaAlta())	,style);
            createCell(row,columnCount++,	formato.format(contingentes.getFechaVenciemiento())	,style);
            createCell(row,columnCount++,	contingentes.getNit(),style);
            createCell(row,columnCount++,	contingentes.getNombreCliente(),style);
            createCell(row,columnCount++,	contingentes.getContrato()	,style);
            createCell(row,columnCount++,	contingentes.getNombreBanco(),style);
            createCell(row,columnCount++,	contingentes.getPaisBanco()	,style);
            createCell(row,columnCount++,	contingentes.getIntergrupo()	,style);
            createCell(row,columnCount++,	contingentes.getTasa()	,style1);
            createCell(row,columnCount++,	contingentes.getPrefijo()	,style);
            createCell(row,columnCount++,	contingentes.getNumero()	,style);
            createCell(row,columnCount++,	contingentes.getNitBanco(),style);
            createCell(row,columnCount++,	contingentes.getPeriodo()	,style);
            createCell(row,columnCount++,	contingentes.getSaldoPesos()	,style1);
            createCell(row,columnCount++,	contingentes.getTipoMoneda(),style);

        }
        rowCount = 1;

        for(YntpSociety yntpSociety: bankList){
            Row row = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,yntpSociety.getYntp(),style);
            createCell(row,columnCount++,yntpSociety.getSociedadDescripcionCorta(),style);
        }

        rowCount = 1;

        for(GarantBank garantBank: bancoList){
            Row row = sheet2.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,garantBank.getNit(),style);
            createCell(row,columnCount++,garantBank.getNombreSimilar(),style);
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

        for (Object[] contingentes : listResume){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,	contingentes[0]	,style);
            createCell(row,columnCount++,	contingentes[1]	,style);
            createCell(row,columnCount++,	contingentes[2]	,style);
            createCell(row,columnCount++,	BigDecimal.valueOf(Double.parseDouble(contingentes[3].toString())).doubleValue()	,style1);
            createCell(row,columnCount++,	BigDecimal.valueOf(Double.parseDouble(contingentes[4].toString())).doubleValue()	,style1);
        }
    }

    public void exportLog(HttpServletResponse response, List<String[]> lista, String option) throws IOException
    {
        int position=0;
        //String[] temporalListNames =lista.get(lista.size()-1);
        //List<String> list=new ArrayList<>();
        if(option.equals("TipoAval") || option.equals("AvalesCRED"))
        {
            exportTipoAval(response, lista,position);
        }
        else if(option.equals("Plantilla"))
        {
            exportPlantilla(response, lista,position);
        }
        else if(option.equals("Banco"))
        {
            exportBanco(response, lista,position);
        }
        else if(option.equals("Contrato"))
        {
            exportContrato(response, lista,position);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
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

    public void exportPlantilla(HttpServletResponse response, List<String[]> lista, int position) throws IOException
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

        sheet1 = workbook.createSheet("Lista Bancos Yntp");
        Row row3 = sheet1.createRow(0);
        createCell(row3, 0, "Yntp", style);
        createCell(row3, 1, "Banco Sociedad Corta", style);

        sheet2 = workbook.createSheet("Lista Bancos Garante");
        Row row4 = sheet2.createRow(0);
        createCell(row4, 0, "NIT", style);
        createCell(row4, 1, "Banco Nombre Similar", style);

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);


        for(int i =0; i<lista.size() && !lista.get(i)[0].equals("Plantilla"); i++)
        {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;

        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        rowCount = 1;

        for(YntpSociety yntpSociety: bankList){
            Row rowP = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++,yntpSociety.getYntp(),style);
            createCell(rowP,columnCount++,yntpSociety.getSociedadDescripcionLarga(),style);
        }

        rowCount = 1;

        for(GarantBank garantBank: bancoList){
            Row rowP = sheet2.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++,garantBank.getNit(),style);
            createCell(rowP,columnCount++,garantBank.getNombreSimilar(),style);
        }

    }

    public void exportBanco(HttpServletResponse response, List<String[]> lista, int position) throws IOException
    {
        sheet = workbook.createSheet("Bancos_Pendientes");
        Row row = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        CellStyle cellStyle = workbook.createCellStyle();
        Font cellFont = workbook.createFont();
        cellFont.setColor(Font.COLOR_RED);
        cellStyle.setFont(cellFont);

        createCell(row, 0, "NIT", style);
        createCell(row, 1, "Nombre Banco Real", style);
        createCell(row, 2, "Nombres Similares", style);
        createCell(row, 3, "Id País", style);

        createCell(row, 5, "No se realizó el cargue de la plantilla de contingentes. Parametrice los bancos de está plantilla y vuelva a intentar el cargue", cellStyle);

        sheet1 = workbook.createSheet("Lista Paises");
        Row row3 = sheet1.createRow(0);
        createCell(row3, 0, "Código", style);
        createCell(row3, 1, "País", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        for(int i =0; i<lista.size() && !lista.get(i)[0].equals("Plantilla"); i++)
        {
            Row row1 = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row1, columnCount++, lista.get(i)[0], style);
            createCell(row1, columnCount++, "", style);
            createCell(row1, columnCount++, lista.get(i)[2], style);

        }

        rowCount = 1;

        for(Country country: paisList){
            Row rowP = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++,country.getId(),style);
            createCell(rowP,columnCount++,country.getNombre(),style);
        }

    }

    public void exportContrato(HttpServletResponse response, List<String[]> lista, int position) throws IOException
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

        sheet1 = workbook.createSheet("Lista Bancos Yntp");
        Row row3 = sheet1.createRow(0);
        createCell(row3, 0, "Yntp", style);
        createCell(row3, 1, "Banco Sociedad Corta", style);

        sheet2 = workbook.createSheet("Lista Bancos Garante");
        Row row4 = sheet2.createRow(0);
        createCell(row4, 0, "NIT", style);
        createCell(row4, 1, "Banco Nombre Similar", style);

        sheet3 = workbook.createSheet("Contratos Parametrizados");
        Row row5 = sheet3.createRow(0);
        createCell(row5, 0, "Contrato", style);
        //createCell(row5, 1, "Banco", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        for(int i =0; i<lista.size() && !lista.get(i)[3].equals("CONTRATO"); i++)
        {
            Row row6 = sheet3.createRow(rowCount++);
            int columnCount = 0;
            createCell(row6, columnCount++, lista.get(i)[0], style);
            //createCell(row6, columnCount++, lista.get(i)[2], style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        rowCount = 1;

        for(YntpSociety yntpSociety: bankList){
            Row rowP = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++,yntpSociety.getYntp(),style);
            createCell(rowP,columnCount++,yntpSociety.getSociedadDescripcionLarga(),style);
        }

        rowCount = 1;

        for(GarantBank garantBank: bancoList){
            Row rowP = sheet2.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++,garantBank.getNit(),style);
            createCell(rowP,columnCount++,garantBank.getNombreSimilar(),style);
        }

    }

    public void exportReclasificacion(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Reclasificación");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        createCell(row,0, "CUENTA_CONTABLE", style);
        createCell(row,	1, "DIVISA", style);
        createCell(row,	2, "TASA", style);
        createCell(row,	3, "SALDO_DIVISA", style);
        createCell(row,	4, "SALDO_PESOS", style);
        createCell(row,	5, "F_ALTA", style);
        createCell(row,	6, "F_VCTO", style);
        createCell(row,	7, "F_CIERRE", style);
        createCell(row,	8, "NIT", style);
        createCell(row,	9, "CONTRATO", style);
        createCell(row,	10, "NOMBRE_CLIENTE", style);
        createCell(row,	11, "NIT BANCO GARANTE", style);
        createCell(row,	12, "NOMBRE_BANCO", style);
        createCell(row,	13, "PAIS_BANCO", style);
        createCell(row,	14, "INTERGRUPO", style);
        createCell(row,	15, "YNTP", style);
        createCell(row,	16, "TIPO_AVAL ", style);
        createCell(row,	17, "TIPO_AVAL_ORIGEN", style);
        createCell(row,	18, "CTA_CONTABLE_60", style);
        createCell(row,	19, "TIPO MONEDA", style);
        createCell(row,	20, "CODCONSOL", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);
        style.setFont(font);
        for (ReclassificationContingent contingentes : contingentTemplateListObject){
            row = sheet.createRow(rowCount++);
            int columnCount = 0;

            columnCount = 0;

            createCell(row,columnCount++,	contingentes.getCuentaContable(),style);
            createCell(row,columnCount++,	contingentes.getDivisa(),style);
            createCell(row,columnCount++,	contingentes.getVrDiv(),style1);
            createCell(row,columnCount++,	contingentes.getSaldoDivisa(),style1);
            createCell(row,columnCount++,	contingentes.getSaldoPesos(),style1);
            createCell(row,columnCount++,	contingentes.getFechaAlta(),style);
            createCell(row,columnCount++,	contingentes.getFechaVenciemiento(),style);
            createCell(row,columnCount++,	contingentes.getFechaCierre(),style);
            createCell(row,columnCount++,	contingentes.getNit(),style);
            createCell(row,columnCount++,	contingentes.getContrato(),style);
            createCell(row,columnCount++,	contingentes.getNombreCliente(),style);
            createCell(row,columnCount++,	contingentes.getNitBanco(),style);
            createCell(row,columnCount++,	contingentes.getNombreBanco(),style);
            createCell(row,columnCount++,	contingentes.getPaisBanco(),style);
            createCell(row,columnCount++,	contingentes.getIntergrupo(),style);
            createCell(row,columnCount++,	contingentes.getYntp(),style);
            createCell(row,columnCount++,	contingentes.getTipoAval(),style);
            createCell(row,columnCount++,	contingentes.getNombreAval(),style);
            createCell(row,columnCount++,	contingentes.getCuentaContable60(),style);
            createCell(row,columnCount++,	contingentes.getTipoMoneda(),style);
            createCell(row,columnCount++,	contingentes.getCodicons(),style);
        }
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportPlano(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Plano Reclasificación");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row,	0, "Contrato", style);
        createCell(row,1, "Cuenta Anterior", style);
        createCell(row,2,   "Cuenta Nueva", style);
        createCell(row,	3, "Empresa", style);
        createCell(row,	4, "Periodo", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);
        style.setFont(font);
        for (planoreclasificacion contingentes : planoreclasificacionList){
            row = sheet.createRow(rowCount++);
            int columnCount = 0;

            columnCount = 0;

            createCell(row,columnCount++,	contingentes.getContrato(),style);
            createCell(row,columnCount++,	contingentes.getCtaanterior(),style);
            createCell(row,columnCount++,	contingentes.getCtanueva(),style);
            createCell(row,columnCount++,	contingentes.getEmpresa(),style);
            createCell(row,columnCount++,	contingentes.getPeriodo().toString(),style);
        }
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportIntergrupo(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Contingentes_Intergrupo");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        createCell(row,	0, "Cod Neocon", style);
        createCell(row,1, "Divisa", style);
        createCell(row,2,   "YNTP", style);
        createCell(row,	3, "Sociedad YNTP", style);
        createCell(row,	4, "Contrato", style);
        createCell(row,	5, "NIT", style);
        createCell(row,	6, "Valor", style);
        createCell(row,	7, "Cod País", style);
        createCell(row,	8, "País", style);
        createCell(row,	9, "Cuenta Local", style);
        createCell(row,	10, "Periodo", style);
        createCell(row,	11, "Fuente", style);
        createCell(row,	12, "Yntp Empresa Reportante", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);
        style.setFont(font);
        for (ContingentIntergroup contingentes : contingentIntergroupList){
            row = sheet.createRow(rowCount++);
            int columnCount = 0;

            columnCount = 0;

            createCell(row,columnCount++,	contingentes.getCodNeocon(),style);
            createCell(row,columnCount++,	contingentes.getDivisa(),style);
            createCell(row,columnCount++,	contingentes.getYntp(),style);
            createCell(row,columnCount++,	contingentes.getSociedad(),style);
            createCell(row,columnCount++,	contingentes.getContrato(),style);
            createCell(row,columnCount++,	contingentes.getNit(),style);
            createCell(row,columnCount++,	contingentes.getValor().doubleValue(),style1);
            createCell(row,columnCount++,	contingentes.getCodPais(),style);
            createCell(row,columnCount++,	contingentes.getPais(),style);
            createCell(row,columnCount++,	contingentes.getCuentaLocal(),style);
            createCell(row,columnCount++,	contingentes.getPeriodo(),style);
            createCell(row,columnCount++,	"CONTINGENTES",style);
            createCell(row,columnCount++,	"00548",style);
        }
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.flush();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportCorep(HttpServletResponse response,List<Object[]> corepListT,List<Object[]> corepListT2) throws IOException
    {
        sheet = workbook.createSheet("Plano COREP");
        sheet1 = workbook.createSheet("H141MES");
        sheet2 = workbook.createSheet("H141MES (6295)");
        Row row = sheet.createRow(0);
        Row row1 = sheet1.createRow(0);
        Row row2 = sheet2.createRow(0);

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy;@"));

        createCell(row,	0, "CTA_CONTA", style);
        createCell(row,1, "CTA_CONTA_MONEDA", style);
        createCell(row,2,   "MONEDA", style);
        createCell(row,	3, "SALDODIVISA", style);
        createCell(row,	4, "SALDOPESOS", style);
        createCell(row,	5, "FEC_ALTA", style);
        createCell(row,	6, "FEC_VENCI", style);
        createCell(row,	7, "FEC_CIERRE", style);
        createCell(row,	8, "NIT", style);
        createCell(row,	9, "CONTRATO", style);
        createCell(row,	10, "NOMBRE_CLIENTE", style);
        createCell(row,	11, "NOMBRE_BANCO", style);
        createCell(row,	12, "PAIS_BANCO", style);
        createCell(row,	13, "INTERGRUPO", style);
        createCell(row,	14, "TCLIENTE", style);
        createCell(row,	15, "Vencimiento_Rel", style);
        createCell(row,	16, "Vencimiento_Recidul", style);
        createCell(row,	17, "Vto_Rel", style);
        createCell(row,	18, "Vto_Residul", style);
        createCell(row,	19, "CCF", style);
        createCell(row,	20, "RW", style);
        createCell(row,	21, "CODCONSOL", style);
        createCell(row,	22, "Tipo_Avl", style);
        createCell(row,	23, "Nuevo Código", style);
        createCell(row,	24, "Provision", style);
        createCell(row,	25, "CUENTAPROVISION", style);
        createCell(row,	26, "ISO", style);
        createCell(row,	27, "ISO_GARANTIA", style);
        //createCell(row,	28, "SEGMENTO FINREP", style);

        createCell(row1,0, "EMPRESA", style);
        createCell(row1,1, "FECHA", style);
        createCell(row1,2,   "CENTRO", style);
        createCell(row1,3,   "NUCTA", style);
        createCell(row1,4,   "DATO1", style);
        createCell(row1,5,   "FECHA PROCESO", style);
        createCell(row1,6,   "CONTRATO", style);
        createCell(row1,7,   "APLICATIVO", style);
        createCell(row1,8,   "RISTRA", style);
        createCell(row1,9,   "VALOR", style);

        createCell(row2,0, "EMPRESA", style);
        createCell(row2,1, "FECHA", style);
        createCell(row2,2,   "CENTRO", style);
        createCell(row2,3,   "NUCTA", style);
        createCell(row2,4,   "DATO1", style);
        createCell(row2,5,   "FECHA PROCESO", style);
        createCell(row2,6,   "CONTRATO", style);
        createCell(row2,7,   "APLICATIVO", style);
        createCell(row2,8,   "RISTRA", style);
        createCell(row2,9,   "VALOR", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);
        style.setFont(font);
        for (Corep contingentes : corepList){
            row = sheet.createRow(rowCount++);
            int columnCount = 0;

            columnCount = 0;

            createCell(row,columnCount++,	contingentes.getCuenta(),style);
            createCell(row,columnCount++,	contingentes.getCtaContaMoneda(),style);
            createCell(row,columnCount++,	contingentes.getDivisa(),style);
            createCell(row,columnCount++,	contingentes.getSaldoDivisa(),style1);
            createCell(row,columnCount++,	contingentes.getSaldoPesos(),style1);
            if(contingentes.getFechaAlta()!=null)
            {
                createCell(row,columnCount++,	contingentes.getFechaAlta().toString().replace(" 00:00:00.0","").replace("-","/"),style2);
            }
            else
            {
                createCell(row,columnCount++,	"",style);
            }
            if(contingentes.getFechaVencimiento()!=null)
            {
                createCell(row,columnCount++,	contingentes.getFechaVencimiento().toString().replace(" 00:00:00.0","").replace("-","/"),style2);
            }
            else
            {
                createCell(row,columnCount++,	"",style);
            }
            if(contingentes.getFechaCierre()!=null)
            {
                createCell(row,columnCount++,	contingentes.getFechaCierre().toString().replace(" 00:00:00.0","").replace("-","/"),style2);
            }
            else
            {
                createCell(row,columnCount++,	"",style);
            }
            createCell(row,columnCount++,	contingentes.getNit(),style);
            createCell(row,columnCount++,	contingentes.getContrato(),style);
            createCell(row,columnCount++,	contingentes.getNombreCliente(),style);
            createCell(row,columnCount++,	contingentes.getNombreBanco(),style);
            if(contingentes.getPaisBanco()!=null)
                createCell(row,columnCount++,	contingentes.getPaisBanco(),style);
            else
                createCell(row,columnCount++,	"",style);
            createCell(row,columnCount++,	contingentes.getIntergrupo(),style);
            if(contingentes.getTCliente()!=null)
                createCell(row,columnCount++,	contingentes.getTCliente().toString(),style);
            else
                createCell(row,columnCount++,	"",style);
            createCell(row,columnCount++,	contingentes.getVencimientoRel(),style);
            createCell(row,columnCount++,	contingentes.getVencimientoResidul(),style);
            createCell(row,columnCount++,	contingentes.getVtoRel(),style);
            createCell(row,columnCount++,	contingentes.getVto_Residul(),style);
            createCell(row,columnCount++,	contingentes.getCcf(),style);
            createCell(row,columnCount++,	contingentes.getRw(),style);
            createCell(row,columnCount++,	contingentes.getCodconsol(),style);
            createCell(row,columnCount++,	contingentes.getTipoAvl(),style);
            createCell(row,columnCount++,	contingentes.getNuevoCodigo(),style);
            createCell(row,columnCount++,	contingentes.getProvision(),style1);
            createCell(row,columnCount++,	contingentes.getCuentaProvision(),style);
            createCell(row,columnCount++,	contingentes.getIso(),style);
            createCell(row,columnCount++,	contingentes.getIsoGarantia(),style);

        }

        rowCount = 1;

        for (Object[] contingentes : corepListT) {
            row = sheet1.createRow(rowCount++);
            int columnCount = 0;

            if(contingentes[0]!=null)
            createCell(row, columnCount++, contingentes[0].toString(), style);
            if(contingentes[1]!=null)
            createCell(row, columnCount++, contingentes[1].toString(), style);
            if(contingentes[2]!=null)
            createCell(row, columnCount++, contingentes[2].toString(), style);
            if(contingentes[3]!=null)
            createCell(row, columnCount++, contingentes[3].toString(), style);
            if(contingentes[4]!=null)
            createCell(row, columnCount++, contingentes[4].toString(), style);
            if(contingentes[5]!=null)
            createCell(row, columnCount++, contingentes[5].toString(), style);
            if(contingentes[6]!=null)
            createCell(row, columnCount++, contingentes[6].toString(), style);
            if(contingentes[7]!=null)
            createCell(row, columnCount++, contingentes[7].toString(), style);
            if(contingentes[8]!=null)
            createCell(row, columnCount++, contingentes[8].toString(), style);
            if(contingentes[9]!=null)
            createCell(row, columnCount++, Double.parseDouble(contingentes[9].toString()), style1);

        }

        rowCount = 1;

        for (Object[] contingentes : corepListT2) {
            row = sheet2.createRow(rowCount++);
            int columnCount = 0;

            if(contingentes[0]!=null)
                createCell(row, columnCount++, contingentes[0].toString(), style);
            if(contingentes[1]!=null)
                createCell(row, columnCount++, contingentes[1].toString(), style);
            if(contingentes[2]!=null)
                createCell(row, columnCount++, contingentes[2].toString(), style);
            if(contingentes[3]!=null)
                createCell(row, columnCount++, contingentes[3].toString(), style);
            if(contingentes[4]!=null)
                createCell(row, columnCount++, contingentes[4].toString(), style);
            if(contingentes[5]!=null)
                createCell(row, columnCount++, contingentes[5].toString(), style);
            if(contingentes[6]!=null)
                createCell(row, columnCount++, contingentes[6].toString(), style);
            if(contingentes[7]!=null)
                createCell(row, columnCount++, contingentes[7].toString(), style);
            if(contingentes[8]!=null)
                createCell(row, columnCount++, contingentes[8].toString(), style);
            if(contingentes[9]!=null)
                createCell(row, columnCount++, Double.parseDouble(contingentes[9].toString()), style1);

        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportReclasificacionCom(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Tablon");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        createCell(row,0, "CUENTA_CONTABLE", style);
        createCell(row,	1, "DIVISA", style);
        createCell(row,	2, "TASA", style);
        createCell(row,	3, "SALDO_DIVISA", style);
        createCell(row,	4, "SALDO_PESOS", style);
        createCell(row,	5, "F_ALTA", style);
        createCell(row,	6, "F_VCTO", style);
        createCell(row,	7, "F_CIERRE", style);
        createCell(row,	8, "NIT", style);
        createCell(row,	9, "CONTRATO", style);
        createCell(row,	10, "NOMBRE_CLIENTE", style);
        createCell(row,	11, "NIT BANCO GARANTE", style);
        createCell(row,	12, "NOMBRE_BANCO", style);
        createCell(row,	13, "PAIS_BANCO", style);
        createCell(row,	14, "INTERGRUPO", style);
        createCell(row,	15, "YNTP", style);
        createCell(row,	16, "TIPO_AVAL ", style);
        createCell(row,	17, "TIPO_AVAL_ORIGEN", style);
        createCell(row,	18, "CTA_CONTABLE_60", style);
        createCell(row,	19, "TIPO MONEDA", style);
        createCell(row,	20, "CODCONSOL", style);
        createCell(row,	21, "ESTADO", style);
        createCell(row,	22, "TP", style);
        createCell(row,	23, "DV", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);
        style.setFont(font);
        for (ReclassificationContingentCom contingentes : contingentTemplateListObjectCom)
        {
            row = sheet.createRow(rowCount++);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            int columnCount = 0;

            columnCount = 0;

            createCell(row,columnCount++,	contingentes.getCuentaContable(),style);
            createCell(row,columnCount++,	contingentes.getDivisa(),style);
            createCell(row,columnCount++,	contingentes.getVrDiv(),style1);
            createCell(row,columnCount++,	contingentes.getSaldoDivisa(),style1);
            createCell(row,columnCount++,	contingentes.getSaldoPesos(),style1);
            if(contingentes.getFechaAlta()!=null) {
                createCell(row, columnCount++, format.format(contingentes.getFechaAlta()), style);
            }
            else{
                createCell(row,columnCount++,	"",style);
            }
            if(contingentes.getFechaVenciemiento()!=null) {
                createCell(row, columnCount++, format.format(contingentes.getFechaVenciemiento()), style);
            }
            else{
                createCell(row,columnCount++,	"",style);
            }
            if(contingentes.getFechaCierre()!=null) {
                createCell(row, columnCount++, format.format(contingentes.getFechaCierre()), style);
            }
            else{
                createCell(row,columnCount++,	"",style);
            }
            createCell(row,columnCount++,	contingentes.getNit(),style);
            createCell(row,columnCount++,	contingentes.getContrato(),style);
            createCell(row,columnCount++,	contingentes.getNombreCliente(),style);
            createCell(row, columnCount++, contingentes.getNitBanco(), style);
            createCell(row,columnCount++,	contingentes.getNombreBanco(),style);
            createCell(row,columnCount++,	contingentes.getNombrePais(),style);
            createCell(row,columnCount++,	contingentes.getIntergrupo(),style);
            createCell(row,columnCount++,	contingentes.getYntp(),style);
            createCell(row,columnCount++,	contingentes.getTipoAval(),style);
            createCell(row,columnCount++,	contingentes.getNombreAval(),style);
            createCell(row,columnCount++,	contingentes.getCuentaContable60(),style);
            createCell(row,columnCount++,	contingentes.getTipoMoneda(),style);
            createCell(row,columnCount++,	contingentes.getCodicons(),style);
            createCell(row,columnCount++,	contingentes.getEstado(),style);
            createCell(row, columnCount++, contingentes.getTd(), style);
            createCell(row, columnCount++, contingentes.getDv(), style);

        }
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
