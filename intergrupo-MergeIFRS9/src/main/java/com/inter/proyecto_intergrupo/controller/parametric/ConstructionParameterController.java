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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ConstructionParameterController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ConstructionParameterService constructionParameterService;

    @Autowired
    private AccountEventMatrixService accountEventMatrixService;

    @Autowired
    private CampoRConcilService campoRConcilService;

    @Autowired
    private CampoRCService campoRCService;

    @Autowired
    private AccountingRouteService accountingRouteService;



    @DeleteMapping("/parametric/deleteParam/{id}")
    public ResponseEntity<?> deleteParam(@PathVariable int id) {
        try {
            constructionParameterService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }

    @GetMapping("/parametric/obtenerParamsCuenta/{id}")
    @ResponseBody
    public List<Object[]> obtenerParamsCuenta(@PathVariable("id") Integer ficheroId) {
        AccountEventMatrix cuenta= accountEventMatrixService.findById(ficheroId);
        List<Object[]> params = constructionParameterService.findParamsByAccount(cuenta.getId());
        return params;
    }

    @PostMapping("/parametric/agregarParametro")
    @ResponseStatus(HttpStatus.CREATED) // O el estado que prefieras
    public void  agregarParametro(@RequestBody ConstructionParameter nuevoParametro) {
        // Aquí puedes agregar la lógica necesaria para validar y guardar el nuevo parámetro

        CampoRConcil campoConcil = campoRConcilService.findById(nuevoParametro.getCampoConciliacion().getId());
        nuevoParametro.setCampoConciliacion(campoConcil);

        AccountingRoute ruta = accountingRouteService.findById(nuevoParametro.getRutaContable().getId());
        nuevoParametro.setRutaContable(ruta);

        CampoRC campoContValidar = campoRCService.findById(nuevoParametro.getCampoContValidar().getId());
        nuevoParametro.setCampoContValidar(campoContValidar);

        CampoRC campoContResultante = campoRCService.findById(nuevoParametro.getCampoContResultante().getId());
        nuevoParametro.setCampoContResultante(campoContResultante);

        AccountEventMatrix cuenta = accountEventMatrixService.findById(nuevoParametro.getAccount().getId());
        nuevoParametro.setAccount(cuenta);


        ConstructionParameter parametroGuardado = constructionParameterService.modificar(nuevoParametro);


    }
}
