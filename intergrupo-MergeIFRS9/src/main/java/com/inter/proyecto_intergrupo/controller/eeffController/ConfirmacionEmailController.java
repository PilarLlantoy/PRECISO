package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.DatesLoadEeFF;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.DatesLoadEeffRepository;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.EnvioDeCorreoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Controller
public class ConfirmacionEmailController {

    @Autowired
    EntityManager entityManager;

    @Autowired
    EnvioDeCorreoService envioDeCorreoService;


    @PostMapping ("/eeffConsolidated/ConfirmarFiduciaria")

    public ModelAndView sendEmailFilial(@RequestParam("rol") String entidad,
                                  @RequestParam("period") String periodo,
                                  Model model)
    {
        String ruta = "redirect:/eeffConsolidated/filiales";
        if (entidad.equals("Valores"))
            ruta = "redirect:/eeffConsolidated/filialesValores";
        else if (entidad.equals("Banco"))
        ruta = "redirect:/eeffConsolidated/filialesBanco";

            ModelAndView modelAndView = new ModelAndView(ruta);

        envioDeCorreoService.SendMailFiliales(entidad , periodo);

        modelAndView.addObject("resp", "Correo Exitoso");

        return modelAndView;
    }

}

