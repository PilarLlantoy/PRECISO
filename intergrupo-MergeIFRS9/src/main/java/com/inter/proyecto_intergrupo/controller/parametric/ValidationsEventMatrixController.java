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
public class ValidationsEventMatrixController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private EventMatrixService eventMatrixService;


    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private ValidationMEService validationMEService;

    @Autowired
    private CampoRConcilService campoRConcilService;

    @Autowired
    private CampoRCService campoRCService;

    @Autowired
    private ConstructionParameterService constructionParameterService;



    @GetMapping(value="/parametric/validationsEventMatrix/{id}")
    public ModelAndView showConditionsEventMatrix(@PathVariable("id") int id, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        if(userService.validateEndpoint(user.getId(),"Ver Países")) { //CAMBIAR A VER Matriz de Eventos

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<EventMatrix> eventMatrixes = eventMatrixService.findAllActive();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), eventMatrixes.size());
            Page<EventMatrix> pageEventMatrix = new PageImpl<>(eventMatrixes.subList(start, end), pageRequest, eventMatrixes.size());

            int totalPage=pageEventMatrix.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allEvents",pageEventMatrix.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","country");
            modelAndView.addObject("registers",eventMatrixes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);

            List<EventType> allTEs = eventTypeService.findAll();
            modelAndView.addObject("allTEs", allTEs);

            List<Conciliation> allConcils = conciliationService.findAll();
            modelAndView.addObject("allConcils", allConcils);

            ValidationME validationME = new ValidationME();
            modelAndView.addObject("validationME", validationME);

            EventMatrix matriz = eventMatrixService.findById(id);
            modelAndView.addObject("matriz", matriz);

            List<ValidationME> validaciones= validationMEService.findByEventMatrix(matriz);
            modelAndView.addObject("validaciones", validaciones);

            List<Object[]> campos = campoRConcilService.findCamposByRutaConcil(matriz.getInventarioConciliacion().getId());
            modelAndView.addObject("campos", campos);

            modelAndView.setViewName("parametric/validationsEventMatrix");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createValidacionME")
    public ModelAndView createValidacionME(@ModelAttribute ValidationME validationME,
            @RequestParam(name = "selectedCampoVal", defaultValue = "0") Integer campoValidacion,
            @RequestParam(name = "selectedCampoAf", defaultValue = "0") Integer campoAfecta,
            @RequestParam(name = "selectedCampo", defaultValue = "0") Integer campo,
            @RequestParam(name = "selectedOperacion", defaultValue = "0") String operacion,
            @RequestParam(name = "matrizId", defaultValue = "0") String matrizId,
            BindingResult bindingResult){

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/validationsEventMatrix/" + matrizId);
        try{

            System.out.println(campoValidacion+" "+ campoAfecta+" "+campo+" "+operacion+" "+matrizId);

            if(campoValidacion!=0){
                CampoRConcil campoVal = campoRConcilService.findById(campoValidacion);
                validationME.setCampoVal(campoVal);
            }

            if(campoAfecta!=0){
                CampoRConcil campoAf = campoRConcilService.findById(campoAfecta);
                validationME.setCampoAfecta(campoAf);
            }

            if(campo!=0){
                CampoRConcil camp = campoRConcilService.findById(campo);
                validationME.setCampoRef(camp);
            }

            if(operacion!="0"){
                validationME.setOperacion(operacion);
            }

            if(matrizId!="0"){
                EventMatrix matriz = eventMatrixService.findById(Integer.valueOf(matrizId));
                validationME.setMatrizEvento(matriz);
            }

            validationMEService.modificar(validationME);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return modelAndView;
    }

    @DeleteMapping("/parametric/deleteValidationME/{id}")
    public ResponseEntity<?> deleteValidationME(@PathVariable int id) {
        try {
            validationMEService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }

}
