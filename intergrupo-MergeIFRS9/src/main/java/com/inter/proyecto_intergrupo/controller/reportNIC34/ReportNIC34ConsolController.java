package com.inter.proyecto_intergrupo.controller.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.BaseNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.BaseNIC34Consol;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.*;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ReportNIC34ConsolController {

    private static final int PAGINATIONCOUNT=16;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ReportNIC34ConsolService reportNIC34Service;

    @Autowired
    private ParamFechasService paramFechasService;

    private List<String> listColumns=List.of("Fecont","L6","Nucta","ID Grupo","Grupo","Aplica","Signo","ID Nota","Nota","ID SubNota","SubNota","Moneda","Saldo","Balance","PYG");
    private List<String> listColumns2=List.of("ID","Moneda","Signo","Grupo","Nota");

    @GetMapping(value="/reportNIC34/nic34Consol")
    public ModelAndView showReportNIC34Consol(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Reporte NIC34")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            String todayString = reportNIC34Service.generatePeriodo(params.get("period"));
            String corte = reportNIC34Service.generateCorte(params.get("corte"));
            List<BaseNIC34Consol> list = reportNIC34Service.getData(corte,todayString);

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<BaseNIC34Consol> pageMda= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageMda.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            if(params.get("vId")!=null && params.get("vFilter")!=null && params.get("vId").toString()!="" && params.get("vFilter").toString()!="")
            {
                modelAndView.addObject("vId",params.get("vId").toString());
                modelAndView.addObject("vFilter",params.get("vFilter").toString());
                modelAndView.addObject("directory", "searchNic34Consol");
            }
            else
            {
                modelAndView.addObject("directory", "nic34Consol");
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
            modelAndView.setViewName("reportNIC34/nic34Consol");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/reportNIC34/nic34Consol")
    public ModelAndView uploadFileConsol(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reportNIC34/nic34Consol");
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
            ArrayList<String[]> list = reportNIC34Service.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if (part[2].equals("SUCCESS")) {
                reportNIC34Service.loadAudit(user, "Cargue exitoso Query NIC34 Consolidado");
                paramFechasService.validateStatusConsol("CARGADO");
                paramFechasService.validateStatusConsol("PENDIENTE");
                modelAndView.addObject("resp", "AddQuery1");
            }
            else{
                reportNIC34Service.loadAudit(user, "Cargue fallido Query NIC34 Consolidado");
                SignatureListReport signatureListReport = new SignatureListReport(list, null);
                signatureListReport.exportLog(response);
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

    @GetMapping(value = "/reportNIC34/nic34Consol/download")
    @ResponseBody
    public void exportExcelBase(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Base_NIC34_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<BaseNIC34Consol>templateList = new ArrayList<>();
        if(params.get("vFilter")!=null && params.get("vId")!=null && params.get("vFilter").toString()!="" && params.get("vId").toString()!="")
            templateList = reportNIC34Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),params.get("corte").toString(),params.get("period").toString());
        else
            templateList = reportNIC34Service.getData(params.get("corte").toString(),params.get("period").toString());
        ReportNIC34ConsolListReport listReport = new ReportNIC34ConsolListReport(null,templateList,null);
        listReport.export(response);

    }

    @GetMapping(value = "/reportNIC34/nic34Consol/downloadPlantilla")
    @ResponseBody
    public void exportExcelPlantilla(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Plantilla_NIC34_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ReportNIC34ConsolListReport listReport = new ReportNIC34ConsolListReport(null,null,null);
        listReport.exportPlantilla(response);

    }


    @GetMapping(value = "/reportNIC34/nic34Consol/processData")
    public ModelAndView processDataBaseConsol(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reportNIC34/nic34Consol");
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
                    reportNIC34Service.loadAudit(user, "Procesamiento exitoso NIC34 Consolidado");
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

                    ReportNIC34ConsolListReport reportNIC34ListReport = new ReportNIC34ConsolListReport(listVal,null,null);
                    reportNIC34ListReport.exportLog(response);
                }
            }
            else
            {
                reportNIC34Service.loadAudit(user, "Procesamiento NIC34 Consolidado con inputs faltantes");
                modelAndView.addObject("resp", "ExecNIC34-1");
            }

        }
        catch (Exception e){
            e.printStackTrace();
            reportNIC34Service.loadAudit(user, "Procesamiento fallido NIC34 Consolidado");
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value="/reportNIC34/searchNic34Consol")
    public ModelAndView showSearchReportNIC34Consol(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());


        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        String todayString = reportNIC34Service.generatePeriodo(params.get("period"));
        String corte = reportNIC34Service.generateCorte(params.get("corte"));
        List<BaseNIC34Consol> list=reportNIC34Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),corte,todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<BaseNIC34Consol> pageMda= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

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
        modelAndView.addObject("directory", "searchNic34Consol");
        modelAndView.setViewName("reportNIC34/nic34Consol");

        return modelAndView;
    }

    @GetMapping(value="/reportNIC34/nic34ConsolBalance")
    public ModelAndView showReportNIC34Balance(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver EEFF NIC34 Balance")) {

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
                modelAndView.addObject("directory", "searchNic34Consol");
            }
            else
            {
                modelAndView.addObject("directory", "nic34ConsolBalance");
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
            modelAndView.setViewName("reportNIC34/nic34ConsolBalance");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34ConsolBalance/processData")
    public ModelAndView processDataBaseBalanceConsol(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reportNIC34/nic34ConsolBalance");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("corte", params.get("corte").toString());

            if(reportNIC34Service.getDataBaseBalance(params.get("corte").toString(),params.get("period").toString()).size()==2)
            {
                reportNIC34Service.generateDataBalance(params.get("corte").toString(),params.get("period").toString(),params.get("puntaje").toString());
                reportNIC34Service.getDataFilBalance(params.get("corte").toString(),params.get("period").toString());
                reportNIC34Service.loadAudit(user, "Procesamiento Exitoso NIC34 EEFF Balance Consolidado");
                modelAndView.addObject("resp", "ExecNIC341");
                modelAndView.addObject("puntaje", params.get("puntaje").toString());
            }
            else
            {
                reportNIC34Service.loadAudit(user, "Procesamiento Fallido NIC34 EEFF Balance Consolidado");
                modelAndView.addObject("resp", "ExecNIC34-2");
            }

        }
        catch (Exception e){
            e.printStackTrace();
            reportNIC34Service.loadAudit(user, "Procesamiento fallido NIC34 Consolidado");
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34ConsolBalance/download")
    @ResponseBody
    public void exportExcelBalanceConsol(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
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

    @GetMapping(value="/reportNIC34/nic34ConsolPyg")
    public ModelAndView showReportNIC34Pyg(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver EEFF NIC34 PYG")) {

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
                modelAndView.addObject("directory", "searchNic34Consol");
            }
            else
            {
                modelAndView.addObject("directory", "nic34ConsolPyg");
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
            modelAndView.setViewName("reportNIC34/nic34ConsolPyg");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34ConsolPyg/processData")
    public ModelAndView processDataBasePygConsol(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reportNIC34/nic34ConsolPyg");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("corte", params.get("corte").toString());

            if(reportNIC34Service.getDataBasePyg(params.get("corte").toString(),params.get("period").toString()).size()==4)
            {
                reportNIC34Service.generateDataPyg(params.get("corte").toString(),params.get("period").toString(),params.get("puntaje").toString());
                reportNIC34Service.getDataFilPyg(params.get("corte").toString(),params.get("period").toString());
                reportNIC34Service.loadAudit(user, "Procesamiento Exitoso NIC34 EEFF PYG Consolidado");
                modelAndView.addObject("resp", "ExecNIC341");
                modelAndView.addObject("puntaje", params.get("puntaje").toString());
            }
            else
            {
                reportNIC34Service.loadAudit(user, "Procesamiento Fallido NIC34 EEFF PYG Consolidado");
                modelAndView.addObject("resp", "ExecNIC34-3");
            }

        }
        catch (Exception e){
            e.printStackTrace();
            reportNIC34Service.loadAudit(user, "Procesamiento fallido NIC34 Consolidado");
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34ConsolPyg/download")
    @ResponseBody
    public void exportExcelPygConsol(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
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

    @GetMapping(value="/reportNIC34/nic34ConsolNotas")
    public ModelAndView showReportNIC34NotasConsol(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver EEFF NIC34 Notas")) {

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
                modelAndView.addObject("directory", "nic34ConsolNotas");
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
            modelAndView.setViewName("reportNIC34/nic34ConsolNotas");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34ConsolNotas/processData")
    public ModelAndView processDataBaseNotas(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reportNIC34/nic34ConsolNotas");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("corte", params.get("corte").toString());

            if(reportNIC34Service.getDataBaseBalance(params.get("corte").toString(),params.get("period").toString()).size()==2)
            {
                reportNIC34Service.generateDataNotas(params.get("corte").toString(),params.get("period").toString(),params.get("puntaje").toString(),params.get("aplica").toString());
                reportNIC34Service.getDataFilNotas(params.get("corte").toString(),params.get("period").toString(),params.get("aplica").toString(),null,null);
                reportNIC34Service.loadAudit(user, "Procesamiento Exitoso NIC34 EEFF Notas Consolidado");
                modelAndView.addObject("resp", "ExecNIC341");
                modelAndView.addObject("puntaje", params.get("puntaje").toString());
                modelAndView.addObject("aplica", params.get("aplica").toString());
            }
            else
            {
                reportNIC34Service.loadAudit(user, "Procesamiento Fallido NIC34 EEFF Balance Consolidado");
                modelAndView.addObject("resp", "ExecNIC34-2");
            }

        }
        catch (Exception e){
            e.printStackTrace();
            reportNIC34Service.loadAudit(user, "Procesamiento fallido NIC34 Consolidado");
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/reportNIC34/nic34ConsolNotas/download")
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

    @GetMapping(value="/reportNIC34/nic34ConsolNotas/searchNic34")
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
        modelAndView.addObject("directory", "nic34ConsolNotas/searchNic34");
        modelAndView.setViewName("reportNIC34/nic34ConsolNotas");

        return modelAndView;
    }

}
