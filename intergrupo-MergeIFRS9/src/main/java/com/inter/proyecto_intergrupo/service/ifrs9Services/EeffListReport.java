package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.bank.TaxBase;
import com.inter.proyecto_intergrupo.model.ifrs9.Eeff;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EeffListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private List<Eeff> eeffArrayList;
    private static final short size = 10;

    public EeffListReport(List<Eeff> eeffArrayList) {
        this.eeffArrayList = eeffArrayList;
        workbook = new SXSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("EEFF");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size);
        style.setFont(font);

        createCell(row, 0, "Codigo Sociedad Informante", style);
        createCell(row, 1, "Descripcion", style);
        createCell(row, 2, "Denominacion de la Cuenta", style);
        createCell(row, 3, "Tipo de Cuenta", style);
        createCell(row, 4, "Cuenta", style);
        createCell(row, 5, "Soc. IC", style);
        createCell(row, 6, "Descripcion", style);
        createCell(row, 7, "Desgloses", style);
        createCell(row, 8, "Divisa españa", style);
        createCell(row, 9, "Saldo", style);
        createCell(row, 10, "Intergrupo", style);
        createCell(row, 11, "Entrada", style);
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
        font.setFontHeightInPoints(size);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Eeff eeff: eeffArrayList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, eeff.getCodigosSocInformante(), style);
            createCell(row, columnCount++, eeff.getDescripcion(), style);
            createCell(row, columnCount++, eeff.getId(), style);
            createCell(row, columnCount++, eeff.getTipoCuenta(), style);
            createCell(row, columnCount++, eeff.getCuenta(), style);
            createCell(row, columnCount++, eeff.getSocIC(), style);
            createCell(row, columnCount++, eeff.getDescripcionIC(), style);
            createCell(row, columnCount++, eeff.getDesgloces(), style);
            createCell(row, columnCount++, eeff.getDivisaespana(), style);
            createCell(row, columnCount++, Double.parseDouble(eeff.getSaldo()), style1);
            if (eeff.getSocIC() == null || eeff.getSocIC().equals("") || eeff.getSocIC().equals(" "))
                createCell(row, columnCount++, "N", style);
            else
                createCell(row, columnCount++, "S", style);
            createCell(row,columnCount, eeff.getEntrada(),style);
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
