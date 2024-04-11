package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.Deaccount;
import com.inter.proyecto_intergrupo.model.parametric.Third;
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

public class DescontabilizacionListReport {
    private List<Deaccount> listDeaccount;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    public DescontabilizacionListReport(List<Deaccount> deaccounts){
        this.listDeaccount = deaccounts;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Diferencias");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Centro", style);
        createCell(row, 1, "Cuenta", style);
        createCell(row, 2, "Contrato", style);
        createCell(row, 3, "Valor Contable", style);
        createCell(row, 4, "Valor Aplicativo", style);
        createCell(row, 5, "Valor Diferencia", style);
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

        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Deaccount deaccount: listDeaccount){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,deaccount.getCentro(),style);
            createCell(row,columnCount++,deaccount.getCuenta(),style);
            createCell(row,columnCount++,deaccount.getContrato(),style);
            createCell(row,columnCount++,Double.parseDouble(deaccount.getValorContable().toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(deaccount.getValorAplicativo().toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(deaccount.getValorDiferencia().toString()),style1);
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
}
