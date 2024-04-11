package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.Manuals;
import com.inter.proyecto_intergrupo.model.information.ChangeAccountQuery;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ManualsListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private SXSSFSheet sheet1;
    private SXSSFSheet sheet2;
    private List<Manuals> manualsList;
    private List<Object[]> manualListSub;
    private List<Object[]> manualListSubString;
    private List<Object[]> manualListQuery;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ManualsListReport(List<Manuals> manualsList, List<Object[]> manualListSub, List<Object[]> manualListSubString,List<Object[]> manualListQuery){
        this.manualsList = manualsList;
        this.manualListSub=manualListSub;
        this.manualListSubString=manualListSubString;
        this.manualListQuery=manualListQuery;
        workbook = new SXSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Manuales(ANEXO8-SICC)");
        sheet1 = workbook.createSheet("Cuentas No Encontradas");
        sheet2 = workbook.createSheet("Validacion Query");
        Row row = sheet.createRow(0);
        Row row1 = sheet1.createRow(0);
        Row row2 = sheet2.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        short num=11;
        font.setBold(true);
        font.setFontHeightInPoints(num);
        style.setFont(font);

        createCell(row, 0, "CENTRO", style);
        createCell(row, 1, "DESCRIPCIÓN CENTRO", style);
        createCell(row, 2, "CUENTA PUC", style);
        createCell(row, 3, "DESCRIPCIÓN CUENTA PUC", style);
        createCell(row, 4, "DIVISA", style);
        createCell(row, 5, "IMPORTE", style);
        createCell(row, 6, "FECHA ORIGEN", style);
        createCell(row, 7, "FECHA CIERRE", style);
        createCell(row, 8, "TP", style);
        createCell(row, 9, "IDENTIFICACIÓN", style);
        createCell(row, 10, "DV", style);
        createCell(row, 11, "NOMBRE", style);
        createCell(row, 12, "CONTRATO", style);
        createCell(row, 13, "OBSERVACION", style);
        createCell(row, 14, "CUENTA PROV", style);
        createCell(row, 15, "IMPORTE PROV", style);
        createCell(row, 16, "IMPORTE ORIGINAL", style);
        createCell(row, 17, "PROBABILIDAD RECUPERACION", style);
        createCell(row, 18, "ALTURA", style);
        createCell(row, 19, "FUENTE DE INFORMACIÓN", style);
        createCell(row, 20, "DESCRIPCIÓN PROVISIONES", style);
        createCell(row1, 0, "CUENTA", style);
        createCell(row1, 1, "SALDO QUERY", style);
        createCell(row2, 0, "CUENTA", style);
        createCell(row2, 1, "CÓDIGO CONSOLIDACIÓN", style);
        createCell(row2, 2, "DIVISA", style);
        createCell(row2, 3, "SALDO MANUALES", style);
        createCell(row2, 4, "SALDO QUERY", style);
        createCell(row2, 5, "DIFERENCIA", style);
        createCell(row2, 6, "FECHA PROCESO", style);

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
        CellStyle style1 = workbook.createCellStyle();
        Font font = workbook.createFont();
        short num=10;
        font.setFontHeightInPoints(num);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Manuals manuals: manualsList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,manuals.getCentro(),style);
            createCell(row,columnCount++,manuals.getDescripcionCentro(),style);
            createCell(row,columnCount++,manuals.getCuentaPuc(),style);
            createCell(row,columnCount++,manuals.getDescripcionCuentaPuc(),style);
            createCell(row,columnCount++,manuals.getDivisa(),style);
            createCell(row,columnCount++,manuals.getImporte(),style1);
            createCell(row,columnCount++,manuals.getFechaOrigen(),style);
            createCell(row,columnCount++,manuals.getFechaCierre(),style);
            createCell(row,columnCount++,manuals.getTp(),style);
            createCell(row,columnCount++,manuals.getIdentificacion(),style);
            createCell(row,columnCount++,manuals.getDv(),style);
            createCell(row,columnCount++,manuals.getNombre(),style);
            createCell(row,columnCount++,manuals.getContrato(),style);
            createCell(row,columnCount++,manuals.getObservacion(),style);
            createCell(row,columnCount++,manuals.getCuentaProv(),style);
            createCell(row,columnCount++,manuals.getImporteProv(),style);
            createCell(row,columnCount++,manuals.getImporteOriginal(),style);
            createCell(row,columnCount++,manuals.getProbabilidadRecuperacion(),style);
            createCell(row,columnCount++,manuals.getAltura(),style);
            createCell(row,columnCount++,manuals.getFuenteInformacion(),style);
            createCell(row,columnCount++,manuals.getDescripcionProvisiones(),style);
        }

        rowCount = 1;
        for(Object[] manuals: manualListSubString) {
            Row row = sheet1.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, manuals[0], style);
            createCell(row, columnCount++, Double.parseDouble(manuals[1].toString()), style1);
        }

        rowCount = 1;
        for(Object[] manuals: manualListQuery) {
            Row row = sheet2.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, manuals[0], style);
            createCell(row, columnCount++, manuals[1], style);
            createCell(row, columnCount++, manuals[2], style);
            createCell(row, columnCount++, Double.parseDouble(manuals[3].toString()), style1);
            createCell(row, columnCount++, Double.parseDouble(manuals[4].toString()), style1);
            createCell(row, columnCount++, Double.parseDouble(manuals[5].toString()), style1);
            if(manuals[6]!=null)
                createCell(row, columnCount++, manuals[6].toString().replace(" 00:00:00.0",""), style);
        }
    }

    private void writeDataLinesExtra(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        Font font = workbook.createFont();
        short num=10;
        font.setFontHeightInPoints(num);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] manuals: manualListSub){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,manuals[0],style);
            createCell(row,columnCount++,manuals[1],style);
            createCell(row,columnCount++,manuals[2],style);
            createCell(row,columnCount++,manuals[3],style);
            createCell(row,columnCount++,manuals[4],style);
            createCell(row,columnCount++,manuals[5],style1);
            createCell(row,columnCount++,manuals[6],style);
            createCell(row,columnCount++,manuals[7],style);
            createCell(row,columnCount++,manuals[8],style);
            createCell(row,columnCount++,manuals[9],style);
            createCell(row,columnCount++,manuals[10],style);
            createCell(row,columnCount++,manuals[11],style);
            createCell(row,columnCount++,manuals[12],style);
            createCell(row,columnCount++,manuals[13],style);
            createCell(row,columnCount++,manuals[14],style);
            createCell(row,columnCount++,manuals[15],style);
            createCell(row,columnCount++,manuals[16],style);
            createCell(row,columnCount++,manuals[17],style);
            createCell(row,columnCount++,manuals[18],style);
            createCell(row,columnCount++,manuals[19],style);
            createCell(row,columnCount++,manuals[20],style);
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
        writeDataLinesExtra();

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
        Font font = workbook.createFont();
        short num=10;
        short num1=10;
        font.setBold(true);
        font.setFontHeightInPoints(num1);
        style.setFont(font);

        createCell(row1, 0, "NIT Contraparte", style);
        createCell(row1, 1, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeightInPoints(num);

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
}
