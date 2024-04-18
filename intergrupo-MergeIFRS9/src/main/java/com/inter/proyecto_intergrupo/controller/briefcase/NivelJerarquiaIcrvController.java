package com.inter.proyecto_intergrupo.controller.briefcase;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.NivelJerarquiaIcrv;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.briefcaseServices.NivelJerarquiaIcrvListReport;
import com.inter.proyecto_intergrupo.service.briefcaseServices.NivelJerarquiaIcrvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class NivelJerarquiaIcrvController {

    @Autowired
    NivelJerarquiaIcrvService nivelJerarquiaIcrvService;

    @Autowired
    private UserService userService;

    private static final int PAGINATIONCOUNT=12;


    @GetMapping(value = "/briefcase/niveljerarquiaicrv")
    public ModelAndView showView(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Nivel Jerarquia ICRV")){
            int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";

            if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                if(calendar.get(Calendar.MONTH)==0)
                {
                    calendar.add(Calendar.YEAR,-1);
                    todayString = calendar.get(Calendar.YEAR) +"-12";
                }
                else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
                } else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
                }
            } else {
                todayString = params.get("period").toString();
            }

            List<NivelJerarquiaIcrv> list = nivelJerarquiaIcrvService.findAllByPeriod(todayString);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<NivelJerarquiaIcrv> pageList = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageList.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("niveljerarquiaicrvList", pageList.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("directory", "niveljerarquiaicrv");
            modelAndView.addObject("registers",list.size());
            modelAndView.setViewName("/briefcase/niveljerarquiaicrv");

        }
        else
        {
            modelAndView.addObject("niveljerarquiaicrv","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/briefcase/niveljerarquiaicrv/download")
    @ResponseBody
    public void download(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        List<NivelJerarquiaIcrv> list = nivelJerarquiaIcrvService.findAllByPeriod(params.get("period").toString());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= NivelJerarquiaICRV_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        NivelJerarquiaIcrvListReport listReport = new NivelJerarquiaIcrvListReport(null, list);
        listReport.export(response);
    }

    @GetMapping(value = "/briefcase/removeNiveljerarquiaicrv/{id}")
    @ResponseBody
    public ModelAndView removeNivelJerarquiaIcrv(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        boolean estado = nivelJerarquiaIcrvService.proccessData(user,id);
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/niveljerarquiaicrv?period="+id+"&resp=ICRV1-"+estado);
        return  modelAndView;
    }
}
