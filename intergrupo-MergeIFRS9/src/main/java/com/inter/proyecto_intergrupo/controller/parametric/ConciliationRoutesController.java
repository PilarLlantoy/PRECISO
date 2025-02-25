package com.inter.proyecto_intergrupo.controller.parametric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ConciliationRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.ConciliationService;
import com.inter.proyecto_intergrupo.service.parametricServices.InformationCrossingService;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ConciliationRoutesController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    private List<String> listColumns=List.of("Conciliación","Nombre","Archivo","Ruta de Acceso","Tipo Archivo","Estado");

    @Autowired
    private UserService userService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private InformationCrossingService informationCrossingService;

    @GetMapping(value="/parametric/conciliationRoutes")
    public ModelAndView showConciliation(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Conciliaciones");
        if(userService.validateEndpoint(user.getId(),"Ver Rutas Conciliaciones")) {

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<ConciliationRoute> conciliations = conciliationRouteService.findAllActiveOrder();
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
            modelAndView.addObject("columns",listColumns);
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

    @GetMapping(value = "/parametric/searchConciliationRoutes")
    @ResponseBody
    public ModelAndView searchConciliationRoutes(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ConciliationRoute> list;
        if(params==null)
            list=conciliationRouteService.findByFilter("inactivo", "Estado");
        else
            list=conciliationRouteService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ConciliationRoute> pageTypeEntity = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageTypeEntity.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allCRoutes",pageTypeEntity.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchConciliationRoutes");
        modelAndView.addObject("registers",list.size());
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Conciliaciones");

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("p_modificar", p_modificar);

        modelAndView.setViewName("parametric/conciliationRoutes");
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
                                                @RequestParam(defaultValue = "N" ,name = "selectedConcil") String concil,
                                                @RequestParam(defaultValue = "N" ,name = "selectedTipoArchivo") String tipoArch,
                                                @RequestParam(defaultValue = "N" ,name = "selectedFormatoFecha") String formFecha,
                                                @RequestParam(defaultValue = "N" ,name = "selectedIdiomaFecha") String idiomFecha,
                                                @RequestParam(defaultValue = "N" ,name = "selecthoraCargue") String horaCargue,
                                                BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/conciliationRoutes");
        ConciliationRoute arouteExists = conciliationRouteService.findById(croute.getId());

        if(arouteExists != null){
            bindingResult
                    .rejectValue("Ruta Conciliacion", "error.rutaConciliacion",
                            "La ruta conciliacion ya se ha registrado");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            croute.setHoraCargue(LocalTime.parse(horaCargue, formatter));;

        } catch (Exception e) {
            System.out.println("Error parsing time: " + e.getMessage());
        }
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createConciliationRoute");
        }else{
            Conciliation conciliation = conciliationService.findByName(concil);
            croute.setConciliacion(conciliation);
            if(tipoArch!="N") croute.setTipoArchivo(tipoArch);
            if(formFecha!="N") croute.setFormatoFecha(formFecha);
            if(idiomFecha!="N") croute.setIdiomaFecha(idiomFecha);
            conciliationRouteService.modificar(croute);
        }
        modelAndView.addObject("resp", "Add1");
        modelAndView.addObject("data", croute.getDetalle());

        return modelAndView;

    }

    @GetMapping(value = "/parametric/modifyConciliationRoute/{id}")
    public ModelAndView modifyAccountingRoute(@PathVariable int id,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        ConciliationRoute croute = conciliationRouteService.findById(id);
        List<Conciliation> conciliations = conciliationService.findAllActive();
        modelAndView.addObject("croute",croute);
        modelAndView.addObject("conciliations",conciliations);
        if(params.get("page1")!=null && !params.get("page1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page1",params.get("page1").toString());
        else
            modelAndView.addObject("page1","1");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            String selecthoraCargue = croute.getHoraCargue().format(formatter);
            modelAndView.addObject("selecthoraCargue", selecthoraCargue);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        modelAndView.setViewName("parametric/modifyConciliationRoute");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyConciliationRoute")
    public ModelAndView modifyConciliationRoute(@ModelAttribute ConciliationRoute croute,
                                                @RequestParam(name = "selectedConcil") String concilId,
                                                @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/conciliationRoutes");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            croute.setHoraCargue(LocalTime.parse(params.get("selecthoraCargue").toString(), formatter));
            croute.setConciliacion(conciliationService.findById(Integer.valueOf(concilId)));
            conciliationRouteService.modificar(croute);
        } catch (Exception e) {
            System.out.println("Error parsing time: " + e.getMessage());
        }
        modelAndView.addObject("resp", "Modify1");
        modelAndView.addObject("data", croute.getDetalle());
        if(params.get("page")!=null && !params.get("page").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page",params.get("page").toString());
        else
            modelAndView.addObject("page","1");
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
        if(params.get("page1")!=null && !params.get("page1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page1",params.get("page1").toString());
        else
            modelAndView.addObject("page1","1");
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

        if(params.get("page1")!=null && !params.get("page1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page1",params.get("page1").toString());
        else
            modelAndView.addObject("page1","1");

        modelAndView.setViewName("parametric/validationLoadingConciliationRoute");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/informationCrossingConciliationRoute/{id}")
    public ModelAndView informationCrossingConciliationRoute(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        ConciliationRoute croute = conciliationRouteService.findById(id);
        modelAndView.addObject("croute",croute);

        List<CrossesConcilRoute> cruces = croute.getCruces();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), cruces.size());

        Page<CrossesConcilRoute> pageConciliation = new PageImpl<>(cruces.subList(start, end), pageRequest, cruces.size());

        List<ConciliationRoute> ficheros = conciliationRouteService.findFicherosActivos();
        modelAndView.addObject("ficheros",ficheros);

        List<CampoRConcil> camposInvActualiza = croute.getCampos();
        modelAndView.addObject("camposInvActualiza",camposInvActualiza);

        List<CampoRConcil> camposInvValid = croute.getCampos();
        modelAndView.addObject("camposInvValid",camposInvValid);

        //LISTAS QUE VAN A SER LLENADAS POR FRONT
        List<CampoRConcil> campoFicValid = null;
        List<CampoRConcil> campoFicResul = null;
        modelAndView.addObject("camposFicValid",campoFicValid);
        modelAndView.addObject("camposFicResul",campoFicResul);

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","informationCrossingConciliationRoute/"+id);
        modelAndView.addObject("allCruces",pageConciliation.getContent());
        modelAndView.addObject("registers",cruces.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        modelAndView.addObject("p_modificar", p_modificar);
        ValidationRConcil validationRC = new ValidationRConcil();
        modelAndView.addObject("validationRC",validationRC);

        if(params.get("page1")!=null && !params.get("page1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page1",params.get("page1").toString());
        else
            modelAndView.addObject("page1","1");

        modelAndView.setViewName("parametric/informationCrossingConciliationRoute");
        return modelAndView;
    }

    @GetMapping("/parametric/obtenerRutasConcilByConcil/{concilID}")
    @ResponseBody
    public List<Object[]> obtenerRutasConcilByConcil(@PathVariable("concilID") Integer concilID) {
        List<Object[]> campos = conciliationRouteService.findRutasByConcil(concilID);
        return campos;
    }

    @GetMapping("/parametric/obtenerTiposEventoByConcil/{concilID}")
    @ResponseBody
    public List<Object[]> obtenerTiposEventoByConcil(@PathVariable("concilID") Integer concilID) {
        List<Object[]> eventos = informationCrossingService.findEventosPorConcil(concilID);
        return eventos;
    }

    @GetMapping("/parametric/obtenerTiposEventoByConcilByInv/{concilID}/{invId}")
    @ResponseBody
    public List<Object[]> obtenerTiposEventoByConcilByInv(@PathVariable("concilID") Integer concilID, @PathVariable("invId") Integer invId) {
        List<Object[]> eventos = informationCrossingService.findEventosxConcilxInv(concilID, invId);
        return eventos;
    }

}
