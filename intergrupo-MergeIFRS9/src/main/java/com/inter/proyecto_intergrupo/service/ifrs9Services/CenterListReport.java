package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.Centers;
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

public class CenterListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Centers> centerList;

    public CenterListReport(List<Centers> centerList){
        this.centerList = centerList;
        workbook = new XSSFWorkbook();
    }

    //TODO
    private void writeHeaderLine(){
        sheet = workbook.createSheet("CentrosDeOficinas");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Oficina", style);
        createCell(row, 1, "Tipo unidad", style);
        createCell(row, 2, "Nomtip", style);
        createCell(row, 3, "Clase unidad", style);
        createCell(row, 4, "Nombre unidad", style);
        createCell(row, 5, "Dar", style);
        createCell(row, 6, "Dug", style);
        createCell(row, 7, "Territorio", style);
        createCell(row, 8, "Direccion regional", style);
        createCell(row, 9, "Area operativa", style);
        createCell(row, 10, "Suprarea", style);
        createCell(row, 11, "Fecha cierre", style);
        createCell(row, 12, "Ofinegocio", style);
        createCell(row, 13, "Fecha apertura", style);
        createCell(row, 14, "Domicilio", style);
        createCell(row, 15, "Telefono", style);

        Comment comment1 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment1.setString(new XSSFRichTextString("Especificaciones Campo Oficina: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(0).setCellComment(comment1);

        Comment comment2 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment2.setString(new XSSFRichTextString("Especificaciones Campo Tipo unidad: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(1).setCellComment(comment2);

        Comment comment3 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment3.setString(new XSSFRichTextString("Especificaciones Campo Nomtip: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(2).setCellComment(comment3);

        Comment comment4 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment4.setString(new XSSFRichTextString("Especificaciones Campo Clase unidad: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(3).setCellComment(comment4);

        Comment comment5 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment5.setString(new XSSFRichTextString("Especificaciones Campo Nombre unidad: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(4).setCellComment(comment5);

        Comment comment6 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment6.setString(new XSSFRichTextString("Especificaciones Campo Dar: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(5).setCellComment(comment6);

        Comment comment7 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment7.setString(new XSSFRichTextString("Especificaciones Campo Dug: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(6).setCellComment(comment7);

        Comment comment8 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment8.setString(new XSSFRichTextString("Especificaciones Campo Territorio: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(7).setCellComment(comment8);

        Comment comment9 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment9.setString(new XSSFRichTextString("Especificaciones Campo Direccion regional: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(8).setCellComment(comment9);

        Comment comment10 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment10.setString(new XSSFRichTextString("Especificaciones Campo Area operativa: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(9).setCellComment(comment10);

        Comment comment11 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment11.setString(new XSSFRichTextString("Especificaciones Campo Suprarea: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(10).setCellComment(comment11);

        Comment comment12 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment12.setString(new XSSFRichTextString("Especificaciones Campo Fecha cierre: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(11).setCellComment(comment12);

        Comment comment13 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment13.setString(new XSSFRichTextString("Especificaciones Campo Ofinegocio: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(12).setCellComment(comment13);

        Comment comment14 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment14.setString(new XSSFRichTextString("Especificaciones Campo Fecha apertura: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(13).setCellComment(comment14);

        Comment comment15 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment15.setString(new XSSFRichTextString("Especificaciones Campo Domicilio: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(14).setCellComment(comment15);

        Comment comment16 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment16.setString(new XSSFRichTextString("Especificaciones Campo Telefono: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(15).setCellComment(comment16);

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

        for(Centers center: centerList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,center.getOficina(),style);
            createCell(row,columnCount++,center.getTipo_unidad(),style);
            createCell(row,columnCount++,center.getNomtip(),style);
            createCell(row,columnCount++,center.getClase_unidad(),style);
            createCell(row,columnCount++,center.getNombre_unidad(),style);
            createCell(row,columnCount++,center.getDar(),style);
            createCell(row,columnCount++,center.getDug(),style);
            createCell(row,columnCount++,center.getTerritorio(),style);
            createCell(row,columnCount++,center.getDireccion_regional(),style);
            createCell(row,columnCount++,center.getArea_operativa(),style);
            createCell(row,columnCount++,center.getSuprarea(),style);
            createCell(row,columnCount++,center.getFecha_cierre(),style);
            createCell(row,columnCount++,center.getOfinegocio(),style);
            createCell(row,columnCount++,center.getFecha_apertura(),style);
            createCell(row,columnCount++,center.getDomicilio(),style);
            createCell(row,columnCount,center.getTelefono(),style);
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

        createCell(row1, 0, "Oficina", style);
        createCell(row1, 1, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        for(String[] third: lista){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,third[0],style);
            createCell(row,columnCount,third[1],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}