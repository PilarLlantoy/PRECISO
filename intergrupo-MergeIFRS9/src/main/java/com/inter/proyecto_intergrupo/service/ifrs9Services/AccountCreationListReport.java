package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccountCreationListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;
    private XSSFSheet sheet2;
    private List<AccountCreation> accountCreationList;
    private List<Object[]> accountCreationListObject;
    private List<String> accountCreationListNOT;
    private List<String> accountCreationListIN;

    public AccountCreationListReport(List<AccountCreation> accountCreationList){
        this.accountCreationList = accountCreationList;
        workbook = new XSSFWorkbook();

    }

    public AccountCreationListReport(List<AccountCreation> accountCreationList,List<String> accountCreationListNOT,List<String> accountCreationListIN){
        this.accountCreationList = accountCreationList;
        this.accountCreationListNOT =accountCreationListNOT;
        this.accountCreationListIN =accountCreationListIN;
        workbook = new XSSFWorkbook();

    }

    public AccountCreationListReport(List<Object[]> accountCreationListObject, int number){
        this.accountCreationListObject = accountCreationListObject;
        workbook = new XSSFWorkbook();

    }

    private void writeHeaderLine(String perfil){
        sheet = workbook.createSheet("Creación de Cuentas");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row,0, "EMPRESA", style);
        createCell(row,	1, "NÚMERO CUENTA", style);
        createCell(row,	2, "NOMBRE CORTO CUENTA", style);
        createCell(row,	3, "TIPOCTA", style);
        createCell(row,	4, "INDIC L/I", style);
        createCell(row,	5, "MON", style);
        if(perfil.equals("GESTION")) {
            createCell(row, 6, "CÓDIGO GESTION", style);
            createCell(row, 7, "EPIGRAFE", style);
        }
        else if(perfil.equals("CONSOLIDACION")) {
            createCell(row, 6, "CONSOLID", style);
        }
        else if(perfil.equals("CONTROL CONTABLE")) {
            createCell(row, 6, "CÓDIGO DE CONTROL", style);
            createCell(row, 7, "DIAS DE PLAZO", style);
            createCell(row, 8, "INDICADOR DE LA CUENTA", style);
            createCell(row, 9, "TIPO DE APUNTE", style);
            createCell(row, 10, "INVENTARIABLE", style);
        }
    }

    private void writeHeaderLineAll(){
        sheet = workbook.createSheet("Creación de Cuentas");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);
        int columnCount = 0;

        createCell(row,columnCount++, "EMPRESA", style);
        createCell(row,columnCount++, "NÚMERO CUENTA", style);
        createCell(row,columnCount++, "CUENTA 4", style);
        createCell(row,columnCount++, "SUBCUENTA 2", style);
        createCell(row,columnCount++, "SUB", style);
        createCell(row,columnCount++, "SEG", style);
        createCell(row,columnCount++, "STAG", style);
        createCell(row,columnCount++, "NOMBRE CUENTA", style);
        createCell(row,columnCount++, "NOMBRE CORTO CUENTA", style);
        createCell(row,columnCount++, "TIPOCTA", style);
        createCell(row,columnCount++, "INDIC L/I", style);
        createCell(row,columnCount++, "CLAVE ACCESO", style);
        createCell(row,columnCount++, "MON", style);
        createCell(row,columnCount++, "TICTOO1", style);
        createCell(row,columnCount++, "TICTOO2", style);
        createCell(row,columnCount++, "TICTOO3", style);
        createCell(row,columnCount++, "TICTOO4", style);
        createCell(row,columnCount++, "TICTOO5", style);
        createCell(row,columnCount++, "TICENAO", style);
        createCell(row,columnCount++, "CENAUO01", style);
        createCell(row,columnCount++, "CENAUO02", style);
        createCell(row,columnCount++, "CENAUO03", style);
        createCell(row,columnCount++, "CENAUO04", style);
        createCell(row,columnCount++, "CENAUO05", style);
        createCell(row,columnCount++, "CENAUO06", style);
        createCell(row,columnCount++, "CENAUO07", style);
        createCell(row,columnCount++, "CENAUO08", style);
        createCell(row,columnCount++, "CENAUO09", style);
        createCell(row,columnCount++, "TICTOD1", style);
        createCell(row,columnCount++, "TICTOD2", style);
        createCell(row,columnCount++, "TICTOD3", style);
        createCell(row,columnCount++, "TICTOD4", style);
        createCell(row,columnCount++, "TICTOD5", style);
        createCell(row,columnCount++, "TICENAD", style);
        createCell(row,columnCount++, "CENAUD01", style);
        createCell(row,columnCount++, "CENAUD02", style);
        createCell(row,columnCount++, "CENAUD03", style);
        createCell(row,columnCount++, "CENAUD04", style);
        createCell(row,columnCount++, "CENAUD05", style);
        createCell(row,columnCount++, "CENAUD06", style);
        createCell(row,columnCount++, "CENAUD07", style);
        createCell(row,columnCount++, "CENAUD08", style);
        createCell(row,columnCount++, "CENAUD09", style);
        createCell(row,columnCount++, "TIPAPUN", style);
        createCell(row,columnCount++, "SIGINIC", style);
        createCell(row,columnCount++, "INDICADOR PROCESO DE BAJA", style);
        createCell(row,columnCount++, "IND CUENTA INVENTARIABLE", style);
        createCell(row,columnCount++, "IND CUENTA OPERACIONAL", style);
        createCell(row,columnCount++, "COD-CDCONMEX", style);
        createCell(row,columnCount++, "COD-REPREGUL", style);
        createCell(row,columnCount++, "INTERFAZ", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL OPERATIVO NIVEL1", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES1", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES2", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES3", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES4", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES5", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES6", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL DE GESTION NIVEL2", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES1", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES2", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES3", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES4", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES5", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES6", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO NIVEL3", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES1", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES2", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES3", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES4", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES5", style);
        createCell(row,columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES6", style);
        createCell(row,columnCount++, "CONTRAPARTIDA DE ORDEN", style);
        createCell(row,columnCount++, "CONTRAPARTIDA DE RESULTADOS D/H", style);
        createCell(row,columnCount++, "CÓDIGO GESTION", style);
        createCell(row,columnCount++, "EPIGRAFE", style);
        createCell(row,columnCount++, "CONSOLID", style);
        createCell(row,columnCount++, "CÓDIGO DE CONTROL", style);
        createCell(row,columnCount++, "DIAS DE PLAZO", style);
        createCell(row,columnCount++, "INDICADOR DE LA CUENTA", style);
        createCell(row,columnCount++, "TIPO DE APUNTE", style);
        createCell(row,columnCount++, "INVENTARIABLE", style);
    }

    private void writeExtra()
    {
        sheet1 = workbook.createSheet("Cuentas Encontradas");
        sheet2 = workbook.createSheet("Cuentas No Encontradas");
        Row row1 = sheet1.createRow(0);
        Row row2 = sheet2.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row1,0, "Cuentas Encontradas", style);
        createCell(row2,0, "Cuentas No Encontradas", style);

        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font1 = workbook.createFont();
        font1.setBold(false);
        font1.setFontHeight(10);
        style1.setFont(font1);

        int rowCount = 1;

        for(String cuenta: accountCreationListIN) {
            Row row = sheet1.createRow(rowCount++);
            createCell(row,0, cuenta, style1);
        }

        rowCount = 1;

        for(String cuenta: accountCreationListNOT) {
            Row row = sheet2.createRow(rowCount++);
            createCell(row,0, cuenta, style1);
        }

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

    private void writeDataLinesAll(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(AccountCreation crear: accountCreationList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            createCell(row,columnCount++, crear.getEMPRESA() ,style);
            createCell(row,columnCount++, crear.getNUMEROCUENTA() ,style);
            createCell(row,columnCount++, crear.getCUENTA4() ,style);
            createCell(row,columnCount++, crear.getSUBCUENTA2() ,style);
            createCell(row,columnCount++, crear.getSUB() ,style);
            createCell(row,columnCount++, crear.getSEG() ,style);
            createCell(row,columnCount++, crear.getSTAG() ,style);
            createCell(row,columnCount++, crear.getNOMBRECUENTA() ,style);
            createCell(row,columnCount++, crear.getNOMBRECORTOCUENTA() ,style);
            createCell(row,columnCount++, crear.getTIPOCTA() ,style);
            createCell(row,columnCount++, crear.getINDICLI() ,style);
            createCell(row,columnCount++, crear.getCLAVEACCESO() ,style);
            createCell(row,columnCount++, crear.getMON() ,style);
            createCell(row,columnCount++, crear.getTICTOO1() ,style);
            createCell(row,columnCount++, crear.getTICTOO2() ,style);
            createCell(row,columnCount++, crear.getTICTOO3() ,style);
            createCell(row,columnCount++, crear.getTICTOO4() ,style);
            createCell(row,columnCount++, crear.getTICTOO5() ,style);
            createCell(row,columnCount++, crear.getTICENAO() ,style);
            createCell(row,columnCount++, crear.getCENAUO01() ,style);
            createCell(row,columnCount++, crear.getCENAUO02() ,style);
            createCell(row,columnCount++, crear.getCENAUO03() ,style);
            createCell(row,columnCount++, crear.getCENAUO04() ,style);
            createCell(row,columnCount++, crear.getCENAUO05() ,style);
            createCell(row,columnCount++, crear.getCENAUO06() ,style);
            createCell(row,columnCount++, crear.getCENAUO07() ,style);
            createCell(row,columnCount++, crear.getCENAUO08() ,style);
            createCell(row,columnCount++, crear.getCENAUO09() ,style);
            createCell(row,columnCount++, crear.getTICTOD1() ,style);
            createCell(row,columnCount++, crear.getTICTOD2() ,style);
            createCell(row,columnCount++, crear.getTICTOD3() ,style);
            createCell(row,columnCount++, crear.getTICTOD4() ,style);
            createCell(row,columnCount++, crear.getTICTOD5() ,style);
            createCell(row,columnCount++, crear.getTICENAD() ,style);
            createCell(row,columnCount++, crear.getCENAUD01() ,style);
            createCell(row,columnCount++, crear.getCENAUD02() ,style);
            createCell(row,columnCount++, crear.getCENAUD03() ,style);
            createCell(row,columnCount++, crear.getCENAUD04() ,style);
            createCell(row,columnCount++, crear.getCENAUD05() ,style);
            createCell(row,columnCount++, crear.getCENAUD06() ,style);
            createCell(row,columnCount++, crear.getCENAUD07() ,style);
            createCell(row,columnCount++, crear.getCENAUD08() ,style);
            createCell(row,columnCount++, crear.getCENAUD09() ,style);
            createCell(row,columnCount++, crear.getTIPAPUN() ,style);
            createCell(row,columnCount++, crear.getSIGINIC() ,style);
            createCell(row,columnCount++, crear.getINDICADORPROCESODEBAJA() ,style);
            createCell(row,columnCount++, crear.getINDCUENTAINVENTARIABLE() ,style);
            createCell(row,columnCount++, crear.getINDCUENTAOPERACIONAL() ,style);
            createCell(row,columnCount++, crear.getCODCDCONMEX() ,style);
            createCell(row,columnCount++, crear.getCODREPREGUL() ,style);
            createCell(row,columnCount++, crear.getINTERFAZ() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLOPERATIVONIVEL1() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLOPERATIVOCENOPERES1() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLOPERATIVOCENOPERES2() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLOPERATIVOCENOPERES3() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLOPERATIVOCENOPERES4() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLOPERATIVOCENOPERES5() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLOPERATIVOCENOPERES6() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLDEGESTIONNIVEL2() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLDEGESTIONCENGESRES1() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLDEGESTIONCENGESRES2() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLDEGESTIONCENGESRES3() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLDEGESTIONCENGESRES4() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLDEGESTIONCENGESRES5() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLDEGESTIONCENGESRES6() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLADMINISTRATIVONIVEL3() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES1() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES2() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES3() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES4() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES5() ,style);
            createCell(row,columnCount++, crear.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES6() ,style);
            createCell(row,columnCount++, crear.getCONTRAPARTIDADEORDEN() ,style);
            createCell(row,columnCount++, crear.getCONTRAPARTIDADERESULTADOSDH() ,style);
            createCell(row,columnCount++, crear.getCODIGOGESTION() ,style);
            createCell(row,columnCount++, crear.getEPIGRAFE() ,style);
            createCell(row,columnCount++, crear.getCONSOLID() ,style);
            createCell(row,columnCount++, crear.getCODIGODECONTROL() ,style);
            createCell(row,columnCount++, crear.getDIASDEPLAZO() ,style);
            createCell(row,columnCount++, crear.getINDICADORDELACUENTA() ,style);
            createCell(row,columnCount++, crear.getTIPODEAPUNTE() ,style);
            createCell(row,columnCount++, crear.getINVENTARIABLE() ,style);

        }
    }

    private void writeDataLines(String perfil){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        for(Object[] crear: accountCreationListObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

            createCell(row,columnCount++,	crear[0]	,style);
            createCell(row,columnCount++,	crear[1]	,style);
            createCell(row,columnCount++,	crear[2]	,style);
            createCell(row,columnCount++,	crear[3]	,style);
            createCell(row,columnCount++,	crear[4]	,style);
            createCell(row,columnCount++,	crear[5]	,style);
            if(perfil.equals("GESTION")) {
                createCell(row,columnCount++,	crear[6]	,style);
                createCell(row,columnCount++,	crear[7]	,style);
            }
            else if(perfil.equals("CONSOLIDACION")) {
                createCell(row,columnCount++,	crear[6]	,style);
            }
            else if(perfil.equals("CONTROL CONTABLE")) {
                createCell(row,columnCount++,	crear[6]	,style);
                createCell(row,columnCount++,	crear[7]	,style);
                createCell(row,columnCount++,	crear[8]	,style);
                createCell(row,columnCount++,	crear[9]	,style);
                createCell(row,columnCount++,	crear[10]	,style);
            }
        }
    }

    public void export(HttpServletResponse response,String perfil) throws IOException {
        writeHeaderLine(perfil);
        writeDataLines(perfil);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportAll(HttpServletResponse response) throws IOException {
        writeHeaderLineAll();
        writeDataLinesAll();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportAllG(HttpServletResponse response) throws IOException {
        writeHeaderLineAll();
        writeDataLinesAll();
        writeExtra();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportLog(HttpServletResponse response, List<String[]> lista)
    {
        int position=0;
        String[] temporalListNames =lista.get(lista.size()-1);
        List<String> list=new ArrayList<>();

        try {
            exportSubLog(response, lista,position);
            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(response.getOutputStream());
            workbook.close();
            outputStream.flush();
            outputStream.close();
        }
        catch (IOException e){

        }
    }

    public void exportSubLog(HttpServletResponse response, List<String[]> lista, int position) throws IOException
    {
        sheet = workbook.createSheet("Log");
        Row row1 = sheet.createRow(1);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row1, 0, "Fila", style);
        createCell(row1, 1, "Columna", style);
        createCell(row1, 2, "Estado", style);

        Row row2 = sheet.createRow(0);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for(int i =0; i<lista.size()-1 ; i++)
        {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,lista.get(i)[0],style);
            createCell(row,columnCount++,lista.get(i)[1],style);
            createCell(row,columnCount++,lista.get(i)[3],style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

    }
}
