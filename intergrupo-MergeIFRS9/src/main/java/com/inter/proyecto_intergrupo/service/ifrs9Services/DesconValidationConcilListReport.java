package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.AccountControl;
import com.inter.proyecto_intergrupo.model.ifrs9.DesconValConcil;
import com.inter.proyecto_intergrupo.model.ifrs9.DesconValDif;
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
import java.util.Date;
import java.util.List;

public class DesconValidationConcilListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;
    private List<DesconValConcil> desconListDif;
    private final short SIZE = 10;

    public DesconValidationConcilListReport(List<DesconValConcil> desconListDif){
        this.workbook = new XSSFWorkbook();
        this.desconListDif = desconListDif;
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Validación Descontabilización");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(SIZE);
        style.setFont(font);

        createCell(row, 0, "Codicons", style);
        createCell(row, 1, "Cuenta", style);
        createCell(row, 2, "DESCON_NIV15_PROV_PLAN00", style);
        createCell(row,	3, "TANQUE_NIV15_PROV_PLAN00", style);
        createCell(row,	4, "CONTAB_NIV15_PROV_PLAN00", style);
        createCell(row,	5, "PATPYG_NIV15_PROV_PLAN00", style);
        createCell(row,	6, "INFORME_NIVEL15_RECLASIFICACION_PLAN00", style);
        createCell(row,	7, "Periodo", style);
        createCell(row,	8, "Nivel", style);
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
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

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();

        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(DesconValConcil account: desconListDif){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,	account.getCodicons()	,style);
            createCell(row,columnCount++,	account.getCuenta()	,style);
            createCell(row,columnCount++,	account.getSaldoDifConc()	,style1);
            createCell(row,columnCount++,	account.getSaldoManuales()	,style1);
            createCell(row,columnCount++,	account.getSaldoProvGenint()	,style1);
            createCell(row,columnCount++,	account.getSaldoPorcCal()	,style1);
            createCell(row,columnCount++,	account.getSaldoRechazosAut()	,style1);
            createCell(row,columnCount++,	account.getPeriodo()	,style);
            createCell(row,columnCount++,	account.getNivel()	,style);
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
