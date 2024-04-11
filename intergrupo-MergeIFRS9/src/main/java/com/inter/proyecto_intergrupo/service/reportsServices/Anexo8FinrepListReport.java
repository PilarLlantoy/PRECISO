package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.ifrs9.Anexo;
import com.inter.proyecto_intergrupo.model.reports.Anexo8Finrep;
import com.inter.proyecto_intergrupo.model.reports.Rp21;
import com.inter.proyecto_intergrupo.model.reports.Rp21_Extend;
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
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Anexo8FinrepListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<Anexo8Finrep> anexo8FinrepList;

    public Anexo8FinrepListReport(List<Anexo8Finrep> anexo8FinrepList) {
        this.anexo8FinrepList = anexo8FinrepList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Anexo 8 FINREP");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int columnCount = 0;
        createCell(row, columnCount++, "Cuenta", style);
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
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for (Anexo8Finrep anexo8Finrep : anexo8FinrepList)
        {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, anexo8Finrep.getCuenta(), style);
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

    public void loadPath(String periodo, List<Object[]> dataList) throws IOException {
        String[] fechaFinal = periodo.split("-");
        String filePath = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\infocontable\\FINREP-COREP\\Anexo_8_" + fechaFinal[1] + fechaFinal[0].substring(2, 4) + ".csv";
        //String filePath = "C:\\Users\\CE66916\\Documents\\BBVA Intergrupo\\Anexo_8_" + fechaFinal[1] + fechaFinal[0].substring(2, 4) + ".csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("CENTRO|DescripcionCentro|CUENTA|DescripcionCuentaPuc|DIVISA|IMPORTE|FECHA_ORIGEN|FECHA_CIERRE|TP|IDENTIFICACION|DV|NOMBRE|CONTRATO|OBSERVACION|CUENTA_PROV|IMPORTE_PROV|IMPORTE_ORIGINAL|PROBABILIDAD_RECUPERACION|ALTURA");
            writer.newLine();
            for (Object[] data : dataList) {
                int conteo = 0;
                writeFixedWidthLine(writer, 4 , data[conteo++],"|","Texto");
                writeFixedWidthLine(writer, 30, data[conteo++],"|","Texto");
                writeFixedWidthLine(writer, 9 , data[conteo++],"|","Texto");
                writeFixedWidthLine(writer, 65, data[conteo++],"|","Texto");
                writeFixedWidthLine(writer, 3 , data[conteo++],"|","Texto");
                writeFixedWidthLine(writer, 1 , data[conteo++],"|","Decimal");
                writeFixedWidthLine(writer, 1, data[conteo++],"|","Plano");
                writeFixedWidthLine(writer, 1, data[conteo++],"|","Plano");
                writeFixedWidthLine(writer, 1 , data[conteo++],"|","Plano");
                writeFixedWidthLine(writer, 1 , data[conteo++],"|","Plano");
                writeFixedWidthLine(writer, 1 , data[conteo++],"|","Plano");
                writeFixedWidthLine(writer, 1, data[conteo++],"|","Plano");
                writeFixedWidthLine(writer, 1 , data[conteo++],"|","Plano");
                writeFixedWidthLine(writer, 1 , data[conteo++],"|","Plano");
                writeFixedWidthLine(writer, 1 , data[conteo++],"|","Plano");
                writeFixedWidthLine(writer, 1 , data[conteo++],"|","Plano");
                writeFixedWidthLine(writer, 1, data[conteo++],"|","Decimal");
                writeFixedWidthLine(writer, 1 , data[conteo++],"|","Plano");
                writeFixedWidthLine(writer, 1 , data[conteo++],"","Plano");
                writer.newLine();
            }
        }
    }

    public void writeFixedWidthLine(BufferedWriter writer, int fieldWidth,Object item, String separador,String format1) throws IOException {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat df = new DecimalFormat("#.00");

        String formattedField = String.format("%1$-" + fieldWidth + "s", "") + separador ;
        if(item!=null) {
            if(format1.equals("Plano"))
                formattedField = item.toString() + separador;
            else if(format1.equals("Texto"))
                formattedField = String.format("%1$-" + fieldWidth + "s", item) + separador;
            else if(format1.equals("Decimal") && !item.toString().trim().equals("") && !item.toString().trim().toUpperCase().equals("NO APLICA"))
                formattedField = String.format("%1$-" + fieldWidth + "s", df.format(Double.parseDouble(item.toString().replace(",","."))).replace(".",",")).replace(" ","") + separador;
        }
        writer.write(formattedField);
    }

    /*public void downloadAnexoCsv(PrintWriter writer, List<Anexo> anexos, String separador) throws ParseException {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat df = new DecimalFormat("#.00");

        for (Anexo8Finrep anexo8Finrep : anexo8FinrepList) {
            Date fechaDateO = formato.parse(anexo.getForigen());
            Date fechaDateC = formato.parse(anexo.getFcierr());
            String number = df.format(anexo.getSaldo());
            writer.println(
                    String.format("%1$-" + 4 + "s", anexo.getEmpresa()) + separador +
                    String.format("%1$-" + 3 + "s", anexo.getAplicativo()) + separador +
                    String.format("%1$-" + 10 + "s", anexo.getPerido()) + separador +
                    String.format("%1$-" + 9 + "s", anexo.getCuenta())+ separador +
                    String.format("%1$-" + 3 + "s", anexo.getDivisa())+ separador +
                    String.format("%1$-" + 4 + "s", anexo.getCentro())+ separador +
                    String.format("%1$-" + 18 + "s", anexo.getContrato()) + separador +
                    String.format("%1$-" + 1 + "s", anexo.getTipo())+separador +
                    String.format("%1$-" + 9 + "s", anexo.getIdent()) + separador +
                    String.format("%1$-" + 1 + "s", anexo.getDigitover()) + separador +
                    String.format("%1$-" + 27 + "s", anexo.getNombre().toUpperCase()).substring(0,27)+ separador +
                    String.format("%1$" + 18 + "s", number.replace(",",".")).replace(" ","0")+ separador +
                    String.format("%1$-" + 10 + "s", formateador.format(fechaDateO))+separador +
                    String.format("%1$-" + 10 + "s", formateador.format(fechaDateC)));
        }
    }*/
}
