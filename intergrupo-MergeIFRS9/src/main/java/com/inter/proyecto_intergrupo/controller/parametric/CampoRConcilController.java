package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class CampoRConcilController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private CampoRConcilService campoRCService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;



    @GetMapping(value = "/parametric/cargueCamposRConcil")
    public ModelAndView cargueCamposRConcil(){
        ModelAndView modelAndView = new ModelAndView();
        Campo campo = new Campo();
        modelAndView.addObject("campo",campo);
        modelAndView.setViewName("/parametric/cargueCampos");
        return modelAndView;
    }


    @PostMapping(value = "/parametric/createCampoRConcil")
    public ModelAndView createCampoRConcil(@ModelAttribute CampoRConcil campoRC,
                                      @RequestParam(name = "selectedTipoCampo") String tipo,
                                      @RequestParam(name = "selectedFormatoFecha") String formFecha,
                                      @RequestParam(name = "selectedIdiomaCampo") String idioma,
                                      @RequestParam(name = "crouteId") String crouteId,
                                      BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fieldLoadingConciliationRoute/" + crouteId);

        ConciliationRoute route = conciliationRouteService.findById(Integer.parseInt(crouteId));
        campoRC.setRutaConciliacion(route);
        campoRC.setTipo(tipo);
        campoRC.setFormatoFecha(formFecha);
        campoRC.setIdioma(idioma);
        campoRCService.modificar(campoRC);

        return modelAndView;

    }



}
