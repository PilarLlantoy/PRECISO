package com.inter.proyecto_intergrupo.controller.reports;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reports.Ifrs9Report;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.reportsServices.Ifrs9ListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.Ifrs9ListReport2;
import com.inter.proyecto_intergrupo.service.reportsServices.Ifrs9ListReportDetail;
import com.inter.proyecto_intergrupo.service.reportsServices.Ifrs9ReportService;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class Ifrs9ReportsController {

    @Autowired
    private UserService userService;

    @Autowired
    private Ifrs9ReportService ifrs9ReportService;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @GetMapping(value="/reports/ifrs9Report")
    public ModelAndView showIfrs9Report(@RequestParam Map<String, Object> params) throws ParseException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Vertical De Saldos")) {

            String todayString = "";
            if (params.get("period").toString() == null || params.get("period").toString() == "") {
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
            List<String[]> list = new ArrayList<>();

            SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM");

            Date actDate = formatter2.parse(todayString);
            Calendar act = Calendar.getInstance();
            act.setTime(actDate);

            Calendar prev = Calendar.getInstance();
            prev.setTime(actDate);
            prev.add(Calendar.MONTH,-1);
            Date resultPrev = prev.getTime();

            String prevDateS = formatter2.format(resultPrev);

            String dateTextPrev = new SimpleDateFormat("MMMM", new Locale("es","ES")).format(prev.getTime());
            String dateText = new SimpleDateFormat("MMMM", new Locale("es","ES")).format(act.getTime());

            if (params.get("order") == null || params.get("order").toString() == "") {
                modelAndView.addObject("order", "A");
            } else {
                modelAndView.addObject("order", params.get("order").toString());
                if (params.get("order") != null && params.get("order").toString().equals("A")) {
                    list = ifrs9ReportService.getIfrs9ReportPrimary(todayString, "A", "S");
                } else if (params.get("order") != null && params.get("order").toString().equals("SA")) {
                    list = ifrs9ReportService.getIfrs9ReportPrimary(todayString, "SA", "S");
                } else if (params.get("order") != null && params.get("order").toString().equals("HA")) {
                    list = ifrs9ReportService.getIfrs9ReportPrimary(todayString, "A", "N");
                }else if (params.get("order") != null && params.get("order").toString().equals("HSA")){
                    list = ifrs9ReportService.getIfrs9ReportPrimary(todayString, "SA", "N");
                }
            }

            List<Object[]> validateSeg = ifrs9ReportService.validateTableVert(todayString);

            if (validateSeg.size() != 0) {
                modelAndView.addObject("validateVert", true);
                Date dateSeg = new Date();
                try {
                    dateSeg = StatusInfoRepository.findByInputAndPeriodo("VERTICAL-SALDOS", todayString).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateSeg);
                modelAndView.addObject("dateVert", dateAsString);
            } else {
                modelAndView.addObject("validateVert", false);
            }

            modelAndView.addObject("datePrev", dateTextPrev);
            modelAndView.addObject("date", dateText);

            modelAndView.addObject("allReport", list);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("directory", "ifrs9Report");
            modelAndView.addObject("totalData", "0");
            modelAndView.addObject("allReportHash", list);

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("reports/ifrs9Report");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/reports/ifrs9Report/load")
    @ResponseBody
    public ModelAndView loadIfrs9Reports(@RequestParam Map<String, Object> params) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/ifrs9Report");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String today= params.get("period").toString();
        modelAndView.addObject("period",today);
        String valAjuste ="";
        if(params.get("order")==null || params.get("order").toString()=="")
        {
            valAjuste="SA";
        }
        else
        {
            valAjuste = params.get("order").toString();
        }
        try
        {
            if (valAjuste.equals("A"))
            {
                if(ifrs9ReportService.vaidateDataAjuste(today).isEmpty())
                    valAjuste = "SA";
            }
        }
        catch(Exception e){
            modelAndView.addObject("resp", "ReportVertical-1");
            e.printStackTrace();
            return  modelAndView;
        }
        modelAndView.addObject("order",valAjuste);
        ifrs9ReportService.loadIfrs9Report(today,valAjuste);
        modelAndView.addObject("resp", "ReportVertical");
        modelAndView.addObject("period",today);
        return  modelAndView;
    }

    @GetMapping(value = "/reports/ifrs9Report/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";
        if(params.get("period").toString()==null || params.get("period").toString()=="") {
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

        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM");

        Date actDate = formatter2.parse(todayString);
        Calendar act = Calendar.getInstance();
        act.setTime(actDate);

        Calendar prev = Calendar.getInstance();
        prev.setTime(actDate);
        prev.add(Calendar.MONTH,-1);
        Date resultPrev = prev.getTime();

        String prevDateS = formatter2.format(resultPrev);

        String dateTextPrev = new SimpleDateFormat("MMMM", new Locale("es","ES")).format(prev.getTime());
        String dateText = new SimpleDateFormat("MMMM", new Locale("es","ES")).format(act.getTime());

        String headerValue = "";
        String headerKey = "Content-Disposition";
        List<String[]> list = new ArrayList<>();
        if(params.get("order")!=null&&params.get("order").toString().equals("A")){
            headerValue = "attachment; filename=VSaldosAjustada_" + currentDateTime + ".xlsx";
            list = ifrs9ReportService.getIfrs9ReportPrimary(todayString, "A", "S");

            response.setHeader(headerKey, headerValue);
            Ifrs9ListReport2 ifrs9ListReport2 = new Ifrs9ListReport2(list, dateTextPrev, dateText, "A");
            ifrs9ListReport2.export(response);
        }
        else if(params.get("order")!=null&&params.get("order").toString().equals("SA")){
            headerValue = "attachment; filename=VSaldosSinAjuste_" + currentDateTime + ".xlsx";
            list = ifrs9ReportService.getIfrs9ReportPrimary(todayString, "SA", "S");

            response.setHeader(headerKey, headerValue);
            Ifrs9ListReport2 ifrs9ListReport2 = new Ifrs9ListReport2(list, dateTextPrev, dateText, "SA");
            ifrs9ListReport2.export(response);
        }
        else if(params.get("order")!=null&&params.get("order").toString().equals("HA"))
        {
            headerValue = "attachment; filename=VSaldosHoldingAjustada_" + currentDateTime + ".xlsx";
            list = ifrs9ReportService.getIfrs9ReportPrimary(todayString, "A", "N");

            response.setHeader(headerKey, headerValue);
            Ifrs9ListReport2 ifrs9ListReport2 = new Ifrs9ListReport2(list, dateTextPrev, dateText, "HA");
            ifrs9ListReport2.export(response);
        }
        else if(params.get("order")!=null&&params.get("order").toString().equals("HSA"))
        {
            headerValue = "attachment; filename=VSaldosHoldingSinAjuste_" + currentDateTime + ".xlsx";
            list = ifrs9ReportService.getIfrs9ReportPrimary(todayString, "SA", "N");

            response.setHeader(headerKey, headerValue);
            Ifrs9ListReport2 ifrs9ListReport2 = new Ifrs9ListReport2(list, dateTextPrev, dateText, "HSA");
            ifrs9ListReport2.export(response);
        }

    }

    @GetMapping(value = "/reports/ifrs9Report/downloadAll")
    @ResponseBody
    public void exportToExcelReport(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";
        if(params.get("period").toString()==null || params.get("period").toString()=="") {
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

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=VerticalSaldos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        if(params.get("order")!=null&&params.get("order").toString().equals("A"))
        {
            List<Object[]> ifrs9List = new ArrayList<>();
            ifrs9List = ifrs9ReportService.findAllExport("A",todayString);
            Ifrs9ListReport ifrs9ListReport = new Ifrs9ListReport(ifrs9List);
            ifrs9ListReport.exportAll(response);
        }else if (params.get("order")!=null&&params.get("order").toString().equals("SA")){
            List<Object[]> ifrs9List = new ArrayList<>();
            ifrs9List = ifrs9ReportService.findAllExport("SA",todayString);
            Ifrs9ListReport ifrs9ListReport = new Ifrs9ListReport(ifrs9List);
            ifrs9ListReport.exportAll(response);
        }else if (params.get("order")!=null&&params.get("order").toString().equals("HA")){
            List<Object[]> ifrs9List = new ArrayList<>();
            ifrs9List = ifrs9ReportService.findAllExport("A",todayString);
            Ifrs9ListReport ifrs9ListReport = new Ifrs9ListReport(ifrs9List);
            ifrs9ListReport.exportAll(response);
        }else if (params.get("order")!=null&&params.get("order").toString().equals("HSA")){
            List<Object[]> ifrs9List = new ArrayList<>();
            ifrs9List = ifrs9ReportService.findAllExport("SA",todayString);
            Ifrs9ListReport ifrs9ListReport = new Ifrs9ListReport(ifrs9List);
            ifrs9ListReport.exportAll(response);
        }
    }

    @GetMapping(value = "/reports/ifrs9Report/downloadFilter")
    @ResponseBody
    public void exportToExcelReportFilter(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";
        if(params.get("period").toString()==null || params.get("period").toString()=="") {
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

        String center = "";

        if(params.get("center").toString()==null || params.get("center").toString()==""){
            center = "";
        }else{
            center = params.get("center").toString();
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=VerticalSaldos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        if(params.get("order")!=null&&params.get("order").toString().equals("A"))
        {
            List<Object[]> ifrs9List = new ArrayList<>();
            ifrs9List = ifrs9ReportService.findAllExportFilter("A",todayString, center);
            Ifrs9ListReportDetail ifrs9ListReportDetail = new Ifrs9ListReportDetail(ifrs9List);
            ifrs9ListReportDetail.exportAll(response);
        }else if (params.get("order")!=null&&params.get("order").toString().equals("SA")){
            List<Object[]> ifrs9List = new ArrayList<>();
            ifrs9List = ifrs9ReportService.findAllExportFilter("SA",todayString, center);
            Ifrs9ListReportDetail ifrs9ListReportDetail = new Ifrs9ListReportDetail(ifrs9List);
            ifrs9ListReportDetail.exportAll(response);
        }else if (params.get("order")!=null&&params.get("order").toString().equals("HA")){
            List<Object[]> ifrs9List = new ArrayList<>();
            ifrs9List = ifrs9ReportService.findAllExportFilter("A",todayString, center);
            Ifrs9ListReportDetail ifrs9ListReportDetail = new Ifrs9ListReportDetail(ifrs9List);
            ifrs9ListReportDetail.exportAll(response);
        }else if (params.get("order")!=null&&params.get("order").toString().equals("HSA")){
            List<Object[]> ifrs9List = new ArrayList<>();
            ifrs9List = ifrs9ReportService.findAllExportFilter("SA",todayString, center);
            Ifrs9ListReportDetail ifrs9ListReportDetail = new Ifrs9ListReportDetail(ifrs9List);
            ifrs9ListReportDetail.exportAll(response);
        }
    }



}
