package com.inter.proyecto_intergrupo.controller.precharge;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.information.Sicc;
import com.inter.proyecto_intergrupo.model.parametric.ComerParametric;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.precharges.ComerPrecharge;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ComerParametricListReport;
import com.inter.proyecto_intergrupo.service.prechargeService.ComerPrechargeListReport;
import com.inter.proyecto_intergrupo.service.prechargeService.ComerPrechargeService;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ComerPrechargeController {

    @Autowired
    ComerPrechargeService comerPrechargeService;

    @Autowired
    UserService userService;

    private static final int PAGINATIONCOUNT=12;

    @GetMapping(value = "/precharges/comer")
    public ModelAndView getInfo(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();

        int page=params.get("page")==null?0:(Integer.parseInt(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
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

        List<ComerPrecharge> list = comerPrechargeService.getInformation(todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ComerPrecharge> pageComer = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageComer.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        modelAndView.addObject("comerList",pageComer.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("vFilter",todayString);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","comer");
        modelAndView.addObject("registers",list.size());

        modelAndView.setViewName("/precharges/comerPrecharge");

        return modelAndView;
    }

    @GetMapping(value = "/precharges/comer/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= PrecargaComer_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ArrayList<ComerPrecharge> comerList;
        comerList = comerPrechargeService.getInformation(params.get("period").toString());

        ComerPrechargeListReport listReport = new ComerPrechargeListReport(comerList);
        listReport.exportMatch(response);
    }

}
