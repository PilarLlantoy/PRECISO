package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.RiskAccount;
import com.inter.proyecto_intergrupo.model.ifrs9.RiskAccountFinal;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.RiskAccountListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.RiskAccountService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class RiskAccountController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private RiskAccountService riskAccountService;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    private List<String> listColumns=List.of("Stage Final","Cambio Segmento","Válida");

    @GetMapping(value="/ifrs/riskAccount")
    public ModelAndView showRiskAccount(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Contabilización Riesgos")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            String todayString="";
            if(params.get("period") == null || (params.get("period") != null && params.get("period").toString()=="")) {
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

            try {
                StatusInfo data1 = StatusInfoRepository.findByInputAndPeriodo("SEGMENTOS-RECHAZOS", todayString);
                if(data1!=null) {
                    Date dateSeg = data1.getFecha();
                    modelAndView.addObject("validateSeg", true);

                    String pattern = "dd/MM/yyyy HH:mm:ss";
                    DateFormat df = new SimpleDateFormat(pattern);
                    String dateAsString = df.format(dateSeg);
                    modelAndView.addObject("dateSeg", dateAsString);
                }
                else
                {
                    modelAndView.addObject("validateSeg", false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<Object[]> list = riskAccountService.findAllResume(todayString);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageRiskAccount = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageRiskAccount.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allRiskAccount", pageRiskAccount.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "riskAccount");
            modelAndView.addObject("registers",pageRiskAccount.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/riskAccount");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value="/ifrs/riskAccountLoad")
    public ModelAndView showRiskAccountLoad(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Riesgos Cargue Fichero")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            String todayString="";
            if(params.get("period") == null || (params.get("period") != null && params.get("period").toString()=="")) {
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
            List<Object[]> list = riskAccountService.findAllResumeLoad(todayString);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageRiskAccount = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageRiskAccount.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allRiskAccount", pageRiskAccount.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "riskAccountLoad");
            modelAndView.addObject("registers",pageRiskAccount.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/riskAccountLoad");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/riskAccountLoad")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/riskAccountLoad");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String todayString="";
        if(params.get("period") == null || (params.get("period") != null && params.get("period").toString()=="")) {
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
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> riskAccountList = riskAccountService.findAllResume(todayString);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            RiskAccountListReport listReport = new RiskAccountListReport(riskAccountList,1);
            ArrayList<String[]> list=riskAccountService.saveFileBD(fileContent,user,todayString);
            String[] part=list.get(0);

            modelAndView.addObject("resp", "AddRep1");
            modelAndView.addObject("row", part[0]);
            modelAndView.addObject("colum", part[1]);
            modelAndView.addObject("period", todayString);
            listReport.exportLog(response,list);
            response.sendRedirect("/ifrs/riskAccountLoad?period="+todayString);
            return  modelAndView;

        }catch(Exception e){
            e.printStackTrace();
            return  modelAndView;
        }
    }

    @GetMapping(value = "/ifrs/clearRiskAccount")
    public ModelAndView clearRiskAccount(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        riskAccountService.clearRiskAccount(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/riskAccount");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/clearRiskAccountLoad")
    public ModelAndView clearRiskAccountLoad(@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String[] dataParts=params.get("period").toString().split(":");

        riskAccountService.removeDataPeriod(dataParts[1]);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/riskAccountLoad?period="+dataParts[1]);
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/riskAccountGenerate")
    public ModelAndView loadDataRiskAccount(@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/riskAccount");
        try {
            riskAccountService.updateDataFinal(params.get("period").toString());
            modelAndView.addObject("resp", "updateDataC");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            modelAndView.addObject("resp", "updateDataC-1");
        }
        modelAndView.addObject("period", params.get("period").toString());
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/riskAccount/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";
        if(params.get("period") == null || (params.get("period") != null && params.get("period").toString()=="")) {
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
        String headerValue = "attachment; filename=Fichero_Ajustes_Riesgos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<RiskAccount> riskAccountList= new ArrayList<RiskAccount>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            riskAccountList = riskAccountService.findAll(todayString);
        }
        else{
            riskAccountList = riskAccountService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        List<Object[]> riskAccountListResumen = riskAccountService.findAllResumeLoad(todayString);
        RiskAccountListReport listReport = new RiskAccountListReport(riskAccountList);
        listReport.export(response,riskAccountListResumen);
    }

    @GetMapping(value = "/ifrs/riskAccount/downloadResume")
    @ResponseBody
    public void exportToExcelResume(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";
        if(params.get("period") == null || (params.get("period") != null && params.get("period").toString()=="")) {
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
        String headerValue = "attachment; filename=Riesgos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> riskAccountList = riskAccountList = riskAccountService.findAllResume(todayString);
        RiskAccountListReport listReport = new RiskAccountListReport(riskAccountList,1);
        listReport.exportResume(response);
    }

    @GetMapping(value = "/ifrs/riskAccount/downloadResumeLoad")
    @ResponseBody
    public void exportToExcelResumeLoad(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";
        if(params.get("period") == null || (params.get("period") != null && params.get("period").toString()=="")) {
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
        String headerValue = "attachment; filename=Riesgos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> riskAccountList = riskAccountList = riskAccountService.findAllResumeLoad(todayString);
        RiskAccountListReport listReport = new RiskAccountListReport(riskAccountList,1);
        listReport.exportResumeLoad(response);
    }

    @GetMapping(value = "/ifrs/searchRiskAccount")
    @ResponseBody
    public ModelAndView searchRiskAccount(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<RiskAccount> list=riskAccountService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<RiskAccount> pageRiskAccount = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageRiskAccount.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allRiskAccount",pageRiskAccount.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchRiskAccount");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/riskAccount");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/riskAccount/downloadCsv")
    @ResponseBody
    public void exportPlane(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, ParseException {
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        List<RiskAccountFinal> riesgos = riskAccountService.getDataCSV(params.get("period").toString());

        if(!riesgos.isEmpty() && riesgos.size()>0)
        {
            response.setHeader("Content-Disposition", "attachment;  filename=ACTUALIZA_STAGE_SEG" + ".txt");
        }
        else {
            response.setHeader("Content-Disposition", "attachment;  filename=ACTUALIZA_STAGE_SEG" + ".txt");
        }

        CsvService.downloadRiesgosCsv(response.getWriter(), riesgos);

    }
}
