package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.briefcase.CalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.F351Icrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaCalculoIcrv;
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

public class F351IcrvListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<String[]> dataStringList;
    private List<F351Icrv> listDataObject;

    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;

    public F351IcrvListReport(List<String[]> dataStringList, List<F351Icrv> listDataObject) {
        this.dataStringList = dataStringList;
        this.listDataObject = listDataObject;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("F351 ICRV");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(11);
        style.setFont(font);

        int consecutive= 0;
        createCell(row, consecutive++, "Fecha Proceso", style);
        createCell(row, consecutive++, "Nro Asignado", style);
        createCell(row, consecutive++, "Codigo Puc", style);
        createCell(row, consecutive++, "Nit", style);
        createCell(row, consecutive++, "Documento Emisor", style);
        createCell(row, consecutive++, "Razon Social Emisor", style);
        createCell(row, consecutive++, "Vinculado", style);
        createCell(row, consecutive++, "Aval", style);
        createCell(row, consecutive++, "Tipo Identificacion Aval", style);
        createCell(row, consecutive++, "Identificacion Aval", style);
        createCell(row, consecutive++, "Razon Social Aval", style);
        createCell(row, consecutive++, "Identifacion Administrador", style);
        createCell(row, consecutive++, "Razon Social Administrador", style);
        createCell(row, consecutive++, "Clase Inversion", style);
        createCell(row, consecutive++, "Nemotecnico", style);
        createCell(row, consecutive++, "Cupon Principal", style);
        createCell(row, consecutive++, "Fecha Emite", style);
        createCell(row, consecutive++, "Fecha Vcto", style);
        createCell(row, consecutive++, "Fecha Vcto Cupon", style);
        createCell(row, consecutive++, "Fecha Compra", style);
        createCell(row, consecutive++, "Cod Moneda", style);
        createCell(row, consecutive++, "Valor Nominal", style);
        createCell(row, consecutive++, "Amortizaciones", style);
        createCell(row, consecutive++, "Valor Nominal Capitalizado", style);
        createCell(row, consecutive++, "20 Numero Acciones", style);
        createCell(row, consecutive++, "Clase Accion", style);
        createCell(row, consecutive++, "Valor Compra", style);
        createCell(row, consecutive++, "Valor Compra Pesos", style);
        createCell(row, consecutive++, "Tasa Facial", style);
        createCell(row, consecutive++, "Valor Tasa", style);
        createCell(row, consecutive++, "Calculo Interes", style);
        createCell(row, consecutive++, "Periodicidad Pago", style);
        createCell(row, consecutive++, "Modalidad", style);
        createCell(row, consecutive++, "Ind Tasa Referencia", style);
        createCell(row, consecutive++, "Valor Mercado-1316", style);
        createCell(row, consecutive++, "Valor Presente Pesos", style);
        createCell(row, consecutive++, "Valor Mercado Dif Peso", style);
        createCell(row, consecutive++, "Tasa Negociacion", style);
        createCell(row, consecutive++, "Dias Vcto", style);
        createCell(row, consecutive++, "Tasa Referencia", style);
        createCell(row, consecutive++, "Valor Tasa Referencia", style);
        createCell(row, consecutive++, "Valor Tasa Primer Flujo", style);
        createCell(row, consecutive++, "Margen Valora", style);
        createCell(row, consecutive++, "Tasa Descuento", style);
        createCell(row, consecutive++, "Precio", style);
        createCell(row, consecutive++, "Metodo Valora", style);
        createCell(row, consecutive++, "Fecha Ultimo Reprecio", style);
        createCell(row, consecutive++, "Valor Presente Reprecio", style);
        createCell(row, consecutive++, "Ind Bursatibilidad", style);
        createCell(row, consecutive++, "Interes Vencidos", style);
        createCell(row, consecutive++, "Puc Provision", style);
        createCell(row, consecutive++, "Base Provision", style);
        createCell(row, consecutive++, "Valor Provision", style);
        createCell(row, consecutive++, "Calificacion", style);
        createCell(row, consecutive++, "Entidad Calificadora", style);
        createCell(row, consecutive++, "Calificacion Riesgo", style);
        createCell(row, consecutive++, "Calificacion Avalista", style);
        createCell(row, consecutive++, "Calificacion Soberania", style);
        createCell(row, consecutive++, "Entidad Calificadora Soberania", style);
        createCell(row, consecutive++, "Custodio", style);
        createCell(row, consecutive++, "Numero Identificacion", style);
        createCell(row, consecutive++, "Fungible", style);
        createCell(row, consecutive++, "Monto Emision", style);
        createCell(row, consecutive++, "Porcentaje Participacion", style);
        createCell(row, consecutive++, "Ramo", style);
        createCell(row, consecutive++, "Relacion Matrix", style);
        createCell(row, consecutive++, "Concentracion Propiedad", style);
        createCell(row, consecutive++, "Relacion Vinculacion", style);
        createCell(row, consecutive++, "Codigo Puc Causacion", style);
        createCell(row, consecutive++, "Causacion Valoracion", style);
        createCell(row, consecutive++, "Codigo Puc Causa Pat", style);
        createCell(row, consecutive++, "Causa Valoracion Pat(Ajuste Mes)", style);
        createCell(row, consecutive++, "Fecha Corte", style);
        createCell(row, consecutive++, "Unidad Captura", style);
        createCell(row, consecutive++, "Cod Emp", style);
        createCell(row, consecutive++, "Portafolio", style);
        createCell(row, consecutive++, "Tipo Evaluacion", style);
        createCell(row, consecutive++, "Tipo Fideicomiso", style);
        createCell(row, consecutive++, "Cod Fideicomiso", style);
        createCell(row, consecutive++, "Tipo Entidad Vig", style);
        createCell(row, consecutive++, "Cod Entidad Vig", style);
        createCell(row, consecutive++, "Valor Valorizacion(199550-9650)", style);
        createCell(row, consecutive++, "Valor Desvaloriza", style);
        createCell(row, consecutive++, "Fecha Tasa Ref", style);
        createCell(row, consecutive++, "Numero Asignado", style);
        createCell(row, consecutive++, "Vlr Mercado Inv", style);
        createCell(row, consecutive++, "Negocio Dn02", style);
        createCell(row, consecutive++, "Operación Futuro", style);
        createCell(row, consecutive++, "Valoracion Portafolio", style);
        createCell(row, consecutive++, "Tipo Titulo", style);
        createCell(row, consecutive++, "Isin Star", style);
        createCell(row, consecutive++, "Registro Manual", style);
        createCell(row, consecutive++, "Ciiu", style);
        createCell(row, consecutive++, "Naturaleza Jurídica", style);
        createCell(row, consecutive++, "Vinculación", style);
        createCell(row, consecutive++, "Proveedor De Precios", style);
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

        CellStyle style2 = workbook.createCellStyle();
        XSSFFont font2 = workbook.createFont();
        font2.setFontHeight(10);
        style2.setFont(font);
        style2.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        for(F351Icrv data: listDataObject){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row,columnCount++,data.getFechaProceso(),style);
            createCell(row,columnCount++,data.getNroAsignado(),style);
            createCell(row,columnCount++,data.getCodigoPuc(),style);
            createCell(row,columnCount++,data.getNit(),style);
            createCell(row,columnCount++,data.getDocumentoEmisor(),style);
            createCell(row,columnCount++,data.getRazonSocialEmisor(),style);
            createCell(row,columnCount++,data.getVinculado(),style);
            createCell(row,columnCount++,data.getAval(),style);
            createCell(row,columnCount++,data.getTipoIdentificacionAval(),style);
            createCell(row,columnCount++,data.getIdentificacionAval(),style);
            createCell(row,columnCount++,data.getRazonSocialAval(),style);
            createCell(row,columnCount++,data.getIdentifacionAdministrador(),style);
            createCell(row,columnCount++,data.getRazonSocialAdministrador(),style);
            createCell(row,columnCount++,data.getClaseInversion(),style);
            createCell(row,columnCount++,data.getNemotecnico(),style);
            createCell(row,columnCount++,data.getCuponPrincipal(),style);
            createCell(row,columnCount++,data.getFechaEmite(),style);
            createCell(row,columnCount++,data.getFechaVcto(),style);
            createCell(row,columnCount++,data.getFechaVctoCupon(),style);
            createCell(row,columnCount++,data.getFechaCompra(),style);
            createCell(row,columnCount++,data.getCodMoneda(),style);
            createCell(row,columnCount++,data.getValorNominal(),style);
            createCell(row,columnCount++,data.getAmortizaciones(),style);
            createCell(row,columnCount++,data.getValorNominalCapitalizado(),style);
            createCell(row,columnCount++,data.getNumeroAcciones(),style);
            createCell(row,columnCount++,data.getClaseAccion(),style);
            createCell(row,columnCount++,data.getValorCompra(),style);
            createCell(row,columnCount++,data.getValorCompraPesos(),style);
            createCell(row,columnCount++,data.getTasaFacial(),style);
            createCell(row,columnCount++,data.getValorTasa(),style);
            createCell(row,columnCount++,data.getCalculoInteres(),style);
            createCell(row,columnCount++,data.getPeriodicidadPago(),style);
            createCell(row,columnCount++,data.getModalidad(),style);
            createCell(row,columnCount++,data.getIndTasaReferencia(),style);
            createCell(row,columnCount++,data.getValorMercado1316(),style);
            createCell(row,columnCount++,data.getValorPresentePesos(),style);
            createCell(row,columnCount++,data.getValorMercadoDifPeso(),style);
            createCell(row,columnCount++,data.getTasaNegociacion(),style);
            createCell(row,columnCount++,data.getDiasVcto(),style);
            createCell(row,columnCount++,data.getTasaReferencia(),style);
            createCell(row,columnCount++,data.getValorTasaReferencia(),style);
            createCell(row,columnCount++,data.getValorTasaPrimerFlujo(),style);
            createCell(row,columnCount++,data.getMargenValora(),style);
            createCell(row,columnCount++,data.getTasaDescuento(),style);
            createCell(row,columnCount++,data.getPrecio(),style);
            createCell(row,columnCount++,data.getMetodoValora(),style);
            createCell(row,columnCount++,data.getFechaUltimoReprecio(),style);
            createCell(row,columnCount++,data.getValorPresenteReprecio(),style);
            createCell(row,columnCount++,data.getIndBursatibilidad(),style);
            createCell(row,columnCount++,data.getInteresVencidos(),style);
            createCell(row,columnCount++,data.getPucProvision(),style);
            createCell(row,columnCount++,data.getBaseProvision(),style);
            createCell(row,columnCount++,data.getValorProvision(),style);
            createCell(row,columnCount++,data.getCalificacion(),style);
            createCell(row,columnCount++,data.getEntidadCalificadora(),style);
            createCell(row,columnCount++,data.getCalificacionRiesgo(),style);
            createCell(row,columnCount++,data.getCalificacionAvalista(),style);
            createCell(row,columnCount++,data.getCalificacionSoberania(),style);
            createCell(row,columnCount++,data.getEntidadCalificadoraSoberania(),style);
            createCell(row,columnCount++,data.getCustodio(),style);
            createCell(row,columnCount++,data.getNumeroIdentificacion(),style);
            createCell(row,columnCount++,data.getFungible(),style);
            createCell(row,columnCount++,data.getMontoEmision(),style);
            createCell(row,columnCount++,data.getPorcentajeParticipacion(),style);
            createCell(row,columnCount++,data.getRamo(),style);
            createCell(row,columnCount++,data.getRelacionMatrix(),style);
            createCell(row,columnCount++,data.getConcentracionPropiedad(),style);
            createCell(row,columnCount++,data.getRelacionVinculacion(),style);
            createCell(row,columnCount++,data.getCodigoPucCausacion(),style);
            createCell(row,columnCount++,data.getCausacionValoracion(),style);
            createCell(row,columnCount++,data.getCodigoPucCausaPat(),style);
            createCell(row,columnCount++,data.getCausaValoracionPat(),style);
            createCell(row,columnCount++,data.getFechaCorte(),style);
            createCell(row,columnCount++,data.getUnidadCaptura(),style);
            createCell(row,columnCount++,data.getCodEmp(),style);
            createCell(row,columnCount++,data.getPortafolio(),style);
            createCell(row,columnCount++,data.getTipoEvaluacion(),style);
            createCell(row,columnCount++,data.getTipoFideicomiso(),style);
            createCell(row,columnCount++,data.getCodFideicomiso(),style);
            createCell(row,columnCount++,data.getTipoEntidadVig(),style);
            createCell(row,columnCount++,data.getCodEntidadVig(),style);
            createCell(row,columnCount++,data.getValorValorizacion(),style);
            createCell(row,columnCount++,data.getValorDesvaloriza(),style);
            createCell(row,columnCount++,data.getFechaTasaRef(),style);
            createCell(row,columnCount++,data.getNumeroAsignado(),style);
            createCell(row,columnCount++,data.getVlrMercadoInv(),style);
            createCell(row,columnCount++,data.getNegocioDn02(),style);
            createCell(row,columnCount++,data.getOperacionFuturo(),style);
            createCell(row,columnCount++,data.getValoracionPortafolio(),style);
            createCell(row,columnCount++,data.getTipoTitulo(),style);
            createCell(row,columnCount++,data.getIsinStar(),style);
            createCell(row,columnCount++,data.getRegistroManual(),style);
            createCell(row,columnCount++,data.getCiiu(),style);
            createCell(row,columnCount++,data.getNaturalezaJuridica(),style);
            createCell(row,columnCount++,data.getVinculacion(),style);
            createCell(row,columnCount++,data.getProveedorDePrecios(),style);
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
