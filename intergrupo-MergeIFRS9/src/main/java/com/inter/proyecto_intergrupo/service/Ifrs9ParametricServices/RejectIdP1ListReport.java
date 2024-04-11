package com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RejectionIdP1;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RejectIdP1ListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<RejectionIdP1> rejectionIdP1List;
    private List<Object[]> rejectionIdP1ListObject;

    public RejectIdP1ListReport(List<RejectionIdP1> rejectionIdP1List){
        this.rejectionIdP1List = rejectionIdP1List;
        workbook = new XSSFWorkbook();

    }

    public RejectIdP1ListReport(List<Object[]> rejectionIdP1ListObject, int number){
        this.rejectionIdP1ListObject = rejectionIdP1ListObject;
        workbook = new XSSFWorkbook();

    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Identificación Cuenta");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "INICIAL CUENTA", style);
        createCell(row,	1, "ASIGNACIÓN", style);
        createCell(row,	2, "TIPO CUENTA", style);
        createCell(row,	3, "INICIAL LINEA", style);
        createCell(row,	4, "CANTIDAD LINEA", style);
        createCell(row,	5, "INICIAL SEGMENTO", style);
        createCell(row,	6, "CANTIDAD SEGMENTO", style);
        createCell(row,	7, "INICIAL STAGE", style);
        createCell(row,	8, "CANTIDAD STAGE", style);
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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(RejectionIdP1 reject: rejectionIdP1List){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            createCell(row,columnCount++,	reject.getInicialCuenta()	,style);
            createCell(row,columnCount++,	reject.getAsignacion()	,style);
            createCell(row,columnCount++,	reject.getTipoCuenta()	,style);
            createCell(row,columnCount++,	reject.getLineaInicial()	,style);
            createCell(row,columnCount++,	reject.getLineaCantidad()	,style);
            createCell(row,columnCount++,	reject.getSegmentoInicial()	,style);
            createCell(row,columnCount++,	reject.getSegmentoCantidad()	,style);
            createCell(row,columnCount++,	reject.getStageInicial()	,style);
            createCell(row,columnCount++,	reject.getStageCantidad()	,style);
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
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "Fila", style);
        createCell(row1, 1, "Columna", style);
        createCell(row1, 2, "Estado", style);

        Row row2 = sheet.createRow(0);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

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
