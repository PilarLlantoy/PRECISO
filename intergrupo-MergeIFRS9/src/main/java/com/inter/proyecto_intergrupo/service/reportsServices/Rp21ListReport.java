package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.parametric.TypeEntity;
import com.inter.proyecto_intergrupo.model.reports.Rp21;
import com.inter.proyecto_intergrupo.model.reports.Rp21_Extend;
import com.inter.proyecto_intergrupo.repository.parametric.TypeEntityRepository;
import com.inter.proyecto_intergrupo.repository.reports.Rp21Repository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.type.EntityType;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Rp21ListReport {

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private XSSFSheet sheet1;
    private List<Rp21> rp21List;
    private List<Rp21_Extend> rp21List_ext;
    private List<Object[]> rp21Object;
    private List<Object[]> rp21Liquidez;
    private List<Object[]> rp21LiquidezEntity;
    private TypeEntityRepository typeEntityRepository;

    public Rp21ListReport(List<Rp21> rp21List,List<Object[]> rp21Liquidez,TypeEntityRepository typeEntityRepository,List<Object[]> rp21LiquidezEntity,List<Rp21_Extend> rp21List_ext) {
        this.rp21List = rp21List;
        this.rp21Liquidez = rp21Liquidez;
        this.rp21LiquidezEntity = rp21LiquidezEntity;
        this.typeEntityRepository = typeEntityRepository;
        this.rp21List_ext = rp21List_ext;
        workbook = new XSSFWorkbook();
    }

    public Rp21ListReport(List<Object[]> rp21Object) {
        this.rp21Object = rp21Object;
        workbook = new XSSFWorkbook();

    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Consolidado");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int columnCount = 0;

        createCell(row, columnCount++, "Tipo Operación", style);
        createCell(row, columnCount++, "Tipo Riesgo", style);
        createCell(row, columnCount++, "Nit", style);
        createCell(row, columnCount++, "Contraparte", style);
        createCell(row, columnCount++, "FAlta_", style);
        createCell(row, columnCount++, "FVencimiento_", style);
        createCell(row, columnCount++, "Divisa", style);
        createCell(row, columnCount++, "Vr Nominal Divisa", style);
        createCell(row, columnCount++, "MtM COP", style);
        createCell(row, columnCount++, "Vr Nominal COP", style);
        createCell(row, columnCount++, "Vr Nominal+MtM", style);
        createCell(row, columnCount++, "Intergrupo_", style);
        createCell(row, columnCount++, "Pais_", style);
        createCell(row, columnCount++, "Neocon", style);
        createCell(row, columnCount++, "Local/RP21", style);
        createCell(row, columnCount++, "DIV/RP21", style);
        if(rp21Liquidez != null && rp21Liquidez.size()>0)
        {
            createCell(row, columnCount++, "Días", style);
            createCell(row, columnCount++, "Días1", style);
            createCell(row, columnCount++, "Vto_Real", style);
            createCell(row, columnCount++, "Vto_Residual", style);
        }
        createCell(row, columnCount++, "Neocon_", style);
        createCell(row, columnCount++, "Local Derec", style);
        createCell(row, columnCount++, "Local Obligacion", style);
        createCell(row, columnCount++, "PyG", style);
        createCell(row, columnCount++, "Tipo Contraparte", style);
        createCell(row, columnCount++, "Yntp", style);
        if(rp21Liquidez != null && rp21Liquidez.size()>0) {
            createCell(row, columnCount++, "DÍAS", style);
        }
        createCell(row, columnCount++, "Operacion_", style);
        createCell(row, columnCount++, "DVSA Conciliación 1", style);
        createCell(row, columnCount++, "DVSA Conciliación 2", style);
        createCell(row, columnCount++, "Fecont", style);
        if(rp21List_ext != null && rp21List_ext.size()>0) {
            createCell(row, columnCount++, "Negocio", style);
            createCell(row, columnCount++, "Id_Negocio", style);
        }
        if(rp21Liquidez != null && rp21Liquidez.size()>0) {
            createCell(row, columnCount++, "Tipo Operación Origen", style);
        }

        if(rp21LiquidezEntity!=null &&rp21LiquidezEntity.size()>0)
        {
            sheet1 = workbook.createSheet("Entidades Nuevas");
            Row row1 = sheet1.createRow(0);

            createCell(row1, 0, "Tipo Contraparte", style);
            createCell(row1, 1, "NIT", style);
            createCell(row1, 2, "Contraparte", style);
            createCell(row1, 3, "Intergrupo", style);
            createCell(row1, 4, "Tipo Entidad", style);
            createCell(row1, 5, "Eliminación", style);
        }

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
        int rowCountCell = 0;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for (Rp21 rp21 : rp21List) {
            if(rp21.getVrNominalCOP()!=0 || rp21.getVrNominalMtm()!=0) {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;

                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

                createCell(row, columnCount++, rp21.getTipoOperacion(), style);
                createCell(row, columnCount++, rp21.getTipoRiesgo(), style);
                createCell(row, columnCount++, rp21.getNit(), style);
                createCell(row, columnCount++, rp21.getContraparte(), style);
                if (rp21.getFAlta() != null) {
                    createCell(row, columnCount++, formato.format(rp21.getFAlta()), style);
                } else {
                    createCell(row, columnCount++, "--", style);
                }
                createCell(row, columnCount++, formato.format(rp21.getFVenciemiento()), style);
                createCell(row, columnCount++, rp21.getDivisa(), style);
                createCell(row, columnCount++, rp21.getVrNominalDivisa(), style1);
                createCell(row, columnCount++, rp21.getMtmCOP(), style1);
                createCell(row, columnCount++, rp21.getVrNominalCOP(), style1);
                createCell(row, columnCount++, rp21.getVrNominalMtm(), style1);
                createCell(row, columnCount++, rp21.getIntergrupo(), style);
                createCell(row, columnCount++, rp21.getPais(), style);
                createCell(row, columnCount++, rp21.getNeocon(), style);
                createCell(row, columnCount++, rp21.getLocalRp21(), style);
                createCell(row, columnCount++, rp21.getDivrp21(), style);
                if(rp21Liquidez != null && rp21Liquidez.size()>0)
                {
                    if(rp21Liquidez.get(rowCountCell)[0]!=null) {
                        createCell(row, columnCount++, Double.parseDouble(rp21Liquidez.get(rowCountCell)[0].toString()), style1);
                    }
                    else
                    {
                        createCell(row, columnCount++, "", style1);
                    }
                    createCell(row, columnCount++, Double.parseDouble(rp21Liquidez.get(rowCountCell)[1].toString()), style1);
                    if(rp21Liquidez.get(rowCountCell)[2]!=null) {
                        createCell(row, columnCount++, rp21Liquidez.get(rowCountCell)[2].toString(), style);
                    }
                    else
                    {
                        createCell(row, columnCount++, "", style1);
                    }
                    createCell(row, columnCount++, rp21Liquidez.get(rowCountCell)[3].toString(), style);
                }
                createCell(row, columnCount++, rp21.getNeocon2(), style);
                createCell(row, columnCount++, rp21.getLocalDerec(), style);
                createCell(row, columnCount++, rp21.getLocalObligacion(), style);
                createCell(row, columnCount++, rp21.getPyg(), style);
                createCell(row, columnCount++, rp21.getTipoContraparte(), style);
                createCell(row, columnCount++, rp21.getYintp(), style);
                if(rp21Liquidez != null && rp21Liquidez.size()>0) {
                    createCell(row, columnCount++, rp21Liquidez.get(rowCountCell)[4].toString(), style);
                }
                createCell(row, columnCount++, rp21.getOperacion(), style);
                createCell(row, columnCount++, rp21.getDvsaconciliacion1(), style);
                createCell(row, columnCount++, rp21.getDvsaconciliacion2(), style);
                if (rp21.getFecont() != null) {
                    createCell(row, columnCount++, formato.format(rp21.getFecont()), style);
                }
            }
            rowCountCell++;
        }
    }

    private void writeDataLinesNew() {
        int rowCount = 1;
        int rowCountCell = 0;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for (Rp21_Extend rp21Ext : rp21List_ext) {
            if(rp21Ext.getVrNominalCOP()!=0 || rp21Ext.getVrNominalMtm()!=0) {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;

                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

                createCell(row, columnCount++, rp21Ext.getTipoOperacion(), style);
                createCell(row, columnCount++, rp21Ext.getTipoRiesgo(), style);
                createCell(row, columnCount++, rp21Ext.getNit(), style);
                createCell(row, columnCount++, rp21Ext.getContraparte(), style);
                if (rp21Ext.getFAlta() != null) {
                    createCell(row, columnCount++, formato.format(rp21Ext.getFAlta()), style);
                } else {
                    createCell(row, columnCount++, "--", style);
                }
                createCell(row, columnCount++, formato.format(rp21Ext.getFVenciemiento()), style);
                createCell(row, columnCount++, rp21Ext.getDivisa(), style);
                createCell(row, columnCount++, rp21Ext.getVrNominalDivisa(), style1);
                createCell(row, columnCount++, rp21Ext.getMtmCOP(), style1);
                createCell(row, columnCount++, rp21Ext.getVrNominalCOP(), style1);
                createCell(row, columnCount++, rp21Ext.getVrNominalMtm(), style1);
                createCell(row, columnCount++, rp21Ext.getIntergrupo(), style);
                createCell(row, columnCount++, rp21Ext.getPais(), style);
                createCell(row, columnCount++, rp21Ext.getNeocon(), style);
                createCell(row, columnCount++, rp21Ext.getLocalRp21(), style);
                createCell(row, columnCount++, rp21Ext.getDivrp21(), style);
                if(rp21Liquidez != null && rp21Liquidez.size()>0)
                {
                    if(rp21Liquidez.get(rowCountCell)[0]!=null) {
                        createCell(row, columnCount++, Double.parseDouble(rp21Liquidez.get(rowCountCell)[0].toString()), style1);
                    }
                    else
                    {
                        createCell(row, columnCount++, "", style1);
                    }
                    createCell(row, columnCount++, Double.parseDouble(rp21Liquidez.get(rowCountCell)[1].toString()), style1);
                    if(rp21Liquidez.get(rowCountCell)[2]!=null) {
                        createCell(row, columnCount++, rp21Liquidez.get(rowCountCell)[2].toString(), style);
                    }
                    else
                    {
                        createCell(row, columnCount++, "", style1);
                    }
                    createCell(row, columnCount++, rp21Liquidez.get(rowCountCell)[3].toString(), style);
                }
                createCell(row, columnCount++, rp21Ext.getNeocon2(), style);
                createCell(row, columnCount++, rp21Ext.getLocalDerec(), style);
                createCell(row, columnCount++, rp21Ext.getLocalObligacion(), style);
                createCell(row, columnCount++, rp21Ext.getPyg(), style);
                createCell(row, columnCount++, rp21Ext.getTipoContraparte(), style);
                createCell(row, columnCount++, rp21Ext.getYintp(), style);
                if(rp21Liquidez != null && rp21Liquidez.size()>0) {
                    createCell(row, columnCount++, rp21Liquidez.get(rowCountCell)[4].toString(), style);
                }
                createCell(row, columnCount++, rp21Ext.getOperacion(), style);
                createCell(row, columnCount++, rp21Ext.getDvsaconciliacion1(), style);
                createCell(row, columnCount++, rp21Ext.getDvsaconciliacion2(), style);

                if (rp21Ext.getFecont() != null) {
                    createCell(row, columnCount++, formato.format(rp21Ext.getFecont()), style);
                }
                else
                {
                    createCell(row, columnCount++, "", style);
                }
                if (rp21Ext.getNegocio() != null) {
                    createCell(row, columnCount++, rp21Ext.getNegocio(), style);
                }
                else
                {
                    createCell(row, columnCount++, "", style);
                }
                if (rp21Ext.getIdNegocio() != null) {
                    createCell(row, columnCount++, rp21Ext.getIdNegocio(), style);
                }
                else
                {
                    createCell(row, columnCount++, "", style);
                }

            }
            rowCountCell++;
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLinesNew();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void loadPath(String periodo) throws IOException {
        writeHeaderLine();
        writeDataLinesNew();

        try (FileOutputStream output = new FileOutputStream("\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\infocontable\\FINREP-COREP\\ReporteRp21_"+periodo.replace("-","_")+".xlsx")) {
        //try (FileOutputStream output = new FileOutputStream("C:\\Users\\CE66916\\Documents\\BBVA Intergrupo\\ReporteRp21_"+periodo.replace("-","_")+".xlsx")) {
            workbook.write(output);
            workbook.close();
        }
    }
    public void exportOriginal(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    private void writeDataLinesLiquidez() {
        int rowCount = 1;
        int rowCountNueva = 1;
        int rowCountCell = 0;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for (Object[] rp21 : rp21Liquidez) {
            if(Double.parseDouble(rp21[9].toString())!=0 || Double.parseDouble(rp21[10].toString())!=0 )
            {
                Row row = sheet.createRow(rowCount++);
                int columnCount = 0;

                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

                createCell(row, columnCount++, rp21[0], style);
                createCell(row, columnCount++, rp21[1], style);
                createCell(row, columnCount++, rp21[2], style);
                createCell(row, columnCount++, rp21[3], style);
                if (rp21[4] != null && rp21[4].toString() != null) {
                    createCell(row, columnCount++, formato.format(rp21[4]), style);
                } else {
                    createCell(row, columnCount++, "--", style);
                }
                createCell(row, columnCount++, formato.format(rp21[5]), style);
                createCell(row, columnCount++, rp21[6], style);
                createCell(row, columnCount++, rp21[7], style1);
                createCell(row, columnCount++, rp21[8], style1);
                createCell(row, columnCount++, rp21[9], style1);
                createCell(row, columnCount++, rp21[10], style1);
                createCell(row, columnCount++, rp21[11], style);
                createCell(row, columnCount++, rp21[12], style);
                createCell(row, columnCount++, rp21[13], style);
                createCell(row, columnCount++, rp21[14], style);
                createCell(row, columnCount++, rp21[15], style);

                if(rp21[16]!=null) {
                    createCell(row, columnCount++, Double.parseDouble(rp21[16].toString()), style1);
                }
                else
                {
                    createCell(row, columnCount++, "", style1);
                }
                createCell(row, columnCount++, Double.parseDouble(rp21[17].toString()), style1);
                if(rp21[18]!=null) {
                    createCell(row, columnCount++, rp21[18], style);
                }
                else
                {
                    createCell(row, columnCount++, "", style1);
                }
                createCell(row, columnCount++, rp21[19], style);

                createCell(row, columnCount++, rp21[20], style);
                createCell(row, columnCount++, rp21[21], style);
                createCell(row, columnCount++, rp21[22], style);
                createCell(row, columnCount++, rp21[23], style);
                if(rp21[33]!=null) {
                    createCell(row, columnCount++, rp21[33], style);
                }
                else if(rp21[32]!=null) {
                    createCell(row, columnCount++, rp21[32], style);
                }
                else if(rp21[31]!=null) {
                    createCell(row, columnCount++, rp21[31], style);
                }
                else
                {
                    createCell(row, columnCount++, "SIN PARAMETRIZAR", style);
                    //Row row1 = sheet1.createRow(rowCountNueva++);
                    //createCell(row1, 0, rp21[24], style);
                }
                createCell(row, columnCount++, rp21[25], style);

                createCell(row, columnCount++, rp21[26], style);

                createCell(row, columnCount++, rp21[27], style);
                createCell(row, columnCount++, rp21[28], style);
                createCell(row, columnCount++, rp21[29], style);
                if (rp21[30] != null && rp21[30].toString() != null) {
                    createCell(row, columnCount++, formato.format(rp21[30]), style);
                }
                else
                {
                    createCell(row, columnCount++, "", style);
                }
                if (rp21[24] != null && rp21[24].toString() != null) {
                    createCell(row, columnCount++, rp21[24], style);
                }
            }
            rowCountCell++;
        }

        rowCount = 1;

        for (Object[] rp21 : rp21LiquidezEntity)
        {
            Row row1 = sheet1.createRow(rowCount++);
            int columnCount = 0;
            createCell(row1, columnCount++, rp21[0], style);
            createCell(row1, columnCount++, rp21[1], style);
            createCell(row1, columnCount++, rp21[2], style);
            createCell(row1, columnCount++, rp21[3], style);
            createCell(row1, columnCount++, rp21[4], style);
            if(rp21[5]!=null && rp21[5].toString().equals("false")) {
                createCell(row1, columnCount++, "No", style);
            }
            else
            {
                createCell(row1, columnCount++, "Si", style);
            }
        }
    }

    public void exportLiquidez(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLinesLiquidez();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public void exportReport(HttpServletResponse response) throws IOException {
        writeHeaderLineReport();
        writeDataLinesReport();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    private void writeHeaderLineReport() {
        sheet = workbook.createSheet("Reporte Rp21");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        createCell(row, 0, "Local Rp21", style);
        createCell(row, 1, "Yntp", style);
        createCell(row, 2, "Divisa", style);
        createCell(row, 3, "VR Nominal Divisa", style);
        createCell(row, 4, "MtM COP", style);
        createCell(row, 5, "VR Nominal COP", style);
        createCell(row, 6, "VR Nominal MtM", style);
    }

    private void writeDataLinesReport() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);
        style1.setFont(font);
        style1.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for (Object[] rp21 : rp21Object) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, rp21[0], style);
            createCell(row, columnCount++, rp21[1], style);
            createCell(row, columnCount++, rp21[2], style);
            createCell(row, columnCount++, BigDecimal.valueOf(Double.parseDouble(rp21[3].toString())).doubleValue(), style1);
            createCell(row, columnCount++, BigDecimal.valueOf(Double.parseDouble(rp21[4].toString())).doubleValue(), style1);
            createCell(row, columnCount++, BigDecimal.valueOf(Double.parseDouble(rp21[5].toString())).doubleValue(), style1);
            createCell(row, columnCount++, BigDecimal.valueOf(Double.parseDouble(rp21[6].toString())).doubleValue(), style1);
        }
    }

    public void exportLog(HttpServletResponse response, List<String[]> lista) throws IOException {
        int position = 0;
        String[] temporalListNames = lista.get(lista.size() - 1);
        List<String> list = new ArrayList<>();
        if (temporalListNames != null) {
            String[] names = temporalListNames[0].split(".xlsx");
            for (String name : names) {
                list.add(name.replace("null",""));
            }
        }
        if (list.contains("FW_CONCILIACION")) {
            position = exportDolares(response, lista, position);
            position = exportDivisas(response, lista, position);
            position = exportTitulos(response, lista, position);
        }
        if (list.contains("SWAP_CONCILIACION")) {
            position = exportSwap(response, lista, position);
        }
        if (list.contains("RYS_CONCILIACION")) {
            position = exportRYS(response, lista, position);
        }
        if (list.contains("OPCIONES_CONCILIACION")) {
            position = exportOpciones(response, lista, position);
        }
        if (list.contains("CRCC_CONCILIACION")) {
            position = exportFuturos(response, lista, position);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }

    public int exportDolares(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_FW_DOLARES");
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
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("FW DOLARES"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }

    public int exportDivisas(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_FW_DIVISAS");
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
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("FW DIVISAS"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }

    public int exportTitulos(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_FW_TITULOS");
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
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("FW TITULOS"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }

    public int exportSwap(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_SWAP");
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
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("SWAP"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }
        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }

    public int exportRYS(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_RYS");
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
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("RYS"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }

        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }

    public int exportOpciones(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_OPCIONES");
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
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("OPCIONES"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }
        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        position++;
        return position;
    }

    public int exportFuturos(HttpServletResponse response, List<String[]> lista, int position) throws IOException {
        sheet = workbook.createSheet("LOG_FUTUROS");
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
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int rowCount = 2;

        font.setBold(false);
        font.setFontHeight(10);

        for (int i = position; i < lista.size() && !lista.get(i)[0].equals("FUTUROS"); i++) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, lista.get(i)[0], style);
            createCell(row, columnCount++, lista.get(i)[1], style);
            createCell(row, columnCount++, lista.get(i)[3], style);
            position++;
        }
        createCell(row2, 3, "Registros validados exitosos: ", style);
        createCell(row2, 4, lista.get(position)[1], style);

        createCell(row1, 3, "Registros validados fallidos: ", style);
        createCell(row1, 4, lista.get(position)[2], style);

        createCell(row2, 6, "¡Los registros cargados en el inventario son los catalogados como miembro M380 y titular P01!", style);

        return position;
    }
}
