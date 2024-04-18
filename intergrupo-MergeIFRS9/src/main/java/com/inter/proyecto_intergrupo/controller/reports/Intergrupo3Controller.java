package com.inter.proyecto_intergrupo.controller.reports;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.PlainIFRS9Intergroup;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.YntpSocietyRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.reportsServices.*;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
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

import javax.servlet.ServletException;
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
public class Intergrupo3Controller {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    private List<String> listColumns = List.of("Cuenta", "Contrato", "Nit", "Cod Neocon", "Divisa");

    @Autowired
    private UserService userService;

    @Autowired
    private Intergrupo2Service intergrupo2Service;

    @Autowired
    private Intergrupo1Service intergrupo1Service;

    @Autowired
    Intergrupo3Service intergrupo3;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @GetMapping(value="/reports/intergroup3")
    public ModelAndView showInterV3(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Intergrupo 3 Banco")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";
            String Id;
            String Vf;
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

            if (params.get("period") == null || params.get("period").toString() == "") {
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

            List<IntergrupoV3> list;
            if (Id == "" | Vf == "") {
                list = intergrupo3.getAllFromV3(todayString);
            } else {
                list = intergrupo3.findByFilter(Id, Vf, todayString);
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<IntergrupoV3> pageInter = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageInter.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            List<Object[]> neocon = intergrupo3.getNeoconInter(todayString);

            List<Object[]> validateProv = intergrupo3.validateTableIntProv(todayString);
            List<Object[]> validateRec = intergrupo3.validateTableIntRec(todayString);

            if (!validateProv.isEmpty()) {
                modelAndView.addObject("validateInterProv", true);
                Date dateProv = new Date();
                try {
                    dateProv = StatusInfoRepository.findByInputAndPeriodo("PLANOINTERGRUPO-PROV", todayString).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateProv);
                modelAndView.addObject("dateProv", dateAsString);
            } else {
                modelAndView.addObject("validateInterProv", false);
            }

            if (!validateRec.isEmpty()) {
                modelAndView.addObject("validateInterRec", true);
                Date dateRec = new Date();
                try {
                    dateRec = StatusInfoRepository.findByInputAndPeriodo("PLANOINTERGRUPO-REC", todayString).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateRec);
                modelAndView.addObject("dateRec", dateAsString);
            } else {
                modelAndView.addObject("validateInterRec", false);
            }

            List<Object[]> validateInterV3 = intergrupo3.validateTableInterV3Val(todayString);

            if (!validateInterV3.isEmpty()) {
                modelAndView.addObject("validateInterV3", true);
                Date dateInterV3 = new Date();
                try {
                    dateInterV3 = StatusInfoRepository.findByInputAndPeriodo("VALIDATE_INTER_V3", todayString).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateInterV3);
                modelAndView.addObject("dateInterV3", dateAsString);
            } else {
                modelAndView.addObject("validateInterV3", false);
            }

            List<Object[]> validateInterAjuV3 = intergrupo3.validateTableInterV3Aju(todayString);

            if (!validateInterAjuV3.isEmpty()) {
                modelAndView.addObject("validateInterAjuV3", true);
                Date dateInterAjuV3 = new Date();
                try {
                    dateInterAjuV3 = StatusInfoRepository.findByInputAndPeriodo("INTER_V3_AJU", todayString).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateInterAjuV3);
                modelAndView.addObject("dateInterAjuV3", dateAsString);
            } else {
                modelAndView.addObject("validateInterAjuV3", false);
            }

            modelAndView.addObject("neocon", neocon);
            modelAndView.addObject("intergroup", pageInter.getContent());
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("vId", Id);
            modelAndView.addObject("vFilter", Vf);
            modelAndView.addObject("directory", "intergroup3");
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("reports/intergroup3");

        }
        else
        {
            modelAndView.addObject("anexo","/home");
           modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/reports/intergroup3/download")
    public void downloadIntergrupoV3(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= IntergrupoV3_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV3> intList = intergrupo3.getAllFromV3(params.get("period").toString());

        Intergrupo3ListReport listReport = new Intergrupo3ListReport(intList);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/intergroup3/downloadC")
    public void downloadCIntergrupoV3(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= CruceIntergrupoV3_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV3Temp> intList = intergrupo3.getAllFromV3Cruce(params.get("period").toString());

        Intergrupo3CListReport listReport = new Intergrupo3CListReport(intList);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/intergroup3/downloadPlanos")
    public void downloadPlanosIntergrupoV3(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= PlanosIntergrupo_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        //List<PlainIFRS9Intergroup> intList = intergrupo3.getAllFromV3Planos(params.get("period").toString());
        List<Object[]> intList = intergrupo3.getAllFromV3PlanosObject(params.get("period").toString());

        Intergrupo3PListReport listReport = new Intergrupo3PListReport(intList);
        listReport.export(response);
    }

    @PostMapping(value="/reports/sendIntergroup3")
    public ModelAndView sendIntergroup(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup3");
        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String todayString="";

        if(params.get("period") == null || params.get("period").toString()=="") {
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

        intergrupo3.insertV3(todayString,user);

        modelAndView.addObject("period", todayString);
        modelAndView.addObject("resp","inter-3");

        return modelAndView;
    }

    @GetMapping(value="/reports/sendIntergroup3P2")
    public ModelAndView sendIntergroupP2(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup3");
        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String todayString="";

        if(params.get("period") == null || params.get("period").toString()=="") {
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

        intergrupo3.insertV3P2(todayString,user);

        modelAndView.addObject("period", todayString);
        modelAndView.addObject("resp","inter-3");

        return modelAndView;
    }

    @GetMapping(value="/reports/sendIntergroup3P1")
    public void sendIntergroupP1(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String todayString="";

        if(params.get("period") == null || params.get("period").toString()=="") {
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
        intergrupo3.insertV3P1(todayString,user);
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= IntergrupoV3_Validacion_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV3Valida> intList = intergrupo3.getAllInterValidacionV3Temp(params.get("period").toString());

        Intergrupo3ListReport listReport = new Intergrupo3ListReport(null);
        listReport.exportValida(response, intList);
    }

    @PostMapping(value="/reports/intergroup3/loadFinal")
    public ModelAndView uploadFileFinal(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup3");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

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

        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logIntergrupoFinalV3_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            List<String[]> list = intergrupo3.saveFileFinalIntergrupo(fileContent, user, todayString);
            String[] part=list.get(list.size()-1);
            if(part[2].equals("COMPLETE"))
            {
                Date today = new Date();
                StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo("INTER_V3_AJU", todayString);

                if (validateStatus == null) {
                    StatusInfo status = new StatusInfo();
                    status.setInput("INTER_V3_AJU");
                    status.setPeriodo(todayString);
                    status.setFecha(today);
                    StatusInfoRepository.save(status);
                } else {
                    validateStatus.setFecha(today);
                    StatusInfoRepository.save(validateStatus);
                }

                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else
            {
                Intergrupo1ListReport report = new Intergrupo1ListReport(null);
                report.exportLog(response, list);
            }

        }catch(Exception e){
            modelAndView.addObject("resp", "Descon-3");
            e.printStackTrace();
        }
        return  modelAndView;
    }

    @GetMapping(value = "/reports/intergroup3/downloadFinal")
    public void downloadIntergrupoV3F(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Ajustes_IntergrupoV3_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV3Final> intList = intergrupo3.getAllFromV3Final(params.get("period").toString());

        Intergrupo3ListReport listReport = new Intergrupo3ListReport(null);
        listReport.exportFinal(response, intList);
    }

    @GetMapping(value = "/reports/intergroup3/downloadFinalAjustes")
    public void downloadIntergrupoV3FAju(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= IntergrupoV3ConAjustes_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV3> intList = intergrupo3.getAllFromV3FinalAju(params.get("period").toString());

        Intergrupo3ListReport listReport = new Intergrupo3ListReport(intList);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/intergroup3/processInterV3")
    public ModelAndView processIntergroupV3(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup3");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            intergrupo3.processAjuInterV3(params.get("period").toString());
            intergrupo3.auditCode("Procesamiento Exitoso Ajustes Intergrupo V3", user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            e.printStackTrace();
            intergrupo3.auditCode("Procesamiento Fallido Ajustes Intergrupo V3", user);
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/reports/intergroup3/downloadValidacionInterV3")
    public void downloadValIntergrupoV3(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= IntergrupoV3_Validación_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV3Valida> intList = intergrupo3.getAllInterValidacionV3(params.get("period").toString());

        Intergrupo3ListReport listReport = new Intergrupo3ListReport(null);
        listReport.exportValida(response, intList);
    }

    @PostMapping(value="/reports/intergroup3/loadValidationInterV3")
    public ModelAndView uploadFileValeInterV3(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup3");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

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

        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logIntergrupoValV3_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            List<String[]> list = intergrupo3.saveFileValidateIntergrupoV3(fileContent, user, todayString);
            String[] part=list.get(list.size()-1);
            if(part[2].equals("COMPLETE"))
            {
                Date today = new Date();
                StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo("VALIDATE_INTER_V3", todayString);

                if (validateStatus == null) {
                    StatusInfo status = new StatusInfo();
                    status.setInput("VALIDATE_INTER_V3");
                    status.setPeriodo(todayString);
                    status.setFecha(today);
                    StatusInfoRepository.save(status);
                } else {
                    validateStatus.setFecha(today);
                    StatusInfoRepository.save(validateStatus);
                }

                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else
            {
                Intergrupo1ListReport report = new Intergrupo1ListReport(null);
                report.exportLog(response, list);
            }

        }catch(Exception e){
            modelAndView.addObject("resp", "Descon-3");
            e.printStackTrace();
        }
        return  modelAndView;
    }

}
