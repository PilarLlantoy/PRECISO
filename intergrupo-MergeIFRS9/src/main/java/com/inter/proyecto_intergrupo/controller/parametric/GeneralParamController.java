package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.CampoRConcil;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.model.parametric.GeneralParam;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ConciliationRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.EventTypeService;
import com.inter.proyecto_intergrupo.service.parametricServices.GeneralParamService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class GeneralParamController {
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private GeneralParamService generalParamService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @GetMapping(value="/parametric/generalParam")
    public ModelAndView showGeneralParam(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Parametro General");
        if(userService.validateEndpoint(user.getId(),"Ver Parametro General")) {
            List<GeneralParam> parametrics = generalParamService.findAll();
            modelAndView.addObject("allParametrics",parametrics);

            List<ConciliationRoute> listCr0 = new ArrayList<>();
            List<ConciliationRoute> crAll = conciliationRouteService.findAllActive();
            if(crAll!=null)
                listCr0=crAll;
            modelAndView.addObject("parametricsData0",listCr0);
            modelAndView.addObject("parametricsData2",listCr0);

            List<CampoRConcil> listCr1 = new ArrayList<>();
            ConciliationRoute cr1 = conciliationRouteService.findByName(parametrics.get(0).getValorUnidad());
            if(cr1!=null)
                listCr1=cr1.getCampos();
            modelAndView.addObject("parametricsData1",listCr1);

            List<CampoRConcil> listCr3 = new ArrayList<>();
            ConciliationRoute cr3 = conciliationRouteService.findByName(parametrics.get(2).getValorUnidad());
            if(cr3!=null)
                listCr3=cr3.getCampos();
            modelAndView.addObject("parametricsData3",listCr3);

            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/generalParam");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyGeneralParam")
    public ModelAndView updateGeneralParam(@ModelAttribute GeneralParam objeto){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/generalParam");
        generalParamService.modificar(objeto);
        return modelAndView;
    }

}
