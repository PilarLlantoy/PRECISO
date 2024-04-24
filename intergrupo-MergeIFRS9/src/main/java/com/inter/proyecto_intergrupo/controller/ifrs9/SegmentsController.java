package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;

import com.inter.proyecto_intergrupo.model.parametric.Ciiu;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.SubsidiariesTemplateListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.SegmentsListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.SegmentsLogListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.SegmentsService;
import com.inter.proyecto_intergrupo.service.parametricServices.CiiuListReport;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
public class SegmentsController {
    @Autowired
    SegmentsService segmentsService;

    @Autowired
    private UserService userService;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    private static final int PAGINATIONCOUNT=12;
    private final List<String> listColumns = List.of("Número Cliente", "Nombre Cliente", "Tipo Persona","Segmento Viejo","Segmento Nuevo","Corasu","Subcorasu","CIIU");


    @GetMapping(value = "/ifrs/segments")
    public ModelAndView findAllSegments(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Validación Segmentos")) {

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
            resultSegments = segmentsService.findAllFinal(todayString);
            modelAndView.addObject("filterExport", "Original");
        } else {
            resultSegments = segmentsService.findByFilter(Id,Vf,todayString);
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

        List<Object[]> validateSeg = segmentsService.validateTableSeg(todayString);

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

        modelAndView.addObject("vId", Id);
        modelAndView.addObject("vFilter", Vf);
        modelAndView.addObject("segList", pageSeg.getContent());
        modelAndView.addObject("columns", listColumns);
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("directory", "segments");
        modelAndView.addObject("registers",resultSegments.size());
        modelAndView.setViewName("/ifrs/segments");
        }
            else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/loadSegments")
    public ModelAndView loadSegments(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
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

        List<Object[]> resultSegments;

        if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
            resultSegments = segmentsService.validateSegments(todayString);
            modelAndView.addObject("filterExport", "Original");
        } else {
            resultSegments = segmentsService.findByFilter(Id,Vf,todayString);
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

        List<Object[]> validateSeg = segmentsService.validateTableSeg(todayString);

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

        modelAndView.addObject("vId", Id);
        modelAndView.addObject("vFilter", Vf);
        modelAndView.addObject("segList", pageSeg.getContent());
        modelAndView.addObject("columns", listColumns);
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("directory", "segments");
        modelAndView.addObject("registers",resultSegments.size());
        modelAndView.setViewName("/ifrs/segments");

        return modelAndView;
    }

    @GetMapping(value = "/ifrs/segments/download")
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
            list = segmentsService.findAllFinal(todayString);
        } else {
            list = segmentsService.findByFilter(Id,Vf,todayString);
        }

        SegmentsListReport listReport = new SegmentsListReport(list, null);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/segments/downloadAll")
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

        if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
            list = segmentsService.findAll(todayString);
        } else {
            list = segmentsService.findByFilter(Id,Vf,todayString);
        }

        SegmentsListReport listReport = new SegmentsListReport(null , list);
        listReport.exportAll(response);
    }

    @GetMapping(value = "/ifrs/segments/downloadAllCustomers")
    public void downloadAllSegmentsCustomers(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {

        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment;  filename=PlanoClientesSegmentos_" + currentDateTime + ".txt");

        String todayString = "";
        String valFilter = "0";
        String itemFilter = "0";

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

        if(params.get("vFilter")!=null && params.get("vFilter").toString()!=null && params.get("vFilter").toString()!="")
        {
            itemFilter = params.get("vFilter").toString();
        }

        if(params.get("vId")!=null && params.get("vId").toString()!=null && params.get("vId").toString()!="")
        {
            valFilter = params.get("vId").toString();
        }

        List<Object[]> list = segmentsService.getAllSegmentsCustomers(todayString,valFilter,itemFilter);
        ArrayList<String> listNew = new ArrayList<>();
        for (Object row : list) {
            listNew.add(row.toString());
        }

        CsvService.downloadCsvCargaMasiva(response.getWriter(), listNew);

    }


    @GetMapping(value = "/ifrs/segments/downloadAllHist")
    public void downloadAllSegmentsHist(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString = "";
        String itemFilter = "0";
        String valFilter = "0";

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

        if(params.get("vFilter")!=null && params.get("vFilter").toString()!=null && params.get("vFilter").toString()!="")
        {
            itemFilter = params.get("vFilter").toString();
        }

        if(params.get("vId")!=null && params.get("vId").toString()!=null && params.get("vId").toString()!="")
        {
            valFilter = params.get("vId").toString();
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= SegmentosHist_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> list;

        list = segmentsService.findAllHist(valFilter,itemFilter);

        SegmentsListReport listReport = new SegmentsListReport(null , list);
        listReport.exportAll(response);
    }


    @PostMapping(value="/ifrs/segments")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "period") String period, @RequestParam Map<String, Object> params, ModelMap model) throws ParseException {
        response.setContentType("application/octet-stream");
        model.addAttribute("period",period);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/segments");
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
            ArrayList<String[]> list = segmentsService.saveFileBD(fileContent, user, todayString);
            String[] part = list.get(0);

            if(part[0] != null && part[0].equals("Fallo Estructura") ){
                modelAndView.addObject("resp","false");
                modelAndView.addObject("period", params.get("period").toString());
            } else if(part[0] != null && part[0].equals("duplicados") ){
                modelAndView.addObject("resp","segments-error");
                modelAndView.addObject("period", params.get("period").toString());
            } else {
                if(part[2].equals("true")){
                    modelAndView.addObject("resp", "segmentsCorrect");
                    modelAndView.addObject("period", params.get("period").toString());
                }else{
                    SegmentsLogListReport segmentsLogListReport = new SegmentsLogListReport(list);
                    segmentsLogListReport.exportLog(response);

                }
            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
            modelAndView.addObject("resp","false");
            modelAndView.addObject("period", params.get("period").toString());
        }

        List<Object[]> validateSeg = segmentsService.validateTableSeg(todayString);

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

    @PostMapping(value="/ifrs/segmentsHist")
    public ModelAndView uploadFileHist(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "period") String period, @RequestParam Map<String, Object> params, ModelMap model) throws ParseException {
        response.setContentType("application/octet-stream");
        model.addAttribute("period",period);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/segments");
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
            ArrayList<String[]> list = segmentsService.saveFileBDHist(fileContent, user, todayString);
            String[] part = list.get(0);

            if(part[0] != null && part[0].equals("Fallo Estructura") ){
                modelAndView.addObject("resp","false");
                modelAndView.addObject("period", params.get("period").toString());
            } else {
                if(part[2].equals("true")){
                    modelAndView.addObject("resp", "segmentsCorrect");
                    modelAndView.addObject("period", params.get("period").toString());
                }else{
                    SegmentsLogListReport segmentsLogListReport = new SegmentsLogListReport(list);
                    segmentsLogListReport.exportLog(response);

                }
            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
            modelAndView.addObject("resp","false");
            modelAndView.addObject("period", params.get("period").toString());
        }

        List<Object[]> validateSeg = segmentsService.validateTableSeg(todayString);

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


}
