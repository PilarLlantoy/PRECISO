package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Desconnv15;
import com.inter.proyecto_intergrupo.model.ifrs9.Informenv15;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.Desconnv15ListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.Desconnv15Service;
import com.inter.proyecto_intergrupo.service.ifrs9Services.Informenv15ListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.Informenv15Service;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class Informenv15Controller {

    private static final int PAGINATIONCOUNT = 12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private Informenv15Service informenv15Service;


    private List<String> listColumns = List.of("No Filtrar","Cuenta", "Divisa", "Observaciones", "Tp", "Tp1");

    @GetMapping(value = "/ifrs/informenv15")
    public ModelAndView showTemplateInformenv15(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Informe Nivel 15")) {

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
        List<Informenv15> informenv15List = informenv15Service.getinformenv15(todayString);
        modelAndView.addObject("period",todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), informenv15List.size());
        Page<Informenv15> pageInformenv15 = new PageImpl<>(informenv15List.subList(start, end), pageRequest, informenv15List.size());

        int totalPage=pageInformenv15.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        if(params.get("vFilter")==null||(params.get("vFilter").toString()).equals("Original") ||(params.get("vFilter").toString()).equals("") ||(params.get("vFilter").toString()).equals("No Filtrar")) {
            modelAndView.addObject("vFilter","No Filtrar");
        }

        modelAndView.addObject("allTemplateInformenv15",pageInformenv15.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","informenv15");
        modelAndView.addObject("registers",informenv15List.size());

        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/informenv15");
        }
            else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/ifrs9/informenv15/upload")
    public ModelAndView uploadInforme(@RequestParam Map<String, Object> params){
        String period = params.get("fileDate").toString();

        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/informenv15");

        try{
            informenv15Service.loadInforme(period);
        }catch (Exception e){
            e.printStackTrace();
        }

        modelAndView.addObject("period", period);

        return modelAndView;

    }

    @GetMapping(value = "/ifrs/informenv15/download")
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
        String headerValue = "attachment; filename=Informenv15_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Informenv15> Informenv15List;
        if(params.get("vFilter")==null||(params.get("vFilter").toString()).equals("Original") ||(params.get("vFilter").toString()).equals("") ||(params.get("vFilter").toString()).equals("No Filtrar")) {
            Informenv15List = informenv15Service.getinformenv15(todayString);
        }
        else{
            Informenv15List = informenv15Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),todayString);
        }
        Informenv15ListReport listReport = new Informenv15ListReport(Informenv15List);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/searchInformenv15")
    @ResponseBody
    public ModelAndView searchInformenv15(@RequestParam Map<String, Object> params) {
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

        List<Informenv15> list;
        String filtro = "";
        if(params.get("vFilter")==null|| (params.get("vFilter").toString()).equals("Original") || (params.get("vFilter").toString()).equals("") ||(params.get("vFilter").toString()).equals("No Filtrar")) {
            list = informenv15Service.getinformenv15(todayString);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("vFilter","No Filtrar");
        }
        else{
            list = informenv15Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),params.get("period").toString());
            modelAndView.addObject("vId",params.get("vId").toString());
            modelAndView.addObject("vFilter",params.get("vFilter").toString());
        }


        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Informenv15> pageSubsidiaries = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageSubsidiaries.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allTemplateInformenv15",pageSubsidiaries.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchInformenv15");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/informenv15");
        return modelAndView;
    }
}