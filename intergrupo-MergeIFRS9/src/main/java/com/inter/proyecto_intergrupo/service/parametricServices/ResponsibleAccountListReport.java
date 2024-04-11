package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.ResponsibleAccount;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
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

public class ResponsibleAccountListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ResponsibleAccount> responsibleAccountList;
    private UserService userService;

    public ResponsibleAccountListReport(List<ResponsibleAccount> responsibleAccountList,UserService userService){
        this.responsibleAccountList = responsibleAccountList;
        this.userService = userService;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(boolean operation){
        sheet = workbook.createSheet("Cuenta Responsable");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Cuenta Local", style);
        createCell(row, 1, "Centro", style);
        createCell(row, 2, "Input", style);
        createCell(row, 3, "Componente", style);
        createCell(row, 4, "Aplica SICC", style);
        createCell(row, 5, "Aplica Base Fiscal", style);
        createCell(row, 6, "Aplica Metodología", style);
        createCell(row, 7, "Aplica Mis", style);

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

    private void writeDataLines(boolean operation){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("0"));

        for(ResponsibleAccount responsibleAccount: responsibleAccountList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            List list =userService.findUserByCentroAccount(responsibleAccount.getCuentaLocal());
            createCell(row, columnCount++, responsibleAccount.getCuentaLocal().toString(), style);
            if(list.size()>0)
            {
                createCell(row, columnCount++, String.valueOf(list.get(0)), style);
            }
            else
            {
                createCell(row, columnCount++, responsibleAccount.getCentro(), style);
            }
            createCell(row, columnCount++, responsibleAccount.getEntrada(), style);
            createCell(row, columnCount++, responsibleAccount.getComponente(), style);
            createCell(row, columnCount++, responsibleAccount.getSicc(), style);
            createCell(row, columnCount++, responsibleAccount.getBaseFiscal(), style);
            createCell(row, columnCount++, responsibleAccount.getMetodologia(), style);
            createCell(row, columnCount++, responsibleAccount.getMis(), style);

        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine(false);
        writeDataLines(false);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLog(HttpServletResponse response, ArrayList<String[]> lista) throws IOException
    {
        sheet = workbook.createSheet("Log");
        Row row1 = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "Cuenta Local", style);
        createCell(row1, 1, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        for(String[] currency: lista){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,currency[0],style);
            createCell(row,columnCount++,currency[1],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportOperation(HttpServletResponse response) throws IOException {
        writeHeaderLine(true);
        writeDataLines(true);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
