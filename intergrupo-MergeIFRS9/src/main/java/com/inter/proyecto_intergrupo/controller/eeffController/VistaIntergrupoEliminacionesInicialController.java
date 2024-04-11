package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.*;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.EliminacionesAjustadaService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.inter.proyecto_intergrupo.service.bankServices.TemplateBankService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.EliminacionesInicialService;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class VistaIntergrupoEliminacionesInicialController {

    private static final int PAGINATIONCOUNT = 10;

    @Autowired
    private EliminacionesInicialService eliminacionesInicialService;

    @Autowired
    private EliminacionesAjustadaService eliminacionesAjustadaService;

    @Autowired
    private statusInfoRepository statusInfoRepository;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/eeffConsolidated/IntergrupoEliminaciones")
    public ModelAndView showEliminacionCuadroGeneral(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if (userService.validateEndpoint(user.getUsuario(), "Ver EEFF Intergrupo Eliminaciones")) {

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

            List<EliminacionesVersionInicialCuadreGeneral> list = new ArrayList<>();
            List<EliminacionesDetalleVersionAjustada> list1 = new ArrayList<>();

            if (params.get("order") == null || params.get("order").toString() == "") {
                modelAndView.addObject("order", "Versión Inicial");
            } else {
                if (params.get("order") != null && params.get("order").toString().equals("Versión Inicial")) {
                    list = eliminacionesInicialService.getCuadreGeneral(todayString);
                } else if (params.get("order") != null && params.get("order").toString().equals("Versión Ajustada")) {
                   list1 = eliminacionesAjustadaService.getCuadreEliminacionDetalleCuadreGeneral(todayString);
                }
                    modelAndView.addObject("order", params.get("order").toString());
            }

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest2 = PageRequest.of(page, PAGINATIONCOUNT);
            Page<EliminacionesVersionInicialCuadreGeneral> pageType = eliminacionesInicialService.getAllCuadre(pageRequest2, todayString, list);
            int totalPage = pageType.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("directory", "IntergrupoEliminaciones");
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("registers", pageType.getTotalElements());
            modelAndView.addObject("listaDeDatosEliminaciones", pageType.getContent());


            int page1 = params.get("page1") != null ? (Integer.valueOf(params.get("page1").toString()) - 1) : 0;
            PageRequest pageRequest1 = PageRequest.of(page1, PAGINATIONCOUNT);
            Page<EliminacionesDetalleVersionAjustada> pageType1 = eliminacionesAjustadaService.getAllEliminacionDetalleCuadreGeneral(pageRequest1,todayString,list1);
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
            modelAndView.addObject("listaDeDatosEliminacionesVersionAjustada", pageType1.getContent());

            StatusInfo registro = statusInfoRepository.findByInputAndPeriodo("ELIMINACIONES CARGUE CONFIRMADO", todayString);
            String estadoDelRegistro = registro != null ? registro.getStatus() : "NULL";
            modelAndView.addObject("estadoDelRegistro", estadoDelRegistro);

            StatusInfo registro1 = statusInfoRepository.findByInputAndPeriodo("ELIMINACIONES AUTORIZACION", todayString);
            String estadoDelRegistro1 = registro1 != null ? registro.getStatus() : "NULL";
            modelAndView.addObject("estadoDelRegistro1", estadoDelRegistro1);

        } else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping("/eeffConsolidated/downloadEliminacionAjustada")
    public ResponseEntity<Resource> exportToExcelReportEliminacionCuadreGeneralAjustada(@RequestParam Map<String, Object> params) {
        String periodo = params.get("period").toString();
        List<EliminacionesDetalleVersionAjustada> listaDatos = eliminacionesAjustadaService.getCuadreEliminacionDetalleCuadreGeneral(periodo);
        byte[] excelBytes = generarArchivoExcel1(listaDatos);
        ByteArrayResource resource = new ByteArrayResource(excelBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Eliminacion_VersionAjustada" + periodo + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelBytes.length)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }


    @GetMapping("/eeffConsolidated/downloadEliminacion")
    public ResponseEntity<Resource> exportToExcelReportEliminacion(@RequestParam Map<String, Object> params) {
        String periodo = params.get("period").toString();
        List<EliminacionesVersionInicialCuadreGeneral> listaDatos = eliminacionesInicialService.getCuadreGeneral(periodo);
        byte[] excelBytes = generarArchivoExcel(listaDatos);
        ByteArrayResource resource = new ByteArrayResource(excelBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=Eliminacion_" + periodo + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(excelBytes.length)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    private byte[] generarArchivoExcel(List<EliminacionesVersionInicialCuadreGeneral> datos) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Datos");
            Row headerRow = sheet.createRow(0);

            String[] headers = {"Nombre", "Concepto", "Plantilla Banco", "Plantilla Filial", "Ajuste", "Total General"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            for (int rowNum = 1; rowNum <= datos.size(); rowNum++) {
                Row row = sheet.createRow(rowNum);
                EliminacionesVersionInicialCuadreGeneral data = datos.get(rowNum - 1);

                CellStyle style = workbook.createCellStyle();
                style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

                row.createCell(0).setCellValue(data.getNombre());
                row.createCell(1).setCellValue(data.getConcepto());

                row.createCell(2).setCellValue(data.getPlantillaBanco());
                row.getCell(2).setCellStyle(style);

                row.createCell(3).setCellValue(data.getPlantillaFilial());
                row.getCell(3).setCellStyle(style);

                row.createCell(4).setCellValue(data.getAjuste());
                row.getCell(4).setCellStyle(style);

                row.createCell(5).setCellValue(data.getTotalGeneral());
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
    @GetMapping("/eeffConsolidated/obtenerDiferenciasPorConcepto")
    public ModelAndView obtenerDiferenciasPorConcepto(@RequestParam("period") String periodo)
    {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/IntergrupoEliminaciones");
        eliminacionesInicialService.obtenerDiferenciasPorConcepto(periodo);

        modelAndView.addObject("resp","ProcessExi1");
        modelAndView.addObject("period", periodo);
        modelAndView.addObject("order","Versión Inicial");
        return modelAndView;
    }

    @GetMapping("/eeffConsolidated/procesarEliminaciones")
    public ModelAndView ProcesarEliminaciones(@RequestParam("period") String periodo)
    {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/IntergrupoEliminacionesDetalle");
        eliminacionesInicialService.ProcesarEliminaciones(periodo);
        modelAndView.addObject("resp", "ProcessExi");
        modelAndView.addObject("period", periodo);
        modelAndView.addObject("order","Versión Ajustada");
        return modelAndView;
    }

    @GetMapping(value="/eeffConsolidated/ConfirmarInfoInicialAnaMaria")
    public ModelAndView confirmarInfoInicial(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/IntergrupoEliminaciones");
        String todayString="";
        if(params.get("period")==null || Objects.equals(params.get("period").toString(), "")) {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);

            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));

            }
        }
        else {
            todayString=params.get("period").toString();
        }
        try{

            modelAndView.addObject("resp","correct");

        }catch (Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp","error");
        }
        eliminacionesInicialService.confirmarInfo(todayString);
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("order", params.get("order").toString());
        modelAndView.addObject("resp", "AddRep3");
        return  modelAndView;
    }

    @GetMapping(value="/eeffConsolidated/ConfirmarInfoInicialLuzmarina")
    public ModelAndView confirmarInfoInicialLuzMarina(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/IntergrupoEliminaciones");
        String todayString="";
        System.out.println(params.get("period").toString());
        if(params.get("period")==null || Objects.equals(params.get("period").toString(), "")) {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);

            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }
        try{
            modelAndView.addObject("resp","correct");
        }catch (Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp","error");
        }
        eliminacionesInicialService.confirmarInfoLuzMa(todayString);
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("order", params.get("order").toString());
        modelAndView.addObject("resp", "AddRep3");
        return  modelAndView;
    }

    private byte[] generarArchivoExcel1(List<EliminacionesDetalleVersionAjustada> datos) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Datos");
            Row headerRow = sheet.createRow(0);

            String[] headers = {"Nombre", "Concepto", "Plantilla Banco", "Plantilla Filial", "Ajuste", "Total General"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            for (int rowNum = 1; rowNum <= datos.size(); rowNum++) {
                Row row = sheet.createRow(rowNum);
                EliminacionesDetalleVersionAjustada data = datos.get(rowNum - 1);

                CellStyle style = workbook.createCellStyle();
                style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

                row.createCell(0).setCellValue(data.getNombre());
                row.createCell(1).setCellValue(data.getConcepto());

                row.createCell(2).setCellValue(data.getPlantillaBanco());
                row.getCell(2).setCellStyle(style);

                row.createCell(3).setCellValue(data.getPlantillaFilial());
                row.getCell(3).setCellStyle(style);

                row.createCell(4).setCellValue(data.getAjuste());
                row.getCell(4).setCellStyle(style);

                row.createCell(5).setCellValue(data.getTotalGeneral());
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
