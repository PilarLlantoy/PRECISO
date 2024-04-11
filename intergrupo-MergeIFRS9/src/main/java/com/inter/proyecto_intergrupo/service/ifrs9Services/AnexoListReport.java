package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Anexo;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnexoListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Anexo> anexoList;
    private List<Object[]> noMatch;

    public AnexoListReport(List<Anexo> anexoList, List<Object[]> noMatch) {
        this.anexoList = anexoList;
        this.noMatch = noMatch;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Anexo");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "identificacion", style);
        createCell(row, 1, "divisa", style);
        createCell(row, 2, "cuenta", style);
        createCell(row, 3, "empresa", style);
        createCell(row, 4, "contrato", style);
        createCell(row, 5, "aplicativo", style);
        createCell(row, 6, "centro", style);
        createCell(row, 7, "tipo", style);
        createCell(row, 8, "digito_verif", style);
        createCell(row, 9, "nombre", style);
        createCell(row, 10, "forigen", style);
        createCell(row, 11, "fcierr", style);
        createCell(row, 12, "saldo", style);
        createCell(row, 13, "periodo", style);

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

    private void writeDataLines() throws ParseException {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd");


        for(Anexo anexo: anexoList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            Date fechaDateO = formato.parse(anexo.getForigen());
            Date fechaDateC = formato.parse(anexo.getFcierr());

            createCell(row,columnCount++,anexo.getIdent(),style);
            createCell(row,columnCount++,anexo.getDivisa(),style);
            createCell(row,columnCount++,anexo.getCuenta(),style);
            createCell(row,columnCount++,anexo.getEmpresa(),style);
            createCell(row,columnCount++,anexo.getContrato(),style);
            createCell(row,columnCount++,anexo.getAplicativo(),style);
            createCell(row,columnCount++,anexo.getCentro(),style);
            createCell(row,columnCount++,anexo.getTipo(),style);
            createCell(row,columnCount++,anexo.getDigitover(),style);
            createCell(row,columnCount++,anexo.getNombre(),style);
            createCell(row,columnCount++,formateador.format(fechaDateO),style);
            createCell(row,columnCount++,formateador.format(fechaDateC),style);
            createCell(row,columnCount++,anexo.getSaldo(),style1);
            createCell(row,columnCount++,anexo.getPerido(),style1);
        }
    }

    public void export(HttpServletResponse response) throws IOException, ParseException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportAccount(HttpServletResponse response) throws IOException {
        writeHeaderLineAccount();
        writeDataLinesAccount();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    private void writeHeaderLineAccount(){
        sheet = workbook.createSheet("Validación Query");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);
        int columnCount = 0;

        createCell(row, columnCount++, "Cuenta Contable", style);
        createCell(row, columnCount++, "Divisa", style);
        createCell(row, columnCount++, "Saldo Pesos Anexo 8", style);
        createCell(row, columnCount++, "Saldo Pesos Query", style);
        createCell(row, columnCount++, "Diferencia Pesos", style);
        createCell(row, columnCount++, "Fecha proceso Query", style);

    }
    private void writeDataLinesAccount(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] acc: noMatch){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,acc[0].toString(),style);
            if(acc[1]!=null)
                createCell(row,columnCount++,acc[1].toString(),style);
            else
                createCell(row,columnCount++,"",style);
            createCell(row,columnCount++,Double.parseDouble(acc[2].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(acc[3].toString()),style1);
            createCell(row,columnCount++,Double.parseDouble(acc[4].toString()),style1);
            if(acc[5]!=null)
                createCell(row,columnCount++,acc[5].toString().replace("00:00:00.0",""),style);

        }
    }
}
