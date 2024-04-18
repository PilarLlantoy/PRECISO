package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.ValQueryEEFF;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.*;
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
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ValidationQueryEEFFController {

    private static final int PAGINATIONCOUNT=10;

    @Autowired
    ValidationQueryEEFFService validationQueryEEFFService;

    @Autowired
    private UserService userService;

    @GetMapping(value="/ifrs/valQueryEeff")
    public ModelAndView showTemplateCuadreQueryEEFF(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/valQueryEeff");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Cuadre Query EEFF")){
            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            String todayString="";
            String opcionQuery = "";
            String opcionEEFF = "";
            String tipoValidacion = "";

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

            if(params.get("tipoValidacion")==null || params.get("tipoValidacion").toString()=="") {
                tipoValidacion = "GENERAL";
            }
            else {
                tipoValidacion = params.get("tipoValidacion").toString();
            }

            List<Object[]> companies = validationQueryEEFFService.getCompany(todayString);
            boolean noQuery = false;
            if (companies.size() == 0) {
                noQuery = false;
            } else {
                noQuery = true;
            }

            List<ValQueryEEFF> valQueryEEFFList = validationQueryEEFFService.getCuadreQueryEEFF(todayString);

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), valQueryEEFFList.size());
            Page<ValQueryEEFF> pageValQueryEEFF = new PageImpl<>(valQueryEEFFList.subList(start, end), pageRequest, valQueryEEFFList.size());

            int totalPage=pageValQueryEEFF.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }

            if (valQueryEEFFList != null){
                modelAndView.addObject("allTemplateCuadre",pageValQueryEEFF.getContent());
                modelAndView.addObject("current",page+1);
                modelAndView.addObject("next",page+2);
                modelAndView.addObject("prev",page);
                modelAndView.addObject("last",totalPage);
                modelAndView.addObject("directory","valQueryEeff");
                modelAndView.addObject("registers",valQueryEEFFList.size());
            }
            modelAndView.addObject("period",todayString);
            modelAndView.addObject("opcionQuery",opcionQuery);
            modelAndView.addObject("opcionEEFF",opcionEEFF);
            modelAndView.addObject("tipoValidacion",tipoValidacion);
            modelAndView.addObject("noQuery", noQuery);
            modelAndView.addObject("companies",companies);
            modelAndView.setViewName("/ifrs/valQueryEeff");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;

    }

    @GetMapping(value="/ifrs/valQueryEeff/validacionQuery")
    public ModelAndView validQuery(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/valQueryEeff");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());


        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogValidaciónCuentas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        String todayString = "";
        String opcionQuery = "";
        String opcionEmpresa = "";
        String opcionEEFF = "";
        String tipoValidacion = "";
        String empresa = "";

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

        if(params.get("opcionCompany")==null || params.get("opcionCompany").toString()=="") {
            opcionEmpresa = "0013";
        }
        else {
            opcionEmpresa = params.get("opcionCompany").toString();
        }

        if(params.get("opcionEEFF")==null || params.get("opcionEEFF").toString()=="") {
            opcionEEFF = "LOCAL";
        }
        else {
            opcionEEFF = params.get("opcionEEFF").toString();
        }


        List<Object[]> validateAccounts = validationQueryEEFFService.validateAccounts(todayString,opcionQuery,opcionEmpresa);

        if(validateAccounts.size()==0){

            List<ValQueryEEFF> list = validationQueryEEFFService.calcularCuadreQueryEeff(todayString,opcionQuery, opcionEEFF,opcionEmpresa);

            if (list.size() == 0){
                modelAndView.addObject("resp","validacionCorrecta");
            }
            else {
                modelAndView.addObject("resp","validacionFallida");
            }

        }else{
            ValidacionAcQueryEEFFListReport validacionAcQueryEEFFListReport = new ValidacionAcQueryEEFFListReport(validateAccounts);
            validacionAcQueryEEFFListReport.export(response);
        }

        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/clearValQueryEeff")
    public ModelAndView clearCuadreConcilifrs9(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params){

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
        validationQueryEEFFService.clearCuadreQueryEEFF(user, todayString);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/valQueryEeff");
        return  modelAndView;
    }

    @RequestMapping(value = "/ifrs/valQueryEeff/download")
    @ResponseBody
    public void exportAllCuadre(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";

            String todayString = "";
            String opcionQuery = "";
            String opcionEEFF = "";
            String tipoValidacion = "";

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

            String headerValue = "attachment; filename=CuadreQueryEEFF_" + todayString + "_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);

            List<Object[]> cuadreValList = validationQueryEEFFService.getCuadreQueryEEFFGroup(todayString);
            List<Object[]> cuadreEEFFl = validationQueryEEFFService.getEeffLocal(todayString);
            List<Object[]> cuadreEEFFi = validationQueryEEFFService.getEeffIfrs9(todayString);
            List<Object[]> cuadreQueryl = validationQueryEEFFService.getQueryLocal(todayString);
            List<Object[]> cuadreQueryi = validationQueryEEFFService.getQueryIfrs9(todayString);
            List<Object[]> cuadreInterl = validationQueryEEFFService.getIntLocal(todayString);
            List<Object[]> cuadreInteri = validationQueryEEFFService.getIntIfrs9(todayString);
            List<Object[]> cuadreAjusH = validationQueryEEFFService.getAdjHom(todayString);
            List<Object[]> cuadreAjusM = validationQueryEEFFService.getAdjMan(todayString);

            ValidationQueryEEFFListReport listReport = new ValidationQueryEEFFListReport(cuadreValList, cuadreEEFFl, cuadreEEFFi, cuadreQueryl, cuadreQueryi, cuadreInterl, cuadreInteri, cuadreAjusH, cuadreAjusM);
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}