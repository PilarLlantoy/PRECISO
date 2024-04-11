package com.inter.proyecto_intergrupo.service.informationServices;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.information.Sicc;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SiccListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Sicc> siccList;
    private ArrayList<Object[]> noMatch;

    public SiccListReport(List<Sicc> siccList, ArrayList<Object[]> noMatch) {
        this.siccList = siccList;
        this.noMatch = noMatch;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("SICC");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "COD NEOCON", style);
        createCell(row, 1, "DIVISA", style);
        createCell(row, 2, "YNTP", style);
        createCell(row, 3, "SOCIEDADES YNTP", style);
        createCell(row, 4, "CONTRATO", style);
        createCell(row, 5, "NIT Contraparte", style);
        createCell(row, 6, "VALOR", style);
        createCell(row, 7, "COD. PAÍS", style);
        createCell(row, 8, "PAÍS", style);
        createCell(row, 9, "CUENTA LOCAL", style);
        createCell(row, 10, "PERIODO CONTABLE", style);

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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));


        for(Sicc sicc: siccList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,sicc.getCodNeocon(),style);
            createCell(row,columnCount++,sicc.getDivisa(),style);
            createCell(row,columnCount++,sicc.getYntp(),style);
            createCell(row,columnCount++,sicc.getSociedadYntp(),style);
            createCell(row,columnCount++,sicc.getContrato(),style);
            createCell(row,columnCount++,sicc.getNitContraparte(),style);
            createCell(row,columnCount++,Double.parseDouble(sicc.getValor()),style1);
            createCell(row,columnCount++,sicc.getCodPais(),style);
            createCell(row,columnCount++,sicc.getPais(),style);
            createCell(row,columnCount++,sicc.getCuentaLocal(),style);
            createCell(row,columnCount++,sicc.getPeriodoContable(),style);
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


    private void writeHeaderLine2(){
        sheet = workbook.createSheet("Cuentas que no cruzan");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "COD NEOCON", style);
        createCell(row, 1, "DIVISA", style);
        createCell(row, 2, "YNTP", style);
        createCell(row, 3, "SOCIEDADES YNTP", style);
        createCell(row, 4, "CONTRATO", style);
        createCell(row, 5, "NIT Contraparte", style);
        createCell(row, 6, "VALOR", style);
        createCell(row, 7, "COD. PAÍS", style);
        createCell(row, 8, "PAÍS", style);
        createCell(row, 9, "CUENTA LOCAL", style);
        createCell(row, 10, "PERIODO CONTABLE", style);

    }

    private void writeDataLines2(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));


        for(Object[] data: noMatch){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,data[0],style);
            createCell(row,columnCount++,data[1],style);
            createCell(row,columnCount++,data[2],style);
            createCell(row,columnCount++,data[3],style);
            createCell(row,columnCount++,data[4],style);
            createCell(row,columnCount++,data[5],style);
            createCell(row,columnCount++,Double.parseDouble(data[6].toString()),style1);
            createCell(row,columnCount++,data[7],style);
            createCell(row,columnCount++,data[8],style);
            createCell(row,columnCount++,data[9],style);
            createCell(row,columnCount++,data[10],style);
        }
    }



    public void exportSecondReport(HttpServletResponse response) throws IOException{
        writeHeaderLine2();
        writeDataLines2();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }




}
