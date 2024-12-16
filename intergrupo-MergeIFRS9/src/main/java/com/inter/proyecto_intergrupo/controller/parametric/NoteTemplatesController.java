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
public class NoteTemplatesController {
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

    @Autowired
    private NoteTemplateService noteTemplateService;

    @GetMapping(value="/parametric/noteTemplates")
    public ModelAndView shownoteTemplates(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Plantilla de Notas");
        if(userService.validateEndpoint(user.getId(),"Ver Plantilla de Notas")) { //CAMBIAR A VER Matriz de Eventos

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<NoteTemplate> noteTemplates = noteTemplateService.findAllActive();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), noteTemplates.size());
            Page<NoteTemplate> pageEventMatrix = new PageImpl<>(noteTemplates.subList(start, end), pageRequest, noteTemplates.size());

            int totalPage=pageEventMatrix.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allRegisters",pageEventMatrix.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","country");
            modelAndView.addObject("registers",noteTemplates.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);

            if(params.get("selectedConcil")!=null && !params.get("selectedConcil").toString().trim().equalsIgnoreCase("")) {
                modelAndView.addObject("selectedConcil", params.get("selectedConcil").toString());
                System.out.println(params.get("selectedConcil").toString());
            }
            if(params.get("selectedInv")!=null && !params.get("selectedInv").toString().trim().equalsIgnoreCase("")) {
                modelAndView.addObject("selectedInv", params.get("selectedInv").toString());
                System.out.println(params.get("selectedInv").toString());
            }

            List<Conciliation> allConcils = conciliationService.findAll();
            modelAndView.addObject("allConcils", allConcils);

            modelAndView.setViewName("parametric/noteTemplates");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/createNoteTemplate")
    public ModelAndView showcreateNoteTemplate(){
        ModelAndView modelAndView = new ModelAndView();
        NoteTemplate noteTemplate = new NoteTemplate();
        List<EventType> allETs = eventTypeService.findAll();

        List<Conciliation> allConciliations = conciliationService.findAllActive();
        List<ConciliationRoute> allConciliationRoutes = null;
        List<CampoRConcil> campos = null;
        modelAndView.addObject("conciliaciones", allConciliations);
        modelAndView.addObject("rutascs", allConciliationRoutes);
        modelAndView.addObject("campos", campos);
        modelAndView.addObject("noteTemplate",noteTemplate);

        modelAndView.setViewName("/parametric/createNoteTemplate");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/modifyNoteTemplate/{id}")
    public ModelAndView showModifyNoteTemplate(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        NoteTemplate noteTemplate = noteTemplateService.findById(id);
        List<EventType> allETs = eventTypeService.findAll();

        List<Conciliation> allConciliations = conciliationService.findAllActive();
        List<Object[]> allConciliationRoutes = null;
        if(noteTemplate.getConciliacion()!=null)
            allConciliationRoutes = conciliationRouteService.findRutasByConcil(noteTemplate.getConciliacion().getId());
        List<Object[]> campos = null;
        if(noteTemplate.getInventarioConciliacion()!=null)
            campos = campoRConcilService.findCamposByRutaConcil(noteTemplate.getInventarioConciliacion().getId());


        List<Integer> matrices = null;
        if(noteTemplate.getInventarioConciliacion()!=null && noteTemplate.getConciliacion()!=null)
            matrices=eventMatrixService.findMatrices(noteTemplate.getConciliacion().getId(), noteTemplate.getInventarioConciliacion().getId());

        modelAndView.addObject("conciliaciones", allConciliations);
        modelAndView.addObject("rutascs", allConciliationRoutes);
        modelAndView.addObject("matrices", matrices);
        modelAndView.addObject("campos", campos);
        modelAndView.addObject("noteTemplate",noteTemplate);

        modelAndView.setViewName("/parametric/modifyNoteTemplate");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createNoteTemplate")
    public ModelAndView createNoteTemplate(
            @ModelAttribute NoteTemplate noteTemplate,
            @RequestParam(defaultValue = "N" ,name = "selectedConcil") String idconcil,
            @RequestParam(defaultValue = "N" ,name = "selectedMatriz") String idMatriz,
            @RequestParam(defaultValue = "N" ,name = "selectedTip") String idTipificacion,
            @RequestParam(defaultValue = "N" ,name = "selectedRutaConcil") String idrutaconcil,
            @RequestParam(defaultValue = "" ,name = "selectedTipoEvento") String TipoEvento,
            @RequestParam(defaultValue = "N" ,name = "campoRefTercero") String campoRefTercero,
            BindingResult bindingResult){

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/noteTemplates");
        System.out.println(idconcil);
        System.out.println(idTipificacion);
        System.out.println(idrutaconcil);
        System.out.println(TipoEvento);


        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createNoteTemplate");
        }else{
            if(!idconcil.equals("N")) {
                Conciliation conciliation = conciliationService.findById(Integer.valueOf(idconcil));
                noteTemplate.setConciliacion(conciliation);
            }
            if(!idMatriz.equals("N")) {
                EventMatrix matriz = eventMatrixService.findById(Integer.valueOf(idMatriz));
                noteTemplate.setMatriz(matriz);
            }
            if(!idTipificacion.equals("N")) {
            Conciliation tipificacion = conciliationService.findById(Integer.valueOf(idTipificacion));
            noteTemplate.setTipificacion(tipificacion);
            }
            if(!idrutaconcil.equals("N")) {
                ConciliationRoute croute = conciliationRouteService.findById(Integer.valueOf(idrutaconcil));
                noteTemplate.setInventarioConciliacion(croute);
            }

            noteTemplate.setEvento(TipoEvento);

            if(!campoRefTercero.equals("N")) {
                CampoRConcil refTercero = campoRConcilService.findById(Integer.valueOf(campoRefTercero));
                noteTemplate.setReferenciaTercero(refTercero);
            }

            noteTemplateService.modificar(noteTemplate);
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyNoteTemplate")
    public ModelAndView modifyNoteTemplate(
            @ModelAttribute NoteTemplate noteTemplate,
            @RequestParam(defaultValue = "N" ,name = "selectedConcil") String idconcil,
            @RequestParam(defaultValue = "N" ,name = "selectedMatriz") String idMatriz,
            @RequestParam(defaultValue = "N" ,name = "selectedTip") String idTipificacion,
            @RequestParam(defaultValue = "N" ,name = "selectedRutaConcil") String idrutaconcil,
            @RequestParam(defaultValue = "" ,name = "selectedTipoEvento") String TipoEvento,
            @RequestParam(defaultValue = "N" ,name = "campoRefTercero") String campoRefTercero,
            BindingResult bindingResult){

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/noteTemplates");

        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/modifyNoteTemplate");
        }else{
            if(!idconcil.equals("N")) {
                Conciliation conciliation = conciliationService.findById(Integer.valueOf(idconcil));
                noteTemplate.setConciliacion(conciliation);
            }
            else noteTemplate.setConciliacion(null);
            if(!idMatriz.equals("N")) {
                EventMatrix matriz = eventMatrixService.findById(Integer.valueOf(idMatriz));
                noteTemplate.setMatriz(matriz);
            }
            else noteTemplate.setMatriz(null);
            if(!idTipificacion.equals("N")) {
                Conciliation tipificacion = conciliationService.findById(Integer.valueOf(idTipificacion));
                noteTemplate.setTipificacion(tipificacion);
            }
            else noteTemplate.setTipificacion(null);
            if(!idrutaconcil.equals("N")) {
                ConciliationRoute croute = conciliationRouteService.findById(Integer.valueOf(idrutaconcil));
                noteTemplate.setInventarioConciliacion(croute);
            }
            else noteTemplate.setInventarioConciliacion(null);

            noteTemplate.setEvento(TipoEvento);

            if(!campoRefTercero.equals("N")) {
                CampoRConcil refTercero = campoRConcilService.findById(Integer.valueOf(campoRefTercero));
                noteTemplate.setReferenciaTercero(refTercero);
            }
            else noteTemplate.setReferenciaTercero(null);

            noteTemplateService.modificar(noteTemplate);
        }
        return modelAndView;
    }



    @GetMapping("/parametric/obtenerCuentas2/{idTipoEvento}/{idConciliacion}/{idInventarioConciliacion}")
    public ResponseEntity<List<String>> obtenerCuentas2(
            @PathVariable(required = false) Integer idTipoEvento,
            @PathVariable(required = false) Integer idConciliacion,
            @PathVariable(required = false) Integer idInventarioConciliacion) {

        // Llama al servicio para obtener la lista de cuentas basado en los parámetros.
        List<String> cuentas = eventMatrixService.findCuentaGanancia(idTipoEvento, idConciliacion, idInventarioConciliacion);
        // Retorna la lista de cuentas en formato JSON.
        return ResponseEntity.ok(cuentas);
    }

    @GetMapping(value = "/parametric/searchNoteTemplate")
    @ResponseBody
    public ModelAndView searchNoteTemplate(
            @RequestParam(name = "selectedConcil", defaultValue= "") Integer  concil,
            @RequestParam(name = "selectedInv", defaultValue= "") Integer  inventario,
            @RequestParam Map<String, Object> params
           ) {

        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Plantilla de Notas");

        if(userService.validateEndpoint(user.getId(),"Ver Plantilla de Notas")) {
            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<NoteTemplate> noteTemplates = noteTemplateService.findByParams(concil, inventario);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), noteTemplates.size());
            Page<NoteTemplate> pageNoteTemplate = new PageImpl<>(noteTemplates.subList(start, end), pageRequest, noteTemplates.size());

            int totalPage=pageNoteTemplate.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allRegisters",pageNoteTemplate.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","noteTemplates");
            modelAndView.addObject("registers",noteTemplates.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.addObject("selectedConcil", concil);
            modelAndView.addObject("selectedInv", inventario);
            System.out.println(concil);
            System.out.println(inventario);

            List<EventType> allTEs = eventTypeService.findAll();
            modelAndView.addObject("allTEs", allTEs);

            List<Conciliation> allConcils = conciliationService.findAll();
            modelAndView.addObject("allConcils", allConcils);
            modelAndView.setViewName("parametric/noteTemplates");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;

    }

}
