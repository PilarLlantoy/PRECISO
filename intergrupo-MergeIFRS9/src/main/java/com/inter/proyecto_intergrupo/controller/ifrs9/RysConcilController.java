package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.RiskRys;
import com.inter.proyecto_intergrupo.model.ifrs9.RysConcil;
import com.inter.proyecto_intergrupo.model.parametric.Contract;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelAccounts;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.IntergrupoContingentesService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.RysConcilListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.RysConcilService;
import com.inter.proyecto_intergrupo.service.parametricServices.ContractService;
import com.inter.proyecto_intergrupo.service.parametricServices.CountryService;
import com.inter.proyecto_intergrupo.service.parametricServices.GarantBankService;
import com.inter.proyecto_intergrupo.service.parametricServices.YntpSocietyService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class RysConcilController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ControlPanelService controlPanelService;

    @Autowired
    private RysConcilService rysConcilService;

    @GetMapping(value="/ifrs/rysConcil")
    public ModelAndView showRys(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Conciliación RYS")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";
            if (params.get("period") == null || params.get("period").toString() == "")
            {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
                } else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
                }
            } else {
                todayString = params.get("period").toString();
            }
            List<Object[]> list = rysConcilService.getAllReportResumen(todayString);
            boolean controlRys = controlPanelService.findByRysFechaReporte(todayString);
            modelAndView.addObject("validateRys", controlRys);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageContingentes = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageContingentes.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allRys", pageContingentes.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("directory", "rysConcil");
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("registers",list.size());

            modelAndView.setViewName("ifrs/rysConcil");

        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/rysConcil")
    public ModelAndView uploadFileContin(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rysConcil");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        try
        {
            Collection<Part> parts = request.getParts();
            String month = params.get("period").toString();
            modelAndView.addObject("period", month);
            parts.remove(request.getPart("period"));
            RysConcilListReport listReport = new RysConcilListReport(rysConcilService.findAllRiskRys());
            List<String[]> list = rysConcilService.saveFileBD(parts, user, month);
            listReport.exportLog(response, list);
            modelAndView.addObject("resp", "contAdd");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            modelAndView.addObject("resp", "ContingentesLoadFail");
            return modelAndView;
        }
        return  modelAndView;
    }

    @RequestMapping(value = "/ifrs/rysConcil/downloadRisk")
    @ResponseBody
    public void exportRisk(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        try
        {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=RiesgosMercados_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<RiskRys> riskList = rysConcilService.getAllRisk(params.get("period").toString());
            RysConcilListReport listReport = new RysConcilListReport(riskList);
            listReport.exportRisk(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/ifrs/rysConcil/downloadReport")
    @ResponseBody
    public void exportReport(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=ResumenConcil_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<Object[]> tempList = rysConcilService.getAllReportResumen(params.get("period").toString());
            List<Object[]> tempListResume = rysConcilService.getAllReportResumenHoja2(params.get("period").toString());
            RysConcilListReport listReport = new RysConcilListReport(null);
            listReport.exportResumen(response,tempList,tempListResume);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping(value="/ifrs/rysConcil/generate")
    public ModelAndView generateConcil(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rysConcil");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("period", params.get("period").toString());
        try
        {
            rysConcilService.generateCruce(params.get("period").toString());
            modelAndView.addObject("resp", "rysConcilAdd");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            modelAndView.addObject("resp", "rysConcilLoadFail");
            return modelAndView;
        }
        return  modelAndView;
    }

    @RequestMapping(value = "/ifrs/rysConcil/downloadConcil")
    @ResponseBody
    public void exportReportConcil(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=AjsteReposMcdo_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<RysConcil> tempList = rysConcilService.getAllReport(params.get("period").toString());
            List<Object[]> tempListDif = rysConcilService.getAllDiferences(params.get("period").toString());
            RysConcilListReport listReport = new RysConcilListReport(null);
            listReport.exportConcil(response,tempList,tempListDif);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/ifrs/rysConcil/downloadRYSRP")
    @ResponseBody
    public void exportRysRp(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
            String currentDateTime = dateFormatter.format(new Date());
            response.setHeader("Content-Disposition", "attachment;  filename=RYSCargaMasiva_" + currentDateTime + ".csv");
            boolean ajuste = false;
            boolean reversion = false;
            if(params.get("CmRev") != null)
                reversion = true;
            if(params.get("CmAju") != null)
                ajuste = true;
            List<Object[]> contingentList =rysConcilService.getPlano(params.get("period").toString(),ajuste,reversion);
            CsvService.downloadCsvRiesgos(response.getWriter(), contingentList,reversion,params.get("period").toString());

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
