package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Collections;


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


    @PostMapping(value = "/parametric/createCampoRConcil")
    public ModelAndView createCampoRConcil(@ModelAttribute CampoRConcil campoRC,
                                      @RequestParam(name = "crouteId") String crouteId,
                                      BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fieldLoadingConciliationRoute/" + crouteId);
        ConciliationRoute route = conciliationRouteService.findById(Integer.parseInt(crouteId));
        campoRC.setRutaConciliacion(route);
        campoRCService.modificar(campoRC);
        return modelAndView;
    }

    @DeleteMapping("/parametric/deleteCampoRConcil/{id}")
    public ResponseEntity<?> deleteCampoRConcil(@PathVariable int id) {
        try {
            campoRCService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }

    @GetMapping("/parametric/obtenerCamposFichero/{ficheroId}")
    @ResponseBody
    public List<Object[]> getCamposByFicheroId(@PathVariable("ficheroId") Integer ficheroId) {
        System.out.println("FICHERO ID "+ ficheroId);
        List<Object[]> campos = campoRCService.findCamposByRutaConcil(ficheroId);
        System.out.println("CAMPOS "+campos.size());
        return campos;
    }


}
