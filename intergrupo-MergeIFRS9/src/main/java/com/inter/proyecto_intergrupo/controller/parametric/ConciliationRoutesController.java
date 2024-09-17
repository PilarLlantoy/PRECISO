package com.inter.proyecto_intergrupo.controller.parametric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
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

            List<ConciliationRoute> conciliations = conciliationRouteService.findAllActive();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), conciliations.size());
            Page<ConciliationRoute> pageConciliation = new PageImpl<>(conciliations.subList(start, end), pageRequest, conciliations.size());

            int totalPage=pageConciliation.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allCRoutes",pageConciliation.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","conciliationRoutes");
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
        Conciliation conciliation = conciliationService.findByName(concil);
        croute.setConciliacion(conciliation);
        croute.setTipoArchivo(tipoArchivo);
        croute.setFormatoFecha(formatoFecha);
        croute.setIdiomaFecha(idiomaFecha);
        conciliationRouteService.modificar(croute);

        return modelAndView;

    }

    @GetMapping(value = "/parametric/modifyConciliationRoute/{id}")
    public ModelAndView modifyAccountingRoute(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        ConciliationRoute croute = conciliationRouteService.findById(id);
        List<Conciliation> conciliations = conciliationService.findAllActive();
        modelAndView.addObject("croute",croute);
        modelAndView.addObject("conciliations",conciliations);

        modelAndView.setViewName("parametric/modifyConciliationRoute");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyConciliationRoute")
    public ModelAndView modifyConciliationRoute(@ModelAttribute ConciliationRoute croute,
                                                @RequestParam(name = "selectedConcil") String concil,
                                                @RequestParam(name = "selectedTipoArchivo") String tipoArchivo,
                                                @RequestParam(name = "selectedFormatoFecha") String formatoFecha,
                                                @RequestParam(name = "selectedIdiomaFecha") String idiomaFecha,
                                                BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/conciliationRoutes");
        Conciliation conciliation = conciliationService.findByName(concil);
        croute.setConciliacion(conciliation);
        croute.setTipoArchivo(tipoArchivo);
        croute.setFormatoFecha(formatoFecha);
        croute.setIdiomaFecha(idiomaFecha);
        conciliationRouteService.modificar(croute);
        return modelAndView;
    }

    @GetMapping(value = "/parametric/fieldLoadingConciliationRoute/{id}")
    public ModelAndView cargueCampos(@PathVariable int id, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        ConciliationRoute croute = conciliationRouteService.findById(id);
        modelAndView.addObject("croute",croute);

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), croute.getCampos().size());
        List<CampoRConcil> loscampos = croute.getCampos();
        Page<CampoRConcil> pageConciliation = new PageImpl<>(loscampos.subList(start, end), pageRequest, loscampos.size());

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","fieldLoadingConciliationRoute/"+id);
        modelAndView.addObject("allCampos",pageConciliation.getContent());
        modelAndView.addObject("registers",loscampos.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Conciliaciones");
        modelAndView.addObject("p_modificar", p_modificar);
        CampoRConcil campoRConcil = new CampoRConcil();
        modelAndView.addObject("campoRC",campoRConcil);

        modelAndView.setViewName("parametric/fieldLoadingConciliationRoute");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/validationLoadingConciliationRoute/{id}")
    public ModelAndView cargueValidaciones(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        ConciliationRoute croute = conciliationRouteService.findById(id);
        modelAndView.addObject("croute",croute);
        List<ValidationRConcil> validaciones = croute.getValidaciones();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), validaciones.size());

        Page<ValidationRConcil> pageConciliation = new PageImpl<>(validaciones.subList(start, end), pageRequest, validaciones.size());
        List<CampoRConcil> campos = croute.getCampos();
        modelAndView.addObject("campos",campos);
        List<CampoRConcil> camposRef = croute.getCampos();
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
        modelAndView.addObject("directory","validationLoadingConciliationRoute/"+id);
        modelAndView.addObject("allValidaciones",pageConciliation.getContent());
        modelAndView.addObject("registers",validaciones.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        modelAndView.addObject("p_modificar", p_modificar);
        ValidationRConcil validationRC = new ValidationRConcil();
        modelAndView.addObject("validationRC",validationRC);


        modelAndView.setViewName("parametric/validationLoadingConciliationRoute");
        return modelAndView;
    }

    @GetMapping("/leer-archivo-rconcil")
    @ResponseBody
    public void leerArchivoTXT(@RequestParam String id) throws IOException {
        ConciliationRoute route = conciliationRouteService.findById(Integer.valueOf(id));
        String filePath = route.getRuta();
        List<CampoRConcil> campos = route.getCampos();
        List<ValidationRConcil> validaciones = route.getValidaciones();
        List<Map<String, String>> lineasMap = new ArrayList<>();
        String rutaArchivoFormato = "D:\\archivo.fmt"; // Cambia esto a la ruta deseada

        try {
            conciliationRouteService.createTableTemporal(route, campos);
            conciliationRouteService.generarArchivoFormato(campos, rutaArchivoFormato);
            conciliationRouteService.bulkImport(route,rutaArchivoFormato);
            conciliationRouteService.validationData(route);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }

}
