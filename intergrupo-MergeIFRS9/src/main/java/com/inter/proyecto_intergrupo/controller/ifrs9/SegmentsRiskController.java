package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.ControlPanelIfrs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.RiskAccountFinal;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelIfrsService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.*;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class SegmentsRiskController {
    @Autowired
    private SegmentsRiskService segmentsRiskService;

    @Autowired
    private RiskAccountService riskAccountService;

    @Autowired
    private UserService userService;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @Autowired
    private ControlPanelIfrsService controlPanelService;

    private static final int PAGINATIONCOUNT=12;
    private final List<String> listColumns = List.of("Número Cliente", "Nombre Cliente", "Tipo Persona","Segmento Viejo","Segmento Nuevo","Corasu","Subcorasu","CIIU");


    @GetMapping(value = "/ifrs/segmentsRisk")
    public ModelAndView findAllSegments(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Ajuste Segmentos Rechazos")) {

        int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
        String todayString = "";
        String Id = "";
        String Vf = "";

        if (params.get("vId") == null) {
            Id = "";
        } else {
            Id = params.get("vId").toString();
        }
        if (params.get("vFilter") == null) {
            Vf = "";
        } else {
            Vf = params.get("vFilter").toString();
        }


        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
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
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        } else {
            todayString = params.get("period").toString();
        }


        List<Object[]> resultSegments;

        if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
            resultSegments = segmentsRiskService.findAllSegments(todayString);
            modelAndView.addObject("filterExport", "Original");
        } else {
            resultSegments = segmentsRiskService.findByFilter(Id,Vf,todayString);
            modelAndView.addObject("filterExport", "Filtrado");
        }

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), resultSegments.size());
        Page<Object[]> pageSeg = new PageImpl<>(resultSegments.subList(start, end), pageRequest, resultSegments.size());

        int totalPage = pageSeg.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }

        try {
            StatusInfo data1 = StatusInfoRepository.findByInputAndPeriodo("SEGMENTOS-RECHAZOS", todayString);
            if(data1!=null) {
                Date dateSeg = data1.getFecha();
                modelAndView.addObject("validateSeg", true);

                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateSeg);
                modelAndView.addObject("dateSeg", dateAsString);
            }
            else
            {
                modelAndView.addObject("validateSeg", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<ControlPanelIfrs> listControl = controlPanelService.findByCPI(user);

        int countFull = 0;

        for (int i = 0; i < listControl.size(); i++) {
            if (listControl.get(i).getSemaforoComponente().equals("FULL")) {
                countFull++;
            }
        }

        if (countFull >= 6 ){
            modelAndView.addObject("statusComponent", true);
        } else {
            modelAndView.addObject("statusComponent", false);
        }

        modelAndView.addObject("vId", Id);
        modelAndView.addObject("vFilter", Vf);
        modelAndView.addObject("segList", pageSeg.getContent());
        modelAndView.addObject("columns", listColumns);
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("directory", "segmentsRisk");
        modelAndView.addObject("registers",resultSegments.size());
        modelAndView.setViewName("/ifrs/segmentsRisk");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/loadSegmentsRisk")
    public ModelAndView loadSegments(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/segmentsRisk");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
        String todayString = "";
        String Id = "";
        String Vf = "";

        if (params.get("vId") == null) {
            Id = "";
        } else {
            Id = params.get("vId").toString();
        }
        if (params.get("vFilter") == null) {
            Vf = "";
        } else {
            Vf = params.get("vFilter").toString();
        }


        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
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
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        } else {
            todayString = params.get("period").toString();
        }

        try {
            riskAccountService.loadDataUpdate(todayString);
            modelAndView.addObject("resp", "updateDataC");
        } catch (Exception e) {
            modelAndView.addObject("resp", "updateDataC-1");
            e.printStackTrace();
        }

        List<Object[]> resultSegments;

        if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
            resultSegments = segmentsRiskService.findAllSegments(todayString);
            modelAndView.addObject("filterExport", "Original");
        } else {
            resultSegments = segmentsRiskService.findByFilter(Id,Vf,todayString);
            modelAndView.addObject("filterExport", "Filtrado");
        }

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), resultSegments.size());
        Page<Object[]> pageSeg = new PageImpl<>(resultSegments.subList(start, end), pageRequest, resultSegments.size());

        int totalPage = pageSeg.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }


        StatusInfo data1 = StatusInfoRepository.findByInputAndPeriodo("SEGMENTOS-RECHAZOS", todayString);
        if(data1!=null) {
            Date dateSeg = data1.getFecha();
            modelAndView.addObject("validateSeg", true);

            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateSeg);
            modelAndView.addObject("dateSeg", dateAsString);
        }
        else
        {
            modelAndView.addObject("validateSeg", false);
        }

        List<ControlPanelIfrs> listControl = controlPanelService.findByCPI(user);

        int countFull = 0;

        for (int i = 0; i < listControl.size(); i++) {
            if (listControl.get(i).getSemaforoComponente().equals("FULL")) {
                countFull++;
            }
        }

        if (countFull >= 6 ){
            modelAndView.addObject("statusComponent", true);
        } else {
            modelAndView.addObject("statusComponent", false);
        }

        modelAndView.addObject("vId", Id);
        modelAndView.addObject("vFilter", Vf);
        modelAndView.addObject("segList", pageSeg.getContent());
        modelAndView.addObject("columns", listColumns);
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("directory", "segmentsRisk");
        modelAndView.addObject("registers",resultSegments.size());

        //modelAndView.setViewName("/ifrs/segmentsRisk");

        return modelAndView;
    }

    @GetMapping(value = "/ifrs/segmentsRisk/download")
    public void downloadSegments(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString = "";

        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
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
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        } else {
            todayString = params.get("period").toString();
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= ValidaSegmentos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> list;

        String Id = "";
        String Vf = "";

        if (params.get("vId") == null) {
            Id = "";
        } else {
            Id = params.get("vId").toString();
        }
        if (params.get("vFilter") == null) {
            Vf = "";
        } else {
            Vf = params.get("vFilter").toString();
        }

        if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
            list = segmentsRiskService.findAllSegments(todayString);
        } else {
            list = segmentsRiskService.findByFilter(Id,Vf,todayString);
        }

        SegmentsListReport listReport = new SegmentsListReport(list, null);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/segmentsRisk/downloadAll")
    public void downloadAllSegments(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString = "";

        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
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
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        } else {
            todayString = params.get("period").toString();
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= ValidaSegmentos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> list;

        String Id = "";
        String Vf = "";

        if (params.get("vId") == null) {
            Id = "";
        } else {
            Id = params.get("vId").toString();
        }
        if (params.get("vFilter") == null) {
            Vf = "";
        } else {
            Vf = params.get("vFilter").toString();
        }
        list = segmentsRiskService.findAllSegmentsPre(todayString);


        SegmentsListReport listReport = new SegmentsListReport(null , list);
        listReport.exportAllRisk(response);
    }

    @GetMapping(value = "/ifrs/segmentsRisk/downloadAllCustomers")
    public void downloadAllSegmentsCustomers(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {

        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment;  filename=PlanoClientesSegmentos_" + currentDateTime + ".txt");

        String todayString = "";

        if(params.get("period")!=null && params.get("period").toString()=="") {
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

        List<Object[]> list = segmentsRiskService.getAllSegmentsCustomers(todayString);
        ArrayList<String> listNew = new ArrayList<>();
        for (Object row : list) {
            listNew.add(row.toString());
        }

        CsvService.downloadCsvCargaMasiva(response.getWriter(), listNew);

    }


    @GetMapping(value = "/ifrs/segmentsRisk/downloadAllHist")
    public void downloadAllSegmentsHist(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString = "";

        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
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
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        } else {
            todayString = params.get("period").toString();
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= SegmentosHist_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> list;

        list = segmentsRiskService.findAllHist();

        SegmentsListReport listReport = new SegmentsListReport(null , list);
        listReport.exportAll(response);
    }


    @PostMapping(value="/ifrs/segmentsRisk")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "period") String period, @RequestParam Map<String, Object> params, ModelMap model) throws ParseException {
        response.setContentType("application/octet-stream");
        model.addAttribute("period",period);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/segmentsRisk");
        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("vFilter",params.get("period").toString());

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String todayString = "";

        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
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
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        } else {
            todayString = params.get("period").toString();
        }

        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logActualizacion_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        try{
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = segmentsRiskService.saveFileBD(fileContent, user, todayString);
            String[] part = list.get(0);

            if(part[0] != null && part[0].equals("Fallo Estructura") ){
                modelAndView.addObject("resp","false");
                modelAndView.addObject("period", params.get("period").toString());
            } else if(part[0] != null && part[0].equals("duplicados") ){
                modelAndView.addObject("resp","segmentsRisk-error");
                modelAndView.addObject("period", params.get("period").toString());
            } else {
                if(part[2].equals("true")){
                    modelAndView.addObject("resp", "segmentsRiskCorrect");
                    modelAndView.addObject("period", params.get("period").toString());
                }else{
                    SegmentsLogListReport segmentsRiskLogListReport = new SegmentsLogListReport(list);
                    segmentsRiskLogListReport.exportLog(response);

                }
            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
            modelAndView.addObject("resp","false");
            modelAndView.addObject("period", params.get("period").toString());
        }

        List<Object[]> validateSeg = segmentsRiskService.validateTableSeg(todayString);

        if (validateSeg.size() != 0) {
            modelAndView.addObject("validateSeg", true);
            Date dateSeg = new Date();
            try {
                dateSeg = StatusInfoRepository.findByInputAndPeriodo("SEGMENTOS-FINREP", todayString).getFecha();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateSeg);
            modelAndView.addObject("dateSeg", dateAsString);
        } else {
            modelAndView.addObject("validateSeg", false);
        }

        return modelAndView;
    }

    @PostMapping(value="/ifrs/segmentsRiskHist")
    public ModelAndView uploadFileHist(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "period") String period, @RequestParam Map<String, Object> params, ModelMap model) throws ParseException {
        response.setContentType("application/octet-stream");
        model.addAttribute("period",period);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/segmentsRisk");
        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("vFilter",params.get("period").toString());

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String todayString = "";

        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
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
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        } else {
            todayString = params.get("period").toString();
        }

        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logActualizacion_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        try{
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = segmentsRiskService.saveFileBDHist(fileContent, user, todayString);
            String[] part = list.get(0);

            if(part[0] != null && part[0].equals("Fallo Estructura") ){
                modelAndView.addObject("resp","false");
                modelAndView.addObject("period", params.get("period").toString());
            } else {
                if(part[2].equals("true")){
                    modelAndView.addObject("resp", "segmentsRiskCorrect");
                    modelAndView.addObject("period", params.get("period").toString());
                }else{
                    SegmentsLogListReport segmentsRiskLogListReport = new SegmentsLogListReport(list);
                    segmentsRiskLogListReport.exportLog(response);

                }
            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
            modelAndView.addObject("resp","false");
            modelAndView.addObject("period", params.get("period").toString());
        }

        List<Object[]> validateSeg = segmentsRiskService.validateTableSeg(todayString);

        if (validateSeg.size() != 0) {
            modelAndView.addObject("validateSeg", true);
            Date dateSeg = new Date();
            try {
                dateSeg = StatusInfoRepository.findByInputAndPeriodo("SEGMENTOS-FINREP", todayString).getFecha();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateSeg);
            modelAndView.addObject("dateSeg", dateAsString);
        } else {
            modelAndView.addObject("validateSeg", false);
        }

        return modelAndView;
    }
    @GetMapping(value = "/ifrs/segmentsRisk/downloadPlano")
    @ResponseBody
    public void exportPlane(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, ParseException {
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());
        List<RiskAccountFinal> riesgos = segmentsRiskService.getDataCSV(params.get("period").toString());
        response.setHeader("Content-Disposition", "attachment;  filename=CCPLM_D02_"+currentDateTime+"_udafistgsg_act_stage_seg.txt");
        CsvService.downloadRiesgosCsv(response.getWriter(), riesgos);
    }
}
