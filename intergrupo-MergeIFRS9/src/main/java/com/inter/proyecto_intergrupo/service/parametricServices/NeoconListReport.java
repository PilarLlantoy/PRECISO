package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.Neocon;
import com.inter.proyecto_intergrupo.model.parametric.Subsidiaries;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NeoconListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Neocon> neoconList;

    public NeoconListReport(List<Neocon> neoconList){
        this.neoconList = neoconList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Cuentas Neocon");
        Row row = sheet.createRow(3);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);


        createCell(row, 0, "Plan de Cuentas", style);
        createCell(row, 1, "Código Jerarquico", style);
        createCell(row, 2, "Cuenta", style);
        createCell(row, 3, "Descripción", style);
        createCell(row, 4, "Entrada", style);
        createCell(row, 5, "Mínimo", style);
        createCell(row, 6, "Naturaleza", style);
        createCell(row, 7, "Intergrupo", style);
        createCell(row, 8, "GrScIng", style);
        createCell(row, 9, "Epigraf", style);
        createCell(row, 10, "Residencia", style);
        createCell(row, 11, "Bancaria", style);
        createCell(row, 12, "Form", style);
        createCell(row, 13, "Tdes", style);
        createCell(row, 14, "Soporte/Derivada", style);
        createCell(row, 15, "Unid", style);
        createCell(row, 16, "Tipo Cambio", style);
        createCell(row, 17, "Agregación", style);
        createCell(row, 18, "Tipo Divisa", style);
        createCell(row, 19, "Tipo País", style);
        createCell(row, 20, "Contrap", style);
        createCell(row, 21, "Timp", style);
        createCell(row, 22, "Conciliación", style);
        createCell(row, 24, "Perímetro IFRS9", style);
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
        int rowCount = 4;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(Neocon neocon: neoconList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,neocon.getPlanDeCuentas(),style);
            createCell(row,columnCount++,neocon.getCodigoJerarquico(),style);
            createCell(row,columnCount++,neocon.getCuenta(),style);
            createCell(row,columnCount++,neocon.getDescripcion(),style);
            createCell(row,columnCount++,neocon.getEntrada(),style);
            createCell(row,columnCount++,neocon.getMinimo(),style);
            createCell(row,columnCount++,neocon.getNaturaleza(),style);
            createCell(row,columnCount++,neocon.getIntergrupo(),style);
            createCell(row,columnCount++,neocon.getGrScIng(),style);
            createCell(row,columnCount++,neocon.getEpigraf(),style);
            createCell(row,columnCount++,neocon.getResidencia(),style);
            createCell(row,columnCount++,neocon.getBancaria(),style);
            createCell(row,columnCount++,neocon.getForm(),style);
            createCell(row,columnCount++,neocon.getTdes(),style);
            createCell(row,columnCount++,neocon.getSoporteDerivada(),style);
            createCell(row,columnCount++,neocon.getUnid(),style);
            createCell(row,columnCount++,neocon.getTipoCambio(),style);
            createCell(row,columnCount++,neocon.getAgregacion(),style);
            createCell(row,columnCount++,neocon.getTipoDivisa(),style);
            createCell(row,columnCount++,neocon.getTipoPais(),style);
            createCell(row,columnCount++,neocon.getContrap(),style);
            createCell(row,columnCount++,neocon.getTimp(),style);
            createCell(row,columnCount++,neocon.getConciliacion(),style);
            columnCount++;

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

    public void exportLog(HttpServletResponse response, ArrayList<String[]> lista) throws IOException
    {
        sheet = workbook.createSheet("Log");
        Row row1 = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "Cuenta Neocon", style);
        createCell(row1, 1, "Estado", style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        for(String[] third: lista){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,third[0],style);
            createCell(row,columnCount++,third[1],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
