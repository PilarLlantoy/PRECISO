package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.RejectionsCc;
import com.inter.proyecto_intergrupo.model.reports.Rp21;
import com.inter.proyecto_intergrupo.repository.parametric.TypeEntityRepository;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RejectionsCcListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;
    private List<RejectionsCc> rejectionsCcList;
    private List<Object[]> rejectionsCcObject;

    public RejectionsCcListReport(List<RejectionsCc> rejectionsCcList,List<Object[]> rejectionsCcObject) {
        this.rejectionsCcList = rejectionsCcList;
        this.rejectionsCcObject = rejectionsCcObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Rechazos");
        Row row = sheet.createRow(1);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        Row row1 = sheet.createRow(0);
        createCell(row1, 0, "En caso de no coincidir con la asignación del rechazo, proceda a parametrizar las nuevas lineas de negocio en la paramétrica de identificación de rechazos y a cargar nuevamente los documentos.", style);


        int columnCount = 0;

        createCell(row, columnCount++, "FECHA", style);
        createCell(row, columnCount++, "EMPRESA", style);
        createCell(row, columnCount++, "CENTRO", style);
        createCell(row, columnCount++, "CONTRATO", style);
        createCell(row, columnCount++, "CUENTA", style);
        createCell(row, columnCount++, "DIVISA", style);
        createCell(row, columnCount++, "STAGE", style);
        createCell(row, columnCount++, "SEGMENTO", style);
        createCell(row, columnCount++, "IMPORTE LOCAL", style);
        createCell(row, columnCount++, "VALOR2", style);
        createCell(row, columnCount++, "TIPO DE RECHAZO", style);
        createCell(row, columnCount++, "IMPORTE OPC", style);
        createCell(row, columnCount++, "TIPOCTA", style);
        createCell(row, columnCount++, "TIPO DE CUENTA", style);
        createCell(row, columnCount++, "LÍNEA DE PRODUCTO", style);
        createCell(row, columnCount++, "SEGMENTO", style);
        createCell(row, columnCount++, "TIPO DE RECHAZO REAL", style);
        createCell(row, columnCount++, "FUENTE", style);
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style) {
        //sheet.autoSizeColumn(columCount);
        Cell cell = row.createCell(columCount);

        if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        }

        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 2;
        int rowCountCell = 0;
        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        for (RejectionsCc rechazo : rejectionsCcList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, rechazo.getFecha(), style);
            createCell(row, columnCount++, rechazo.getEmpresa(), style);
            createCell(row, columnCount++, rechazo.getCentro(), style);
            createCell(row, columnCount++, rechazo.getContrato(), style);
            createCell(row, columnCount++, rechazo.getCuenta(), style);
            createCell(row, columnCount++, rechazo.getDivisa(), style);
            createCell(row, columnCount++, rechazo.getStage(), style);
            createCell(row, columnCount++, rechazo.getSegmento(), style);
            createCell(row, columnCount++, rechazo.getImporteLocal(), style1);
            createCell(row, columnCount++, rechazo.getValor2(), style);
            createCell(row, columnCount++, rechazo.getTipoRechazo(), style);
            createCell(row, columnCount++, rechazo.getImporteOpc(), style1);
            createCell(row, columnCount++, rechazo.getTipoCta(), style);
            createCell(row, columnCount++, rechazo.getTipoCuenta(), style);
            createCell(row, columnCount++, rechazo.getLineaProducto(), style);
            createCell(row, columnCount++, rechazo.getSegementoProceso(), style);
            createCell(row, columnCount++, rechazo.getTipoRechazoReal(), style);
            createCell(row, columnCount++, rechazo.getOrigen(), style);
            rowCountCell++;
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

    public void exportReport(HttpServletResponse response) throws IOException {
        writeHeaderLineReport();
        writeDataLinesReport();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    private void writeHeaderLineReport() {
        sheet = workbook.createSheet("Reporte Rechazos");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Centro", style);
        createCell(row, 1, "Empresa", style);
        createCell(row, 2, "Cuenta", style);
        createCell(row, 3, "Divisa", style);
        createCell(row, 4, "Tipo Rechazo Real", style);
    }

    private void writeDataLinesReport() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for (Object[] rechazo : rejectionsCcObject) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, rechazo[0], style);
            createCell(row, columnCount++, rechazo[1], style);
            createCell(row, columnCount++, rechazo[2], style);
            createCell(row, columnCount++, rechazo[3], style);
            createCell(row, columnCount++, rechazo[4], style);
        }
    }

    public void exportLog(HttpServletResponse response, List<String[]> lista) throws IOException {
        int position = 0;
        String[] temporalListNames = lista.get(lista.size() - 1);
        List<String> list = new ArrayList<>();
        if (temporalListNames != null) {
            String[] names = temporalListNames[0].split(".TXT");
            for (String name : names) {
                list.add(name.replace("null",""));
            }
        }
        if (list.contains("RECHAZOS_CUENTA_PROV_PLAN00")) {
            position = exportCuentaProv(response, lista, position);
        }
        if (list.contains("RECHAZOS_CUENTA_RECLASIFICACION_PLAN00")) {
            position = exportCuentaRecla(response, lista, position);
        }
        if (list.contains("RECHAZOS_CUENTA_IMPUESTOS")) {
            position = exportCuentaImpuestos(response, lista, position);
        }
        if (list.contains("RECHAZOS_RISTRA_PROV_PLAN00")) {
            position = exportRistraProv(response, lista, position);
        }
        if (list.contains("RECHAZOS_RISTRA_RECLASIFICACION_PLAN00")) {
            position = exportRistraRecla(response, lista, position);
        }
        if (list.contains("RECHAZOS_RISTRA_IMPUESTOS")) {
            position = exportRistraImpuesto(response, lista, position);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public int exportCuentaProv(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_CUENTA_PROV");
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
        createCell(row2, 0, "INFORME CTA PROV", style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("CUENTA PROV"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }

    public int exportCuentaRecla(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_CUENTA_RECLASIFICACION");
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
        createCell(row2, 0, "INFORME CTA RCLC", style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("CUENTA RECLASIFICACION"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }

    public int exportCuentaImpuestos(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_CUENTA_IMPUESTOS");
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
        createCell(row2, 0, "INFORME CTA IMPU", style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("CUENTA IMPUESTOS"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }

    public int exportRistraProv(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_RISTRAS_PROV");
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
        createCell(row2, 0, "INFORME RIS PROV", style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("RISTRAS PROV"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }
        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }

    public int exportRistraRecla(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_RISTRAS_RECLASIFICACION");
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
        createCell(row2, 0, "INFORME RIS RCLC", style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("RISTRAS RECLASIFICACION"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }

    public int exportRistraImpuesto(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_RISTRAS_IMPUESTOS");
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
        createCell(row2, 0, "INFORME RIS IMPU", style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("RISTRAS IMPUESTOS"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }
        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }
}
