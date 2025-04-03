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

import static com.inter.proyecto_intergrupo.controller.parametric.AccountingLoadController.columnasOmitir;

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

    public void exportDetail(HttpServletResponse response,List<ConciliationRoute> crList, String fecha, String evento) throws IOException {
        writeHeaderLineDetail(crList,fecha,evento);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
        outputStream.close();
    }

    private void writeHeaderLineDetail(List<ConciliationRoute> crList,String fecha , String evento){
        for (ConciliationRoute ruta :crList) {
            String sheetName = ruta.getDetalle().replace(" ", "_");
            XSSFSheet sheet = workbook.createSheet(sheetName);

            Row row0 = sheet.createRow(0);
            int columCount = 0;
            StringBuilder  listCampos = new StringBuilder("");

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
                if(!campo[0].toString().equalsIgnoreCase("NOVEDADES_PRECISOKEY")) {
                    listCampos.append(campo[0].toString()).append(",");
                    createCell(row0, columCount++, campo[0].toString().replace("_PRECISOKEY", ""), style);
                }
            }
            listCampos.setLength(listCampos.length()-1);

            Query query1 = entityManager.createNativeQuery("SELECT "+listCampos.toString()+" FROM preciso_ci_"+cr.getId()+"_"+ruta.getId()+" WHERE FECHA_CONCILIACION_PRECISOKEY = ? AND TIPO_EVENTO_PRECISOKEY = (select nombre_tipo_evento from preciso_tipo_evento where id_tipo_evento = ? ) ");
            query1.setParameter(1,fecha);
            query1.setParameter(2,evento);
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

    public void exportNove(HttpServletResponse response, List<ConciliationRoute> crList, String fecha,String evento) throws IOException {
        writeHeaderLineNove(crList,fecha,evento);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
        outputStream.close();
    }

    private void writeHeaderLineNove(List<ConciliationRoute> crList,String fecha, String evento){
        for (ConciliationRoute ruta :crList) {
            Query firstQuery = entityManager.createNativeQuery("select string_agg( NOVEDADES_PRECISOKEY , ',') as resultado from \n" +
            "(select distinct NOVEDADES_PRECISOKEY from preciso_ci_"+ruta.getConciliacion().getId()+ "_"+ruta.getId()+" t WHERE t.FECHA_CONCILIACION_PRECISOKEY like '"+fecha+"%' and t.TIPO_EVENTO_PRECISOKEY ='"+evento+"' and t.NOVEDADES_PRECISOKEY !='' and t.NOVEDADES_PRECISOKEY IS NOT NULL) as subquery");
            List<String> validation = firstQuery.getResultList();
            if(!validation.isEmpty() && validation.get(0)!=null &&validation.get(0).toString().contains("A"))
                createSheet(ruta,fecha,evento,"_CUENTAS","A");
            if(!validation.isEmpty() && validation.get(0)!=null  && validation.get(0).toString().contains("D"))
                createSheet(ruta,fecha,evento,"_DUPLICADOS","D");
        }
    }

    public void createSheet(ConciliationRoute ruta,String fecha, String evento, String novedad,String indica){
        String sheetName = ruta.getDetalle().replace(" ", "_")+novedad;
        XSSFSheet sheet = workbook.createSheet(sheetName);

        Row row0 = sheet.createRow(0);
        int columCount = 0;
        StringBuilder  listCampos = new StringBuilder("");

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
            if(!campo[0].toString().equalsIgnoreCase("NOVEDADES_PRECISOKEY")) {
                listCampos.append(campo[0].toString()).append(",");
                createCell(row0, columCount++, campo[0].toString().replace("_PRECISOKEY", ""), style);
            }
        }
        listCampos.setLength(listCampos.length()-1);

        Query query1 = entityManager.createNativeQuery("SELECT "+listCampos.toString()+" FROM preciso_ci_"+cr.getId()+"_"+ruta.getId()+" WHERE FECHA_CONCILIACION_PRECISOKEY = ? AND TIPO_EVENTO_PRECISOKEY = ? AND NOVEDADES_PRECISOKEY = ? ");
        query1.setParameter(1,fecha);
        query1.setParameter(2,evento);
        query1.setParameter(3,indica);
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
