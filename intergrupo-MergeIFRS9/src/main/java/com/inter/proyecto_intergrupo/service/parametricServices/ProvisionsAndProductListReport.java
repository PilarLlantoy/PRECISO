package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.ProvisionsAndProduct;
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

public class ProvisionsAndProductListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ProvisionsAndProduct> provisionsAndProductList;

    public ProvisionsAndProductListReport(List<ProvisionsAndProduct> provisionsAndProductList){
        this.provisionsAndProductList = provisionsAndProductList;
        workbook = new XSSFWorkbook();
    }

    //TODO
    private void writeHeaderLine(){
        sheet = workbook.createSheet("ProvisionesYCuenta");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Cuenta", style);
        createCell(row, 1, "Instrumento", style);
        createCell(row, 2, "Jerarquia", style);
        createCell(row, 3, "Descripcion", style);
        createCell(row, 4, "Minimo", style);
        createCell(row, 5, "Perimetro IFRS9", style);
        createCell(row, 6, "Stages España", style);
        createCell(row, 7, "Producto España", style);
        createCell(row, 8, "Sector España", style);
        createCell(row, 9, "Signo", style);


        Comment comment = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment.setString(new XSSFRichTextString("Especificaciones Campo Cuenta: \n*Longitud Maxima:18 \n*Longitud Minima:4 \n*Tipo dato: Alfanúmerico"));
        row.getCell(0).setCellComment(comment);

        Comment comment1 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment1.setString(new XSSFRichTextString("Especificaciones Campo Instrumento: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(1).setCellComment(comment1);

        Comment comment2 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment2.setString(new XSSFRichTextString("Especificaciones Campo Jerarquia: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(2).setCellComment(comment2);

        Comment comment3 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment3.setString(new XSSFRichTextString("Especificaciones Campo Descripcion: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(3).setCellComment(comment3);

        Comment comment4 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment4.setString(new XSSFRichTextString("Especificaciones Campo Minimo: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(4).setCellComment(comment4);

        Comment comment5 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment5.setString(new XSSFRichTextString("Especificaciones Campo Perimetro IFRS9: \n*Tipo dato: Booleano"));
        row.getCell(5).setCellComment(comment5);

        Comment comment6 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment6.setString(new XSSFRichTextString("Especificaciones Campo Stages España: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(6).setCellComment(comment6);

        Comment comment7 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment7.setString(new XSSFRichTextString("Especificaciones Campo Producto España: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(7).setCellComment(comment7);

        Comment comment8 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment8.setString(new XSSFRichTextString("Especificaciones Campo Sector España: \n*Longitud Maxima:50 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(8).setCellComment(comment8);

        Comment comment9 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment9.setString(new XSSFRichTextString("Especificaciones Campo Sector Signo: \n*Longitud Maxima:1 \n*Longitud Minima:1 \n*Tipo dato: Alfanúmerico"));
        row.getCell(9).setCellComment(comment9);
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

        for(ProvisionsAndProduct provisionsAndProduct: provisionsAndProductList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,provisionsAndProduct.getCuenta(),style);
            createCell(row,columnCount++,provisionsAndProduct.getInstrumento(),style);
            createCell(row,columnCount++,provisionsAndProduct.getJerarquia(),style);
            createCell(row,columnCount++,provisionsAndProduct.getDescripcion(),style);
            createCell(row,columnCount++,provisionsAndProduct.getMinimo(),style);
            createCell(row,columnCount++,provisionsAndProduct.getPerimetroIFRS9(),style);
            createCell(row,columnCount++,provisionsAndProduct.getStagesSpain(),style);
            createCell(row,columnCount++,provisionsAndProduct.getProductoSpain(),style);
            createCell(row,columnCount++,provisionsAndProduct.getSectorSpain(),style);
            createCell(row,columnCount,provisionsAndProduct.getSigno(),style);
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