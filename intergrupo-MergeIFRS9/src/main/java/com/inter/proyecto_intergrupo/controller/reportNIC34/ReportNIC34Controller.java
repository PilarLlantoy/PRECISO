package com.inter.proyecto_intergrupo.controller.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.dataquality.PointRulesDQ;
import com.inter.proyecto_intergrupo.model.dataquality.RulesDQ;
import com.inter.proyecto_intergrupo.model.reportNIC34.BalanceNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.BaseNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamFechas;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.dataqualityServices.PointRulesDQListReport;
import com.inter.proyecto_intergrupo.service.dataqualityServices.RulesDQListReport;
import com.inter.proyecto_intergrupo.service.dataqualityServices.RulesDQService;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamFechasService;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamNIC34ListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ReportNIC34ListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ReportNIC34Service;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ReportNIC34Controller {

    private static final int PAGINATIONCOUNT=16;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ReportNIC34Service reportNIC34Service;

    @Autowired
    private ParamFechasService paramFechasService;

    private List<String> listColumns=List.of("Fecont","L6","Nucta","ID Grupo","Grupo","Aplica","Signo","ID Nota","Nota","ID SubNota","SubNota","Moneda","Saldo","Balance","PYG");
    private List<String> listColumns2=List.of("ID","Moneda","Signo","Grupo","Nota");

    @GetMapping(value="/reportNIC34/nic34")
    public ModelAndView showReportNIC34(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Reporte NIC34")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            String todayString = reportNIC34Service.generatePeriodo(params.get("period"));
            String corte = reportNIC34Service.generateCorte(params.get("corte"));
            List<BaseNIC34> list = reportNIC34Service.getData(corte,todayString);

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<BaseNIC34> pageMda= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageMda.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            if(params.get("vId")!=null && params.get("vFilter")!=null && params.get("vId").toString()!="" && params.get("vFilter").toString()!="")
            {
                modelAndView.addObject("vId",params.get("vId").toString());
                modelAndView.addObject("vFilter",params.get("vFilter").toString());
                modelAndView.addObject("directory", "searchNic34");
            }
            else
            {
                modelAndView.addObject("directory", "nic34");
            }

            modelAndView.addObject("allData", pageMda.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("registers",pageMda.getTotalElements());
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("corte", corte);
            modelAndView.addObject("vPeriod", corte+"-"+todayString);
            modelAndView.setViewName("reportNIC34/nic34");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/reportNIC34/nic34")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reportNIC34/nic34");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            boolean validation = reportNIC34Service.saveFileBD(fileContent,params.get("corte").toString(),params.get("period").toString(),user);

            if(validation){
                reportNIC34Service.loadAudit(user, "Cargue exitoso Query NIC34");
                paramFechasService.validateStatus("CARGADO");
                paramFechasService.validateStatus("PENDIENTE");
                modelAndView.addObject("resp", "AddQuery1");
            }
            else{
                reportNIC34Service.loadAudit(user, "Cargue fallido Query NIC34");
                modelAndView.addObject("resp", "AddQuery-1");
            }

            modelAndView.addObject("period",params.get("period").toString());
            modelAndView.addObject("corte",params.get("corte").toString());
            modelAndView.addObject("vFilter",params.get("corte").toString()+"-"+params.get("period").toString());

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34/download")
    @ResponseBody
    public void exportExcelBase(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Base_NIC34_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<BaseNIC34>templateList = new ArrayList<>();
        System.out.println(params.get("vFilter"));
        System.out.println(params.get("vId"));
        if(params.get("vFilter")!=null && params.get("vId")!=null && params.get("vFilter").toString()!="" && params.get("vId").toString()!="")
            templateList = reportNIC34Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),params.get("corte").toString(),params.get("period").toString());
        else
            templateList = reportNIC34Service.getData(params.get("corte").toString(),params.get("period").toString());
        ReportNIC34ListReport listReport = new ReportNIC34ListReport(null,templateList,null);
        listReport.export(response);

    }

    @GetMapping(value = "/reportNIC34/nic34/processData")
    public ModelAndView processDataBase(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reportNIC34/nic34");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("corte", params.get("corte").toString());
            if(reportNIC34Service.getAllData(params.get("corte").toString(),params.get("period").toString()).size()>=2)
            {
                List<Object[]> listVal = reportNIC34Service.validateData(params.get("corte").toString(),params.get("period").toString());
                if(listVal.isEmpty())
                {
                    reportNIC34Service.processData(params.get("corte").toString(),params.get("period").toString());
                    reportNIC34Service.loadAudit(user, "Procesamiento exitoso NIC34");
                    modelAndView.addObject("resp", "ExecNIC341");
                }
                else
                {
                    response.setContentType("application/octet-stream");
                    DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                    String currentDateTime = dateFormatter.format(new Date());
                    String headerKey = "Content-Disposition";
                    String headerValue = "attachment; filename=Log_Proceso_" + currentDateTime + ".xlsx";
                    response.setHeader(headerKey, headerValue);

                    ReportNIC34ListReport reportNIC34ListReport = new ReportNIC34ListReport(listVal,null,null);
                    reportNIC34ListReport.exportLog(response);
                }
            }
            else
            {
                reportNIC34Service.loadAudit(user, "Procesamiento NIC34 con inputs faltantes");
                modelAndView.addObject("resp", "ExecNIC34-1");
            }

        }
        catch (Exception e){
            e.printStackTrace();
            reportNIC34Service.loadAudit(user, "Procesamiento fallido NIC34");
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value="/reportNIC34/searchNic34")
    public ModelAndView showSearchReportNIC34(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());


        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        String todayString = reportNIC34Service.generatePeriodo(params.get("period"));
        String corte = reportNIC34Service.generateCorte(params.get("corte"));
        List<BaseNIC34> list=reportNIC34Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),corte,todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<BaseNIC34> pageMda= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage = pageMda.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }

        modelAndView.addObject("allData", pageMda.getContent());
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("registers",pageMda.getTotalElements());
        modelAndView.addObject("columns", listColumns);
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("corte", corte);
        modelAndView.addObject("vPeriod", corte+"-"+todayString);
        modelAndView.addObject("directory", "searchNic34");
        modelAndView.setViewName("reportNIC34/nic34");

        return modelAndView;
    }

    @GetMapping(value="/reportNIC34/nic34Balance")
    public ModelAndView showReportNIC34Balance(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver EEFF NIC34 Balance")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            String todayString = reportNIC34Service.generatePeriodo(params.get("period"));
            String corte = reportNIC34Service.generateCorte(params.get("corte"));
            List<Object[]> list = reportNIC34Service.getDataBalanceVista(corte,todayString);
            List<String> listFechas = reportNIC34Service.getDataFecontBalance(corte,todayString);

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageData= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageData.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            if(params.get("vId")!=null && params.get("vFilter")!=null && params.get("vId").toString()!="" && params.get("vFilter").toString()!="")
            {
                modelAndView.addObject("vId",params.get("vId").toString());
                modelAndView.addObject("vFilter",params.get("vFilter").toString());
                modelAndView.addObject("directory", "searchNic34");
            }
            else
            {
                modelAndView.addObject("directory", "nic34Balance");
            }
            if( params.get("puntaje") != null && !params.get("puntaje").toString().equals(""))
                modelAndView.addObject("puntaje", params.get("puntaje").toString());

            modelAndView.addObject("allData", pageData.getContent());
            modelAndView.addObject("allFechas", listFechas);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("registers",pageData.getTotalElements());
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("corte", corte);
            modelAndView.addObject("vPeriod", corte+"-"+todayString);
            modelAndView.setViewName("reportNIC34/nic34Balance");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34Balance/processData")
    public ModelAndView processDataBaseBalance(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reportNIC34/nic34Balance");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("corte", params.get("corte").toString());

            if(reportNIC34Service.getDataBaseBalance(params.get("corte").toString(),params.get("period").toString()).size()==2)
            {
                reportNIC34Service.generateDataBalance(params.get("corte").toString(),params.get("period").toString(),params.get("puntaje").toString());
                reportNIC34Service.getDataFilBalance(params.get("corte").toString(),params.get("period").toString());
                reportNIC34Service.loadAudit(user, "Procesamiento Exitoso NIC34 EEFF Balance");
                modelAndView.addObject("resp", "ExecNIC341");
                modelAndView.addObject("puntaje", params.get("puntaje").toString());
            }
            else
            {
                reportNIC34Service.loadAudit(user, "Procesamiento Fallido NIC34 EEFF Balance");
                modelAndView.addObject("resp", "ExecNIC34-2");
            }

        }
        catch (Exception e){
            e.printStackTrace();
            reportNIC34Service.loadAudit(user, "Procesamiento fallido NIC34");
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34Balance/download")
    @ResponseBody
    public void exportExcelBalance(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Balance_NIC34_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]>templateList = reportNIC34Service.getDataBalanceVista(params.get("corte").toString(),params.get("period").toString());
        List<Object[]>templateList2 = reportNIC34Service.getDataBaseGroup(params.get("corte").toString(),params.get("period").toString(),"BALANCE");
        List<String> listFechas = reportNIC34Service.getDataFecontBalance(params.get("corte").toString(),params.get("period").toString());
        ReportNIC34ListReport listReport = new ReportNIC34ListReport(templateList2,null,templateList);
        listReport.exportBalance(response,listFechas);

    }

    @GetMapping(value="/reportNIC34/nic34Pyg")
    public ModelAndView showReportNIC34Pyg(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver EEFF NIC34 PYG")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            String todayString = reportNIC34Service.generatePeriodo(params.get("period"));
            String corte = reportNIC34Service.generateCorte(params.get("corte"));
            List<Object[]> list = reportNIC34Service.getDataPygVista(corte,todayString);
            List<String> listFechas = reportNIC34Service.getDataFecontPyg(corte,todayString);

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageData= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageData.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            if(params.get("vId")!=null && params.get("vFilter")!=null && params.get("vId").toString()!="" && params.get("vFilter").toString()!="")
            {
                modelAndView.addObject("vId",params.get("vId").toString());
                modelAndView.addObject("vFilter",params.get("vFilter").toString());
                modelAndView.addObject("directory", "searchNic34");
            }
            else
            {
                modelAndView.addObject("directory", "nic34Pyg");
            }
            if( params.get("puntaje") != null && !params.get("puntaje").toString().equals(""))
                modelAndView.addObject("puntaje", params.get("puntaje").toString());

            modelAndView.addObject("allData", pageData.getContent());
            modelAndView.addObject("allFechas", listFechas);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("registers",pageData.getTotalElements());
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("corte", corte);
            modelAndView.addObject("vPeriod", corte+"-"+todayString);
            modelAndView.setViewName("reportNIC34/nic34Pyg");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34Pyg/processData")
    public ModelAndView processDataBasePyg(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reportNIC34/nic34Pyg");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("corte", params.get("corte").toString());

            if(reportNIC34Service.getDataBasePyg(params.get("corte").toString(),params.get("period").toString()).size()==4)
            {
                reportNIC34Service.generateDataPyg(params.get("corte").toString(),params.get("period").toString(),params.get("puntaje").toString());
                reportNIC34Service.getDataFilPyg(params.get("corte").toString(),params.get("period").toString());
                reportNIC34Service.loadAudit(user, "Procesamiento Exitoso NIC34 EEFF PYG");
                modelAndView.addObject("resp", "ExecNIC341");
                modelAndView.addObject("puntaje", params.get("puntaje").toString());
            }
            else
            {
                reportNIC34Service.loadAudit(user, "Procesamiento Fallido NIC34 EEFF PYG");
                modelAndView.addObject("resp", "ExecNIC34-3");
            }

        }
        catch (Exception e){
            e.printStackTrace();
            reportNIC34Service.loadAudit(user, "Procesamiento fallido NIC34");
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34Pyg/download")
    @ResponseBody
    public void exportExcelPyg(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Pyg_NIC34_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]>templateList = reportNIC34Service.getDataPygVista(params.get("corte").toString(),params.get("period").toString());
        List<Object[]>templateList2 = reportNIC34Service.getDataBaseGroup(params.get("corte").toString(),params.get("period").toString(),"PYG");
        List<String> listFechas = reportNIC34Service.getDataFecontPyg(params.get("corte").toString(),params.get("period").toString());
        ReportNIC34ListReport listReport = new ReportNIC34ListReport(templateList2,null,templateList);
        listReport.exportPyg(response,listFechas);

    }

    @GetMapping(value="/reportNIC34/nic34Notas")
    public ModelAndView showReportNIC34Notas(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver EEFF NIC34 Notas")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            String todayString = reportNIC34Service.generatePeriodo(params.get("period"));
            String corte = reportNIC34Service.generateCorte(params.get("corte"));
            List<Object[]> list = reportNIC34Service.getDataNotasVista(corte,todayString);
            List<String> listFechas = reportNIC34Service.getDataFecontNotas(corte,todayString);

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageData= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageData.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            if(params.get("vId")!=null && params.get("vFilter")!=null && params.get("vId").toString()!="" && params.get("vFilter").toString()!="")
            {
                modelAndView.addObject("vId",params.get("vId").toString());
                modelAndView.addObject("vFilter",params.get("vFilter").toString());
                modelAndView.addObject("directory", "searchNic34");
            }
            else
            {
                modelAndView.addObject("directory", "nic34Notas");
            }
            if( params.get("puntaje") != null && !params.get("puntaje").toString().equals(""))
                modelAndView.addObject("puntaje", params.get("puntaje").toString());
            if( params.get("aplica") != null && !params.get("aplica").toString().equals(""))
                modelAndView.addObject("aplica", params.get("aplica").toString());

            modelAndView.addObject("allData", pageData.getContent());
            modelAndView.addObject("allFechas", listFechas);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("columns", listColumns2);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("registers",pageData.getTotalElements());
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("corte", corte);
            modelAndView.addObject("vPeriod", corte+"-"+todayString);
            modelAndView.setViewName("reportNIC34/nic34Notas");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34Notas/processData")
    public ModelAndView processDataBaseNotas(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reportNIC34/nic34Notas");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("corte", params.get("corte").toString());

            if(reportNIC34Service.getDataBaseBalance(params.get("corte").toString(),params.get("period").toString()).size()==2)
            {
                reportNIC34Service.generateDataNotas(params.get("corte").toString(),params.get("period").toString(),params.get("puntaje").toString(),params.get("aplica").toString());
                reportNIC34Service.getDataFilNotas(params.get("corte").toString(),params.get("period").toString(),params.get("aplica").toString(),null,null);
                reportNIC34Service.loadAudit(user, "Procesamiento Exitoso NIC34 EEFF Notas");
                modelAndView.addObject("resp", "ExecNIC341");
                modelAndView.addObject("puntaje", params.get("puntaje").toString());
                modelAndView.addObject("aplica", params.get("aplica").toString());
            }
            else
            {
                reportNIC34Service.loadAudit(user, "Procesamiento Fallido NIC34 EEFF Balance");
                modelAndView.addObject("resp", "ExecNIC34-2");
            }

        }
        catch (Exception e){
            e.printStackTrace();
            reportNIC34Service.loadAudit(user, "Procesamiento fallido NIC34");
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34Notas/download")
    @ResponseBody
    public void exportExcelNotas(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Notas_NIC34_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]>templateList = reportNIC34Service.getDataNotasVista(params.get("corte").toString(),params.get("period").toString());
        if(params.get("vId")!=null && params.get("vFilter")!=null && !params.get("vId").toString().equals("") && !params.get("vFilter").toString().equals(""))
            templateList = reportNIC34Service.getDataNotasVistaFiltro(params.get("corte").toString(),params.get("period").toString(),params.get("vId").toString(),params.get("vFilter").toString());
        List<Object[]>templateList2 = reportNIC34Service.getDataBaseGroupNotas(params.get("corte").toString(),params.get("period").toString());
        List<String> listFechas = reportNIC34Service.getDataFecontNotas(params.get("corte").toString(),params.get("period").toString());
        ReportNIC34ListReport listReport = new ReportNIC34ListReport(templateList2,null,templateList);
        listReport.exportNotas(response,listFechas);

    }

    @GetMapping(value="/reportNIC34/nic34Notas/searchNic34")
    public ModelAndView showSearchReportNIC34Notas(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());


        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        String todayString = reportNIC34Service.generatePeriodo(params.get("period"));
        String corte = reportNIC34Service.generateCorte(params.get("corte"));
        List<Object[]> list = reportNIC34Service.getDataNotasVistaFiltro(corte,todayString,params.get("vId").toString(),params.get("vFilter").toString());
        List<String> listFechas = reportNIC34Service.getDataFecontNotas(corte,todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Object[]> pageMda= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage = pageMda.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }

        modelAndView.addObject("allData", pageMda.getContent());
        modelAndView.addObject("allFechas", listFechas);
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("registers",pageMda.getTotalElements());
        modelAndView.addObject("columns", listColumns2);
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("corte", corte);
        modelAndView.addObject("vPeriod", corte+"-"+todayString);
        modelAndView.addObject("directory", "nic34Notas/searchNic34");
        modelAndView.setViewName("reportNIC34/nic34Notas");

        return modelAndView;
    }

}
