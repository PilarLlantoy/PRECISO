package com.inter.proyecto_intergrupo.service.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.BaseNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.BaseNIC34Consol;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ReportNIC34ConsolListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet2;
    private List<Object[]> nic34List;
    private List<BaseNIC34Consol> listNic34;
    private List<Object[]> listBalance;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ReportNIC34ConsolListReport(List<Object[]> nic34List, List<BaseNIC34Consol> listNic34, List<Object[]> listBalance) {
        this.nic34List = nic34List;
        this.listBalance = listBalance;
        this.listNic34 = listNic34;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Base NIC34");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int columnCountH = 0;
        createCell(row, columnCountH++, "FECONT", style);
        createCell(row, columnCountH++, "L6", style);
        createCell(row, columnCountH++,"NUCTA", style);
        createCell(row, columnCountH++,"ID_GRUPO", style);
        createCell(row, columnCountH++,"GRUPO", style);
        createCell(row, columnCountH++,"APLICA", style);
        createCell(row, columnCountH++,"SIGNO", style);
        createCell(row, columnCountH++,"ID_NOTA", style);
        createCell(row, columnCountH++,"NOTA", style);
        createCell(row, columnCountH++,"ID_SUBNOTA", style);
        createCell(row, columnCountH++,"SUBNOTA", style);
        createCell(row, columnCountH++,"ID_CAMPO", style);
        createCell(row, columnCountH++,"CAMPO", style);
        createCell(row, columnCountH++,"MONEDA", style);
        createCell(row, columnCountH++,"SALDO", style);
        createCell(row, columnCountH++,"BALANCE", style);
        createCell(row, columnCountH++,"PYG", style);
        createCell(row, columnCountH++,"QAPLICA", style);
    }

    private void writeHeaderLinePlantilla(){
        sheet = workbook.createSheet("Plantilla Query");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int columnCountH = 0;
        createCell(row, columnCountH++, "FECONT", style);
        createCell(row, columnCountH++,"NUCTA", style);
        createCell(row, columnCountH++,"MONEDA", style);
        createCell(row, columnCountH++,"SALDO", style);
    }

    private void writeHeaderLineBalance()
    {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        sheet = workbook.createSheet("Balance NIC34");
        Row row = sheet.createRow(0);

        int columnCountH = 0;
        createCell(row, columnCountH++, "ID Grupo", style);
        createCell(row, columnCountH++, "Moneda", style);
        createCell(row, columnCountH++, "Signo", style);
        createCell(row, columnCountH++, "Grupo", style);
        createCell(row, columnCountH++, "Nota", style);
        createCell(row, columnCountH++,"Saldo 1", style);
        createCell(row, columnCountH++,"Saldo 2", style);
        createCell(row, columnCountH++,"Variación", style);

        sheet2 = workbook.createSheet("Base NIC34");
        Row row1 = sheet2.createRow(0);

        columnCountH = 0;
        createCell(row1, columnCountH++, "Fecont", style);
        createCell(row1, columnCountH++, "Nucta", style);
        createCell(row1, columnCountH++, "ID Grupo", style);
        createCell(row1, columnCountH++, "Grupo", style);
        createCell(row1, columnCountH++,"ID Nota", style);
        createCell(row1, columnCountH++,"Nota", style);
        createCell(row1, columnCountH++,"ID SubNota", style);
        createCell(row1, columnCountH++,"SubNota", style);
        createCell(row1, columnCountH++,"Saldo", style);
        createCell(row1, columnCountH++,"Moneda", style);
    }

    private void writeHeaderLineNotas()
    {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        sheet = workbook.createSheet("Notas NIC34");
        Row row = sheet.createRow(0);

        int columnCountH = 0;
        createCell(row, columnCountH++, "ID", style);
        createCell(row, columnCountH++, "Moneda", style);
        createCell(row, columnCountH++, "Signo", style);
        createCell(row, columnCountH++, "Grupo", style);
        createCell(row, columnCountH++, "Nota", style);
        createCell(row, columnCountH++,"Saldo 1", style);
        createCell(row, columnCountH++,"Saldo 2", style);
        createCell(row, columnCountH++,"Variación 1", style);
        createCell(row, columnCountH++,"Variación 2", style);
        createCell(row, columnCountH++,"Saldo 3", style);
        createCell(row, columnCountH++,"Saldo 4", style);

        sheet2 = workbook.createSheet("Base NIC34");
        Row row1 = sheet2.createRow(0);

        columnCountH = 0;
        createCell(row1, columnCountH++, "Fecont", style);
        createCell(row1, columnCountH++, "Nucta", style);
        createCell(row1, columnCountH++, "ID Grupo", style);
        createCell(row1, columnCountH++, "Grupo", style);
        createCell(row1, columnCountH++,"ID Nota", style);
        createCell(row1, columnCountH++,"Nota", style);
        createCell(row1, columnCountH++,"ID SubNota", style);
        createCell(row1, columnCountH++,"SubNota", style);
        createCell(row1, columnCountH++,"Saldo", style);
        createCell(row1, columnCountH++,"Moneda", style);
    }

    private void writeHeaderLinePyg()
    {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        sheet = workbook.createSheet("PYG NIC34");
        Row row = sheet.createRow(0);

        int columnCountH = 0;
        createCell(row, columnCountH++, "ID Grupo", style);
        createCell(row, columnCountH++, "Moneda", style);
        createCell(row, columnCountH++, "Signo", style);
        createCell(row, columnCountH++, "Grupo", style);
        createCell(row, columnCountH++, "Nota", style);
        createCell(row, columnCountH++,"Saldo 1", style);
        createCell(row, columnCountH++,"Saldo 2", style);
        createCell(row, columnCountH++,"Variación 1", style);
        createCell(row, columnCountH++,"Variación 2", style);
        createCell(row, columnCountH++,"Saldo 3", style);
        createCell(row, columnCountH++,"Saldo 4", style);

        sheet2 = workbook.createSheet("Base NIC34");
        Row row1 = sheet2.createRow(0);

        columnCountH = 0;
        createCell(row1, columnCountH++, "Fecont", style);
        createCell(row1, columnCountH++, "Nucta", style);
        createCell(row1, columnCountH++, "ID Grupo", style);
        createCell(row1, columnCountH++, "Grupo", style);
        createCell(row1, columnCountH++,"ID Nota", style);
        createCell(row1, columnCountH++,"Nota", style);
        createCell(row1, columnCountH++,"ID SubNota", style);
        createCell(row1, columnCountH++,"SubNota", style);
        createCell(row1, columnCountH++,"Saldo", style);
        createCell(row1, columnCountH++,"Moneda", style);
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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        CellStyle style1 = workbook.createCellStyle();
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(BaseNIC34Consol paramNIC34: listNic34){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,paramNIC34.getFecont(),style);
            createCell(row,columnCount++,paramNIC34.getL6(),style);
            createCell(row,columnCount++,paramNIC34.getNucta(),style);
            createCell(row,columnCount++,paramNIC34.getIdGrupo(),style);
            createCell(row,columnCount++,paramNIC34.getGrupo(),style);
            createCell(row,columnCount++,paramNIC34.getAplica(),style);
            createCell(row,columnCount++,paramNIC34.getSigno(),style);
            createCell(row,columnCount++,paramNIC34.getIdNota(),style);
            createCell(row,columnCount++,paramNIC34.getNota(),style);
            createCell(row,columnCount++,paramNIC34.getIdSubnota(),style);
            createCell(row,columnCount++,paramNIC34.getSubnota(),style);
            createCell(row,columnCount++,paramNIC34.getIdCampo(),style);
            createCell(row,columnCount++,paramNIC34.getCampo(),style);
            createCell(row,columnCount++,paramNIC34.getMoneda(),style);
            createCell(row,columnCount++,paramNIC34.getSaldoquery(),style1);
            createCell(row,columnCount++,paramNIC34.getBalance(),style);
            createCell(row,columnCount++,paramNIC34.getPyg(),style);
            createCell(row,columnCount++,paramNIC34.getQAplica(),style);
        }
    }

    private void writeDataLinesBalance(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        XSSFFont font1 = workbook.createFont();
        font1.setColor(IndexedColors.WHITE.getIndex());
        font1.setFontHeight(10);
        font1.setBold(true);

        CellStyle style1 = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        CellStyle style3 = workbook.createCellStyle();
        CellStyle style4 = workbook.createCellStyle();
        style1.setFont(font);
        style2.setFont(font);
        style3.setFont(font1);
        style4.setFont(font1);

        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style4.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style3.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style4.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style4.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for(Object[] paramNIC34: listBalance){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            if(paramNIC34[9].toString().equals("1"))
            {
                createCell(row,columnCount++,"",style3);
                createCell(row,columnCount++,paramNIC34[2].toString(),style3);
                createCell(row,columnCount++,paramNIC34[3].toString(),style3);
                createCell(row,columnCount++,paramNIC34[4].toString(),style3);
                createCell(row,columnCount++,paramNIC34[5].toString(),style3);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[6].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[7].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[8].toString()),style4);
            }
            else
            {
                createCell(row,columnCount++,paramNIC34[1].toString(),style);
                createCell(row,columnCount++,paramNIC34[2].toString(),style);
                createCell(row,columnCount++,paramNIC34[3].toString(),style);
                createCell(row,columnCount++,paramNIC34[4].toString(),style);
                createCell(row,columnCount++,paramNIC34[5].toString(),style);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[6].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[7].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[8].toString()),style2);
            }
        }

        rowCount = 1;

        for(Object[] paramNIC34: nic34List){
            Row row = sheet2.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,paramNIC34[0].toString(),style);
            createCell(row,columnCount++,paramNIC34[1].toString(),style);
            createCell(row,columnCount++,paramNIC34[2].toString(),style);
            createCell(row,columnCount++,paramNIC34[3].toString(),style);
            createCell(row,columnCount++,paramNIC34[4].toString(),style);
            createCell(row,columnCount++,paramNIC34[5].toString(),style);
            createCell(row,columnCount++,paramNIC34[6].toString(),style);
            createCell(row,columnCount++,paramNIC34[7].toString(),style);
            createCell(row,columnCount++,Double.parseDouble(paramNIC34[8].toString()),style1);
            createCell(row,columnCount++,paramNIC34[9].toString(),style);
        }
    }

    private void writeDataLinesNotas(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        XSSFFont font1 = workbook.createFont();
        font1.setColor(IndexedColors.WHITE.getIndex());
        font1.setFontHeight(10);
        font1.setBold(true);

        CellStyle style1 = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        CellStyle style3 = workbook.createCellStyle();
        CellStyle style4 = workbook.createCellStyle();
        style1.setFont(font);
        style2.setFont(font);
        style3.setFont(font1);
        style4.setFont(font1);

        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style4.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style3.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style4.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style4.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for(Object[] paramNIC34: listBalance){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            if(paramNIC34[8].toString().equals("1"))
            {
                createCell(row,columnCount++,"",style3);
                createCell(row,columnCount++,paramNIC34[2].toString(),style3);
                createCell(row,columnCount++,paramNIC34[3].toString(),style3);
                createCell(row,columnCount++,paramNIC34[4].toString(),style3);
                createCell(row,columnCount++,paramNIC34[12].toString(),style3);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[5].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[6].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[7].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[9].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[10].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[11].toString()),style4);
            }
            else
            {
                createCell(row,columnCount++,paramNIC34[1].toString(),style);
                createCell(row,columnCount++,paramNIC34[2].toString(),style);
                createCell(row,columnCount++,paramNIC34[3].toString(),style);
                createCell(row,columnCount++,paramNIC34[4].toString(),style);
                createCell(row,columnCount++,paramNIC34[12].toString(),style);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[5].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[6].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[7].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[9].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[10].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[11].toString()),style2);
            }
        }

        rowCount = 1;

        for(Object[] paramNIC34: nic34List){
            Row row = sheet2.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,paramNIC34[0].toString(),style);
            createCell(row,columnCount++,paramNIC34[1].toString(),style);
            createCell(row,columnCount++,paramNIC34[2].toString(),style);
            createCell(row,columnCount++,paramNIC34[3].toString(),style);
            createCell(row,columnCount++,paramNIC34[4].toString(),style);
            createCell(row,columnCount++,paramNIC34[5].toString(),style);
            createCell(row,columnCount++,paramNIC34[6].toString(),style);
            createCell(row,columnCount++,paramNIC34[7].toString(),style);
            createCell(row,columnCount++,Double.parseDouble(paramNIC34[8].toString()),style1);
            createCell(row,columnCount++,paramNIC34[9].toString(),style);
        }
    }

    private void writeDataLinesPyg(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        XSSFFont font1 = workbook.createFont();
        font1.setColor(IndexedColors.WHITE.getIndex());
        font1.setFontHeight(10);
        font1.setBold(true);

        CellStyle style1 = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        CellStyle style3 = workbook.createCellStyle();
        CellStyle style4 = workbook.createCellStyle();
        style1.setFont(font);
        style2.setFont(font);
        style3.setFont(font1);
        style4.setFont(font1);

        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style4.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style3.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style4.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style4.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for(Object[] paramNIC34: listBalance){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            if(paramNIC34[1].toString().length()==0)
            {
                createCell(row,columnCount++,"",style3);
                createCell(row,columnCount++,paramNIC34[2].toString(),style3);
                createCell(row,columnCount++,paramNIC34[3].toString(),style3);
                createCell(row,columnCount++,paramNIC34[4].toString(),style3);
                createCell(row,columnCount++,paramNIC34[5].toString(),style3);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[6].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[7].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[8].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[9].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[10].toString()),style4);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[11].toString()),style4);
            }
            else
            {
                createCell(row,columnCount++,paramNIC34[1].toString(),style);
                createCell(row,columnCount++,paramNIC34[2].toString(),style);
                createCell(row,columnCount++,paramNIC34[3].toString(),style);
                createCell(row,columnCount++,paramNIC34[4].toString(),style);
                createCell(row,columnCount++,paramNIC34[5].toString(),style);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[6].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[7].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[8].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[9].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[10].toString()),style2);
                createCell(row,columnCount++,Double.parseDouble(paramNIC34[11].toString()),style2);
            }
        }

        rowCount = 1;

        for(Object[] paramNIC34: nic34List){
            Row row = sheet2.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,paramNIC34[0].toString(),style);
            createCell(row,columnCount++,paramNIC34[1].toString(),style);
            createCell(row,columnCount++,paramNIC34[2].toString(),style);
            createCell(row,columnCount++,paramNIC34[3].toString(),style);
            createCell(row,columnCount++,paramNIC34[4].toString(),style);
            createCell(row,columnCount++,paramNIC34[5].toString(),style);
            createCell(row,columnCount++,paramNIC34[6].toString(),style);
            createCell(row,columnCount++,paramNIC34[7].toString(),style);
            createCell(row,columnCount++,Double.parseDouble(paramNIC34[8].toString()),style1);
            createCell(row,columnCount++,paramNIC34[9].toString(),style);
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

    public void exportPlantilla(HttpServletResponse response) throws IOException {
        writeHeaderLinePlantilla();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportBalance(HttpServletResponse response) throws IOException {
        writeHeaderLineBalance();
        writeDataLinesBalance();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportPyg(HttpServletResponse response) throws IOException {
        writeHeaderLinePyg();
        writeDataLinesPyg();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportNotas(HttpServletResponse response) throws IOException {
        writeHeaderLineNotas();
        writeDataLinesNotas();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Cargue");
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

        Row row2 = sheet.createRow(0);
        createCell(row2, 0, "Cuenta", style);
        createCell(row2, 1, "Estado", style);

        int rowCount = 1;
        for (Object[] log : nic34List)
        {
            Row row3 = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row3, columnCount++, log[0].toString(), style1);
            createCell(row3, columnCount++, "La Cuenta no se encuentra parametrizada.", style1);
        }


        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
