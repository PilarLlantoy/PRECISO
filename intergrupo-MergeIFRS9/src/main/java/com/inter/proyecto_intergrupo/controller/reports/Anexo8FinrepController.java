package com.inter.proyecto_intergrupo.controller.reports;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.model.reports.Anexo8Finrep;
import com.inter.proyecto_intergrupo.model.reports.Rp21_Extend;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamMDAListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamMDAService;
import com.inter.proyecto_intergrupo.service.reportsServices.Anexo8FinrepListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.Anexo8FinrepService;
import com.inter.proyecto_intergrupo.service.reportsServices.Rp21ListReport;
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
public class Anexo8FinrepController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private Anexo8FinrepService anexo8FinrepService;

    private List<String> listColumns=List.of("Cuenta");

    @GetMapping(value="/reports/anexo8finrep")
    public ModelAndView showAllData(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Anexo 8 FINREP")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            List<Anexo8Finrep> list = anexo8FinrepService.findAll();

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Anexo8Finrep> pageAnexo8= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageAnexo8.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allData", pageAnexo8.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "anexo8finrep");
            modelAndView.addObject("registers",pageAnexo8.getTotalElements());
            modelAndView.setViewName("reports/anexo8finrep");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/reports/anexo8finrep")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/anexo8finrep");
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
            ArrayList<String[]> list = anexo8FinrepService.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                SignatureListReport signatureListReport = new SignatureListReport(list,null);
                signatureListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value="/reports/addAnexo8finrep")
    public ModelAndView showAddRegister(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Anexo8Finrep anexo8Finrep = new Anexo8Finrep();
        modelAndView.addObject("anexo8finrep", anexo8Finrep);
        modelAndView.setViewName("reports/addAnexo8finrep");
        return modelAndView;
    }

    @PostMapping(value = "/reports/addAnexo8finrep")
    public ModelAndView createRegister(@ModelAttribute Anexo8Finrep anexo8Finrep) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/anexo8finrep");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            anexo8FinrepService.saveAnexo8(anexo8Finrep, user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/reports/removeAnexo8finrep/{id}")
    @ResponseBody
    public ModelAndView removeRegister(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        anexo8FinrepService.removeAnexo8(id, user);
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/anexo8finrep");
        return  modelAndView;
    }

    @GetMapping(value = "/reports/clearAnexo8finrep")
    public ModelAndView clearRegisters(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        anexo8FinrepService.clearAnexo8(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/anexo8finrep");
        return  modelAndView;
    }

    @GetMapping(value = "/reports/anexo8finrep/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Anexo_8_Finrep_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Anexo8Finrep> dataList= new ArrayList<Anexo8Finrep>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            dataList = anexo8FinrepService.findAll();
        }
        else{
            dataList = anexo8FinrepService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        Anexo8FinrepListReport listReport = new Anexo8FinrepListReport(dataList);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/anexo8finrep/downloadPath")
    @ResponseBody
    public ModelAndView exportToPathExcelFinrep(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/anexo8finrep");
        try{
            Anexo8FinrepListReport listReport = new Anexo8FinrepListReport(null);
            List<Object[]> dataList = anexo8FinrepService.findDataFinrep(params.get("period").toString());
            listReport.loadPath(params.get("period").toString(),dataList);
            modelAndView.addObject("resp", "RP214");
        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "RP21-4");
        }

        return  modelAndView;
    }

    @GetMapping(value = "/reports/searchAnexo8finrep")
    @ResponseBody
    public ModelAndView searchRegisters(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Anexo8Finrep> list=anexo8FinrepService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Anexo8Finrep> pageData = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageData.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allData",pageData.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchAnexo8finrep");
        modelAndView.addObject("registers",list.size());
        modelAndView.setViewName("reports/anexo8finrep");
        return modelAndView;
    }

}