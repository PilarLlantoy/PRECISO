package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.ConcilFiliales;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.ConcilFilialesService;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class BotonConcilFilialesController {

    @Autowired
    ConcilFilialesService concilFilialesService;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/eeffConsolidated/concilFiliales")
    public ModelAndView concilFilialesEeff(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/ConcilFilialesEEFF");
        String todayString = "";
        System.out.println(params.get("period").toString());
        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);

            if (calendar.get(Calendar.MONTH) == 0) {
                calendar.add(Calendar.YEAR, -1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            } else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));

            }
        } else {
            todayString = params.get("period").toString();
        }
        try {

            modelAndView.addObject("resp", "correct");

        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "error");
        }

        concilFilialesService.concilFilialesEeff(todayString);

        modelAndView.addObject("period", todayString);
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/exportar", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportAllConciliacionFiliales(@RequestParam String period) {
        try {
            concilFilialesService.updateColumns(period);
            List<ConcilFiliales> datosFiltrados = concilFilialesService.getEeffConsolidatedDataByPeriod(period);
            List<Object[]> datosFiltrados1 = concilFilialesService.getDetalleEliminaciones(period);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet1 = workbook.createSheet("Consolidacion");

            Sheet sheet = workbook.createSheet("Eliminaciones");

            Row headerRow = sheet.createRow(0);
            CellStyle style = workbook.createCellStyle();
            style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
            int contador = 0;

            headerRow.createCell(contador++).setCellValue("L4");
            headerRow.createCell(contador++).setCellValue("L9");
            headerRow.createCell(contador++).setCellValue("Entidad");
            headerRow.createCell(contador++).setCellValue("Cuenta");
            headerRow.createCell(contador++).setCellValue("Moneda");
            headerRow.createCell(contador++).setCellValue("Debe");
            headerRow.createCell(contador++).setCellValue("Haber");
            headerRow.createCell(contador++).setCellValue("Saldo");
            headerRow.createCell(contador++).setCellValue("Concepto");

            int rowNum = 1;
            for (Object[] fila : datosFiltrados1) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(fila[0].toString());
                row.createCell(1).setCellValue(fila[1].toString());
                row.createCell(2).setCellValue(fila[2].toString());
                row.createCell(3).setCellValue(fila[3].toString());
                row.createCell(4).setCellValue(fila[4].toString());
                if (fila[5] != null) {
                    row.createCell(5).setCellValue(Double.parseDouble(fila[5].toString()));
                    row.getCell(5).setCellStyle(style);
                }
                if (fila[6] != null) {
                    row.createCell(6).setCellValue(Math.abs(Double.parseDouble(fila[6].toString())));
                    row.getCell(6).setCellStyle(style);
                }
                if (fila[7] != null) {
                    row.createCell(7).setCellValue(Double.parseDouble(fila[7].toString()));
                    row.getCell(7).setCellStyle(style);
                }
                row.createCell(8).setCellValue(fila[8].toString());
            }

            headerRow = sheet1.createRow(0);
            contador = 0;

            headerRow.createCell(contador++).setCellValue("L1");
            headerRow.createCell(contador++).setCellValue("L2");
            headerRow.createCell(contador++).setCellValue("L4");
            headerRow.createCell(contador++).setCellValue("L6");
            headerRow.createCell(contador++).setCellValue("L9");
            headerRow.createCell(contador++).setCellValue("Cuenta");
            headerRow.createCell(contador++).setCellValue("Codicons");
            headerRow.createCell(contador++).setCellValue("Nombre de la Cuenta");
            headerRow.createCell(contador++).setCellValue("Moneda");
            headerRow.createCell(contador++).setCellValue("Banco");
            headerRow.createCell(contador++).setCellValue("Fiduciaria");
            headerRow.createCell(contador++).setCellValue("Valores");
            headerRow.createCell(contador++).setCellValue("Total");
            headerRow.createCell(contador++).setCellValue("Debe_IFRS");
            headerRow.createCell(contador++).setCellValue("Haber_IFRS");
            headerRow.createCell(contador++).setCellValue("Total IFRS");
            headerRow.createCell(contador++).setCellValue("Debe");
            headerRow.createCell(contador++).setCellValue("Haber");
            headerRow.createCell(contador++).setCellValue("Total Consolidado");
            headerRow.createCell(contador++).setCellValue("Periodo");

            rowNum = 1;
            for (ConcilFiliales fila : datosFiltrados) {
                Row row = sheet1.createRow(rowNum++);
                row.createCell(0).setCellValue(fila.getL_1());
                row.createCell(1).setCellValue(fila.getL_2());
                row.createCell(2).setCellValue(fila.getL_4());
                row.createCell(3).setCellValue(fila.getL_6());
                row.createCell(4).setCellValue(fila.getL_9());
                row.createCell(5).setCellValue(fila.getCuenta());
                row.createCell(6).setCellValue(fila.getCodicons());
                row.createCell(7).setCellValue(fila.getNombreCuenta().toUpperCase());
                row.createCell(8).setCellValue(fila.getMoneda());
                row.createCell(9).setCellValue(fila.getBanco());
                row.getCell(9).setCellStyle(style);
                row.createCell(10).setCellValue(fila.getFiduciaria());
                row.getCell(10).setCellStyle(style);
                row.createCell(11).setCellValue(fila.getValores());
                row.getCell(11).setCellStyle(style);

                row.createCell(12).setCellValue(fila.getTotal());
                row.getCell(12).setCellStyle(style);

                if (fila.getDebeAjustesMayores() != null) {
                    row.createCell(13).setCellValue(fila.getDebeAjustesMayores());
                    row.getCell(13).setCellStyle(style);
                }
                else {
                    row.createCell(13).setCellValue(0);
                    row.getCell(13).setCellStyle(style);
                }
                if (fila.getHaberAjustesMayores() != null) {
                    row.createCell(14).setCellValue(Math.abs(fila.getHaberAjustesMayores()));
                    row.getCell(14).setCellStyle(style);
                }
                else {
                    row.createCell(14).setCellValue(0);
                    row.getCell(14).setCellStyle(style);
                }
                if (fila.getTotalIfrs2() != null) {
                    row.createCell(15).setCellValue(fila.getTotalIfrs2());
                    row.getCell(15).setCellStyle(style);
                }

                if (fila.getDebeTotal() != null) {
                    row.createCell(16).setCellValue(fila.getDebeTotal());
                    row.getCell(16).setCellStyle(style);
                }
                if (fila.getHaberTotal() != null) {
                    row.createCell(17).setCellValue(Math.abs(fila.getHaberTotal()));
                    row.getCell(17).setCellStyle(style);
                }
                if (fila.getTotalConsol2() != null) {
                    row.createCell(18).setCellValue(fila.getTotalConsol2());
                    row.getCell(18).setCellStyle(style);
                }
                row.createCell(19).setCellValue(fila.getPeriodo());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "Consolidacion_EEFF_" + period + ".xlsx";
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/eeffConsolidated/exportarAdd", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportAllConciliacionFilialesAdd(@RequestParam Map<String, Object> params) {
        try {
            String period =params.get("period").toString();
            String level ="";

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet2 = workbook.createSheet("Consolidación_2");

            CellStyle style = workbook.createCellStyle();
            style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
            int contador = 0;
            Row headerRow = sheet2.createRow(0);

            if (params.get("level").toString().equals("l_9")) {
                headerRow.createCell(contador++).setCellValue("L1");
                headerRow.createCell(contador++).setCellValue("L2");
                headerRow.createCell(contador++).setCellValue("L4");
                headerRow.createCell(contador++).setCellValue("L6");
                headerRow.createCell(contador++).setCellValue("L9");
                level="l_1,l_2,l_4,l_6,l_9";
            }
            else if (params.get("level").toString().equals("l_6")) {
                headerRow.createCell(contador++).setCellValue("L1");
                headerRow.createCell(contador++).setCellValue("L2");
                headerRow.createCell(contador++).setCellValue("L4");
                headerRow.createCell(contador++).setCellValue("L6");
                level="l_1,l_2,l_4,l_6";
            }
            else if (params.get("level").toString().equals("l_4")) {
                headerRow.createCell(contador++).setCellValue("L1");
                headerRow.createCell(contador++).setCellValue("L2");
                headerRow.createCell(contador++).setCellValue("L4");
                level="l_1,l_2,l_4";
            }
            else if (params.get("level").toString().equals("l_2")) {
                headerRow.createCell(contador++).setCellValue("L1");
                headerRow.createCell(contador++).setCellValue("L2");
                level="l_1,l_2";
            }
            else if (params.get("level").toString().equals("l_1")) {
                headerRow.createCell(contador++).setCellValue("L1");
                level="l_1";
            }

            //headerRow.createCell(contador++).setCellValue("Codicons");
            headerRow.createCell(contador++).setCellValue("Nombre de la Cuenta");
            headerRow.createCell(contador++).setCellValue("Moneda");
            headerRow.createCell(contador++).setCellValue("Banco");
            headerRow.createCell(contador++).setCellValue("Fiduciaria");
            headerRow.createCell(contador++).setCellValue("Valores");
            headerRow.createCell(contador++).setCellValue("Total");
            headerRow.createCell(contador++).setCellValue("Debe_IFRS");
            headerRow.createCell(contador++).setCellValue("Haber_IFRS");
            headerRow.createCell(contador++).setCellValue("Total IFRS");
            headerRow.createCell(contador++).setCellValue("Debe");
            headerRow.createCell(contador++).setCellValue("Haber");
            headerRow.createCell(contador++).setCellValue("Total Consol");
            headerRow.createCell(contador++).setCellValue("Periodo");

            concilFilialesService.updateColumns(period);
            List<Object[]> datosFiltrados2 = concilFilialesService.getEeffConsolidatedDataByPeriod2(period,level,params.get("level").toString());

            int rowNum = 1;
            for (Object[] fila : datosFiltrados2) {

                contador = 0;
                Row row = sheet2.createRow(rowNum++);

                if (params.get("level").toString().equals("l_9")) {
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                }
                else if (params.get("level").toString().equals("l_6")) {
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                }
                else if (params.get("level").toString().equals("l_4")) {
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                }
                else if (params.get("level").toString().equals("l_2")) {
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                }
                else if (params.get("level").toString().equals("l_1")) {
                    row.createCell(contador).setCellValue(fila[contador++].toString());
                }

                //row.createCell(contador).setCellValue(fila[contador++].toString());
                row.createCell(contador).setCellValue(fila[contador++].toString());
                row.createCell(contador).setCellValue(fila[contador++].toString());

                row.createCell(contador).setCellValue(Double.parseDouble(fila[contador].toString()));
                row.getCell(contador++).setCellStyle(style);

                row.createCell(contador).setCellValue(Double.parseDouble(fila[contador].toString()));
                row.getCell(contador++).setCellStyle(style);

                row.createCell(contador).setCellValue(Double.parseDouble(fila[contador].toString()));
                row.getCell(contador++).setCellStyle(style);

                row.createCell(contador).setCellValue(Double.parseDouble(fila[contador].toString()));
                row.getCell(contador++).setCellStyle(style);

                row.createCell(contador).setCellValue(Double.parseDouble(fila[contador].toString()));
                row.getCell(contador++).setCellStyle(style);

                row.createCell(contador).setCellValue(Math.abs(Double.parseDouble(fila[contador].toString())));
                row.getCell(contador++).setCellStyle(style);

                row.createCell(contador).setCellValue(Double.parseDouble(fila[contador].toString()));
                row.getCell(contador++).setCellStyle(style);

                row.createCell(contador).setCellValue(Double.parseDouble(fila[contador].toString()));
                row.getCell(contador++).setCellStyle(style);

                row.createCell(contador).setCellValue(Math.abs(Double.parseDouble(fila[contador].toString())));
                row.getCell(contador++).setCellStyle(style);

                row.createCell(contador).setCellValue(Double.parseDouble(fila[contador].toString()));
                row.getCell(contador++).setCellStyle(style);

                row.createCell(contador).setCellValue(fila[contador++].toString());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "Consolidacion_EEFF_" + period + ".xlsx";
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/eeffConsolidated/exportarAdicional", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportAllConciliacionFilialesAdicional(@RequestParam String period) {
        try {
            List<Object[]> datosMayoriza = concilFilialesService.getMayorizaAccion(period);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet1 = workbook.createSheet("Mayoriza Axion");

            CellStyle style = workbook.createCellStyle();
            style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
            int contador = 0;

            Row headerRow1 = sheet1.createRow(0);
            contador = 0;

            headerRow1.createCell(contador++).setCellValue("Cuenta");
            headerRow1.createCell(contador++).setCellValue("Descripción");
            headerRow1.createCell(contador++).setCellValue("Banco");
            headerRow1.createCell(contador++).setCellValue("Fiduciaria");
            headerRow1.createCell(contador++).setCellValue("Valores");
            headerRow1.createCell(contador++).setCellValue("Eliminaciones");
            headerRow1.createCell(contador++).setCellValue("Moneda Legal");
            headerRow1.createCell(contador++).setCellValue("Moneda Extranjera");
            headerRow1.createCell(contador++).setCellValue("Total Moneda");
            headerRow1.createCell(contador++).setCellValue("Nivel");

            int rowNum = 1;
            for (Object[] fila : datosMayoriza) {

                Row row = sheet1.createRow(rowNum++);

                row.createCell(0).setCellValue(fila[0].toString());
                row.createCell(1).setCellValue(fila[1].toString());
                row.createCell(2).setCellValue(Double.parseDouble(fila[2].toString()));
                row.getCell(2).setCellStyle(style);
                row.createCell(3).setCellValue(Double.parseDouble(fila[3].toString()));
                row.getCell(3).setCellStyle(style);
                row.createCell(4).setCellValue(Double.parseDouble(fila[4].toString()));
                row.getCell(4).setCellStyle(style);
                row.createCell(5).setCellValue(Double.parseDouble(fila[5].toString()));
                row.getCell(5).setCellStyle(style);
                row.createCell(6).setCellValue(Double.parseDouble(fila[6].toString()));
                row.getCell(6).setCellStyle(style);
                row.createCell(7).setCellValue(Double.parseDouble(fila[7].toString()));
                row.getCell(7).setCellStyle(style);
                row.createCell(8).setCellValue(Double.parseDouble(fila[8].toString()));
                row.getCell(8).setCellStyle(style);
                row.createCell(9).setCellValue(fila[9].toString());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "Consolidacion_EEFF_ADICIONAL" + period + ".xlsx";
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


