package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.parametric.CampoRConcil;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
import com.inter.proyecto_intergrupo.model.parametric.CrossesConcilRoute;
import com.inter.proyecto_intergrupo.service.parametricServices.CampoRConcilService;
import com.inter.proyecto_intergrupo.service.parametricServices.ConciliationRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.CrossesConcilRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CrossesRConcilController {
    @Autowired
    private CampoRConcilService campoRCService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private CrossesConcilRouteService crossesConcilRouteService;

    @PostMapping(value = "/parametric/createCrossingRConcil")
    public ModelAndView createCrossingRConcil(@ModelAttribute CrossesConcilRoute cruce,
                                              @RequestParam(name = "selectedFichero") String ficheroId,
                                              @RequestParam(name = "selectedCampoInvActualiza") String campoInvActualizaId,
                                              @RequestParam(name = "selectedcampoInvValid") String campoInvValidId,
                                              @RequestParam(name = "selectedCampoFicVald") String campoFicValdId,
                                              @RequestParam(name = "selectedCampoFicResl") String campoFicReslId,
                                              @RequestParam(name = "crouteId") String crouteId,
                                              @RequestParam(name = "page1", defaultValue = "0") String page1,
                                           BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/informationCrossingConciliationRoute/" + crouteId);
        try{
            ConciliationRoute fichero = conciliationRouteService.findById(Integer.parseInt(ficheroId));
            cruce.setFichero(fichero);
            ConciliationRoute inventario = conciliationRouteService.findById(Integer.parseInt(crouteId));
            cruce.setInventario(inventario);

            CampoRConcil invActualizaId = campoRCService.findById(Integer.valueOf(campoInvActualizaId));
            cruce.setCampoInvActualiza(invActualizaId);
            CampoRConcil invValidId = campoRCService.findById(Integer.valueOf(campoInvValidId));
            cruce.setCampoInvValid(invValidId);

            CampoRConcil ficValdId = campoRCService.findById(Integer.valueOf(campoFicValdId));
            cruce.setCampoFicValid(ficValdId);
            CampoRConcil ficReslId = campoRCService.findById(Integer.valueOf(campoFicReslId));
            cruce.setCampoFicResul(ficReslId);

            crossesConcilRouteService.modificar(cruce);

            if(!page1.equalsIgnoreCase("0"))
                modelAndView.addObject("page1",page1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return modelAndView;
    }

    @DeleteMapping("/parametric/deleteCrossingRConcil/{id}")
    public ResponseEntity<?> deleteCrossingRConcil(@PathVariable int id) {
        try {
            crossesConcilRouteService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }

}
