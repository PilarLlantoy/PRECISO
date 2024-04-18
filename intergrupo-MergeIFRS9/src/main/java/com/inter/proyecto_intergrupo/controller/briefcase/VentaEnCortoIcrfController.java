package com.inter.proyecto_intergrupo.controller.briefcase;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.GarantiasActivasIcrf;
import com.inter.proyecto_intergrupo.model.briefcase.ReposIcrf;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.briefcaseServices.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class VentaEnCortoIcrfController {

    @Autowired
    VentaEnCortoIcrfService ventaEnCortoIcrfService;

    @Autowired
    private UserService userService;

    private static final int PAGINATIONCOUNT=12;


    @GetMapping(value = "/briefcase/ventaencortoicrf")
    public ModelAndView showView(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Venta En Corto ICRF")){
            int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";

            if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                if(calendar.get(Calendar.MONTH)==0)
                {
                    calendar.add(Calendar.YEAR,-1);
                    todayString = calendar.get(Calendar.YEAR) + "-12";
                }
                else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
                } else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
                }
            } else {
                todayString = params.get("period").toString();
            }

            List<Object[]> list = ventaEnCortoIcrfService.getDataResumeTotal(todayString);
            modelAndView.addObject("filterExport", "Original");

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageList = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageList.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("icrfList", pageList.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("registers",list.size());
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("directory", "ventaencortoicrf");
            modelAndView.setViewName("/briefcase/ventaencortoicrf");

        }
        else
        {
            modelAndView.addObject("ventaencortoicrf","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    /*@PostMapping(value = "/briefcase/icrv")
    public ModelAndView loadPlantilla(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/icrv");
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
            ArrayList<String[]> list = reportIcrvService.saveFileBDPlantilla(fileContent,user,params.get("period").toString());
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                ReportIcrvListReport reportIcrvListReport = new ReportIcrvListReport(list,null);
                reportIcrvListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/icrv/download")
    @ResponseBody
    public void downloadReport(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        String Id = "";
        String Vf = "";

        if (params.get("vId") == null) {
            Id = "";
        } else {
            Id = params.get("vId").toString();
        }
        if (params.get("vFilter") == null) {
            Vf = "";
        } else {
            Vf = params.get("vFilter").toString();
        }
        List<ReportIcrv> list;
        if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
            list = reportIcrvService.findAllReport(params.get("period").toString());
        } else {
            list = reportIcrvService.findByFilter(Id,Vf,params.get("period").toString());
        }
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= ReporteICRV_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ReportIcrvListReport listReport = new ReportIcrvListReport(null, list);
        listReport.export(response);
    }

    @GetMapping(value = "/briefcase/icrv/downloadPlano")
    @ResponseBody
    public void downloadReportPlane(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment;  filename=ICRV_CARGA_" + currentDateTime + ".txt");

        List<ReportIcrv> data = reportIcrvService.findAllReport(params.get("period").toString());

        CsvService.downloadTxtReportIcrv(response.getWriter(), data);
    }

    @GetMapping(value = "/briefcase/modifyIcrv/{id}")
    @ResponseBody
    public ModelAndView modifyReporticrv(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        ReportIcrv toModify = reportIcrvService.findByIdReport(idTemp);
        modelAndView.addObject("icrvModify",toModify);
        modelAndView.setViewName("briefcase/modifyIcrv");
        return modelAndView;
    }

    @PostMapping(value = "/briefcase/modifyIcrv")
    @ResponseBody
    public ModelAndView updateReporticrv(@ModelAttribute ReportIcrv reportIcrv){
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/icrv");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            reportIcrvService.modifyReport(reportIcrv, user);
            modelAndView.addObject("period", reportIcrv.getPeriodo());
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }*/

    @GetMapping(value = "/briefcase/icrf/downloadVcRepos")
    @ResponseBody
    public void downloadReport(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        List<ReposIcrf> list = ventaEnCortoIcrfService.getDataRepos(params.get("period").toString());

        LocalDate fecha = LocalDate.parse(ventaEnCortoIcrfService.generateDatecalendar(params.get("period").toString()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy").withLocale(java.util.Locale.ENGLISH);
        String fechaFormateada = fecha.format(formatter);

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Repos_e_Interbancarios_" + fechaFormateada + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ReposIcrfListReport listReport = new ReposIcrfListReport(null, list);
        listReport.export(response);
    }

    @GetMapping(value = "/briefcase/icrf/downloadVcGarantias")
    @ResponseBody
    public void downloadReportGarantias(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        List<GarantiasActivasIcrf> list = ventaEnCortoIcrfService.getDataGarantias(params.get("period").toString());

        LocalDate fecha = LocalDate.parse(ventaEnCortoIcrfService.generateDatecalendar(params.get("period").toString()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy").withLocale(java.util.Locale.ENGLISH);
        String fechaFormateada = fecha.format(formatter);

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Garantias_Activas_" + fechaFormateada + ".xlsx";
        response.setHeader(headerKey, headerValue);
        GarantiasActivasIcrfListReport listReport = new GarantiasActivasIcrfListReport(null, list);
        listReport.export(response);
    }

    @GetMapping(value = "/briefcase/icrf/downloadVcResume")
    @ResponseBody
    public void downloadReportResumen(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        List<Object[]> list = ventaEnCortoIcrfService.getDataResumeTotal(params.get("period").toString());

        LocalDate fecha = LocalDate.parse(ventaEnCortoIcrfService.generateDatecalendar(params.get("period").toString()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy").withLocale(java.util.Locale.ENGLISH);
        String fechaFormateada = fecha.format(formatter);

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Resumen_Venta_En Corto_" + fechaFormateada + ".xlsx";
        response.setHeader(headerKey, headerValue);
        VentaEnCortoIcrfListReport listReport = new VentaEnCortoIcrfListReport(null, null,list);
        listReport.export(response);
    }

    @GetMapping(value = "/briefcase/generateInputsIcrf")
    @ResponseBody
    public ModelAndView removeIcrv(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/ventaencortoicrf");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            ventaEnCortoIcrfService.insertFilePortafolioDiario(params.get("period").toString());
            ventaEnCortoIcrfService.insertFileGarantiasActivas(params.get("period").toString());
            ventaEnCortoIcrfService.insertFileRepos(params.get("period").toString());
            ventaEnCortoIcrfService.generateResume(params.get("period").toString());
            modelAndView.addObject("resp", "ICRF1");
        }
        catch (Exception e)
        {
            modelAndView.addObject("resp", "ICRF-1");
            e.printStackTrace();
        }
        modelAndView.addObject("period", params.get("period").toString());
        return  modelAndView;
    }
}
