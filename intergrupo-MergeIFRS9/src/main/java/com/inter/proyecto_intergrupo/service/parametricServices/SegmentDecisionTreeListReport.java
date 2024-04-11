package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.SegmentDecisionTree;
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

public class SegmentDecisionTreeListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<SegmentDecisionTree> segmentDecisionTreeList;

    public SegmentDecisionTreeListReport(List<SegmentDecisionTree> segmentDecisionTreeList){
        this.segmentDecisionTreeList = segmentDecisionTreeList;
        workbook = new XSSFWorkbook();
    }
    
    private void writeHeaderLine(){
        sheet = workbook.createSheet("ArbolDeDecisionSegmento");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Codigo IFRS9", style);
        createCell(row, 1, "Descripción Sectorización", style);
        createCell(row, 2, "Operador Corasu", style);
        createCell(row, 3, "Corasu", style);
        createCell(row, 4, "Operador SubCorasu", style);
        createCell(row, 5, "SubCorasu", style);
        createCell(row, 6, "Operador CIIU", style);
        createCell(row, 7, "CIIU", style);
        createCell(row, 8, "Operador Número de empleados (N/A)", style);
        createCell(row, 9, "Número de empleados (N/A)", style);
        createCell(row, 10, "Operador Total Activos", style);
        createCell(row, 11, "Total Activos", style);
        createCell(row, 12, "Operador Total Ventas", style);
        createCell(row, 13, "Total Ventas", style);
        createCell(row, 14, "Verificación Contratos", style);
        createCell(row, 15, "Otros criterios", style);


        Comment comment = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment.setString(new XSSFRichTextString("Especificaciones Campo Codigo IFRS9: \n*Longitud Maxima:3 \n*Longitud Minima:3 \n*Tipo dato: Alfanúmerico"));
        row.getCell(0).setCellComment(comment);

        Comment comment1 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment1.setString(new XSSFRichTextString("Especificaciones Campo Descripción Sectorización: \n*Longitud Maxima:50 \n*Longitud Minima:50 \n*Tipo dato: Alfanúmerico"));
        row.getCell(1).setCellComment(comment1);

        Comment comment2 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment2.setString(new XSSFRichTextString("Especificaciones Campo Operador Corasu: \n*Lista valores: \"=\",\"<>\",\">\",\"<\",\"IN\",\"NOT IN\""));
        row.getCell(2).setCellComment(comment2);

        Comment comment3 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment3.setString(new XSSFRichTextString("Especificaciones Campo Corasu: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(3).setCellComment(comment3);

        Comment comment4 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment4.setString(new XSSFRichTextString("Especificaciones Campo Operador SubCorasu: \n*Lista valores: \"=\",\"<>\",\">\",\"<\",\"IN\",\"NOT IN\""));
        row.getCell(4).setCellComment(comment4);

        Comment comment5 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment5.setString(new XSSFRichTextString("Especificaciones Campo SubCorasu: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(5).setCellComment(comment5);

        Comment comment6 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment6.setString(new XSSFRichTextString("Especificaciones Campo Operador CIIU: \n*Lista valores: \"=\",\"<>\",\">\",\"<\",\"IN\",\"NOT IN\""));
        row.getCell(6).setCellComment(comment6);

        Comment comment7 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment7.setString(new XSSFRichTextString("Especificaciones Campo CIIU: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(7).setCellComment(comment7);

        Comment comment8 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment8.setString(new XSSFRichTextString("Especificaciones Campo Operador Número de empleados: \n*Lista valores: \"=\",\"<>\",\">\",\"<\",\"IN\",\"NOT IN\""));
        row.getCell(8).setCellComment(comment8);

        Comment comment9 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment9.setString(new XSSFRichTextString("Especificaciones Campo Número de empleados: \n*Longitud Maxima:255 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(9).setCellComment(comment9);

        Comment comment10 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment10.setString(new XSSFRichTextString("Especificaciones Campo Operador Total Activos: \n*Lista valores: \"=\",\"<>\",\">\",\"<\",\"IN\",\"NOT IN\""));
        row.getCell(10).setCellComment(comment10);

        Comment comment11 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment11.setString(new XSSFRichTextString("Especificaciones Campo Total Activos: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(11).setCellComment(comment11);

        Comment comment12 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment12.setString(new XSSFRichTextString("Especificaciones Campo Operador Total Ventas: \n*Lista valores: \"=\",\"<>\",\">\",\"<\",\"IN\",\"NOT IN\""));
        row.getCell(12).setCellComment(comment12);

        Comment comment13 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment13.setString(new XSSFRichTextString("Especificaciones Campo Total Ventas: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(13).setCellComment(comment13);

        Comment comment14 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment14.setString(new XSSFRichTextString("Especificaciones Campo Verificación Contratos: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(14).setCellComment(comment14);

        Comment comment15 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment15.setString(new XSSFRichTextString("Especificaciones Campo Otros criterios: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(15).setCellComment(comment15);
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

        for(SegmentDecisionTree segmentDecisionTree: segmentDecisionTreeList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,segmentDecisionTree.getCodigoIFRS9(),style);
            createCell(row,columnCount++,segmentDecisionTree.getDescripcionSectorizacion(),style);
            createCell(row,columnCount++,segmentDecisionTree.getCorasuOp(),style);
            createCell(row,columnCount++,segmentDecisionTree.getCorasu(),style);
            createCell(row,columnCount++,segmentDecisionTree.getSubCorasuOp(),style);
            createCell(row,columnCount++,segmentDecisionTree.getSubCorasu(),style);
            createCell(row,columnCount++,segmentDecisionTree.getCiiuOp(),style);
            createCell(row,columnCount++,segmentDecisionTree.getCiiu(),style);
            createCell(row,columnCount++,segmentDecisionTree.getNumeroEmpleadosOp(),style);
            createCell(row,columnCount++,segmentDecisionTree.getNumeroEmpleados(),style);
            createCell(row,columnCount++,segmentDecisionTree.getTotalActivosOp(),style);
            createCell(row,columnCount++,segmentDecisionTree.getTotalActivos(),style);
            createCell(row,columnCount++,segmentDecisionTree.getTotalVentasOp(),style);
            createCell(row,columnCount++,segmentDecisionTree.getTotalVentas(),style);
            createCell(row,columnCount++,segmentDecisionTree.getVerificacionContratos(),style);
            createCell(row,columnCount,segmentDecisionTree.getOtrosCriterios(),style);
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
            createCell(row,columnCount,third[1],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}