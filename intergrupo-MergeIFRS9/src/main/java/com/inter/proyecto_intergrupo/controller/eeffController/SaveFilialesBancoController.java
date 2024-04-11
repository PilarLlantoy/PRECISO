package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.service.eeffconsolidated.DatesLoadEeffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class SaveFilialesBancoController {

    @Autowired
    private DatesLoadEeffService datesLoadEeffService;


    @PostMapping(value = "/eeffConsolidated/Guardar")
    public ModelAndView saveBankInformation(@RequestParam("rol") String entidad,
                                            @RequestParam("period") String periodo,
                                            Model model) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/SubsidiaryVerification");
        datesLoadEeffService.guardarInfoBanco(entidad, periodo);
        modelAndView.addObject("resp", "Correo Exitoso");
        return modelAndView;
    }

}
