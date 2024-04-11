package com.inter.proyecto_intergrupo.service.accountsReceivable;

import com.inter.proyecto_intergrupo.model.accountsReceivable.InvoicesCc;
import com.inter.proyecto_intergrupo.model.dataquality.RulesDQ;
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

public class InvoicesCcListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> invoiceList;
    private List<InvoicesCc> listInvoice;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public InvoicesCcListReport(List<String[]> invoiceList, List<InvoicesCc> listInvoice) {
        this.invoiceList = invoiceList;
        this.listInvoice = listInvoice;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Cuentas Por Cobrar");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Tercero", style);
        createCell(row, 1, "Fecha", style);
        createCell(row, 2, "Concepto", style);
        createCell(row, 3, "Persona", style);
        createCell(row, 4, "Valor", style);
        createCell(row, 5, "Estado Cargue", style);
        createCell(row, 6, "Estado Pago", style);
        createCell(row, 7, "Periodo Cargue", style);
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

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style2.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style2.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));

        for(InvoicesCc invoicesCc: listInvoice){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,invoicesCc.getTercero(),style);
            createCell(row,columnCount++,invoicesCc.getFecha(),style);
            createCell(row,columnCount++,invoicesCc.getConcepto(),style);
            createCell(row,columnCount++,invoicesCc.getPersona(),style);
            createCell(row,columnCount++,invoicesCc.getValor(),style1);
            createCell(row,columnCount++,invoicesCc.getEstado(),style);
            if(invoicesCc.getPago() == null)
                createCell(row,columnCount++,"No Procesado",style);
            else if(invoicesCc.getPago() == true)
                createCell(row,columnCount++,"Pagado",style);
            else if(invoicesCc.getPago() == false)
                createCell(row,columnCount++,"Pendiente",style);
            createCell(row,columnCount++,invoicesCc.getPeriodo(),style);

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

    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Cuentas_Por_Cobrar");
        Row row = sheet.createRow(0);
        Row row1 = sheet.createRow(1);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font1 = workbook.createFont();
        font1.setBold(false);
        font1.setFontHeight(10);
        style1.setFont(font1);

        createCell(row, 0, "#Exitosos", style);
        createCell(row, 1, "#Fallidos", style);
        createCell(row, 2, "Estado Final", style);

        createCell(row1, 0, invoiceList.get(invoiceList.size()-1)[0], style1);
        createCell(row1, 1, invoiceList.get(invoiceList.size()-1)[1], style1);
        createCell(row1, 2, invoiceList.get(invoiceList.size()-1)[2], style1);

        if(invoiceList.size()>1)
        {
            invoiceList.remove(invoiceList.size()-1);
            Row row2 = sheet.createRow(2);
            createCell(row2, 0, "Fila", style);
            createCell(row2, 1, "Columna", style);
            createCell(row2, 2, "Estado", style);

            int rowCount = 3;
            for (String[] log : invoiceList)
            {
                Row row3 = sheet.createRow(rowCount++);
                int columnCount = 0;
                createCell(row3, columnCount++, log[0], style1);
                createCell(row3, columnCount++, log[1], style1);
                createCell(row3, columnCount++, log[2], style1);
            }
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
