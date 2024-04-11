package com.inter.proyecto_intergrupo.service.informationServices;

import com.inter.proyecto_intergrupo.model.ifrs9.Neocon60;
import com.inter.proyecto_intergrupo.model.information.Neocon60Ajuste;
import com.inter.proyecto_intergrupo.model.information.Neocon60Carga;
import com.inter.proyecto_intergrupo.model.information.Neocon60Cuadre;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
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

public class Neocon60ListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> neoconList;
    private List<Neocon60> listNeocon;
    private List<Neocon60Ajuste> listNeoconAju;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public Neocon60ListReport(List<String[]> neoconList, List<Neocon60> listNeocon,List<Neocon60Ajuste> listNeoconAju) {
        this.neoconList = neoconList;
        this.listNeocon = listNeocon;
        this.listNeoconAju = listNeoconAju;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Neocon 60");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int count = 0;
        createCell(row, count++, "Año", style);
        createCell(row, count++, "Mes", style);
        createCell(row, count++, "YNTP Reportante", style);
        createCell(row, count++, "Codicons", style);
        createCell(row, count++, "Divisa", style);
        createCell(row, count++, "Pais", style);
        createCell(row, count++, "YNTP Intergrupo", style);
        createCell(row, count++, "Saldo", style);
        createCell(row, count++, "Naturaleza", style);
        createCell(row, count++, "Pais Contraparte", style);
    }

    private void writeHeaderLineCuadre(){
        sheet = workbook.createSheet("Neocon 60 Cuadre");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int count = 0;
        createCell(row, count++, "Codicons", style);
        createCell(row, count++, "Divisa", style);
        createCell(row, count++, "Saldo Base", style);
        createCell(row, count++, "Saldo Ajuste Contabilidad", style);
        createCell(row, count++, "Saldo Ajuste Neocon", style);
        createCell(row, count++, "Saldo Carga Masiva", style);
        createCell(row, count++, "Saldo Recla InterV2", style);
        createCell(row, count++, "Saldo Recla Avales", style);
        createCell(row, count++, "Saldo Total", style);
        createCell(row, count++, "Saldo Plano Inter V2", style);
        createCell(row, count++, "Validación", style);
    }

    private void writeHeaderLineAjuste(){
        sheet = workbook.createSheet("Neocon 60 Ajuste");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int count = 0;
        createCell(row, count++, "Año", style);
        createCell(row, count++, "Mes", style);
        createCell(row, count++, "Codicons", style);
        createCell(row, count++, "Divisa", style);
        createCell(row, count++, "Saldo", style);
        createCell(row, count++, "Observación", style);
        createCell(row, count++, "Aplica", style);
    }

    private void writeHeaderLineCarga(){
        sheet = workbook.createSheet("Neocon 60 Carga Masiva");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int count = 0;
        createCell(row, count++, "Año", style);
        createCell(row, count++, "Mes", style);
        createCell(row, count++, "Codicons", style);
        createCell(row, count++, "Divisa", style);
        createCell(row, count++, "Saldo", style);
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
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Neocon60 neocon60: listNeocon){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,neocon60.getAno(),style);
            createCell(row,columnCount++,neocon60.getMes(),style);
            createCell(row,columnCount++,neocon60.getYntpReportante(),style);
            createCell(row,columnCount++,neocon60.getCodicons(),style);
            createCell(row,columnCount++,neocon60.getDivisa(),style);
            createCell(row,columnCount++,neocon60.getPais(),style);
            createCell(row,columnCount++,neocon60.getYntpIntergrupo(),style);
            createCell(row,columnCount++,neocon60.getSaldo(),style1);
            createCell(row,columnCount++,neocon60.getNaturaleza(),style);
            createCell(row,columnCount++,neocon60.getPaisContraparte(),style);
        }
    }

    private void writeDataLinesCuadre(List<Neocon60Cuadre> list){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Neocon60Cuadre neocon60: list){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,neocon60.getCodicons(),style);
            createCell(row,columnCount++,neocon60.getDivisa(),style);
            createCell(row,columnCount++,neocon60.getBase(),style1);
            createCell(row,columnCount++,neocon60.getAjuste(),style1);
            createCell(row,columnCount++,neocon60.getAjuste2(),style1);
            createCell(row,columnCount++,neocon60.getCarga(),style1);
            createCell(row,columnCount++,neocon60.getIntergrupo(),style1);
            createCell(row,columnCount++,neocon60.getContingentes(),style1);
            createCell(row,columnCount++,neocon60.getTotal(),style1);
            createCell(row,columnCount++,neocon60.getPlano(),style1);
            createCell(row,columnCount++,neocon60.getEstado(),style);
        }
    }

    private void writeDataLinesAjuste(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Neocon60Ajuste neocon60: listNeoconAju){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,neocon60.getAno(),style);
            createCell(row,columnCount++,neocon60.getMes(),style);
            createCell(row,columnCount++,neocon60.getCodicons(),style);
            createCell(row,columnCount++,neocon60.getDivisa(),style);
            createCell(row,columnCount++,neocon60.getSaldo(),style1);
            createCell(row,columnCount++,neocon60.getObservacion(),style);
            createCell(row,columnCount++,neocon60.getAplica(),style);
        }
    }

    private void writeDataLinesCarga(List<Object[]> lista){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] neocon60: lista){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,neocon60[0].toString(),style);
            createCell(row,columnCount++,neocon60[1].toString(),style);
            createCell(row,columnCount++,neocon60[2].toString(),style);
            createCell(row,columnCount++,neocon60[3].toString(),style);
            createCell(row,columnCount++,Double.parseDouble(neocon60[4].toString()),style1);
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

    public void exportCuadre(HttpServletResponse response, List<Neocon60Cuadre> lista) throws IOException {
        writeHeaderLineCuadre();
        writeDataLinesCuadre(lista);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportAjustado(HttpServletResponse response) throws IOException {
        writeHeaderLineAjuste();
        writeDataLinesAjuste();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportCarga(HttpServletResponse response, List<Object[]> lista) throws IOException {
        writeHeaderLineCarga();
        writeDataLinesCarga(lista);

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

        createCell(row1, 0, neoconList.get(neoconList.size()-1)[0], style1);
        createCell(row1, 1, neoconList.get(neoconList.size()-1)[1], style1);
        createCell(row1, 2, neoconList.get(neoconList.size()-1)[2], style1);

        if(neoconList.size()>1)
        {
            neoconList.remove(neoconList.size()-1);
            Row row2 = sheet.createRow(2);
            createCell(row2, 0, "Fila", style);
            createCell(row2, 1, "Columna", style);
            createCell(row2, 2, "Estado", style);

            int rowCount = 3;
            for (String[] log : neoconList)
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
