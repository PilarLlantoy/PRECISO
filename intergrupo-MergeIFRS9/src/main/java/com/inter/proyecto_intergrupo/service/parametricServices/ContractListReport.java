package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.Contract;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContractListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;
    private XSSFSheet sheet2;
    private XSSFSheet sheet3;
    private List<Contract> contractList;
    private List<Object[]> contractListObject;
    private List<Object[]> bankList;
    private List<GarantBank> bancoList;
    private List<Country> countryList;

    public ContractListReport(List<Contract> contractList,List<Object[]> contractListObject,List<Object[]> bankList, List<GarantBank> bancoList, List<Country> countryList){
        this.bankList = bankList;
        this.contractList = contractList;
        this.bancoList = bancoList;
        this.countryList = countryList;
        this.workbook = new XSSFWorkbook();
        this.contractListObject = contractListObject;
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
        createCell(row, 1, "Origen Contrato", style);
        createCell(row, 2, "Id Banco/Nit Banco Garante", style);
        createCell(row, 3, "Tipo Aval", style);
        createCell(row, 4, "Tipo Proceso", style);
        createCell(row, 5, "Tipo Aval Origen", style);
        createCell(row, 6, "Id País", style);

        sheet1 = workbook.createSheet("Lista Bancos Yntp");
        Row row1 = sheet1.createRow(0);
        createCell(row1, 0, "Yntp", style);
        createCell(row1, 1, "Banco Sociedad Corta", style);

        sheet2 = workbook.createSheet("Lista Bancos Garante");
        Row row2 = sheet2.createRow(0);
        createCell(row2, 0, "NIT", style);
        createCell(row2, 1, "Banco Nombre Similar", style);

        sheet3 = workbook.createSheet("Lista Países");
        Row row3 = sheet3.createRow(0);
        createCell(row3, 0, "Id País", style);
        createCell(row3, 1, "Nombre País", style);

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


        for(Contract contract: contractList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,contract.getContrato(),style);
            createCell(row,columnCount++,contract.getArchivoEntrada(),style);
            createCell(row,columnCount++,contract.getBanco(),style);
            createCell(row,columnCount++,contract.getTipoAval(),style);
            createCell(row,columnCount++,contract.getTipoProceso(),style);
            createCell(row,columnCount++,contract.getTipoAvalOrigen(),style);
            if(contract.getPaisContrato()!=null)
            {
                createCell(row,columnCount++,contract.getPaisContrato().getId(),style);
            }
            else
            {
                createCell(row,columnCount++,"",style);
            }
        }
    }

    private void writeDataLinesF(){
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


        for(Object[] contract: contractListObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,contract[0],style);
            createCell(row,columnCount++,contract[1],style);
            createCell(row,columnCount++,contract[2],style);
            createCell(row,columnCount++,contract[3],style);
            createCell(row,columnCount++,contract[4],style);
            createCell(row,columnCount++,contract[5],style);
            if(contract[6]!=null)
            {
                createCell(row,columnCount++,contract[6],style);
            }
            else
            {
                createCell(row,columnCount++,"",style);
            }
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

        rowCount = 1;

        for(Country country: countryList){
            Row row = sheet3.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,country.getId() ,style);
            createCell(row,columnCount++,country.getNombre() ,style);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        if(contractListObject==null||contractListObject.size()==0)
        {
            writeDataLines();
            writeDataLinesBank();
        }
        else{
            writeDataLinesF();
            writeDataLinesBank();
        }

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
        createCell(row, 0, "Código Contrato", style);
        createCell(row, 1, "Estado", style);

        /*sheet1 = workbook.createSheet("Lista Bancos Yntp");
        Row row1 = sheet1.createRow(0);
        createCell(row1, 0, "Yntp", style);
        createCell(row1, 1, "Banco Sociedad Corta", style);*/

        sheet2 = workbook.createSheet("Lista Bancos Garante");
        Row row2 = sheet2.createRow(0);
        createCell(row2, 0, "NIT", style);
        createCell(row2, 1, "Banco Nombre Similar", style);

        sheet3 = workbook.createSheet("Lista Países");
        Row row3 = sheet3.createRow(0);
        createCell(row3, 0, "Id País", style);
        createCell(row3, 1, "Nombre País", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        for(String[] currency: lista){
            Row rowP = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++,currency[0],style);
            createCell(rowP,columnCount++,currency[1],style);
        }

        /*rowCount = 1;

        for(Object[] yntpSociety: bankList){
            Row rowP = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++,yntpSociety[0],style);
            createCell(rowP,columnCount++,yntpSociety[1],style);
        }*/

        rowCount = 1;

        for(GarantBank garantBank: bancoList){
            Row rowP = sheet2.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++,garantBank.getNit(),style);
            createCell(rowP,columnCount++,garantBank.getNombreSimilar(),style);
        }

        rowCount = 1;

        for(Country country: countryList){
            Row rowP = sheet3.createRow(rowCount++);
            int columnCount = 0;

            createCell(rowP,columnCount++, country.getId(),style);
            createCell(rowP,columnCount++,country.getNombre() ,style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
