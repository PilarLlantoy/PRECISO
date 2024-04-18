package com.inter.proyecto_intergrupo.controller.information;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Neocon60;
import com.inter.proyecto_intergrupo.model.information.Neocon60Ajuste;
import com.inter.proyecto_intergrupo.model.information.Neocon60Carga;
import com.inter.proyecto_intergrupo.model.information.Neocon60Cuadre;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.informationServices.Neocon60ListReport;
import com.inter.proyecto_intergrupo.service.informationServices.Neocon60Service;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamMDAListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamMDAService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
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
public class Neocon60Controller {
    private static final int PAGINATIONCOUNT=14;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private Neocon60Service neocon60Service;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @GetMapping(value="/information/neocon60")
    public ModelAndView showNeocon60(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Neocon 60")) {

            String todayString = "";
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

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            List<Neocon60> list = neocon60Service.findAll(todayString);

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Neocon60> pageData= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageData.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            List<Neocon60> validateBase = neocon60Service.findAllBase(todayString);

            if (!validateBase.isEmpty()) {
                modelAndView.addObject("validateBase", true);
                Date dateInterAjuV2 = new Date();
                try {
                    dateInterAjuV2 = StatusInfoRepository.findByInputAndPeriodo("NEOCON 60 BASE", todayString).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateInterAjuV2);
                modelAndView.addObject("dateBase", dateAsString);
            } else {
                modelAndView.addObject("validateBase", false);
            }

            List<Neocon60Ajuste> validateAjuste = neocon60Service.findAllAjuste(todayString);

            if (!validateAjuste.isEmpty()) {
                modelAndView.addObject("validateAjuste", true);
                Date dateInterAjuV2 = new Date();
                try {
                    dateInterAjuV2 = StatusInfoRepository.findByInputAndPeriodo("NEOCON 60 AJUSTE", todayString).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateInterAjuV2);
                modelAndView.addObject("dateAjuste", dateAsString);
            } else {
                modelAndView.addObject("validateAjuste", false);
            }

            List<Object[]> validateCarga = neocon60Service.findAllCarga(todayString);

            if (!validateCarga.isEmpty()) {
                modelAndView.addObject("validateCarga", true);
                Date dateInterAjuV2 = new Date();
                try {
                    dateInterAjuV2 = StatusInfoRepository.findByInputAndPeriodo("NEOCON 60 CARGA", todayString).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateInterAjuV2);
                modelAndView.addObject("dateCarga", dateAsString);
            } else {
                modelAndView.addObject("validateCarga", false);
            }

            modelAndView.addObject("allData", pageData.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("directory", "neocon60");
            modelAndView.addObject("registers",pageData.getTotalElements());
            modelAndView.addObject("period", todayString);
            modelAndView.setViewName("information/neocon60");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/information/neocon60")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/information/neocon60");
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
            ArrayList<String[]> list = neocon60Service.saveFileBD(fileContent,user,params.get("period").toString());
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
            }
            else{
                Neocon60ListReport neocon60ListReport = new Neocon60ListReport(list,null,null);
                neocon60ListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        modelAndView.addObject("period", params.get("period").toString());
        return  modelAndView;
    }

    @PostMapping(value="/information/neocon60Ajuste")
    public ModelAndView uploadFileAjuste(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/information/neocon60");
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
            ArrayList<String[]> list = neocon60Service.saveFileBDAjuste(fileContent,user,params.get("period").toString());
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
            }
            else{
                Neocon60ListReport neocon60ListReport = new Neocon60ListReport(list,null,null);
                neocon60ListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        modelAndView.addObject("period", params.get("period").toString());
        return  modelAndView;
    }

    @PostMapping(value="/information/neocon60CargaMasiva")
    public ModelAndView uploadFileCargaMasiva(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/information/neocon60");
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
            ArrayList<String[]> list = neocon60Service.saveFileBDCargaMasiva(fileContent,user,params.get("period").toString());
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
            }
            else{
                Neocon60ListReport neocon60ListReport = new Neocon60ListReport(list,null,null);
                neocon60ListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        modelAndView.addObject("period", params.get("period").toString());
        return  modelAndView;
    }

    @GetMapping(value="/information/neocon60/processAjustes")
    public ModelAndView processAjuste(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/information/neocon60");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        try {
            neocon60Service.processAjuste(params.get("period").toString());
            modelAndView.addObject("resp", "loadAjuste1");
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        modelAndView.addObject("period", params.get("period").toString());
        return  modelAndView;
    }

    @GetMapping(value = "/information/neocon60/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Neocon60_Unificado" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Neocon60> list = neocon60Service.findAll(params.get("period").toString());

        Neocon60ListReport listReport = new Neocon60ListReport(null,list,null);
        listReport.export(response);
    }

    @GetMapping(value = "/information/neocon60/downloadCuadre")
    @ResponseBody
    public void exportToExcelCuadre(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Neocon60_Cuadre" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Neocon60Cuadre> list = neocon60Service.findAllCuadre(params.get("period").toString());

        Neocon60ListReport listReport = new Neocon60ListReport(null,null,null);
        listReport.exportCuadre(response,list);
    }

    @GetMapping(value = "/information/neocon60/downloadTxt")
    @ResponseBody
    public void exportToTxt(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Neocon60_Unificado" + currentDateTime + ".txt";
        response.setHeader(headerKey, headerValue);
        List<Neocon60> list = neocon60Service.findAll(params.get("period").toString());

        CsvService.downloadTxtNeocon60(response.getWriter(), list);
    }

    @GetMapping(value = "/information/neocon60/downloadBase")
    @ResponseBody
    public void exportToExcelBase(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Neocon60_Base_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Neocon60> list = neocon60Service.findAllBase(params.get("period").toString());

        Neocon60ListReport listReport = new Neocon60ListReport(null,list,null);
        listReport.export(response);
    }

    @GetMapping(value = "/information/neocon60/downloadAjuste")
    @ResponseBody
    public void exportToExcelAjuste(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Neocon60_Ajuste" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Neocon60Ajuste> list = neocon60Service.findAllAjuste(params.get("period").toString());

        Neocon60ListReport listReport = new Neocon60ListReport(null,null,list);
        listReport.exportAjustado(response);
    }

    @GetMapping(value = "/information/neocon60/downloadCarga")
    @ResponseBody
    public void exportToExcelCarga(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Neocon60_Carga_Masiva" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> list = neocon60Service.findAllCarga(params.get("period").toString());

        Neocon60ListReport listReport = new Neocon60ListReport(null,null,null);
        listReport.exportCarga(response,list);
    }
}