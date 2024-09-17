package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class AccountConcilController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private AccountConcilService accountConcilService;


    @GetMapping(value = "/parametric/cargueCuentaConcil")
    public ModelAndView cargueCuentaConcil(){
        ModelAndView modelAndView = new ModelAndView();
        Campo campo = new Campo();
        modelAndView.addObject("campo",campo);
        modelAndView.setViewName("/parametric/cargueCuentaConcil");
        return modelAndView;
    }


    @PostMapping(value = "/parametric/createAccountConcil")
    public ModelAndView createCuenta(@ModelAttribute AccountConcil cuenta,
                                           @RequestParam(name = "concilId") String concilId,
                                           BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountsConciliation/" + concilId);
        Conciliation conciliacion= conciliationService.findById(Integer.valueOf(concilId));
        cuenta.setConciliacion(conciliacion);
        accountConcilService.modificar(cuenta);
        return modelAndView;

    }



}
