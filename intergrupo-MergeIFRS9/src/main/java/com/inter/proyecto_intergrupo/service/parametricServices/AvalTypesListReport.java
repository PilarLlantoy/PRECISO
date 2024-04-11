package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AvalTypes;
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

public class AvalTypesListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<AvalTypes> avalList;

    public AvalTypesListReport(List<AvalTypes> avalList){
        this.avalList = avalList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Tipo Aval");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Tipo Aval Origen", style);
        createCell(row, 1, "Tipo de aval", style);
        createCell(row, 2, "Cuenta Contable 13", style);
        createCell(row, 3, "Cuenta Contable 60", style);
        createCell(row, 4, "Tipo de Archivo", style);
        createCell(row, 5, "Contrapartida Genérica", style);
        Comment comment = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment.setString(new XSSFRichTextString("Especificaciones Campo Tipo de Aval Origen: \n*Longitud Maxima:255 \n*Longitud Minima:1 \n*Tipo dato: Alfabetico"));
        row.getCell(0).setCellComment(comment);

        Comment comment1 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment1.setString(new XSSFRichTextString("Especificaciones Campo Tipo de Aval: \n*Longitud Maxima:1 \n*Longitud Minima:1 \n*Tipo dato: Numérico"));
        row.getCell(1).setCellComment(comment1);

        Comment comment2 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment2.setString(new XSSFRichTextString("Especificaciones Campo Cuenta Contable 13: \n*Longitud Maxima:18 \n*Longitud Minima:4 \n*Tipo dato: Númerico"));
        row.getCell(2).setCellComment(comment2);

        Comment comment3 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment3.setString(new XSSFRichTextString("Especificaciones Campo Cuenta Contable 60: \n*Longitud Maxima:18 \n*Longitud Minima:4 \n*Tipo dato: Númerico"));
        row.getCell(3).setCellComment(comment3);

        Comment comment4 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment4.setString(new XSSFRichTextString("Especificaciones Campo Tipo Archivo: \n*Longitud Maxima:255 \n*Longitud Minima:1 \n*Tipo dato: Alfanumérico"));
        row.getCell(4).setCellComment(comment4);

        Comment comment5 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment5.setString(new XSSFRichTextString("Especificaciones Campo Contrapartida Genérica: \n*Longitud Maxima:14 \n*Longitud Minima:4 \n*Tipo dato: Númerico"));
        row.getCell(5).setCellComment(comment5);

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

        for(AvalTypes avalTypes : avalList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,avalTypes.getAvalOrigen(),style);
            createCell(row,columnCount++,avalTypes.getTipoAval(),style);
            createCell(row,columnCount++,avalTypes.getCuentaContable13(),style);
            createCell(row,columnCount++,avalTypes.getCuentaContable60(),style);
            createCell(row,columnCount++,avalTypes.getTipoArchivo(),style);
            createCell(row,columnCount++,avalTypes.getContraGenerica(),style);
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

        createCell(row1, 0, "Tipo Aval", style);
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
