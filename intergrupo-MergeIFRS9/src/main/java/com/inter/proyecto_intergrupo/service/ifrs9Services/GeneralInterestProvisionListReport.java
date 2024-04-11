package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.GenericsParametric;
import com.inter.proyecto_intergrupo.model.ifrs9.GeneralInterestProvision;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GeneralInterestProvisionListReport {
    private SXSSFWorkbook workbook;
    private SXSSFSheet sheet;
    ArrayList<GeneralInterestProvision> dataList;
    List<String[]> provLog;
    private static final short size1 = 11;
    private static final short size2 = 10;


    public GeneralInterestProvisionListReport(ArrayList<GeneralInterestProvision> dataList, List<String[]> provLog){
        this.workbook = new SXSSFWorkbook();
        this.dataList = dataList;
        this.provLog = provLog;
    }

    public GeneralInterestProvisionListReport() {
        this.workbook = new SXSSFWorkbook();
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
        Cell cell = row.createCell(columCount);

        if(value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if(value instanceof Integer){
            cell.setCellValue((Integer) value);
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        }else if(value instanceof Long){
            cell.setCellValue((Long) value);
        }else if(value instanceof Double){
            cell.setCellValue((Double) value);
        }else if(value instanceof String){
            cell.setCellValue((String) value);
        }

        cell.setCellStyle(style);
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Provisión General de Interés");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size1);
        style.setFont(font);

        createCell(row, 0, "TP", style);
        createCell(row, 1, "NIT", style);
        createCell(row, 2, "TP_CAR", style);
        createCell(row, 3, "SUC_OBL_AL", style);
        createCell(row, 4, "NUMERO_OP", style);
        createCell(row, 5, "SUCURSAL", style);
        createCell(row, 6, "LIN_SUBPRO", style);
        createCell(row, 7, "CARTERA", style);
        createCell(row, 8, "PROD_ALT", style);
        createCell(row, 9, "ALIVIOS", style);
        createCell(row, 10, "ALIVIO_COVID", style);
        createCell(row, 11, "CAPITAL", style);
        createCell(row, 12, "INTERESES", style);
        createCell(row, 13, "VR_INT_CTE", style);
        createCell(row, 14, "VR_MORA", style);
        createCell(row, 15, "CXC", style);
        createCell(row, 16, "PROVIS_CAP", style);
        createCell(row, 17, "PROVIS_INT", style);
        createCell(row, 18, "CALIFICA_7_INI", style);
        createCell(row, 19, "CALIFICA_INI", style);
        createCell(row, 20, "FEC_VTO_I", style);
        createCell(row, 21, "FECHA_INI_MORA", style);
        createCell(row, 22, "LINEA_SFC", style);
        createCell(row, 23, "NUEVA_CALIFICA", style);
        createCell(row, 24, "NUEVA_CALIFICA_7", style);
        createCell(row, 25, "CALIFICA", style);
        createCell(row, 26, "CALIFICA_7", style);
        createCell(row, 27, "ACEPTADO", style);
        createCell(row, 28, "APLICADO", style);
        createCell(row, 29, "ESTADO", style);
        createCell(row, 30, "FECHA_VCTO_ALIVIO", style);
        createCell(row, 31, "FECHA_VCTO_PAD", style);
        createCell(row, 32, "PERIODO_GRACIA_INT", style);
        createCell(row, 33, "TIPO", style);
        createCell(row, 34, "BOLSILLOS", style);
        createCell(row, 35, "FILTRO", style);
        createCell(row, 36, "INTERES", style);
        createCell(row, 37, "PROV_INTERES_INI", style);
        createCell(row, 38, "PROV_GRAL_INT_FM", style);
        createCell(row, 39, "CALIFICA_7_PDA", style);
        createCell(row, 40, "MARCA", style);
        createCell(row, 41, "CALIFICA_PDA", style);
        createCell(row, 42, "PROV_GRAL_INT_100", style);
        createCell(row, 43, "MARCA_MRCO", style);
        createCell(row, 44, "CHECK_INT", style);
        createCell(row, 45, "SALDO_INTERESES_BOLSILLO", style);
        createCell(row, 46, "INTERESES_BOLSILLOS", style);
        createCell(row, 47, "PROV_GRAL_INT", style);
        createCell(row, 48, "CHECK_BOLSILLO", style);
        createCell(row, 49, "CHECK_PROV_INT", style);
        createCell(row, 50, "TP_FIDEI", style);
        createCell(row, 51, "NIT_FIDEI", style);
        createCell(row, 52, "TP_DEF", style);
        createCell(row, 53, "NIT_DEF", style);
        createCell(row, 54, "CIUU", style);
        createCell(row, 55, "NOMBRE_2", style);
        createCell(row, 56, "CORAZU", style);
        createCell(row, 57, "SECTOR", style);
        createCell(row, 58, "CODIGO_IFRS9", style);
        createCell(row, 59, "NIT_SIN_DV", style);
        createCell(row, 60, "DV", style);
        createCell(row, 61, "BANCA", style);
        createCell(row, 62, "NIVEL_VENTAS2", style);
        createCell(row, 63, "EMPRESA", style);
        createCell(row, 64, "VALOR", style);
        createCell(row, 65, "MES ANTERIOR", style);
        createCell(row, 66, "DIFERENCIA ENTRE MESES", style);
        createCell(row, 67, "CUENTA BALANCE", style);
        createCell(row, 68, "CUENTA PYG", style);
        createCell(row, 69, "CONTRATO", style);
        createCell(row, 70, "FUENTE DE INFO", style);
    }

    private void writeDataLinesReport(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontHeightInPoints(size2);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(GeneralInterestProvision gen: dataList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,gen.getTp(),style);
            createCell(row,columnCount++,gen.getNit(),style);
            createCell(row,columnCount++,gen.getTpCar(),style);
            createCell(row,columnCount++,gen.getSucOblAl(),style);
            createCell(row,columnCount++,gen.getNumeroOp(),style);
            createCell(row,columnCount++,gen.getSucursal(),style);
            createCell(row,columnCount++,gen.getLinSubpro(),style);
            createCell(row,columnCount++,gen.getCartera(),style);
            createCell(row,columnCount++,gen.getProdAlta(),style);
            createCell(row,columnCount++,gen.getAlivios(),style);
            createCell(row,columnCount++,gen.getAliviosCovid(),style);
            createCell(row,columnCount++,gen.getCapital(),style1);
            createCell(row,columnCount++,gen.getIntereses(),style1);
            createCell(row,columnCount++,gen.getVrIntCte(),style1);
            createCell(row,columnCount++,gen.getVrMora(),style1);
            createCell(row,columnCount++,gen.getCxc(),style1);
            createCell(row,columnCount++,gen.getProvisCap(),style1);
            createCell(row,columnCount++,gen.getProvisInt(),style1);
            createCell(row,columnCount++,gen.getCalifica7Ini(),style);
            createCell(row,columnCount++,gen.getCalificaIni(),style);
            createCell(row,columnCount++,gen.getFecVtoI(),style);
            createCell(row,columnCount++,gen.getFechaIniMora(),style);
            createCell(row,columnCount++,gen.getLineasSfc(),style);
            createCell(row,columnCount++,gen.getNuevaCalifica(),style);
            createCell(row,columnCount++,gen.getNuevaCalifica7(),style);
            createCell(row,columnCount++,gen.getCalifica(),style);
            createCell(row,columnCount++,gen.getCalifica7(),style);
            createCell(row,columnCount++,gen.getAceptado(),style);
            createCell(row,columnCount++,gen.getAplicado(),style);
            createCell(row,columnCount++,gen.getEstado(),style);
            createCell(row,columnCount++,gen.getFechaVctoAlivio(),style);
            createCell(row,columnCount++,gen.getFechaVctoPad(),style);
            createCell(row,columnCount++,gen.getPeriodoGraciaInt(),style);
            createCell(row,columnCount++,gen.getTipo(),style);
            createCell(row,columnCount++,gen.getBolsillos(),style);
            createCell(row,columnCount++,gen.getFiltro(),style);
            createCell(row,columnCount++,gen.getInteres(),style1);
            createCell(row,columnCount++,gen.getProvInteresIni(),style1);
            createCell(row,columnCount++,gen.getProvGralIntFm(),style1);
            createCell(row,columnCount++,gen.getCalifica7Pda(),style);
            createCell(row,columnCount++,gen.getMarca(),style);
            createCell(row,columnCount++,gen.getCalificaPda(),style);
            createCell(row,columnCount++,gen.getProvGralInt100(),style1);
            createCell(row,columnCount++,gen.getMarcaMrco(),style);
            createCell(row,columnCount++,gen.getCheckInt(),style);
            createCell(row,columnCount++,gen.getSaldoInteresesBolsillo(),style1);
            createCell(row,columnCount++,gen.getInteresesBolsillo(),style1);
            createCell(row,columnCount++,gen.getProvGralInt(),style1);
            createCell(row,columnCount++,gen.getCheckBolsillo(),style);
            createCell(row,columnCount++,gen.getCheckProvInt(),style);
            createCell(row,columnCount++,gen.getTpFidei(),style);
            createCell(row,columnCount++,gen.getNitFidei(),style);
            createCell(row,columnCount++,gen.getTpDef(),style);
            createCell(row,columnCount++,gen.getNitDef(),style);
            createCell(row,columnCount++,gen.getCiiu(),style);
            createCell(row,columnCount++,gen.getNombre2(),style);
            createCell(row,columnCount++,gen.getCorazu(),style);
            createCell(row,columnCount++,gen.getSector(),style);
            createCell(row,columnCount++,gen.getCodigoIfrs9(),style);
            createCell(row,columnCount++,gen.getNitSinDv(),style);
            createCell(row,columnCount++,gen.getDv(),style);
            createCell(row,columnCount++,gen.getBanca(),style);
            createCell(row,columnCount++,gen.getNivelVentas2(),style);
            createCell(row,columnCount++,gen.getEmpresa(),style);
            createCell(row,columnCount++,gen.getValor(),style1);
            createCell(row,columnCount++,gen.getMesAnterior(),style1);
            createCell(row,columnCount++,gen.getDiferenciaMeses(),style1);
            createCell(row,columnCount++,gen.getCuentaBalance(),style);
            createCell(row,columnCount++,gen.getCuentaPyG(),style);
            createCell(row,columnCount++,gen.getContrato(),style);
            createCell(row,columnCount++,gen.getFunteInfo(),style);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLinesReport();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }


    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Provisiones");
        Row row1 = sheet.createRow(0);
        sheet.setRandomAccessWindowSize(1000);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size1);
        style.setFont(font);

        createCell(row1, 0, "Calificación", style);
        createCell(row1, 1, "Cartera", style);
        createCell(row1, 2, "Error", style);

        int rowCount = 1;

        CellStyle style2 = workbook.createCellStyle();
        Font font2 = workbook.createFont();
        font2.setBold(false);
        font2.setFontHeightInPoints(size2);
        style.setFont(font2);
            provLog.remove(provLog.size()-1);
        for(String[] log: provLog){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style2);
            createCell(row,columnCount++,log[1],style2);
            createCell(row,columnCount++,log[2],style2);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLogParam(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Provisiones");
        Row row1 = sheet.createRow(0);
        sheet.setRandomAccessWindowSize(1000);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size2);
        style.setFont(font);

        /*createCell(row1, 0, "TP", style);
        createCell(row1, 1, "Cartera", style);*/
        createCell(row1, 0, "Fila", style);
        createCell(row1, 1, "Mensaje", style);

        int rowCount = 1;

        CellStyle style2 = workbook.createCellStyle();
        Font font2 = workbook.createFont();
        font2.setBold(false);
        font2.setFontHeightInPoints(size2);
        style.setFont(font2);

        for(String[] log: provLog){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0],style2);
            //createCell(row,columnCount++,log[1],style2);
            createCell(row,columnCount++,log[2],style2);
            //createCell(row,columnCount++,log[3],style2);
            //createCell(row,columnCount++,log[4],style2);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    private void writeHeaderLineMassive() {
        sheet = workbook.createSheet("Carga Masiva");
        sheet.setRandomAccessWindowSize(1000);
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(size1);
        style.setFont(font);

        createCell(row, 0, "CENTRO DE COSTO", style);
        createCell(row, 1, "CUENTA", style);
        createCell(row, 2, "DIVISA", style);
        createCell(row, 3, "CONTRATO", style);
        createCell(row, 4, "REFERENCIA CRUCE", style);
        createCell(row, 5, "IMPORTE", style);
        createCell(row, 6, "DESCRIPCIÓN", style);
        createCell(row, 7, "FECHA", style);
        createCell(row, 8, "TIPO DE DOCUMENTO", style);
        createCell(row, 9, "NÚMERO DE DOCUMENTO", style);
        createCell(row, 10, "DÍGITO DE VERIFICACIÓN", style);
        createCell(row, 11, "TIPO DE PERDIDA", style);
        createCell(row, 12, "CLASE DE RIESGO", style);
        createCell(row, 13, "TIPO DE MOVIMIENTO", style);
        createCell(row, 14, "PRODUCTO", style);
        createCell(row, 15, "PROCESO", style);
        createCell(row, 16, "LÍNEA OPERATIVA", style);
        createCell(row, 17, "VALOR BASE", style);
    }

    private void writeDataLinesReportMassive(List<Object[]> data) {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontHeightInPoints(size2);
        style.setFont(font);

        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(Object[] log: data){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,log[0].toString(),style);
            createCell(row,columnCount++,log[1].toString(),style);
            createCell(row,columnCount++,log[2].toString(),style);
            createCell(row,columnCount++,log[3].toString(),style);
            createCell(row,columnCount++,log[4].toString(),style);
            createCell(row,columnCount++,Double.parseDouble(log[5].toString().replace(",","")),style1);
            createCell(row,columnCount++,log[6].toString(),style);
            createCell(row,columnCount++,log[7].toString(),style);
            createCell(row,columnCount++,log[8].toString(),style);
            createCell(row,columnCount++,log[9].toString(),style);
            createCell(row,columnCount++,log[10].toString(),style);
            createCell(row,columnCount++,log[11].toString(),style);
            createCell(row,columnCount++,log[12].toString(),style);
            createCell(row,columnCount++,log[13].toString(),style);
            createCell(row,columnCount++,log[14].toString(),style);
            createCell(row,columnCount++,log[15].toString(),style);
            createCell(row,columnCount++,log[16].toString(),style);
            createCell(row,columnCount++,Double.parseDouble("0.00"),style1);
        }

    }

    public void exportMassive(List<Object[]> data, HttpServletResponse response) throws IOException {
        writeHeaderLineMassive();
        writeDataLinesReportMassive(data);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

}
