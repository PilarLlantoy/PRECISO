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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class CamposParametrosReportesController {
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

    @Autowired
    private ValidationParametroReportesService validationParametroReportesService;

    @Autowired
    private ResultingFieldsParametroReportesService resultingFieldsParametroReportesService;

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

}
