package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountingRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.ConciliationRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.EventTypeService;
import com.inter.proyecto_intergrupo.service.parametricServices.GeneralParamService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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
public class GeneralParamController {
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private GeneralParamService generalParamService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @GetMapping(value="/parametric/generalParam")
    public ModelAndView showGeneralParam(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Parametro General");
        if(userService.validateEndpoint(user.getId(),"Ver Parametro General")) {
            List<GeneralParam> parametrics = generalParamService.findAll();
            modelAndView.addObject("allParametrics",parametrics);

            List<AccountingRoute> listCr0 = new ArrayList<>();
            List<AccountingRoute> crAll = accountingRouteService.findAllActive();
            if(crAll!=null)
                listCr0=crAll;
            modelAndView.addObject("parametricsData0",listCr0);
            modelAndView.addObject("parametricsData2",listCr0);
            modelAndView.addObject("parametricsData5",listCr0);

            List<CampoRC> listCr1 = new ArrayList<>();
            AccountingRoute cr1 = accountingRouteService.findByName(parametrics.get(0).getValorUnidad());
            if(cr1!=null)
                listCr1=cr1.getCampos();
            modelAndView.addObject("parametricsData1",listCr1);

            List<CampoRC> listCr3 = new ArrayList<>();
            AccountingRoute cr3 = accountingRouteService.findByName(parametrics.get(2).getValorUnidad());
            if(cr3!=null)
                listCr3=cr3.getCampos();
            modelAndView.addObject("parametricsData3",listCr3);

            List<CampoRC> listCr4 = new ArrayList<>();
            AccountingRoute cr4 = accountingRouteService.findByName(parametrics.get(2).getValorUnidad());
            if(cr4!=null)
                listCr4=cr4.getCampos();
            modelAndView.addObject("parametricsData4",listCr4);

            List<CampoRC> listCr6 = new ArrayList<>();
            AccountingRoute cr6 = accountingRouteService.findByName(parametrics.get(5).getValorUnidad());
            if(cr6!=null)
                listCr6=cr6.getCampos();
            modelAndView.addObject("parametricsData6",listCr6);

            List<CampoRC> listCr7 = new ArrayList<>();
            AccountingRoute cr7 = accountingRouteService.findByName(parametrics.get(5).getValorUnidad());
            if(cr7!=null)
                listCr7=cr7.getCampos();
            modelAndView.addObject("parametricsData7",listCr7);


            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/generalParam");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyGeneralParam")
    public ModelAndView updateGeneralParam(@ModelAttribute GeneralParam objeto){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/generalParam");
        generalParamService.modificar(objeto);
        return modelAndView;
    }

    @ResponseBody
    @PostMapping(value = "/parametric/generalParam/firstLevel", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, String>> searchGeneralParam(@RequestParam String dataLevel) {
        AccountingRoute crAll = accountingRouteService.findByName(dataLevel);
        return crAll.getCampos().stream()
                .map(campo -> Map.of("nombre", campo.getNombre())) // Ajusta según los campos relevantes de tu objeto.
                .collect(Collectors.toList());
    }

    @ResponseBody
    @PostMapping("/parametric/generalParam/save")
    public ResponseEntity<?> saveParams(@RequestBody Map<String, String> params) {
        generalParamService.modificarValor(Long.valueOf(1),params.get("centroContable").toString());
        generalParamService.modificarValor(Long.valueOf(2),params.get("centro").toString());
        generalParamService.modificarValor(Long.valueOf(3),params.get("cuentaContable").toString());
        generalParamService.modificarValor(Long.valueOf(4),params.get("cuenta").toString());
        generalParamService.modificarValor(Long.valueOf(5),params.get("conto").toString());
        generalParamService.modificarValor(Long.valueOf(6),params.get("cuentaDivisa").toString());
        generalParamService.modificarValor(Long.valueOf(7),params.get("cuentaD").toString());
        generalParamService.modificarValor(Long.valueOf(8),params.get("divisa").toString());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Parámetros guardados con éxito.");
        return ResponseEntity.ok(response);
    }


}
