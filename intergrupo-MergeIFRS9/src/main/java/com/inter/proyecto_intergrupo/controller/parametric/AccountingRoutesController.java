package com.inter.proyecto_intergrupo.controller.parametric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.TipoDocumento;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.admin.View;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountingRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.CampoRCService;
import com.inter.proyecto_intergrupo.service.parametricServices.CountryService;
import com.inter.proyecto_intergrupo.service.parametricServices.SourceSystemService;
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
public class AccountingRoutesController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private CampoRCService campoRCService;

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
    public ModelAndView cargueCampos(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        AccountingRoute aroute = accountingRouteService.findById(id);
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        modelAndView.addObject("allSFs", allSFs);
        modelAndView.addObject("aroute",aroute);

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), aroute.getCampos().size());
        List<CampoRC> loscampos = aroute.getCampos();
        Page<CampoRC> pageConciliation = new PageImpl<>(loscampos.subList(start, end), pageRequest, loscampos.size());

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","fieldLoadingAccountingRoute/"+id);
        modelAndView.addObject("allCampos",pageConciliation.getContent());
        modelAndView.addObject("registers",loscampos.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        modelAndView.addObject("p_modificar", p_modificar);
        CampoRC campoRC = new CampoRC();
        modelAndView.addObject("campoRC",campoRC);

        modelAndView.setViewName("parametric/fieldLoadingAccountingRoute");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/conditionLoadingAccountingRoute/{id}")
    public ModelAndView cargueCondiciones(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        AccountingRoute aroute = accountingRouteService.findById(id);
        modelAndView.addObject("aroute",aroute);
        List<CondicionRC> condiciones = aroute.getCondiciones();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), condiciones.size());

        Page<CondicionRC> pageConciliation = new PageImpl<>(condiciones.subList(start, end), pageRequest, condiciones.size());
        List<CampoRC> campos = aroute.getCampos();
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
        modelAndView.addObject("directory","conditionLoadingAccountingRoute/"+id);
        modelAndView.addObject("allCondiciones",pageConciliation.getContent());
        modelAndView.addObject("registers",condiciones.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        modelAndView.addObject("p_modificar", p_modificar);
        CondicionRC condicionRC = new CondicionRC();
        modelAndView.addObject("condicionRC",condicionRC);


        modelAndView.setViewName("parametric/conditionLoadingAccountingRoute");
        return modelAndView;
    }


    @GetMapping(value = "/parametric/validationLoadingAccountingRoute/{id}")
    public ModelAndView cargueValidaciones(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        AccountingRoute aroute = accountingRouteService.findById(id);
        modelAndView.addObject("aroute",aroute);
        List<ValidationRC> validaciones = aroute.getValidaciones();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), validaciones.size());

        Page<ValidationRC> pageConciliation = new PageImpl<>(validaciones.subList(start, end), pageRequest, validaciones.size());
        List<CampoRC> campos = aroute.getCampos();
        modelAndView.addObject("campos",campos);
        List<CampoRC> camposRef = aroute.getCampos();
        modelAndView.addObject("camposRef",camposRef);

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","validationLoadingAccountingRoute/"+id);
        modelAndView.addObject("allValidaciones",pageConciliation.getContent());
        modelAndView.addObject("registers",validaciones.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        modelAndView.addObject("p_modificar", p_modificar);
        ValidationRC validationRC = new ValidationRC();
        modelAndView.addObject("validationRC",validationRC);


        modelAndView.setViewName("parametric/validationLoadingAccountingRoute");
        return modelAndView;
    }

}
