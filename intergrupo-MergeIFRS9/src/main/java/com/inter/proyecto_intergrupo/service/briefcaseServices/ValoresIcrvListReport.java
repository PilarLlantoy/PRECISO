package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.briefcase.BalvaloresIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.ValoresIcrv;
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

public class ValoresIcrvListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> dataStringList;
    private List<ValoresIcrv> listDataObject;

    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ValoresIcrvListReport(List<String[]> dataStringList, List<ValoresIcrv> listDataObject) {
        this.dataStringList = dataStringList;
        this.listDataObject = listDataObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Valores ICRV");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int consecutive= 0;
        createCell(row, consecutive++, "Descripción", style);
        createCell(row, consecutive++, "Capital Autorizado", style);
        createCell(row, consecutive++, "Capital por suscribir (db)", style);
        createCell(row, consecutive++, "Reserva legal", style);
        createCell(row, consecutive++, "Reservas ocasionales", style);
        createCell(row, consecutive++, "Acciones bvc voluntarias", style);
        createCell(row, consecutive++, "P.A. FAB Asobolsa", style);
        createCell(row, consecutive++, "Acciones BVC Obligatorias", style);
        createCell(row, consecutive++, "Impuesto Diferido", style);
        createCell(row, consecutive++, "Provision de Cartera", style);
        createCell(row, consecutive++, "Revalorización del Patrimonio", style);
        createCell(row, consecutive++, "Utilidades Acumuladas E.A.", style);
        createCell(row, consecutive++, "Pérdidas Acumuladas E.A.", style);
        createCell(row, consecutive++, "Utilidad del Ejercicio", style);
        createCell(row, consecutive++, "Pérdida del Ejercicio", style);
        createCell(row, consecutive++, "Patrimonio Total", style);
        createCell(row, consecutive++, "ORI", style);
        createCell(row, consecutive++, "Patrimonio Sin ORI", style);
        createCell(row, consecutive++, "K + RL", style);
        createCell(row, consecutive++, "Pérdidas Acumuladas E.A.", style);
        createCell(row, consecutive++, "Variación Otras", style);
        createCell(row, consecutive++, "Inversión", style);
        createCell(row, consecutive++, "ORI Calculado", style);
        createCell(row, consecutive++, "Variación ORI Calculdo", style);
        createCell(row, consecutive++, "PYG Calculado", style);
        createCell(row, consecutive++, "Saldo ORI 382005772 + 382005002 Contable", style);
        createCell(row, consecutive++, "Variación ORI Registrado", style);
        createCell(row, consecutive++, "PYG 514195771 + 415005776 Contable", style);
        createCell(row, consecutive++, "DIF PYG OK", style);
        createCell(row, consecutive++, "DIF ORI OK", style);
        createCell(row, consecutive++, "Borrar", style);
        createCell(row, consecutive++, "131505002 - Nominal", style);
        createCell(row, consecutive++, "131505772", style);
        createCell(row, consecutive++, "Variación INV", style);
        createCell(row, consecutive++, "Nominal + Var Inv", style);
        createCell(row, consecutive++, "Patrimonio Total * %", style);
        createCell(row, consecutive++, "Diferencia", style);
        createCell(row, consecutive++, "1315", style);
        createCell(row, consecutive++, "ORI", style);
        createCell(row, consecutive++, "PYG", style);
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

        for(ValoresIcrv data: listDataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getDescripcion(),style);
            createCell(row,columnCount++,data.getCapitalAutorizado(),style2);
            createCell(row,columnCount++,data.getCapitalPorSuscribir(),style2);
            createCell(row,columnCount++,data.getReservaLegal(),style2);
            createCell(row,columnCount++,data.getReservasOcasionales(),style2);
            createCell(row,columnCount++,data.getAccionesBvcVoluntarias(),style2);
            createCell(row,columnCount++,data.getPaFabAsobolsa(),style2);
            createCell(row,columnCount++,data.getAccionesBvcObligatorias(),style2);
            createCell(row,columnCount++,data.getImpuestoDiferido(),style2);
            createCell(row,columnCount++,data.getProvisionDeCartera(),style2);
            createCell(row,columnCount++,data.getRevalorizacionDelPatrimonio(),style2);
            createCell(row,columnCount++,data.getUtilidadesAcumuladasEa(),style2);
            createCell(row,columnCount++,data.getPerdidasAcumuladasEa(),style2);
            createCell(row,columnCount++,data.getUtilidadDelEjercicio(),style2);
            createCell(row,columnCount++,data.getPerdidaDelEjercicio(),style2);
            createCell(row,columnCount++,data.getPatrimonioTotal(),style2);
            createCell(row,columnCount++,data.getOri(),style2);
            createCell(row,columnCount++,data.getPatrimonioSinOri(),style2);
            createCell(row,columnCount++,data.getKrl(),style2);
            createCell(row,columnCount++,data.getPerdidasAcumuladasEaT(),style2);
            createCell(row,columnCount++,data.getVariacionOtras(),style2);
            createCell(row,columnCount++,data.getInversion(),style2);
            createCell(row,columnCount++,data.getOriCalculado(),style2);
            createCell(row,columnCount++,data.getVariacionOriCalculdo(),style2);
            createCell(row,columnCount++,data.getPygCalculado(),style2);
            createCell(row,columnCount++,data.getSaldoOriContable(),style2);
            createCell(row,columnCount++,data.getVariacionOriRegistrado(),style2);
            createCell(row,columnCount++,data.getPyg(),style2);
            createCell(row,columnCount++,data.getDifPygOk(),style2);
            createCell(row,columnCount++,data.getDifOriOk(),style2);
            createCell(row,columnCount++,data.getBorrar(),style2);
            createCell(row,columnCount++,data.getNominal002(),style2);
            createCell(row,columnCount++,data.getNominal772(),style2);
            createCell(row,columnCount++,data.getVariacionInv(),style2);
            createCell(row,columnCount++,data.getNominalVarInv(),style2);
            createCell(row,columnCount++,data.getPatrimonioTotalPorcentaje(),style2);
            createCell(row,columnCount++,data.getDiferencia(),style2);
            createCell(row,columnCount++,data.getNominal1315(),style2);
            createCell(row,columnCount++,data.getOriT(),style2);
            createCell(row,columnCount++,data.getPygT(),style2);
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

        int count= 0;
        createCell(row, count++, "#Exitosos", style);
        createCell(row, count++, "#Fallidos", style);
        createCell(row, count++, "Estado Final", style);

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
