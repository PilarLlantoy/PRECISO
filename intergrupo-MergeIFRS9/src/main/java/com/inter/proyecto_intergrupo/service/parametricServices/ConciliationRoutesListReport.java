package com.inter.proyecto_intergrupo.service.parametricServices;

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

public class ConciliationRoutesListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet2;
    private XSSFSheet sheet3;
    private XSSFSheet sheet4;
    private List<Object[]> dataList;
    private List<Object[]> dataListVal;
    private List<Object[]> dataListCam;

    public ConciliationRoutesListReport(List<Object[]> dataList, List<Object[]> dataListVal, List<Object[]> dataListCam){
        this.dataList = dataList;
        this.dataListVal = dataListVal;
        this.dataListCam = dataListCam;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        if(dataList!=null)
        {
            sheet = workbook.createSheet("Encabezados");
            Row row = sheet.createRow(0);

            createCell(row, 0,"Cod. Concil", style);
            createCell(row, 1,"Nom. Concil", style);
            createCell(row, 2,"Cod. Invent", style);
            createCell(row, 3,"Nom. Invent", style);
            createCell(row, 4,"Nom. Archivo", style);
            createCell(row, 5,"Ruta", style);
            createCell(row, 6,"Tipo Archivo", style);
            createCell(row, 7,"Estado", style);
        }

        if(dataListCam!=null)
        {
            sheet4 = workbook.createSheet("Campos");
            Row row4 = sheet4.createRow(0);

            createCell(row4, 0,"Cod. Concil", style);
            createCell(row4, 1,"Nom. Concil", style);
            createCell(row4, 2,"Cod. Invent", style);
            createCell(row4, 3,"Nom. Invent", style);
            createCell(row4, 4,"Cod. Campo", style);
            createCell(row4, 5,"Nom. Campo", style);
            createCell(row4, 6,"Primario", style);
            createCell(row4, 7,"Tipo", style);
            createCell(row4, 8,"Longitud", style);
            createCell(row4, 9,"Conciliacion", style);
            createCell(row4, 10,"Nulo", style);
            createCell(row4, 11,"Separador", style);
            createCell(row4, 12,"Formato", style);
        }

        if(dataListVal!=null)
        {
            sheet3 = workbook.createSheet("Validaciones");
            Row row3 = sheet3.createRow(0);

            createCell(row3, 0,"Cod. Concil", style);
            createCell(row3, 1,"Nom. Concil", style);
            createCell(row3, 2,"Cod. Invent", style);
            createCell(row3, 3,"Nom. Invent", style);
            createCell(row3, 4,"Cod. Campo Ref", style);
            createCell(row3, 5,"Nom. Campo Ref", style);
            createCell(row3, 6,"Cod. Campo Val", style);
            createCell(row3, 7,"Nom. Campo Val", style);
            createCell(row3, 8,"Val. Validacion", style);
            createCell(row3, 9,"Operacion", style);
            createCell(row3, 10,"Val. Operacion", style);
        }
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

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        int rowCount = 1;

        if(dataList!=null){
            for(Object[] data: dataList){
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;

                createCell(row,columnCount++,(data[0]!=null ? data[0].toString():""),style);
                createCell(row,columnCount++,(data[1]!=null ? data[1].toString():""),style);
                createCell(row,columnCount++,(data[2]!=null ? data[2].toString():""),style);
                createCell(row,columnCount++,(data[3]!=null ? data[3].toString():""),style);
                createCell(row,columnCount++,(data[4]!=null ? data[4].toString():""),style);
                createCell(row,columnCount++,(data[5]!=null ? data[5].toString():""),style);
                createCell(row,columnCount++,(data[6]!=null ? data[6].toString():""),style);
                createCell(row,columnCount++,((boolean)data[7] ? "Activo":"Inactivo"),style);
            }
        }

        if(dataListCam!=null){
            rowCount = 1;

            for(Object[] data: dataListCam){
                Row row = sheet4.createRow(rowCount++);
                int columnCount = 0;

                createCell(row,columnCount++,(data[0]!=null ? data[0].toString():""),style);
                createCell(row,columnCount++,(data[1]!=null ? data[1].toString():""),style);
                createCell(row,columnCount++,(data[2]!=null ? data[2].toString():""),style);
                createCell(row,columnCount++,(data[3]!=null ? data[3].toString():""),style);
                createCell(row,columnCount++,(data[4]!=null ? data[4].toString():""),style);
                createCell(row,columnCount++,(data[5]!=null ? data[5].toString():""),style);
                createCell(row,columnCount++,((boolean)data[6] ? "Si":"No"),style);
                createCell(row,columnCount++,(data[7]!=null ? data[7].toString():""),style);
                createCell(row,columnCount++,(data[8]!=null ? data[8].toString():""),style);
                createCell(row,columnCount++,((boolean)data[9] ? "Si":"No"),style);
                createCell(row,columnCount++,((boolean)data[10] ? "Si":"No"),style);
                createCell(row,columnCount++,(data[11]!=null && data[7]!=null && data[7].toString().equalsIgnoreCase("Date") ? data[11].toString():""),style);
                createCell(row,columnCount++,(data[12]!=null && data[7]!=null && data[7].toString().equalsIgnoreCase("Date") ? data[12].toString():""),style);

            }
        }

        if(dataListVal!=null){
            rowCount = 1;

            for(Object[] data: dataListVal){
                Row row = sheet3.createRow(rowCount++);
                int columnCount = 0;

                createCell(row,columnCount++,(data[0]!=null ? data[0].toString():""),style);
                createCell(row,columnCount++,(data[1]!=null ? data[1].toString():""),style);
                createCell(row,columnCount++,(data[2]!=null ? data[2].toString():""),style);
                createCell(row,columnCount++,(data[3]!=null ? data[3].toString():""),style);
                createCell(row,columnCount++,(data[4]!=null ? data[4].toString():""),style);
                createCell(row,columnCount++,(data[5]!=null ? data[5].toString():""),style);
                createCell(row,columnCount++,(data[6]!=null ? data[6].toString():""),style);
                createCell(row,columnCount++,(data[7]!=null ? data[7].toString():""),style);
                createCell(row,columnCount++,(data[8]!=null ? data[8].toString():""),style);
                createCell(row,columnCount++,(data[9]!=null && data[11]!=null && data[11].equals(true) ? data[9].toString():""),style);
                createCell(row,columnCount++,(data[10]!=null ? data[10].toString():""),style);
            }
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
