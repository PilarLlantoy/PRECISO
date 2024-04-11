package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.GeneralInterestProvision;
import com.inter.proyecto_intergrupo.model.ifrs9.OnePercent;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OnePercentListReport {
    private final SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    ArrayList<OnePercent> dataList;
    List<String[]> provLog;
    private static final short size1 = 11;
    private static final short size2 = 10;


    public OnePercentListReport(ArrayList<OnePercent> dataList, List<String[]> provLog){
        this.workbook = new SXSSFWorkbook();
        this.dataList = dataList;
        this.provLog = provLog;
    }

    public OnePercentListReport() {
        this.workbook = new SXSSFWorkbook();
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

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Tablón 1%");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size1);
        style.setFont(font);

        createCell(row, 0, "Cod Necon", style);
        createCell(row, 1, "Cartera", style);
        createCell(row, 2, "Oficina", style);
        createCell(row, 3, "Calificación", style);
        createCell(row, 4, "Nucta", style);
        createCell(row, 5, "Epigrafe", style);
        createCell(row, 6, "Fecha Corte", style);
        createCell(row, 7, "Fecha Creación", style);
        createCell(row, 8, "Versión 0", style);
        createCell(row, 9, "Versión 1", style);
        createCell(row, 10, "Versión 2", style);
        createCell(row, 11, "Versión 3", style);
        createCell(row, 12, "Versión 4", style);
        createCell(row, 13, "Versión 5", style);
        createCell(row, 14, "Versión 6", style);
        createCell(row, 15, "Versión 7", style);
        createCell(row, 16, "Versión 8", style);
        createCell(row, 17, "Variación 1", style);
        createCell(row, 18, "Variación 2", style);
        createCell(row, 19, "Variación 3", style);
        createCell(row, 20, "Variación 4", style);
        createCell(row, 21, "Variación 5", style);
        createCell(row, 22, "Variación 6", style);
        createCell(row, 23, "Variación 7", style);
        createCell(row, 24, "Variación 8", style);
        createCell(row, 25, "Cálculo 1", style);
        createCell(row, 26, "Cálculo 2", style);
        createCell(row, 27, "Cálculo 3", style);
        createCell(row, 28, "Cálculo 4", style);
        createCell(row, 29, "Cálculo 5", style);
        createCell(row, 30, "Cálculo 6", style);
        createCell(row, 31, "Cálculo 7", style);
        createCell(row, 32, "Cálculo 8", style);
        createCell(row, 33, "Porcentaje Calculado", style);
        createCell(row, 34, "Cuenta Balance", style);
        createCell(row, 35, "Cuenta PyG", style);
        createCell(row, 36, "Fuente de Información", style);

    }

    private void writeDataLinesReport(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontHeightInPoints(size2);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(OnePercent data: dataList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getCodNeocon(),style);
            createCell(row,columnCount++,data.getCartera(),style);
            createCell(row,columnCount++,data.getOficina(),style);
            createCell(row,columnCount++,data.getCalificacion(),style);
            createCell(row,columnCount++,data.getNucta(),style);
            createCell(row,columnCount++,data.getEpigrafe(),style);
            createCell(row,columnCount++,data.getFechaCorte(),style);
            createCell(row,columnCount++,data.getFechaCreacion(),style);
            createCell(row,columnCount++,data.getVersion0(),style1);
            createCell(row,columnCount++,data.getVersion1(),style1);
            createCell(row,columnCount++,data.getVersion2(),style1);
            createCell(row,columnCount++,data.getVersion3(),style1);
            createCell(row,columnCount++,data.getVersion4(),style1);
            createCell(row,columnCount++,data.getVersion5(),style1);
            createCell(row,columnCount++,data.getVersion6(),style1);
            createCell(row,columnCount++,data.getVersion7(),style1);
            createCell(row,columnCount++,data.getVersion8(),style1);
            createCell(row,columnCount++,data.getVariacion1(),style1);
            createCell(row,columnCount++,data.getVariacion2(),style1);
            createCell(row,columnCount++,data.getVariacion3(),style1);
            createCell(row,columnCount++,data.getVariacion4(),style1);
            createCell(row,columnCount++,data.getVariacion5(),style1);
            createCell(row,columnCount++,data.getVariacion6(),style1);
            createCell(row,columnCount++,data.getVariacion7(),style1);
            createCell(row,columnCount++,data.getVariacion8(),style1);
            createCell(row,columnCount++,data.getCalculo1(),style1);
            createCell(row,columnCount++,data.getCalculo2(),style1);
            createCell(row,columnCount++,data.getCalculo3(),style1);
            createCell(row,columnCount++,data.getCalculo4(),style1);
            createCell(row,columnCount++,data.getCalculo5(),style1);
            createCell(row,columnCount++,data.getCalculo6(),style1);
            createCell(row,columnCount++,data.getCalculo7(),style1);
            createCell(row,columnCount++,data.getCalculo8(),style1);
            createCell(row,columnCount++,data.getPorcentajeCalc(),style);
            createCell(row,columnCount++,data.getCuentaBalance(),style);
            createCell(row,columnCount++,data.getCuentaPyG(),style);
            createCell(row,columnCount++,data.getFuenteInfo(),style);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLinesReport();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }


    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Provisiones");
        Row row1 = sheet.createRow(0);
        sheet.setRandomAccessWindowSize(1000);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size1);
        style.setFont(font);

        createCell(row1, 0, "Calificación", style);
        createCell(row1, 1, "Cartera", style);
        createCell(row1, 2, "Error", style);

        int rowCount = 1;

        CellStyle style2 = workbook.createCellStyle();
        Font font2 = workbook.createFont();
        font2.setBold(false);
        font2.setFontHeightInPoints(size2);
        style.setFont(font2);
            provLog.remove(provLog.size()-1);
        for(String[] log: provLog){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style2);
            createCell(row,columnCount++,log[1],style2);
            createCell(row,columnCount++,log[2],style2);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLogParam(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Provisiones");
        Row row1 = sheet.createRow(0);
        sheet.setRandomAccessWindowSize(1000);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size1);
        style.setFont(font);

        createCell(row1, 0, "TP", style);
        createCell(row1, 1, "Cartera", style);
        createCell(row1, 2, "Calificación", style);
        createCell(row1, 3, "Código IFRS9", style);
        createCell(row1, 4, "Mensaje", style);

        int rowCount = 1;

        CellStyle style2 = workbook.createCellStyle();
        Font font2 = workbook.createFont();
        font2.setBold(false);
        font2.setFontHeightInPoints(size2);
        style.setFont(font2);

        for(String[] log: provLog){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style2);
            createCell(row,columnCount++,log[1],style2);
            createCell(row,columnCount++,log[2],style2);
            createCell(row,columnCount++,log[3],style2);
            createCell(row,columnCount++,log[4],style2);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    private void writeHeaderLineMassive() {
        sheet = workbook.createSheet("Carga Masiva");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size1);
        style.setFont(font);

        createCell(row, 0, "CENTRO DE COSTO", style);
        createCell(row, 1, "CUENTA", style);
        createCell(row, 2, "DIVISA", style);
        createCell(row, 3, "CONTRATO", style);
        createCell(row, 4, "REFERENCIA CRUCE", style);
        createCell(row, 5, "IMPORTE", style);
        createCell(row, 6, "DESCRIPCIÓN", style);
        createCell(row, 7, "FECHA", style);
        createCell(row, 8, "TIPO DE DOCUMENTO", style);
        createCell(row, 9, "NÚMERO DE DOCUMENTO", style);
        createCell(row, 10, "DÍGITO DE VERIFICACIÓN", style);
        createCell(row, 11, "TIPO DE PERDIDA", style);
        createCell(row, 12, "CLASE DE RIESGO", style);
        createCell(row, 13, "TIPO DE MOVIMIENTO", style);
        createCell(row, 14, "PRODUCTO", style);
        createCell(row, 15, "PROCESO", style);
        createCell(row, 16, "LÍNEA OPERATIVA", style);
        createCell(row, 17, "VALOR BASE", style);
    }

    private void writeDataLinesReportMassive(List<Object[]> data) {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontHeightInPoints(size2);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] log: data){
            if(Double.parseDouble(log[5].toString())!=0){
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;
                createCell(row,columnCount++,log[0].toString(),style);
                createCell(row,columnCount++,log[1].toString(),style);
                createCell(row,columnCount++,log[2].toString(),style);
                createCell(row,columnCount++,log[3].toString(),style);
                createCell(row,columnCount++,log[4].toString(),style);
                createCell(row,columnCount++,Double.parseDouble(log[5].toString()),style1);
                createCell(row,columnCount++,log[6].toString(),style);
                createCell(row,columnCount++,log[7].toString(),style);
                createCell(row,columnCount++,log[8].toString(),style);
                createCell(row,columnCount++,log[9].toString(),style);
                createCell(row,columnCount++,log[10].toString(),style);
                createCell(row,columnCount++,log[11].toString(),style);
                createCell(row,columnCount++,log[12].toString(),style);
                createCell(row,columnCount++,log[13].toString(),style);
                createCell(row,columnCount++,log[14].toString(),style);
                createCell(row,columnCount++,log[15].toString(),style);
                createCell(row,columnCount++,log[16].toString(),style);
                createCell(row,columnCount++,Double.parseDouble("0.00"),style1);
            }
        }

    }

    public void exportMassive(List<Object[]> data, HttpServletResponse response) throws IOException {
        writeHeaderLineMassive();
        writeDataLinesReportMassive(data);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}
