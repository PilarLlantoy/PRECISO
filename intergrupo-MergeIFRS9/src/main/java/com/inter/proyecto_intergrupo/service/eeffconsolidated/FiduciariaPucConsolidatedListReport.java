package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaPucFiliales;
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

public class FiduciariaPucConsolidatedListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> pucFiduciaria;
    private List<FiduciariaPucFiliales> listPuc;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public FiduciariaPucConsolidatedListReport(List<String[]> PucList, List<FiduciariaPucFiliales> listPuc) {
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

        createCell(row, 0, "Tipo de Puc", style);
        createCell(row, 1, "Clase Cuenta", style);
        createCell(row, 2, "Cuenta", style);
        createCell(row, 3, "Nombre Cuenta", style);
        createCell(row, 4, "Nivel", style);
        createCell(row, 5, "Imputable", style);
        createCell(row, 6, "Naturaleza", style);
        createCell(row, 7, "Estado Cuenta", style);
        createCell(row, 8, "Descripcion Tipo Puc", style);

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

        for (FiduciariaPucFiliales rule : listPuc) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, rule.getTipoDePuc(), style);
            createCell(row, columnCount++, rule.getClaseCuenta(), style);
            createCell(row, columnCount++, rule.getCuenta(), style);
            createCell(row, columnCount++, rule.getNombreCuenta(), style);
            createCell(row, columnCount++, rule.getNivel(), style);
            createCell(row, columnCount++, rule.getImputable(), style);
            createCell(row, columnCount++, rule.getNaturaleza(), style);
            createCell(row, columnCount++, rule.getEstadoCuenta(), style);
            createCell(row, columnCount++, rule.getDescripcionTipoPuc(), style);

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
