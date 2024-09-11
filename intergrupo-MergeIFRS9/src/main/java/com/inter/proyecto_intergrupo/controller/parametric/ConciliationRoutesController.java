package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ConciliationRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.ConciliationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ConciliationRoutesController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @GetMapping(value="/parametric/conciliationRoutes")
    public ModelAndView showConciliation(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Conciliaciones");
        if(userService.validateEndpoint(user.getId(),"Ver Rutas Conciliaciones")) { //CAMBIAR A VER Conciliaciones

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
            modelAndView.addObject("allCountry",pageConciliation.getContent());
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
            modelAndView.setViewName("parametric/conciliationRoutes");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/createConciliationRoute")
    public ModelAndView showCreateConcilitionRoute(){
        ModelAndView modelAndView = new ModelAndView();
        ConciliationRoute croute = new ConciliationRoute();
        List<Conciliation> conciliations = conciliationService.findAllActive();
        modelAndView.addObject("croute",croute);
        modelAndView.addObject("conciliations",conciliations);
        modelAndView.setViewName("/parametric/createConciliationRoute");
        return modelAndView;
    }



    @PostMapping(value = "/parametric/createConciliationRoute")
    public ModelAndView createConciliationRoute(@ModelAttribute ConciliationRoute croute,
                                                @RequestParam(name = "selectedConcil") String concil,
                                                @RequestParam(name = "selectedTipoArchivo") String tipoArchivo,
                                                @RequestParam(name = "selectedFormatoFecha") String formatoFecha,
                                                @RequestParam(name = "selectedIdiomaFecha") String idiomaFecha,
                                                BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/conciliationRoutes");
        System.out.println("AYUDAAAAAAAAAA");
        Conciliation conciliation = conciliationService.findByName(concil);
        croute.setConciliacion(conciliation);
        croute.setTipoArchivo(tipoArchivo);
        croute.setFormatoFecha(formatoFecha);
        croute.setIdiomaFecha(idiomaFecha);
        conciliationRouteService.modificar(croute);

        return modelAndView;

    }



}
