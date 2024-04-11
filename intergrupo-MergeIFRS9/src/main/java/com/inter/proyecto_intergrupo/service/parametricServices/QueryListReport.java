package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.information.ChangeAccountQuery;
import com.inter.proyecto_intergrupo.model.parametric.MarcacionConcil;
import com.inter.proyecto_intergrupo.model.parametric.Query;
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

public class QueryListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Query> queryList;
    private List<ChangeAccountQuery> changeAccountQueriesList;

    public QueryListReport(List<Query> queryList, List<ChangeAccountQuery> changeAccountQueriesList){
        this.queryList = queryList;
        this.changeAccountQueriesList = changeAccountQueriesList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Query");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "EMPRESA", style);
        createCell(row, 1, "NUCTA", style);
        createCell(row, 2, "FECONT", style);
        createCell(row, 3, "CODDIV", style);
        createCell(row, 4, "SALMES", style);
        createCell(row, 5, "SALMESD", style);
        createCell(row, 6, "SALMED", style);
        createCell(row, 7, "SALMEDD", style);
        createCell(row, 8, "CODIGEST", style);
        createCell(row, 9, "CODICONS", style);
        createCell(row, 10, "FECHA PROCESO", style);
        createCell(row, 11, "SALDO MES", style);
        createCell(row, 12, "SALDO MES DIVISA", style);
        createCell(row, 13, "DIVISA ESPAÑA", style);
    }

    private void writeHeaderLineConcil(){
        sheet = workbook.createSheet("Marcacion");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "APLICATIVO", style);
        createCell(row, 1, "CENTRO", style);
        createCell(row, 2, "CUENTA", style);
        createCell(row, 3, "DIFERENCIA", style);
        createCell(row, 4, "DIVISA", style);
        createCell(row, 5, "EMPRESA", style);
        createCell(row, 6, "FECHA", style);
        createCell(row, 7, "SALDO APLICATIVO", style);
        createCell(row, 8, "SALDO CONTABLE", style);
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){

        Cell cell = row.createCell(columCount);

        if(value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if(value instanceof Integer){
            cell.setCellValue((Integer) value);
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        }else if(value instanceof Double){
            cell.setCellValue((Double) value);
        }else if(value instanceof String){
            cell.setCellValue((String) value);
        }else if(value instanceof Long){
            cell.setCellValue((Long) value);
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

        for(Query query: queryList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,query.getEmpresa().toString(),style);
            createCell(row,columnCount++,query.getNUCTA().toString(),style);
            createCell(row,columnCount++,query.getFECONT().toString(),style);
            createCell(row,columnCount++,query.getCODDIV().toString(),style);
            createCell(row,columnCount++,query.getSALMES().doubleValue(),style1);
            createCell(row,columnCount++,query.getSALMESD().doubleValue(),style1);
            createCell(row,columnCount++,query.getSALMED().doubleValue(),style1);
            createCell(row,columnCount++,query.getSALMEDD().doubleValue(),style1);
            createCell(row,columnCount++,query.getCODIGEST().toString(),style);
            createCell(row,columnCount++,query.getCODICONS().toString(),style);
            createCell(row,columnCount++,query.getFECHPROCE().toString(),style);
            createCell(row,columnCount++,query.getSALDOQUERY().doubleValue(),style1);
            createCell(row,columnCount++,query.getSALDOQUERYDIVISA().doubleValue(),style1);
            createCell(row,columnCount++,query.getDIVISA(),style);
        }
    }

    private void writeDataLinesConcil(List<MarcacionConcil> concilList){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(MarcacionConcil concil: concilList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,concil.getAplicativo(),style);
            createCell(row,columnCount++,concil.getCentro(),style);
            createCell(row,columnCount++,concil.getCuenta(),style);
            createCell(row,columnCount++,concil.getDiferencia(),style1);
            createCell(row,columnCount++,concil.getDivisa(),style);
            createCell(row,columnCount++,concil.getEmpresa(),style);
            createCell(row,columnCount++,concil.getFecha(),style);
            createCell(row,columnCount++,concil.getSaldoAplicativo(),style1);
            createCell(row,columnCount++,concil.getSaldoContable(),style1);
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

    public void exportConcil(HttpServletResponse response, List<MarcacionConcil> concilList) throws IOException {
        writeHeaderLineConcil();
        writeDataLinesConcil(concilList);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportCodicons(HttpServletResponse response) throws IOException {

        sheet = workbook.createSheet("CambioCodicons");
        Row row = sheet.createRow(0);

        XSSFFont font1 = workbook.createFont();
        font1.setBold(false);
        font1.setFontHeight(11);

        CellStyle style = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);
        style2.setFont(font1);

        createCell(row, 0, "EMPRESA", style);
        createCell(row, 1, "CUENTA", style);
        createCell(row, 2, "CÓDIGO CONSOLIDACIÓN ANTERIOR", style);
        createCell(row, 3, "CÓDIGO CONSOLIDACIÓN NUEVO", style);
        createCell(row, 4, "FECHA CORTE", style);
        createCell(row, 5, "PERÍMETRO IFRS9", style);
        createCell(row, 6, "OBSERVACIÓN", style);
        createCell(row, 7, "CAMBIO", style);

        int rowCount = 1;

        CellStyle style1 = workbook.createCellStyle();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(ChangeAccountQuery query: changeAccountQueriesList){
            Row row1 = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row1,columnCount++,query.getEmpresa(),style2);
            createCell(row1,columnCount++,query.getCuenta(),style2);
            createCell(row1,columnCount++,query.getCodiconsAnterior(),style2);
            createCell(row1,columnCount++,query.getCodiconsNuevo(),style2);
            createCell(row1,columnCount++,query.getFechaCorte(),style2);
            createCell(row1,columnCount++,query.getPerimetroIfrs9(),style2);
            createCell(row1,columnCount++,query.getObservacion(),style2);
            createCell(row1,columnCount++,query.getCambio(),style2);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
