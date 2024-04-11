package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuditListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Audit> auditList;
    //private String filename = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\infocontable\\01-NEXCO\\";
    private String filename = "\\\\co.igrupobbva\\svrfilesystem\\TX\\ENVIO_HOST\\XC\\CONSOLIDACION\\";

    public AuditListReport(List<Audit> auditList){
        this.auditList = auditList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Auditoría");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Id Auditoría", style);
        createCell(row, 1, "Usuario", style);
        createCell(row, 2, "Nombre", style);
        createCell(row, 3, "Centro Costos", style);
        createCell(row, 4, "Componente", style);
        createCell(row, 5, "Input", style);
        createCell(row, 6, "Acción", style);
        createCell(row, 7, "Fecha", style);
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
        sheet.autoSizeColumn(columCount);
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

        for(Audit audit: auditList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row,columnCount++,audit.getIdAuditoria(),style);
            createCell(row,columnCount++,audit.getUsuario(),style);
            createCell(row,columnCount++,audit.getNombre(),style);
            createCell(row,columnCount++,audit.getCentro(),style);
            createCell(row,columnCount++,audit.getComponente(),style);
            createCell(row,columnCount++,audit.getInput(),style);
            createCell(row,columnCount++,audit.getAccion(),style);
            createCell(row,columnCount++,audit.getFecha().toString(),style);
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

    public void exportFile() throws IOException {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Logs auditoria");
        HSSFRow rowhead = sheet.createRow((short)0);
        rowhead.createCell(0).setCellValue("Id Auditoría");
        rowhead.createCell(1).setCellValue("Usuario");
        rowhead.createCell(2).setCellValue("Nombre");
        rowhead.createCell(3).setCellValue("Centro Costos");
        rowhead.createCell(4).setCellValue("Componente");
        rowhead.createCell(5).setCellValue("Input");
        rowhead.createCell(6).setCellValue("Acción");
        rowhead.createCell(7).setCellValue("Fecha");

        for(int i=0; i< auditList.size();i++){
            int line = i+1;
            HSSFRow row = sheet.createRow((short)line);
            row.createCell(0).setCellValue(auditList.get(i).getIdAuditoria());
            row.createCell(1).setCellValue(auditList.get(i).getUsuario());
            row.createCell(2).setCellValue(auditList.get(i).getNombre());
            row.createCell(3).setCellValue(auditList.get(i).getCentro());
            row.createCell(4).setCellValue(auditList.get(i).getComponente());
            row.createCell(5).setCellValue(auditList.get(i).getInput());
            row.createCell(6).setCellValue(auditList.get(i).getAccion());
            row.createCell(7).setCellValue(auditList.get(i).getFecha().toString());
        }

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM");
        String currentDateTime = dateFormatter.format(new Date());

        FileOutputStream fileOut = new FileOutputStream(filename + "Log_Auditoria_"+currentDateTime + ".xlsx");


        workbook.write(fileOut);
        workbook.close();

        fileOut.close();
    }
}
