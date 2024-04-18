package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Desconnv15;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.Desconnv15ListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.Desconnv15Service;
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
public class Desconnv15Controller {

    private static final int PAGINATIONCOUNT = 12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private Desconnv15Service desconnv15Service;


    private List<String> listColumns = List.of("No Filtrar","Cuenta", "Divisa", "Observaciones", "Tp", "Tp1");

    @GetMapping(value = "/ifrs/desconnv15")
    public ModelAndView showTemplateDesconnv15(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Descontabilización Nivel 15")) {

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

        String todayString="";

        if(params.get("period")==null || params.get("period").toString()=="") {
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
        List<Desconnv15> desconnv15List = desconnv15Service.getdesconnv15(todayString);
        modelAndView.addObject("period",todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), desconnv15List.size());
        Page<Desconnv15> pageDesconnv15 = new PageImpl<>(desconnv15List.subList(start, end), pageRequest, desconnv15List.size());

        int totalPage=pageDesconnv15.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        if(params.get("vFilter")==null||(params.get("vFilter").toString()).equals("Original") ||(params.get("vFilter").toString()).equals("") ||(params.get("vFilter").toString()).equals("No Filtrar")) {
            modelAndView.addObject("vFilter","No Filtrar");
        }

        modelAndView.addObject("allTemplateDesconnv15",pageDesconnv15.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","desconnv15");
        modelAndView.addObject("registers",desconnv15List.size());

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/desconnv15");
        }
            else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/ifrs9/descon15/upload")
    public ModelAndView uploadDescon(@RequestParam Map<String, Object> params){
        String period = params.get("fileDate").toString();

        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/desconnv15");

        try{
            desconnv15Service.loadDescon(period);
        }catch (Exception e){
            e.printStackTrace();
        }

        modelAndView.addObject("period", period);

        return modelAndView;

    }

    @GetMapping(value = "/ifrs/desconnv15/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";

        if(params.get("period")==null || params.get("period").toString()=="") {
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
        String headerValue = "attachment; filename=Desconv15_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Desconnv15> desconnv15List;
        if(params.get("vFilter")==null||(params.get("vFilter").toString()).equals("Original") ||(params.get("vFilter").toString()).equals("") ||(params.get("vFilter").toString()).equals("No Filtrar")) {
            desconnv15List = desconnv15Service.getdesconnv15(todayString);
        }
        else{
            desconnv15List = desconnv15Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),todayString);
        }
        Desconnv15ListReport listReport = new Desconnv15ListReport(desconnv15List);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/searchDesconnv15")
    @ResponseBody
    public ModelAndView searchDesconnv15(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String todayString="";

        if(params.get("period")==null || params.get("period").toString()=="") {
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
        modelAndView.addObject("period",todayString);

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

        List<Desconnv15> list;
        String filtro = "";
        if(params.get("vFilter")==null|| (params.get("vFilter").toString()).equals("Original") || (params.get("vFilter").toString()).equals("") ||(params.get("vFilter").toString()).equals("No Filtrar")) {
            list = desconnv15Service.getdesconnv15(todayString);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("vFilter","No Filtrar");
        }
        else{
            list = desconnv15Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),params.get("period").toString());
            modelAndView.addObject("vId",params.get("vId").toString());
            modelAndView.addObject("vFilter",params.get("vFilter").toString());
        }


        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Desconnv15> pageSubsidiaries = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageSubsidiaries.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allTemplateDesconnv15",pageSubsidiaries.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchDesconnv15");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/desconnv15");
        return modelAndView;
    }
}