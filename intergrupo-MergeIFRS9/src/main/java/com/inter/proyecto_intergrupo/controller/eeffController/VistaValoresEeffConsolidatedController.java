package com.inter.proyecto_intergrupo.controller.eeffController;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.*;

import com.inter.proyecto_intergrupo.service.eeffconsolidated.*;
import org.apache.commons.lang3.StringUtils;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.DatesLoadEeffRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class VistaValoresEeffConsolidatedController {

    private static final int PAGINATIONCOUNT = 12;


    @Autowired
    private ValoresEeffConsolidatedService valoresEeffConsolidatedService;

    @Autowired
    private ValoresPucConsolidatedService valorespucConsolidatedService;

    @Autowired
    private BancoPucConsolidatedService bancoPucConsolidatedService;

    @Autowired
    private DatesLoadEeffService datesLoadEeffService;

    @Autowired
    private DatesLoadEeffRepository datesLoadEeffRepository;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/eeffConsolidated/filialesValores")
    public ModelAndView showTemplateEEFF(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        if (userService.validateEndpoint(user.getId(), "Ver EEFF Consolidado Valores")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

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

            int page1 = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest2 = PageRequest.of(page1, PAGINATIONCOUNT);
            Page<ValoreseeffFiliales> pageType = valoresEeffConsolidatedService.getAll(pageRequest2,todayString);
            int totalPage = pageType.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("current", page1 + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("rol", "Valores");
            modelAndView.addObject("directory", "filialesValores");
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("registers",pageType.getTotalElements());
            modelAndView.addObject("listaDeDatosValores", pageType.getContent());

            DatesLoadEeFF registro = datesLoadEeffRepository.findByEntidadAndPeriodo("Valores", todayString);
            String estadoDelRegistro = registro != null ? registro.getEstado() : "PENDING";

            modelAndView.addObject("estadoDelRegistro", estadoDelRegistro);
            modelAndView.setViewName("eeffConsolidated/filialesValores");
        } else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }

        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/filialesValores")
    public ModelAndView uploadFileEEFFValores(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/filialesValores");
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

            ArrayList<String[]> list = valoresEeffConsolidatedService.saveFileBD(fileContent , params.get("period").toString());
            String[] part = list.get(0);

            if (part[2].equals("SUCCESS")) {

                valoresEeffConsolidatedService.loadAudit(user, "Cargue exitoso plantilla EEFF Valores");
                Date fechaCargue = new Date(); // Obtener la fecha actual
                datesLoadEeffService.guardarFechasEnTabla("Valores", params.get("period").toString(), "Eeff", fechaCargue);
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);


            } else {
                valoresEeffConsolidatedService.loadAudit(user, "Cargue fallido plantilla EEFF Valores");
                ValoresEeffConsolidatedListReport rulesDQListReport = new ValoresEeffConsolidatedListReport(list, null);
                rulesDQListReport.exportLog(response);
            }

            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("vFilter", params.get("period").toString());

        }

        catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/eeffConsolidated/DescargarPucPeriodoValores")
    public void descargarPuc(HttpServletResponse response,
                             @RequestParam(value = "period", required = false) String periodo) {
        try {
            response.setContentType("application/vnd.ms-excel");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String todayString = "";

            if (StringUtils.isEmpty(periodo)) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);

                if (calendar.get(Calendar.MONTH) == 0) {
                    calendar.add(Calendar.YEAR, -1);
                    todayString = calendar.get(Calendar.YEAR) + "-12";
                } else {
                    todayString = String.format("%d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
                }
            } else {
                todayString = periodo;
            }

            List<ValoresPucFiliales> pucData = valorespucConsolidatedService.getPucDataByPeriod(todayString);

            valorespucConsolidatedService.downloadPucValores(response, pucData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RequestMapping(value = "/eeffConsolidated/DescargarPucCompletoV")
    public void descargarPucCompleto(HttpServletResponse response) {
        try {
            valorespucConsolidatedService.downloadAllPuc(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/eeffConsolidated/DescargarPUCCompletoBanco")
    public void descargarPucCompletoBanco(HttpServletResponse response , @RequestParam Map<String, Object> params) {
        try {
            bancoPucConsolidatedService.downloadPucBanco(response, params.get("period").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}