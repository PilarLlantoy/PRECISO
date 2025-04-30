package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.model.parametric.LogAccountingLoad;
import com.inter.proyecto_intergrupo.model.parametric.LogInventoryLoad;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountingLoadListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountingRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.ConciliationRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.SourceSystemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@EnableScheduling
public class CuadroMandoInvAndContController {
    private static final int PAGINATIONCOUNT=10;
    private static final int PAGINATIONCOUNTDATA=500;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @GetMapping(value="/parametric/cuadroMandoInvAndCont")
    public ModelAndView cuadroMandoInvAndCont(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Cargue Contable");
        if(userService.validateEndpoint(user.getId(),"Ver Cargue Contable")) {
            List<Object[]> aroutes = new ArrayList<>();
            List<Object[]> croutes = new ArrayList<>();
            List<Object[]> logAroutes = new ArrayList<>();
            List<Object[]> logCroutes = new ArrayList<>();
            String fecha1 = java.time.LocalDate.now().toString();
            String fecha2 = java.time.LocalDate.now().toString();

            if (params.get("tab") != null && params.get("tab").toString() != null)
                modelAndView.addObject("tab", params.get("tab").toString());
            if (params.get("period") != null && !params.get("period").toString().equalsIgnoreCase(""))
                fecha1 = params.get("period").toString();
            if (params.get("period2") != null && !params.get("period2").toString().equalsIgnoreCase(""))
                fecha2 = params.get("period2").toString();

            //CONTABLES
            //*-------------------------------------------------
            if (fecha2!=null) {
                logAroutes = accountingRouteService.findAllLogByDate(fecha2);

                int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
                PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
                int start = (int) pageRequest.getOffset();
                int end = Math.min((start + pageRequest.getPageSize()), logAroutes.size());
                Page<Object[]> pageLog = new PageImpl<>(logAroutes.subList(start, end), pageRequest, logAroutes.size());

                int totalPage = pageLog.getTotalPages();
                if (totalPage > 0) {
                    List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                    modelAndView.addObject("pages", pages);
                }
                modelAndView.addObject("period2",fecha2);
                modelAndView.addObject("allLog", pageLog.getContent());
                modelAndView.addObject("current", page + 1);
                modelAndView.addObject("next", page + 2);
                modelAndView.addObject("prev", page);
                modelAndView.addObject("last", totalPage);
                modelAndView.addObject("registers", logAroutes.size());
                modelAndView.addObject("registersInventarios", logCroutes.size());
                modelAndView.addObject("registersData", aroutes.size());
            }

            //INVENTARIOS
            //*-------------------------------------------------
            if (fecha1 != null) {
                logCroutes = conciliationRouteService.findAllLogByDate(fecha1);

                int page2 = params.get("page2") != null ? (Integer.valueOf(params.get("page2").toString()) - 1) : 0;
                PageRequest pageRequest2 = PageRequest.of(page2, PAGINATIONCOUNT);
                int start2 = (int) pageRequest2.getOffset();
                int end2 = Math.min((start2 + pageRequest2.getPageSize()), logCroutes.size());
                Page<Object[]> pageLog2 = new PageImpl<>(logCroutes.subList(start2, end2), pageRequest2, logCroutes.size());

                int totalPage2 = pageLog2.getTotalPages();
                if (totalPage2 > 0) {
                    List<Integer> pages2 = IntStream.rangeClosed(1, totalPage2).boxed().collect(Collectors.toList());
                    modelAndView.addObject("pages2", pages2);
                }
                modelAndView.addObject("period", fecha1);
                modelAndView.addObject("allLog2", pageLog2.getContent());
                modelAndView.addObject("current2", page2 + 1);
                modelAndView.addObject("next2", page2 + 2);
                modelAndView.addObject("prev2", page2);
                modelAndView.addObject("last2", totalPage2);
                modelAndView.addObject("registers2", logCroutes.size());
                modelAndView.addObject("registersInventarios", logCroutes.size());
                modelAndView.addObject("registersData2", croutes.size());
            }

            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.addObject("directory", "cuadroMandoInvAndCont");
            modelAndView.setViewName("parametric/cuadroMandoInvAndCont");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }
    @PostMapping("/parametric/cuadroMandoInvAndCont/invMasive")
    public ModelAndView manejarSeleccionados(@RequestParam("idsSeleccionados") String idsSeleccionados,
                                             @RequestParam("period2") String period2,
                                             @RequestParam("period") String period) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/cuadroMandoInvAndCont");
        String[] ids = idsSeleccionados.split(",");
        modelAndView.addObject("period", period);
        modelAndView.addObject("period2", period2);
        if(idsSeleccionados.trim().length()!=0)
        {
            conciliationRouteService.leerArchivosMasivo(ids,period);
            modelAndView.addObject("resp", "CM1");
        }
        else
        {
            modelAndView.addObject("resp", "CM-1");
        }
        return modelAndView;
    }
    @PostMapping("/parametric/cuadroMandoInvAndCont/conMasive")
    public ModelAndView manejarSeleccionados2(@RequestParam("idsSeleccionados2") String idsSeleccionados2,
                                             @RequestParam("period2") String period2,
                                             @RequestParam("period") String period) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/cuadroMandoInvAndCont");
        String[] ids = idsSeleccionados2.split(",");
        modelAndView.addObject("period", period);
        modelAndView.addObject("period2", period2);
        modelAndView.addObject("tab", "contables");
        System.out.println(idsSeleccionados2);
        if(idsSeleccionados2.trim().length()!=0)
        {
            accountingRouteService.leerArchivosMasivo(ids,period2);
            modelAndView.addObject("resp", "CM2");
        }
        else
        {
            modelAndView.addObject("resp", "CM-1");
        }
        return modelAndView;
    }
}
