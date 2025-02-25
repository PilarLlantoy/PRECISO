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
public class ValidationRCController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private CondicionRCService condicionRCService;

    @Autowired
    private CampoRCService campoRCService;

    @Autowired
    private ValidationRCService validationRCService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @GetMapping(value="/parametric/mostrarValidacionesRC")
    public ModelAndView mostrarValidacionesRC(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        if(userService.validateEndpoint(user.getId(),"Ver Países")) { //CAMBIAR A VER CargueCampos

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
            modelAndView.setViewName("parametric/conciliation");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/cargueValidacionesRC")
    public ModelAndView cargueValidacionesRC(){
        ModelAndView modelAndView = new ModelAndView();
        Campo campo = new Campo();
        modelAndView.addObject("campo",campo);
        modelAndView.setViewName("/parametric/cargueCampos");
        return modelAndView;
    }


    @PostMapping(value = "/parametric/createValidacionRC")
    public ModelAndView createValidacionRC(@ModelAttribute ValidationRC validationRC,
                                           @RequestParam(name = "selectedCampoRef") String campoRefid,
                                           @RequestParam(name = "selectedOperacion",defaultValue = "") String operacion,
                                           @RequestParam(name = "arouteId") String arouteId,
                                           BindingResult bindingResult, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/validationLoadingAccountingRoute/" + arouteId);
        try{
            AccountingRoute aroute = accountingRouteService.findById(Integer.parseInt(arouteId));
            validationRC.setRutaContable(aroute);
            CampoRC referencia = campoRCService.findById(Integer.valueOf(campoRefid));
            validationRC.setCampoRef(referencia);
            validationRC.setOperacion(operacion);
            validationRCService.modificar(validationRC);

            if(params.get("page1")!=null && !params.get("page1").toString().equalsIgnoreCase(""))
                modelAndView.addObject("page1", params.get("page1").toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return modelAndView;

    }
    @DeleteMapping("/parametric/deleteValidationRC/{id}")
    public ResponseEntity<?> deleteValidationRC(@PathVariable int id) {
        try {
            validationRCService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }
}
