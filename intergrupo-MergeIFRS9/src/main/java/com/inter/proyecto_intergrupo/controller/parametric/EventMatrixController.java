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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

            List<Object[]> eventMatrixes = eventMatrixService.findAllOrdered();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), eventMatrixes.size());
            Page<Object[]> pageEventMatrix = new PageImpl<>(eventMatrixes.subList(start, end), pageRequest, eventMatrixes.size());

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

            //List<AccountEventMatrix> cuentas = accountEventMatrixService.findAllActive();


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
    public ModelAndView modifyEventMatrix(@PathVariable int id,@RequestParam Map<String, Object> params){
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

        if(params.get("selectedConcil")!= null && !params.get("selectedConcil").toString().equalsIgnoreCase(""))
            modelAndView.addObject("selectedConcil1", params.get("selectedConcil").toString());
        if(params.get("selectedInv")!= null && !params.get("selectedInv").toString().equalsIgnoreCase(""))
            modelAndView.addObject("selectedInv1", params.get("selectedInv").toString());
        if(params.get("selectedET")!= null && !params.get("selectedET").toString().equalsIgnoreCase(""))
            modelAndView.addObject("selectedET1", params.get("selectedET").toString());
        if(params.get("selectedEstado")!= null && !params.get("selectedEstado").toString().equalsIgnoreCase(""))
            modelAndView.addObject("selectedEstado1", params.get("selectedEstado").toString());
        if(params.get("page")!= null && !params.get("page").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page1", params.get("page").toString());

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
            BindingResult bindingResult,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/searchEventMatrix");


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

            if(params.get("selectedConcil2")!=null && !params.get("selectedConcil2").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedConcil", params.get("selectedConcil2").toString());
            if(params.get("selectedInv2")!=null && !params.get("selectedInv2").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedInv", params.get("selectedInv2").toString());
            if(params.get("selectedET2")!=null && !params.get("selectedET2").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedET", params.get("selectedET2").toString());
            if(params.get("selectedEstado2")!=null && !params.get("selectedEstado2").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedEstado", params.get("selectedEstado2").toString());
            if(params.get("page2")!=null && !params.get("page2").toString().equalsIgnoreCase(""))
                modelAndView.addObject("page", params.get("page2").toString());

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
            @RequestParam(name = "selectedEstado", defaultValue= "-1") String estado,
            @RequestParam Map<String, Object> params
           ) {

        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Matriz de Eventos");

        if(userService.validateEndpoint(user.getId(),"Ver Matriz de Eventos")) { //CAMBIAR A VER Matriz de Eventos
            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<Object[]> eventMatrixes = eventMatrixService.findByParams(tipoEvento, concil, inventario, cuenta,estado);
            //List<EventMatrix> eventMatrixes = eventMatrixService.findByParams(1, 1, 1, "0");

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), eventMatrixes.size());
            Page<Object[]> pageEventMatrix = new PageImpl<>(eventMatrixes.subList(start, end), pageRequest, eventMatrixes.size());

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
            modelAndView.addObject("selectedEstado1", estado);

            if(params.get("page")!=null && !params.get("page").toString().equalsIgnoreCase(""))
                modelAndView.addObject("page", params.get("page").toString());
            else
                modelAndView.addObject("page", "1");

            List<EventType> allTEs = eventTypeService.findAll();
            modelAndView.addObject("allTEs", allTEs);

            List<Conciliation> allConcils = conciliationService.findAll();
            modelAndView.addObject("allConcils", allConcils);

            //List<AccountEventMatrix> cuentas = accountEventMatrixService.findAllActive();


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

    @GetMapping(value = "/parametric/eventMatrix/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,
                            @RequestParam(name = "selectedET", defaultValue= "0") Integer  tipoEvento,
                            @RequestParam(name = "selectedConcil", defaultValue= "0") Integer  concil,
                            @RequestParam(name = "selectedInv", defaultValue= "0") Integer  inventario,
                            @RequestParam(name = "selectedCuenta", defaultValue= "0") String cuenta,
                            @RequestParam(name = "selectedEstado", defaultValue= "-1") String estado,
                            @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Matrices_Eventos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> eventMatrixes = eventMatrixService.findByParams(tipoEvento, concil, inventario, cuenta,estado);
        MatrixEventListReport listReport = new MatrixEventListReport(eventMatrixes);
        listReport.export(response);
    }


}
