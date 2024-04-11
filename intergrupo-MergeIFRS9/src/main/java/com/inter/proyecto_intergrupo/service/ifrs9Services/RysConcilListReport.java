package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.RiskRys;
import com.inter.proyecto_intergrupo.model.ifrs9.RysConcil;
import com.inter.proyecto_intergrupo.model.parametric.Contract;
import com.inter.proyecto_intergrupo.model.parametric.Country;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RysConcilListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;
    private List<RiskRys> riskRysList;

    public RysConcilListReport(List<RiskRys> riskRysList){
        this.riskRysList = riskRysList;
        this.workbook = new XSSFWorkbook();
    }

    private void writeHeaderLineRisk(){
        sheet = workbook.createSheet("Riesgos de Mercados");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Número Papeleta", style);
        createCell(row, 1, "Cod Nombre", style);
        createCell(row, 2, "CNTA_CNTBLE_1_CONCILIACION", style);
        createCell(row, 3, "Fecha", style);
        createCell(row, 4, "Fecha Final", style);
        createCell(row, 5, "Valor Total", style);
        createCell(row, 6, "Intereses", style);
        createCell(row, 7, "Causación Hoy", style);
        createCell(row, 8, "Moneda", style);
        createCell(row, 9, "Fecha Corte", style);
        createCell(row, 10, "Exposición", style);
        createCell(row, 11, "Días al Vto", style);
        createCell(row, 12, "Tasa Descuento", style);
        createCell(row, 13, "FD", style);
        createCell(row, 14, "Valor Presente", style);
        createCell(row, 15, "Diferencia", style);

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

    private void writeDataLinesRisk(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));


        for(RiskRys riskRys: riskRysList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,riskRys.getNumeroPapeleta(),style);
            createCell(row,columnCount++,riskRys.getCodNombre(),style);
            createCell(row,columnCount++,riskRys.getCodPuc(),style);
            createCell(row,columnCount++,riskRys.getFecha().toString(),style);
            createCell(row,columnCount++,riskRys.getFechaFinal().toString(),style);
            createCell(row,columnCount++,riskRys.getValorTotal(),style1);
            createCell(row,columnCount++,riskRys.getIntereses(),style1);
            createCell(row,columnCount++,riskRys.getCausacionHoy(),style1);
            createCell(row,columnCount++,riskRys.getMoneda(),style);
            createCell(row,columnCount++,riskRys.getFechaCorte().toString(),style);
            createCell(row,columnCount++,riskRys.getExposicion(),style);
            createCell(row,columnCount++,riskRys.getDiasAlVto(),style);
            createCell(row,columnCount++,riskRys.getTasaDescuento(),style1);
            createCell(row,columnCount++,riskRys.getFd(),style);
            createCell(row,columnCount++,riskRys.getValorPresente(),style1);
            createCell(row,columnCount++,riskRys.getDiferencia(),style1);
        }
    }

    public void exportRisk(HttpServletResponse response) throws IOException {
        writeHeaderLineRisk();
        writeDataLinesRisk();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLog(HttpServletResponse response, List<String[]> lista) throws IOException
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
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(lista.size()-1 )[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(lista.size()-1 )[2], style);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(response.getOutputStream());
        workbook.close();
        outputStream.flush();
        outputStream.close();

    }

    public void exportResumen(HttpServletResponse response, List<Object[]> lista,List<Object[]> lista2) throws IOException
    {
        sheet = workbook.createSheet("Resumen 1");
        Row row1 = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row1, 0, "NÚMERO PAPELETA", style);
        createCell(row1, 1, "NÚMERO IDENTIFICACIÓN", style);
        createCell(row1, 2, "TIPO ENTIDAD", style);
        createCell(row1, 3, "CNTRO_CNTBLE_1_CONCILIACION", style);
        createCell(row1, 4, "CNTA_CNTBLE_1_CONCILIACION", style);
        createCell(row1, 5, "DVSA_CNTBLE_1_CONCILIACION", style);
        createCell(row1, 6, "CNTA_CNTBLE_1_PYG", style);
        createCell(row1, 7, "TOTAL RYS", style);

        sheet1 = workbook.createSheet("Resumen 2");
        Row row2 = sheet1.createRow(0);

        createCell(row2, 0, "CTA NEOCON", style);
        createCell(row2, 1, "CNTA_CNTBLE_1_CONCILIACION", style);
        createCell(row2, 2, "CNTA_CNTBLE_1_CONCILIACION_PYG", style);
        createCell(row2, 3, "CTA NEOCON PYG", style);
        createCell(row2, 4, "DVSA_CNTBLE_1_CONCILIACION", style);
        createCell(row2, 5, "TIPO ENTIDAD", style);
        createCell(row2, 6, "AJUSTE", style);
        createCell(row2, 7, "AJUSTE PYG", style);

        int rowCount = 1;

        CellStyle style2 = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font1 = workbook.createFont();
        font1.setBold(false);
        font1.setFontHeight(11);
        style1.setFont(font1);
        style2.setFont(font1);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(int i =0; i<lista.size(); i++)
        {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,lista.get(i)[0],style1);
            createCell(row,columnCount++,lista.get(i)[1],style1);
            createCell(row,columnCount++,lista.get(i)[2],style1);
            createCell(row,columnCount++,lista.get(i)[3],style1);
            createCell(row,columnCount++,lista.get(i)[4],style1);
            createCell(row,columnCount++,lista.get(i)[5],style1);
            createCell(row,columnCount++,lista.get(i)[6],style1);
            createCell(row,columnCount++,lista.get(i)[7],style2);
        }

        rowCount = 1;

        for(int i =0; i<lista2.size(); i++)
        {
            Row row = sheet1.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,lista2.get(i)[0],style1);
            createCell(row,columnCount++,lista2.get(i)[1],style1);
            createCell(row,columnCount++,lista2.get(i)[2],style1);
            createCell(row,columnCount++,lista2.get(i)[3],style1);
            createCell(row,columnCount++,lista2.get(i)[4],style1);
            createCell(row,columnCount++,lista2.get(i)[5],style1);
            createCell(row,columnCount++,lista2.get(i)[6],style2);
            createCell(row,columnCount++,lista2.get(i)[7],style2);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(response.getOutputStream());
        workbook.close();
        outputStream.flush();
        outputStream.close();

    }

    public void exportConcil(HttpServletResponse response, List<RysConcil> lista, List<Object[]> listSubDif) throws IOException
    {
        sheet = workbook.createSheet("AjsteReposMcdo");
        Row row1 = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row1, 0, "CNTA_CNTBLE_1_CONCILIACION", style);
        createCell(row1, 1, "TIPO ENTIDAD", style);
        createCell(row1, 2, "CNTRO_CNTBLE_1_CONCILIACION", style);
        createCell(row1, 3, "DVSA_CNTBLE_1_CONCILIACION", style);
        createCell(row1, 4, "NÚMERO PAPELETA", style);
        createCell(row1, 5, "NÚMERO IDENTIFICACIÓN", style);
        createCell(row1, 6, "CNTA_CNTBLE_1_PYG", style);
        createCell(row1, 7, "TOTAL RYS", style);
        createCell(row1, 8, "VALOR PRESENTE RIESGOS", style);
        createCell(row1, 9, "AJUSTE", style);
        createCell(row1, 10, "DIFERENCIA", style);
        createCell(row1, 11, "CTA PYG", style);
        createCell(row1, 12, "AJUSTE PYG", style);
        createCell(row1, 13, "VÁLIDA", style);
        createCell(row1, 14, "CTA NEOCON", style);
        createCell(row1, 15, "CTA NEOCON PYG", style);

        if(!listSubDif.isEmpty())
        {
            sheet1 = workbook.createSheet("Diferencias");
            Row row = sheet1.createRow(0);

            createCell(row, 0, "NÚMERO PAPELETA", style);
            createCell(row, 1, "DIFERENCIA RIESGOS", style);
            createCell(row, 2, "AJUSTE PYG", style);
            createCell(row, 3, "VÁLIDA", style);
        }

        int rowCount = 1;

        CellStyle style2 = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font1 = workbook.createFont();
        font1.setBold(false);
        font1.setFontHeight(11);
        style1.setFont(font1);
        style2.setFont(font1);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(int i =0; i<lista.size(); i++)
        {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,lista.get(i).getCntaCntble1Conciliacion(),style1);
            createCell(row,columnCount++,lista.get(i).getTipoEntidad(),style1);
            createCell(row,columnCount++,lista.get(i).getCntroCntble1Conciliacion(),style1);
            createCell(row,columnCount++,lista.get(i).getDvsaCntble1conciliacion(),style1);
            createCell(row,columnCount++,lista.get(i).getNumeroPapeleta(),style1);
            createCell(row,columnCount++,lista.get(i).getNroIdentificacion(),style1);
            createCell(row,columnCount++,lista.get(i).getCntaCntble1Pyg(),style1);
            createCell(row,columnCount++,lista.get(i).getTotalRyS(),style2);
            createCell(row,columnCount++,lista.get(i).getTotalRiesgos(),style2);
            createCell(row,columnCount++,lista.get(i).getAjuste(),style2);
            createCell(row,columnCount++,lista.get(i).getDiferencia(),style2);
            createCell(row,columnCount++,lista.get(i).getCtaPyg(),style1);
            createCell(row,columnCount++,lista.get(i).getAjustePyg(),style2);
            createCell(row,columnCount++,lista.get(i).getValida(),style2);
            createCell(row,columnCount++,lista.get(i).getCtaNeocon(),style1);
            createCell(row,columnCount++,lista.get(i).getCtaNeoconPyg(),style1);
        }

        if(!listSubDif.isEmpty())
        {
            rowCount = 1;

            for(int i =0; i<listSubDif.size(); i++) {
                Row row2 = sheet1.createRow(rowCount++);
                int columnCount = 0;
                createCell(row2, columnCount++, listSubDif.get(i)[0], style1);
                createCell(row2, columnCount++, listSubDif.get(i)[1], style2);
                createCell(row2, columnCount++, listSubDif.get(i)[2], style2);
                createCell(row2, columnCount++, listSubDif.get(i)[3], style2);
            }
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(response.getOutputStream());
        workbook.close();
        outputStream.flush();
        outputStream.close();

    }
}
