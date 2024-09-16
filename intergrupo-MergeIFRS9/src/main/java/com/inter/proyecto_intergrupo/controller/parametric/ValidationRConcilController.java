package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ValidationRConcilController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private CampoRConcilService campoRCService;

    @Autowired
    private ValidationRConcilService validationRCService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @PostMapping(value = "/parametric/createValidacionRConcil")
    public ModelAndView createValidacionRConcil(@ModelAttribute ValidationRConcil validationRC,
                                           @RequestParam(name = "selectedCampoRef") String campoRefid,
                                           @RequestParam(name = "selectedCampoVal") String campoValid,
                                           @RequestParam(name = "selectedOperacion") String operacion,
                                           @RequestParam(name = "crouteId") String crouteId,
                                           BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/validationLoadingConciliationRoute/" + crouteId);
        ConciliationRoute croute = conciliationRouteService.findById(Integer.parseInt(crouteId));
        validationRC.setRutaConciliacion(croute);
        CampoRConcil referencia = campoRCService.findById(Integer.valueOf(campoRefid));
        validationRC.setCampoRef(referencia);
        CampoRConcil validacion = campoRCService.findById(Integer.valueOf(campoValid));
        validationRC.setCampoVal(validacion);
        validationRC.setOperacion(operacion);
        validationRCService.modificar(validationRC);
        return modelAndView;
    }


}
