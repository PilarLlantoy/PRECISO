package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.Country;
import org.apache.logging.log4j.spi.ObjectThreadContextMap;
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

public class MatrixEventListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet2;
    private XSSFSheet sheet3;
    private List<Object[]> dataList;
    private List<Object[]> dataListVal;
    private List<Object[]> dataListCon;

    public MatrixEventListReport(List<Object[]> dataList,List<Object[]> dataListVal,List<Object[]> dataListCon){
        this.dataList = dataList;
        this.dataListVal = dataListVal;
        this.dataListCon = dataListCon;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        sheet = workbook.createSheet("Encabezados");
        Row row = sheet.createRow(0);

        createCell(row, 0,"Cod. Concil", style);
        createCell(row, 1,"Nom. Concil", style);
        createCell(row, 2,"Cod. Invent", style);
        createCell(row, 3,"Nom. Invent", style);
        createCell(row, 4,"Cod. Matriz", style);
        createCell(row, 5,"Estado", style);
        createCell(row, 6,"Cuenta 1", style);
        createCell(row, 7,"Cuenta 1 PYG", style);
        createCell(row, 8,"Cuenta 2", style);
        createCell(row, 9,"Cuenta 2 PYG", style);
        createCell(row, 10,"Tipo Evento", style);

        sheet2 = workbook.createSheet("Condiciones");
        Row row2 = sheet2.createRow(0);

        createCell(row2, 0,"Cod. Concil", style);
        createCell(row2, 1,"Nom. Concil", style);
        createCell(row2, 2,"Cod. Invent", style);
        createCell(row2, 3,"Nom. Invent", style);
        createCell(row2, 4,"Cod. Matriz", style);
        createCell(row2, 5,"Estado", style);
        createCell(row2, 6,"Cod. Campo", style);
        createCell(row2, 7,"Nom. Campo", style);
        createCell(row2, 8,"Condicion", style);
        createCell(row2, 9,"Val. Condic", style);

        sheet3 = workbook.createSheet("Validaciones");
        Row row3 = sheet3.createRow(0);

        createCell(row3, 0,"Cod. Concil", style);
        createCell(row3, 1,"Nom. Concil", style);
        createCell(row3, 2,"Cod. Invent", style);
        createCell(row3, 3,"Nom. Invent", style);
        createCell(row3, 4,"Cod. Matriz", style);
        createCell(row3, 5,"Estado", style);
        createCell(row3, 6,"Campo Inv. Apl.", style);
        createCell(row3, 7,"Apl. Formula", style);
        createCell(row3, 8,"Cod. Campo Val.", style);
        createCell(row3, 9,"Nom. Campo Val.", style);
        createCell(row3, 10,"Val. Inv. Val.", style);
        createCell(row3, 11,"Tipo Operacion", style);
        createCell(row3, 12,"Val. Ope. Res.", style);

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

        for(Object[] data: dataList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,(data[0]!=null ? data[0].toString():""),style);
            createCell(row,columnCount++,(data[1]!=null ? data[1].toString():""),style);
            createCell(row,columnCount++,(data[2]!=null ? data[2].toString():""),style);
            createCell(row,columnCount++,(data[3]!=null ? data[3].toString():""),style);
            createCell(row,columnCount++,(data[4]!=null ? data[4].toString():""),style);
            createCell(row,columnCount++,((boolean)data[5] ? "Activo":"Inactivo"),style);
            createCell(row,columnCount++,(data[6]!=null ? data[6].toString():""),style);
            createCell(row,columnCount++,(data[7]!=null ? data[7].toString():""),style);
            createCell(row,columnCount++,(data[8]!=null ? data[8].toString():""),style);
            createCell(row,columnCount++,(data[9]!=null ? data[9].toString():""),style);
            createCell(row,columnCount++,(data[10]!=null ? data[10].toString():""),style);

        }

        rowCount = 1;

        for(Object[] data: dataListCon){
            Row row = sheet2.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,(data[0]!=null ? data[0].toString():""),style);
            createCell(row,columnCount++,(data[1]!=null ? data[1].toString():""),style);
            createCell(row,columnCount++,(data[2]!=null ? data[2].toString():""),style);
            createCell(row,columnCount++,(data[3]!=null ? data[3].toString():""),style);
            createCell(row,columnCount++,(data[4]!=null ? data[4].toString():""),style);
            createCell(row,columnCount++,((boolean)data[5] ? "Activo":"Inactivo"),style);
            createCell(row,columnCount++,(data[6]!=null ? data[6].toString():""),style);
            createCell(row,columnCount++,(data[7]!=null ? data[7].toString():""),style);
            createCell(row,columnCount++,(data[8]!=null ? data[8].toString():""),style);
            createCell(row,columnCount++,(data[9]!=null ? data[9].toString():""),style);

        }

        rowCount = 1;

        for(Object[] data: dataListVal){
            Row row = sheet3.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,(data[0]!=null ? data[0].toString():""),style);
            createCell(row,columnCount++,(data[1]!=null ? data[1].toString():""),style);
            createCell(row,columnCount++,(data[2]!=null ? data[2].toString():""),style);
            createCell(row,columnCount++,(data[3]!=null ? data[3].toString():""),style);
            createCell(row,columnCount++,(data[4]!=null ? data[4].toString():""),style);
            createCell(row,columnCount++,((boolean)data[5] ? "Activo":"Inactivo"),style);
            createCell(row,columnCount++,(data[6]!=null ? data[6].toString():""),style);
            createCell(row,columnCount++,(data[7].equals(1) ? "Si":"No"),style);
            createCell(row,columnCount++,(data[8]!=null ? data[8].toString():""),style);
            createCell(row,columnCount++,(data[9]!=null ? data[9].toString():""),style);
            createCell(row,columnCount++,(data[10]!=null ? data[10].toString():""),style);
            createCell(row,columnCount++,(data[11]!=null ? data[11].toString():""),style);
            createCell(row,columnCount++,(data[12]!=null ? data[12].toString():""),style);

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
