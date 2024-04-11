package com.inter.proyecto_intergrupo.service.bankServices;

import com.inter.proyecto_intergrupo.model.bank.GpsReport;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class GpsListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> gpsLogList;
    private List<GpsReport> gpsReports;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public GpsListReport(List<GpsReport> gpsReports,List<String[]> gpsLogList) {
        this.gpsReports = gpsReports;
        this.gpsLogList = gpsLogList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("GPS");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Nombre 1", style);
        createCell(row, 1, "Razón Social", style);
        createCell(row, 2, "Identificación Fiscal", style);
        createCell(row, 3, "Nif", style);
        createCell(row, 4, "Soc.", style);
        createCell(row, 5, "Clase", style);
        createCell(row, 6, "Cuenta", style);
        createCell(row, 7, "Cuenta Local", style);
        createCell(row, 8, "Importe en MD", style);
        createCell(row, 9, "Tp.camb.ef.", style);
        createCell(row, 10, "Mon.", style);
        createCell(row, 11, "Fe.contab", style);
        createCell(row, 12, "Ejerc./mes", style);
        createCell(row, 13, "Ce.coste", style);
        createCell(row, 14, "Fecha doc.", style);
        createCell(row, 15, "Texto", style);
        createCell(row, 16, "Texto cab. EM", style);
        createCell(row, 17, "Mon./ Divisa", style);
        createCell(row, 18, "Importe en ML", style);
        createCell(row, 19, "Referencia", style);
        createCell(row, 20, "Nº doc.", style);
        createCell(row, 21, "Clave referencia 3", style);
        createCell(row, 22, "Doc.compr.", style);
        createCell(row, 23, "Activo fijo", style);
        createCell(row, 24, "Elemento PEP", style);
        createCell(row, 25, "Usuario EM", style);
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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        style.setFont(font);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(GpsReport gps: gpsReports){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,gps.getNombre1(),style);
            createCell(row,columnCount++,gps.getRazon_social(),style);
            createCell(row,columnCount++,gps.getIdent_fis(),style);
            createCell(row,columnCount++,gps.getNif(),style);
            createCell(row,columnCount++,gps.getSoc(),style);
            createCell(row,columnCount++,gps.getClase(),style);
            createCell(row,columnCount++,gps.getCuenta(),style);
            createCell(row,columnCount++,gps.getCuenta_local(),style);
            createCell(row,columnCount++,Double.parseDouble(gps.getImporte_md()),style1);
            createCell(row,columnCount++,gps.getTipo_cambio(),style);
            createCell(row,columnCount++,gps.getMon1(),style);
            createCell(row,columnCount++,gps.getFecont(),style);
            createCell(row,columnCount++,gps.getEjercicioMes(),style);
            createCell(row,columnCount++,gps.getCe_coste(),style);
            createCell(row,columnCount++,gps.getFecha_doc(),style);
            createCell(row,columnCount++,gps.getTexto(),style);
            createCell(row,columnCount++,gps.getTexto_camb(),style);
            createCell(row,columnCount++,gps.getDivisa(),style);
            createCell(row,columnCount++,Double.parseDouble(gps.getImporte_ml()),style1);
            createCell(row,columnCount++,gps.getReferencia(),style);
            createCell(row,columnCount++,gps.getNumero_doc(),style);
            createCell(row,columnCount++,gps.getClave_3(),style);
            createCell(row,columnCount++,gps.getDoc_comp(),style);
            createCell(row,columnCount++,gps.getArchv_fijo(),style);
            createCell(row,columnCount++,gps.getElemento_pep(),style);
            createCell(row,columnCount++,gps.getUsuario_em(),style);
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
        sheet = workbook.createSheet("Log_GPS");
        Row row1 = sheet.createRow(0);
        Row row0 = sheet.createRow(1);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row1, 0, "Fila", style);
        createCell(row1, 1, "Columna", style);
        createCell(row1, 2, "Estado", style);
        createCell(row1, 3, "Registros Exitosos: ", style);
        createCell(row0, 3, "Resgitros Fallidos: ", style);
        createCell(row1, 4, gpsLogList.get(gpsLogList.size()-1)[0], style);
        createCell(row0, 4, gpsLogList.get(gpsLogList.size()-1)[1], style);

        int rowCount = 1;

        font.setBold(false);
        font.setFontHeight(10);

        gpsLogList.remove(gpsLogList.size()-1);
        for(String[] log: gpsLogList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style);
            createCell(row,columnCount++,log[1],style);
            createCell(row,columnCount++,log[2],style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
