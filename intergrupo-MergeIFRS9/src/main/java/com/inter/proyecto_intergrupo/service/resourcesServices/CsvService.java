package com.inter.proyecto_intergrupo.service.resourcesServices;

import com.inter.proyecto_intergrupo.model.dataquality.PointRulesDQ;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CsvService {

    public static void downloadCsv(PrintWriter writer, List<Object[]> taxBase) {
        writer.println("COD NEOCON;DIVISA;YNTP;SOCIEDADES YNTP;CONTRATO;NIT Contraparte;VALOR;COD. PAÍS;PAÍS;CUENTA LOCAL");
        for (Object[] tax : taxBase) {
            writer.println(
                    tax[0].toString() + ";" +
                            tax[1].toString() + ";" +
                            tax[2].toString() + ";" +
                            tax[3].toString() + ";" +
                            tax[4].toString() + ";" +
                            tax[5].toString() + ";" +
                            Double.parseDouble(tax[6].toString()) + ";" +
                            tax[7].toString() + ";" +
                            tax[8].toString() + ";" +
                            tax[9].toString());
        }
    }

    public static void downloadTxtDQ(PrintWriter writer, List<PointRulesDQ> data) {
        SimpleDateFormat formato1= new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formato2= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat formato3= new DecimalFormat("0.00");
        DecimalFormat formato4= new DecimalFormat("0");
        writer.println("FECHA DE CIERRE|FECHA DE EJECUCIÓN REGLA DE CALIDAD|IDENTIFICADOR DEL PAIS DEL DATA SYSTEM MODELO GLOBAL|IDENTIFICADOR UUAA|NOMBRE DEL DATA SYSTEM|DESCRIPCIÓN DESGLOSE REGLA DE CALIDAD|IDENTIFICADOR SECUENCIAL REGLA DE CALIDAD LEGACY|TIPO DE PRINCIPIO REGLA DE CALIDAD MODELO GLOBAL|TIPO DE REGLA DE CALIDAD MODELO GLOBAL|NOMBRE FÍSICO OBJETO|NOMBRE FÍSICO CAMPO|PORCENTAJE CUMPLIMIENTO DE REGLA DE CALIDAD|NUMERADOR DE CUMPLIMIENTO DE REGLA DE CALIDAD|DENOMINADOR DE CUMPLIMIENTO DE REGLA DE CALIDAD|TIPO FRECUENCIA EJECUCION DE REGLA DE CALIDAD MODELO GLOBAL|PORCENTAJE UMBRAL MINIMO DE LA REGLA DE CALIDAD|PORCENTAJE UMBRAL OBJETIVO DE LA REGLA DE CALIDAD|NOMBRE CAMPO IMPORTE REGLA DE CALIDAD|PORCENTAJE CUMPLIMIENTO SALDO EN UNA REGLA DE CALIDAD|IMPORTE NUMERADOR REGLA DE CALIDAD|IMPORTE DENOMINADOR REGLA DE CALIDAD");
        for (PointRulesDQ dq : data) {
            String dataNumber="";
            if(dq.getImporteNumerador()!=0.00 && dq.getImporteDenominador()!=0.00)
            {
                dataNumber=formato3.format(Math.abs(dq.getPorcentajeCumplimientoSaldo()));
            }
            writer.println(
                    String.format("%1$-" + 10 + "s", formato1.format(dq.getFechaCierre())) + "|" +
                    String.format("%1$-" + 26 + "s", formato2.format(dq.getFechaEjecucion()))+ "|" +
                    String.format("%1$-" + 3 + "s", dq.getIdentificadorPais())+ "|" +
                    String.format("%1$-" + 4 + "s", dq.getIdentificadorUuaa())+ "|" +
                    String.format("%1$-" + 40 + "s", dq.getNombreDataSystem())+ "|" +
                    String.format("%1$-" + 255 + "s", dq.getDescripcionDesglose())+ "|" +
                    String.format("%1$-" + 10 + "s", dq.getIdentificadorSecuencialLegacy())+ "|" +
                    String.format("%1$-" + 4 + "s", dq.getTipoPrincipio())+ "|" +
                    String.format("%1$-" + 4 + "s", dq.getTipoRegla())+ "|" +
                    String.format("%1$-" + 40 + "s", dq.getNombreFisicoObjeto())+ "|" +
                    String.format("%1$-" + 40 + "s", dq.getNombreFisicoCampo()).substring(0,40)+ "|" +
                    String.format("%1$-" + 6 + "s", formato3.format(dq.getPorcentajeCumplimiento())).replace(",",".")+ "|" +
                    String.format("%1$-" + 11 + "s", formato4.format(dq.getNumeradorCumplimiento()))+ "|" +
                    String.format("%1$-" + 11 + "s", formato4.format(dq.getDenominadorCumplimiento()))+ "|" +
                    String.format("%1$-" + 10 + "s", dq.getTipoFrecuenciaEjecucion())+ "|" +
                    String.format("%1$-" + 6 + "s", formato3.format(dq.getPorcentajeUmbralMinimo())).replace(",",".")+ "|" +
                    String.format("%1$-" + 6 + "s", formato3.format(dq.getPorcentajeUmbralObjetivo())).replace(",",".")+ "|" +
                    String.format("%1$-" + 40 + "s", dq.getNombreCampoImporte())+ "|" +
                    String.format("%1$-" + 6 + "s", dataNumber.replace(",","."))+ "|" +
                    String.format("%1$-" + 27 + "s", formato3.format(dq.getImporteNumerador())).replace(",",".").replace("0.00","    ")+ "|" +
                    String.format("%1$-" + 27 + "s", formato3.format(dq.getImporteDenominador())).replace(",",".").replace("0.00","    "));
        }
    }
/*
    public static void downloadTxtReportIcrv(PrintWriter writer, List<ReportIcrv> data) {
        SimpleDateFormat formato1= new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formato2= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat formato3= new DecimalFormat("0.00");
        DecimalFormat formato4= new DecimalFormat("00000");
        for (ReportIcrv part : data) {
            if(part.getCosteValor()==null)
                part.setCosteValor(0.0);
            if(part.getAjusteValorRazonable()==null)
                part.setAjusteValorRazonable(0.0);
            if(part.getMicrocoberturas()==null)
                part.setMicrocoberturas(0.0);
            if(part.getCorreccionesPorDeterioro()==null)
                part.setCorreccionesPorDeterioro(0.0);
            if(part.getValorCotizado()==null)
                part.setValorCotizado(0.0);
            if(part.getDesembolsoPdte()==null)
                part.setDesembolsoPdte(0.0);
            if(part.getNumTitulos()==null)
                part.setNumTitulos(0.0);
            if(part.getCapitalSocial()==null)
                part.setCapitalSocial(0.0);
            if(part.getCosteAdquisicion()==null)
                part.setCosteAdquisicion(0.0);
            writer.println(
                String.format("%1$-" + 6 + "s", part.getCodPeriodo()) + "" +
                String.format("%1$-" + 5 + "s", "00548")+ "" +
                String.format("%1$-" + 2 + "s", part.getXtiCartera())+ "" +
                String.format("%1$-" + 5 + "s", part.getCodSocipart())+ "" +
                String.format("%1$-" + 12 + "s", part.getCodIsin()).replace("null","    ")+ "" +
                String.format("%0" + 18 + "d", Long.parseLong(formato4.format(Math.abs(part.getCosteValor()))))+ "" +
                String.format("%0" + 18 + "d", Long.parseLong(formato4.format(Math.abs(part.getAjusteValorRazonable()))))+ "" +
                String.format("%0" + 18 + "d", Long.parseLong(formato4.format(Math.abs(part.getMicrocoberturas()))))+ "" +
                String.format("%0" + 18 + "d", Long.parseLong(formato4.format(Math.abs(part.getCorreccionesPorDeterioro()))))+ "" +
                String.format("%0" + 18 + "d", Long.parseLong(formato4.format(Math.abs(part.getValorCotizado()))))+ "" +
                String.format("%0" + 18 + "d", Long.parseLong(formato4.format(Math.abs(part.getDesembolsoPdte()))))+ "" +
                String.format("%0" + 22 + "d", Long.parseLong(formato4.format(Math.abs(part.getNumTitulos()))))+ "" +
                String.format("%0" + 20 + "d", Long.parseLong(formato4.format(Math.abs(part.getCapitalSocial()))))+ "" +
                String.format("%0" + 20 + "d", Long.parseLong(formato4.format(Math.abs(part.getCosteAdquisicion()))))+ "" +
                String.format("%1$-" + 1 + "s", part.getSignoValorContable())+ "" +
                String.format("%1$-" + 1 + "s", part.getSignoMicrocobertura()));
        }
    }

    public static void downloadMassiveChargeImpuestos(PrintWriter writer, List<Object[]> taxBase,String periodo) {
        writer.println("CENTRO DE COSTO;CUENTA;DIVISA;CONTRATO;REFERENCIA CRUCE;IMPORTE;DESCRIPCION;FECHA;TIPO DE DOCUMENTO;NUMERO DE DOCUMENTO;DIGITO DE VERIFICACION;TIPO DE PERDIDA;CLASE DE RIESGO;TIPO DE MOVIMIENTO;PRODUCTO;PROCESO;LINEA OPERATIVA;VALOR BASE");
        for (Object[] tax : taxBase) {
            String cuentaFinal = "CUENTA ESPAÑA NO ENCONTRADA";
            if(tax[8].toString().equals(tax[22].toString()))
            {
                cuentaFinal=tax[23].toString();
            }
            else if(tax[8].toString().equals(tax[24].toString()))
            {
                cuentaFinal=tax[25].toString();
            }
                writer.println(
                        tax[18].toString() + ";" +
                        cuentaFinal + ";" +
                        tax[11].toString().replace("MLL","COP") + ";" +
                        "" + ";" +
                        tax[5].toString() + ";" +
                        tax[10].toString().replace(",","").replace(".",",") + ";" +
                        tax[3].toString().trim() + ";" +
                        tax[15].toString() + ";" +
                        tax[19].toString() + ";" +
                        tax[20].toString() + ";" +
                        tax[21].toString() + ";" +
                        "" + ";" +
                        "" + ";" +
                        "" + ";" +
                        "" + ";" +
                        "" + ";" +
                        "" + ";0,00" );
        }
    }

    public static void downloadMassiveChargeCalculo(PrintWriter writer, List<Object[]> calculo, String periodo) {
        DecimalFormat formato3= new DecimalFormat("0.00");
        writer.println("CENTRO DE COSTO;CUENTA;DIVISA;CONTRATO;REFERENCIA CRUCE;IMPORTE;DESCRIPCION;FECHA;TIPO DE DOCUMENTO;NUMERO DE DOCUMENTO;DIGITO DE VERIFICACION;TIPO DE PERDIDA;CLASE DE RIESGO;TIPO DE MOVIMIENTO;PRODUCTO;PROCESO;LINEA OPERATIVA;VALOR BASE");
        for (Object[] parte : calculo) {
            writer.println(
                "3008" + ";" +
                parte[0].toString() + ";" +
                "COP" + ";;;" +
                formato3.format(Double.parseDouble(parte[1].toString())).replace(",",".") + ";" +
                parte[2].toString() + ";" +
                parte[3].toString() + ";" +
                "3" + ";" +
                parte[4].toString() + ";" +
                parte[5].toString() + ";;;;;;");
        }
    }


    public static void downloadMassiveCharge(PrintWriter writer, List<Object[]> taxBase) {
        writer.println("CENTRO DE COSTO;CUENTA;DIVISA;CONTRATO;REFERENCIA CRUCE;IMPORTE;DESCRIPCION;FECHA;TIPO DE DOCUMENTO;NUMERO DE DOCUMENTO;DIGITO DE VERIFICACION;TIPO DE PERDIDA;CLASE DE RIESGO;TIPO DE MOVIMIENTO;PRODUCTO;PROCESO;LINEA OPERATIVA;VALOR BASE");
        for (Object[] tax : taxBase) {
            if(!tax[5].toString().replace(",","").replace(".",",").equals("0,00")){
                writer.println(
                    tax[0].toString() + ";" +
                            tax[1].toString() + ";" +
                            tax[2].toString() + ";" +
                            tax[3].toString() + ";" +
                            tax[4].toString() + ";" +
                            tax[5].toString().replace(",","").replace(".",",") + ";" +
                            tax[6].toString() + ";" +
                            tax[7].toString() + ";" +
                            tax[8].toString() + ";" +
                            tax[9].toString() + ";" +
                            tax[10].toString() + ";" +
                            tax[11].toString() + ";" +
                            tax[12].toString() + ";" +
                            tax[13].toString() + ";" +
                            tax[14].toString() + ";" +
                            tax[15].toString() + ";" +
                            tax[16].toString() + ";0,00" );
            }
        }
    }

    public static void downloadTxtNeocon60(PrintWriter writer, List<Neocon60> neocon60List) {
        DecimalFormat formato3= new DecimalFormat("0");
        for (Neocon60 neocon60 : neocon60List)
        {
            writer.println(
                String.format("%1$-" + 4 + "s", neocon60.getAno()) +
                String.format("%1$-" + 2 + "s", neocon60.getMes()) +
                String.format("%1$-" + 5 + "s", neocon60.getYntpReportante()) +
                String.format("%1$-" + 5 + "s", neocon60.getCodicons()) +
                String.format("%1$-" + 3 + "s", neocon60.getDivisa()).replace("null","   ") +
                String.format("%1$-" + 2 + "s", neocon60.getPais()) +
                String.format("%1$-" + 5 + "s", neocon60.getYntpIntergrupo()) +
                String.format("%021.2f",neocon60.getSaldo()).replace(",00","") +
                String.format("%1$-" + 1 + "s", neocon60.getNaturaleza()) +
                String.format("%1$-" + 2 + "s", neocon60.getPaisContraparte()));
        }
    }

    public static void downloadMassiveChargeCC(PrintWriter writer, List<Object[]> taxBase) {
        writer.println("CENTRO DE COSTO;CUENTA;DIVISA;CONTRATO;REFERENCIA CRUCE;IMPORTE;DESCRIPCION;FECHA;TIPO DE DOCUMENTO;NUMERO DE DOCUMENTO;DIGITO DE VERIFICACION;TIPO DE PERDIDA;CLASE DE RIESGO;TIPO DE MOVIMIENTO;PRODUCTO;PROCESO;LINEA OPERATIVA;VALOR BASE");
        for (Object[] tax : taxBase) {
            if(!tax[5].toString().replace(",","").replace(".",",").equals("0,00")){
                writer.println(
                        tax[0].toString() + ";" +
                                tax[1].toString() + ";" +
                                tax[2].toString() + ";" +
                                tax[3].toString() + ";" +
                                tax[4].toString() + ";" +
                                tax[5].toString().replace(",","").replace(".",",") + ";" +
                                tax[6].toString() + ";" +
                                tax[7].toString() + ";" +
                                tax[8].toString() + ";" +
                                tax[9].toString() + ";" +
                                tax[10].toString() + ";" +
                                tax[11].toString() + ";" +
                                tax[12].toString() + ";" +
                                tax[13].toString() + ";" +
                                tax[14].toString() + ";" +
                                tax[15].toString() + ";" +
                                tax[16].toString() + ";" +
                                tax[17].toString().replace(",","").replace(".",","));
            }
        }
    }

    public static void downloadAnexo8(PrintWriter writer, List<Object[]> taxBase) {
        writer.println("CENTRO;CUENTA PUC;DIVISA;IMPORTE;FECHA ORIGEN;FECHA CIERRE PARTIDA;TP;IDENTIFICACION;DV;NOMBRE O RAZON SOCIAL;CONTRATO;OBSERVACION;CUENTA PROVISION;VALOR PROVISION;IMPORTE MONEDA;PROBABILIDAD DE RECUPERACION");
        for (Object[] tax : taxBase) {

            writer.println(
                    tax[0].toString() + ";" +
                            tax[1].toString() + ";" +
                            tax[2].toString() + ";" +
                            tax[3].toString().replace(",","").replace(".",",") + ";" +
                            tax[4].toString() + ";" +
                            tax[5].toString() + ";" +
                            tax[6].toString() + ";" +
                            tax[7].toString() + ";" +
                            tax[8].toString() + ";" +
                            tax[9].toString() + ";" +
                            tax[10].toString() + ";" +
                            tax[11].toString() + ";" +
                            tax[12].toString() + ";" +
                            tax[13].toString() + ";" +
                            tax[14].toString() + ";" +
                            tax[15].toString());
        }
    }

    public static void downloadTaxBase(PrintWriter writer, ArrayList<TaxBaseComplete> taxBaseComplete){
        writer.println("COD NEOCON;DIVISA;YNTP;SOCIEDADES YNTP;CONTRATO;NIT Contraparte;VALOR;COD. PAÍS;PAÍS;CUENTA LOCAL;PERIODO");
        for(TaxBaseComplete tax: taxBaseComplete){
            writer.println(
                    tax.getCodNeocon()+";"+
                            tax.getDivisa()+";"+
                            tax.getYntp()+";"+
                            tax.getSociedadYntp()+";"+
                            tax.getContrato()+";"+
                            tax.getNitContraparte()+";"+
                            tax.getValor()+";"+
                            tax.getCodPais()+";"+
                            tax.getPais()+";"+
                            tax.getCuentaLocal()+";"+
                            tax.getPeriodo());
        }
    }

    public static void downloadManuals(PrintWriter writer, List<Object[]> manualsList){
        writer.println( "CENTRO; DESCRIPCIÓN CENTRO; CUENTA PUC; DESCRIPCIÓN CUENTA PUC; DIVISA; IMPORTE; FECHA ORIGEN; FECHA CIERRE; TP; IDENTIFICACIÓN; DV; NOMBRE; CONTRATO; OBSERVACION; CUENTA PROV; IMPORTE PROV; IMPORTE ORIGINAL; PROBABILIDAD RECUPERACION; ALTURA; FUENTE DE INFORMACIÓN");
        for(Object[] tax: manualsList){
            writer.println(
                    tax[0]+";"+
                            tax[1]+";"+
                            tax[2]+";"+
                            tax[3]+";"+
                            tax[4]+";"+
                            tax[5]+";"+
                            tax[6]+";"+
                            tax[7]+";"+
                            tax[8]+";"+
                            tax[9]+";"+
                            tax[10]+";"+
                            tax[11]+";"+
                            tax[12]+";"+
                            tax[13]+";"+
                            tax[14]+";"+
                            tax[15]+";"+
                            tax[16]+";"+
                            tax[17]+";"+
                            tax[18]+";"+
                            tax[19]);
        }
    }

    public static void downloadDescontabilizacionTemplate(PrintWriter writer, List<Object[]> descontabilizacionTemplateList){
        //writer.println("Centro|Cuenta|Divisa|Contrato|Concepto|Saldo");
        for(Object[] descontabilizacion: descontabilizacionTemplateList){
            writer.println(
                    String.format("%" + 4 + "s", descontabilizacion[0])+"|"+
                            descontabilizacion[1]+"|"+
                            descontabilizacion[2]+"|"+
                            descontabilizacion[3]+"|"+
                            descontabilizacion[4]+"|"+
                            String.format("%.2f",Double.parseDouble(descontabilizacion[5].toString())).replace(",",".")
            );
        }
    }

    public static void downloadCsvCargaMasiva(PrintWriter writer, ArrayList<String> rows) {
        rows.forEach(row -> writer.println(row));
    }

    public static void downloadCsvPlanoRecla(PrintWriter writer, ArrayList<String> rows) {
        rows.forEach(row -> writer.println(row));
    }

    public static void downloadTxtMarcacion(PrintWriter writer, List<MarcacionConcil> rows) {
        writer.println("APLICATIVO;CENTRO;CUENTA;DIFERENCIA;DIVISA;EMPRESA;FECHA;SALDO_APLICATIVO;SALDO_CONTABLE");
        for (MarcacionConcil row : rows) {
            writer.println(
                row.getAplicativo() + ";" +
                row.getCentro() + ";" +
                row.getCuenta() + ";" +
                BigDecimal.valueOf(row.getDiferencia()) + ";" +
                row.getDivisa() + ";" +
                row.getEmpresa() + ";" +
                row.getFecha() + ";" +
                BigDecimal.valueOf(row.getSaldoAplicativo()) + ";" +
                BigDecimal.valueOf(row.getSaldoContable()));
        }
    }

    public static void downloadCsvRiesgos(PrintWriter writer, List<Object[]> riesgos, boolean reversion, String fecha) throws ParseException {
        DecimalFormat df = new DecimalFormat("#.00");
        writer.println("CENTRO DE COSTO;CUENTA;DIVISA;CONTRATO;REFERENCIA DE CRUCE;IMPORTE; DESCRIPCION ;FECHA;TIPO DE DOCUMENTO;NUMERO DE DOCUMENTO;DIGITO DE VERIFICACION;TIPO DE PERDIDA;CLASE DE RIESGO;TIPO DE MOVIMIENTO;PRODUCTO;PROCESO;LINEA OPERATIVA;VALOR BASE");
        for (Object[] risk : riesgos) {
            String number = "";
            String numberDeb = "";
            number=df.format(Double.parseDouble(risk[5].toString())*-1);
            numberDeb=df.format(Double.parseDouble(risk[5].toString()));

            if(number.equals(",00"))
            {
                number="0,00";
            }
            if(numberDeb.equals("-,00"))
            {
                numberDeb="0,00";
            }
            writer.println(
                    risk[0].toString() + ";" +
                    risk[1].toString() + ";" +
                    risk[2].toString() + ";" +
                    ";" +
                    risk[3].toString() + ";" +
                    number.replace(".",",") + ";" +
                    risk[10].toString()+" "+ fecha +";" +
                    risk[6].toString() + ";" +
                    risk[7].toString().replace("0","") + ";" +
                    risk[8].toString() + ";" +
                    risk[9].toString()+ ";" +
                    ";" +
                    ";" +
                    ";" +
                    ";" +
                    ";" +
                    ";0,00");
            writer.println(
                    risk[0].toString() + ";" +
                    risk[4].toString() + ";" +
                    risk[2].toString() + ";" +
                    ";" +
                    risk[3].toString() + ";" +
                    numberDeb.replace(".",",") + ";" +
                    risk[10].toString()+" "+ fecha +";" +
                    risk[6].toString() + ";" +
                    risk[7].toString().replace("0","") + ";" +
                    risk[8].toString() + ";" +
                    risk[9].toString()+ ";" +
                    ";" +
                    ";" +
                    ";" +
                    ";" +
                    ";" +
                    ";0,00");
        }
    }


    public static void downloadAnexoCsv(PrintWriter writer, List<Anexo> anexos,String separador) throws ParseException {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat df = new DecimalFormat("#.00");
        for (Anexo anexo : anexos) {
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
    }

    public static void downloadRiesgosCsv(PrintWriter writer, List<RiskAccountFinal> riesgos) throws ParseException {
        DecimalFormat df = new DecimalFormat("#.00");
        for (RiskAccountFinal riesgo : riesgos) {
            String number = "";
            String stage = "";
            String segmento = "";
            if(riesgo.getValida().contains("cambia provisión"))
            {

                number = df.format(riesgo.getProvision());

                if(number.substring(0,1).equals(","))
                {
                    number="0"+number;
                }

            }
            if(riesgo.getValida().contains("cambia stage"))
                stage = riesgo.getStage();
            if(riesgo.getValida().contains("cambia segmento"))
                segmento = riesgo.getSegmento();
            writer.println(
                    String.format("%1$-" + 42 + "s", riesgo.getContrato()) + "" +
                    String.format("%1$-" + 1 + "s", stage) + "" +
                    String.format("%1$-" + 3 + "s", segmento) + "" +
                    String.format("%1$-" + 20 + "s", number.replace(",",".")));
        }
    }

    public static void downloadCsvCreationAccount(PrintWriter writer, List<AccountCreationPlane> cuentas) throws ParseException {
        for (AccountCreationPlane cuenta : cuentas)
        {
            if(cuenta.getINDICLI().equals("I")) {
                writer.println(
                        String.format("%1$-" + 4 + "s", cuenta.getEMPRESA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 15 + "s", cuenta.getNUMEROCUENTA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 65 + "s", cuenta.getNOMBRECUENTA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 35 + "s", cuenta.getNOMBRECORTOCUENTA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", "0008") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTIPOCTA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", "") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getINDICLI()).replace("null", "   ") + "" +
                                String.format("%1$-" + 15 + "s", cuenta.getCONTRAPARTIDADERESULTADOSDH()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getINDICADORDELACUENTA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 15 + "s", cuenta.getCONTRAPARTIDADERESULTADOSDH()).replace("null", "   ") + "" +
                                String.format("%1$-" + 15 + "s", cuenta.getCONTRAPARTIDADEORDEN()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getCLAVEACCESO()).replace("null", "   ") + "" +//no se puede pedir a los literales
                                String.format("%1$-" + 1 + "s", "") + "" +
                                String.format("%1$-" + 1 + "s", "") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getMON()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getINDICADORPROCESODEBAJA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getINDCUENTAINVENTARIABLE()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getINDCUENTAOPERACIONAL()).replace("null", "   ") + "" +
                                //String.format("%1$-" + 1 + "s", cuenta.getMON()).replace("null","   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICTOO1()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICTOO2()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICTOO3()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICTOO4()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICTOO5()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICENAO()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUO01()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUO02()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUO03()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUO04()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUO05()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUO06()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUO07()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUO08()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUO09()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICTOD1()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICTOD2()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICTOD3()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICTOD4()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICTOD5()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTICENAD()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUD01()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUD02()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUD03()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUD04()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUD05()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUD06()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUD07()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUD08()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", cuenta.getCENAUD09()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getCODIGOGESTION()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getCONSOLID()).replace("null", "   ") + "" +
                                String.format("%1$-" + 2 + "s", cuenta.getCODIGODECONTROL()).replace("null", "   ") + "" +
                                String.format("%1$-" + 3 + "s", cuenta.getDIASDEPLAZO()).replace("null", "   ").replace(" ", "0") + "" +
                                String.format("%1$-" + 9 + "s", "") + "" +//plano de cuenta espacios y ristras -
                                String.format("%1$-" + 10 + "s", cuenta.getNUMEROCUENTA().substring(0, 7) + "001").replace("null", "   ") + "" +
                                String.format("%1$-" + 2 + "s", cuenta.getRESPONSABLECONTROLOPERATIVONIVEL1()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLOPERATIVOCENOPERES1()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLOPERATIVOCENOPERES2()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLOPERATIVOCENOPERES3()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLOPERATIVOCENOPERES4()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLOPERATIVOCENOPERES5()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLOPERATIVOCENOPERES6()).replace("null", "   ") + "" +
                                String.format("%1$-" + 2 + "s", cuenta.getRESPONSABLECONTROLDEGESTIONNIVEL2()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLDEGESTIONCENGESRES1()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLDEGESTIONCENGESRES2()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLDEGESTIONCENGESRES3()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLDEGESTIONCENGESRES4()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLDEGESTIONCENGESRES5()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLDEGESTIONCENGESRES6()).replace("null", "   ") + "" +
                                String.format("%1$-" + 2 + "s", cuenta.getRESPONSABLECONTROLADMINISTRATIVONIVEL3()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES1()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES2()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES3()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES4()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES5()).replace("null", "   ") + "" +
                                String.format("%1$-" + 5 + "s", cuenta.getRESPONSABLECONTROLADMINISTRATIVOCENADMRES6()).replace("null", "   ") + "" +
                                String.format("%1$-" + 9 + "s", "00" + cuenta.getEPIGRAFE()).replace("null", "   "));
            }
            else{
                writer.println(
                        String.format("%1$-" + 4 + "s", cuenta.getEMPRESA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 15 + "s", cuenta.getNUMEROCUENTA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 65 + "s", cuenta.getNOMBRECUENTA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 35 + "s", cuenta.getNOMBRECORTOCUENTA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 4 + "s", "0008") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getTIPOCTA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", "") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getINDICLI()).replace("null", "   ") + "" +
                                String.format("%1$-" + 15 + "s", cuenta.getCONTRAPARTIDADERESULTADOSDH()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getINDICADORDELACUENTA()).replace("null", "   ") + "" +
                                String.format("%1$-" + 15 + "s", cuenta.getCONTRAPARTIDADERESULTADOSDH()).replace("null", "   ") + "" +
                                String.format("%1$-" + 15 + "s", cuenta.getCONTRAPARTIDADEORDEN()).replace("null", "   ") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getCLAVEACCESO()).replace("null", "   ") + "" +//no se puede pedir a los literales
                                String.format("%1$-" + 1 + "s", "") + "" +
                                String.format("%1$-" + 1 + "s", "") + "" +
                                String.format("%1$-" + 1 + "s", cuenta.getMON()).replace("null", "   "));
            }
        }
    }

    public static void downloadCsvPlaneRistras(PrintWriter writer, List<PlaneRistras> cuentas) throws ParseException {
        for (PlaneRistras cuenta : cuentas)
        {
            writer.println(
                    String.format("%1$-" + 4 + "s", cuenta.getBanco()).replace("null","   ") + "" +
                            String.format("%1$-" + 3 + "s", cuenta.getInterfaz()).replace("null","   ") + "" +
                            String.format("%1$-" + 15 + "s", cuenta.getCuentaDefinitiva()).replace("null","   ") + "" +
                            String.format("%1$-" + 2 + "s", cuenta.getProducto()).replace("null","   ") + "" +
                            String.format("%1$-" + 4 + "s", cuenta.getTipoDeCartera()).replace("null","   ") + "" +
                            String.format("%1$-" + 3 + "s", cuenta.getCampo12()).replace("null","   ") + "" +
                            String.format("%1$-" + 1 + "s", cuenta.getCalificacion()).replace("null","   ") + "" +
                            String.format("%1$-" + 3 + "s", cuenta.getCampo14()).replace("null","   ") + "" +
                            String.format("%1$-" + 1 + "s", cuenta.getCodigoSector()).replace("null","   ") + "" +
                            String.format("%1$-" + 2 + "s", cuenta.getCodigoSubsector()).replace("null","   ") + "" +
                            String.format("%1$-" + 5 + "s", cuenta.getFormaDePago()).replace("null","   ") + "" +
                            String.format("%1$-" + 4 + "s", cuenta.getLineaDeCredito()).replace("null","   ") + "" +
                            String.format("%1$-" + 2 + "s", cuenta.getEntidRedescuento()).replace("null","   ") + "" +
                            String.format("%1$-" + 1 + "s", cuenta.getMorosidad()).replace("null","   ") + "" +
                            String.format("%1$-" + 1 + "s", cuenta.getTipoInversion()).replace("null","   ") + "" +
                            String.format("%1$-" + 3 + "s", cuenta.getTipoDeGasto()).replace("null","   ") + "" +
                            String.format("%1$-" + 5 + "s", cuenta.getConceptoContable()).replace("null","   ") + "" +
                            String.format("%1$-" + 3 + "s", cuenta.getDivisa()).replace("null","   ") + "" +
                            String.format("%1$-" + 1 + "s", cuenta.getTipoMoneda()).replace("null","   ") + "" +
                            String.format("%1$-" + 10 + "s", cuenta.getFiller()).replace("null","   ") + "" +
                            String.format("%1$-" + 18 + "s", cuenta.getVarios()).replace("null","   ") + "" +
                            String.format("%1$-" + 15 + "s", cuenta.getValor()).replace("null","   ") + "" +
                            String.format("%1$-" + 9 + "s", cuenta.getSagrupas()).replace("null","   "));
        }
    }

    public static void downloadMasterContract(PrintWriter writer, List<Object[]> cuentas){
        writer.println("EMPRESA;INSTRUMENTO;CONTRATO;STAGE;V SALDO LOCAL;V IMPORTE PROVISION;V IMPORTE EAD;M SALDO LOCAL;M IMPORTE PROVISION;M IMPORTE EAD;DIF SALDO LOCAL;DIF IMPORTE PROVISION;DIF IMPORTE EAD;");
        //String.format("%.2f",Double.parseDouble(descontabilizacion[5].toString())).replace(",",".")
        //String.format("%" + 4 + "s", descontabilizacion[0])
        for(Object[] cuenta: cuentas){
            writer.println(
                String.format("%" + 4 + "s", cuenta[1]) + ";" +
                String.format("%" + 4 + "s", cuenta[2]) + ";" +
                String.format("%" + 4 + "s", cuenta[3]) + ";" +
                String.format("%" + 4 + "s", cuenta[4]) + ";" +
                String.format("%.2f",Double.parseDouble(cuenta[5].toString())).replace(",",".") + ";" +
                String.format("%.2f",Double.parseDouble(cuenta[6].toString())).replace(",",".") + ";" +
                String.format("%.2f",Double.parseDouble(cuenta[7].toString())).replace(",",".") + ";" +
                String.format("%.2f",Double.parseDouble(cuenta[8].toString())).replace(",",".") + ";" +
                String.format("%.2f",Double.parseDouble(cuenta[9].toString())).replace(",",".") + ";" +
                String.format("%.2f",Double.parseDouble(cuenta[10].toString())).replace(",",".") + ";" +
                String.format("%.2f",Double.parseDouble(cuenta[11].toString())).replace(",",".") + ";" +
                String.format("%.2f",Double.parseDouble(cuenta[12].toString())).replace(",",".") + ";" +
                String.format("%.2f",Double.parseDouble(cuenta[13].toString())).replace(",",".") + ";"
            );
        }

    }*/


}