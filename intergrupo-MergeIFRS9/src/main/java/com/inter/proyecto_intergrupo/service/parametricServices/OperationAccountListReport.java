package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.OperationAccount;
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

public class OperationAccountListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<OperationAccount> operationAccountList;
    private UserService userService;

    public OperationAccountListReport(List<OperationAccount> operationAccountList, UserService userService){
        this.operationAccountList = operationAccountList;
        this.userService = userService;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(boolean operation){
        sheet = workbook.createSheet("Operacion_Riesgo");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(14);
        style.setFont(font);

        createCell(row, 0, "Cuenta Local", style);
        createCell(row, 1, "Tipo Operación", style);
        createCell(row, 2, "Tipo Riesgo", style);

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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for(OperationAccount operationAccount: operationAccountList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, operationAccount.getCuentaLocal(), style);
            createCell(row, columnCount++, operationAccount.getOperacion(), style);
            createCell(row, columnCount++, operationAccount.getRiesgo(), style);

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
        font.setFontHeight(14);
        style.setFont(font);

        createCell(row1, 0, "Cuenta Local", style);
        createCell(row1, 1, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(13);

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
