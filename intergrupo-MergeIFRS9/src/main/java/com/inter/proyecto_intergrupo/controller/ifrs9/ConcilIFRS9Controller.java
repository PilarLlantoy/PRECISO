package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.IFRS9ConcilAccount;
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

import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ConcilIFRS9Controller {

    private static final int PAGINATIONCOUNT=10;

    @Autowired
    ConcilIFRS9Service concilIFRS9Service;

    @Autowired
    private UserService userService;

    @GetMapping(value="/ifrs9/concilScopeIFRS9")
    public ModelAndView showTemplateConsolidacion(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Conciliación IFRS9")) {
        int page=params.get("page")!=null?(Integer.parseInt(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

        String todayString="";
        if(params.get("period")==null || Objects.equals(params.get("period").toString(), "")) {
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

        ArrayList<IFRS9ConcilAccount> list = concilIFRS9Service.getConciliation(todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<IFRS9ConcilAccount> pageScope = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageScope.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        modelAndView.addObject("allData",pageScope.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","concilScopeIFRS9");
        modelAndView.addObject("registers",list.size());
        modelAndView.addObject("period",todayString);

        modelAndView.setViewName("/ifrs/concilScopeIFRS9");
        }
            else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value="/ifrs9/conciliateIfrs9")
    public ModelAndView validar(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/concilScopeIFRS9");
        String todayString="";
        if(params.get("period")==null || Objects.equals(params.get("period").toString(), "")) {
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
            concilIFRS9Service.createIFRS9Conciliation(todayString);
            modelAndView.addObject("resp","correct");
        }catch (Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp","error");
        }

        modelAndView.addObject("period",todayString);

        return  modelAndView;
    }

    @RequestMapping(value = "/ifrs/concilifrs9/download")
    @ResponseBody
    public void exportAllConciliacion(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String todayString="";
            String order = "";
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
                    todayString = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
                }
                else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
                }
            }
            else {
                todayString=params.get("period").toString();
            }

            ArrayList<IFRS9ConcilAccount> list = concilIFRS9Service.getConciliationAccum(todayString);

            ConcilIFRS9ListReport listReport = new ConcilIFRS9ListReport(list);
            String headerValue = "attachment; filename=ConciliaciónIFRS9_" + todayString + "_" + order + "_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
