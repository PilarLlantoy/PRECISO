package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.AdjustmentsHom;
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

public class AdjustmentsHomListReport {
    private List<AdjustmentsHom> listManual;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    public AdjustmentsHomListReport(List<AdjustmentsHom> manuals){
        this.listManual = manuals;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Ajustes Homogeneización");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Tipo Asiento", style);
        createCell(row, 1, "Descripción Asiento", style);
        createCell(row, 2, "Código Consolidación", style);
        createCell(row, 3, "Debe 1", style);
        createCell(row, 4, "Haber 1", style);
        createCell(row, 5, "Debe 2", style);
        createCell(row, 6, "Haber 2", style);
        createCell(row, 7, "Divisa", style);
        createCell(row, 8, "Sociedad IC", style);
        createCell(row, 9, "Descripción Sociedad IC", style);
        createCell(row, 10, "Periodo", style);

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

        for(AdjustmentsHom manual: listManual){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,manual.getTipoAsiento(),style);
            createCell(row,columnCount++,manual.getDescripcionAsiento(),style);
            createCell(row,columnCount++,manual.getCodicons(),style);
            createCell(row,columnCount++,Double.parseDouble(manual.getSaldoDebe1().toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(manual.getSaldoHaber1().toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(manual.getSaldoDebe2().toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(manual.getSaldoHaber2().toString()),style1);
            createCell(row,columnCount++,manual.getDivisa(),style);
            createCell(row,columnCount++,manual.getSociedadIc(),style);
            createCell(row,columnCount++,manual.getDescripcionIc(),style);
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
