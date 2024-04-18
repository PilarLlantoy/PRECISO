package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.DatesLoadEeFF;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionAjustada;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionInicialCuadreGeneral;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionInicialDetalle;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.TemplateBankService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.*;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class VistaIntergrupoEliminacionesDetalleController {

    private static final int PAGINATIONCOUNT = 12;

    @Autowired
    private EliminacionesInicialService eliminacionesInicialService;

    @Autowired
    private EliminacionesAjustadaService eliminacionesAjustadaService;

    @Autowired
    private UserService userService;

    @Autowired
    private statusInfoRepository statusInfoRepository;

    @GetMapping(value = "/eeffConsolidated/IntergrupoEliminacionesDetalle")
    public ModelAndView showEliminacionDetalle(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        if (userService.validateEndpoint(user.getId(), "Ver EEFF Intergrupo Eliminaciones Detalle")) {

            String todayString = "";
            if (params.get("period") == null || params.get("period").toString() == "") {
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

            List<EliminacionesVersionInicialDetalle> list = new ArrayList<>();
            List<EliminacionesVersionAjustada> list1 = new ArrayList<>();
            if (params.get("order") != null && params.get("order").toString() == "") {
                modelAndView.addObject("order", "Versión Inicial");
            } else {
                if (params.get("order") != null && params.get("order").toString().equals("Versión Inicial")) {
                   list = eliminacionesInicialService.getCuadreDetalle(todayString);
                } else if (params.get("order") != null && params.get("order").toString().equals("Versión Ajustada")) {
                   list1 = eliminacionesAjustadaService.getCuadreEliminacionDetalle(todayString);
                }
                if (params.get("order") == null) {
                    modelAndView.addObject("order", "Versión Ajustada");
                } else {
                    modelAndView.addObject("order", params.get("order").toString());
                }
            }

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest2 = PageRequest.of(page, PAGINATIONCOUNT);
            Page<EliminacionesVersionInicialDetalle> pageType = eliminacionesInicialService.getAllDetalle(pageRequest2,todayString,list);

            int totalPage = pageType.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("directory", "IntergrupoEliminacionesDetalle");
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("registers",pageType.getTotalElements());
            modelAndView.addObject("listaDeDatosEliminaciones", pageType.getContent());

            StatusInfo registro = statusInfoRepository.findByInputAndPeriodo("ELIMINACIONES CARGUE CONFIRMADO", todayString);
            String estadoDelRegistro = registro != null ? registro.getStatus() : "NULL";
            modelAndView.addObject("estadoDelRegistro", estadoDelRegistro);

            StatusInfo registro1 = statusInfoRepository.findByInputAndPeriodo("ELIMINACIONES AUTORIZACION", todayString);
            String estadoDelRegistro1 = registro1 != null ? registro.getStatus() : "NULL";
            modelAndView.addObject("estadoDelRegistro1", estadoDelRegistro1);


            int page1 = params.get("page1") != null ? (Integer.valueOf(params.get("page1").toString()) - 1) : 0;
            PageRequest pageRequest1 = PageRequest.of(page1, PAGINATIONCOUNT);
            Page<EliminacionesVersionAjustada> pageType1 = eliminacionesAjustadaService.getAllEliminacionDetalle(pageRequest1,todayString,list1);
            int totalPage1 = pageType1.getTotalPages();
            if (totalPage1 > 0) {
                List<Integer> pages1 = IntStream.rangeClosed(1, totalPage1).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages1", pages1);
            }
            modelAndView.addObject("current1", page1 + 1);
            modelAndView.addObject("next1", page1 + 2);
            modelAndView.addObject("prev1", page1);
            modelAndView.addObject("last1", totalPage1);
            modelAndView.addObject("registers1",pageType1.getTotalElements());
            modelAndView.addObject("listaDeDatosEliminacionesAjustada", pageType1.getContent());

        } else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }

        return modelAndView;
    }

    @GetMapping("/eeffConsolidated/downloadEliminacionDetalle")
    public ResponseEntity<Resource> exportToExcelReportEliminacionDetalle(@RequestParam Map<String, Object> params) {
        String periodo = params.get("period").toString();
        List<EliminacionesVersionInicialDetalle> listaDatos = eliminacionesInicialService.getCuadreDetalle(periodo);
        byte[] excelBytes = generarArchivoExcel(listaDatos);
        ByteArrayResource resource = new ByteArrayResource(excelBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Eliminacion_Detalle" + periodo + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelBytes.length)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    private byte[] generarArchivoExcel(List<EliminacionesVersionInicialDetalle> datos) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Datos");
            Row headerRow = sheet.createRow(0);

            String[] headers = {"Id", "Nombre", "Concepto", "Cuenta Local", "Valor", "Yntp", "L", "ABS", "Nat"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            for (int rowNum = 1; rowNum <= datos.size(); rowNum++) {
                Row row = sheet.createRow(rowNum);
                EliminacionesVersionInicialDetalle data = datos.get(rowNum - 1);

                CellStyle style = workbook.createCellStyle();
                style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));


                row.createCell(0).setCellValue(data.getId());
                row.createCell(1).setCellValue(data.getNombre());
                row.createCell(2).setCellValue(data.getConcepto());
                row.createCell(3).setCellValue(data.getCuentaLocal());

                row.createCell(4).setCellValue(data.getValor());
                row.getCell(4).setCellStyle(style);

                row.createCell(5).setCellValue(data.getYntp());
                row.createCell(6).setCellValue(data.getL());

                row.createCell(7).setCellValue(data.getAbs());
                row.getCell(7).setCellStyle(style);

                row.createCell(8).setCellValue(data.getNat());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] excelBytes = outputStream.toByteArray();

            workbook.close();
            return excelBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    @PostMapping(value = "/eeffConsolidated/CargarEliminacionAjustada")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/IntergrupoEliminacionesDetalle");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Log_Cargue_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();

            ArrayList<String[]> list = eliminacionesAjustadaService.saveFileBD(fileContent, params.get("period").toString());
            String[] part = list.get(0);

            if (part[2].equals("SUCCESS")) {

                eliminacionesAjustadaService.loadAudit(user, "Cargue exitoso Eliminaciones");
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                modelAndView.addObject("order","Versión Ajustada");
            } else {
                eliminacionesAjustadaService.loadAudit(user, "Cargue fallido Eliminaciones");
                modelAndView.addObject("resp", "error--1");
                SignatureListReport signatureListReport = new SignatureListReport(list,null);
                signatureListReport.exportLog(response);
            }
            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("vFilter", params.get("period").toString());
            modelAndView.addObject("order","Versión Ajustada");
        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return modelAndView;
    }

    @GetMapping("/eeffConsolidated/downloadEliminacionversionAjustadaDetalle")
    public ResponseEntity<Resource> exportToExcelReportEliminacionversion_ajustada_detallee(@RequestParam Map<String, Object> params) {
        String periodo = params.get("period").toString();
        List<EliminacionesVersionAjustada> listaDatos = eliminacionesAjustadaService.getCuadreEliminacionDetalle(periodo);
        byte[] excelBytes = generarArchivoExcelDetalleAjustada(listaDatos);
        ByteArrayResource resource = new ByteArrayResource(excelBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Eliminacion_Detalle_Version_Ajustada" + periodo + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelBytes.length)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    private byte[] generarArchivoExcelDetalleAjustada(List<EliminacionesVersionAjustada> datos) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Datos");
            Row headerRow = sheet.createRow(0);

            String[] headers = {"Id", "Nombre", "Concepto", "Cuenta Local", "Valor", "Yntp", "L", "ABS", "Nat"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            for (int rowNum = 1; rowNum <= datos.size(); rowNum++) {
                Row row = sheet.createRow(rowNum);
                EliminacionesVersionAjustada data = datos.get(rowNum - 1);

                CellStyle style = workbook.createCellStyle();
                style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));


                row.createCell(0).setCellValue(data.getId());
                row.createCell(1).setCellValue(data.getNombre());
                row.createCell(2).setCellValue(data.getConcepto());
                row.createCell(3).setCellValue(data.getCuentaLocal());

                row.createCell(4).setCellValue(data.getValor());
                row.getCell(4).setCellStyle(style);

                row.createCell(5).setCellValue(data.getYntp());
                row.createCell(6).setCellValue(data.getL());

                row.createCell(7).setCellValue(data.getAbs());
                row.getCell(7).setCellStyle(style);

                row.createCell(8).setCellValue(data.getNat());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] excelBytes = outputStream.toByteArray();

            workbook.close();
            return excelBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

}