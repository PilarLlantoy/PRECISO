package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.Anexo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SegmentsListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Object[]> list;
    private List<Object[]> listAll;

    public SegmentsListReport(List<Object[]> list, List<Object[]> listAll) {
        this.list = list;
        this.listAll = listAll;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Segmentos");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "NUMCLIEN", style);
        createCell(row, 1, "IDENTIFICACION", style);
        createCell(row, 2, "NOMBRE CLIENTE", style);
        createCell(row, 3, "TIPO_PERSONA", style);
        createCell(row, 4, "VIEJO SEGMENTO_FINREP", style);
        createCell(row, 5, "NUEVO SEGMENTO_FINREP", style);
        createCell(row, 6, "CORAZU", style);
        createCell(row, 7, "SUB_CORAZU", style);
        createCell(row, 8, "NUEVO CORAZU", style);
        createCell(row, 9, "NUEVO SUBCORAZU", style);
        createCell(row, 10, "TIPO_INSTITUCION", style);
        createCell(row, 11, "NUEVO TIPO_INSTITUCION", style);
        createCell(row, 12, "OBSERVACIONES", style);
        createCell(row, 13, "PERIODO", style);

    }

    private void writeHeaderLineAll(String fuente){
        sheet = workbook.createSheet("Segmentos");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Número Cliente", style);
        createCell(row, 1, "Identificación", style);
        createCell(row, 2, "Nombre Cliente", style);
        createCell(row, 3, "Tipo Persona", style);
        createCell(row, 4, "Segmento FINREP Viejo", style);
        createCell(row, 5, "Segmento FINREP Nuevo", style);
        createCell(row, 6, "Corasu", style);
        createCell(row, 7, "Subcorasu", style);
        createCell(row, 8, "CIIU", style);
        createCell(row, 9, "Número Empleados", style);
        createCell(row, 10, "Total Activos", style);
        createCell(row, 11, "Total Ventas", style);
        createCell(row, 12, "Tipo Institución", style);
        createCell(row, 13, "Periodo", style);
        createCell(row, 14, "Observación", style);
        if(fuente.equals("RISK"))
            createCell(row, 15, "Contrato", style);
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

    private void writeDataLines() throws ParseException {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] List: list){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,List[0],style);
            createCell(row,columnCount++,List[14],style);
            createCell(row,columnCount++,List[1],style);
            createCell(row,columnCount++,List[2],style);
            createCell(row,columnCount++,List[3],style);
            createCell(row,columnCount++,List[4],style);
            createCell(row,columnCount++,List[5],style);
            createCell(row,columnCount++,List[6],style);
            createCell(row,columnCount++,List[7],style);
            createCell(row,columnCount++,List[8],style);
            createCell(row,columnCount++,List[9],style);
            createCell(row,columnCount++,List[10],style);
            createCell(row,columnCount++,List[11],style);
            createCell(row,columnCount++,List[12],style);
            createCell(row,columnCount++,List[13],style);
        }
    }

    private void writeDataLinesAllRisk() throws ParseException {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] List: listAll){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,List[0],style);
            createCell(row,columnCount++,List[14],style);
            createCell(row,columnCount++,List[1],style);
            createCell(row,columnCount++,List[2],style);
            createCell(row,columnCount++,List[3],style);
            createCell(row,columnCount++,List[4],style);
            createCell(row,columnCount++,List[5],style);
            createCell(row,columnCount++,List[6],style);
            createCell(row,columnCount++,List[7],style);
            createCell(row,columnCount++,List[8],style);
            createCell(row,columnCount++,List[9],style);
            createCell(row,columnCount++,List[10],style1);
            createCell(row,columnCount++,List[11],style1);
            createCell(row,columnCount++,List[12],style);
            //createCell(row,columnCount++,List[13],style);
            if(List[15]!=null && !List[15].toString().isEmpty()) {
                createCell(row, columnCount++, "SUGERENCIA ÁRBOL", style);
            }
            else {
                createCell(row, columnCount++, "HISTÓRICO", style);
            }
            if(List[16]!=null && List[16].toString()!=null) {
                createCell(row, columnCount++, List[16].toString(), style);
            }
            else {
                createCell(row, columnCount++, "", style);
            }
        }
    }

    private void writeDataLinesAll() throws ParseException {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] List: listAll){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,List[0],style);
            createCell(row,columnCount++,List[14],style);
            createCell(row,columnCount++,List[1],style);
            createCell(row,columnCount++,List[2],style);
            createCell(row,columnCount++,List[3],style);
            createCell(row,columnCount++,List[4],style);
            createCell(row,columnCount++,List[5],style);
            createCell(row,columnCount++,List[6],style);
            createCell(row,columnCount++,List[7],style);
            createCell(row,columnCount++,List[8],style);
            createCell(row,columnCount++,List[9],style1);
            createCell(row,columnCount++,List[10],style1);
            createCell(row,columnCount++,List[11],style);
            createCell(row,columnCount++,List[12],style);
            createCell(row,columnCount++,List[13],style);
        }
    }

    public void export(HttpServletResponse response) throws IOException, ParseException {
        writeHeaderLineAll("SEGMENTS");
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportAll(HttpServletResponse response) throws IOException, ParseException {
        writeHeaderLineAll("MIS");
        writeDataLinesAll();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportAllRisk(HttpServletResponse response) throws IOException, ParseException {
        writeHeaderLineAll("RISK");
        writeDataLinesAllRisk();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}
