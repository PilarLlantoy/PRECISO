package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@EnableScheduling
public class CuadroMandoConsoController {
    private static final int PAGINATIONCOUNT=100;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private InformationCrossingService informationCrossingService;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @GetMapping(value="/parametric/cuadroMandoCons")
    public ModelAndView cuadroMandoCru(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Cuadro Mando Conciliación");
        if(userService.validateEndpoint(user.getId(),"Ver Cuadro Mando Conciliación")) {
            String fecha1 = LocalDate.now().toString();

            if (params.get("period") != null && !params.get("period").toString().equalsIgnoreCase(""))
                fecha1 = params.get("period").toString();

            List<Object[]> registrosLogVista = conciliationService.findAllLogByDate(fecha1);

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
            modelAndView.addObject("directory", "cuadroMandoCons");
            modelAndView.setViewName("parametric/cuadroMandoCons");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }
    @PostMapping("/parametric/cuadroMandoCons/invMasive")
    public ModelAndView manejarSeleccionados(@RequestParam("idsSeleccionados") String idsSeleccionados,
                                             @RequestParam("period") String fecha) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/cuadroMandoCons");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String[] ids = idsSeleccionados.split(",");
        modelAndView.addObject("period", fecha);
        if(idsSeleccionados.trim().length()!=0)
        {
            for (String id :ids)
            {
                try {
                    Conciliation conciliacion = conciliationService.findById(Integer.parseInt(id));
                    List<Object[]> fechaCont=conciliationService.findFechaCont(Integer.parseInt(id),fecha);
                    if(!fechaCont.isEmpty())
                    {
                        boolean resp = conciliationService.generarConciliacion(conciliacion, fecha, fechaCont.get(0)[0].toString(), conciliacion.getRutaContable().getId());
                        if(resp) {
                            if(informationCrossingService.findNovedadesAll(Integer.parseInt(id),fecha)) {
                                conciliationService.loadLogConciliation(user, Integer.parseInt(id), fecha, "Fallido", "Hay novedades en cruce de información", "Cargue Masivo");
                            }
                            else {
                                conciliationService.loadLogConciliation(user, Integer.parseInt(id), fecha, "Exitoso", "", "Cargue Masivo");
                            }
                        }else
                            conciliationService.loadLogConciliation(user, Integer.parseInt(id), fecha, "Fallido","No se encontraron cuentas a cruzar.'","Cargue Masivo");
                    }
                    else {
                        conciliationService.loadLogConciliation(user, Integer.parseInt(id), fecha, "Fallido","Falta completar el maestro de inventarios.'","Cargue Masivo");
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                    Throwable rootCause = e;
                    while (rootCause.getCause() != null) {
                        rootCause = rootCause.getCause(); // Navega a la causa raíz
                    }
                    conciliationService.loadLogConciliation(user, Integer.parseInt(id), fecha, "Fallido",rootCause.getMessage(),"Cargue Masivo");
                }
            }
            modelAndView.addObject("resp", "CM4");
        }
        else
        {
            modelAndView.addObject("resp", "CM-1");
        }
        return modelAndView;
    }
    @Scheduled(cron = "0 30 8-12/2 * * ?")
    public void jobConciliacion() {
        LocalDateTime fechaHoy = LocalDateTime.now();
        fechaHoy = fechaHoy.minusDays(1);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fecha = fechaHoy.format(formato);
        List<String> ids = conciliationService.findByJob(fecha);

        for (String id :ids)
        {
            try {
                Conciliation conciliacion = conciliationService.findById(Integer.parseInt(id));
                List<Object[]> fechaCont=conciliationService.findFechaCont(Integer.parseInt(id),fecha);
                if(!fechaCont.isEmpty())
                {
                    boolean resp = conciliationService.generarConciliacion(conciliacion, fecha, fechaCont.get(0)[0].toString(), conciliacion.getRutaContable().getId());
                    if(resp) {
                        if(informationCrossingService.findNovedadesAll(Integer.parseInt(id),fecha)) {
                            conciliationService.loadLogConciliation(null, Integer.parseInt(id), fecha, "Fallido", "Hay novedades en cruce de información", "Automàtico");
                        }
                        else {
                            conciliationService.loadLogConciliation(null, Integer.parseInt(id), fecha, "Exitoso", "", "Automàtico");
                        }
                    }else
                        conciliationService.loadLogConciliation(null, Integer.parseInt(id), fecha, "Fallido","No se encontraron cuentas a cruzar.'","Automàtico");
                }
                else {
                    conciliationService.loadLogConciliation(null, Integer.parseInt(id), fecha, "Fallido","Falta completar el maestro de inventarios.'","Automàtico");
                }

            }
            catch (Exception e) {
                e.printStackTrace();
                Throwable rootCause = e;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause(); // Navega a la causa raíz
                }
                conciliationService.loadLogConciliation(null, Integer.parseInt(id), fecha, "Fallido",rootCause.getMessage(),"Automàtico");
            }
        }

    }
}
