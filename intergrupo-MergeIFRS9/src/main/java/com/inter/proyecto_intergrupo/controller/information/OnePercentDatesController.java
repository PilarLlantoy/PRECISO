package com.inter.proyecto_intergrupo.controller.information;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.information.Mis;
import com.inter.proyecto_intergrupo.model.information.OnePercentDates;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.OnePercentService;
import com.inter.proyecto_intergrupo.service.informationServices.OnePercentDatesServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class OnePercentDatesController {

    private static final int PAGINATIONCOUNT = 12;
    @Autowired
    OnePercentDatesServices onePercentDatesServices;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/information/calendarDates")
    public ModelAndView loadMisView(@RequestParam Map<String, Object> params) throws ParseException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Fechas Calculo 1%")) {
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

        ArrayList<OnePercentDates> list = onePercentDatesServices.getDates(todayString);

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<OnePercentDates> pageDates = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage = pageDates.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }

        modelAndView.addObject("dateList", pageDates.getContent());
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("vFilter", todayString);
        modelAndView.addObject("filterExport", "Original");
        modelAndView.addObject("directory", "calendarDates");
        modelAndView.addObject("registers", list.size());
        modelAndView.setViewName("/information/calendarDates");
        }
            else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping("/information/calendarDates/refresh")
    public ModelAndView modifyDates(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/information/calendarDates");
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

        try{
            onePercentDatesServices.generateDates(todayString);
            modelAndView.addObject("succes", "true");
        }catch (Exception e){
            modelAndView.addObject("succes", "false");
        }

        modelAndView.addObject("period", todayString);

        return modelAndView;
    }

}
