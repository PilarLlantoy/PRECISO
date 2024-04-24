package com.inter.proyecto_intergrupo.controller.reports;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.model.reports.DescontabilizacionReport;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.reportsServices.DescontabilizacionReportListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.DescontabilizacionReportService;
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
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class DescontabilizacionReportController {

    private static final int PAGINATIONCOUNT = 15;

    @Autowired
    private UserService userService;

    @Autowired
    private DescontabilizacionReportService descontabilizacionReportService;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @GetMapping(value="/reports/descontabilizacionReport")
    public ModelAndView showTemplateConsolidacion(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Fichero Descontabilización")) {

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

        String todayString="";
        if(params.get("period")==null || params.get("period").toString()=="") {
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
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }

        List<Object[]> listDescon = descontabilizacionReportService.getDesconFilter(todayString);

        //List<DescontabilizacionReport> listDescon = descontabilizacionReportService.getDescontabilizacionReport(todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), listDescon.size());
        Page<Object[]> pageDesconParcial = new PageImpl<>(listDescon.subList(start, end), pageRequest, listDescon.size());

        int totalPage=pageDesconParcial.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        modelAndView.addObject("allDescon",pageDesconParcial.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","descontabilizacionReport");
        modelAndView.addObject("registers",listDescon.size());
        modelAndView.addObject("period",todayString);
        modelAndView.setViewName("/reports/descontabilizacionReport");

        List<Object[]> validateDescon = descontabilizacionReportService.validateDescon(todayString);

        if (validateDescon.size() != 0) {
            modelAndView.addObject("validateDescon", true);
            Date dateSicc = new Date();
            try {
                dateSicc = StatusInfoRepository.findByInputAndPeriodo("FICHERODESCON", todayString).getFecha();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateSicc);
            modelAndView.addObject("dateDescon", dateAsString);
        } else {
            modelAndView.addObject("validateDescon", false);
        }
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }

        return modelAndView;
    }

    @GetMapping(value="/reports/descontabilizacionReport/calcularDescon")
    public ModelAndView generarDescontabilizacion(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/descontabilizacionReport");

        String todayString="";
        if(params.get("period")==null || Objects.equals(params.get("period").toString(), "")) {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        }
        else {
            todayString=params.get("period").toString();
        }

        boolean salida = descontabilizacionReportService.generateDesconFile(todayString);
        if (salida){
            Date today = new Date();
            String input = "FICHERODESCON";

            StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo(input, todayString);

            if (validateStatus == null) {
                StatusInfo status = new StatusInfo();
                status.setInput(input);
                status.setPeriodo(todayString);
                status.setFecha(today);
                StatusInfoRepository.save(status);
            } else {
                validateStatus.setFecha(today);
                StatusInfoRepository.save(validateStatus);
            }

            descontabilizacionReportService.getDescontabilizacionReport(todayString);
            modelAndView.addObject("resp","desconFinalRealizado");
        }
        else{
            modelAndView.addObject("resp","revisarConcil");
        }

        return  modelAndView;
    }

    @RequestMapping(value = "/reports/descontabilizacionReport/downloadExcel")
    @ResponseBody
    public void exportDescontabilizacionParcial(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String todayString= params.get("period").toString();
            String headerValue = "attachment; filename=DescontabilizacionParcialReport_" + todayString + "_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<DescontabilizacionReport> descontabilizacionReportList = descontabilizacionReportService.getDescontabilizacionReport(todayString);
            DescontabilizacionReportListReport listReport = new DescontabilizacionReportListReport(descontabilizacionReportList);
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/reports/descontabilizacionReport/downloadComplete")
    @ResponseBody
    public void exportDescontabilizacion(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String todayString= params.get("period").toString();

        response.setHeader("Content-Disposition", "attachment;  filename= PROVISION_GENERAL.TXT");

        List<Object[]> descontabilizacionTemplateList = descontabilizacionReportService.getDescontabilizacionReportTxt(todayString);

        CsvService.downloadDescontabilizacionTemplate(response.getWriter(), descontabilizacionTemplateList);
    }

}
