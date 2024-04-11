package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.briefcase.GarantiasActivasIcrf;
import com.inter.proyecto_intergrupo.model.briefcase.ReposIcrf;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ReposIcrfListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> dataStringList;
    private List<ReposIcrf> listDataObject;

    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public ReposIcrfListReport(List<String[]> dataStringList, List<ReposIcrf> listDataObject) {
        this.dataStringList = dataStringList;
        this.listDataObject = listDataObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Repos e Interbancarios ICRF");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int consecutive= 0;
        createCell(row, consecutive++, "Codigo", style);
        createCell(row, consecutive++, "Nombre", style);
        createCell(row, consecutive++, "Negociador", style);
        createCell(row, consecutive++, "Cod Cli", style);
        createCell(row, consecutive++, "Fecha", style);
        createCell(row, consecutive++, "Fecha Final", style);
        createCell(row, consecutive++, "Valor Total", style);
        createCell(row, consecutive++, "Intereses", style);
        createCell(row, consecutive++, "Tasa", style);
        createCell(row, consecutive++, "Negocio", style);
        createCell(row, consecutive++, "Estado", style);
        createCell(row, consecutive++, "Tipo Mov", style);
        createCell(row, consecutive++, "Nro Papeleta", style);
        createCell(row, consecutive++, "Tipo Op Mdo", style);
        createCell(row, consecutive++, "Causacion Hoy", style);
        createCell(row, consecutive++, "Causacion Ayer", style);
        createCell(row, consecutive++, "Portafolio", style);
        createCell(row, consecutive++, "Contabilidad", style);
        createCell(row, consecutive++, "Tasa Mesa", style);
        createCell(row, consecutive++, "Causacion Neta", style);
        createCell(row, consecutive++, "Nombre1", style);
        createCell(row, consecutive++, "Nro Identificacion", style);
        createCell(row, consecutive++, "Duracion Modificada Anual", style);
        createCell(row, consecutive++, "Moneda", style);
        createCell(row, consecutive++, "Ctro Contable Alt", style);
        createCell(row, consecutive++, "Tipo Entidad", style);
        createCell(row, consecutive++, "Vlr Libros", style);
        createCell(row, consecutive++, "Vlr Libros Intereses Hoy", style);
        createCell(row, consecutive++, "Vlr Mdo Gtia Act", style);
        createCell(row, consecutive++, "Vlr Mdo Gtia Pas", style);
        createCell(row, consecutive++, "Pap Reemplaza", style);
        createCell(row, consecutive++, "Calif Contraparte", style);
        createCell(row, consecutive++, "Cod Puc", style);
        createCell(row, consecutive++, "Codpuc Incumpl", style);
        createCell(row, consecutive++, "Monto Prov", style);
        createCell(row, consecutive++, "Monto Ext", style);
        createCell(row, consecutive++, "Monto Fin Ext", style);
        createCell(row, consecutive++, "Monto Legal F", style);
        createCell(row, consecutive++, "Puc Rend", style);
        createCell(row, consecutive++, "Monto Rend", style);
        createCell(row, consecutive++, "Cod Puccupon Ar", style);
        createCell(row, consecutive++, "Monto Cupon Ar", style);
        createCell(row, consecutive++, "Codpuc Cupon", style);
        createCell(row, consecutive++, "Monto Cupon", style);
        createCell(row, consecutive++, "Llamado Margen", style);
        createCell(row, consecutive++, "Tp Llamado Margen", style);
        createCell(row, consecutive++, "Codpuc Llamado Margend", style);
        createCell(row, consecutive++, "Codpuc Llamado Margenv", style);
        createCell(row, consecutive++, "Mnto Acum Llamado Margen", style);
        createCell(row, consecutive++, "Exposicon Neta", style);
        createCell(row, consecutive++, "Tipo Vinculacion", style);
        createCell(row, consecutive++, "Codigo Normalizado", style);
        createCell(row, consecutive++, "Portafolio Front", style);
        createCell(row, consecutive++, "Cta Balance", style);
        createCell(row, consecutive++, "Cta Balance Interes", style);
        createCell(row, consecutive++, "Cta Pyg", style);
        createCell(row, consecutive++, "Isin Cdd", style);
        createCell(row, consecutive++, "Cod Emisor", style);
        createCell(row, consecutive++, "Nom Emisor", style);



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

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        style.setFont(font);

        CellStyle style1 = workbook.createCellStyle();
        XSSFFont font1 = workbook.createFont();
        font1.setFontHeight(10);
        style1.setFont(font1);
        style1.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm:ss"));

        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font2 = workbook.createFont();
        font2.setFontHeight(10);
        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(ReposIcrf data: listDataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getCodigo(),style);
            createCell(row,columnCount++,data.getNombre(),style);
            createCell(row,columnCount++,data.getNegociador(),style);
            createCell(row,columnCount++,data.getCodCli(),style);
            createCell(row,columnCount++,data.getFecha(),style1);
            createCell(row,columnCount++,data.getFechaFinal(),style1);
            createCell(row,columnCount++,data.getValorTotal(),style2);
            createCell(row,columnCount++,data.getIntereses(),style);
            createCell(row,columnCount++,data.getTasa(),style);
            createCell(row,columnCount++,data.getNegocio(),style);
            createCell(row,columnCount++,data.getEstado(),style);
            createCell(row,columnCount++,data.getTipoMov(),style);
            createCell(row,columnCount++,data.getNroPapeleta(),style);
            createCell(row,columnCount++,data.getTipoOpMdo(),style);
            createCell(row,columnCount++,data.getCausacionHoy(),style2);
            createCell(row,columnCount++,data.getCausacionAyer(),style2);
            createCell(row,columnCount++,data.getPortafolio(),style);
            createCell(row,columnCount++,data.getContabilidad(),style);
            createCell(row,columnCount++,data.getTasaMesa(),style);
            createCell(row,columnCount++,data.getCausacionNeta(),style);
            createCell(row,columnCount++,data.getNombre1(),style);
            createCell(row,columnCount++,data.getNroIdentificacion(),style);
            createCell(row,columnCount++,data.getDuracionModificadaAnual(),style);
            createCell(row,columnCount++,data.getMoneda(),style);
            createCell(row,columnCount++,data.getCtroContableAlt(),style);
            createCell(row,columnCount++,data.getTipoEntidad(),style);
            createCell(row,columnCount++,data.getVlrLibros(),style2);
            createCell(row,columnCount++,data.getVlrLibrosInteresesHoy(),style);
            createCell(row,columnCount++,data.getVlrMdoGtiaAct(),style);
            createCell(row,columnCount++,data.getVlrMdoGtiaPas(),style);
            createCell(row,columnCount++,data.getPapReemplaza(),style);
            createCell(row,columnCount++,data.getCalifContraparte(),style);
            createCell(row,columnCount++,data.getCodPuc(),style);
            createCell(row,columnCount++,data.getCodpucIncumpl(),style);
            createCell(row,columnCount++,data.getMontoProv(),style2);
            createCell(row,columnCount++,data.getMontoExt(),style2);
            createCell(row,columnCount++,data.getMontoFinExt(),style2);
            createCell(row,columnCount++,data.getMontoLegalF(),style2);
            createCell(row,columnCount++,data.getPucRend(),style);
            createCell(row,columnCount++,data.getMontoRend(),style);
            createCell(row,columnCount++,data.getCodPuccuponAr(),style);
            createCell(row,columnCount++,data.getMontoCuponAr(),style);
            createCell(row,columnCount++,data.getCodpucCupon(),style);
            createCell(row,columnCount++,data.getMontoCupon(),style);
            createCell(row,columnCount++,data.getLlamadoMargen(),style);
            createCell(row,columnCount++,data.getTpLlamadoMargen(),style);
            createCell(row,columnCount++,data.getCodpucLlamadoMargend(),style);
            createCell(row,columnCount++,data.getCodpucLlamadoMargenv(),style);
            createCell(row,columnCount++,data.getMntoAcumLlamadoMargen(),style);
            createCell(row,columnCount++,data.getExposiconNeta(),style);
            createCell(row,columnCount++,data.getTipoVinculacion(),style);
            createCell(row,columnCount++,data.getCodigoNormalizado(),style);
            createCell(row,columnCount++,data.getPortafolioFront(),style);
            createCell(row,columnCount++,data.getCtaBalance(),style);
            createCell(row,columnCount++,data.getCtaBalanceInteres(),style);
            createCell(row,columnCount++,data.getCtaPyg(),style);
            createCell(row,columnCount++,data.getIsinCdd(),style);
            createCell(row,columnCount++,data.getCodEmisor(),style);
            createCell(row,columnCount++,data.getNomEmisor(),style);

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
        sheet = workbook.createSheet("Log_Cargue");
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

        createCell(row1, 0, dataStringList.get(dataStringList.size()-1)[0], style1);
        createCell(row1, 1, dataStringList.get(dataStringList.size()-1)[1], style1);
        createCell(row1, 2, dataStringList.get(dataStringList.size()-1)[2], style1);

        if(dataStringList.size()>1)
        {
            dataStringList.remove(dataStringList.size()-1);
            Row row2 = sheet.createRow(2);
            createCell(row2, 0, "Fila", style);
            createCell(row2, 1, "Columna", style);
            createCell(row2, 2, "Estado", style);

            int rowCount = 3;
            for (String[] log : dataStringList)
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
