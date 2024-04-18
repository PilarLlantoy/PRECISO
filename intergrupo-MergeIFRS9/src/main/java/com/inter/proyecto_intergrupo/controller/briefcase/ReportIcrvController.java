package com.inter.proyecto_intergrupo.controller.briefcase;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.CalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaCalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.ReportIcrv;
import com.inter.proyecto_intergrupo.model.dataquality.PointRulesDQ;
import com.inter.proyecto_intergrupo.model.ifrs9.Repo;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.briefcaseServices.CalculoIcrvListReport;
import com.inter.proyecto_intergrupo.service.briefcaseServices.CalculoIcrvService;
import com.inter.proyecto_intergrupo.service.briefcaseServices.ReportIcrvListReport;
import com.inter.proyecto_intergrupo.service.briefcaseServices.ReportIcrvService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
public class ReportIcrvController {

    @Autowired
    ReportIcrvService reportIcrvService;

    @Autowired
    private UserService userService;

    private static final int PAGINATIONCOUNT=12;
    private final List<String> listColumns = List.of("Entidad", "Código Periodo","CodSociinfo","XtiCartera","CodSocipart","CodISIN","Signo Valor Contable","Signo Microcobertura");


    @GetMapping(value = "/briefcase/icrv")
    public ModelAndView showView(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Reporte ICRV")){
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

            List<ReportIcrv> list;

            if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
                list = reportIcrvService.findAllReport(todayString);
                modelAndView.addObject("filterExport", "Original");
            } else {
                list = reportIcrvService.findByFilter(Id,Vf,todayString);
                modelAndView.addObject("filterExport", "Filtrado");
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ReportIcrv> pageList = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageList.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("vId", Id);
            modelAndView.addObject("vFilter", Vf);
            modelAndView.addObject("icrvList", pageList.getContent());
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("directory", "icrv");
            modelAndView.addObject("registers",list.size());
            modelAndView.setViewName("/briefcase/icrv");

        }
        else
        {
            modelAndView.addObject("calculoicrv","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/briefcase/icrv")
    public ModelAndView loadPlantilla(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/icrv");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Log_Cargue_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = reportIcrvService.saveFileBDPlantilla(fileContent,user,params.get("period").toString());
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                ReportIcrvListReport reportIcrvListReport = new ReportIcrvListReport(list,null);
                reportIcrvListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/icrv/download")
    @ResponseBody
    public void downloadReport(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

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
        List<ReportIcrv> list;
        if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
            list = reportIcrvService.findAllReport(params.get("period").toString());
        } else {
            list = reportIcrvService.findByFilter(Id,Vf,params.get("period").toString());
        }
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= ReporteICRV_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ReportIcrvListReport listReport = new ReportIcrvListReport(null, list);
        listReport.export(response);
    }

    @GetMapping(value = "/briefcase/icrv/downloadPlano")
    @ResponseBody
    public void downloadReportPlane(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment;  filename=ICRV_CARGA_" + currentDateTime + ".txt");

        List<ReportIcrv> data = reportIcrvService.findAllReport(params.get("period").toString());

        CsvService.downloadTxtReportIcrv(response.getWriter(), data);
    }

    @GetMapping(value = "/briefcase/modifyIcrv/{id}")
    @ResponseBody
    public ModelAndView modifyReporticrv(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        ReportIcrv toModify = reportIcrvService.findByIdReport(idTemp);
        modelAndView.addObject("icrvModify",toModify);
        modelAndView.setViewName("briefcase/modifyIcrv");
        return modelAndView;
    }

    @PostMapping(value = "/briefcase/modifyIcrv")
    @ResponseBody
    public ModelAndView updateReporticrv(@ModelAttribute ReportIcrv reportIcrv){
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/icrv");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            reportIcrvService.modifyReport(reportIcrv, user);
            modelAndView.addObject("period", reportIcrv.getPeriodo());
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value = "/briefcase/removeIcrv/{id}")
    @ResponseBody
    public ModelAndView removeIcrv(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        reportIcrvService.clearReport(user,id);
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/icrv?period="+id);
        return  modelAndView;
    }
}
