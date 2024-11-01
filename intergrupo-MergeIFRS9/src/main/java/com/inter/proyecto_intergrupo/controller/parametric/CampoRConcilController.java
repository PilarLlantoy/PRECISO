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
                                            @RequestParam(name = "longitud") String longitud,
                                            BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fieldLoadingConciliationRoute/" + crouteId);

        if((longitud.toUpperCase()).equals("MAX")) {
            var ll=getMaxCharacterLength(campoRC.getTipo());
            campoRC.setLongitud(ll);
        }
        else
            campoRC.setLongitud(longitud);
        ConciliationRoute route = conciliationRouteService.findById(Integer.parseInt(crouteId));
        campoRC.setRutaConciliacion(route);
        campoRCService.modificar(campoRC);
        campoRCService.recreateTable(route);
        return modelAndView;
    }

    public String getMaxCharacterLength(String dataType) {
        System.out.println("RA");
        switch (dataType.toUpperCase()) {
            case "INTEGER":
                return "11"; // Hasta 11 caracteres
            case "BIGINT":
                return "20"; // Hasta 20 caracteres
            case "FLOAT":
                return "16"; // Indefinido, depende de la precisión
            case "VARCHAR":
                return "2147483647"; // Hasta 2,147,483,647 caracteres
            case "DATE":
                return "10"; // Formato YYYY-MM-DD
            case "TIME":
                return "16"; // Formato HH:MM:SS.ffffff
            case "DATETIME":
                return "23"; // Formato YYYY-MM-DD HH:MM:SS.fff
            case "BIT":
                return "1"; // Representación como '0' o '1'
            default:
                return "Tipo de dato desconocido";
        }
    }

    @DeleteMapping("/parametric/deleteCampoRConcil/{id}")
    public ResponseEntity<?> deleteCampoRConcil(@PathVariable int id) {
        try {
            ConciliationRoute cr = campoRCService.findById(id).getRutaConciliacion();
            campoRCService.deleteById(id);
            campoRCService.recreateTable(cr);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }

    @GetMapping("/parametric/obtenerCamposFichero/{ficheroId}")
    @ResponseBody
    public List<Object[]> getCamposByFicheroId(@PathVariable("ficheroId") Integer ficheroId) {
        List<Object[]> campos = campoRCService.findCamposByRutaConcil(ficheroId);
        return campos;
    }


}
