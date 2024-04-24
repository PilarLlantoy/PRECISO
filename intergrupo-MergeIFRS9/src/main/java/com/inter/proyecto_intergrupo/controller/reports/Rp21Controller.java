package com.inter.proyecto_intergrupo.controller.reports;

import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reports.Rp21;
import com.inter.proyecto_intergrupo.model.reports.Rp21_Extend;
import com.inter.proyecto_intergrupo.repository.parametric.TypeEntityRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.QueryService;
import com.inter.proyecto_intergrupo.service.reportsServices.Rp21ListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.Rp21DiffListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.Rp21Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class Rp21Controller {
    private static final int PAGINATIONCOUNT=20;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private Rp21Service rp21Service;

    @Autowired
    private QueryService queryService;

    @Autowired
    private TypeEntityRepository typeEntityRepository;

    @Autowired
    private ControlPanelService controlPanelService;

    @GetMapping(value="/reports/rp21")
    public ModelAndView showRp21(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver RP21")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";
            if (params.get("period") != null && params.get("period").toString() == "") {
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
            List<Object[]> list = rp21Service.getAllReport(todayString, 1);
            List<ControlPanel> listControl = controlPanelService.findByFechaReporteRP21(todayString, user);
            List<Object[]> companies = rp21Service.getCompany(todayString);
            boolean noQuery = false;

            int countFull = 0;

            for (int i = 0; i < listControl.size(); i++) {
                if (listControl.get(i).getSemaforoComponente().equals("FULL")) {
                    countFull++;
                }
            }

            if (countFull >= 4) {
                modelAndView.addObject("statusComponent", true);
            } else {
                modelAndView.addObject("statusComponent", false);
            }

            if (companies.size() == 0) {
                noQuery = false;
            } else {
                noQuery = true;
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageRp21 = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageRp21.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allRp21", pageRp21.getContent());
            modelAndView.addObject("allControlPanel", listControl);
            modelAndView.addObject("companies", companies);
            modelAndView.addObject("noQuery", noQuery);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("directory", "rp21");
            modelAndView.addObject("registers",list.size());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("reports/rp21");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/reports/rp21")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/rp21");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        String todayString="";
        if(params.get("period").toString()=="") {
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
        List<Rp21> rp21List = rp21Service.findAll(todayString);
        try {
            Collection<Part> parts = request.getParts();
            String month=params.get("period").toString();
            modelAndView.addObject("period",month);
            parts.remove(request.getPart("period"));
            Rp21ListReport listReport = new Rp21ListReport(rp21List,null,null,null,null);
            List<String[]> list = new ArrayList<>();
            try{
                long startTime = System.nanoTime();
                list=rp21Service.saveFileBD(parts,user,month);
            }catch(Exception e){
                e.printStackTrace();
            }
            if(list.get(0)[0].equals("ERROR"))
            {
                modelAndView.addObject("resp", "RP21-1");
            }
            else if(list.get(0)[0].equals("PERMISO"))
            {
                modelAndView.addObject("resp", "RP21-2");
            }
            else if(list.get(0)[0].equals("DUPLICADO"))
            {
                modelAndView.addObject("resp", "RP21-3");
            }
            else
            {
                try{
                listReport.exportLog(response,list);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return  modelAndView;

    }

    @PostMapping(value = "/reports/modifyRp21")
    @ResponseBody
    public ModelAndView updateRp21(@ModelAttribute Rp21 rp21,@Param(value = "id") long id){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/rp21");
        try {
            Rp21 searchRp21 = rp21Service.findRp21ByIdReporte(rp21.getIdReporte());
            if (searchRp21==null||id==rp21.getIdReporte())
            {
                //rp21Service.modifyRp21(rp21, id);
                modelAndView.addObject("resp", "Modify1");
            }
            else
            {
                modelAndView.addObject("resp", "Modify0");
            }
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value = "/reports/validateIdRp21")
    @ResponseBody
    public String validateNitRp21(@RequestParam String idNew,@RequestParam String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(rp21Service.findRp21ByIdReporte(Long.parseLong(idNew))==null||idNew.equals(id))
            result="valid";
        return  result;
    }

    @GetMapping(value="/reports/addRp21")
    public ModelAndView showAddRp21(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Rp21 rp21 = new Rp21();
        modelAndView.addObject("rp21", rp21);
        modelAndView.setViewName("reports/addRp21");
        return modelAndView;
    }

    @PostMapping(value = "/reports/addRp21")
    public ModelAndView createNewRp21(@ModelAttribute Rp21 rp21) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/rp21");
        try {
            if (rp21Service.findRp21ByIdReporte(rp21.getIdReporte()) == null) {
                rp21Service.saveRp21(rp21);
                modelAndView.addObject("resp", "Add1");
            } else {
                modelAndView.addObject("resp", "Add0");
            }
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/reports/removeRp21/{id}")
    @ResponseBody
    public ModelAndView removeRp21(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Rp21 toRemove = rp21Service.findRp21ByIdReporte(Long.parseLong(id));
        rp21Service.removeRp21(toRemove.getIdReporte());
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/rp21");
        return  modelAndView;
    }

    @GetMapping(value = "/reports/rp21/downloadReport")
    @ResponseBody
    public void exportToExcelReport(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ReporteRp21_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> rp21List= new ArrayList<Object[]>();
        rp21List = rp21Service.getAllReport(params.get("period").toString(),1);
        Rp21ListReport listReport = new Rp21ListReport(rp21List);
        listReport.exportReport(response);
    }
    @GetMapping(value = "/reports/rp21/downloadLiquidez")
    @ResponseBody
    public void exportToExcelExtra(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ReporteRp21Liquidez_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        //String futuros = params.get("checkFuturos2").toString();

        String todayString="";
        if(params.get("period").toString()=="") {
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
        Object n= new Object();
        //List<Object[]> rp21listEntity = rp21Service.insertAllLiquidez(todayString,params.get("fw"),params.get("swap"),params.get("rys"),params.get("opc"), futuros,params.get("fu"));
        //List<Object[]> rp21ListExtra = rp21Service.findAllLiquidez(todayString,params.get("fw"),params.get("swap"),params.get("rys"),params.get("opc"), futuros,params.get("fu"));
        List<Object[]> rp21listEntity = rp21Service.insertAllLiquidez(todayString,n,n,n,n,"",n);
        List<Object[]> rp21ListExtra = rp21Service.findAllLiquidez(todayString,n,n,n,n,"",n);
        Rp21ListReport listReport = new Rp21ListReport(null,rp21ListExtra,typeEntityRepository,rp21listEntity,null);
        listReport.exportLiquidez(response);
    }

    @GetMapping(value = "/reports/rp21/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String nameFile = "ReporteRp21_";
        if(params.get("concil") != null)
            nameFile = "RYSConcil_";
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename="+nameFile + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        //String futuros = params.get("checkFuturos2").toString();

        String todayString="";
        if(params.get("period")== null || params.get("period").toString()=="") {
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
        Object n= new Object();
        List<Rp21> rp21List= new ArrayList<>();
        rp21List = rp21Service.findAllExcludeOriginal(todayString,n,n,n,n,"",n);
        Rp21ListReport listReport = new Rp21ListReport(rp21List,null,null,null,null);
        listReport.exportOriginal(response);
    }

    @GetMapping(value = "/reports/rp21/downloadFinrep")
    @ResponseBody
    public void exportToExcelFinrep(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String nameFile = "ReporteRp21_";
        if(params.get("concil") != null)
            nameFile = "RYSConcil_";
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename="+nameFile + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        //String futuros = params.get("checkFuturos2").toString();

        String todayString="";
        if(params.get("period")== null || params.get("period").toString()=="") {
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
        Object n= new Object();
        List<Rp21_Extend> rp21List= new ArrayList<>();
        //rp21List = rp21Service.findAllExclude(todayString,params.get("fw"),params.get("swap"),params.get("rys"),params.get("opc"), futuros,params.get("fu"));
        rp21List = rp21Service.findAllExclude(todayString,n,n,n,n,"",n);
        Rp21ListReport listReport = new Rp21ListReport(null,null,null,null,rp21List);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/rp21/downloadFinrepPath")
    @ResponseBody
    public ModelAndView exportToPathExcelFinrep(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/rp21");
        String todayString="";
        if(params.get("period")== null || params.get("period").toString()=="") {
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
        modelAndView.addObject("period", todayString);
        try{
            Object n= new Object();
            List<Rp21_Extend> rp21List= new ArrayList<>();
            rp21List = rp21Service.findAllExclude(todayString,n,n,n,n,"",n);
            Rp21ListReport listReport = new Rp21ListReport(null,null,null,null,rp21List);
            listReport.loadPath(todayString);
            modelAndView.addObject("resp", "RP214");
        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "RP21-4");
        }

        return  modelAndView;
    }

    @GetMapping(value="/reports/validateRp21")
    @ResponseBody
    public ModelAndView validateRp21Query(HttpServletResponse response,RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("reports/rp21");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String todayString="";
        String company = params.get("company").toString();

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

        List<Object[]> accounts = rp21Service.validateRp21AndQuery(todayString,user.getCentro(),company);

        if(accounts.size() > 0)
        {
            modelAndView.addObject("resp", false);
        }
        else {
            modelAndView.addObject("resp", true);
        }

        modelAndView.addObject("company", company);

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

        List<Object[]> list=rp21Service.getAllReport(todayString,0);
        List<ControlPanel> listControl=controlPanelService.findByFechaReporteRP21(todayString,user);
        List<Object[]> companies=rp21Service.getCompany(todayString);
        int countFull = 0;

        for (int i=0;i<listControl.size();i++){
            if(listControl.get(i).getSemaforoComponente().equals("FULL")){
                countFull++;
            }
        }

        if(countFull >= 4){
            modelAndView.addObject("statusComponent", true);
        }else{
            modelAndView.addObject("statusComponent",false);
        }

        boolean noQuery = false;

        if (companies.size() == 0){
            noQuery = false;
        }else{
            noQuery = true;
        }

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Object[]> pageRp21 = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageRp21.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allRp21",pageRp21.getContent());
        modelAndView.addObject("allControlPanel",listControl);
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("directory","rp21");
        modelAndView.addObject("companies",companies);
        modelAndView.addObject("noQuery",noQuery);

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());

        return modelAndView;
    }

    @GetMapping(value = "/reports/validateRp21/downloadDiff")
    @ResponseBody
    public void exportToExcelReportDiff(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";
        String company="";

        if(params.get("period").toString()=="") {
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

        company=params.get("company").toString();
        List<Object[]> reconciliation;

        reconciliation=rp21Service.validateRp21AndQuery(todayString, "", company);

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=DiferenciasRP21_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        Rp21DiffListReport listReport = new Rp21DiffListReport(reconciliation,1);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/validateRp21/downloadRes")
    @ResponseBody
    public void exportToExcelReportRes(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString="";
        String company="";

        if(params.get("period").toString()=="") {
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

        company=params.get("company").toString();
        List<Object[]> reconciliation;

        reconciliation=rp21Service.validateRp21AndQueryRes(todayString, "", company);

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=DiferenciasRP21_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        Rp21DiffListReport listReport = new Rp21DiffListReport(reconciliation,1);
        listReport.export(response);
    }

    @GetMapping(value="/reports/sendRp21")
    @ResponseBody
    public ModelAndView sendRp21(@RequestParam Map<String, Object> params) {

        ModelAndView modelAndView = new ModelAndView("redirect:/reports/rp21");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String todayString="";
        String company = params.get("company").toString();
        String futuros = params.get("checkFuturos").toString();

        if(params.get("period").toString()=="") {
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

        boolean resultInsert = rp21Service.insertIntoIntergroup(todayString, futuros);

        List<Object[]> accounts = rp21Service.validateRp21AndQuery(todayString,user.getCentro(),company);

        List<Rp21> contentRP21 = rp21Service.findAll(todayString);

        if(contentRP21.size() == 0){
            modelAndView.addObject("resp", "IRP-3");
            modelAndView.addObject("statusComponent", false);
        }else {

            if (accounts.size() > 0) {
                if (resultInsert) {
                    modelAndView.addObject("resp", "IRP-0");
                    modelAndView.addObject("statusComponent", true);
                } else {
                    modelAndView.addObject("resp", "IRP-1");
                    modelAndView.addObject("statusComponent", false);
                }
            } else {
                if (resultInsert) {
                    modelAndView.addObject("resp", "IRP-2");
                    modelAndView.addObject("statusComponent", true);
                } else {
                    modelAndView.addObject("resp", "IRP-1");
                    modelAndView.addObject("statusComponent", false);
                }
            }

        }

        modelAndView.addObject("period",todayString);

        return modelAndView;

    }

    @GetMapping(value="/reports/deleteInputRp21")
    @ResponseBody
    public ModelAndView deleteInputRp21(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/rp21");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String todayString="";
        String input = params.get("input").toString();

        if(params.get("period").toString()=="") {
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

        boolean resultDelete = rp21Service.deleteInputRP21(todayString, input);

        if (resultDelete){
            modelAndView.addObject("resp","DRP-0");
        }else{
            modelAndView.addObject("resp","DRP-1");
        }

        modelAndView.addObject("period",todayString);

        return modelAndView;

    }

}
