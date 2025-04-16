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
public class ReportesConciliacionesController {
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

    @Autowired
    private EventMatrixService eventMatrixService;

    @GetMapping(value="/parametric/reportesConciliaciones")
    public ModelAndView reportesConciliaciones(@RequestParam Map<String, Object> params) {
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


            modelAndView.setViewName("parametric/reportesConciliaciones");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }
}
