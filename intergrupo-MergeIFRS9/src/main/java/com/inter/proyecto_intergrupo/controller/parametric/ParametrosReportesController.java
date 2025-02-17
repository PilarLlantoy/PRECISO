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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ParametrosReportesController {
    private static final int PAGINATIONCOUNT=12;
    //private static final int PAGINATIONCOUNT=5;
    private static final int PAGINATIONCOUNTDATA=500;


    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ParametrosReportesService parametrosReportesService;

    @Autowired
    private FilterParametroReportesService filterParametroReportesService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private CampoParametroReportesService campoParametroReportesService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private SourceParametroReportesService sourceParametroReportesService;

    @Autowired
    private AdditionalSourceParametroReportesService additionalSourceParametroReportesService;

    @Autowired
    private CampoRConcilService campoRConcilService;

    @Autowired
    private StructureParametroReportesService structureParametroReportesService;

    @GetMapping(value="/parametric/parametrosReportes")
    public ModelAndView parametrosReportes(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Conciliaciones");
        if(userService.validateEndpoint(user.getId(),"Ver Conciliaciones")) {

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<ParametrosReportes> parametros = parametrosReportesService.findAll();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), parametros.size());
            Page<ParametrosReportes> pageParametros = new PageImpl<>(parametros.subList(start, end), pageRequest, parametros.size());

            int totalPage=pageParametros.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allParametros",pageParametros.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","parametrosReportes");
            modelAndView.addObject("registers",parametros.size());
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
        ParametrosReportes parametro = new ParametrosReportes();

        List<Country> allCountries = countryService.findAllActiveCountries();
        modelAndView.addObject("paises", allCountries);
        modelAndView.addObject("parametro",parametro);

        modelAndView.setViewName("/parametric/createParametroReportes");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/modifyParametroReportes/{id}")
    public ModelAndView showModifyParametroReportes(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        ParametrosReportes parametro = parametrosReportesService.findById(id);

        List<Country> allCountries = countryService.findAll();
        modelAndView.addObject("paises", allCountries);

        modelAndView.addObject("parametro",parametro);
        modelAndView.setViewName("/parametric/modifyParametroReportes");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createParametrosReportes")
    public ModelAndView createConciliation(@ModelAttribute ParametrosReportes parametro,
                                           @RequestParam(defaultValue = "N", name = "selectedPais") String pais,
                                           BindingResult bindingResult){

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/parametrosReportes/");

        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createConciliation");
        }else{
            if(!pais.equalsIgnoreCase("N")){
                Country paisSeleccionado = countryService.findCountryByName(pais).get(0);
                parametro.setPais(paisSeleccionado);
            }
            parametrosReportesService.modificar(parametro);
        }

        modelAndView.addObject("resp", "Add1");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyParametrosReportes")
    public ModelAndView modifyParametrosReportes(@ModelAttribute ParametrosReportes parametro,
                                     @RequestParam(name = "selectedPais") String pais,
                                     BindingResult bindingResult){

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/parametrosReportes/");
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/modifyParametrosReportes");
        }else {

            Country paisSeleccionado = countryService.findCountryByName(pais).get(0);
            parametro.setPais(paisSeleccionado);
            parametrosReportesService.modificar(parametro);
        }

        modelAndView.addObject("resp", "Modify1");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/fieldLoadingParametrosReportes/{id}")
    public ModelAndView cargueCampos(@PathVariable int id, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();

        ParametrosReportes parametro = parametrosReportesService.findById(id);
        modelAndView.addObject("parametro",parametro);

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), parametro.getCampos().size());
        List<CampoParamReportes> loscampos = parametro.getCampos();
        Page<CampoParamReportes> pageConciliation = new PageImpl<>(loscampos.subList(start, end), pageRequest, loscampos.size());

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","fieldLoadingParametrosReportes/"+id);
        modelAndView.addObject("allCampos",pageConciliation.getContent());
        modelAndView.addObject("registers",loscampos.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Conciliaciones");
        modelAndView.addObject("p_modificar", p_modificar);

        CampoParamReportes campoParamReportes = new CampoParamReportes();
        modelAndView.addObject("campo", campoParamReportes);


        modelAndView.setViewName("parametric/fieldLoadingParametrosReportes");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createCampoParamReportes")
    public ModelAndView createCampoParamReportes(@ModelAttribute CampoParamReportes campoNuevo,
                                      @RequestParam(name = "paramId") String paramId,
                                      @RequestParam(name = "longitud") String longitud,
                                      BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fieldLoadingParametrosReportes/" + paramId);
        if(longitud!=null && longitud.length()>0 ){
            campoNuevo.setLongitud(longitud);
        }
        else {
            campoNuevo.setLongitud("MAX");
        }

        ParametrosReportes parametro = parametrosReportesService.findById(Integer.parseInt(paramId));
        campoNuevo.setParametroReportes(parametro);

        List<CampoParamReportes> campoBusqueda= campoParametroReportesService.findCamposByParametroVsDetalle(Integer.parseInt(paramId),campoNuevo.getDetalle());
        CampoParamReportes campoAntiguo= campoParametroReportesService.findById(campoNuevo.getId());
        System.out.println("Nuevo-> ID:"+campoNuevo.getId()+" - NOM:"+campoNuevo.getDetalle());
        if(campoAntiguo!=null)
            System.out.println("Antiguo-> ID:"+campoAntiguo.getId()+" - NOM:"+campoAntiguo.getDetalle());
        else
            System.out.println("Antiguo-> ID:Vacio - NOM:Vacio");
        if(!campoBusqueda.isEmpty())
            System.out.println("Busqueda-> ID:"+campoBusqueda.get(0).getId()+" - NOM:"+campoBusqueda.get(0).getDetalle());
        else
            System.out.println("Busqueda-> ID:Vacio - NOM:Vacio");
        if((campoBusqueda.isEmpty() && campoNuevo.getId()==0) ||
                (campoAntiguo!=null && campoAntiguo.getId() == campoNuevo.getId() && campoAntiguo.getDetalle().equalsIgnoreCase(campoNuevo.getDetalle()) && campoNuevo.getId()!=0) ||
                (campoAntiguo!=null && campoAntiguo.getId() == campoNuevo.getId() && !campoAntiguo.getDetalle().equalsIgnoreCase(campoNuevo.getDetalle()) && campoBusqueda.isEmpty() && campoNuevo.getId()!=0))
        {
            campoParametroReportesService.modificar(campoNuevo);
        }
        return modelAndView;

    }

    @DeleteMapping("/parametric/deleteCampoParamReportes/{id}")
    public ResponseEntity<?> deleteCampoParamReportes(@PathVariable int id) {
        try {
            ParametrosReportes param = campoParametroReportesService.findById(id).getParametroReportes();
            System.out.println("ID->"+id);
            campoParametroReportesService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }

    @GetMapping(value = "/parametric/filtersParametroReportes/{id}")
    public ModelAndView filtersParametroReportes(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        ParametrosReportes parametro = parametrosReportesService.findById(id);
        modelAndView.addObject("parametro",parametro);

        List<FilterParametroReportes> filtros = parametro.getFiltros();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filtros.size());

        Page<FilterParametroReportes> pageConciliation = new PageImpl<>(filtros.subList(start, end), pageRequest, filtros.size());
        List<CampoParamReportes> campos = parametro.getCampos();
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
        modelAndView.addObject("directory","filtersParametroReportes/"+id);
        modelAndView.addObject("allCondiciones",pageConciliation.getContent());
        modelAndView.addObject("registers",filtros.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Contables");
        modelAndView.addObject("p_modificar", p_modificar);


        FilterParametroReportes filtro = new FilterParametroReportes();
        modelAndView.addObject("filtro",filtro);


        modelAndView.setViewName("parametric/filtersParametroReportes");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/sourcesParametroReportes/{id}")
    public ModelAndView sourcesParametroReportes(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        ParametrosReportes parametro = parametrosReportesService.findById(id);
        modelAndView.addObject("parametro",parametro);

        List<SourceParametroReportes> fuentes = parametro.getFuentes();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), fuentes.size());

        Page<SourceParametroReportes> pageConciliation = new PageImpl<>(fuentes.subList(start, end), pageRequest, fuentes.size());


        List<Conciliation> conciliaciones = conciliationService.findAllActive();
        modelAndView.addObject("conciliaciones",conciliaciones);
        List<ConciliationRoute> inventarios = new ArrayList<>();
        modelAndView.addObject("inventarios",inventarios);
        List<EventType> eventos = new ArrayList<>();
        modelAndView.addObject("eventos",eventos);

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","sourcesParametroReportes/"+id);
        modelAndView.addObject("allFuentes",pageConciliation.getContent());
        modelAndView.addObject("registers",fuentes.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Contables");
        modelAndView.addObject("p_modificar", p_modificar);


        SourceParametroReportes fuente = new SourceParametroReportes();
        modelAndView.addObject("fuente",fuente);


        modelAndView.setViewName("parametric/sourcesParametroReportes");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createFilterParametroReportes")
    public ModelAndView createFilterParametroReportes(@ModelAttribute FilterParametroReportes filtro,
                                                 @RequestParam(name = "paramId") String paramId,
                                                 @RequestParam(name = "selectedCampo") int campoId,
                                                 BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/filtersParametroReportes/" + paramId);

        ParametrosReportes parametro = parametrosReportesService.findById(Integer.parseInt(paramId));
        filtro.setParametroReportes(parametro);

        CampoParamReportes campoBusqueda = campoParametroReportesService.findById(campoId);
        filtro.setCampo(campoBusqueda);

        filterParametroReportesService.modificar(filtro);

        return modelAndView;

    }

    @DeleteMapping("/parametric/deleteFilterParametroReportes/{id}")
    public ResponseEntity<?> deleteFilterParametroReportes(@PathVariable int id) {
        try {
            filterParametroReportesService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }


    @PostMapping(value = "/parametric/createSourceParametroReportes")
    public ModelAndView createSourceParametroReportes(@ModelAttribute SourceParametroReportes fuente,
                                          @RequestParam(name = "selectedConcil") int idConcil,
                                          @RequestParam(name = "selectedInventario") int idInv,
                                          @RequestParam(name = "selectedEvento") int idEvento,
                                          @RequestParam(name = "parametroId") String parametroId,
                                          BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/sourcesParametroReportes/" + parametroId);

        ParametrosReportes parametro = parametrosReportesService.findById(Integer.parseInt(parametroId));
        fuente.setParametroReportes(parametro);

        Conciliation concil = conciliationService.findById(idConcil);
        fuente.setFuente(concil);
        ConciliationRoute inventario = conciliationRouteService.findById(idInv);
        fuente.setInventario(inventario);
        EventType evento = eventTypeService.findAllById(idEvento);
        fuente.setEvento(evento);

        sourceParametroReportesService.modificar(fuente);

        return modelAndView;

    }


    @GetMapping(value = "/parametric/structuresParametroReportes/{id}/{fuenteId}")
    public ModelAndView structuresParametroReportes(@PathVariable int id,@PathVariable int fuenteId,
         @RequestParam Map<String, Object> params){

        ModelAndView modelAndView = new ModelAndView();

        SourceParametroReportes fuente = new SourceParametroReportes();
        if(fuenteId!=0)
            fuente = sourceParametroReportesService.findById(fuenteId);
        modelAndView.addObject("fuente",fuente);

        ParametrosReportes parametro = parametrosReportesService.findById(id);
        modelAndView.addObject("parametro",parametro);

        List<SourceParametroReportes> fuentes = parametro.getFuentes();
        modelAndView.addObject("fuentes",fuentes);

        List<CampoParamReportes> campos =  new ArrayList<>();
        if(fuenteId!=0) campos = parametro.getCampos();
        modelAndView.addObject("campos",campos);

        List<Object[]> camposRc = new ArrayList<>();
        if(fuenteId!=0) camposRc = campoRConcilService.findCamposByRutaConcil(fuente.getInventario().getId());
        modelAndView.addObject("camposRc",camposRc);

        List<StructureParametroReportes> allEstructuras = structureParametroReportesService.findByParamByFuente(id, fuenteId);
        if(allEstructuras.size()==0){
            for(int i=0; i<campos.size();i++){
                StructureParametroReportes estructura = new StructureParametroReportes();
                estructura.setParametroReportes(parametro);
                estructura.setFuente(fuente);
                estructura.setValorFormula("");
                estructura.setCampoReporte(campos.get(i));
                allEstructuras.add(estructura);
            }
            fuente.setEstructuras(allEstructuras);
        }

        modelAndView.addObject("allEstructuras",allEstructuras);

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), campos.size());

        Page<CampoParamReportes> pageConciliation = new PageImpl<>(campos.subList(start, end), pageRequest, campos.size());

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","structuresParametroReportes/"+id);
        modelAndView.addObject("campos",pageConciliation.getContent());
        modelAndView.addObject("registers",campos.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Contables");
        modelAndView.addObject("p_modificar", p_modificar);

        modelAndView.setViewName("parametric/structuresParametroReportes");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/additionalSourcesParametrosReportes/{id}")
    public ModelAndView additionalSourcesParametrosReportes(@PathVariable int id, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();

        ParametrosReportes parametro = parametrosReportesService.findById(id);
        modelAndView.addObject("parametro",parametro);

        List<Conciliation> conciliaciones = conciliationService.findAllActive();
        modelAndView.addObject("conciliaciones",conciliaciones);

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), parametro.getFuentesAdicionales().size());
        List<AdditionalSourceParametroReportes> loscampos = parametro.getFuentesAdicionales();
        Page<AdditionalSourceParametroReportes> pageConciliation = new PageImpl<>(loscampos.subList(start, end), pageRequest, loscampos.size());

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","additionalSourcesParametrosReportes/"+id);
        modelAndView.addObject("allCampos",pageConciliation.getContent());
        modelAndView.addObject("registers",loscampos.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Conciliaciones");
        modelAndView.addObject("p_modificar", p_modificar);

        AdditionalSourceParametroReportes fuente = new AdditionalSourceParametroReportes();
        modelAndView.addObject("fuente", fuente);


        modelAndView.setViewName("parametric/additionalSourcesParametrosReportes");
        return modelAndView;
    }

    @GetMapping("/parametric/obtenerCamposFuente/{fuenteId}")
    @ResponseBody
    public List<Object[]> obtenerCamposFuente(@PathVariable("fuenteId") Integer fuenteId) {
        List<Object[]> campos = campoParametroReportesService.findCamposByFuente(fuenteId);
        System.out.println(campos.size());
        return campos;
    }

    @PostMapping(value = "/parametric/createAdditionalSourceParametroReportes")
    public ModelAndView createAdditionalSourceParametroReportes(@ModelAttribute AdditionalSourceParametroReportes fuente,
                                          @RequestParam(name = "selectedConcil") int idConcil,
                                          @RequestParam(name = "selectedInventario") int idInv,
                                          @RequestParam(name = "selectedEvento") int idEvento,
                                          @RequestParam(name = "parametroId") String parametroId,
                                          BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/additionalSourcesParametrosReportes/" + parametroId);

        ParametrosReportes parametro = parametrosReportesService.findById(Integer.parseInt(parametroId));
        fuente.setParametroReportes(parametro);

        Conciliation concil = conciliationService.findById(idConcil);
        fuente.setFuente(concil);
        ConciliationRoute inventario = conciliationRouteService.findById(idInv);
        fuente.setInventario(inventario);
        EventType evento = eventTypeService.findAllById(idEvento);
        fuente.setEvento(evento);

        additionalSourceParametroReportesService.modificar(fuente);

        return modelAndView;

    }

    @PostMapping("/parametric/createStructureParametroReportes")
    public ModelAndView createStructureParametroReportes(
                                                   @RequestParam(name = "numCampos") int numCampos,
                                                   @RequestParam(name = "parametroId") int parametroId,
                                                   @RequestParam(name = "fuenteId") int fuenteId,
                                                   @RequestParam Map<String, String> params) {

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/structuresParametroReportes/" + parametroId+"/"+fuenteId);

        for(int i=1; i<numCampos+1; i++){

            StructureParametroReportes estructura = structureParametroReportesService.findById(Integer.valueOf(params.get("estructuraId_"+i)));

            CampoParamReportes campoReporte = campoParametroReportesService.findById(Integer.valueOf(params.get("campoReporte_"+i)));
            if(campoReporte!=null)
                estructura.setCampoReporte(campoReporte);

            if(params.get("campo1_"+i)!=null) {
                CampoRConcil campo1 = campoRConcilService.findById(Integer.valueOf(params.get("campo1_" + i)));
                estructura.setCampo1(campo1);
            }
            else estructura.setCampo1(null);

            estructura.setOperacion(params.get("operacion_"+i));

            if(params.get("campo2_"+i)!=null) {
                CampoRConcil campo2 = campoRConcilService.findById(Integer.valueOf(params.get("campo2_" + i)));
                estructura.setCampo2(campo2);
            }
            else estructura.setCampo2(null);

            if(Boolean.valueOf(params.get("aplicaFormula_"+i))==true)
                estructura.setAplicaFormula(true);
            else
                estructura.setAplicaFormula(false);

            estructura.setValorFormula(params.get("valorFormula_"+i));


            structureParametroReportesService.modificar(estructura);
        }

        return modelAndView;

    }

}
