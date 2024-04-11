package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.briefcase.CalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaCalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.ReportIcrv;
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

public class ReportIcrvListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> dataStringList;
    private List<ReportIcrv> listDataObject;

    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ReportIcrvListReport(List<String[]> dataStringList, List<ReportIcrv> listDataObject) {
        this.dataStringList = dataStringList;
        this.listDataObject = listDataObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Reporte ICRV");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int consecutive= 0;
        createCell(row, consecutive++, "Entidad", style);
        createCell(row, consecutive++, "Cod Periodo", style);
        createCell(row, consecutive++, "Cod Sociinfo", style);
        createCell(row, consecutive++, "Xti Cartera", style);
        createCell(row, consecutive++, "Cod Socipart", style);
        createCell(row, consecutive++, "Cod ISIN", style);
        createCell(row, consecutive++, "Coste Valor", style);
        createCell(row, consecutive++, "Ajuste Valor Razonable", style);
        createCell(row, consecutive++, "Microcoberturas", style);
        createCell(row, consecutive++, "Correcciones Por Deterioro", style);
        createCell(row, consecutive++, "Valor Cotizado", style);
        createCell(row, consecutive++, "Desembolso Pdte", style);
        createCell(row, consecutive++, "Num Titulos", style);
        createCell(row, consecutive++, "Capital Social", style);
        createCell(row, consecutive++, "Coste Adquisicion", style);
        createCell(row, consecutive++, "Signo Valor Contable", style);
        createCell(row, consecutive++, "Signo Microcobertura", style);

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

        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font2 = workbook.createFont();
        font2.setFontHeight(10);
        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(ReportIcrv data: listDataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getEntidad(),style);
            createCell(row,columnCount++,data.getCodPeriodo(),style);
            createCell(row,columnCount++,data.getCodSociinfo(),style);
            createCell(row,columnCount++,data.getXtiCartera(),style);
            createCell(row,columnCount++,data.getCodSocipart(),style);
            createCell(row,columnCount++,data.getCodIsin(),style);
            createCell(row,columnCount++,data.getCosteValor(),style2);
            createCell(row,columnCount++,data.getAjusteValorRazonable(),style2);
            createCell(row,columnCount++,data.getMicrocoberturas(),style2);
            createCell(row,columnCount++,data.getCorreccionesPorDeterioro(),style2);
            createCell(row,columnCount++,data.getValorCotizado(),style2);
            createCell(row,columnCount++,data.getDesembolsoPdte(),style2);
            createCell(row,columnCount++,data.getNumTitulos(),style2);
            createCell(row,columnCount++,data.getCapitalSocial(),style2);
            createCell(row,columnCount++,data.getCosteAdquisicion(),style2);
            createCell(row,columnCount++,data.getSignoValorContable(),style);
            createCell(row,columnCount++,data.getSignoMicrocobertura(),style);

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
        sheet = workbook.createSheet("Log_Cargue");
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

        createCell(row1, 0, dataStringList.get(dataStringList.size()-1)[0], style1);
        createCell(row1, 1, dataStringList.get(dataStringList.size()-1)[1], style1);
        createCell(row1, 2, dataStringList.get(dataStringList.size()-1)[2], style1);

        if(dataStringList.size()>1)
        {
            dataStringList.remove(dataStringList.size()-1);
            Row row2 = sheet.createRow(2);
            createCell(row2, 0, "Fila", style);
            createCell(row2, 1, "Columna", style);
            createCell(row2, 2, "Estado", style);

            int rowCount = 3;
            for (String[] log : dataStringList)
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
