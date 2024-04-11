package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.briefcase.FiduciariaIcrv;
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

public class FiduciariaIcrvListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> dataStringList;
    private List<FiduciariaIcrv> listDataObject;

    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public FiduciariaIcrvListReport(List<String[]> dataStringList, List<FiduciariaIcrv> listDataObject) {
        this.dataStringList = dataStringList;
        this.listDataObject = listDataObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Fiduciaria ICRV");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int consecutive= 0;
        createCell(row, consecutive++, "Descripción", style);
        createCell(row, consecutive++, "Capital Autorizado", style);
        createCell(row, consecutive++, "Capital Por Suscribir (-)", style);
        createCell(row, consecutive++, "Reserva Legal", style);
        createCell(row, consecutive++, "Apropiación De Utilidades", style);
        createCell(row, consecutive++, "Para Readquisicion De Acciones", style);
        createCell(row, consecutive++, "Acciones Propias Readquiridas (-)", style);
        createCell(row, consecutive++, "Instrumentos Financieros Medidios Al Valor Razonab", style);
        createCell(row, consecutive++, "Instrumentos Financieros Medidos Al Valor Razonable Con Cambios En El Ori Tit. Partic Baja Bursatilidad", style);
        createCell(row, consecutive++, "Titulos De Tesoreria - Tes", style);
        createCell(row, consecutive++, "Impto Diferido X Dif En Valor Inv Disponible Vta", style);
        createCell(row, consecutive++, "Resultado De Ejercicios Anteriores", style);
        createCell(row, consecutive++, "Resultado De Ejercicios Anteriores 2", style);
        createCell(row, consecutive++, "Resultados Del Ejercicio", style);
        createCell(row, consecutive++, "Patrimonio Total", style);
        createCell(row, consecutive++, "Ori", style);
        createCell(row, consecutive++, "Patrimonio Sin Ori", style);
        createCell(row, consecutive++, "K + Reservas", style);
        createCell(row, consecutive++, "Otras", style);
        createCell(row, consecutive++, "Ori Calculado", style);
        createCell(row, consecutive++, "Variacion Ori Calculado", style);
        createCell(row, consecutive++, "Pyg Calculado", style);
        createCell(row, consecutive++, "Saldo Ori Contable 382005001 + 382005771", style);
        createCell(row, consecutive++, "Variacion Ori Registrado", style);
        createCell(row, consecutive++, "Pyg Contable 415005775", style);
        createCell(row, consecutive++, "Dif Pyg", style);
        createCell(row, consecutive++, "Dif Ori", style);
        createCell(row, consecutive++, "Borrar", style);
        createCell(row, consecutive++, "131505001- Nominal", style);
        createCell(row, consecutive++, "131505771", style);
        createCell(row, consecutive++, "1315", style);
        createCell(row, consecutive++, "Ori", style);
        createCell(row, consecutive++, "Pyg", style);


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

        for(FiduciariaIcrv data: listDataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getDescripcion(),style);
            createCell(row,columnCount++,data.getCapitalAutorizado(),style2);
            createCell(row,columnCount++,data.getCapitalPorSuscribir(),style2);
            createCell(row,columnCount++,data.getReservaLegal(),style2);
            createCell(row,columnCount++,data.getApropiacionDeUtilidades(),style2);
            createCell(row,columnCount++,data.getReadquisicionDeAcciones(),style2);
            createCell(row,columnCount++,data.getAccionesPropiasReadquiridas(),style2);
            createCell(row,columnCount++,data.getInstrumentosFinancierosValorRazonable(),style2);
            createCell(row,columnCount++,data.getInstrumentosFinancierosValorRazonableCambiosOri(),style2);
            createCell(row,columnCount++,data.getTitulosDeTesoreria(),style2);
            createCell(row,columnCount++,data.getImptoDiferidoValorInvDisponibleVta(),style2);
            createCell(row,columnCount++,data.getResultadoEjerciciosAnteriores(),style2);
            createCell(row,columnCount++,data.getResultadoEjerciciosAnteriores2(),style2);
            createCell(row,columnCount++,data.getResultadosDelEjercicio(),style2);
            createCell(row,columnCount++,data.getPatrimonioTotal(),style2);
            createCell(row,columnCount++,data.getOri(),style2);
            createCell(row,columnCount++,data.getPatrimonioSinOri(),style2);
            createCell(row,columnCount++,data.getKReservas(),style2);
            createCell(row,columnCount++,data.getOtras(),style2);
            createCell(row,columnCount++,data.getOriCalculado(),style2);
            createCell(row,columnCount++,data.getVariacionOriCalculdo(),style2);
            createCell(row,columnCount++,data.getPygCalculado(),style2);
            createCell(row,columnCount++,data.getSaldoOriContable(),style2);
            createCell(row,columnCount++,data.getVariacionOriRegistrado(),style2);
            createCell(row,columnCount++,data.getPyg(),style2);
            createCell(row,columnCount++,data.getDifPygOk(),style2);
            createCell(row,columnCount++,data.getDifOriOk(),style2);
            createCell(row,columnCount++,data.getBorrar(),style2);
            createCell(row,columnCount++,data.getNominal001(),style2);
            createCell(row,columnCount++,data.getNominal771(),style2);
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
