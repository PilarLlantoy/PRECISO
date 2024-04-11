package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.accountsReceivable.InvoicesCc;
import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreationOther;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class AccountCreationOtherListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> accountList;
    private List<AccountCreationOther> listAccount;
    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public AccountCreationOtherListReport(List<String[]> accountList, List<AccountCreationOther> listAccount) {
        this.accountList = accountList;
        this.listAccount = listAccount;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Creación de Cuentas");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeight(11);
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font1 = workbook.createFont();
        font1.setBold(true);
        font1.setFontHeight(11);
        style1.setFont(font1);

        style1.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        style1.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        int columnCount = 0;
        createCell(row, columnCount++, "EMPRESA", style);
        createCell(row, columnCount++, "NUCTA", style);
        createCell(row, columnCount++, "NOMBRE CUENTA LARGA", style);
        createCell(row, columnCount++, "NOMBRE CUENTA CORTA", style);
        createCell(row, columnCount++, "JUSTIFICACIÓN GLOBAL", style);
        createCell(row, columnCount++, "DINAMICA CUENTA", style);
        createCell(row, columnCount++, "TIPO CTA", style);
        createCell(row, columnCount++, "IND UTIZ L/I", style);
        createCell(row, columnCount++, "CLAVE ACCESO", style);
        createCell(row, columnCount++, "MON", style);
        createCell(row, columnCount++, "CENTRO ORIGEN", style);
        createCell(row, columnCount++, "CENTRO DESTINO", style);
        createCell(row, columnCount++, "CÓDIGO GESTION", style1);
        createCell(row, columnCount++, "EPIGRAFE", style1);
        createCell(row, columnCount++, "CONSOLID", style1);
        createCell(row, columnCount++, "TIPO APUNTE", style1);
        createCell(row, columnCount++, "COD CONTROL", style1);
        createCell(row, columnCount++, "IND CIERRE", style);
        createCell(row, columnCount++, "DIAS PLAZO", style);
        createCell(row, columnCount++, "IND CTA INV", style1);
        createCell(row, columnCount++, "INTERFAZ", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES1", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES2", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES3", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES4", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES5", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES6", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES1", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES2", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES3", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES4", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES5", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL DE GESTION CEN-GES-RES6", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES1", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES2", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES3", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES4", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES5", style);
        createCell(row, columnCount++, "RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES6", style);
        createCell(row, columnCount++, "CONTRAPARTIDA DE ORDEN", style);
        createCell(row, columnCount++, "TIPO CUENTA DE ORDEN", style);
        createCell(row, columnCount++, "CONTRAPARTIDA DE RESULTADOS D/H", style);
        createCell(row, columnCount++, "FECHA CARGUE", style);

        columnCount = 0;
        commentCell("Especificaciones Campo EMPRESA-> \n*Longitud Maxima: 4 \n*Longitud Minima: 4\n*Tipo dato: Texto\n*Responsable: Solicitante",row,columnCount++);
        commentCell("Especificaciones Campo NUCTA-> \n*Longitud Maxima: 15 \n*Longitud Minima: 6\n*Tipo dato: Texto\n*Responsable: Solicitante",row,columnCount++);
        commentCell("Especificaciones Campo NOMBRE CUENTA LARGA-> \n*Longitud Maxima: 65 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Solicitante",row,columnCount++);
        commentCell("Especificaciones Campo NOMBRE CUENTA CORTA-> \n*Longitud Maxima: 35 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Solicitante",row,columnCount++);
        commentCell("Especificaciones Campo JUSTIFICACIÓN GLOBAL-> \n*Longitud Maxima: 255 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Solicitante",row,columnCount++);
        commentCell("Especificaciones Campo DINAMICA CUENTA-> \n*Longitud Maxima: 255 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Solicitante",row,columnCount++);
        commentCell("Especificaciones Campo TIPO CTA-> \n*Longitud Maxima: 1 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo IND UTIZ L/I-> \n*Longitud Maxima: 1 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo CLAVE ACCESO-> \n*Longitud Maxima: 1 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo MON-> \n*Longitud Maxima: 1 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo CENTRO ORIGEN-> \n*Longitud Maxima: 4 \n*Longitud Minima: 4\n*Tipo dato: Texto\n*Responsable: Solicitante",row,columnCount++);
        commentCell("Especificaciones Campo CENTRO DESTINO-> \n*Longitud Maxima: 4 \n*Longitud Minima: 4\n*Tipo dato: Texto\n*Responsable: Solicitante",row,columnCount++);
        commentCell("Especificaciones Campo CÓDIGO GESTION-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Gestión",row,columnCount++);
        commentCell("Especificaciones Campo EPIGRAFE-> \n*Longitud Maxima: 9 \n*Longitud Minima: 9\n*Tipo dato: Texto\n*Responsable: Gestión",row,columnCount++);
        commentCell("Especificaciones Campo CONSOLID-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Consolidación",row,columnCount++);
        commentCell("Especificaciones Campo TIPO APUNTE-> \n*Longitud Maxima: 1 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Control Contable",row,columnCount++);
        commentCell("Especificaciones Campo COD CONTROL-> \n*Longitud Maxima: 2 \n*Longitud Minima: 2\n*Tipo dato: Texto\n*Responsable: Control Contable",row,columnCount++);
        commentCell("Especificaciones Campo IND CIERRE-> \n*Longitud Maxima: 1 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo DIAS PLAZO-> \n*Longitud Maxima: 3 \n*Longitud Minima: 3\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo IND CTA INV-> \n*Longitud Maxima: 1 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Control Contable",row,columnCount++);
        commentCell("Especificaciones Campo INTERFAZ-> \n*Longitud Maxima: 3 \n*Longitud Minima: 3\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL OPERATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL OPERATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL OPERATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL OPERATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL OPERATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL OPERATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL DE GESTION-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL DE GESTION-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL DE GESTION-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL DE GESTION-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL DE GESTION-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL DE GESTION-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL ADMINISTRATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL ADMINISTRATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL ADMINISTRATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL ADMINISTRATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL ADMINISTRATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo RESPONSABLE CONTROL ADMINISTRATIVO-> \n*Longitud Maxima: 5 \n*Longitud Minima: 5\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo CONTRAPARTIDA DE ORDEN-> \n*Longitud Maxima: 9 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo TIPO CUENTA DE ORDEN-> \n*Longitud Maxima: 1 \n*Longitud Minima: 1\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo CONTRAPARTIDA DE RESULTADOS D/H-> \n*Longitud Maxima: 9 \n*Longitud Minima: 9\n*Tipo dato: Texto\n*Responsable: Politica Contable & Tributaria",row,columnCount++);
        commentCell("Especificaciones Campo FECHA CARGUE-> \n*Longitud Maxima: 10 \n*Longitud Minima: 10\n*Tipo dato: Fecha\n*Responsable: Sistematico\n*(Es un campo informativo no se carga)",row,columnCount++);
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

    private void commentCell(String contenido, Row row, int column){
        Comment comment = this.sheet
                .createDrawingPatriarch()
                .createCellComment(new XSSFClientAnchor(15, 15, 15, 15, (short) 10, 10, (short) 15, 15));
        comment.setString(new XSSFRichTextString(contenido));
        row.getCell(column).setCellComment(comment);
    }

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style2.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style2.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));

        for(AccountCreationOther accountCreationOther: listAccount){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,accountCreationOther.getEmpresa(),style);
            createCell(row,columnCount++,accountCreationOther.getNucta(),style);
            createCell(row,columnCount++,accountCreationOther.getNombreCuentaLarga(),style);
            createCell(row,columnCount++,accountCreationOther.getNombreCuentaCorta(),style);
            createCell(row,columnCount++,accountCreationOther.getJustificacionGlobal(),style);
            createCell(row,columnCount++,accountCreationOther.getDinamicaCuenta(),style);
            createCell(row,columnCount++,accountCreationOther.getTipoCta(),style);
            createCell(row,columnCount++,accountCreationOther.getIndic(),style);
            createCell(row,columnCount++,accountCreationOther.getClaveAcceso(),style);
            createCell(row,columnCount++,accountCreationOther.getMon(),style);
            createCell(row,columnCount++,accountCreationOther.getCentroOrigen(),style);
            createCell(row,columnCount++,accountCreationOther.getCentroDestino(),style);
            createCell(row,columnCount++,accountCreationOther.getCodigoGestion(),style);
            createCell(row,columnCount++,accountCreationOther.getEpigrafe(),style);
            createCell(row,columnCount++,accountCreationOther.getConsolid(),style);
            createCell(row,columnCount++,accountCreationOther.getTipoApunte(),style);
            createCell(row,columnCount++,accountCreationOther.getCodigoControl(),style);
            createCell(row,columnCount++,accountCreationOther.getIndicadorCierre(),style);
            createCell(row,columnCount++,accountCreationOther.getDiasPlazo(),style);
            createCell(row,columnCount++,accountCreationOther.getIndicadorCuenta(),style);
            createCell(row,columnCount++,accountCreationOther.getInterfaz(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroloperativocenoperes1(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroloperativocenoperes2(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroloperativocenoperes3(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroloperativocenoperes4(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroloperativocenoperes5(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroloperativocenoperes6(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroldegestioncengesres1(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroldegestioncengesres2(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroldegestioncengesres3(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroldegestioncengesres4(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroldegestioncengesres5(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroldegestioncengesres6(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroladministrativocenadmres1(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroladministrativocenadmres2(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroladministrativocenadmres3(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroladministrativocenadmres4(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroladministrativocenadmres5(),style);
            createCell(row,columnCount++,accountCreationOther.getResponsablecontroladministrativocenadmres6(),style);
            createCell(row,columnCount++,accountCreationOther.getContrapartidaOrden(),style);
            createCell(row,columnCount++,accountCreationOther.getTipoCuentaOrden(),style);
            createCell(row,columnCount++,accountCreationOther.getContrapartidaResultados(),style);
            createCell(row,columnCount++,accountCreationOther.getPeriodo(),style);
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

    public void exportLog(HttpServletResponse response) throws IOException
    {
        sheet = workbook.createSheet("Log_Cuentas_Por_Cobrar");
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

        createCell(row1, 0, accountList.get(accountList.size()-1)[0], style1);
        createCell(row1, 1, accountList.get(accountList.size()-1)[1], style1);
        createCell(row1, 2, accountList.get(accountList.size()-1)[2], style1);

        if(accountList.size()>1)
        {
            accountList.remove(accountList.size()-1);
            Row row2 = sheet.createRow(2);
            createCell(row2, 0, "Fila", style);
            createCell(row2, 1, "Columna", style);
            createCell(row2, 2, "Estado", style);

            int rowCount = 3;
            for (String[] log : accountList)
            {
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
