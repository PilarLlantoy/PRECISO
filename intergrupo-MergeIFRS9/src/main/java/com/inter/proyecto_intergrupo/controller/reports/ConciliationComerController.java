package com.inter.proyecto_intergrupo.controller.reports;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reports.ConciliationComer;
import com.inter.proyecto_intergrupo.model.reports.ContingentTemplate;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.reportsServices.ComerConciliationStatusListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.ConciliationComerListReportService;
import com.inter.proyecto_intergrupo.service.reportsServices.ConciliationComerService;
import com.inter.proyecto_intergrupo.service.reportsServices.ContingentesListReport;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ConciliationComerController {

    private static final int PAGINATIONCOUNT=12;

    @Autowired
    private ConciliationComerService conciliationComerService;

    @Autowired
    private UserService userService;

    @GetMapping(value="/reports/conciliacionComer")
    public ModelAndView showTemplateH140(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Conciliación Comer")) {

            String todayString = "";
            if (params.get("period") != null && params.get("period").toString() == "") {
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

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            List<ConciliationComer> templatesConciliationComer = conciliationComerService.getConciliation(todayString);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), templatesConciliationComer.size());
            Page<ConciliationComer> pageComer = new PageImpl<>(templatesConciliationComer.subList(start, end), pageRequest, templatesConciliationComer.size());

            int totalPage = pageComer.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allTemplateComer", pageComer.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("directory", "conciliacionComer");
            modelAndView.addObject("registers",templatesConciliationComer.size());

            modelAndView.setViewName("/reports/conciliacionComer");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/reports/conciliacionComer/conciliate")
    public ModelAndView reloadComerConciliation(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/conciliacionComer");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";
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

        try{
            modelAndView.addObject("resp","true");
            conciliationComerService.insertComerConciliation(todayString);
            modelAndView.addObject("period",todayString);
        } catch(Exception e){
            modelAndView.addObject("period",todayString);
            modelAndView.addObject("resp","false");
        }

        modelAndView.addObject("directory","conciliacionComer");
        modelAndView.addObject("vFilter",todayString);

        return modelAndView;

    }


    @RequestMapping(value = "/reports/conciliacionComer/downloadStatus")
    @ResponseBody
    public void export(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Conciliacion comer_" + currentDateTime + ".xlsx";
            String todayString="";
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
            response.setHeader(headerKey, headerValue);
            List<ConciliationComer> comerList = conciliationComerService.getConciliation(todayString);
            ComerConciliationStatusListReport listReport = new ComerConciliationStatusListReport(comerList);
            listReport.exportResume(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/reports/conciliacionComer/download")
    @ResponseBody
    public void exportAll(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Conciliacion comer_" + currentDateTime + ".xlsx";
            String todayString="";
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
            response.setHeader(headerKey, headerValue);
            //List<ConciliationComer> comerList = conciliationComerService.getConciliation(todayString);
            List<ConciliationComer> comerList = conciliationComerService.getConciliationWithTotal(todayString);
            ComerConciliationStatusListReport listReport = new ComerConciliationStatusListReport(comerList);
            listReport.exportConciliation(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
