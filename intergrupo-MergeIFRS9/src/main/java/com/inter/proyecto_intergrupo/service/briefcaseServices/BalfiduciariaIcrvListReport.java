package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.briefcase.BalfiduciariaIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.BalvaloresIcrv;
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

public class BalfiduciariaIcrvListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> dataStringList;
    private List<BalfiduciariaIcrv> listDataObject;

    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public BalfiduciariaIcrvListReport(List<String[]> dataStringList, List<BalfiduciariaIcrv> listDataObject) {
        this.dataStringList = dataStringList;
        this.listDataObject = listDataObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Bal Fiduciaria ICRV");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int consecutive= 0;
        createCell(row, consecutive++, "Corte", style);
        createCell(row, consecutive++, "Código Contabilidad", style);
        createCell(row, consecutive++, "Nombre Fideicomiso", style);
        createCell(row, consecutive++, "Año", style);
        createCell(row, consecutive++, "Mes", style);
        createCell(row, consecutive++, "Código PUC", style);
        createCell(row, consecutive++, "Código Cuenta NIIF", style);
        createCell(row, consecutive++, "Código PUC Local", style);
        createCell(row, consecutive++, "Nombre", style);
        createCell(row, consecutive++, "Saldo Final", style);
        createCell(row, consecutive++, "Nivel", style);
        createCell(row, consecutive++, "Código Consolidación", style);
        createCell(row, consecutive++, "Código Gestión", style);
        createCell(row, consecutive++, "L4", style);
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

        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font2 = workbook.createFont();
        font2.setFontHeight(10);
        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(BalfiduciariaIcrv data: listDataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getCorte(),style);
            createCell(row,columnCount++,data.getCodigoContabilidad(),style);
            createCell(row,columnCount++,data.getNombreFideicomiso(),style);
            createCell(row,columnCount++,data.getAno(),style);
            createCell(row,columnCount++,data.getMes(),style);
            createCell(row,columnCount++,data.getCodigoPuc(),style);
            createCell(row,columnCount++,data.getCodigoCuentaNiif(),style);
            createCell(row,columnCount++,data.getCodigoPucLocal(),style);
            createCell(row,columnCount++,data.getNombre(),style);
            createCell(row,columnCount++,data.getSaldoFinal(),style2);
            createCell(row,columnCount++,data.getNivel(),style);
            createCell(row,columnCount++,data.getCodicons(),style);
            createCell(row,columnCount++,data.getCodigest(),style);
            createCell(row,columnCount++,data.getL4(),style);
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

        createCell(row1, 0, dataStringList.get(dataStringList.size()-1)[0], style1);
        createCell(row1, 1, dataStringList.get(dataStringList.size()-1)[1], style1);
        createCell(row1, 2, dataStringList.get(dataStringList.size()-1)[2], style1);

        if(dataStringList.size()>1)
        {
            dataStringList.remove(dataStringList.size()-1);
            Row row2 = sheet.createRow(2);
            createCell(row2, 0, "Fila", style);
            createCell(row2, 1, "Columna", style);
            createCell(row2, 2, "Estado", style);

            int rowCount = 3;
            for (String[] log : dataStringList)
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
