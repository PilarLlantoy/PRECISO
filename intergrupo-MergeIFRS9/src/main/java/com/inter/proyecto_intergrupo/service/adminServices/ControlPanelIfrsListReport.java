package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.ControlPanelIfrs;
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

public class ControlPanelIfrsListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ControlPanelIfrs> controlList;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ControlPanelIfrsListReport(List<ControlPanelIfrs> controlList){
        this.controlList = controlList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Panel De Control");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Componente", style);
        createCell(row, 1, "Semaforo_Componente", style);
        createCell(row, 2, "Input", style);
        createCell(row, 3, "Semaforo Input", style);
        createCell(row, 4, "Fecha Cargue", style);
        createCell(row, 5, "Empresa", style);
        createCell(row, 6, "Usuario Carga", style);
        createCell(row, 7, "Estado", style);

    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
        Cell cell = row.createCell(columCount);

        if(value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if(value instanceof Integer){
            cell.setCellValue((Integer) value);
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        } else if(value instanceof String){
            cell.setCellValue((String) value);
        }else if(value instanceof Long){
            cell.setCellValue((Long) value);
        }else if(value instanceof Double){
            cell.setCellValue((Double) value);
        }

        cell.setCellStyle(style);
    }

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(ControlPanelIfrs controlPanel: controlList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,controlPanel.getComponente(),style);
            createCell(row,columnCount++,controlPanel.getSemaforoComponente(),style);
            createCell(row,columnCount++,controlPanel.getInput(),style);
            createCell(row,columnCount++,controlPanel.getSemaforoInput(),style);
            createCell(row,columnCount++,controlPanel.getFechaCargue(),style);
            createCell(row,columnCount++,controlPanel.getEmpresa(),style);
            createCell(row,columnCount++,controlPanel.getUsuarioCarga(),style);
            if (controlPanel.getEstado()==true)
                createCell(row,columnCount++,"Activo",style);
            else
                createCell(row,columnCount++,"Inactivo",style);
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

}
