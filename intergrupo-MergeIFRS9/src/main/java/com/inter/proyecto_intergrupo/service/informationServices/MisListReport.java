package com.inter.proyecto_intergrupo.service.informationServices;

import com.inter.proyecto_intergrupo.model.information.Mis;
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

public class MisListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Mis> misList;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public MisListReport(List<Mis> misList) {
        this.misList = misList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("MIS");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "COD NEOCON", style);
        createCell(row, 1, "DIVISA", style);
        createCell(row, 2, "YNTP", style);
        createCell(row, 3, "SOCIEDAD YNTP", style);
        createCell(row, 4, "CONTRATO", style);
        createCell(row, 5, "NIT CONTRAPARTE", style);
        createCell(row, 6, "VALOR", style);
        createCell(row, 7, "COD PAÍS", style);
        createCell(row, 8, "PAÍS", style);
        createCell(row, 9,"CUENTA LOCAL", style);
        createCell(row, 10,"OBSERVACIONES", style);
        createCell(row, 11,"PERIODO", style);

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
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Mis mis: misList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,mis.getCodNeocon(),style);
            createCell(row,columnCount++,mis.getDivisa(),style);
            createCell(row,columnCount++,mis.getYntp(),style);
            createCell(row,columnCount++,mis.getSociedadYntp(),style);
            createCell(row,columnCount++,mis.getContrato(),style);
            createCell(row,columnCount++,mis.getNit(),style);
            createCell(row,columnCount++,mis.getValor(),style1);
            createCell(row,columnCount++,mis.getCodPais(),style);
            createCell(row,columnCount++,mis.getPais(),style);
            createCell(row,columnCount++,mis.getCuentaLocal(),style);
            createCell(row,columnCount++,mis.getObservaciones(),style);
            createCell(row,columnCount++,mis.getPeriodoContable(),style);
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




}
