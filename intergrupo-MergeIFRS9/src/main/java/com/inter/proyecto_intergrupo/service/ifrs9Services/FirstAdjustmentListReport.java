package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.FirstAdjustment;
import com.inter.proyecto_intergrupo.model.ifrs9.IncurredLoss;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class FirstAdjustmentListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private List<FirstAdjustment> firstList;
    private List<Object[]> firstListObject;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public FirstAdjustmentListReport(List<FirstAdjustment> firstList, List<Object[]> firstListObject){
        this.firstList = firstList;
        this.firstListObject = firstListObject;
        workbook = new SXSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Ajuste Primera Vez");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        short short1 = 11;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(short1);
        style.setFont(font);

        createCell(row, 0, "Aplicativo", style);
        createCell(row, 1, "Centro Destino", style);
        createCell(row, 2, "Centro Operante", style);
        createCell(row, 3, "Centro Origen", style);
        createCell(row, 4, "Ceros", style);
        createCell(row, 5, "Clave de Interfaz", style);
        createCell(row, 6, "Código de consolidación", style);
        createCell(row, 7, "Contrato", style);
        createCell(row, 8, "Correctora", style);
        createCell(row, 9, "Cuenta", style);
        createCell(row, 10, "Cuenta Gasto", style);
        createCell(row, 11, "Cuenta Ingreso", style);
        createCell(row, 12, "Descripción", style);
        createCell(row, 13, "Diferencia pesos", style);
        createCell(row, 14, "Empresa", style);
        createCell(row, 15, "Fecha Contable", style);
        createCell(row, 16, "Fecha Proceso", style);
        createCell(row, 17, "Importe Pesos Debe", style);
        createCell(row, 18, "Importe Pesos Haber", style);
        createCell(row, 19, "Importe Divisa Debe", style);
        createCell(row, 20, "Importe Divisa Haber", style);
        createCell(row, 21, "Numero Movimientos Debe", style);
        createCell(row, 22, "Numero Movimientos Haber", style);
        createCell(row, 23, "Referencia", style);
        createCell(row, 24, "Ristra", style);
        createCell(row, 25, "Segmento", style);
        createCell(row, 26, "Stage", style);
    }

    private void writeHeaderLineResume(){
        sheet = workbook.createSheet("Ajuste Primera Vez");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        short short1 = 11;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(short1);
        style.setFont(font);

        createCell(row, 0, "Cuenta", style);
        createCell(row, 1, "Centro Origen", style);
        createCell(row, 2, "Importe Pesos Debe", style);
        createCell(row, 3, "Importe Pesos Haber", style);
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
        short short1 = 10;
        short short2 = 10;
        Font font = workbook.createFont();
        Font font1 = workbook.createFont();
        font.setFontHeightInPoints(short1);
        font1.setFontHeightInPoints(short2);
        style.setFont(font);
        style1.setFont(font1);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(FirstAdjustment first: firstList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,first.getAplicativo(),style);
            createCell(row,columnCount++,first.getCentrodestino(),style);
            createCell(row,columnCount++,first.getCentrooperante(),style);
            createCell(row,columnCount++,first.getCentroorigen(),style);
            createCell(row,columnCount++,first.getCeros(),style);
            createCell(row,columnCount++,first.getClaveinterfaz(),style);
            createCell(row,columnCount++,first.getCodigoconsolidacion(),style);
            createCell(row,columnCount++,first.getContrato(),style);
            createCell(row,columnCount++,first.getCorrectora(),style);
            createCell(row,columnCount++,first.getCuenta(),style);
            createCell(row,columnCount++,first.getCuentagasto(),style);
            createCell(row,columnCount++,first.getCuentaingreso(),style);
            createCell(row,columnCount++,first.getDescripcion(),style);
            createCell(row,columnCount++,first.getDiferenciaPesos(),style1);
            createCell(row,columnCount++,first.getEmpresa(),style);
            createCell(row,columnCount++,first.getFechacontable(),style);
            createCell(row,columnCount++,first.getFechaproceso(),style);
            createCell(row,columnCount++,first.getImportepesosdebe(),style1);
            createCell(row,columnCount++,first.getImportepesoshaber(),style1);
            createCell(row,columnCount++,first.getImportedivisadebe(),style1);
            createCell(row,columnCount++,first.getImportedivisahaber(),style1);
            createCell(row,columnCount++,first.getNumeromovdebe(),style);
            createCell(row,columnCount++,first.getNumeromovhaber(),style);
            createCell(row,columnCount++,first.getReferencia(),style);
            createCell(row,columnCount++,first.getRistra(),style);
            createCell(row,columnCount++,first.getSegmento(),style);
            createCell(row,columnCount++,first.getStage(),style);
        }
    }

    private void writeDataLinesResume(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        short short1 = 10;
        short short2 = 10;
        Font font = workbook.createFont();
        Font font1 = workbook.createFont();
        font.setFontHeightInPoints(short1);
        font1.setFontHeightInPoints(short2);
        style.setFont(font);
        style1.setFont(font1);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] loss: firstListObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            if(loss[0]!=null)
            createCell(row,columnCount++,loss[0].toString(),style);
            if(loss[1]!=null)
            createCell(row,columnCount++,loss[1].toString(),style);
            if(loss[2]!=null)
            createCell(row,columnCount++,Double.parseDouble(loss[2].toString()),style1);
            if(loss[3]!=null)
            createCell(row,columnCount++,Double.parseDouble(loss[3].toString()),style1);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        if(firstList.size()>0) {
            writeHeaderLine();
            writeDataLines();
        }
        else
        {
            sheet = workbook.createSheet("Ajuste Primera Vez");
            sheet.setRandomAccessWindowSize(1000);
            Row row = sheet.createRow(0);

            short short1 = 11;
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints(short1);
            style.setFont(font);

            createCell(row, 0, "La cantidad de registros de descarga no es soportado por Excel ", style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportResume(HttpServletResponse response) throws IOException {
        writeHeaderLineResume();
        writeDataLinesResume();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }


}
