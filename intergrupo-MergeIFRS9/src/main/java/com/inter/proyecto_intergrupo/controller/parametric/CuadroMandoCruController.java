package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@EnableScheduling
public class CuadroMandoCruController {
    private static final int PAGINATIONCOUNT=10;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private InformationCrossingService informationCrossingService;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @GetMapping(value="/parametric/cuadroMandoCru")
    public ModelAndView cuadroMandoCru(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Cuadro Mando Cruce");
        if(userService.validateEndpoint(user.getId(),"Ver Cuadro Mando Cruce")) {
            String fecha1 = LocalDate.now().toString();

            if (params.get("period") != null && !params.get("period").toString().equalsIgnoreCase(""))
                fecha1 = params.get("period").toString();

            List<Object[]> registrosLogVista = informationCrossingService.findAllLogByDate(fecha1);

            int page2 = params.get("page2") != null ? (Integer.valueOf(params.get("page2").toString()) - 1) : 0;
            PageRequest pageRequest2 = PageRequest.of(page2, PAGINATIONCOUNT);
            int start2 = (int) pageRequest2.getOffset();
            int end2 = Math.min((start2 + pageRequest2.getPageSize()), registrosLogVista.size());
            Page<Object[]> pageLog = new PageImpl<>(registrosLogVista.subList(start2, end2), pageRequest2, registrosLogVista.size());

            int totalPage2 = pageLog.getTotalPages();
            if (totalPage2 > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage2).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages2", pages);
            }
            modelAndView.addObject("period", fecha1);
            modelAndView.addObject("allLog2", pageLog.getContent());
            modelAndView.addObject("current2", page2 + 1);
            modelAndView.addObject("next2", page2 + 2);
            modelAndView.addObject("prev2", page2);
            modelAndView.addObject("last2", totalPage2);
            modelAndView.addObject("registers2", registrosLogVista.size());
            modelAndView.addObject("registersInventarios", registrosLogVista.size());


            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.addObject("directory", "cuadroMandoCru");
            modelAndView.setViewName("parametric/cuadroMandoCru");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }
    @PostMapping("/parametric/cuadroMandoCru/invMasive")
    public ModelAndView manejarSeleccionados(@RequestParam("idsSeleccionados") String idsSeleccionados,
                                             @RequestParam("period") String period) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/cuadroMandoCru");
        String[] ids = idsSeleccionados.split(",");
        modelAndView.addObject("period", period);
        if(idsSeleccionados.trim().length()!=0)
        {
            informationCrossingService.leerArchivosMasivo(ids,period);
            modelAndView.addObject("resp", "CM1");
        }
        else
        {
            modelAndView.addObject("resp", "CM-1");
        }
        return modelAndView;
    }
}
