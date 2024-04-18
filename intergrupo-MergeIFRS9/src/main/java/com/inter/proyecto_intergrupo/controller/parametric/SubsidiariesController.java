package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Subsidiaries;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.SegmentsLogListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
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
public class SubsidiariesController {

    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private SubsidiariesService subsidiariesService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private YntpSocietyService yntpSocietyService;

    private List<String> listColumns=List.of("Yntp Empresa Reportante", "Cuenta Reportante","Yntp Local","Cuenta Local");

    @GetMapping(value="/parametric/subsidiaries")
    public ModelAndView showSubsidiaries(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Cruce Filiales")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<Subsidiaries> pageSubsidiaries = subsidiariesService.getAll(pageRequest);
            int totalPage = pageSubsidiaries.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allSubsidiaries", pageSubsidiaries.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "subsidiaries");
            modelAndView.addObject("registers",pageSubsidiaries.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/subsidiaries");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/subsidiaries")
    public ModelAndView uploadFileSubsidiaries(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/subsidiaries");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Subsidiaries> subsidiariesList = subsidiariesService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            SubsidiariesListReport listReport = new SubsidiariesListReport(subsidiariesList);
            ArrayList<String[]> list=subsidiariesService.saveFileBD(fileContent, user);
            String[] part=list.get(0);
            if(part[2].equals("true"))
            {
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                listReport.exportLog(response,list);
            }
            else if(part[2].equals("falseFormat"))
            {
                SegmentsLogListReport segmentsLogListReport = new SegmentsLogListReport(list);
                segmentsLogListReport.exportLog(response);
            }
            else
            {
                SegmentsLogListReport segmentsLogListReport = new SegmentsLogListReport(list);
                segmentsLogListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        finally {
            return  modelAndView;
        }
    }

    @GetMapping(value = "/parametric/modifySubsidiaries/{id}")
    @ResponseBody
    public ModelAndView modifySubsidiaries(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String[] listPart =id.split(":");
        String listPart2 = "";
        if(listPart.length>1 && listPart[1]!=null)
            listPart2 = listPart[1];
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("userEmail",user.getCorreo());
        Subsidiaries toModify = subsidiariesService.findSubsidiariesById(id).get(0);
        modelAndView.addObject("subsidiariesModify",toModify);
        modelAndView.setViewName("parametric/modifySubsidiaries");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifySubsidiaries")
    @ResponseBody
    public ModelAndView updateSubsidiaries(@ModelAttribute Subsidiaries subsidiaries,@Param(value = "id") String id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/subsidiaries");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            Subsidiaries searchSubsidiaries = subsidiariesService.findSubsidiariesById(id).get(0);
            if (searchSubsidiaries!=null)
            {
                System.out.println(id);
                subsidiariesService.modifySubsidiaries(subsidiaries,user);
                modelAndView.addObject("resp", "Modify1");
            }
            else
            {
                modelAndView.addObject("resp", "Modify0");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value = "/parametric/validateYntpSubsidiaries")
    @ResponseBody
    public String validateYntpSubsidiaries(@RequestParam String idNew,@RequestParam String id,@RequestParam String idCuenta,@RequestParam String idCuentaOld){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(yntpSocietyService.findYntpByYntp(idNew)!=null) {
            if (subsidiariesService.findSubsidiariesByYntpCuenta(idNew, idCuenta).size() == 0|| (idNew.equals(id) && idCuenta.equals(idCuentaOld))) {
                result = "valid";
            }
            else
            {
                result ="yntp";
            }
        }
        return  result;
    }

    @GetMapping(value="/parametric/addSubsidiaries")
    public ModelAndView showAddSubsidiaries(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Subsidiaries subsidiaries = new Subsidiaries();
        modelAndView.addObject("subsidiaries", subsidiaries);
        modelAndView.setViewName("parametric/addSubsidiaries");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addSubsidiaries")
    public ModelAndView createNewSubsidiaries(@ModelAttribute Subsidiaries subsidiaries) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/subsidiaries");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (subsidiariesService.findSubsidiariesByYntpCuenta(subsidiaries.getYntpEmpresa(),subsidiaries.getCuentaFilial()).size() == 0) {
                subsidiaries.setYtnpLocal("00548");
                subsidiariesService.saveSubsidiaries(subsidiaries, user);
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

    @GetMapping(value = "/parametric/removeSubsidiaries/{id}")
    @ResponseBody
    public ModelAndView removeSubsidiaries(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User user = userService.findUserByUserName(auth.getName());
        String[] listPart=id.split(":");
        Subsidiaries toRemove = subsidiariesService.findSubsidiariesByYntpCuenta(listPart[0],listPart[1]).get(0);
        subsidiariesService.removeSubsidiaries(toRemove.getYntpEmpresa(), toRemove.getCuentaFilial(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/subsidiaries");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearSubsidiaries")
    public ModelAndView clearSubsidiaries(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        subsidiariesService.clearSubsidiaries(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/subsidiaries");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/subsidiaries/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Filiales_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Subsidiaries> subsidiariesList= new ArrayList<Subsidiaries>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            subsidiariesList = subsidiariesService.findAll();
        }
        else{
            subsidiariesList = subsidiariesService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        SubsidiariesListReport listReport = new SubsidiariesListReport(subsidiariesList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchSubsidiaries")
    @ResponseBody
    public ModelAndView searchSubsidiaries(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Subsidiaries> list=subsidiariesService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Subsidiaries> pageSubsidiaries = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageSubsidiaries.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allSubsidiaries",pageSubsidiaries.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchSubsidiaries");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/subsidiaries");
        return modelAndView;
    }
}
