package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.PlainIFRS9;
import com.inter.proyecto_intergrupo.model.ifrs9.ValIFRS9;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PlainIFRS9ListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;
    private XSSFSheet sheet2;
    private List<PlainIFRS9> repoList;
    private List<Object[]> repoListResumen;

    public PlainIFRS9ListReport(List<PlainIFRS9> cuadreList, List<Object[]> repoListResumen){
        this.repoList = cuadreList;
        this.repoListResumen = repoListResumen;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(boolean pyg,List<String> typeList,List<Object[]> typeListFuente){
        sheet = workbook.createSheet("Plano IFRS9");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,	0, "Tipo Registro", style);
        createCell(row, 1, "Sociedad", style);
        createCell(row,	2, "Tipo Consolidación", style);
        createCell(row,	3, "Tipo Asiento", style);
        createCell(row,	4, "Descripción", style);
        createCell(row,	5, "Movimiento", style);
        createCell(row,	6, "Referencia", style);
        createCell(row,	7, "Modo Ejecución", style);
        createCell(row,	8, "Usuario", style);
        createCell(row,	9, "Código Consolidación", style);
        createCell(row,	10, "Naturaleza", style);
        createCell(row,	11, "Saldo", style);
        createCell(row,	12, "Divisa", style);
        createCell(row,	13, "País", style);
        if(pyg == false) {
            createCell(row, 14, "Intergrupo", style);
            createCell(row, 15, "Periodo", style);
        }
        else{
            createCell(row, 14, "Valor Paramétrica", style);
            createCell(row, 15, "Valor Fuente", style);

            if(typeListFuente!=null &&!typeListFuente.isEmpty()) {
                sheet2 = workbook.createSheet("Resumen");
                Row row1 = sheet2.createRow(0);
                createCell(row1, 0, "Código Consolidación", style);
                createCell(row1, 1, "Divisa", style);
                createCell(row1, 2, "Instrumento", style);
                createCell(row1, 3, "Stage", style);
                createCell(row1, 4, "Saldo", style);
            }
        }

        if(typeList!=null &&!typeList.isEmpty())
        {
            sheet1 = workbook.createSheet("Pendientes Parametrizar");
            Row row1 = sheet1.createRow(0);
            createCell(row1, 0, "Tipo Entidad Pendientes", style);
        }
    }

    private void writeHeaderLineResumen(){
        sheet = workbook.createSheet("Resumen IFRS9");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,	0, "Descripción", style);
        createCell(row,	1, "Dígito", style);
        createCell(row,	2, "Divisa", style);
        createCell(row,	3, "Saldo D", style);
        createCell(row,	4, "Saldo H", style);
        createCell(row,	5, "Diferencia", style);

    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
        //sheet.autoSizeColumn(columCount);
        Cell cell = row.createCell(columCount);

        if(value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if(value instanceof Integer){
            cell.setCellValue((Integer) value);
        } else if(value instanceof Double){
            cell.setCellValue((Double) value);
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        } else if(value instanceof Long){
            cell.setCellValue((Long) value);
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

        for(PlainIFRS9 plain: repoList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            createCell(row,columnCount++,	plain.getTipoRegistro()	,style);
            createCell(row,columnCount++,	plain.getSociedad(),style);
            createCell(row,columnCount++,	plain.getTipocons(),style);
            createCell(row,columnCount++,	plain.getTipoAsiento(),style);
            createCell(row,columnCount++,	plain.getDescripcion(),style);
            createCell(row,columnCount++,	plain.getMovimiento(),style);
            createCell(row,columnCount++,	plain.getReferencia(),style);
            createCell(row,columnCount++,	plain.getModoEjecucion(),style);
            createCell(row,columnCount++,	plain.getUsuario(),style);
            createCell(row,columnCount++,	plain.getCodicons(),style);
            createCell(row,columnCount++,	plain.getNaturalezaTotal(),style);
            if(plain.getNaturalezaTotal().equals("H"))
                createCell(row,columnCount++,	Math.abs(Double.parseDouble((plain.getSaldo()).toString()))*-1,style1);
            else if(plain.getNaturalezaTotal().equals("D"))
                createCell(row,columnCount++,	Math.abs(Double.parseDouble((plain.getSaldo()).toString())),style1);
            createCell(row,columnCount++,	plain.getDivisa(),style);
            createCell(row,columnCount++,	plain.getPaisNegocio()	,style);
            if(plain.getIntergrupo()==null)
                createCell(row,columnCount++,"",style);
            else
                createCell(row,columnCount++,	plain.getIntergrupo()	,style);
            createCell(row,columnCount++,	plain.getPeriodo()	,style);

        }
    }

    private void writeDataLinesPyg(boolean pyg, List<String> typeList,List<Object[]> typeListFuente){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);

        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] plain: repoListResumen){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            createCell(row,columnCount++,	plain[0],style);
            createCell(row,columnCount++,	plain[1],style);
            createCell(row,columnCount++,	plain[2],style);
            createCell(row,columnCount++,	plain[3],style);
            createCell(row,columnCount++,	plain[4],style);
            createCell(row,columnCount++,	plain[5],style);
            createCell(row,columnCount++,	plain[6],style);
            createCell(row,columnCount++,	plain[7],style);
            createCell(row,columnCount++,	plain[8],style);
            createCell(row,columnCount++,	plain[9],style);
            createCell(row,columnCount++,	plain[10],style);
            if(plain[11]!=null && plain[11].toString()!=null)
                createCell(row,columnCount++,	Double.parseDouble(plain[11].toString()),style1);
            else
                createCell(row,columnCount++,	0,style1);
            createCell(row,columnCount++,	plain[12],style);
            createCell(row,columnCount++,	plain[13],style);
            if(plain[15]!=null && plain[15].toString()!=null)
                createCell(row,columnCount++,	Double.parseDouble(plain[15].toString()),style1);
            else
                createCell(row,columnCount++,	0,style1);
            if(plain[16]!=null && plain[16].toString()!=null)
                createCell(row,columnCount++,	Double.parseDouble(plain[16].toString()),style1);
            else
                createCell(row,columnCount++,	0,style1);
        }

        if(typeList!=null && !typeList.isEmpty())
        {
            rowCount = 1;
            for(String plain: typeList){
                Row row = sheet1.createRow(rowCount++);
                int columnCount = 0;
                createCell(row,columnCount++,	plain	,style);
            }
        }

        if(pyg == true && typeListFuente!=null && !typeListFuente.isEmpty())
        {
            rowCount = 1;
            for(Object[] plain: typeListFuente){
                Row row = sheet2.createRow(rowCount++);
                int columnCount = 0;
                createCell(row,columnCount++,	plain[0]	,style);
                createCell(row,columnCount++,	plain[1]	,style);
                createCell(row,columnCount++,	plain[2]	,style);
                createCell(row,columnCount++,	plain[3]	,style);
                createCell(row,columnCount++,	Double.parseDouble(plain[4].toString())	,style1);
            }
        }
    }

    private void writeDataLinesResumen(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);

        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] plain: repoListResumen){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            if(plain[0]!=null && plain[0].toString()!=null)
                createCell(row,columnCount++,	plain[0].toString()	,style);
            else
                createCell(row,columnCount++,"",style);
            if(plain[1]!=null && plain[1].toString()!=null)
                createCell(row,columnCount++,	plain[1].toString()	,style);
            else
                createCell(row,columnCount++,"",style);
            if(plain[2]!=null && plain[2].toString()!=null)
                createCell(row,columnCount++,	plain[2].toString()	,style);
            else
                createCell(row,columnCount++,"",style);
            if(plain[3]!=null && plain[3].toString()!=null)
                createCell(row,columnCount++,	Double.parseDouble(plain[3].toString())	,style1);
            else
                createCell(row,columnCount++,"",style);
            if(plain[4]!=null && plain[4].toString()!=null)
                createCell(row,columnCount++,	Double.parseDouble(plain[4].toString())	,style1);
            else
                createCell(row,columnCount++,"",style);
            if(plain[5]!=null && plain[5].toString()!=null)
                createCell(row,columnCount++,	Double.parseDouble(plain[5].toString())	,style1);
            else
                createCell(row,columnCount++,"",style);

        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine(false,null,null);
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportPyg(HttpServletResponse response,List<String> typeList,List<Object[]> intListFuente) throws IOException {
        writeHeaderLine(true,typeList,intListFuente);
        writeDataLinesPyg(true,typeList,intListFuente);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportList(HttpServletResponse response,List<String> typeList) throws IOException {
        sheet = workbook.createSheet("Listado Fallido");
        Row row1 = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row1,	0, "Codicons", style);

        int rowCount = 1;

        font.setBold(false);
        style.setFont(font);

        for(String plain: typeList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, plain, style);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportResumen(HttpServletResponse response) throws IOException {
        writeHeaderLineResumen();
        writeDataLinesResumen();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
