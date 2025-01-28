package com.inter.proyecto_intergrupo.controller.parametric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ParametrosReportesController {
    private static final int PAGINATIONCOUNT=12;
    //private static final int PAGINATIONCOUNT=5;
    private static final int PAGINATIONCOUNTDATA=500;

    private List<String> listColumns=List.of("Nombre", "Estado", "Sistema Fuente", "Fuente Contable");

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private AccountConcilService accountConcilService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @Autowired
    private CampoRConcilService campoRConcilService;

    @Autowired
    private MasterInventService masterInventService;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private InformationCrossingService informationCrossingService;

    @Autowired
    private CampoRCService campoRCService;

    @GetMapping(value="/parametric/parametrosReportes")
    public ModelAndView parametrosReportes(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Conciliaciones");
        if(userService.validateEndpoint(user.getId(),"Ver Conciliaciones")) {

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<Conciliation> conciliations = conciliationService.findAll();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), conciliations.size());
            Page<Conciliation> pageConciliation = new PageImpl<>(conciliations.subList(start, end), pageRequest, conciliations.size());

            int totalPage=pageConciliation.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allConcils",pageConciliation.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("columns",listColumns);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","parametrosReportes");
            modelAndView.addObject("registers",conciliations.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/parametrosReportes");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/createParametroReportes")
    public ModelAndView createParametroReportes(){
        ModelAndView modelAndView = new ModelAndView();
        Conciliation concil = new Conciliation();

        List<Country> allCountries = countryService.findAllActiveCountries();
        List<SourceSystem> allSFs = sourceSystemService.findAllActive();
        List<SourceSystem> allSFCCs = sourceSystemService.findAllActive();
        modelAndView.addObject("paises", allCountries);
        modelAndView.addObject("sfs", allSFs);
        modelAndView.addObject("sfcs", allSFCCs);
        modelAndView.addObject("concil",concil);

        //LISTAS QUE VAN A SER LLENADAS POR FRONT
        List<AccountingRoute> rutasContables = null;
        List<CampoRC> campoCentro = null;
        List<CampoRC> campoCuenta = null;
        List<CampoRC> campoDivisa = null;
        List<CampoRC> campoSaldo = null;
        modelAndView.addObject("rutasContables", rutasContables);
        modelAndView.addObject("camposCentro",campoCentro);
        modelAndView.addObject("camposCuenta",campoCuenta);
        modelAndView.addObject("camposDivisa",campoDivisa);
        modelAndView.addObject("camposSaldo",campoSaldo);

        modelAndView.setViewName("/parametric/createParametroReportes");
        return modelAndView;
    }


}
