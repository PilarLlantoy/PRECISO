package com.inter.proyecto_intergrupo.controller.information;

import com.inter.proyecto_intergrupo.service.parametricServices.QueryService;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;

@Controller
public class InformationController {

    @Autowired
    QueryService queryService;

    @Autowired
    SendEmailService sendEmailService;

    @GetMapping(value = "/information/lastCharge")
    public ModelAndView getLastChargeView() throws ParseException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/information/lastCharge");
        String [] resp = queryService.getActualMonthCharge();
        modelAndView.addObject("queryLastUpdate", resp[1]);
        modelAndView.addObject("fecont", resp[0]);
        return modelAndView;
    }

    @PostMapping(value = "/information/lastCharge")
    public ModelAndView getByDateView(@RequestParam(name ="period")String period){
        ModelAndView modelAndView = new ModelAndView();

        String[] result = queryService.getByMonth(period);
        modelAndView.addObject("queryLastUpdate", result[1]);
        modelAndView.addObject("fecont",period);
        modelAndView.setViewName("/information/lastCharge");
        return modelAndView;
    }

}
