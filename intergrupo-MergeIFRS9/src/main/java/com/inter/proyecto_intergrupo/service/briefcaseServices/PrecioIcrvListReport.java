package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.briefcase.BaseIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaPrecioIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PrecioIcrv;
import com.inter.proyecto_intergrupo.repository.briefcase.PlantillaPrecioIcrvRepository;
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

public class PrecioIcrvListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> dataStringList;
    private List<PrecioIcrv> listDataObject;
    private List<PlantillaPrecioIcrv> list2DataObject;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public PrecioIcrvListReport(List<String[]> dataStringList, List<PrecioIcrv> listDataObject,List<PlantillaPrecioIcrv> list2DataObject) {
        this.dataStringList = dataStringList;
        this.listDataObject = listDataObject;
        this.list2DataObject = list2DataObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Precio ICRV");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int consecutive= 0;
        createCell(row, consecutive++, "Método", style);
        createCell(row, consecutive++, "Fecha Contable", style);
        createCell(row, consecutive++, "Empresa", style);
        createCell(row, consecutive++, "Precio Valoración", style);
        createCell(row, consecutive++, "Patrimonio", style);
        createCell(row, consecutive++, "Acciones Circulación", style);
        createCell(row, consecutive++, "VR Intrinseco", style);
        createCell(row, consecutive++, "ORI", style);
        createCell(row, consecutive++, "Fecha Recibo", style);
        createCell(row, consecutive++, "Fecha Actualización", style);
        createCell(row, consecutive++, "Resultado", style);
        createCell(row, consecutive++, "ISIN", style);
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

        for(PrecioIcrv data: listDataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getMetodo(),style);
            createCell(row,columnCount++,data.getFechaContable(),style);
            createCell(row,columnCount++,data.getEmpresa(),style);
            createCell(row,columnCount++,data.getPrecioValoracion(),style2);
            createCell(row,columnCount++,data.getPatrimonio(),style2);
            createCell(row,columnCount++,data.getAcciones(),style2);
            createCell(row,columnCount++,data.getVrIntrinseco(),style2);
            createCell(row,columnCount++,data.getOri(),style2);
            createCell(row,columnCount++,data.getFechaRecibo(),style);
            createCell(row,columnCount++,data.getFechaActualizacion(),style);
            createCell(row,columnCount++,data.getResultado(),style);
            createCell(row,columnCount++,data.getIsin(),style);
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

        for(PlantillaPrecioIcrv data: list2DataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getMetodo(),style);
            createCell(row,columnCount++,"",style);
            createCell(row,columnCount++,data.getEmpresa(),style);
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
