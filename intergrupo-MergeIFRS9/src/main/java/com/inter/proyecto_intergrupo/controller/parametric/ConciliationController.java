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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ConciliationController {
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

    @GetMapping(value="/parametric/conciliation")
    public ModelAndView showConciliation(@RequestParam Map<String, Object> params) {
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
            modelAndView.addObject("directory","conciliation");
            modelAndView.addObject("registers",conciliations.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/conciliation");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/createConciliation")
    public ModelAndView showCreateConciliation(){
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

        modelAndView.setViewName("/parametric/createConciliation");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/modifyConciliation/{id}")
    public ModelAndView showModifyConciliation(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Conciliation concil = conciliationService.findById(id);

        List<Country> allCountries = countryService.findAll();
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        List<SourceSystem> allSFCCs = sourceSystemService.findAll();
        List<Currency> allDivisas = currencyService.findAll();
        List<AccountingRoute> rutasContables = accountingRouteService.findAll();

        //LISTAS QUE VAN A SER LLENADAS POR FRONT
        List<CampoRC> campoCentro = concil.getRutaContable().getCampos();
        List<CampoRC> campoCuenta = concil.getRutaContable().getCampos();
        List<CampoRC> campoDivisa = concil.getRutaContable().getCampos();
        List<CampoRC> campoSaldo = concil.getRutaContable().getCampos();
        modelAndView.addObject("rutasContables", rutasContables);
        modelAndView.addObject("camposCentro",campoCentro);
        modelAndView.addObject("camposCuenta",campoCuenta);
        modelAndView.addObject("camposDivisa",campoDivisa);
        modelAndView.addObject("camposSaldo",campoSaldo);

        modelAndView.addObject("paises", allCountries);
        modelAndView.addObject("sfs", allSFs);
        modelAndView.addObject("sfcs", allSFCCs);
        modelAndView.addObject("divisas", allDivisas);
        modelAndView.addObject("rutasContables", rutasContables);
        modelAndView.addObject("concil",concil);
        modelAndView.setViewName("/parametric/modifyConciliation");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createConcil")
    public ModelAndView createConciliation(@ModelAttribute Conciliation conciliacion,
                                           @RequestParam(name = "selectedPais") String pais,
                                           @RequestParam(name = "selectedSF") String idsf,
                                           @RequestParam(name = "selectedSFC") String idsfc,
                                           @RequestParam(name = "selectedRutaContable") String rutaContId,
                                           @RequestParam(name = "selectedCentro") String centro,
                                           @RequestParam(name = "selectedCuenta") String divisa,
                                           @RequestParam(name = "selectedDivisa") String cuenta,
                                           @RequestParam(name = "selectedSaldo") String saldo,
                                           BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/conciliation/");

        Conciliation concil = conciliationService.findById(conciliacion.getId());
        if(concil != null){
            bindingResult
                    .rejectValue("Conciliacion", "error.conciliacion",
                            "Conciliacion ya se ha registrado");
        }

        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createConciliation");
        }else{
            concil=conciliacion;
            concil.setNombre(concil.getDetalle());

            Country paisSeleccionado = countryService.findCountryByName(pais).get(0);
            concil.setPais(paisSeleccionado);
            SourceSystem sistema = sourceSystemService.findById(Integer.valueOf(idsf));
            concil.setSf(sistema);
            SourceSystem sistemaContable = sourceSystemService.findById(Integer.valueOf(idsfc));
            concil.setSfc(sistemaContable);
            AccountingRoute ruta = accountingRouteService.findById(Integer.valueOf(rutaContId));
            concil.setRutaContable(ruta);

            concil.setCentro((campoRCService.findById(Integer.valueOf(centro)).getNombre()));
            concil.setCuenta((campoRCService.findById(Integer.valueOf(divisa)).getNombre()));
            concil.setDivisa((campoRCService.findById(Integer.valueOf(cuenta)).getNombre()));
            concil.setSaldo((campoRCService.findById(Integer.valueOf(saldo)).getNombre()));
            conciliationService.modificarConciliacion(concil);
        }

        modelAndView.addObject("resp", "Add1");
        modelAndView.addObject("data", conciliacion.getNombre());
        return modelAndView;
    }

    @GetMapping(value = "/parametric/validatePrincipal")
    @ResponseBody
    public List<String> validatePrincipal(@RequestParam("principalSelect") String principalSelect) {
        List<String> response = campoRConcilService.validatePrincipal(principalSelect);
        return response;
    }

    @GetMapping(value = "/parametric/validatePrincipal2")
    @ResponseBody
    public List<String> validatePrincipal2(@RequestParam("principalSelect") String principalSelect) {
        List<String> response = campoRConcilService.validatePrincipal2(principalSelect);
        return response;
    }

    @GetMapping(value = "/parametric/accountsConciliation/{id}")
    public ModelAndView cargueCuentas(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Conciliation conciliacion = conciliationService.findById(id);
        List<AccountConcil> allCuentas = accountConcilService.findByEstadoAndConciliacion(conciliacion);

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()),allCuentas.size());

        for(AccountConcil cuenta:allCuentas){
            System.out.println(cuenta.getValor());
        }
        Page<AccountConcil> pageConciliation = new PageImpl<>(allCuentas.subList(start, end), pageRequest, allCuentas.size());

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","accountsConciliation/"+id);
        modelAndView.addObject("allCuentas",pageConciliation.getContent());
        modelAndView.addObject("registers",allCuentas.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        modelAndView.addObject("p_modificar", p_modificar);

        AccountConcil cuenta = new AccountConcil();
        modelAndView.addObject("cuenta",cuenta);
        modelAndView.addObject("conciliacion",conciliacion);

        modelAndView.setViewName("parametric/accountsConciliation");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/deleteAccountConcil/{idConcil}/{idCuenta}")
    public ModelAndView deleteAccountConcil(@PathVariable int idConcil, @PathVariable int idCuenta){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountsConciliation/"+idConcil);
        try {
            accountConcilService.eliminar(idCuenta);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyConcil")
    public ModelAndView modifyConcil(@ModelAttribute Conciliation concil,
                                     @RequestParam(name = "selectedPais") String pais,
                                     @RequestParam(name = "selectedSF") String idsf,
                                     @RequestParam(name = "selectedSFC") String idsfc,
                                     @RequestParam(name = "selectedRutaContable") String rutaContId,
                                     @RequestParam(name = "selectedCentro") String centro,
                                     @RequestParam(name = "selectedCuenta") String divisa,
                                     @RequestParam(name = "selectedDivisa") String cuenta,
                                     @RequestParam(name = "selectedSaldo") String saldo,
                                     BindingResult bindingResult){

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/conciliation/");
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createConciliation");
        }else {

            concil.setNombre(concil.getDetalle());

            Country paisSeleccionado = countryService.findCountryByName(pais).get(0);
            concil.setPais(paisSeleccionado);
            SourceSystem sistema = sourceSystemService.findById(Integer.valueOf(idsf));
            concil.setSf(sistema);
            SourceSystem sistemaContable = sourceSystemService.findById(Integer.valueOf(idsfc));
            concil.setSfc(sistemaContable);
            AccountingRoute ruta = accountingRouteService.findById(Integer.valueOf(rutaContId));
            concil.setRutaContable(ruta);

            concil.setCentro((campoRCService.findById(Integer.valueOf(centro)).getNombre()));
            concil.setCuenta((campoRCService.findById(Integer.valueOf(divisa)).getNombre()));
            concil.setDivisa((campoRCService.findById(Integer.valueOf(cuenta)).getNombre()));
            concil.setSaldo((campoRCService.findById(Integer.valueOf(saldo)).getNombre()));
            conciliationService.modificarConciliacion(concil);
        }

        modelAndView.addObject("resp", "Modify1");
        modelAndView.addObject("data", concil.getNombre());
        return modelAndView;
    }

    @GetMapping(value = "/parametric/searchConciliations")
    @ResponseBody
    public ModelAndView searchConciliations(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Conciliation> list;
        if(params==null) list=conciliationService.findAll();
        else list=conciliationService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Conciliation> pageTypeEntity = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageTypeEntity.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allConcils",pageTypeEntity.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchConciliations");
        modelAndView.addObject("registers",list.size());
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Conciliaciones");

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("p_modificar", p_modificar);



        modelAndView.setViewName("parametric/conciliation");
        return modelAndView;
    }


    //PROCESO DE GENERACION DE CONCILIACION
    @GetMapping(value="/parametric/generateConciliation")
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
            modelAndView.addObject("directory","generateConciliation");
            modelAndView.addObject("registers",logConciliacion.size());
            modelAndView.addObject("registersData",registros.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/generateConciliation");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }


    //GENERAR CONCILIACION
    //-----------------------------------

    @PostMapping("/parametric/conciliation/generateConciliation")
    @ResponseBody
    public ResponseEntity<String> generateConciliation(@RequestParam int id,
                                                 @RequestParam String fecha,
                                                 @RequestParam String fechaContabilidad) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Conciliation conciliacion = conciliationService.findById(id);
        //EventType evento = eventTypeService.findByName("Conciliación").get(0);

        System.out.println("GENERAR CONCILIACION");
        System.out.println("======================");
        try {
            String idCont = conciliationService.findFechaCont(id, fecha).get(0)[1].toString();
            conciliationService.generarConciliacion(conciliacion,fecha, fechaContabilidad, Integer.valueOf(idCont));
            conciliationService.loadLogConciliation(user, id, fecha, "Exitoso", "");
            return ResponseEntity.ok("Bulk--1");
        }
        catch (Exception e) {
            e.printStackTrace();
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause(); // Navega a la causa raíz
            }
            conciliationService.loadLogConciliation(user, id, fecha, "Fallido",rootCause.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk--2");
        }

    }

    @GetMapping("/parametric/obtenerFechaCont/{concilID}/{fechaInformacion}")
    @ResponseBody
    public List<Object[]> obtenerCamposRC(@PathVariable("concilID") Integer idRCont,
                                          @PathVariable("fechaInformacion") String fechaInformacion) {
        List<Object[]> campos = conciliationService.findFechaCont(idRCont, fechaInformacion);
        return campos;
    }

}
