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

            List<AccountingRoute> listAroutes = accountingRouteService.findAllActive();
            List<Object[]> aroutes = new ArrayList<>();
            List<Object[]> croutes = new ArrayList<>();
            List<CampoRC> colAroutes = new ArrayList<>();
            List<CampoRC> colCroutes = new ArrayList<>();
            List<Object[]> logAroutes = new ArrayList<>();
            List<Object[]> logCroutes = new ArrayList<>();

            if (params.get("tab") != null && params.get("tab").toString() != null)
                modelAndView.addObject("tab", params.get("tab").toString());

            if (params.get("period2") != null && params.get("period2").toString() != null) {
                logAroutes = accountingRouteService.findAllLogByDate(params.get("period2").toString());
            }
            if (params.get("period") != null && params.get("period").toString() != null) {
                logCroutes = conciliationRouteService.findAllLogByDate(params.get("period").toString());
            }

            //CONTABLES
            //*-------------------------------------------------
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

            int pageData = params.get("pageData") != null ? (Integer.valueOf(params.get("pageData").toString()) - 1) : 0;
            PageRequest pageRequestData = PageRequest.of(pageData, PAGINATIONCOUNTDATA);
            int startData = (int) pageRequestData.getOffset();
            int endData = Math.min((startData + pageRequestData.getPageSize()), aroutes.size());
            Page<Object[]> pageLogData = new PageImpl<>(aroutes.subList(startData, endData), pageRequestData, aroutes.size());

            int totalPageData = pageLogData.getTotalPages();
            if (totalPageData > 0) {
                List<Integer> pagesData = IntStream.rangeClosed(1, totalPageData).boxed().collect(Collectors.toList());
                modelAndView.addObject("pagesData", pagesData);
            }

            //INVENTARIOS
            //*-------------------------------------------------
            int page2 = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest2 = PageRequest.of(page, PAGINATIONCOUNT);
            int start2 = (int) pageRequest2.getOffset();
            int end2 = Math.min((start2 + pageRequest2.getPageSize()), logCroutes.size());
            Page<Object[]> pageLog2 = new PageImpl<>(logCroutes.subList(start2, end2), pageRequest2, logCroutes.size());

            int totalPage2 = pageLog2.getTotalPages();
            if (totalPage2 > 0) {
                List<Integer> pages2 = IntStream.rangeClosed(1, totalPage2).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages2", pages2);
            }

            int pageData2 = params.get("pageData") != null ? (Integer.valueOf(params.get("pageData").toString()) - 1) : 0;
            PageRequest pageRequestData2 = PageRequest.of(pageData2, PAGINATIONCOUNTDATA);
            int startData2 = (int) pageRequestData2.getOffset();
            int endData2 = Math.min((startData2 + pageRequestData2.getPageSize()), croutes.size());
            Page<Object[]> pageLogData2 = new PageImpl<>(croutes.subList(startData2, endData2), pageRequestData2, croutes.size());

            int totalPageData2 = pageLogData2.getTotalPages();
            if (totalPageData2 > 0) {
                List<Integer> pagesData2 = IntStream.rangeClosed(1, totalPageData2).boxed().collect(Collectors.toList());
                modelAndView.addObject("pagesData2", pagesData2);
            }

            modelAndView.addObject("allLog", pageLog.getContent());
            modelAndView.addObject("allRCs", pageLogData.getContent());
            modelAndView.addObject("allColRCs", colAroutes);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("currentData", pageData + 1);
            modelAndView.addObject("nextData", pageData + 2);
            modelAndView.addObject("prevData", pageData);
            modelAndView.addObject("lastData", totalPageData);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("listRouteCont", listAroutes);
            modelAndView.addObject("directory", "cuadroMandoInvAndCont");
            modelAndView.addObject("registers", logAroutes.size());
            modelAndView.addObject("registersInventarios", logCroutes.size());
            modelAndView.addObject("registersData", aroutes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/cuadroMandoInvAndCont");

            modelAndView.addObject("allLog2", pageLog2.getContent());
            modelAndView.addObject("allRCs2", pageLogData2.getContent());
            modelAndView.addObject("allColRCs", colAroutes);
            modelAndView.addObject("current2", page2 + 1);
            modelAndView.addObject("next2", page2 + 2);
            modelAndView.addObject("prev2", page2);
            modelAndView.addObject("last2", totalPage2);
            modelAndView.addObject("currentData2", pageData2 + 1);
            modelAndView.addObject("nextData2", pageData2 + 2);
            modelAndView.addObject("prevData2", pageData2);
            modelAndView.addObject("lastData2", totalPageData2);
            modelAndView.addObject("listRouteCont", listAroutes);
            modelAndView.addObject("registers2", logCroutes.size());
            modelAndView.addObject("registersInventarios", logCroutes.size());
            modelAndView.addObject("registersData2", croutes.size());
            if (params.get("period") != null)
                modelAndView.addObject("period", params.get("period").toString());
            if(params.get("period2")!=null)
                modelAndView.addObject("period2",params.get("period2").toString());
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }



}
