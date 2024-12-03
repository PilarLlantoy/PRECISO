package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
                                           @RequestParam(name = "selectedOperacion", defaultValue= "") String operacion,
                                           @RequestParam(name = "crouteId") String crouteId,
                                           BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/validationLoadingConciliationRoute/" + crouteId);
        try{
            ConciliationRoute croute = conciliationRouteService.findById(Integer.parseInt(crouteId));
            validationRC.setRutaConciliacion(croute);
            CampoRConcil referencia = campoRCService.findById(Integer.valueOf(campoRefid));
            validationRC.setCampoRef(referencia);
            validationRC.setOperacion(operacion);
            validationRCService.modificar(validationRC);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return modelAndView;
    }

    @DeleteMapping("/parametric/deleteValidationRConcil/{id}")
    public ResponseEntity<?> deleteValidationRConcil(@PathVariable int id) {
        try {
            validationRCService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }

}
