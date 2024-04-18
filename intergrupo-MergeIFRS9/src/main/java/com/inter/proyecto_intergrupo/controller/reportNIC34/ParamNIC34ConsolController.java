package com.inter.proyecto_intergrupo.controller.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamNIC34Consol;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamNIC34ConsolListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamNIC34ConsolService;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamNIC34ListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamNIC34Service;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ParamNIC34ConsolController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ParamNIC34ConsolService paramNIC34Service;

    private List<String> listColumns=List.of("L6", "Cuenta", "ID Grupo", "Grupo", "ID Nota", "Nota", "ID Subnota", "Subnota", "ID Campo", "Campo", "Moneda","Responsable");

    @GetMapping(value="/parametric/nic34Consol")
    public ModelAndView showParamNIC34(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Parametrica NIC34 Consolidado")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            List<ParamNIC34Consol> list = paramNIC34Service.findAll();

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ParamNIC34Consol> pageNic34= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageNic34.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allNic34", pageNic34.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "nic34Consol");
            modelAndView.addObject("registers",pageNic34.getTotalElements());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/nic34Consol");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/nic34Consol")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/nic34Consol");
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
            ArrayList<String[]> list = paramNIC34Service.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                ParamNIC34ConsolListReport paramNIC34ConsolListReport = new ParamNIC34ConsolListReport(list,null);
                paramNIC34ConsolListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyNic34Consol/{id}")
    @ResponseBody
    public ModelAndView modifyNic34(@PathVariable String id){
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
        ParamNIC34Consol toModify = paramNIC34Service.findByIdNic34(idTemp);
        modelAndView.addObject("nic34ConsolModify",toModify);
        modelAndView.setViewName("parametric/modifyNic34Consol");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyNic34Consol")
    @ResponseBody
    public ModelAndView updateNic34(@ModelAttribute ParamNIC34Consol paramNIC34){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/nic34Consol");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            paramNIC34Service.modifyNic34(paramNIC34, user);
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value="/parametric/addNic34Consol")
    public ModelAndView showAddNic34(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ParamNIC34Consol paramNIC34 = new ParamNIC34Consol();
        modelAndView.addObject("nic34Consol", paramNIC34);
        modelAndView.setViewName("parametric/addNic34Consol");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addNic34Consol")
    public ModelAndView createNewNic34(@ModelAttribute ParamNIC34Consol paramNIC34) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/nic34Consol");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            paramNIC34Service.saveNic34(paramNIC34, user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/removeNic34Consol/{id}")
    @ResponseBody
    public ModelAndView removeNic34(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ParamNIC34Consol toRemove = paramNIC34Service.findByIdNic34(Long.parseLong(id));
        paramNIC34Service.removeNic34(toRemove.getIdNic34(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/nic34Consol");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearNic34Consol")
    public ModelAndView clearNic34(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        paramNIC34Service.clearNic34(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/nic34Consol");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/nic34Consol/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=NIC34_Param_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ParamNIC34Consol> nic34List= new ArrayList<ParamNIC34Consol>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            nic34List = paramNIC34Service.findAll();
        }
        else{
            nic34List = paramNIC34Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ParamNIC34ConsolListReport listReport = new ParamNIC34ConsolListReport(null,nic34List);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchNic34Consol")
    @ResponseBody
    public ModelAndView searchNic34(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ParamNIC34Consol> list=paramNIC34Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ParamNIC34Consol> pageNic34 = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageNic34.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allNic34",pageNic34.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchNic34Consol");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/nic34Consol");
        return modelAndView;
    }

}