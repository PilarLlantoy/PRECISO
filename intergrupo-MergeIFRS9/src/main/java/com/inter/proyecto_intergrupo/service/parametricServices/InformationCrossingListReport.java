package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class InformationCrossingListReport {
    private XSSFWorkbook workbook;
    private Map<String, XSSFSheet> sheets;
    private List<String> colCroutes;
    private List<Object[]> aroutes;
    private Conciliation cr;

    private EntityManager entityManager;

    public static final List<String> listHeaderCruce = Arrays.asList("INVENTARIO_PRECISOKEY","ID_INVENTARIO_PRECISOKEY","FECHA_CONCILIACION_PRECISOKEY","TIPO_EVENTO_PRECISOKEY","CDGO_MATRIZ_EVENTO_PRECISOKEY","CENTRO_CONTABLE_PRECISOKEY","CUENTA_CONTABLE_1_PRECISOKEY","DIVISA_CUENTA_1_PRECISOKEY","VALOR_CUENTA_1_PRECISOKEY","CUENTA_CONTABLE_2_PRECISOKEY","DIVISA_CUENTA_2_PRECISOKEY","VALOR_CUENTA_2_PRECISOKEY");
    public static final List<String> listHeaderCruceCons = Arrays.asList("FECHA_CONCILIACION","NUMERO_OPERACION","CENTRO_CONTABLE","CUENTA_CONTABLE","DIVISA_CUENTA","VALOR_CUENTA");
    public InformationCrossingListReport(List<Object[]> aroutes, List<String> colCroutes, Conciliation cr,EntityManager entityManager){
        this.aroutes = aroutes;
        this.colCroutes = colCroutes;
        this.cr = cr;
        this.workbook = new XSSFWorkbook();
        this.sheets = new HashMap<>();
        this.entityManager = entityManager;
    }

    private void writeHeaderLine(String nombre){
        String sheetName = cr.getNombre().replace(" ","_")+nombre;
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

    private void writeDataLines(String nombre){
        int rowCount = 1;
        String sheetName = cr.getNombre().replace(" ","_")+nombre;

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
            if(data.length>5) {if(data[5]!=null) createCell(row,columnCount++,Double.parseDouble(data[5].toString()),style1); else createCell(row,columnCount++,0.0,style1);}
            if(data.length>6) {if(data[6]!=null) createCell(row,columnCount++,Double.parseDouble(data[6].toString()),style1); else createCell(row,columnCount++,0.0,style1);}
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine("");
        writeDataLines("");

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush(); // Asegúrate de que todos los datos se envíen
        outputStream.close();
    }

    public void exportConsol(HttpServletResponse response,List<ConciliationRoute> crList, String fecha) throws IOException {
        StringBuilder data = writeHeaderLineDetail1(crList,fecha,null);
        writeHeaderLineDetail2(crList,fecha,null,data);
        writeHeaderLine("_CONCILIACION");
        writeDataLines("_CONCILIACION");

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush(); // Asegúrate de que todos los datos se envíen
        outputStream.close();
    }

    public void exportDetail(HttpServletResponse response,List<ConciliationRoute> crList, String fecha, String evento) throws IOException {
        StringBuilder data = writeHeaderLineDetail1(crList,fecha,evento);
        writeHeaderLineDetail2(crList,fecha,evento,data);
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.flush();
        outputStream.close();
    }

    private StringBuilder writeHeaderLineDetail1(List<ConciliationRoute> crList, String fecha , String evento) {
        StringBuilder stringQuery = new StringBuilder("");
        for (ConciliationRoute ruta : crList) {
            String sheetName = ruta.getDetalle().replace(" ", "_");
            XSSFSheet sheet = workbook.createSheet(sheetName);

            Row row0 = sheet.createRow(0);
            int columCount = 0;
            StringBuilder listCampos = new StringBuilder("");

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

            CellStyle style4 = workbook.createCellStyle();
            XSSFFont font4 = workbook.createFont();
            font4.setFontHeight(10);
            font4.setBold(true);
            style4.setFont(font4);

            Query query = entityManager.createNativeQuery("select COLUMN_NAME,coalesce(tipo,DATA_TYPE) as tipo from \n" +
                    "(SELECT COLUMN_NAME,DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'preciso_ci_" + cr.getId() + "_" + ruta.getId() + "') a\n" +
                    "INNER JOIN (select nombre,tipo from preciso_campos_rconcil where id_rconcil = " + ruta.getId() + " ) b on a.COLUMN_NAME = b.nombre");
            List<Object[]> campos = query.getResultList();

            //Ordenar campos

            for (Object[] campo : campos) {
                listCampos.append(",").append(campo[0].toString());
                createCell(row0, columCount++, campo[0].toString().toUpperCase(), style4);
            }

            for (String campo : listHeaderCruce) {
                listCampos.append(",").append(campo);
                createCell(row0, columCount++, campo.toUpperCase().replace("_PRECISOKEY", ""), style4);
            }

            listCampos.delete(0, 1);

            String dataEvento = "";

            if(evento!=null)
                dataEvento=" AND TIPO_EVENTO_PRECISOKEY = (select nombre_tipo_evento from preciso_tipo_evento where id_tipo_evento = :evento )";

            //Lleno la Info

            Query query1 = entityManager.createNativeQuery("SELECT " + listCampos.toString() + " FROM preciso_ci_" + cr.getId() + "_" + ruta.getId() + " WHERE FECHA_CONCILIACION_PRECISOKEY = :fecha "+dataEvento);
            query1.setParameter("fecha", fecha);
            if(evento!=null)
                query1.setParameter("evento", evento);
            List<Object[]> datas = query1.getResultList();
            int rowCount = 1;

            for (Object[] data : datas) {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;
                for (int i = 0; i < campos.size(); i++) {
                    if (campos.get(i)[1].toString().equalsIgnoreCase("float"))
                        if (data[i] != null)
                            createCell(row, columnCount++, Double.parseDouble(data[i].toString()), style1);
                        else createCell(row, columnCount++, "", style);
                    else if (campos.get(i)[1].toString().equalsIgnoreCase("int"))
                        if (data[i] != null)
                            createCell(row, columnCount++, Integer.parseInt(data[i].toString()), style2);
                        else createCell(row, columnCount++, "", style);
                    else if (campos.get(i)[1].toString().equalsIgnoreCase("Date") || campos.get(i)[1].toString().equalsIgnoreCase("DateTime"))
                        if (data[i] != null) createCell(row, columnCount++, normalizeDate(data[i].toString()), style3);
                        else createCell(row, columnCount++, "", style);
                    else if (data[i] != null) createCell(row, columnCount++, data[i].toString(), style);
                    else createCell(row, columnCount++, "", style);
                }

                for (int i = campos.size(); i < campos.size() + listHeaderCruce.size(); i++) {
                    if (i == (campos.size() + 8) || i == (campos.size() + 11))
                        if (data[i] != null)
                            createCell(row, columnCount++, Double.parseDouble(data[i].toString()), style1);
                        else createCell(row, columnCount++, "", style);
                    else if (data[i] != null) createCell(row, columnCount++, data[i].toString(), style);
                    else createCell(row, columnCount++, "", style);
                }

            }

            sheets.put(sheetName, sheet);

            stringQuery.append("UNION ALL\n");
            stringQuery.append("select FECHA_CONCILIACION_PRECISOKEY,OPERACION_PRECISOKEY,CENTRO_CONTABLE_PRECISOKEY,CUENTA_CONTABLE_1_PRECISOKEY as CUENTA_CONTABLE_PRECISOKEY,DIVISA_CUENTA_1_PRECISOKEY as DIVISA_CUENTA_PRECISOKEY,VALOR_CUENTA_1_PRECISOKEY as VALOR_CUENTA_PRECISOKEY\n");
            stringQuery.append("from preciso_ci_" + cr.getId() + "_" + ruta.getId() + " where FECHA_CONCILIACION_PRECISOKEY = :fecha and CUENTA_CONTABLE_1_PRECISOKEY is not null "+dataEvento+"\n");
            stringQuery.append("UNION ALL\n");
            stringQuery.append("select FECHA_CONCILIACION_PRECISOKEY,OPERACION_PRECISOKEY,CENTRO_CONTABLE_PRECISOKEY,CUENTA_CONTABLE_2_PRECISOKEY as CUENTA_CONTABLE_PRECISOKEY,DIVISA_CUENTA_2_PRECISOKEY as DIVISA_CUENTA_PRECISOKEY,VALOR_CUENTA_2_PRECISOKEY as VALOR_CUENTA_PRECISOKEY\n");
            stringQuery.append("from preciso_ci_" + cr.getId() + "_" + ruta.getId() + " where FECHA_CONCILIACION_PRECISOKEY = :fecha and CUENTA_CONTABLE_2_PRECISOKEY is not null "+dataEvento+"\n");
        }
        return stringQuery;
    }
    private void writeHeaderLineDetail2(List<ConciliationRoute> crList,String fecha , String evento,StringBuilder stringQuery){

        String sheetName =cr.getNombre().replace(" ","_")+"_OPERACIONES";
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

        CellStyle style4 = workbook.createCellStyle();
        XSSFFont font4 = workbook.createFont();
        font4.setFontHeight(10);
        font4.setBold(true);
        style4.setFont(font4);

        //Ordenar campos

        for (String campo :listHeaderCruceCons) {
            listCampos.append(",").append(campo);
            createCell(row0, columCount++, campo, style4);
        }

        listCampos.delete(0,1);
        stringQuery.delete(0,9);

        //Lleno la Info

        Query query1 = entityManager.createNativeQuery("select FECHA_CONCILIACION_PRECISOKEY,OPERACION_PRECISOKEY,CENTRO_CONTABLE_PRECISOKEY,CUENTA_CONTABLE_PRECISOKEY,DIVISA_CUENTA_PRECISOKEY,sum(VALOR_CUENTA_PRECISOKEY) as Valor_final from\n" +
                "("+stringQuery.toString()+") pr\n" +
                "group by FECHA_CONCILIACION_PRECISOKEY,OPERACION_PRECISOKEY,CENTRO_CONTABLE_PRECISOKEY,CUENTA_CONTABLE_PRECISOKEY,DIVISA_CUENTA_PRECISOKEY");
        query1.setParameter("fecha",fecha);
        if(evento!=null)
            query1.setParameter("evento",evento);
        List<Object[]> datas =  query1.getResultList();
        int rowCount = 1;

        for(Object[] data: datas){
            Row row = sheet.createRow(rowCount++);

            if(data[0]!=null) createCell(row,0,data[0].toString(),style); else createCell(row,0,"",style);
            if(data[1]!=null) createCell(row,1,data[1].toString(),style); else createCell(row,1,"",style);
            if(data[2]!=null) createCell(row,2,data[2].toString(),style); else createCell(row,2,"",style);
            if(data[3]!=null) createCell(row,3,data[3].toString(),style); else createCell(row,3,"",style);
            if(data[4]!=null) createCell(row,4,data[4].toString(),style); else createCell(row,4,"",style);
            if(data[5]!=null) createCell(row,5,Double.parseDouble(data[5].toString()),style1); else createCell(row,5,"",style);
        }

        sheets.put(sheetName, sheet);
    }

    public String normalizeDate(String dateStr) {
        // Lista de formatos posibles
        List<String> possibleFormats = List.of(
                "ddMMyyyy", "yyyyMMdd", "MMddyyyy", "yyMMdd", "ddMMyy", "yyyyddMM",
                "dd-MM-yyyy", "yyyy-MM-dd", "MM-dd-yyyy", "yy-MM-dd", "dd-MM-yy",
                "dd/MM/yyyy", "yyyy/MM/dd", "MM/dd/yyyy", "yy/MM/dd", "dd/MM/yy"
        );

        for (String format : possibleFormats) {
            try {
                // Intentamos parsear la fecha con cada formato
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(format);
                LocalDate date = LocalDate.parse(dateStr, inputFormatter);

                // Convertimos la fecha al formato deseado
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ignored) {
                // Si falla, intentamos con el siguiente formato
            }
        }

        throw new IllegalArgumentException("Formato de fecha no reconocido: " + dateStr);
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
            for (int i =0; i<data.length;i++) {
                if (campos.get(i)[1].toString().equalsIgnoreCase("float")){
                    try {
                        if (data[i] != null) createCell(row, columnCount++, Double.parseDouble(data[i].toString()), style1); else createCell(row, columnCount++, "", style);
                    } catch (Exception e) {
                        if (data[i] != null) createCell(row, columnCount++, data[i].toString(), style); else createCell(row, columnCount++, "", style);
                    }
                }
                else if(campos.get(i)[1].toString().equalsIgnoreCase("int")) {
                    try {
                        if (data[i] != null) createCell(row, columnCount++, Integer.parseInt(data[i].toString()), style2); else createCell(row, columnCount++, "", style);
                    } catch (Exception e) {
                        if (data[i] != null) createCell(row, columnCount++, data[i].toString(), style); else createCell(row, columnCount++, "", style);
                    }
                }
                else {
                    if (data[i] != null) createCell(row, columnCount++, data[i].toString(), style); else createCell(row, columnCount++, "", style);
                }
            }
        }

        sheets.put(sheetName, sheet);
    }

}
