package com.inter.proyecto_intergrupo.service.bankServices;

import com.inter.proyecto_intergrupo.model.bank.IntergrupoSubsidiariesTemplate;
import com.inter.proyecto_intergrupo.model.bank.SubsidiariesTemplate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class SubsidiariesTemplateListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> subsidiariesList;
    private List<SubsidiariesTemplate> subsidiariesTemplateList;
    private List<IntergrupoSubsidiariesTemplate> intergrupoSubsidiariesList;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public SubsidiariesTemplateListReport(List<SubsidiariesTemplate> subsidiariesTemplateList ,List<String[]> subsidiariesList,List<IntergrupoSubsidiariesTemplate> intergrupoSubsidiariesList) {
        this.subsidiariesTemplateList = subsidiariesTemplateList;
        this.subsidiariesList = subsidiariesList;
        this.intergrupoSubsidiariesList = intergrupoSubsidiariesList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Plantilla Filiales");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "YNTP EMPRESA", style);
        createCell(row, 1, "COD NEOCON", style);
        createCell(row, 2, "DIVISA", style);
        createCell(row, 3, "YNTP", style);
        createCell(row, 4, "SOCIEDAD YNTP", style);
        createCell(row, 5, "CONTRATO", style);
        createCell(row, 6, "NIT CONTRAPARTE", style);
        createCell(row, 7, "VALOR", style);
        createCell(row, 8, "COD PAÍS", style);
        createCell(row, 9, "PAÍS", style);
        createCell(row, 10, "CUENTA LOCAL", style);
        createCell(row, 11, "OBSERVACIONES", style);
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

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(SubsidiariesTemplate sub: subsidiariesTemplateList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,sub.getYntpReportante(),style);
            createCell(row,columnCount++,sub.getCodNeocon(),style);
            createCell(row,columnCount++,sub.getDivisa(),style);
            createCell(row,columnCount++,sub.getYntp(),style);
            createCell(row,columnCount++,sub.getSociedadYntp(),style);
            createCell(row,columnCount++,sub.getCodNeocon(),style);
            createCell(row,columnCount++,sub.getNitContraparte(),style);
            createCell(row,columnCount++,sub.getValor(),style1);
            createCell(row,columnCount++,sub.getCodPais(),style);
            createCell(row,columnCount++,sub.getPais(),style);
            createCell(row,columnCount++,sub.getCuentaLocal(),style);
            createCell(row,columnCount++,sub.getObservaciones(),style);
        }
    }

    private void writeDataLinesReport(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(Object[] tax: subsidiariesList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,tax[0],style);
            createCell(row,columnCount++,tax[1].toString(),style);
            createCell(row,columnCount++,tax[2],style);
            createCell(row,columnCount++,tax[3],style);
            createCell(row,columnCount++,tax[4],style);
            createCell(row,columnCount++,tax[5],style);
            createCell(row,columnCount++,tax[6].toString(),style);
            createCell(row,columnCount++,tax[7].toString(),style);
            createCell(row,columnCount++,tax[8],style);
            createCell(row,columnCount++,tax[9],style);
            createCell(row,columnCount++,tax[10],style);
            createCell(row,columnCount++,tax[11],style);
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
        writeHeaderLine();
        writeDataLinesReport();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Plantilla");
        Row row1 = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "Fila", style);
        createCell(row1, 1, "Columna", style);
        createCell(row1, 2, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        subsidiariesList.remove(subsidiariesList.size()-1);
        for(String[] log: subsidiariesList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style);
            createCell(row,columnCount++,log[1],style);
            createCell(row,columnCount++,log[2],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportQuery(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Plantilla");
        Row row1 = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        createCell(row1, 0, "Cuenta", style);
        createCell(row1, 1, "Saldo Filial", style);
        createCell(row1, 2, "Saldo S2", style);
        createCell(row1, 3, "Mensaje", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        subsidiariesList.remove(subsidiariesList.size()-1);
        for(String[] log: subsidiariesList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            boolean resp = false;

            createCell(row,columnCount++,log[0],style);
            createCell(row,columnCount++,Double.parseDouble(log[1]),style1);
            try{
                Double.parseDouble(log[2]);
                resp = true;
            }catch (Exception e){
            }

            if(resp){
                createCell(row,columnCount++,Double.parseDouble(log[2]),style1);
            } else{
                createCell(row,columnCount++,log[2],style);
            }

            createCell(row,columnCount++,log[3],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    private void writeHeaderLineInter(){
        sheet = workbook.createSheet("Plantilla Filiales");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "YNTP EMPRESA", style);
        createCell(row, 1, "COD NEOCON", style);
        createCell(row, 2, "DIVISA", style);
        createCell(row, 3, "YNTP", style);
        createCell(row, 4, "SOCIEDAD YNTP", style);
        createCell(row, 5, "CONTRATO", style);
        createCell(row, 6, "NIT CONTRAPARTE", style);
        createCell(row, 7, "VALOR", style);
        createCell(row, 8, "COD PAÍS", style);
        createCell(row, 9, "PAÍS", style);
        createCell(row, 10, "CUENTA LOCAL", style);
        createCell(row, 11, "OBSERVACIONES", style);
        createCell(row, 12, "USUARIO", style);
    }

    private void writeDataLinesInter(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(IntergrupoSubsidiariesTemplate sub: intergrupoSubsidiariesList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,sub.getYntpReportante(),style);
            createCell(row,columnCount++,sub.getCodNeocon(),style);
            createCell(row,columnCount++,sub.getDivisa(),style);
            createCell(row,columnCount++,sub.getYntp(),style);
            createCell(row,columnCount++,sub.getSociedadYntp(),style);
            createCell(row,columnCount++,sub.getContrato(),style);
            createCell(row,columnCount++,sub.getNitContraparte(),style);
            createCell(row,columnCount++,sub.getValor(),style1);
            createCell(row,columnCount++,sub.getCodPais(),style);
            createCell(row,columnCount++,sub.getPais(),style);
            createCell(row,columnCount++,sub.getCuentaLocal(),style);
            createCell(row,columnCount++,sub.getObservaciones(),style);
            createCell(row,columnCount++,sub.getUsuario(),style);
        }
    }

    public void exportReportInter(HttpServletResponse response) throws IOException {
        writeHeaderLineInter();
        writeDataLinesInter();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public void exportCurrency(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Plantilla");
        Row row1 = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "Cuenta Local", style);
        createCell(row1, 1, "Divisa", style);
        createCell(row1, 2, "Error", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        subsidiariesList.remove(subsidiariesList.size()-1);
        for(String[] log: subsidiariesList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style);
            createCell(row,columnCount++,log[1],style);
            createCell(row,columnCount++,log[2],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
