package com.inter.proyecto_intergrupo.controller.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamInforme;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamInformeNotas;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamInformeListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamInformeNotasListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamInformeNotasService;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamInformeService;
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
public class ParamInformeNotasController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ParamInformeNotasService paramInformeService;

    private List<String> listColumns=List.of("Agupar 1", "Aplica Query", "Agupar 2", "Id", "Moneda", "Concepto", "Condición", "Nota", "Aplica");

    @GetMapping(value="/parametric/informeNotas")
    public ModelAndView showParamInformeNotas(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Parametrica Informe Notas")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            List<ParamInformeNotas> list = paramInformeService.findAll();

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ParamInformeNotas> pageData= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

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
            modelAndView.addObject("directory", "informeNotas");
            modelAndView.addObject("registers",pageData.getTotalElements());
            modelAndView.setViewName("parametric/informeNotas");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/informeNotas")
    public ModelAndView uploadFileInforme(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/informeNotas");
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
                ParamInformeNotasListReport paramInformeNotasListReport = new ParamInformeNotasListReport(list,null);
                paramInformeNotasListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyInformeNotas/{id}")
    @ResponseBody
    public ModelAndView modifyInformeNotas(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        ParamInformeNotas toModify = paramInformeService.findByIdNic34(idTemp);
        modelAndView.addObject("informeModify",toModify);
        modelAndView.setViewName("parametric/modifyInformeNotas");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyInformeNotas")
    @ResponseBody
    public ModelAndView updateInformeNotas(@ModelAttribute ParamInformeNotas paramInforme){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/informeNotas");
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

    @GetMapping(value="/parametric/addInformeNotas")
    public ModelAndView showAddInformeNotas(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ParamInformeNotas paramInforme = new ParamInformeNotas();
        modelAndView.addObject("informe", paramInforme);
        modelAndView.setViewName("parametric/addInformeNotas");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addInformeNotas")
    public ModelAndView createNewInformeNotas(@ModelAttribute ParamInformeNotas paramInforme) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/informeNotas");
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

    @GetMapping(value = "/parametric/removeInformeNotas/{id}")
    @ResponseBody
    public ModelAndView removeInformeNotas(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ParamInformeNotas toRemove = paramInformeService.findByIdNic34(Long.parseLong(id));
        paramInformeService.removeInforme(toRemove.getIdNic34(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/informeNotas");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearInformeNotas")
    public ModelAndView clearInformeNotas(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        paramInformeService.clearInforme(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/informeNotas");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/informeNotas/download")
    @ResponseBody
    public void exportToExcelNotas(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Informe_Notas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ParamInformeNotas> informeList= new ArrayList<>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            informeList = paramInformeService.findAll();
        }
        else{
            informeList = paramInformeService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ParamInformeNotasListReport listReport = new ParamInformeNotasListReport(null,informeList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchInformeNotas")
    @ResponseBody
    public ModelAndView searchInformeNotas(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ParamInformeNotas> list=paramInformeService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ParamInformeNotas> pageData = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

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
        modelAndView.addObject("directory","searchInformeNotas");
        modelAndView.addObject("registers",list.size());
        modelAndView.setViewName("parametric/informeNotas");
        return modelAndView;
    }

}