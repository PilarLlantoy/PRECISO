package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.briefcase.GarantiasActivasIcrf;
import com.inter.proyecto_intergrupo.model.briefcase.ReportIcrv;
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

public class GarantiasActivasIcrfListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> dataStringList;
    private List<GarantiasActivasIcrf> listDataObject;

    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public GarantiasActivasIcrfListReport(List<String[]> dataStringList, List<GarantiasActivasIcrf> listDataObject) {
        this.dataStringList = dataStringList;
        this.listDataObject = listDataObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Garantias Activas ICRF");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int consecutive= 0;
        createCell(row, consecutive++, "Cod Emp", style);
        createCell(row, consecutive++, "Portafolio", style);
        createCell(row, consecutive++, "Negocio", style);
        createCell(row, consecutive++, "Fecha", style);
        createCell(row, consecutive++, "Fecha Final", style);
        createCell(row, consecutive++, "Estado", style);
        createCell(row, consecutive++, "Operacion", style);
        createCell(row, consecutive++, "Nro Papeleta", style);
        createCell(row, consecutive++, "Seguimiento", style);
        createCell(row, consecutive++, "Acceso", style);
        createCell(row, consecutive++, "Origen", style);
        createCell(row, consecutive++, "Moneda", style);
        createCell(row, consecutive++, "Emisor", style);
        createCell(row, consecutive++, "Tipo", style);
        createCell(row, consecutive++, "Nro Titulo", style);
        createCell(row, consecutive++, "Valor Nominal", style);
        createCell(row, consecutive++, "Tasa Nominal", style);
        createCell(row, consecutive++, "Pago Interes", style);
        createCell(row, consecutive++, "Modalidad", style);
        createCell(row, consecutive++, "Tipo Interes", style);
        createCell(row, consecutive++, "Pago Capital", style);
        createCell(row, consecutive++, "Pago Dia", style);
        createCell(row, consecutive++, "Fecha Emision", style);
        createCell(row, consecutive++, "Fecha Vcto", style);
        createCell(row, consecutive++, "Fecha Compra", style);
        createCell(row, consecutive++, "Fecha Val", style);
        createCell(row, consecutive++, "Sw Val Tir", style);
        createCell(row, consecutive++, "Sw 360 Comp", style);
        createCell(row, consecutive++, "Sw 360 Vlr", style);
        createCell(row, consecutive++, "Sw Met Lin", style);
        createCell(row, consecutive++, "Sw Met Mar", style);
        createCell(row, consecutive++, "Tasa Basica", style);
        createCell(row, consecutive++, "Tipo Emisor", style);
        createCell(row, consecutive++, "Sw Margen", style);
        createCell(row, consecutive++, "Nemo Bvc", style);
        createCell(row, consecutive++, "Valor Compra", style);
        createCell(row, consecutive++, "Valor Dia", style);
        createCell(row, consecutive++, "Real 360", style);
        createCell(row, consecutive++, "Real 365", style);
        createCell(row, consecutive++, "Tir 360", style);
        createCell(row, consecutive++, "Tir 365", style);
        createCell(row, consecutive++, "Margen", style);
        createCell(row, consecutive++, "T360 Tre", style);
        createCell(row, consecutive++, "T365 Tre", style);
        createCell(row, consecutive++, "Valor Mercado", style);
        createCell(row, consecutive++, "Valor Vcto", style);
        createCell(row, consecutive++, "Plazo", style);
        createCell(row, consecutive++, "Dias Vcto", style);
        createCell(row, consecutive++, "Dias Int", style);
        createCell(row, consecutive++, "Intereses Cobrar", style);
        createCell(row, consecutive++, "Plaza Expedicion", style);
        createCell(row, consecutive++, "Plazo Rango", style);
        createCell(row, consecutive++, "Cod Cli", style);
        createCell(row, consecutive++, "Aleatorio", style);
        createCell(row, consecutive++, "Tir Margen", style);
        createCell(row, consecutive++, "Tasa Basica Marg", style);
        createCell(row, consecutive++, "Estado Anterior", style);
        createCell(row, consecutive++, "Autorizacion", style);
        createCell(row, consecutive++, "Pap Ant", style);
        createCell(row, consecutive++, "Cod Titulo", style);
        createCell(row, consecutive++, "Nro Derecho", style);
        createCell(row, consecutive++, "Tipo Derecho", style);
        createCell(row, consecutive++, "Llave Titulo", style);
        createCell(row, consecutive++, "Mercado Rtefte", style);
        createCell(row, consecutive++, "Inicio Flujo", style);
        createCell(row, consecutive++, "Fin Flujo", style);
        createCell(row, consecutive++, "Valor Flujo", style);
        createCell(row, consecutive++, "Valor Autoretencion", style);
        createCell(row, consecutive++, "Valor Ret Trasladada", style);
        createCell(row, consecutive++, "Valor Ret Por Pagar", style);
        createCell(row, consecutive++, "Valor Ret Comision", style);
        createCell(row, consecutive++, "Sw Genera Plano", style);
        createCell(row, consecutive++, "Moneda Compra", style);
        createCell(row, consecutive++, "Moneda Emision", style);
        createCell(row, consecutive++, "Fec Cumplimiento", style);
        createCell(row, consecutive++, "Vlr Futuro", style);
        createCell(row, consecutive++, "Intereses Futuros", style);
        createCell(row, consecutive++, "Vlr Compromiso", style);
        createCell(row, consecutive++, "Oper Cubierta", style);
        createCell(row, consecutive++, "Rend Papel Fut", style);
        createCell(row, consecutive++, "Vlr Riesgo Ayer", style);
        createCell(row, consecutive++, "Vlr Riesgo Hoy", style);
        createCell(row, consecutive++, "Tir Papel", style);
        createCell(row, consecutive++, "Fec Val Riesgo", style);
        createCell(row, consecutive++, "Dias Futuros", style);
        createCell(row, consecutive++, "Ajuste Indice", style);
        createCell(row, consecutive++, "Vp Titulo", style);
        createCell(row, consecutive++, "Vp Compromiso", style);
        createCell(row, consecutive++, "Vp Titulo Ayer", style);
        createCell(row, consecutive++, "Vp Compromiso Ayer", style);
        createCell(row, consecutive++, "Pap Cruzada", style);
        createCell(row, consecutive++, "Fecha", style);
        createCell(row, consecutive++, "Negocio", style);
        createCell(row, consecutive++, "Carrusel", style);
        createCell(row, consecutive++, "Usr Actualiza Remate", style);
        createCell(row, consecutive++, "Valor Total", style);
        createCell(row, consecutive++, "Causacion Neta", style);
        createCell(row, consecutive++, "Tipo Op Mdo", style);
        createCell(row, consecutive++, "Fec Val Riesgo", style);
        createCell(row, consecutive++, "Isin Star", style);
        createCell(row, consecutive++, "Nombre", style);
        createCell(row, consecutive++, "Nombre Portafolio", style);
        createCell(row, consecutive++, "Nro Identificacion", style);
        createCell(row, consecutive++, "Ctro Contable Alt", style);
        createCell(row, consecutive++, "Tipo Entidad", style);
        createCell(row, consecutive++, "Nro Ope Origen", style);
        createCell(row, consecutive++, "C", style);
        createCell(row, consecutive++, "N", style);
        createCell(row, consecutive++, "Isin Star", style);
        createCell(row, consecutive++, "Nom Emisor", style);
        createCell(row, consecutive++, "Cta Orden", style);
        createCell(row, consecutive++, "Valor Cupon", style);

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

        for(GarantiasActivasIcrf data: listDataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getCodEmp(),style);
            createCell(row,columnCount++,data.getPortafolio(),style);
            createCell(row,columnCount++,data.getNegocio(),style);
            createCell(row,columnCount++,data.getFecha(),style1);
            createCell(row,columnCount++,data.getFechaFinal(),style1);
            createCell(row,columnCount++,data.getEstado(),style);
            createCell(row,columnCount++,data.getOperacion(),style);
            createCell(row,columnCount++,data.getNroPapeleta(),style);
            createCell(row,columnCount++,data.getSeguimiento(),style);
            createCell(row,columnCount++,data.getAcceso(),style);
            createCell(row,columnCount++,data.getOrigen(),style);
            createCell(row,columnCount++,data.getMoneda(),style);
            createCell(row,columnCount++,data.getEmisor(),style);
            createCell(row,columnCount++,data.getTipo(),style);
            createCell(row,columnCount++,data.getNroTitulo(),style);
            createCell(row,columnCount++,data.getValorNominal(),style2);
            createCell(row,columnCount++,data.getTasaNominal(),style2);
            createCell(row,columnCount++,data.getPagoInteres(),style);
            createCell(row,columnCount++,data.getModalidad(),style);
            createCell(row,columnCount++,data.getTipoInteres(),style);
            createCell(row,columnCount++,data.getPagoCapital(),style);
            createCell(row,columnCount++,data.getPagoDia(),style);
            createCell(row,columnCount++,data.getFechaEmision(),style1);
            createCell(row,columnCount++,data.getFechaVcto(),style1);
            createCell(row,columnCount++,data.getFechaCompra(),style1);
            createCell(row,columnCount++,data.getFechaVal(),style1);
            createCell(row,columnCount++,data.getSwValTir(),style);
            createCell(row,columnCount++,data.getSw360Comp(),style);
            createCell(row,columnCount++,data.getSw360Vlr(),style);
            createCell(row,columnCount++,data.getSwMetLin(),style);
            createCell(row,columnCount++,data.getSwMetMar(),style);
            createCell(row,columnCount++,data.getTasaBasica(),style);
            createCell(row,columnCount++,data.getTipoEmisor(),style);
            createCell(row,columnCount++,data.getSwMargen(),style);
            createCell(row,columnCount++,data.getNemoBvc(),style);
            createCell(row,columnCount++,data.getValorCompra(),style2);
            createCell(row,columnCount++,data.getValorDia(),style2);
            createCell(row,columnCount++,data.getReal360(),style2);
            createCell(row,columnCount++,data.getReal365(),style2);
            createCell(row,columnCount++,data.getTir360(),style2);
            createCell(row,columnCount++,data.getTir365(),style2);
            createCell(row,columnCount++,data.getMargen(),style2);
            createCell(row,columnCount++,data.getT360Tre(),style2);
            createCell(row,columnCount++,data.getT365Tre(),style2);
            createCell(row,columnCount++,data.getValorMercado(),style2);
            createCell(row,columnCount++,data.getValorVcto(),style2);
            createCell(row,columnCount++,data.getPlazo(),style);
            createCell(row,columnCount++,data.getDiasVcto(),style);
            createCell(row,columnCount++,data.getDiasInt(),style);
            createCell(row,columnCount++,data.getInteresesCobrar(),style);
            createCell(row,columnCount++,data.getPlazaExpedicion(),style);
            createCell(row,columnCount++,data.getPlazoRango(),style);
            createCell(row,columnCount++,data.getCodCli(),style);
            createCell(row,columnCount++,data.getAleatorio(),style);
            createCell(row,columnCount++,data.getTirMargen(),style);
            createCell(row,columnCount++,data.getTasaBasicaMarg(),style);
            createCell(row,columnCount++,data.getEstadoAnterior(),style);
            createCell(row,columnCount++,data.getAutorizacion(),style);
            createCell(row,columnCount++,data.getPapAnt(),style);
            createCell(row,columnCount++,data.getCodTitulo(),style);
            createCell(row,columnCount++,data.getNroDerecho(),style);
            createCell(row,columnCount++,data.getTipoDerecho(),style);
            createCell(row,columnCount++,data.getLlaveTitulo(),style);
            createCell(row,columnCount++,data.getMercadoRtefte(),style);
            createCell(row,columnCount++,data.getInicioFlujo(),style1);
            createCell(row,columnCount++,data.getFinFlujo(),style1);
            createCell(row,columnCount++,data.getValorFlujo(),style2);
            createCell(row,columnCount++,data.getValorAutoretencion(),style);
            createCell(row,columnCount++,data.getValorRetTrasladada(),style);
            createCell(row,columnCount++,data.getValorRetPorPagar(),style);
            createCell(row,columnCount++,data.getValorRetComision(),style);
            createCell(row,columnCount++,data.getSwGeneraPlano(),style);
            createCell(row,columnCount++,data.getMonedaCompra(),style2);
            createCell(row,columnCount++,data.getMonedaEmision(),style2);
            createCell(row,columnCount++,data.getFecCumplimiento(),style1);
            createCell(row,columnCount++,data.getVlrFuturo(),style2);
            createCell(row,columnCount++,data.getInteresesFuturos(),style);
            createCell(row,columnCount++,data.getVlrCompromiso(),style);
            createCell(row,columnCount++,data.getOperCubierta(),style);
            createCell(row,columnCount++,data.getRendPapelFut(),style2);
            createCell(row,columnCount++,data.getVlrRiesgoAyer(),style2);
            createCell(row,columnCount++,data.getVlrRiesgoHoy(),style2);
            createCell(row,columnCount++,data.getTirPapel(),style2);
            createCell(row,columnCount++,data.getFecValRiesgo(),style1);
            createCell(row,columnCount++,data.getDiasFuturos(),style);
            createCell(row,columnCount++,data.getAjusteIndice(),style2);
            createCell(row,columnCount++,data.getVpTitulo(),style2);
            createCell(row,columnCount++,data.getVpCompromiso(),style2);
            createCell(row,columnCount++,data.getVpTituloAyer(),style2);
            createCell(row,columnCount++,data.getVpCompromisoAyer(),style2);
            createCell(row,columnCount++,data.getPapCruzada(),style);
            createCell(row,columnCount++,data.getFecha1(),style1);
            createCell(row,columnCount++,data.getNegocio1(),style);
            createCell(row,columnCount++,data.getCarrusel(),style);
            createCell(row,columnCount++,data.getUsrActualizaRemate(),style);
            createCell(row,columnCount++,data.getValorTotal(),style2);
            createCell(row,columnCount++,data.getCausacionNeta(),style2);
            createCell(row,columnCount++,data.getTipoOpMdo(),style);
            createCell(row,columnCount++,data.getFecValRiesgo1(),style1);
            createCell(row,columnCount++,data.getIsinStar(),style);
            createCell(row,columnCount++,data.getNombre(),style);
            createCell(row,columnCount++,data.getNombrePortafolio(),style);
            createCell(row,columnCount++,data.getNroIdentificacion(),style);
            createCell(row,columnCount++,data.getCtroContableAlt(),style);
            createCell(row,columnCount++,data.getTipoEntidad(),style);
            createCell(row,columnCount++,data.getNroOpeOrigen(),style);
            createCell(row,columnCount++,data.getC(),style);
            createCell(row,columnCount++,data.getN(),style);
            createCell(row,columnCount++,data.getIsinStar1(),style);
            createCell(row,columnCount++,data.getNomEmisor(),style);
            createCell(row,columnCount++,data.getCtaOrden(),style);
            createCell(row,columnCount++,data.getValorCupon(),style);
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
