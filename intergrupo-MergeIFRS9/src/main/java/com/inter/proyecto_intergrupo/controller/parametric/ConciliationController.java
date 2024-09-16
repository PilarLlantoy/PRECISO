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
    private SourceSystemService sourceSystemService;

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
        List<AccountingRoute> rutasContables = accountingRouteService.findAll();
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
                                           @RequestParam(name = "selectedPeriodicidad") String periodicidad,
                                           @RequestParam(name = "selectedPais") String pais,
                                           @RequestParam(name = "selectedSF") String sf,
                                           @RequestParam(name = "selectedSFC") String sfc,
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
            concil.setPeriodicidad(periodicidad);
            Country paisSeleccionado = countryService.findCountryByName(pais);
            concil.setPais(paisSeleccionado);
            SourceSystem sistema = sourceSystemService.findByNombre(sf);
            concil.setSf(sistema);
            SourceSystem sistemaContable = sourceSystemService.findByNombre(sfc);
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("User authenticated: " + auth.getName());
        System.out.println("Received principalSelect: " + principalSelect);

        List<String> response = campoRCService.validatePrincipal(principalSelect);
        System.out.println("Response from service: " + response);
        return response;
    }

    @GetMapping(value = "/parametric/validatePrincipal2")
    @ResponseBody
    public List<String> validatePrincipal2(@RequestParam("principalSelect") String principalSelect) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("User authenticated: " + auth.getName());
        System.out.println("Received principalSelect: " + principalSelect);

        List<String> response = campoRCService.validatePrincipal2(principalSelect);
        System.out.println("Response from service: " + response);
        return response;
    }



}
