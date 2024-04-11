package com.inter.proyecto_intergrupo.controller.bank;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.GpsReport;
import com.inter.proyecto_intergrupo.model.precharges.ComerPrecharge;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.GpsListReport;
import com.inter.proyecto_intergrupo.service.bankServices.GpsReportService;
import com.inter.proyecto_intergrupo.service.reportsServices.Intergrupo1Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
public class GpsReportController {
    private static final int PAGINATIONCOUNT=12;

    @Autowired
    UserService userService;

    @Autowired
    GpsReportService gpsReportService;

    @Autowired
    Intergrupo1Service intergrupo1Service;

    private List<String> listColumns=List.of("Cta Local","Clase","Moneda", "Fecha contable", "Mes", "ImporteenML");

    @GetMapping(value="/bank/gpsreport")
    public ModelAndView showGpsReport(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver GPS")) {
            String todayString = "";
            if (params.get("period") == null || params.get("period").toString() == "") {
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

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            List<GpsReport> gpsReport = gpsReportService.findByPeriodo(todayString);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), gpsReport.size());
            Page<GpsReport> pageGPS = new PageImpl<>(gpsReport.subList(start, end), pageRequest, gpsReport.size());

            int totalPage = pageGPS.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allGps", pageGPS.getContent());
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("directory", "gpsreport");
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("registers",gpsReport.size());
            modelAndView.setViewName("/bank/gpsReport");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/bank/gpsreport/insert")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/bank/gpsreport");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=GPS Log_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            List<String[]> resultGps = gpsReportService.loadQueryDatabase(fileContent, params.get("period").toString());
            String[] part = resultGps.get(0);

            if(Objects.equals(part[1], "0")){
                gpsReportService.valdatePreComerAndInsert(params.get("period").toString(), user);
                modelAndView.addObject("resp","true");
                modelAndView.addObject("period",params.get("period").toString());
            } else {
                modelAndView.addObject("resp","false");
                modelAndView.addObject("period",params.get("period").toString());
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return modelAndView;
    }

    @GetMapping(value = "/bank/gpsreport/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= ReporteGPS_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<GpsReport> gpsList= gpsReportService.findByPeriodo(params.get("vFilter").toString());

        GpsListReport listReport = new GpsListReport(gpsList,null);
        listReport.export(response);
    }

    @GetMapping(value = "/bank/gpsreport/downloadGPSForm")
    @ResponseBody
    public void exportToExcelForm(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= ReporteGPSFiltrado_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<GpsReport> gpsList= gpsReportService.matchGPSForm(params.get("vFilter").toString());

        GpsListReport listReport = new GpsListReport(gpsList,null);
        listReport.export(response);
    }

    @GetMapping(value = "/bank/gpsreport/sendIntergrupo")
    public ModelAndView sendPreChargeToIntergrupo(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/bank/gpsreport");

        boolean result = intergrupo1Service.getFromComer(params.get("period").toString());

        if(result){
            modelAndView.addObject("resp","correcto");
        } else{
            modelAndView.addObject("resp","fallo");
        }

        modelAndView.addObject("period",params.get("period").toString());

        return modelAndView;
    }
}
