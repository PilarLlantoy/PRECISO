package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.reports.ConciliationComer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ConciliationComerListReportService {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ConciliationComer> listConciliation;

    public ConciliationComerListReportService(List<ConciliationComer> listConciliation){
        this.listConciliation = listConciliation;
        workbook = new XSSFWorkbook();
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
        //sheet.autoSizeColumn(columCount);
        Cell cell = row.createCell(columCount);

        if(value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if(value instanceof Integer){
            cell.setCellValue((Integer) value);
        } else if(value instanceof Double){
            cell.setCellValue((Double) value);
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        } else if(value instanceof Long){
            cell.setCellValue((Long) value);
        }else if(value instanceof String){
            cell.setCellValue((String) value);
        }

        cell.setCellStyle(style);
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Reporte Conciliacion Comer");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row,0, "Cuenta banco", style);
        createCell(row,	1, "Cuenta Comercializadora", style);
        createCell(row,	2, "Nombre Cuenta Banco", style);
        createCell(row,	3, "Importe Comercializadora", style);
        createCell(row,	4, "Prorrata Iva", style);
        createCell(row,	5, "Total Comer", style);
        createCell(row,	6, "Importe Real", style);
        createCell(row,	7, "Importe Provisiones", style);
        createCell(row,	8, "Total Gps", style);
        createCell(row,	9, "Diferencias Tot", style);
        createCell(row,	10, "Importe Base Fiscal", style);
        createCell(row,	11, "Diferencia Pagos Reales", style);
        createCell(row,	12, "Importe Balance", style);
        createCell(row,	13, "Diferencia Total Gps Balance", style);
    }

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(ConciliationComer conciliationComer: listConciliation){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            createCell(row,columnCount++,	conciliationComer.getCuentaBanco()	,style);
            createCell(row,columnCount++,	conciliationComer.getCuentaComercializadora()	,style);
            createCell(row,columnCount++,	conciliationComer.getNombreCuentaBanco()	,style);
            createCell(row,columnCount++,	conciliationComer.getImporteComercializadora()	,style);
            createCell(row,columnCount++,	conciliationComer.getProrrataIva()	,style);
            createCell(row,columnCount++,	conciliationComer.getTotalComer()	,style);
            createCell(row,columnCount++,	conciliationComer.getImporteReal(),style);
            createCell(row,columnCount++,	conciliationComer.getImporteProvisiones(),style);
            createCell(row,columnCount++,	conciliationComer.getTotalGps(),style);
            createCell(row,columnCount++,	conciliationComer.getDiferenciasTot()	,style);
            createCell(row,columnCount++,	conciliationComer.getImporteBaseFiscal()	,style);
            createCell(row,columnCount++,	conciliationComer.getDiferenciaPagosReales()	,style);
            createCell(row,columnCount++,	conciliationComer.getImporteBalance()	,style);
            createCell(row,columnCount++,	conciliationComer.getDiferenciaTotalGpsBalance()	,style);
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
