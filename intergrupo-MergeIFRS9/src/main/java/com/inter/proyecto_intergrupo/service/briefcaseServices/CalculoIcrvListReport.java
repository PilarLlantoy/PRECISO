package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.briefcase.CalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaCalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaPrecioIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PrecioIcrv;
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

public class CalculoIcrvListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> dataStringList;
    private List<CalculoIcrv> listDataObject;

    private List<PlantillaCalculoIcrv> list2DataObject;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public CalculoIcrvListReport(List<String[]> dataStringList, List<CalculoIcrv> listDataObject, List<PlantillaCalculoIcrv> list2DataObject) {
        this.dataStringList = dataStringList;
        this.listDataObject = listDataObject;
        this.list2DataObject = list2DataObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Cálculo ICRV");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int consecutive= 0;
        createCell(row, consecutive++, "Valoración", style);
        createCell(row, consecutive++, "Empresa", style);
        createCell(row, consecutive++, "NIT", style);
        createCell(row, consecutive++, "DV", style);
        createCell(row, consecutive++, "ISIN", style);
        createCell(row, consecutive++, "%Participación", style);
        createCell(row, consecutive++, "VR Acción", style);
        createCell(row, consecutive++, "No. Acciones", style);
        createCell(row, consecutive++, "Valor Nominal", style);
        createCell(row, consecutive++, "Precio", style);
        createCell(row, consecutive++, "VR Patrimonio", style);
        createCell(row, consecutive++, "VR Mercado", style);
        createCell(row, consecutive++, "Saldo Libros Valoración", style);
        createCell(row, consecutive++, "Ajuste", style);
        createCell(row, consecutive++, "Dividendos pagados en #Acciones", style);
        createCell(row, consecutive++, "Dividendos pagados en Acciones", style);
        createCell(row, consecutive++, "Capital", style);
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

        for(CalculoIcrv data: listDataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getValoracion(),style);
            createCell(row,columnCount++,data.getEmpresa(),style);
            createCell(row,columnCount++,data.getNit(),style);
            createCell(row,columnCount++,data.getDv(),style);
            createCell(row,columnCount++,data.getIsin(),style);
            createCell(row,columnCount++,data.getParticipacion(),style2);
            createCell(row,columnCount++,data.getVrAccion(),style2);
            createCell(row,columnCount++,data.getNoAcciones(),style2);
            createCell(row,columnCount++,data.getValorNominal(),style2);
            createCell(row,columnCount++,data.getPrecio(),style2);
            createCell(row,columnCount++,data.getVrPatrimonio(),style2);
            createCell(row,columnCount++,data.getVrMercado(),style2);
            createCell(row,columnCount++,data.getSaldoLibrosValoracion(),style2);
            createCell(row,columnCount++,data.getAjuste(),style2);
            createCell(row,columnCount++,data.getDividendosPagadosNoAcciones(),style2);
            createCell(row,columnCount++,data.getDividendosPagadosAcciones(),style2);
            createCell(row,columnCount++,data.getCapital(),style2);
        }
    }

    private void writeDataLinesPlantilla(){
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

        for(PlantillaCalculoIcrv data: list2DataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getValoracion(),style);
            createCell(row,columnCount++,data.getEmpresa(),style);
            createCell(row,columnCount++,data.getNit(),style);
            createCell(row,columnCount++,data.getDv(),style);
            createCell(row,columnCount++,data.getIsin(),style);
            createCell(row,columnCount++,data.getParticipacion(),style2);
            createCell(row,columnCount++,data.getVrAccion(),style2);
            createCell(row,columnCount++,data.getNoAcciones(),style2);
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
        writeHeaderLine();
        writeDataLinesPlantilla();

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
