package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.FitDifferences;
import com.inter.proyecto_intergrupo.model.ifrs9.FitDifferences;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FitDifferencesListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    private SXSSFSheet sheet1;
    private SXSSFSheet sheet2;
    private List<FitDifferences> fitList;
    private List<Object[]> fitListDatbase140;
    private List<Object[]> fitListMatch;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public FitDifferencesListReport(List<FitDifferences> fitList, List<Object[]> fitListDatbase140, List<Object[]> fitListMatch){
        this.fitList = fitList;
        this.fitListDatbase140 = fitListDatbase140;
        this.fitListMatch = fitListMatch;
        workbook = new SXSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("FICHERO DE CARGA");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        short short1 = 11;
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(short1);
        style.setFont(font);

        createCell(row, 0	,"Neocon", style);
        createCell(row, 1	,"Fichero Saldos IFRS9", style);
        createCell(row, 2	,"Ficha Saldos Inicial", style);
        createCell(row, 3	,"Diferencias IFRS9 - PI", style);
        createCell(row, 4	,"Saldo EEFF", style);
        createCell(row, 5	,"Diferencia IFRS9 vs EEFF", style);
        createCell(row, 6	,"Porcentaje EEFF", style);
        createCell(row, 7	,"Saldo Conciliación", style);
        createCell(row, 8	,"Diferencia IFRS9 vs Conciliación", style);
        createCell(row, 9	,"Porcentaje Conciliación", style);
        createCell(row, 10	,"Diferencia EEFF vs Conciliación", style);
        createCell(row, 11	,"Periodo", style);

        sheet1 = workbook.createSheet("DETALLE CTA LOCAL");
        sheet1.setRandomAccessWindowSize(1000);
        Row row1 = sheet1.createRow(0);

        createCell(row1, 0	,"Neocon", style);
        createCell(row1, 1	,"Cuenta contable", style);
        createCell(row1, 2	,"Nombre Cuenta", style);
        createCell(row1, 3	,"Divisa", style);
        createCell(row1, 4	,"Saldo Aplicativo", style);
        createCell(row1, 5	,"Saldo Contable", style);
        createCell(row1, 6	,"Diferencia", style);
        createCell(row1, 7	,"Fecha Contable", style);

        sheet2 = workbook.createSheet("DETALLE CODCONS");
        sheet2.setRandomAccessWindowSize(1000);
        Row row2 = sheet2.createRow(0);

        createCell(row2, 0	,"Neocon", style);
        createCell(row2, 1	,"Nombre Neocon", style);
        createCell(row2, 2	,"Diferencia Motor", style);
        createCell(row2, 3	,"Diferencia 140", style);
        createCell(row2, 4	,"Diferencias", style);
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

        for(FitDifferences first: fitList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,first.getNeocon(),style);
            createCell(row,columnCount++,first.getFicheroSaldosIFRS9(),style1);
            createCell(row,columnCount++,first.getFichaSaldosInicial(),style1);
            createCell(row,columnCount++,first.getDiferenciasIFRS9PI(),style1);
            createCell(row,columnCount++,first.getSaldoEEFF(),style1);
            createCell(row,columnCount++,first.getDiferenciaIFRS9EEFF(),style1);
            createCell(row,columnCount++,first.getPorcentajeEEFF(),style);
            createCell(row,columnCount++,first.getSaldoConciliacion(),style1);
            createCell(row,columnCount++,first.getDiferenciaIFRS9Conciliacion(),style1);
            createCell(row,columnCount++,first.getPorcentajeConciliacion(),style);
            createCell(row,columnCount++,first.getDiferenciaEEFFConciliacion(),style1);
            createCell(row,columnCount++,first.getPeriodo(),style);
        }

        rowCount = 1;

        for(Object[] first: fitListDatbase140){
            Row row = sheet1.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,first[0].toString(),style);

            if(first[1] != null && first[1].toString()!=null)
                createCell(row,columnCount++,first[1].toString(),style);
            else
                createCell(row,columnCount++,"",style);
            if(first[2] != null && first[2].toString()!=null)
                createCell(row,columnCount++,first[2].toString(),style);
            else
                createCell(row,columnCount++,"",style);
            if(first[3] != null && first[3].toString()!=null)
                createCell(row,columnCount++,first[3].toString(),style);
            else
                createCell(row,columnCount++,"",style);
            if(first[4] != null && first[4].toString()!=null)
                createCell(row,columnCount++,Double.parseDouble(first[4].toString()),style1);
            else
                createCell(row,columnCount++,0,style1);
            if(first[5] != null && first[5].toString()!=null)
                createCell(row,columnCount++,Double.parseDouble(first[5].toString()),style1);
            else
                createCell(row,columnCount++,0,style1);
            if(first[6] != null && first[6].toString()!=null)
                createCell(row,columnCount++,Double.parseDouble(first[6].toString()),style1);
            else
                createCell(row,columnCount++,0,style1);
            if(first[7] != null && first[7].toString()!=null)
                createCell(row,columnCount++,first[7].toString(),style);
            else
                createCell(row,columnCount++,"",style);
        }

        rowCount = 1;

        for(Object[] first: fitListMatch){
            Row row = sheet2.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,first[0].toString(),style);

            if(first[1] != null && first[1].toString()!=null)
                createCell(row,columnCount++,first[1].toString(),style);
            else
                createCell(row,columnCount++,"",style1);
            if(first[2] != null && first[2].toString()!=null)
                createCell(row,columnCount++,Double.parseDouble(first[2].toString()),style1);
            else
                createCell(row,columnCount++,0,style1);
            if(first[3] != null && first[3].toString()!=null)
                createCell(row,columnCount++,Double.parseDouble(first[3].toString()),style1);
            else
                createCell(row,columnCount++,0,style1);
            if(first[4] != null && first[4].toString()!=null)
                createCell(row,columnCount++,Double.parseDouble(first[4].toString()),style1);
            else
                createCell(row,columnCount++,0,style1);
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

    public void exportLog(HttpServletResponse response, List<String[]> lista)
    {
        int position=0;
        String[] temporalListNames =lista.get(lista.size()-1);
        List<String> list=new ArrayList<>();

        try {
            exportSubLog(response, lista,position);
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(response.getOutputStream());
            workbook.close();
            outputStream.flush();
            outputStream.close();
        }
        catch (IOException e){

        }
    }

    public void exportSubLog(HttpServletResponse response, List<String[]> lista, int position) throws IOException
    {
        sheet = workbook.createSheet("Log");
        Row row1 = sheet.createRow(1);
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

        createCell(row1, 0, "Fila", style);
        createCell(row1, 1, "Columna", style);
        createCell(row1, 2, "Estado", style);

        Row row2 = sheet.createRow(0);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,2));

        int rowCount = 2;

        for(int i =0; i<lista.size()-1 ; i++)
        {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,lista.get(i)[0],style);
            createCell(row,columnCount++,lista.get(i)[1],style);
            createCell(row,columnCount++,lista.get(i)[3],style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

    }
}
