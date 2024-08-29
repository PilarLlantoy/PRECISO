package com.inter.proyecto_intergrupo.controller.parametric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.TipoDocumento;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.admin.View;
import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.model.parametric.SourceSystem;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountingRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.CountryService;
import com.inter.proyecto_intergrupo.service.parametricServices.SourceSystemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
public class AccountingRoutesController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private AccountingRouteService conciliationService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @GetMapping(value="/parametric/accountingRoutes")
    public ModelAndView showAccountingRoutes(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        if(userService.validateEndpoint(user.getId(),"Ver Países")) { //CAMBIAR A VER Conciliaciones

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<AccountingRoute> aroutes = conciliationService.findAllActive();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), aroutes.size());
            Page<AccountingRoute> pageAR= new PageImpl<>(aroutes.subList(start, end), pageRequest, aroutes.size());

            int totalPage=pageAR.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allRCs",pageAR.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","country");
            modelAndView.addObject("registers",aroutes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/accountingRoutes");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }


    @GetMapping(value = "/parametric/createAccountingRoute")
    public ModelAndView showCreateAccountingRoute(){
        ModelAndView modelAndView = new ModelAndView();
        AccountingRoute aroute = new AccountingRoute();
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        modelAndView.addObject("allSFs", allSFs);
        modelAndView.addObject("aroute",aroute);
        modelAndView.setViewName("/parametric/createAccountingRoute");
        return modelAndView;
    }

    /*
    @GetMapping(value = "/parametric/modifyCountry/{id}")
    @ResponseBody
    public ModelAndView modifyCountry(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Country toModify = countryService.findCountryById(id);
        modelAndView.addObject("countryModify",toModify);
        modelAndView.addObject("paisId",toModify.getId());
        modelAndView.setViewName("parametric/modifyCountry");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyCountry")
    public ModelAndView updateCountry(@ModelAttribute Country pais){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
        countryService.modificarCountry(pais);
        return modelAndView;
    }

    @PostMapping(value = "/parametric/deleteCountry/{id}")
    public ModelAndView deleteCountry(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
        try {
            Country pais = countryService.findCountryById(id);
            pais.setEstado(false);
            countryService.modificarCountry(pais);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }

*/

    /*

            @GetMapping(value = "/parametric/validateIdCountry")
            @ResponseBody
            public String validateIdCountry(@RequestParam String idNew,@RequestParam String idT){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String result = "invalid";
                if(countryService.findCountryById(idNew)==null||idNew.equals(idT))
                    result="valid";
                return  result;
            }

            @GetMapping(value = "/parametric/removeCountry/{id}")
            @ResponseBody
            public boolean removeCountry(@PathVariable String id){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User user = userService.findUserByUserName(auth.getName());
                ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
                boolean response=false;
                try {
                    Country toRemove = countryService.findCountryById(id);
                    countryService.removeCountry(toRemove.getId()+"",user);
                    response=true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return  response;

            }

            @GetMapping(value = "/parametric/clearCountry")
            @ResponseBody
            public boolean clearCountry(){
                boolean response=false;
                ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User user = userService.findUserByUserName(auth.getName());
                try{
                    countryService.clearCountry(user);
                    response=true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return  response;
            }

            @GetMapping(value = "/parametric/country/download")
            @ResponseBody
            public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
                response.setContentType("application/octet-stream");
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String currentDateTime = dateFormatter.format(new Date());

                String headerKey = "Content-Disposition";
                String headerValue = "attachment; filename=País_" + currentDateTime + ".xlsx";
                response.setHeader(headerKey, headerValue);
                List<Country> countryList= new ArrayList<Country>();
                if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
                    countryList = countryService.findAll();
                }
                else{
                    countryList = countryService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
                }
                CountryListReport listReport = new CountryListReport(countryList);
                listReport.export(response);
            }

        */




    @PostMapping(value = "/parametric/createAccountingRoute")
    public ModelAndView createAccountingRoute(
            @ModelAttribute AccountingRoute aroute,
            @RequestParam(name = "selectedSF") String sistFuente,
            @RequestParam(name = "selectedTipoArchivo") String tipoArch,
            @RequestParam(name = "selectedFormatoFecha") String formFecha,
            @RequestParam(name = "selectedIdiomaFecha") String idiomFecha,

            BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountingRoutes");
        AccountingRoute arouteExists = accountingRouteService.findById(aroute.getId());
        if(arouteExists != null){
            bindingResult
                    .rejectValue("pais", "error.pais",
                            "El pais ya se ha registrado");
        }
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createAccountingRoute");
        }else{
            SourceSystem SF = sourceSystemService.findByNombre(sistFuente);
            aroute.setSfrc(SF);
            aroute.setTipoArchivo(tipoArch);
            aroute.setFormatoFecha(formFecha);
            aroute.setIdiomaFecha(idiomFecha);
            accountingRouteService.modificar(aroute);
        }
        return modelAndView;

    }

    @GetMapping(value = "/parametric/modifyAccountingRoute/{id}")
    public ModelAndView modifyAccountingRoute(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        AccountingRoute aroute = accountingRouteService.findById(id);
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        modelAndView.addObject("allSFs", allSFs);
        modelAndView.addObject("aroute",aroute);
        modelAndView.setViewName("parametric/modifyAccountingRoute");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/fieldLoadingAccountingRoute/{id}")
    public ModelAndView cargueCampos(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        AccountingRoute aroute = accountingRouteService.findById(id);
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        modelAndView.addObject("allSFs", allSFs);
        modelAndView.addObject("aroute",aroute);
        CampoRC campoRC = new CampoRC();
        modelAndView.addObject("campoRC",campoRC);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        modelAndView.addObject("p_modificar", p_modificar);
        modelAndView.setViewName("parametric/fieldLoadingAccountingRoute");
        return modelAndView;
    }




}
