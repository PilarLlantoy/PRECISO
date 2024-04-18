package com.inter.proyecto_intergrupo.controller.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamInforme;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamInformeListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamInformeService;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamMDAListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamMDAService;
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
public class ParamInformeController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ParamInformeService paramInformeService;

    private List<String> listColumns=List.of("Agupar 1", "Aplica Query", "Agupar 2", "Id", "Moneda", "Concepto", "Condición", "Nota", "Aplica");

    @GetMapping(value="/parametric/informe")
    public ModelAndView showParamInforme(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Parametrica Informe Balance y PYG")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            List<ParamInforme> list = paramInformeService.findAll();

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ParamInforme> pageData= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageData.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allData", pageData.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "informe");
            modelAndView.addObject("registers",pageData.getTotalElements());
            modelAndView.setViewName("parametric/informe");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/informe")
    public ModelAndView uploadFileInforme(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/informe");
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
            ArrayList<String[]> list = paramInformeService.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                ParamInformeListReport paramInformeListReport = new ParamInformeListReport(list,null);
                paramInformeListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyInforme/{id}")
    @ResponseBody
    public ModelAndView modifyInforme(@PathVariable String id){
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
        ParamInforme toModify = paramInformeService.findByIdNic34(idTemp);
        modelAndView.addObject("informeModify",toModify);
        modelAndView.setViewName("parametric/modifyInforme");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyInforme")
    @ResponseBody
    public ModelAndView updateInforme(@ModelAttribute ParamInforme paramInforme){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/informe");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            paramInformeService.modifyInforme(paramInforme, user);
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value="/parametric/addInforme")
    public ModelAndView showAddInforme(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ParamInforme paramInforme = new ParamInforme();
        modelAndView.addObject("informe", paramInforme);
        modelAndView.setViewName("parametric/addInforme");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addInforme")
    public ModelAndView createNewInforme(@ModelAttribute ParamInforme paramInforme) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/informe");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            paramInformeService.saveInforme(paramInforme, user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/removeInforme/{id}")
    @ResponseBody
    public ModelAndView removeInforme(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ParamInforme toRemove = paramInformeService.findByIdNic34(Long.parseLong(id));
        paramInformeService.removeInforme(toRemove.getIdNic34(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/informe");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearInforme")
    public ModelAndView clearInforme(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        paramInformeService.clearInforme(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/informe");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/informe/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Informe_Balan_PYG_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ParamInforme> informeList= new ArrayList<>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            informeList = paramInformeService.findAll();
        }
        else{
            informeList = paramInformeService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ParamInformeListReport listReport = new ParamInformeListReport(null,informeList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchInforme")
    @ResponseBody
    public ModelAndView searchInforme(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ParamInforme> list=paramInformeService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ParamInforme> pageData = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

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
        modelAndView.addObject("directory","searchInforme");
        modelAndView.addObject("registers",list.size());
        modelAndView.setViewName("parametric/informe");
        return modelAndView;
    }

}