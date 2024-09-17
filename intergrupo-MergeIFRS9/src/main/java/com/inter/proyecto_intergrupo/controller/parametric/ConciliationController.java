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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ConciliationController {
    private static final int PAGINATIONCOUNT=12;
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
    private CampoRCService campoRCService;

    @GetMapping(value="/parametric/conciliation")
    public ModelAndView showConciliation(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Conciliaciones");
        if(userService.validateEndpoint(user.getId(),"Ver Conciliaciones")) { //CAMBIAR A VER Conciliaciones

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<Conciliation> conciliations = conciliationService.findAllActive();
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
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","country");
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

        List<Country> allCountries = countryService.findAll();
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        List<SourceSystem> allSFCCs = sourceSystemService.findAll();
        List<Currency> allDivisas = currencyService.findAll();
        List<AccountingRoute> rutasContables = accountingRouteService.findAll();

        modelAndView.addObject("paises", allCountries);
        modelAndView.addObject("sfs", allSFs);
        modelAndView.addObject("sfcs", allSFCCs);
        modelAndView.addObject("divisas", allDivisas);
        modelAndView.addObject("rutasContables", rutasContables);


        modelAndView.addObject("concil",concil);
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
        List<CampoRC> campos = concil.getRutaContable().getCampos();
        List<AccountingRoute> rutasContables = accountingRouteService.findAll();
        modelAndView.addObject("paises", allCountries);
        modelAndView.addObject("campos", campos);
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
                                           @RequestParam(name = "selectedPeriodicidad") String periodicidad,
                                           @RequestParam(name = "selectedPais") String pais,
                                           @RequestParam(name = "selectedSF") String idsf,
                                           @RequestParam(name = "selectedSFC") String idsfc,
                                           @RequestParam(name = "selectedRutaContable") String rutaCont,
                                           @RequestParam(name = "centroSelect") String centro,
                                           @RequestParam(name = "divisaSelect") String divisa,
                                           @RequestParam(name = "cuentaSelect") String cuenta,
                                           @RequestParam(name = "saldoSelect") String saldo,
                                           BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/conciliation/");

        Conciliation concil = conciliationService.findById(conciliacion.getId());
        if(concil!=null) System.out.println("ERROR");
        else{
            concil=conciliacion;
            concil.setNombre(concil.getDetalle());
            concil.setPeriodicidad(periodicidad);
            Country paisSeleccionado = countryService.findCountryByName(pais);
            concil.setPais(paisSeleccionado);
            SourceSystem sistema = sourceSystemService.findById(Integer.valueOf(idsf));
            concil.setSf(sistema);
            SourceSystem sistemaContable = sourceSystemService.findById(Integer.valueOf(idsfc));
            concil.setSfc(sistemaContable);
            AccountingRoute ruta = accountingRouteService.findByName(rutaCont);
            concil.setRutaContable(ruta);

            concil.setCentro(centro);
            concil.setCuenta(cuenta);
            concil.setDivisa(divisa);
            concil.setSaldo(saldo);
            conciliationService.modificarConciliacion(concil);
        }


        return modelAndView;
    }

    @GetMapping(value = "/parametric/validatePrincipal")
    @ResponseBody
    public List<String> validatePrincipal(@RequestParam("principalSelect") String principalSelect) {
        System.out.println(principalSelect+"AYUDA");
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
        System.out.println(allCuentas.size());
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

        System.out.println(idConcil+idCuenta+"RA");
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountsConciliation/"+idConcil);
        try {
            AccountConcil cuenta = accountConcilService.findById(idCuenta);
            cuenta.setEstado(false);
            accountConcilService.modificar(cuenta);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyConcil")
    public ModelAndView modifyConcil(@ModelAttribute Conciliation concil,
                                           @RequestParam(name = "selectedPeriodicidad") String periodicidad,
                                           @RequestParam(name = "selectedPais") String pais,
                                           @RequestParam(name = "selectedSF") String idsf,
                                           @RequestParam(name = "selectedSFC") String idsfc,
                                           @RequestParam(name = "selectedRutaContable") String rutaCont,
                                           @RequestParam(name = "centroSelect") String centro,
                                           @RequestParam(name = "divisaSelect") String divisa,
                                           @RequestParam(name = "cuentaSelect") String cuenta,
                                           @RequestParam(name = "saldoSelect") String saldo,
                                           BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/conciliation/");

            concil.setNombre(concil.getDetalle());
            concil.setPeriodicidad(periodicidad);

        System.out.println(concil.getDetalle()+' '+concil.getPeriodicidad());
            Country paisSeleccionado = countryService.findCountryByName(pais);
            concil.setPais(paisSeleccionado);

            SourceSystem sistema = sourceSystemService.findById(Integer.valueOf(idsf));
            concil.setSf(sistema);

            SourceSystem sistemaContable = sourceSystemService.findById(Integer.valueOf(idsfc));
            concil.setSfc(sistemaContable);

            AccountingRoute ruta = accountingRouteService.findByName(rutaCont);
            concil.setRutaContable(ruta);

            concil.setCentro(centro);
            concil.setCuenta(cuenta);
            concil.setDivisa(divisa);
            concil.setSaldo(saldo);

            conciliationService.modificarConciliacion(concil);

        return modelAndView;
    }




}
