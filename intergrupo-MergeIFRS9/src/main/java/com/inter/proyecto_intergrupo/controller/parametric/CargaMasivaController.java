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
public class CargaMasivaController {
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

    //PROCESO DE CARGA MASIVA
    @GetMapping(value="/parametric/cargaMasiva")
    public ModelAndView showGenerateConciliation(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Cargue Contable");
        if(userService.validateEndpoint(user.getId(),"Ver Cargue Contable")) {
            List<Conciliation> listConcil = conciliationService.findAllActive();
            List<Object[]> registros = new ArrayList<>();
            List<String> colRegistros = new ArrayList<>();
            List<LogConciliation> logConciliacion = new ArrayList<>();

            if(params.get("arhcont") != null && params.get("arhcont").toString() != null
                    && params.get("period") != null && params.get("period").toString() != null
                    && params.get("period2") != null && params.get("period2").toString() != null)
            {
                System.out.println("periodo " +params.get("period2"));
                modelAndView.addObject("period",params.get("period").toString());
                modelAndView.addObject("period2",params.get("period2").toString());
                Conciliation concil = conciliationService.findById(Integer.parseInt(params.get("arhcont").toString()));
                modelAndView.addObject("arhcont",concil);
                colRegistros = List.of("FECHA", "CENTRO CONTABLE", "CUENTA CONTABLE","DIVISA CUENTA","SALDO INVENTARIO", "SALDO CONTABLE", "TOTAL");;
                registros = conciliationService.processList(conciliationService.findAllData(concil,params.get("period").toString()),colRegistros);
                logConciliacion = conciliationService.findAllLog(concil,params.get("period").toString());

            }
            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            int start = (int) pageRequest.getOffset();

            int end = Math.min((start + pageRequest.getPageSize()), logConciliacion.size());
            Page<LogConciliation> pageLog= new PageImpl<>(logConciliacion.subList(start, end), pageRequest, logConciliacion.size());
            int totalPage=pageLog.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }

            int pageData=params.get("pageData")!=null?(Integer.valueOf(params.get("pageData").toString())-1):0;
            PageRequest pageRequestData=PageRequest.of(pageData,PAGINATIONCOUNTDATA);
            int startData = (int) pageRequestData.getOffset();
            int endData = Math.min((startData + pageRequestData.getPageSize()), registros.size());
            Page<Object[]> pageLogData= new PageImpl<>(registros.subList(startData, endData), pageRequestData, registros.size());
            int totalPageData=pageLogData.getTotalPages();
            if(totalPageData>0){
                List<Integer> pagesData = IntStream.rangeClosed(1, totalPageData).boxed().collect(Collectors.toList());
                modelAndView.addObject("pagesData",pagesData);
            }

            modelAndView.addObject("allLog",pageLog.getContent());
            modelAndView.addObject("allRCs",pageLogData.getContent());
            modelAndView.addObject("allColRCs",colRegistros);
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("currentData",pageData+1);
            modelAndView.addObject("nextData",pageData+2);
            modelAndView.addObject("prevData",pageData);
            modelAndView.addObject("lastData",totalPageData);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("listConcil",listConcil);
            modelAndView.addObject("directory","cargaMasiva");
            modelAndView.addObject("registers",logConciliacion.size());
            modelAndView.addObject("registersData",registros.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/cargaMasiva");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }



}
