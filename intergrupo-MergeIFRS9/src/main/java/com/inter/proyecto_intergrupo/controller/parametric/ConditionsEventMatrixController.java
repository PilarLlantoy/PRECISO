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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ConditionsEventMatrixController {
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
    private CondicionMEService condicionMEService;

    @Autowired
    private CampoRConcilService campoRConcilService;

    @GetMapping(value="/parametric/conditionsEventMatrix/{id}")
    public ModelAndView showConditionsEventMatrix(@PathVariable("id") int id, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        if(userService.validateEndpoint(user.getId(),"Ver Países")) { //CAMBIAR A VER Matriz de Eventos

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            EventMatrix eventMatrix = eventMatrixService.findById(id);
            List<CampoRConcil> campos = eventMatrix.getInventarioConciliacion().getCampos();
            List<CondicionEventMatrix> condiciones = condicionMEService.findByMatrizEvento(eventMatrix);
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), condiciones.size());
            Page<CondicionEventMatrix> pageEventMatrix = new PageImpl<>(condiciones.subList(start, end), pageRequest, condiciones.size());

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
            modelAndView.addObject("registers",condiciones.size());
            modelAndView.addObject("allCondiciones",pageEventMatrix.getContent());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);

            CondicionEventMatrix condicion = new CondicionEventMatrix();
            modelAndView.addObject("condicion",condicion);
            modelAndView.addObject("campos", campos);

            List<EventType> allTEs = eventTypeService.findAll();
            modelAndView.addObject("allTEs", allTEs);

            List<Conciliation> allConcils = conciliationService.findAll();
            modelAndView.addObject("allConcils", allConcils);

            modelAndView.addObject("matriz", eventMatrix);

            if(params.get("selectedConcil1")!= null && !params.get("selectedConcil1").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedConcil1", params.get("selectedConcil1").toString());
            if(params.get("selectedInv1")!= null && !params.get("selectedInv1").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedInv1", params.get("selectedInv1").toString());
            if(params.get("selectedET1")!= null && !params.get("selectedET1").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedET1", params.get("selectedET1").toString());
            if(params.get("selectedEstado1")!= null && !params.get("selectedEstado1").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedEstado1", params.get("selectedEstado1").toString());
            if(params.get("page1")!= null && !params.get("page1").toString().equalsIgnoreCase(""))
                modelAndView.addObject("page1", params.get("page1").toString());

            modelAndView.setViewName("parametric/conditionsEventMatrix");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @DeleteMapping("/parametric/deleteCondicionME/{id}")
    public ResponseEntity<?> deleteCondicionME(@PathVariable int id) {
        try {
            condicionMEService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }


    @PostMapping(value = "/parametric/createCondicionEventMatrix")
    public ModelAndView createCondicionEventMatrix(@ModelAttribute CondicionEventMatrix condicion,
                                                   @RequestParam(name = "matrizId") String matrixId,
                                                   @RequestParam(name = "selectedOperacion", defaultValue= "") String operacion,
                                                   @RequestParam(name = "selectedCampo") String idcampo,
                                                   BindingResult bindingResult,@RequestParam Map<String, Object> params) {

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/conditionsEventMatrix/" + matrixId);

        EventMatrix matriz = eventMatrixService.findById(Integer.valueOf(matrixId));
        CampoRConcil campo = campoRConcilService.findById(Integer.valueOf(idcampo));
        condicion.setCampo(campo);
        condicion.setCondicion(operacion);
        condicion.setMatrizEvento(matriz);
        condicionMEService.modificar(condicion);

        if(params.get("selectedConcil1")!= null && !params.get("selectedConcil1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("selectedConcil1", params.get("selectedConcil1").toString());
        if(params.get("selectedInv1")!= null && !params.get("selectedInv1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("selectedInv1", params.get("selectedInv1").toString());
        if(params.get("selectedET1")!= null && !params.get("selectedET1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("selectedET1", params.get("selectedET1").toString());
        if(params.get("selectedEstado1")!= null && !params.get("selectedEstado1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("selectedEstado1", params.get("selectedEstado1").toString());
        if(params.get("page1")!= null && !params.get("page1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page1", params.get("page1").toString());

        return modelAndView;
    }

}
