package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaPucFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ValoresPucFiliales;
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

public class ValoresPucConsolidatedListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> pucFiduciaria;
    private List<ValoresPucFiliales> listPuc;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ValoresPucConsolidatedListReport(List<String[]> PucList, List<ValoresPucFiliales> listPuc) {
        this.pucFiduciaria = PucList;
        this.listPuc = listPuc;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("EEFF");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Cuenta", style);
        createCell(row, 1, "Nombre Cuenta", style);
        createCell(row, 2, "Tipo de Cuenta", style);
        createCell(row, 3, "Maneja Costos", style);
        createCell(row, 4, "Maneja Cierre", style);
        createCell(row, 5, "Maneja Movimiento", style);
        createCell(row, 6, "Maneja Moneda", style);
        createCell(row, 7, "Maneja Ajustes", style);
        createCell(row, 8, "Presupuesto", style);
        createCell(row, 9, "Porcentaje Impuesto", style);
        createCell(row, 10, "Cta PUC", style);
        createCell(row, 11, "Flujo efectivo", style);
        createCell(row, 12, "Codigo Flujo Efectivo", style);
        createCell(row, 13, "Naturaleza", style);
        createCell(row, 14, "Cuenta orden Super Valores", style);
        createCell(row, 15, "Cuenta ajuste Diferencia en cambio", style);
        createCell(row, 16, "Maneja Segmento", style);
        createCell(row, 17, "Codigo norma contable", style);
        createCell(row, 18, "Maneja Kardex", style);
        createCell(row, 19, "Moneda", style);
        createCell(row, 20, "Usuario Actualizacion", style);
        createCell(row, 21, "Fecha Actualizacion", style);
        createCell(row, 22, "Usuario Creacion", style);
        createCell(row, 23, "Fecha Creacion", style);

    }

    private void createCell(Row row, int columCount, Object value, CellStyle style) {
        Cell cell = row.createCell(columCount);

        if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        }

        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for (ValoresPucFiliales rule : listPuc) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, rule.getIdcuenta(), style);
            createCell(row, columnCount++, rule.getNombreCuenta(), style);
            createCell(row, columnCount++, rule.getTipoCuenta(), style);
            createCell(row, columnCount++, rule.getManejaCostos(), style);
            createCell(row, columnCount++, rule.getManejaCierre(), style);
            createCell(row, columnCount++, rule.getManejaMovimientos(), style1);
            createCell(row, columnCount++, rule.getMoneda(), style1);
            createCell(row, columnCount++, rule.getManejaAjustes(), style1);
            createCell(row, columnCount++, rule.getPresupuesto(), style1);
            createCell(row, columnCount++, rule.getPorcentajeImpuesto(), style);
            createCell(row, columnCount++, rule.getCtaPuc(), style);
            createCell(row, columnCount++, rule.getFlujoEfecivo(), style1);
            createCell(row, columnCount++, rule.getCodigoFlujoEfectivo(), style1);
            createCell(row, columnCount++, rule.getNaturaleza(), style);
            createCell(row, columnCount++, rule.getCuentaOrdenSuperValores(), style);
            createCell(row, columnCount++, rule.getCuentaOrdenDiferenciaEnCambio(), style);
            createCell(row, columnCount++, rule.getManejaSegmento(), style);
            createCell(row, columnCount++, rule.getCodigoNormaContable(), style);
            createCell(row, columnCount++, rule.getManejaKardex(), style);
            createCell(row, columnCount++, rule.getMoneda(), style);
            createCell(row, columnCount++, rule.getUsuarioActualizacion(), style);
            createCell(row, columnCount++, rule.getFechaActualizacion(), style);
            createCell(row, columnCount++, rule.getUsuarioCreacion(), style);
            createCell(row, columnCount++, rule.getFechaCreacion(), style);
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

    public void exportLog(HttpServletResponse response) throws IOException {
        sheet = workbook.createSheet("Log_Filiales");
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

        createCell(row1, 0, pucFiduciaria.get(pucFiduciaria.size() - 1)[0], style1);
        createCell(row1, 1, pucFiduciaria.get(pucFiduciaria.size() - 1)[1], style1);
        createCell(row1, 2, pucFiduciaria.get(pucFiduciaria.size() - 1)[2], style1);

        if (pucFiduciaria.size() > 1) {
            pucFiduciaria.remove(pucFiduciaria.size() - 1);
            Row row2 = sheet.createRow(2);
            createCell(row2, 0, "Fila", style);
            createCell(row2, 1, "Columna", style);
            createCell(row2, 2, "Estado", style);

            int rowCount = 3;
            for (String[] log : pucFiduciaria) {
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
