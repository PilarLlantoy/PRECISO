package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.Provisions;
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

public class ProvisionsListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Provisions> provisionsList;

    public ProvisionsListReport(List<Provisions> provisionsList){
        this.provisionsList = provisionsList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Provisiones");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 3, "Cuenta Neocon", style);
        createCell(row, 0, "Instrumento", style);
        createCell(row, 1, "Jerarquía", style);
        createCell(row, 2, "Descripción", style);
        createCell(row, 4, "Mínimo", style);
        createCell(row, 5, "Perímetro IFRS9", style);
        createCell(row, 6, "Stage España", style);
        createCell(row, 7, "Producto España", style);
        createCell(row, 8, "Sector", style);
        createCell(row, 9, "Signo", style);
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

        for(Provisions provisions: provisionsList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,provisions.getInstrumento(),style);
            createCell(row,columnCount++,provisions.getJerarquia(),style);
            createCell(row,columnCount++,provisions.getDescripcion(),style);
            createCell(row,columnCount++,provisions.getCuentaNeocon(),style);
            createCell(row,columnCount++,provisions.getMinimo(),style);
            createCell(row,columnCount++,provisions.getIfrs9(),style);
            createCell(row,columnCount++,provisions.getStageEspana(),style);
            createCell(row,columnCount++,provisions.getProductoEspana(),style);
            createCell(row,columnCount++,provisions.getSector(),style);
            createCell(row,columnCount++,provisions.getSigno(),style);
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

        createCell(row1, 0, "Cuenta Neocon", style);
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
