package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.GenericAccount;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenericAccountListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<GenericAccount> genericAccountList;

    public GenericAccountListReport(List<GenericAccount> genericAccountList){
        this.genericAccountList = genericAccountList;
        workbook = new XSSFWorkbook();
    }

    //TODO
    private void writeHeaderLine(){
        sheet = workbook.createSheet("CuentasGenericas");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Cuenta", style);
        createCell(row, 1, "Empresa", style);
        createCell(row, 2, "Codigo neocon", style);
        createCell(row, 3, "Obervaciones", style);
        createCell(row, 4, "Divisa", style);
        createCell(row, 5, "Concepto", style);
        createCell(row, 6, "Fecha", style);

        Comment comment = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment.setString(new XSSFRichTextString("Especificaciones Campo Cuenta: \n*Longitud Maxima:18 \n*Longitud Minima:4 \n*Tipo dato: Alfanúmerico"));
        row.getCell(0).setCellComment(comment);

        Comment comment1 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment1.setString(new XSSFRichTextString("Especificaciones Campo Empresa: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(1).setCellComment(comment1);

        Comment comment2 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment2.setString(new XSSFRichTextString("Especificaciones Campo Codigo neocon: \n*Longitud Maxima:5 \n*Longitud Minima:5 \n*Tipo dato: Alfanúmerico"));
        row.getCell(2).setCellComment(comment2);

        Comment comment3 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment3.setString(new XSSFRichTextString("Especificaciones Campo Observaciones: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(3).setCellComment(comment3);

        Comment comment4 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment4.setString(new XSSFRichTextString("Especificaciones Campo Divisa: \n*Longitud Maxima:3 \n*Longitud Minima:3 \n*Tipo dato: Alfanúmerico"));
        row.getCell(4).setCellComment(comment4);

        Comment comment5 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment5.setString(new XSSFRichTextString("Especificaciones Campo Concepto: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(5).setCellComment(comment5);

        Comment comment6 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment6.setString(new XSSFRichTextString("Especificaciones Campo Fecha: \n*Longitud Maxima:10 \n*Longitud Minima:10 \n*Tipo dato: Fecha"));
        row.getCell(6).setCellComment(comment6);
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

        for(GenericAccount genericAccount: genericAccountList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,genericAccount.getCuenta(),style);
            createCell(row,columnCount++,genericAccount.getEmpresa(),style);
            createCell(row,columnCount++,genericAccount.getCodCons(),style);
            createCell(row,columnCount++,genericAccount.getObservaciones(),style);
            createCell(row,columnCount++,genericAccount.getDivisa(),style);
            createCell(row,columnCount++,genericAccount.getConcepto(),style);
            createCell(row,columnCount,genericAccount.getFecha(),style);
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

    public void exportLog(HttpServletResponse response, ArrayList<String[]> lista) throws IOException
    {
        sheet = workbook.createSheet("Log");
        Row row1 = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "Cuenta", style);
        createCell(row1, 1, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

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
