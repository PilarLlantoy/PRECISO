package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.Deaccount;
import com.inter.proyecto_intergrupo.model.ifrs9.ManualAdjustments;
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

public class ManualAdjustmentsListReport {
    private List<ManualAdjustments> listManual;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    public ManualAdjustmentsListReport(List<ManualAdjustments> manuals){
        this.listManual = manuals;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Ajustes Manuales");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Código Consolidación", style);
        createCell(row, 1, "Divisa España", style);
        createCell(row, 2, "Saldo", style);
        createCell(row, 3, "Fuente", style);
        createCell(row, 4, "Observación", style);
        createCell(row, 5, "Periodo", style);

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

        for(ManualAdjustments manual: listManual){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,manual.getCodicons(),style);
            createCell(row,columnCount++,manual.getDivisaEspana(),style);
            createCell(row,columnCount++,Double.parseDouble(manual.getSaldo().toString()),style1);
            createCell(row,columnCount++,manual.getFuente(),style);
            createCell(row,columnCount++,manual.getObservacion(),style);
            createCell(row,columnCount++,manual.getPeriodo(),style);

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
