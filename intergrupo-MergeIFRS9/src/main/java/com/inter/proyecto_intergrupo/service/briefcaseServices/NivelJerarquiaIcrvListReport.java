package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.briefcase.FiduciariaIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.NivelJerarquiaIcrv;
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

public class NivelJerarquiaIcrvListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> dataStringList;
    private List<NivelJerarquiaIcrv> listDataObject;

    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public NivelJerarquiaIcrvListReport(List<String[]> dataStringList, List<NivelJerarquiaIcrv> listDataObject) {
        this.dataStringList = dataStringList;
        this.listDataObject = listDataObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Nivel Jerarquia ICRV");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int consecutive= 0;
        createCell(row, consecutive++, "Cuenta Contable Inversión", style);
        createCell(row, consecutive++, "Cuenta Contable Valorización", style);
        createCell(row, consecutive++, "Empresa", style);
        createCell(row, consecutive++, "Fecha De Adquisición", style);
        createCell(row, consecutive++, "Nit", style);
        createCell(row, consecutive++, "ISIN", style);
        createCell(row, consecutive++, "% Participación", style);
        createCell(row, consecutive++, "Acciones En Circulación", style);
        createCell(row, consecutive++, "Acciones Que Posee BBVA", style);
        createCell(row, consecutive++, "Capital", style);
        //createCell(row, consecutive++, "Val. Patrimonial", style);
        createCell(row, consecutive++, "Val. Nominal Acción", style);
        createCell(row, consecutive++, "Nominal", style);
        createCell(row, consecutive++, "# Acciones", style);
        createCell(row, consecutive++, "Vr De Mcado Inver", style);
        createCell(row, consecutive++, "Saldo Inversión", style);
        createCell(row, consecutive++, "Saldo Valoración", style);
        createCell(row, consecutive++, "Vr De Mcado Inver", style);
        createCell(row, consecutive++, "Vr Intrinseco De La Acción", style);
        createCell(row, consecutive++, "Corte", style);
        createCell(row, consecutive++, "Corte De Eeff", style);
        createCell(row, consecutive++, "Método De Valoración", style);
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

        for(NivelJerarquiaIcrv data: listDataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getCuentaContableInversion(),style);
            createCell(row,columnCount++,data.getCuentaContableValorizacion(),style);
            createCell(row,columnCount++,data.getEmpresa(),style);
            createCell(row,columnCount++,data.getFechaDeAdquision(),style);
            createCell(row,columnCount++,data.getNit(),style);
            createCell(row,columnCount++,data.getIsin(),style);
            createCell(row,columnCount++,data.getPorcentajeParticipacion(),style2);
            createCell(row,columnCount++,data.getAccionesEnCirculacion(),style2);
            createCell(row,columnCount++,data.getAccionesQuePoseeBbva(),style2);
            createCell(row,columnCount++,data.getCapital(),style2);
            //createCell(row,columnCount++,data.getValPatrimonial(),style2);
            createCell(row,columnCount++,data.getValNominalAccion(),style2);
            createCell(row,columnCount++,data.getNominal(),style2);
            createCell(row,columnCount++,data.getNoAcciones(),style2);
            createCell(row,columnCount++,data.getVrMercadoInver(),style2);
            createCell(row,columnCount++,data.getSaldoInversion(),style2);
            createCell(row,columnCount++,data.getSaldoValoracion(),style2);
            createCell(row,columnCount++,data.getVrMercadoInver2(),style2);
            createCell(row,columnCount++,data.getVrIntrinsecoDeLaAccion(),style2);
            createCell(row,columnCount++,data.getCorte(),style);
            createCell(row,columnCount++,data.getCorteDeEeff(),style);
            createCell(row,columnCount++,data.getMetodoDeValoracion(),style);
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
