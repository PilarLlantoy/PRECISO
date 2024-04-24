package com.inter.proyecto_intergrupo.controller.reports;

import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Subsidiaries;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.QueryService;
import com.inter.proyecto_intergrupo.service.parametricServices.SubsidiariesListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.ReconciliationService;
import com.inter.proyecto_intergrupo.service.reportsServices.ReconciliationListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.ReconciliationDiffListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.ReconciliationFilListReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ReconciliationController {

    private static final int PAGINATIONCOUNT=20;

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private QueryService queryService;

    @Autowired
    private ReconciliationService ReconciliationService;

    @Autowired
    private ControlPanelService controlPanelService;

    @GetMapping(value="/reports/reconciliationIntV1")
    public ModelAndView showReconciliation(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Intergrupo - Query")) {

            String todayString = "";

            if (params.get("period").toString() == "") {
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
                } else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
                }
            } else {
                todayString = params.get("period").toString();
            }

            List<Object[]> companies = ReconciliationService.getCompany(todayString);
            boolean noQuery = false;
            if (companies.size() == 0) {
                noQuery = false;
            } else {
                noQuery = true;
            }

            List<Object[]> reconciliation = new ArrayList<>();
            List<Object[]> cdm = ReconciliationService.getCdM(todayString);

            modelAndView.addObject("reconciliation", reconciliation);
            modelAndView.addObject("cdm", cdm);
            modelAndView.addObject("companies", companies);
            modelAndView.addObject("itsFine", false);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("isPost", false);
            modelAndView.addObject("noQuery", noQuery);
            modelAndView.addObject("empty",false);

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("reports/reconciliationV1");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }


    @PostMapping(value="/reports/reconciliationIntV1")
    public ModelAndView runReconciliation(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {

        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        response.setContentType("application/octet-stream");

        String todayString="";
        String company="";
        String account="";
        String queryV="";
        String interV="";

        if(params.get("period").toString()==null || params.get("period").toString()=="") {
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

        company=params.get("company").toString();
        account=params.get("account").toString();

        queryV=params.get("queryV").toString();
        interV=params.get("interV").toString();

        List<Object[]> reconciliation;

        if(account.equals("local")){
            reconciliation=ReconciliationService.reconciliationIntergroupV1(todayString, company, queryV, interV, "diff");
        }else{
            ReconciliationService.reconciliationIntergroupUpdateNeocon(todayString);
            reconciliation=ReconciliationService.reconciliationIntergroupV1Neocon(todayString, company, queryV, interV, "diff");
        }
        List<Object[]> companies=ReconciliationService.getCompany(todayString);

        List<Object[]> cdm=ReconciliationService.getCdM(todayString);

        if(reconciliation.isEmpty()){
            modelAndView.addObject("empty",true);
        }

        modelAndView.addObject("reconciliation",reconciliation);
        modelAndView.addObject("companies",companies);
        modelAndView.addObject("cdm",cdm);

        if (reconciliation.size() >  0){
            modelAndView.addObject("itsFine",false);
        }else{
            modelAndView.addObject("itsFine",true);
        }

        boolean noQuery = false;
        if (companies.size() == 0){
            noQuery = false;
        }else{
            noQuery = true;
        }

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("reconciliation",reconciliation);
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("company",company);
        modelAndView.addObject("account",account);
        modelAndView.addObject("queryV",queryV);
        modelAndView.addObject("interV",interV);
        modelAndView.addObject("isPost", true);
        modelAndView.addObject("noQuery", noQuery);
        modelAndView.setViewName("reports/reconciliationV1");

        return modelAndView;
    }

    @PostMapping(value="/reports/reconciliationIntV1/download")
    public void runReconciliationDownload(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        response.setContentType("application/octet-stream");

        String todayString="";
        String company="";
        String account="";
        String queryV="";
        String interV="";

        if(params.get("period").toString()==null || params.get("period").toString()=="") {
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

        company=params.get("company").toString();
        account=params.get("account").toString();
        queryV=params.get("queryV").toString();
        interV=params.get("interV").toString();

        List<Object[]> reconciliation;

        if(account.equals("local")){
            reconciliation=ReconciliationService.reconciliationIntergroupV1(todayString, company, queryV, interV, "all");
        }else{
            reconciliation=ReconciliationService.reconciliationIntergroupV1Neocon(todayString, company, queryV, interV, "all");
        }

        //Export parameters

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ConciliacionIntergrupoV1_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        ReconciliationDiffListReport listReport = new ReconciliationDiffListReport(reconciliation,1);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/reconciliationIntV1/download")
    @ResponseBody
    public void exportToExcelReport(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";

        if(params.get("period").toString()=="") {
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

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=IntergrupoV1_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV1> intV1= new ArrayList<>();
        intV1 = ReconciliationService.getIntergroupV1(todayString);
        ReconciliationListReport listReport = new ReconciliationListReport(intV1,1);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/reconciliationIntV1/downloadDiff")
    @ResponseBody
    public void exportToExcelReportDiff(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";
        String company="";
        String account="";
        String queryV="";
        String interV="";

        if(params.get("period").toString()=="") {
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

        company=params.get("company").toString();
        account=params.get("account").toString();
        queryV=params.get("queryV").toString();
        interV=params.get("interV").toString();

        List<Object[]> reconciliation;

        if(account.equals("local")){
            reconciliation=ReconciliationService.reconciliationIntergroupV1(todayString, company, queryV, interV, "all");
        }else{
            reconciliation=ReconciliationService.reconciliationIntergroupV1Neocon(todayString, company, queryV, interV, "all");
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=IntergrupoV1_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ReconciliationDiffListReport listReport = new ReconciliationDiffListReport(reconciliation,1);
        listReport.export(response);
    }

    @GetMapping(value="/reports/reconciliationIntV1Fil")
    public ModelAndView showReconciliationFil(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Intergrupo Banco - Filiales")) {

            String todayString = "";
            String yntpSelect = "";
            String levelSelect = "";
            String versionSelect = "";

            if (params.get("period").toString() == null || params.get("period").toString() == "") {
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
                } else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
                }
            } else {
                todayString = params.get("period").toString();
            }

            if (params.get("yntp") == null) {
                yntpSelect = "ALL";
            } else {
                yntpSelect = params.get("yntp").toString();
            }

            if (params.get("level") == null) {
                levelSelect = "D";
            } else {
                levelSelect = params.get("level").toString();
            }

            if (params.get("version") == null) {
                versionSelect = "v1";
            } else {
                versionSelect = params.get("version").toString();
            }


            //

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            List<Object[]> list = ReconciliationService.reconciliationIntergroupV1FilDownload(todayString, yntpSelect, levelSelect);
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageYntp = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageYntp.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("reconciliation", pageYntp.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);

            //

            List<Object[]> yntp = ReconciliationService.getYNTPFil();
            modelAndView.addObject("yntps", yntp);
            modelAndView.addObject("yntp", yntpSelect);
            modelAndView.addObject("level", levelSelect);
            modelAndView.addObject("version", versionSelect);
            modelAndView.addObject("directory", "reconciliationIntV1Fil");

            modelAndView.addObject("period", todayString);
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("reports/reconciliationV1Fil");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value="/reports/reconciliationIntV1Save")
    public ModelAndView showReconciliationSave(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/reconciliationIntV1Fil");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String todayString="";

        if(params.get("period").toString()==null || params.get("period").toString()=="") {
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

        String yntpSelect = "";

        if(params.get("yntp") == null){
            yntpSelect = "ALL";
        }else{
            yntpSelect = params.get("yntp").toString();
        }

        String levelSelect = "";

        if (params.get("level") == null) {
            levelSelect = "D";
        } else {
            levelSelect = params.get("level").toString();
        }

        String versionSelect = "";

        if (params.get("version") == null) {
            versionSelect = "v1";
        } else {
            versionSelect = params.get("version").toString();
        }

        ReconciliationService.reconciliationIntergroupV1Fil(todayString, versionSelect);
        List<Object[]> reconciliation;
        reconciliation=ReconciliationService.reconciliationIntergroupV1FilDownload(todayString, yntpSelect, levelSelect);

        modelAndView.addObject("reconciliation",reconciliation);

        List<Object[]> yntp=ReconciliationService.getYNTPFil();
        modelAndView.addObject("yntps",yntp);
        modelAndView.addObject("yntp",yntpSelect);
        modelAndView.addObject("level", levelSelect);
        modelAndView.addObject("version", versionSelect);
        modelAndView.addObject("resp", "ReconSave");
        modelAndView.addObject("period",todayString);
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());

        return modelAndView;
    }

    @PostMapping(value="/reports/reconciliationIntV1/reconJust")
    @ResponseBody
    public ModelAndView updateReconJust(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/reconciliationIntV1Fil");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String todayString="";

        if(params.get("period").toString()==null || params.get("period").toString()=="") {
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

        String yntpSelect = "";

        if(params.get("yntp") == null){
            yntpSelect = "ALL";
        }else{
            yntpSelect = params.get("yntp").toString();
        }

        String levelSelect = "";

        if (params.get("level") == null) {
            levelSelect = "D";
        } else {
            levelSelect = params.get("level").toString();
        }

        if(params.get("idConc") == null){
            modelAndView.addObject("resp", "JustSave-0");
        }else{
            if(params.get("just") != null){
                ReconciliationService.updateReconJust(params.get("idConc").toString(), params.get("just").toString(), todayString);
                modelAndView.addObject("resp", "JustSave-1");
            }
        }

        List<Object[]> reconciliation;
        reconciliation=ReconciliationService.reconciliationIntergroupV1FilDownload(todayString, yntpSelect, levelSelect);
        modelAndView.addObject("reconciliation",reconciliation);

        List<Object[]> yntp=ReconciliationService.getYNTPFil();
        modelAndView.addObject("yntps",yntp);
        modelAndView.addObject("yntp",yntpSelect);
        modelAndView.addObject("level", levelSelect);
        modelAndView.addObject("period",todayString);
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());

        return modelAndView;
    }

    @GetMapping(value = "/reports/reconciliationIntV1/downloadRecon")
    @ResponseBody
    public void exportToExcelReportFil(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";

        if(params.get("period").toString()=="") {
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

        String yntpSelect = "";

        if(params.get("yntp") == null){
            yntpSelect = "ALL";
        }else{
            yntpSelect = params.get("yntp").toString();
        }

        String levelSelect = "";

        if (params.get("level") == null) {
            levelSelect = "D";
        } else {
            levelSelect = params.get("level").toString();
        }

        List<Object[]> reconciliation;
        List<Object[]> reconciliationAcc;

        reconciliation=ReconciliationService.reconciliationIntergroupV1FilDownload(todayString, yntpSelect, levelSelect);
        reconciliationAcc=ReconciliationService.reconciliationIntergroupV1FilDownloadDiff();

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ConciliaciónV1_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ReconciliationFilListReport listReport = new ReconciliationFilListReport(reconciliation,reconciliationAcc, levelSelect);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/reconciliationIntV1/downloadFaltantes")
    @ResponseBody
    public void exportToExcelReportFilFaltantes(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";

        if(params.get("period").toString()=="") {
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

        List<Subsidiaries> reconciliation;

        reconciliation=ReconciliationService.reconciliationIntergroupV1FilDownloadR(todayString);

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CuentasFaltantes" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        SubsidiariesListReport listReport = new SubsidiariesListReport(reconciliation);
        listReport.export(response);
    }

}
