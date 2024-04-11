package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.PlaneRistras;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlaneRistrasListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<PlaneRistras> planeRistraslList;
    private List<Object[]> planeRistraslListObject;

    public PlaneRistrasListReport(List<PlaneRistras> planeRistraslList){
        this.planeRistraslList = planeRistraslList;
        workbook = new XSSFWorkbook();

    }

    public PlaneRistrasListReport(List<Object[]> planeRistraslListObject, int number){
        this.planeRistraslListObject = planeRistraslListObject;
        workbook = new XSSFWorkbook();

    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Plano Ristras");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Banco", style);
        createCell(row,	1, "Interfaz", style);
        createCell(row,	2, "Cuenta Definitiva", style);
        createCell(row,	3, "Producto", style);
        createCell(row,	4, "Tipo De Cartera", style);
        createCell(row,	5, "Campo12", style);
        createCell(row,	6, "Calificacion", style);
        createCell(row,	7, "Campo14", style);
        createCell(row,	8, "Código Sector", style);
        createCell(row,	9, "Código Subsector", style);
        createCell(row,	10, "Forma De Pago", style);
        createCell(row,	11, "Línea De Crédito", style);
        createCell(row,	12, "Entid Redescuento", style);
        createCell(row,	13, "Morosidad", style);
        createCell(row,	14, "Tipo Inversión", style);
        createCell(row,	15, "Tipo De Gasto", style);
        createCell(row,	16, "Concepto Contable", style);
        createCell(row,	17, "Divisa", style);
        createCell(row,	18, "Tipo Moneda", style);
        createCell(row,	19, "Filler", style);
        createCell(row,	20, "Varios", style);
        createCell(row,	21, "Valor", style);
        createCell(row,	22, "Sagrupas", style);
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

        for(PlaneRistras account: planeRistraslList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,	account.getBanco()	,style);
            createCell(row,columnCount++,	account.getInterfaz()	,style);
            createCell(row,columnCount++,	account.getCuentaDefinitiva()	,style);
            createCell(row,columnCount++,	account.getProducto()	,style);
            createCell(row,columnCount++,	account.getTipoDeCartera()	,style);
            createCell(row,columnCount++,	account.getCampo12()	,style);
            createCell(row,columnCount++,	account.getCalificacion()	,style);
            createCell(row,columnCount++,	account.getCampo14()	,style);
            createCell(row,columnCount++,	account.getCodigoSector()	,style);
            createCell(row,columnCount++,	account.getCodigoSubsector()	,style);
            createCell(row,columnCount++,	account.getFormaDePago()	,style);
            createCell(row,columnCount++,	account.getLineaDeCredito()	,style);
            createCell(row,columnCount++,	account.getEntidRedescuento()	,style);
            createCell(row,columnCount++,	account.getMorosidad()	,style);
            createCell(row,columnCount++,	account.getTipoInversion()	,style);
            createCell(row,columnCount++,	account.getTipoDeGasto()	,style);
            createCell(row,columnCount++,	account.getConceptoContable()	,style);
            createCell(row,columnCount++,	account.getDivisa()	,style);
            createCell(row,columnCount++,	account.getTipoMoneda()	,style);
            createCell(row,columnCount++,	account.getFiller()	,style);
            createCell(row,columnCount++,	account.getVarios()	,style);
            createCell(row,columnCount++,	account.getValor()	,style);
            createCell(row,columnCount++,	account.getSagrupas()	,style);
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
