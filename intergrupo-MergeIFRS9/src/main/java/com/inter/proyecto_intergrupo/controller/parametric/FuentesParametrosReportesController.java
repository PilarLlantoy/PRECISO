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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class FuentesParametrosReportesController {
    private static final int PAGINATIONCOUNT=12;
    //private static final int PAGINATIONCOUNT=5;
    private static final int PAGINATIONCOUNTDATA=500;


    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ParametrosReportesService parametrosReportesService;

    @Autowired
    private FilterParametroReportesService filterParametroReportesService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private CampoParametroReportesService campoParametroReportesService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private SourceParametroReportesService sourceParametroReportesService;

    @Autowired
    private AdditionalSourceParametroReportesService additionalSourceParametroReportesService;

    @Autowired
    private CampoRConcilService campoRConcilService;

    @Autowired
    private StructureParametroReportesService structureParametroReportesService;

    @Autowired
    private ValidationParametroReportesService validationParametroReportesService;

    @Autowired
    private ResultingFieldsParametroReportesService resultingFieldsParametroReportesService;


    @GetMapping(value = "/parametric/sourcesParametroReportes/{id}")
    public ModelAndView sourcesParametroReportes(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        ParametrosReportes parametro = parametrosReportesService.findById(id);
        modelAndView.addObject("parametro",parametro);

        List<SourceParametroReportes> fuentes = parametro.getFuentes();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), fuentes.size());

        Page<SourceParametroReportes> pageConciliation = new PageImpl<>(fuentes.subList(start, end), pageRequest, fuentes.size());


        List<Conciliation> conciliaciones = conciliationService.findAllActive();
        modelAndView.addObject("conciliaciones",conciliaciones);
        List<ConciliationRoute> inventarios = new ArrayList<>();
        modelAndView.addObject("inventarios",inventarios);
        List<EventType> eventos = new ArrayList<>();
        modelAndView.addObject("eventos",eventos);

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","sourcesParametroReportes/"+id);
        modelAndView.addObject("allFuentes",pageConciliation.getContent());
        modelAndView.addObject("registers",fuentes.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Contables");
        modelAndView.addObject("p_modificar", p_modificar);


        SourceParametroReportes fuente = new SourceParametroReportes();
        modelAndView.addObject("fuente",fuente);


        modelAndView.setViewName("parametric/sourcesParametroReportes");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createSourceParametroReportes")
    public ModelAndView createSourceParametroReportes(@ModelAttribute SourceParametroReportes fuente,
                                          @RequestParam(name = "selectedConcil") int idConcil,
                                          @RequestParam(name = "selectedInventario") int idInv,
                                          @RequestParam(name = "selectedEvento") int idEvento,
                                          @RequestParam(name = "parametroId") String parametroId,
                                          BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/sourcesParametroReportes/" + parametroId);

        ParametrosReportes parametro = parametrosReportesService.findById(Integer.parseInt(parametroId));
        fuente.setParametroReportes(parametro);

        Conciliation concil = conciliationService.findById(idConcil);
        fuente.setFuente(concil);
        ConciliationRoute inventario = conciliationRouteService.findById(idInv);
        fuente.setInventario(inventario);
        EventType evento = eventTypeService.findAllById(idEvento);
        fuente.setEvento(evento);

        sourceParametroReportesService.modificar(fuente);
        return modelAndView;

    }


}
