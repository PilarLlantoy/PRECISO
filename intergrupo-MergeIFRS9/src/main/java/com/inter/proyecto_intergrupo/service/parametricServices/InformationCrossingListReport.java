package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.CampoRConcil;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
import com.inter.proyecto_intergrupo.model.parametric.LogInformationCrossing;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class InformationCrossingListReport {
    private XSSFWorkbook workbook;
    private Map<String, XSSFSheet> sheets;
    private List<String> colCroutes;
    private List<Object[]> aroutes;
    private Conciliation cr;

    private EntityManager entityManager;

    public InformationCrossingListReport(List<Object[]> aroutes, List<String> colCroutes, Conciliation cr,EntityManager entityManager){
        this.aroutes = aroutes;
        this.colCroutes = colCroutes;
        this.cr = cr;
        this.workbook = new XSSFWorkbook();
        this.sheets = new HashMap<>();
        this.entityManager = entityManager;
    }

    private void writeHeaderLine(){
        String sheetName = cr.getNombre().replace(" ","_");
        XSSFSheet sheet = workbook.createSheet(sheetName);

        Row row = sheet.createRow(0);
        int count = 0;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        for (String campo :colCroutes) {
            createCell(row, count++, campo.toUpperCase(), style);
        }

        sheets.put(sheetName, sheet);
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
        String sheetName = cr.getNombre().replace(" ","_");

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);


        CellStyle style1 = workbook.createCellStyle();
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        CellStyle style2 = workbook.createCellStyle();
        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        CellStyle style3 = workbook.createCellStyle();
        style3.setFont(font);
        style3.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));

        for(Object[] data: aroutes){
            Row row = sheets.get(sheetName).createRow(rowCount++);
            int columnCount = 0;
            if(data[0]!=null) createCell(row,columnCount++,data[0].toString(),style3); else createCell(row,columnCount++,"",style);
            if(data[1]!=null) createCell(row,columnCount++,data[1].toString(),style); else createCell(row,columnCount++,"",style);
            if(data[2]!=null) createCell(row,columnCount++,data[2].toString(),style); else createCell(row,columnCount++,"",style);
            if(data[3]!=null) createCell(row,columnCount++,data[3].toString(),style); else createCell(row,columnCount++,"",style);
            if(data[4]!=null) createCell(row,columnCount++,Double.parseDouble(data[4].toString()),style1); else createCell(row,columnCount++,0.0,style1);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush(); // Asegúrate de que todos los datos se envíen
        outputStream.close();
    }

    public void exportDetail(HttpServletResponse response,List<ConciliationRoute> crList, String fecha) throws IOException {
        writeHeaderLineDetail(crList,fecha);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
        outputStream.close();
    }

    private void writeHeaderLineDetail(List<ConciliationRoute> crList,String fecha){
        for (ConciliationRoute ruta :crList) {
            String sheetName = ruta.getDetalle().replace(" ", "_");
            XSSFSheet sheet = workbook.createSheet(sheetName);

            Row row0 = sheet.createRow(0);
            int columCount = 0;

            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(10);
            style.setFont(font);

            CellStyle style1 = workbook.createCellStyle();
            style1.setFont(font);
            style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            CellStyle style2 = workbook.createCellStyle();
            style2.setFont(font);
            style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

            CellStyle style3 = workbook.createCellStyle();
            style3.setFont(font);
            style3.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));

            Query query = entityManager.createNativeQuery("SELECT COLUMN_NAME,DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'preciso_ci_"+cr.getId()+"_"+ruta.getId()+"' ");
            List<Object[]> campos =  query.getResultList();
            for (Object[] campo :campos) {
                createCell(row0,columCount++,campo[0].toString(),style);
            }

            Query query1 = entityManager.createNativeQuery("SELECT * FROM preciso_ci_"+cr.getId()+"_"+ruta.getId()+" WHERE FECHA_CONCILIACION = ? ");
            query1.setParameter(1,fecha);
            List<Object[]> datas =  query1.getResultList();
            int rowCount = 1;

            for(Object[] data: datas){
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;
                for (int i =0; i<data.length;i++)
                {
                    if(campos.get(i)[1].toString().equalsIgnoreCase("float"))
                        if(data[i]!=null) createCell(row,columnCount++,Double.parseDouble(data[i].toString()),style1); else createCell(row,columnCount++,"",style);
                    else if(campos.get(i)[1].toString().equalsIgnoreCase("int"))
                        if(data[i]!=null) createCell(row,columnCount++,Integer.parseInt(data[i].toString()),style2); else createCell(row,columnCount++,"",style);
                    else
                        if(data[i]!=null) createCell(row,columnCount++,data[i].toString(),style); else createCell(row,columnCount++,"",style);
                }
            }

            sheets.put(sheetName, sheet);
        }
    }

    public void exportNove(HttpServletResponse response, List<LogInformationCrossing> crList, String fecha) throws IOException {
        writeHeaderLineNove(crList,fecha);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
        outputStream.close();
    }

    private void writeHeaderLineNove(List<LogInformationCrossing> crList,String fecha){
        XSSFSheet sheet = workbook.createSheet("Novedades");

        Row row0 = sheet.createRow(0);
        int columCount = 0;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        CellStyle style1 = workbook.createCellStyle();
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        CellStyle style2 = workbook.createCellStyle();
        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

        CellStyle style3 = workbook.createCellStyle();
        style3.setFont(font);
        style3.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));

        Query query = entityManager.createNativeQuery("SELECT COLUMN_NAME,DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'preciso_log_cruce_informacion' ");
        List<Object[]> campos =  query.getResultList();
        for (Object[] campo :campos) {
            createCell(row0,columCount++,campo[0].toString(),style);
        }
        int rowCount = 1;

        for (int i =0; i<crList.size();i++)
        {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            if(campos.get(i)[1].toString().equalsIgnoreCase("float"))
                if(crList.get(i)!=null) createCell(row,columnCount++,Double.parseDouble(crList.get(i).toString()),style1); else createCell(row,columnCount++,"",style);
            else if(campos.get(i)[1].toString().equalsIgnoreCase("int"))
                if(crList.get(i)!=null) createCell(row,columnCount++,Integer.parseInt(crList.get(i).toString()),style2); else createCell(row,columnCount++,"",style);
            else
            if(crList.get(i)!=null) createCell(row,columnCount++,crList.get(i).toString(),style); else createCell(row,columnCount++,"",style);

        }
        sheets.put("Novedades", sheet);
    }
}
