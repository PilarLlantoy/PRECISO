package com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.GenericsParametric;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenericsParametricListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    List<String[]> genericsLog;
    ArrayList<GenericsParametric> genericsParametricList;


    public GenericsParametricListReport(ArrayList<GenericsParametric> genericsParametricList,List<String[]> genericsLog){
        workbook = new XSSFWorkbook();
        this.genericsParametricList = genericsParametricList;
        this.genericsLog = genericsLog;
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
        sheet = workbook.createSheet("Parametría Genéricas");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Fuente", style);
        createCell(row, 1, "TP", style);
        createCell(row, 2, "Indicador", style);
        createCell(row, 3, "Cartera", style);
        createCell(row, 4, "Clase", style);
        createCell(row, 5, "Calificacion", style);
        createCell(row, 6, "Empresa", style);
        createCell(row, 7, "Cuenta", style);
        createCell(row, 8, "1", style);
        createCell(row, 9, "2", style);
        createCell(row, 10, "3", style);
        createCell(row, 11, "4", style);
        createCell(row, 12, "5", style);
        createCell(row, 13, "6", style);
        createCell(row, 14, "Nombre Cuenta", style);
        createCell(row, 15, "Porcentaje Calculo", style);
        createCell(row, 16, "Código IFRS9", style);
    }

    private void writeDataLinesReport(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(GenericsParametric gen: genericsParametricList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,gen.getFuenteInfo(),style);
            createCell(row,columnCount++,gen.getTp(),style);
            createCell(row,columnCount++,gen.getIndicador(),style);
            createCell(row,columnCount++,gen.getCartera(),style);
            createCell(row,columnCount++,gen.getClase(),style);
            createCell(row,columnCount++,gen.getCalificacion(),style);
            createCell(row,columnCount++,gen.getEmpresa(),style);
            createCell(row,columnCount++,gen.getCuenta(),style);
            createCell(row,columnCount++,gen.getUno(),style);
            createCell(row,columnCount++,gen.getDos(),style);
            createCell(row,columnCount++,gen.getTres(),style);
            createCell(row,columnCount++,gen.getCuatro(),style);
            createCell(row,columnCount++,gen.getCinco(),style);
            createCell(row,columnCount++,gen.getSeis(),style);
            createCell(row,columnCount++,gen.getNombreCuenta(),style);
            createCell(row,columnCount++,gen.getPorcentajeCalc(),style);
            createCell(row,columnCount++,gen.getCodigoIfrs9(),style);
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
        sheet = workbook.createSheet("Log_Plantilla");
        Row row1 = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "Fila", style);
        createCell(row1, 1, "Columna", style);
        createCell(row1, 2, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        if(genericsLog.size()>0)
            genericsLog.remove(genericsLog.size()-1);
        for(String[] log: genericsLog){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style);
            createCell(row,columnCount++,log[1],style);
            createCell(row,columnCount++,log[2],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}
