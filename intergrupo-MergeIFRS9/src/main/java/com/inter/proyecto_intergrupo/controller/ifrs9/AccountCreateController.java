package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.ControlPanelIfrs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreation;
import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreationPlane;
import com.inter.proyecto_intergrupo.model.ifrs9.Anexo;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelIfrsService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.AccountCreationListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.AccountCreationService;
import com.inter.proyecto_intergrupo.service.parametricServices.CountryService;
import com.inter.proyecto_intergrupo.service.parametricServices.GarantBankListReport;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class AccountCreateController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private AccountCreationService accountCreationService;

    @Autowired
    private ControlPanelIfrsService controlPanelService;

    private List<String> listColumns=List.of("NIT", "Nombre Banco Real", "Nombre Similar","País");

    @GetMapping(value="/ifrs/accountCreation")
    public ModelAndView showAccountCreation(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (General)")) {

            int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            List<Object[]> list = accountCreationService.findAllResumeView("GENERAL");

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageAccountCreation = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageAccountCreation.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            String resultData = accountCreationService.validateNext("GENERAL");

            if (resultData.equals("OK")) {
                modelAndView.addObject("resp", "confirmDataR");
            } else {
                modelAndView.addObject("resp", "confirmDataR-1");
            }

            List<ControlPanelIfrs> listControl = controlPanelService.findByCPI(user);

            int countFull = 0;

            for (int i = 0; i < listControl.size(); i++) {
                if (listControl.get(i).getSemaforoComponente().equals("FULL")) {
                    countFull++;
                }
            }

            if (countFull >= 6 ){
                modelAndView.addObject("statusComponent", true);
            } else {
                modelAndView.addObject("statusComponent", false);
            }

            modelAndView.addObject("allAccountCreation", pageAccountCreation.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("perfil", "GENERAL");
            modelAndView.addObject("nextp", resultData);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "accountCreation");
            modelAndView.addObject("registers",pageAccountCreation.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/accountCreation");
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Gestión)")) {

            int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            List<Object[]> list = accountCreationService.findAllResumeView("GESTION");

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageAccountCreation = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageAccountCreation.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            String resultData = accountCreationService.validateNext("GESTION");

            if (resultData.equals("OK")) {
                modelAndView.addObject("resp", "confirmDataR");
            } else {
                modelAndView.addObject("resp", "confirmDataR-1");
            }
            modelAndView.addObject("statusComponent", false);

            modelAndView.addObject("allAccountCreation", pageAccountCreation.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("perfil", "GESTION");
            modelAndView.addObject("nextp", resultData);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "accountCreation");
            modelAndView.addObject("registers",pageAccountCreation.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/accountCreation");
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Consolidación)")) {

            int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            List<Object[]> list = accountCreationService.findAllResumeView("CONSOLIDACION");

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageAccountCreation = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageAccountCreation.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            String resultData = accountCreationService.validateNext("CONSOLIDACION");

            if (resultData.equals("OK")) {
                modelAndView.addObject("resp", "confirmDataR");
            } else {
                modelAndView.addObject("resp", "confirmDataR-1");
            }

            modelAndView.addObject("statusComponent", false);

            modelAndView.addObject("allAccountCreation", pageAccountCreation.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("nextp", resultData);
            modelAndView.addObject("perfil", "CONSOLIDACION");
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "accountCreation");
            modelAndView.addObject("registers",pageAccountCreation.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/accountCreation");
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Control Contable)")) {

            int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            List<Object[]> list = accountCreationService.findAllResumeView("CONTROL CONTABLE");

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageAccountCreation = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageAccountCreation.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            String resultData = accountCreationService.validateNext("CONTROL CONTABLE");

            if (resultData.equals("OK")) {
                modelAndView.addObject("resp", "confirmDataR");
            } else {
                modelAndView.addObject("resp", "confirmDataR-1");
            }

            modelAndView.addObject("statusComponent", false);

            modelAndView.addObject("allAccountCreation", pageAccountCreation.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("perfil", "CONTROL CONTABLE");
            modelAndView.addObject("nextp", resultData);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "accountCreation");
            modelAndView.addObject("registers",pageAccountCreation.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/accountCreation");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/accountCreation")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreation");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String perfil ="";

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (General)"))
        {
            perfil = "GENERAL";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Gestión)"))
        {
            perfil = "GESTION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Consolidación)"))
        {
            perfil = "CONSOLIDACION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Control Contable)"))
        {
            perfil = "CONTROL CONTABLE";
        }

        List<AccountCreation> accountCreationList = accountCreationService.findAll();

        try {
            if(accountCreationService.validateNext(perfil).equals("NEXT") || perfil.equals("GENERAL")) {
                Part filePart = request.getPart("file");
                InputStream fileContent = filePart.getInputStream();
                AccountCreationListReport listReport = new AccountCreationListReport(accountCreationList);
                ArrayList<String[]> list = accountCreationService.saveFileBD(fileContent, user, perfil);
                String[] part = list.get(0);

                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                listReport.exportLog(response, list);
            }
            else
            {
                modelAndView.addObject("resp", "loadFail-2");
            }
        }catch(Exception e){
            e.printStackTrace();
            //return  modelAndView;
        }
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/accountCreation/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CreaciónCuentas" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String perfil="";
        if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Gestión)"))
        {
            perfil = "GESTION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Consolidación)"))
        {
            perfil = "CONSOLIDACION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Control Contable)"))
        {
            perfil = "CONTROL CONTABLE";
        }

        List<Object[]> accountCreateList= accountCreationService.findAllResumeView(perfil);
        AccountCreationListReport listReport = new AccountCreationListReport(accountCreateList,1);
        listReport.export(response,perfil);
    }

    @GetMapping(value = "/ifrs/accountCreation/downloadAll")
    @ResponseBody
    public void exportToExcelAll(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CreaciónCuentas" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (General)"))
        {
            List<AccountCreation> accountCreateList = accountCreationService.findAll();
            List<String> accountCreateListNOT = accountCreationService.listAccountCreateNOTIN();
            List<String> accountCreateListIN = accountCreationService.listAccountCreateIN();
            AccountCreationListReport listReport = new AccountCreationListReport(accountCreateList,accountCreateListNOT,accountCreateListIN);
            listReport.exportAllG(response);
        }
        else {
            List<AccountCreation> accountCreateList = accountCreationService.findAll();
            AccountCreationListReport listReport = new AccountCreationListReport(accountCreateList);
            listReport.exportAll(response);
        }
    }

    @GetMapping(value = "/ifrs/accountCreation/downloadPlane")
    @ResponseBody
    public void exportToExcelPlane(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CreaciónCuentas" + currentDateTime + ".txt";
        response.setHeader(headerKey, headerValue);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        List<AccountCreationPlane> accountCreateList= accountCreationService.findAllBusiness();
        CsvService.downloadCsvCreationAccount(response.getWriter(), accountCreateList);
    }

    @GetMapping(value="/ifrs/accountCreation/confirmData")
    @ResponseBody
    public ModelAndView confirmInputR(@RequestParam Map<String, Object> params,HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreation");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String perfil ="";
        if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (General)"))
        {
            perfil = "GENERAL";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Gestión)"))
        {
            perfil = "GESTION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Consolidación)"))
        {
            perfil = "CONSOLIDACION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Control Contable)"))
        {
            perfil = "CONTROL CONTABLE";
        }

        boolean resultData = accountCreationService.confirmData(perfil,user,request);

        if (resultData) {
            modelAndView.addObject("resp", "confirmDataR");
        } else {
            modelAndView.addObject("resp", "confirmDataC-"+perfil);
        }



        return modelAndView;
    }

    @GetMapping(value="/ifrs/accountCreation/generateStagesR")
    @ResponseBody
    public ModelAndView generateStagesR(@RequestParam Map<String, Object> params,HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreation");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String perfil ="";
        if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (General)"))
        {
            perfil = "GENERAL";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Gestión)"))
        {
            perfil = "GESTION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Consolidación)"))
        {
            perfil = "CONSOLIDACION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Control Contable)"))
        {
            perfil = "CONTROL CONTABLE";
        }

        boolean resultData = accountCreationService.creationStagesR(perfil,user,request);

        if (resultData) {
            modelAndView.addObject("resp", "stageDataC");
        } else {
            modelAndView.addObject("resp", "stageDataC-1");
        }



        return modelAndView;
    }

    @GetMapping(value="/ifrs/accountCreation/generateStagesC")
    @ResponseBody
    public ModelAndView generateStagesC(@RequestParam Map<String, Object> params,HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreation");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String perfil ="";
        if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (General)"))
        {
            perfil = "GENERAL";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Gestión)"))
        {
            perfil = "GESTION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Consolidación)"))
        {
            perfil = "CONSOLIDACION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Control Contable)"))
        {
            perfil = "CONTROL CONTABLE";
        }

        boolean resultData = accountCreationService.creationStagesC(perfil,user,request);

        if (resultData) {
            modelAndView.addObject("resp", "stageDataC");
        } else {
            modelAndView.addObject("resp", "stageDataC-1");
        }



        return modelAndView;
    }

    @GetMapping(value="/ifrs/accountCreation/updateControl")
    @ResponseBody
    public ModelAndView updateC(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreation");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String perfil ="";
        if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (General)"))
        {
            perfil = "GENERAL";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Gestión)"))
        {
            perfil = "GESTION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Consolidación)"))
        {
            perfil = "CONSOLIDACION";
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas (Control Contable)"))
        {
            perfil = "CONTROL CONTABLE";
        }

        boolean resultData = accountCreationService.updateDataControl();

        if (resultData) {
            modelAndView.addObject("resp", "updateDataC");
        } else {
            modelAndView.addObject("resp", "updateDataC-1");
        }



        return modelAndView;
    }

    @GetMapping(value = "/ifrs/clearAccountCreation")
    public ModelAndView clearAccountCreation(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        accountCreationService.clearAccountCreation("ALL");
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreation");
        return  modelAndView;
    }

}
