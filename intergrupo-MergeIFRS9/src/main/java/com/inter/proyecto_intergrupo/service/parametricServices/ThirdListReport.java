package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.Third;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ThirdListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Third> thirdList;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ThirdListReport(List<Third> thirdList){
        this.thirdList = thirdList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Terceros");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "NIT Contraparte", style);
        createCell(row, 1, "Contraparte", style);
        createCell(row, 2, "Código Cliente", style);
        createCell(row, 3, "TIPO", style);
        createCell(row, 4, "DV", style);
        createCell(row, 5, "Yntp", style);
        createCell(row, 6, "Marca Tipo de Institución", style);
        createCell(row, 7, "Fecha Creación", style);

        Comment comment = this.sheet
                        .createDrawingPatriarch()
                        .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment.setString(new XSSFRichTextString("Especificaciones Campo NIT Contraparte: \n*Longitud Maxima:14 \n*Longitud Minima:1 \n*Tipo dato: Númerico"));
        row.getCell(0).setCellComment(comment);

        Comment comment1 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment1.setString(new XSSFRichTextString("Especificaciones Campo Contraparte: \n*Longitud Maxima:254 \n*Longitud Minima:1 \n*Tipo dato: Alfanumérico"));
        row.getCell(1).setCellComment(comment1);

        Comment comment2 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment2.setString(new XSSFRichTextString("Especificaciones Campo Código Cliente: \n*Longitud Maxima:14 \n*Longitud Minima:1 \n*Tipo dato: Númerico"));
        row.getCell(2).setCellComment(comment2);

        Comment comment3 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment3.setString(new XSSFRichTextString("Especificaciones Campo TIPO: \n*Longitud Maxima:2 \n*Longitud Minima:1 \n*Tipo dato: Númerico"));
        row.getCell(3).setCellComment(comment3);

        Comment comment4 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment4.setString(new XSSFRichTextString("Especificaciones Campo DV: \n*Longitud Maxima:1 \n*Longitud Minima:1 \n*Tipo dato: Númerico"));
        row.getCell(4).setCellComment(comment4);

        Comment comment5 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment5.setString(new XSSFRichTextString("Especificaciones Campo Yntp: \n*Longitud Maxima:5 \n*Longitud Minima:1 \n*Tipo dato: Númerico"));
        row.getCell(5).setCellComment(comment5);

        Comment comment6 = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment6.setString(new XSSFRichTextString("Especificaciones Campo Marca Tipo de Institución: \n*Longitud Maxima:5 \n*Longitud Minima:1 \n*Tipo dato: Númerico"));
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

        for(Third third: thirdList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,third.getNit(),style);
            createCell(row,columnCount++,third.getContraparte(),style);
            createCell(row,columnCount++,third.getCodigoCliente(),style);
            createCell(row,columnCount++,third.getTipo(),style);
            createCell(row,columnCount++,third.getDv(),style);
            createCell(row,columnCount++,third.getYntp(),style);
            createCell(row,columnCount++,third.getMarcaTipoInstitucion(),style);
            createCell(row,columnCount++,third.getFecha().toString(),style);
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

    public static String completeWithZero(String nit){

        int numZeros = 15 - nit.length();

        String zeros = "";
        for (int i = 0; i < numZeros; i++){
            zeros = zeros.concat("0");
        }
        return zeros+ nit;
    }

    public void exportNit(HttpServletResponse response) throws IOException {
        File file = new File("C:\\Users\\CE66390\\Documents\\BBVA Intergrupo\\Pruebas\\Base\\Terceros.txt");
        //File file = new File("D:\\BBVA\\Pruebas\\BaseFiscal\\Terceros\\Terceros.txt");
        try (PrintWriter out = new PrintWriter(file.getAbsolutePath())){
            for (Third third: thirdList){
                out.println(third.getTipo() + completeWithZero(String.valueOf(third.getNit())) + third.getDv());
            }
            out.println("FINAL");
            out.flush();
            out.close();

            FileInputStream archivo = new FileInputStream(file.getAbsolutePath());
            int longitud = archivo.available();
            byte[] datos = new byte[longitud];
            archivo.read(datos);
            archivo.close();

            response.getOutputStream().write(datos);
            response.getOutputStream().flush();
            response.getOutputStream().close();

            file.delete();
        }
    }

    public void exportLog(HttpServletResponse response, ArrayList<String[]> lista) throws IOException
    {
        sheet = workbook.createSheet("LOG_TERCEROS");
        Row row1 = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "NIT Contraparte", style);
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
