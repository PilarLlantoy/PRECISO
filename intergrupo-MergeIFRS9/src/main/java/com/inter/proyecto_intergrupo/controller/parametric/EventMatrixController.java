package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.TipoDocumento;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.admin.View;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
public class EventMatrixController {
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
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private CampoRConcilService campoRConcilService;

    @Autowired
    private AccountEventMatrixService accountEventMatrixService;

    @GetMapping(value="/parametric/eventMatrix")
    public ModelAndView showEventMatrix(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Matriz de Eventos");
        if(userService.validateEndpoint(user.getId(),"Ver Matriz de Eventos")) { //CAMBIAR A VER Matriz de Eventos

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<EventMatrix> eventMatrixes = eventMatrixService.findAllOrdered();
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
            modelAndView.addObject("directory","eventMatrix");
            modelAndView.addObject("registers",eventMatrixes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);

            List<EventType> allTEs = eventTypeService.findAll();
            modelAndView.addObject("allTEs", allTEs);

            List<Conciliation> allConcils = conciliationService.findAll();
            modelAndView.addObject("allConcils", allConcils);

            List<AccountEventMatrix> cuentas = accountEventMatrixService.findAllActive();


            modelAndView.setViewName("parametric/eventMatrix");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/createEventMatrix")
    public ModelAndView showCreateEventMatrix(){
        ModelAndView modelAndView = new ModelAndView();
        EventMatrix eventMatrix = new EventMatrix();
        List<EventType> allETs = eventTypeService.findAll();

        List<Conciliation> allConciliations = conciliationService.findAllActive();
        List<ConciliationRoute> allConciliationRoutes = null;
        List<CampoRConcil> campos = null;
        modelAndView.addObject("tipoEventos", allETs);
        modelAndView.addObject("conciliaciones", allConciliations);
        modelAndView.addObject("rutascs", allConciliationRoutes);
        modelAndView.addObject("campos", campos);
        modelAndView.addObject("eventMatrix",eventMatrix);
        modelAndView.setViewName("/parametric/createEventMatrix");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/modifyEventMatrix/{id}")
    public ModelAndView modifyEventMatrix(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        EventMatrix eventMatrix = eventMatrixService.findById(id);
        List<EventType> allETs = eventTypeService.findAll();

        List<Conciliation> allConciliations = conciliationService.findAllActive();
        List<Object[]> allConciliationRoutes = conciliationRouteService.findRutasByConcil(eventMatrix.getConciliacion().getId());
        List<Object[]> campos = campoRConcilService.findCamposByRutaConcil(eventMatrix.getInventarioConciliacion().getId());
        modelAndView.addObject("tipoEventos", allETs);
        modelAndView.addObject("conciliaciones", allConciliations);
        modelAndView.addObject("rutascs", allConciliationRoutes);
        modelAndView.addObject("campos", campos);
        modelAndView.addObject("eventMatrix",eventMatrix);
        modelAndView.setViewName("/parametric/modifyEventMatrix");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createEventMatrix")
    public ModelAndView createEventMatrix(
            @ModelAttribute EventMatrix eventMatrix,
            @RequestParam(defaultValue = "N" ,name = "selectedConcil") String idconcil,
            @RequestParam(defaultValue = "N" ,name = "selectedRutaConcil") String idrutaconcil,
            @RequestParam(defaultValue = "N" ,name = "selectedTipoEvento") String idTipoEvento,
            @RequestParam(defaultValue = "N" ,name = "campoOperacion") String idCampoOperacion,
            @RequestParam(defaultValue = "N" ,name = "campocontable") String idCampoContable,
            BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/eventMatrix");


        EventMatrix matrixExists = eventMatrixService.findById(eventMatrix.getId());
        if(matrixExists != null){
            bindingResult
                    .rejectValue("matriz", "error.matriz",
                            "La matriz ya se ha registrado");
        }
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createEventMatrix");
        }else{
            Conciliation conciliation = conciliationService.findById(Integer.valueOf(idconcil));
            eventMatrix.setConciliacion(conciliation);

            if(!idrutaconcil.equals("N")) {
                ConciliationRoute croute = conciliationRouteService.findById(Integer.valueOf(idrutaconcil));
                eventMatrix.setInventarioConciliacion(croute);
            }

            if(!idTipoEvento.equals("N")) {
                EventType tipoEvento = eventTypeService.findAllById(Integer.valueOf(idTipoEvento));
                eventMatrix.setTipoEvento(tipoEvento);
            }

            if(!idCampoOperacion.equals("N")) {
                CampoRConcil campoOperacion = campoRConcilService.findById(Integer.valueOf(idCampoOperacion));
                eventMatrix.setCampoOperacion(campoOperacion);
            }

            if(!idCampoContable.equals("N")){
                CampoRConcil campoContable= campoRConcilService.findById(Integer.valueOf(idCampoContable));
                eventMatrix.setCampoCC(campoContable);
            }

            eventMatrix.setConsecutivo(campoRConcilService.findConsecutivo(eventMatrix)+1);
            eventMatrixService.modificar(eventMatrix);
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyEventMatrix")
    public ModelAndView modifyEventMatrix(
            @ModelAttribute EventMatrix eventMatrix,
            @RequestParam(defaultValue = "N" ,name = "selectedConcil") String idconcil,
            @RequestParam(defaultValue = "N" ,name = "selectedRutaConcil") String idrutaconcil,
            @RequestParam(defaultValue = "N" ,name = "selectedTipoEvento") String idTipoEvento,
            @RequestParam(defaultValue = "N" ,name = "campoOperacion") String idCampoOperacion,
            @RequestParam(defaultValue = "N" ,name = "campocontable") String idCampoContable,
            BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/eventMatrix");


        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createEventMatrix");
        }else{
            Conciliation conciliation = conciliationService.findById(Integer.valueOf(idconcil));
            eventMatrix.setConciliacion(conciliation);

            ConciliationRoute croute = conciliationRouteService.findById(Integer.valueOf(idrutaconcil));
            eventMatrix.setInventarioConciliacion(croute);

            EventType tipoEvento = eventTypeService.findAllById(Integer.valueOf(idTipoEvento));
            eventMatrix.setTipoEvento(tipoEvento);

            CampoRConcil campoOperacion = campoRConcilService.findById(Integer.valueOf(idCampoOperacion));
            eventMatrix.setCampoOperacion(campoOperacion);

            if(!idCampoContable.equalsIgnoreCase("N"))
            {
                CampoRConcil campoContable= campoRConcilService.findById(Integer.valueOf(idCampoContable));
                eventMatrix.setCampoCC(campoContable);
            }

            eventMatrixService.modificar(eventMatrix);
        }
        return modelAndView;
    }

    @GetMapping("/parametric/obtenerCuentas/{idTipoEvento}/{idConciliacion}/{idInventarioConciliacion}")
    public ResponseEntity<List<String>> obtenerCuentas(
            @PathVariable(required = false) Integer idTipoEvento,
            @PathVariable(required = false) Integer idConciliacion,
            @PathVariable(required = false) Integer idInventarioConciliacion) {

        // Llama al servicio para obtener la lista de cuentas basado en los parámetros.
        List<String> cuentas = eventMatrixService.findCuentaGanancia(idTipoEvento, idConciliacion, idInventarioConciliacion);
        // Retorna la lista de cuentas en formato JSON.
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping(value = "/parametric/searchEventMatrix")
    @ResponseBody
    public ModelAndView searchEventMatrix(
            @RequestParam(name = "selectedET", defaultValue= "0") Integer  tipoEvento,
            @RequestParam(name = "selectedConcil", defaultValue= "0") Integer  concil,
            @RequestParam(name = "selectedInv", defaultValue= "0") Integer  inventario,
            @RequestParam(name = "selectedCuenta", defaultValue= "0") String cuenta,
            @RequestParam Map<String, Object> params
           ) {

        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");

        if(userService.validateEndpoint(user.getId(),"Ver Países")) { //CAMBIAR A VER Matriz de Eventos
            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<EventMatrix> eventMatrixes = eventMatrixService.findByParams(tipoEvento, concil, inventario, cuenta);
            //List<EventMatrix> eventMatrixes = eventMatrixService.findByParams(1, 1, 1, "0");

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
            modelAndView.addObject("directory","eventMatrix");
            modelAndView.addObject("registers",eventMatrixes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);

            modelAndView.addObject("selectedConcil1", concil);
            modelAndView.addObject("selectedInv1", inventario);
            modelAndView.addObject("selectedET1", tipoEvento);
            modelAndView.addObject("selectedCuenta1", cuenta);

            List<EventType> allTEs = eventTypeService.findAll();
            modelAndView.addObject("allTEs", allTEs);

            List<Conciliation> allConcils = conciliationService.findAll();
            modelAndView.addObject("allConcils", allConcils);

            List<AccountEventMatrix> cuentas = accountEventMatrixService.findAllActive();


            modelAndView.setViewName("parametric/eventMatrix");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;

    }


    @GetMapping("/parametric/obtenerMatrices/{idConciliacion}/{idInventarioConciliacion}")
    @ResponseBody
    public List<Integer> obtenerMatrices(
            @PathVariable(required = false) Integer idConciliacion,
            @PathVariable(required = false) Integer idInventarioConciliacion) {
        List<Integer> matrices = eventMatrixService.findMatrices(idConciliacion, idInventarioConciliacion);
        System.out.println(idConciliacion + " " + idInventarioConciliacion);
        System.out.println(matrices.size());
        for(Integer a:matrices){
            System.out.println(a);
        }

        return matrices; // Asegúrate de que esto devuelva lo que esperas
    }




}
