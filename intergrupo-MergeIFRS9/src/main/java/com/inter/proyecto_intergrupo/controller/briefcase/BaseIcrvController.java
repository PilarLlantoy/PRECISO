package com.inter.proyecto_intergrupo.controller.briefcase;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.BaseIcrv;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.briefcaseServices.BaseIcrvListReport;
import com.inter.proyecto_intergrupo.service.briefcaseServices.BaseIcrvService;
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

import javax.servlet.ServletOutputStream;
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
public class BaseIcrvController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private BaseIcrvService baseIcrvService;

    private List<String> listColumns=List.of("Cuenta", "Empresa", "Naturaleza", "Evento","Concepto","No Asignado","Codicons","Epigrafe","Descripción Plano","Cta","Descripción Cta");

    @GetMapping(value="/briefcase/baseicrv")
    public ModelAndView showGeneral(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Parametrica Base ICRV")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<BaseIcrv> pageBaseIcrv = baseIcrvService.getAll(pageRequest);
            int totalPage = pageBaseIcrv.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allBaseicrv", pageBaseIcrv.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "baseicrv");
            modelAndView.addObject("registers",pageBaseIcrv.getTotalElements());
            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("briefcase/baseicrv");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/briefcase/baseicrv")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/baseicrv");
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
            ArrayList<String[]> list = baseIcrvService.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                BaseIcrvListReport baseIcrvListReport = new BaseIcrvListReport(list,null);
                baseIcrvListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/modifyBaseicrv/{id}")
    @ResponseBody
    public ModelAndView modifyBaseicrv(@PathVariable String id){
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
        BaseIcrv toModify = baseIcrvService.findByIdBase(idTemp);
        modelAndView.addObject("baseicrvModify",toModify);
        modelAndView.setViewName("briefcase/modifyBaseicrv");
        return modelAndView;
    }

    @PostMapping(value = "/briefcase/modifyBaseicrv")
    @ResponseBody
    public ModelAndView updateBaseicrv(@ModelAttribute BaseIcrv baseIcrv){
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/baseicrv");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            baseIcrvService.modifyBase(baseIcrv, user);
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value="/briefcase/addBaseicrv")
    public ModelAndView showAddBaseicrv(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        BaseIcrv baseIcrv = new BaseIcrv();
        modelAndView.addObject("baseicrv", baseIcrv);
        modelAndView.setViewName("briefcase/addBaseicrv");
        return modelAndView;
    }

    @PostMapping(value = "/briefcase/addBaseicrv")
    public ModelAndView createNewBaseicrv(@ModelAttribute BaseIcrv baseIcrv) {
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/baseicrv");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            baseIcrvService.saveBase(baseIcrv, user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/removeBaseicrv/{id}")
    @ResponseBody
    public ModelAndView removeBaseicrv(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        BaseIcrv toRemove = baseIcrvService.findByIdBase(Long.parseLong(id));
        baseIcrvService.removeBase(toRemove.getIdBase(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/baseicrv");
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/clearBaseicrv")
    public ModelAndView clearBaseicrv(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        baseIcrvService.clearBase(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/baseicrv");
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/baseicrv/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=BaseICRV_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<BaseIcrv> baseIcrvList= new ArrayList<BaseIcrv>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            baseIcrvList = baseIcrvService.findAll();
        }
        else{
            baseIcrvList = baseIcrvService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
            BaseIcrvListReport listReport = new BaseIcrvListReport(null,baseIcrvList);
        listReport.export(response);
    }

    @GetMapping(value = "/briefcase/searchBaseicrv")
    @ResponseBody
    public ModelAndView searchBaseicrv(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<BaseIcrv> list=baseIcrvService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<BaseIcrv> pageBaseIcrv = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageBaseIcrv.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allBaseicrv",pageBaseIcrv.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchBaseicrv");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("briefcase/baseicrv");
        return modelAndView;
    }
}