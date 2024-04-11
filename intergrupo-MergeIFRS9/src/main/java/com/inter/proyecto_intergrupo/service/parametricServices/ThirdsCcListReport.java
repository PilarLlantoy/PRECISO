package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.dataquality.RulesDQ;
import com.inter.proyecto_intergrupo.model.parametric.ThirdsCc;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ThirdsCcListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> thirdList;
    private List<ThirdsCc> listThirds;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ThirdsCcListReport(List<String[]> thirdList, List<ThirdsCc> listThirds) {
        this.thirdList = thirdList;
        this.listThirds = listThirds;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Terceros CC");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "NIT", style);
        createCell(row, 1, "Nombre Completo", style);
        createCell(row, 2, "Impuesto", style);
        createCell(row, 3, "Teléfono", style);
        createCell(row, 4, "Correo Principal", style);
        createCell(row, 5, "Correo Alterno", style);
        createCell(row, 6, "Correo Alterno 2", style);
        createCell(row, 7, "Dirección", style);
        createCell(row, 8, "Correo Copia 1", style);
        createCell(row, 9, "Correo Copia 2", style);
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
        Cell cell = row.createCell(columCount);

        if(value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if(value instanceof Integer){
            cell.setCellValue((Integer) value);
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        }else if(value instanceof Long){
            cell.setCellValue((Long) value);
        }else if(value instanceof Double){
            cell.setCellValue((Double) value);
        }else if(value instanceof String){
            cell.setCellValue((String) value);
        }

        cell.setCellStyle(style);
    }

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(ThirdsCc rule: listThirds){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,rule.getNit(),style);
            createCell(row,columnCount++,rule.getNombre(),style);
            createCell(row,columnCount++,rule.getImpuesto(),style);
            createCell(row,columnCount++,rule.getTelefono(),style);
            createCell(row,columnCount++,rule.getCorreo(),style);
            createCell(row,columnCount++,rule.getCorreoAlterno(),style);
            createCell(row,columnCount++,rule.getCorreoAlterno2(),style);
            createCell(row,columnCount++,rule.getDireccion(),style);
            createCell(row,columnCount++,rule.getCorreoCopia1(),style);
            createCell(row,columnCount++,rule.getCorreoCopia2(),style);
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

    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Cargue");
        Row row = sheet.createRow(0);
        Row row1 = sheet.createRow(1);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font1 = workbook.createFont();
        font1.setBold(false);
        font1.setFontHeight(10);
        style1.setFont(font1);

        createCell(row, 0, "#Exitosos", style);
        createCell(row, 1, "#Fallidos", style);
        createCell(row, 2, "Estado Final", style);

        createCell(row1, 0, thirdList.get(thirdList.size()-1)[0], style1);
        createCell(row1, 1, thirdList.get(thirdList.size()-1)[1], style1);
        createCell(row1, 2, thirdList.get(thirdList.size()-1)[2], style1);

        if(thirdList.size()>1)
        {
            thirdList.remove(thirdList.size()-1);
            Row row2 = sheet.createRow(2);
            createCell(row2, 0, "Fila", style);
            createCell(row2, 1, "Columna", style);
            createCell(row2, 2, "Estado", style);

            int rowCount = 3;
            for (String[] log : thirdList)
            {
                Row row3 = sheet.createRow(rowCount++);
                int columnCount = 0;
                createCell(row3, columnCount++, log[0], style1);
                createCell(row3, columnCount++, log[1], style1);
                createCell(row3, columnCount++, log[2], style1);
            }
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
