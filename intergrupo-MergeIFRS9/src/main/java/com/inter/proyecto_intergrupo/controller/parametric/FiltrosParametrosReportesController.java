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
public class FiltrosParametrosReportesController {
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


    @GetMapping(value = "/parametric/filtersParametroReportes/{id}")
    public ModelAndView filtersParametroReportes(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        ParametrosReportes parametro = parametrosReportesService.findById(id);
        modelAndView.addObject("parametro",parametro);

        List<FilterParametroReportes> filtros = parametro.getFiltros();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filtros.size());

        Page<FilterParametroReportes> pageConciliation = new PageImpl<>(filtros.subList(start, end), pageRequest, filtros.size());
        List<CampoParamReportes> campos = parametro.getCampos();
        modelAndView.addObject("campos",campos);

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","filtersParametroReportes/"+id);
        modelAndView.addObject("allCondiciones",pageConciliation.getContent());
        modelAndView.addObject("registers",filtros.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Contables");
        modelAndView.addObject("p_modificar", p_modificar);


        FilterParametroReportes filtro = new FilterParametroReportes();
        modelAndView.addObject("filtro",filtro);


        modelAndView.setViewName("parametric/filtersParametroReportes");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createFilterParametroReportes")
    public ModelAndView createFilterParametroReportes(@ModelAttribute FilterParametroReportes filtro,
                                                 @RequestParam(name = "paramId") String paramId,
                                                 @RequestParam(name = "selectedCampo") int campoId,
                                                 BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/filtersParametroReportes/" + paramId);

        ParametrosReportes parametro = parametrosReportesService.findById(Integer.parseInt(paramId));
        filtro.setParametroReportes(parametro);

        CampoParamReportes campoBusqueda = campoParametroReportesService.findById(campoId);
        filtro.setCampo(campoBusqueda);

        filterParametroReportesService.modificar(filtro);

        return modelAndView;

    }

    @DeleteMapping("/parametric/deleteFilterParametroReportes/{id}")
    public ResponseEntity<?> deleteFilterParametroReportes(@PathVariable int id) {
        try {
            filterParametroReportesService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }


}
