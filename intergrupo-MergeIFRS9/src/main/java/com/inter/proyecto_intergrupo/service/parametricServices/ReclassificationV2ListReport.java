package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.AvalTypes;
import com.inter.proyecto_intergrupo.model.parametric.ReclassificationIntergroup;
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

public class ReclassificationV2ListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ReclassificationIntergroup> reclist;

    public ReclassificationV2ListReport(List<ReclassificationIntergroup> list){
        this.reclist = list;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Reclasificaciones V2");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Concepto", style);
        createCell(row, 1, "Código de Consolidación", style);
        createCell(row, 2, "Tipo Sociedad", style);
        createCell(row, 3, "Segmento", style);
        createCell(row, 4, "Producto", style);
        createCell(row, 5, "Tipo", style);
        createCell(row, 6, "Stage", style);
        createCell(row, 7, "Cuenta", style);
        createCell(row, 8, "Cuenta Contrapartida", style);

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

        for(ReclassificationIntergroup rec : reclist){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,rec.getConcepto(),style);
            createCell(row,columnCount++,rec.getCodicons(),style);
            createCell(row,columnCount++,rec.getTipoSociedad(),style);
            createCell(row,columnCount++,rec.getSegmento(),style);
            createCell(row,columnCount++,rec.getProducto(),style);
            createCell(row,columnCount++,rec.getTipo(),style);
            createCell(row,columnCount++,rec.getStage(),style);
            createCell(row,columnCount++,rec.getCuenta(),style);
            createCell(row,columnCount++,rec.getCuentaContrapartida(),style);
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

        createCell(row1, 0, "Registro", style);
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
