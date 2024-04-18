package com.inter.proyecto_intergrupo.controller.ifrs9;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.*;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Subsidiaries;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.PerimeterListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.PerimeterService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.QuotaListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xpath.operations.Quo;
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

import javax.mail.Quota;
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
public class PerimeterController {

    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private PerimeterService perimeterService;


    private List<String> listColumns=List.of("No Filtrar","Segmento","Subproducto","Codigo Consolidacion","Divisa");

    @GetMapping(value="/ifrs/perimeter")
    public ModelAndView showPerimeter(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/perimeter");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

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

        List<Perimeter> perimeterList = perimeterService.getPerimetro(todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), perimeterList.size());
        Page<Perimeter> perimeterPage = new PageImpl<>(perimeterList.subList(start, end), pageRequest, perimeterList.size());

        List<Object[]> companies = perimeterService.getCompany(todayString);
        boolean noQuery = false;
        if (companies.size() == 0) {
            noQuery = false;
        } else {
            noQuery = true;
        }

        int totalPage=perimeterPage.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        if (perimeterList != null){
            modelAndView.addObject("allPerimeter",perimeterPage.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","perimeter");
            modelAndView.addObject("registers",perimeterList.size());
        }

        modelAndView.addObject("companies",companies);
        modelAndView.addObject("noQuery", noQuery);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("vFilter","Original");
        modelAndView.setViewName("ifrs/perimeter");

        return modelAndView;
    }

    @PostMapping(value="/ifrs/perimeter")
    public ModelAndView validar(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/perimeter");

        String todayString="";
        String opcionQuery = "";
        String opcionEmpresa = "";
        String opcionEEFF = "";

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

        if(params.get("opcionQuery")==null || params.get("opcionQuery").toString()=="") {
            opcionQuery = "LOCAL";
        }
        else {
            opcionQuery = params.get("opcionQuery").toString();
        }

        if(params.get("opcionEEFF")==null || params.get("opcionEEFF").toString()=="") {
            opcionEEFF = "LOCAL";
        }
        else {
            opcionEEFF = params.get("opcionEEFF").toString();
        }

        if(params.get("opcionCompany")==null || params.get("opcionCompany").toString()=="") {
            opcionEmpresa = "0013";
        }
        else {
            opcionEmpresa = params.get("opcionCompany").toString();
        }

        List<Perimeter> perimeterList = new ArrayList<>();
        perimeterList = perimeterService.calcularPerimetro(todayString, opcionEEFF, opcionQuery, opcionEmpresa);

        if (perimeterList.size() == 0){
            modelAndView.addObject("resp","cuadreCorrecto");
        }
        else {
            modelAndView.addObject("resp","cuadreFallido");
        }

        modelAndView.addObject("opcionQuery",opcionQuery);
        modelAndView.addObject("opcionEEFF",opcionEEFF);

        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/clearPerimeter")
    public ModelAndView clearPerimeter(@RequestParam Map<String, Object> params){

        String todayString = "";

        if(params.get("period")!=null && params.get("period").toString()=="") {
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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        perimeterService.clearPerimetro(user,todayString);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/perimeter");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/perimeter/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=PerimetroIFRS9_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Perimeter> perimeterList= new ArrayList<Perimeter>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("") ||(params.get("vFilter").toString()).equals("No Filtrar")) {
            perimeterList = perimeterService.getPerimetro(params.get("period").toString());
        }
        else{
            perimeterList = perimeterService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),params.get("period").toString());
        }
        PerimeterListReport listReport = new PerimeterListReport(perimeterList);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/searchPerimeter")
    @ResponseBody
    public ModelAndView searchPerimeter(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Perimeter> perimeterList= new ArrayList<Perimeter>();
        if(params.get("vFilter")==null||(params.get("vFilter").toString()).equals("Original") ||(params.get("vFilter").toString()).equals("") ||(params.get("vFilter").toString()).equals("No Filtrar")) {
            perimeterList = perimeterService.getPerimetro(params.get("period").toString());
            modelAndView.addObject("filterExport","Original");
        }
        else{
            perimeterList = perimeterService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),params.get("period").toString());
            modelAndView.addObject("vId",params.get("vId").toString());
            modelAndView.addObject("vFilter",params.get("vFilter").toString());
        }
        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), perimeterList.size());
        Page<Perimeter> pageSubsidiaries = new PageImpl<>(perimeterList.subList(start, end), pageRequest, perimeterList.size());

        int totalPage=pageSubsidiaries.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        List<Object[]> companies = perimeterService.getCompany(params.get("period").toString());
        boolean noQuery = false;
        if (companies.size() == 0) {
            noQuery = false;
        } else {
            noQuery = true;
        }

        modelAndView.addObject("noQuery",noQuery);
        modelAndView.addObject("companies",companies);
        modelAndView.addObject("allPerimeter",pageSubsidiaries.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchPerimeter");
        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("registers",perimeterList.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/perimeter");

        return modelAndView;
    }


}
