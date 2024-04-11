package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.parametric.Contract;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContingentesConListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;
    private XSSFSheet sheet2;
    private List<Object[]> contractList;
    private List<Object[]> bankList;
    private List<GarantBank> bancoList;

    public ContingentesConListReport(List<Object[]> contractList, List<Object[]> bankList, List<GarantBank> bancoList){
        this.bankList = bankList;
        this.contractList = contractList;
        this.bancoList = bancoList;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Contrato");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Contrato", style);
        createCell(row, 1, "Id Banco/Nit Banco Garante", style);

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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        /*DataValidation dataValidation = null;
        DataValidationConstraint constraint = null;
        DataValidationHelper validationHelper = null;

        validationHelper=new XSSFDataValidationHelper(sheet);
        CellRangeAddressList addressList = new  CellRangeAddressList(1,contractList.size(),2,2);
        constraint =validationHelper.createExplicitListConstraint(bankListSelect);
        dataValidation = validationHelper.createValidation(constraint, addressList);
        dataValidation.setSuppressDropDownArrow(true);
        sheet.addValidationData(dataValidation);*/


        for(Object[] contract: contractList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,contract[0],style);
            createCell(row,columnCount++,contract[1],style);

        }
    }

    private void writeDataLinesBank(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(Object[] yntpSociety: bankList){
            Row row = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,yntpSociety[0],style);
            createCell(row,columnCount++,yntpSociety[1],style);
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
        writeDataLinesBank();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLog(HttpServletResponse response, ArrayList<String[]> lista) throws IOException
    {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        sheet = workbook.createSheet("Log");
        Row row = sheet.createRow(0);
        createCell(row, 0, "Fila", style);
        createCell(row, 1, "Columna", style);
        createCell(row, 2, "Estado", style);

        sheet1 = workbook.createSheet("Lista Bancos Yntp");
        Row row1 = sheet1.createRow(0);
        createCell(row1, 0, "Yntp", style);
        createCell(row1, 1, "Banco Sociedad Corta", style);

        sheet2 = workbook.createSheet("Lista Bancos Garante");
        Row row2 = sheet2.createRow(0);
        createCell(row2, 0, "NIT", style);
        createCell(row2, 1, "Banco Nombre Similar", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        for(String[] currency: lista){
            Row rowP = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++,currency[0],style);
            createCell(rowP,columnCount++,currency[1],style);
            createCell(rowP,columnCount++,currency[2],style);
        }

        rowCount = 1;

        for(Object[] yntpSociety: bankList){
            Row rowP = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++,yntpSociety[0],style);
            createCell(rowP,columnCount++,yntpSociety[1],style);
        }

        rowCount = 1;

        for(GarantBank garantBank: bancoList){
            Row rowP = sheet2.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++,garantBank.getNit(),style);
            createCell(rowP,columnCount++,garantBank.getNombreSimilar(),style);
        }


        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
