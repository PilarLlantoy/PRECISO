package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.briefcase.CalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PduIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaCalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaPduIcrv;
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

public class PduIcrvListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> dataStringList;
    private List<PduIcrv> listDataObject;

    private List<PlantillaPduIcrv> list2DataObject;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public PduIcrvListReport(List<String[]> dataStringList, List<PduIcrv> listDataObject, List<PlantillaPduIcrv> list2DataObject) {
        this.dataStringList = dataStringList;
        this.listDataObject = listDataObject;
        this.list2DataObject = list2DataObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("PDU ICRV");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int consecutive= 0;
        createCell(row, consecutive++, "NOISIN", style);
        createCell(row, consecutive++, "GRUPO", style);
        createCell(row, consecutive++, "ENTIDAD", style);
        createCell(row, consecutive++, "%", style);
        createCell(row, consecutive++, "FECHA ASAMBLEA", style);
        createCell(row, consecutive++, "FECHA CAUSACION", style);
        createCell(row, consecutive++, "UTILIDAD DEL EJERCICIO 2022", style);
        createCell(row, consecutive++, "RESERVA NO DISTRIBUIDA", style);
        createCell(row, consecutive++, "UTILIDAD A DISTRIBUIR", style);
        createCell(row, consecutive++, "DIVIDENDOS RECIBIDOS BBVA", style);
        createCell(row, consecutive++, "%", style);
        createCell(row, consecutive++, "EFECTIVO", style);
        createCell(row, consecutive++, "%", style);
        createCell(row, consecutive++, "ACCION", style);
        createCell(row, consecutive++, "TOTAL", style);
        createCell(row, consecutive++, "VALIDACION", style);
        createCell(row, consecutive++, "Aplica Retfuente", style);
        createCell(row, consecutive++, "RETENCIÓN EN LA FUENTE", style);
        createCell(row, consecutive++, "VALOR A RECIBIR", style);
        createCell(row, consecutive++, "FECHAS DE PAGO 1", style);
        createCell(row, consecutive++, "FECHAS DE PAGO 2", style);
        createCell(row, consecutive++, "FECHAS DE PAGO 3", style);
        createCell(row, consecutive++, "VALOR RECIBIDO EN EL PAGO DE DIVIDENDOS 1", style);
        createCell(row, consecutive++, "VALOR RECIBIDO EN EL PAGO DE DIVIDENDOS 2", style);
        createCell(row, consecutive++, "VALOR RECIBIDO EN EL PAGO DE DIVIDENDOS 3", style);
        createCell(row, consecutive++, "CORREO", style);

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
        XSSFFont font1 = workbook.createFont();
        font1.setFontHeight(10);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));

        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font2 = workbook.createFont();
        font2.setFontHeight(10);
        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(PduIcrv data: listDataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getNoisin(),style);
            createCell(row,columnCount++,data.getGrupo(),style);
            createCell(row,columnCount++,data.getEntidad(),style);
            createCell(row,columnCount++,data.getPorcentaje(),style1);
            createCell(row,columnCount++,data.getFechaAsamblea(),style);
            createCell(row,columnCount++,data.getFechaCausacion(),style);
            createCell(row,columnCount++,data.getUtilidadDelEjercicio(),style2);
            createCell(row,columnCount++,data.getReservaNoDistribuida(),style2);
            createCell(row,columnCount++,data.getUtilidadDistribuir(),style2);
            createCell(row,columnCount++,data.getDividendosRecibidos(),style2);
            if(data.getPorcentajeEfectivo()!=null)
                createCell(row,columnCount++,data.getPorcentajeEfectivo()/100,style1);
            else
                createCell(row,columnCount++,0,style1);
            createCell(row,columnCount++,data.getEfectivo(),style2);
            if(data.getPorcentajeAccion()!=null)
                createCell(row,columnCount++,data.getPorcentajeAccion()/100,style1);
            else
                createCell(row,columnCount++,0,style1);
            createCell(row,columnCount++,data.getAccion(),style2);
            createCell(row,columnCount++,data.getTotal(),style2);
            createCell(row,columnCount++,data.getValidacion(),style2);
            createCell(row,columnCount++,data.getAplicaRetfuente(),style);
            createCell(row,columnCount++,data.getRetencionEnFuente(),style2);
            createCell(row,columnCount++,data.getValorRecibir(),style2);
            createCell(row,columnCount++,data.getFechasDePago1(),style);
            createCell(row,columnCount++,data.getFechasDePago2(),style);
            createCell(row,columnCount++,data.getFechasDePago3(),style);
            createCell(row,columnCount++,data.getValorDividendosPago1(),style2);
            createCell(row,columnCount++,data.getValorDividendosPago2(),style2);
            createCell(row,columnCount++,data.getValorDividendosPago3(),style2);
            createCell(row,columnCount++,data.getCorreo(),style2);
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

        for(PlantillaPduIcrv data: list2DataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getNoisin(),style);
            createCell(row,columnCount++,data.getGrupo(),style);
            createCell(row,columnCount++,data.getEntidad(),style);
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
