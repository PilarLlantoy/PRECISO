package com.inter.proyecto_intergrupo.service.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.ParamNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamNIC34Consol;
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

public class ParamNIC34ConsolListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> nic34List;
    private List<ParamNIC34Consol> listNic34;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ParamNIC34ConsolListReport(List<String[]> nic34List, List<ParamNIC34Consol> listNic34) {
        this.nic34List = nic34List;
        this.listNic34 = listNic34;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Param NIC34");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int columnCountH = 0;
        createCell(row, columnCountH++, "L6", style);
        createCell(row, columnCountH++,"CUENTA", style);
        createCell(row, columnCountH++,"ID_GRUPO", style);
        createCell(row, columnCountH++,"GRUPO", style);
        createCell(row, columnCountH++,"APLICA", style);
        createCell(row, columnCountH++,"ID_NOTA", style);
        createCell(row, columnCountH++,"NOTA", style);
        createCell(row, columnCountH++,"ID_SUBNOTA", style);
        createCell(row, columnCountH++,"SUBNOTA", style);
        createCell(row, columnCountH++,"ID_CAMPO", style);
        createCell(row, columnCountH++,"CAMPO", style);
        createCell(row, columnCountH++,"MONEDA", style);
        createCell(row, columnCountH++,"SIGNO", style);
        createCell(row, columnCountH++,"RESPONSABLE", style);
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

        CellStyle style1 = workbook.createCellStyle();
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(ParamNIC34Consol paramNIC34: listNic34){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,paramNIC34.getL6(),style);
            createCell(row,columnCount++,paramNIC34.getCuenta(),style);
            createCell(row,columnCount++,paramNIC34.getIdGrupo(),style);
            createCell(row,columnCount++,paramNIC34.getGrupo(),style);
            createCell(row,columnCount++,paramNIC34.getAplica(),style);
            createCell(row,columnCount++,paramNIC34.getIdNota(),style);
            createCell(row,columnCount++,paramNIC34.getNota(),style);
            createCell(row,columnCount++,paramNIC34.getIdSubnota(),style);
            createCell(row,columnCount++,paramNIC34.getSubnota(),style);
            createCell(row,columnCount++,paramNIC34.getIdCampo(),style);
            createCell(row,columnCount++,paramNIC34.getCampo(),style);
            createCell(row,columnCount++,paramNIC34.getMoneda(),style);
            createCell(row,columnCount++,paramNIC34.getSigno(),style1);
            createCell(row,columnCount++,paramNIC34.getResponsable(),style);
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

        createCell(row1, 0, nic34List.get(nic34List.size()-1)[0], style1);
        createCell(row1, 1, nic34List.get(nic34List.size()-1)[1], style1);
        createCell(row1, 2, nic34List.get(nic34List.size()-1)[2], style1);

        if(nic34List.size()>1)
        {
            nic34List.remove(nic34List.size()-1);
            Row row2 = sheet.createRow(2);
            createCell(row2, 0, "Fila", style);
            createCell(row2, 1, "Columna", style);
            createCell(row2, 2, "Estado", style);

            int rowCount = 3;
            for (String[] log : nic34List)
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
