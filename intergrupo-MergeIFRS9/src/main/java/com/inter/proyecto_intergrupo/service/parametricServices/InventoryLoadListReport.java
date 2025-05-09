package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.CampoRConcil;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

public class InventoryLoadListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private List<CampoRConcil> colCroutes;
    private List<Object[]> aroutes;
    private ConciliationRoute cr;

    public InventoryLoadListReport(List<Object[]> aroutes, List<CampoRConcil> colCroutes, ConciliationRoute cr){
        this.aroutes = aroutes;
        this.colCroutes = colCroutes;
        this.cr = cr;
        this.workbook = new SXSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet(cr.getNombreArchivo().replace(" ","_"));
        Row row = sheet.createRow(0);
        int count = 0;

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        createCell(row, count++,"ID", style);
        for (CampoRConcil campo :colCroutes) {
            createCell(row, count++, campo.getNombre().toUpperCase().replace("PERIODO_PRECISO","FECHA_CARGUE"), style);
        }
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

    public String normalizeDate(String dateStr) {
        // Lista de formatos posibles
        List<String> possibleFormats = List.of(
                "ddMMyyyy", "yyyyMMdd", "MMddyyyy", "yyMMdd", "ddMMyy", "yyyyddMM",
                "dd-MM-yyyy", "yyyy-MM-dd", "MM-dd-yyyy", "yy-MM-dd", "dd-MM-yy",
                "dd/MM/yyyy", "yyyy/MM/dd", "MM/dd/yyyy", "yy/MM/dd", "dd/MM/yy"
        );

        for (String format : possibleFormats) {
            try {
                // Intentamos parsear la fecha con cada formato
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(format);
                LocalDate date = LocalDate.parse(dateStr, inputFormatter);

                // Convertimos la fecha al formato deseado
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ignored) {
                // Si falla, intentamos con el siguiente formato
            }
        }

        throw new IllegalArgumentException("Formato de fecha no reconocido: " + dateStr);
    }

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);


        CellStyle style1 = workbook.createCellStyle();
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        CellStyle style2 = workbook.createCellStyle();
        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        CellStyle style3 = workbook.createCellStyle();
        style3.setFont(font);
        style3.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));

        int countReg=1;
        for(Object[] data: aroutes){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, countReg++, style2);
            for (int i =0;i<data.length;i++)
            {
                if(data.length==(columnCount+1) && data[i]!=null) {
                    createCell(row, columnCount++, data[i], style3);
                }
                else if(data[i]!=null && colCroutes.get(i).getTipo()!=null && (colCroutes.get(i).getTipo().equalsIgnoreCase("Integer") || colCroutes.get(i).getTipo().equalsIgnoreCase("Bigint")))
                    createCell(row,columnCount++,Integer.parseInt(data[i].toString()),style2);
                else if(data[i]!=null && colCroutes.get(i).getTipo()!=null && (colCroutes.get(i).getTipo().equalsIgnoreCase("Float")))
                    createCell(row,columnCount++,Double.parseDouble(data[i].toString()),style1);
                else if(data[i]!=null && colCroutes.get(i).getTipo()!=null && (colCroutes.get(i).getTipo().equalsIgnoreCase("Date")))
                    createCell(row, columnCount++, normalizeDate(data[i].toString()), style3);
                else if(data[i]!=null)
                    createCell(row,columnCount++,data[i].toString(),style);
                else
                    createCell(row,columnCount++,"",style);
                if(columnCount == 1000000)
                    break;
            }
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        workbook.dispose();
        outputStream.flush(); // Asegúrate de que todos los datos se envíen
        outputStream.close();
    }
}
