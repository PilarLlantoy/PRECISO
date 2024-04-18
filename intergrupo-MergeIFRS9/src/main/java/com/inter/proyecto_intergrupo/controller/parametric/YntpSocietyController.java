package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
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
import javax.validation.Valid;
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
public class YntpSocietyController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private YntpSocietyService yntpSocietyService;

    private List<String> listColumns=List.of("Código Sociedad", "Descripción Larga Sociedad", "Descripción Corta Sociedad","Divisa","Mtdo. Consolidación IFRS","Grupo Consolidación IFRS","País");

    @Autowired
    private CountryService countryService;

    @Autowired
    private ConsolidationGroupService consolidationGroupService;

    @Autowired
    private ConsolidationMethodService consolidationMethodService;

    @Autowired
    private CurrencyService currencyService;

    @GetMapping(value="/parametric/yntp")
    public ModelAndView showYntpSocietys(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Sociedades YNTP")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            List<Object[]> list = yntpSocietyService.findAllCeros();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageYntp = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageYntp.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allYntp", pageYntp.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "yntp");
            modelAndView.addObject("registers",list.size());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/yntp");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/yntp")
    public ModelAndView uploadFileYntp(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/yntp");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {

            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            YntpSocietyListReport listReport = new YntpSocietyListReport(null);
            ArrayList<String[]> list=yntpSocietyService.saveFileBD(fileContent, user);
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
                int rowTemp=(Integer.parseInt(part[0])+1);
                part[0]=String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep-1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else
            {
                int rowTemp=(Integer.parseInt(part[0])+1);
                part[0]=String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep0");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                return  modelAndView;
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "LoadDoc-1");
            modelAndView.addObject("row", "");
            modelAndView.addObject("colum", "");
        }

        return  modelAndView;

    }

    @GetMapping(value = "/parametric/modifyYntps/{id}")
    @ResponseBody
    public ModelAndView modifyYntp(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        YntpSociety toModify = yntpSocietyService.findYntpByYntp(id);
        modelAndView.addObject("yntpModify",toModify);
        modelAndView.addObject("yntpId",toModify.getYntp());
        List <Country> allCountry = countryService.findAll();
        modelAndView.addObject("paises",allCountry);
        List <ConsolidationGroup> allGroups = consolidationGroupService.findAll();
        modelAndView.addObject("grupos",allGroups);
        List <ConsolidationMethod> allMethods = consolidationMethodService.findAll();
        modelAndView.addObject("metodos",allMethods);
        List <Currency> allCurrency = currencyService.findAll();
        modelAndView.addObject("divisas",allCurrency);
        modelAndView.setViewName("parametric/modifyYntps");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyYntps")
    public ModelAndView updateYntp(@ModelAttribute YntpSociety yntp, @Param(value = "id") String id, @RequestParam("paisSelect") String pais,@RequestParam("grupoSelect") String grupo,@RequestParam("metodoSelect") String metodo,@RequestParam("divisaSelect") String divisa){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/yntp");
        try {
            YntpSociety searchYntp = yntpSocietyService.findYntpByYntp(yntp.getYntp());
            if (searchYntp == null || id.equals(yntp.getYntp())) {
                yntpSocietyService.modifyYntp(yntp, id, pais, divisa, grupo, metodo);
                modelAndView.addObject("resp", "Modify1");
            }else
            {
                modelAndView.addObject("resp", "Modify0");
            }
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/validateSociety")
    @ResponseBody
    public String validateYntpSociety(@RequestParam String idNew,@RequestParam String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(yntpSocietyService.findYntpByYntp(idNew)==null||idNew.equals(id))
            result="valid";
        return  result;
    }

    @GetMapping(value = "/parametric/validateSocietyInverse")
    @ResponseBody
    public String validateYntpSocietyInvert(@RequestParam String yntpNew,@RequestParam String yntpOld){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(yntpSocietyService.findYntpByYntp(yntpNew)!=null||yntpNew.equals(yntpOld))
            result="valid";
        return  result;
    }

    @GetMapping(value="/parametric/addYntp")
    public ModelAndView showAddYntp(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        YntpSociety yntp = new YntpSociety();
        modelAndView.addObject("yntp", yntp);
        List <Country> allPaises = countryService.findAll();
        modelAndView.addObject("paises",allPaises);
        List <ConsolidationGroup> allGroups = consolidationGroupService.findAll();
        modelAndView.addObject("grupos",allGroups);
        List <ConsolidationMethod> allMethods = consolidationMethodService.findAll();
        modelAndView.addObject("metodos",allMethods);
        List <Currency> allCurrency = currencyService.findAll();
        modelAndView.addObject("divisas",allCurrency);
        modelAndView.setViewName("parametric/addYntp");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addYntp")
    public ModelAndView createNewYntp(@Valid YntpSociety yntp, @RequestParam("paisSelect") String pais, @RequestParam("divisaSelect") String divisa, @RequestParam("grupoSelect") String grupo,@RequestParam("metodoSelect") String metodo) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/yntp");

        try{
            if(yntpSocietyService.findYntpByYntp(yntp.getYntp())==null){
                yntpSocietyService.saveYntp(yntp,pais,divisa,grupo,metodo);
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

    @GetMapping(value = "/parametric/removeYntp/{id}")
    @ResponseBody
    public boolean removeYntp(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/yntp");
        User user = userService.findUserByUserName(auth.getName());
        boolean response=false;
        try{
            YntpSociety toRemove = yntpSocietyService.findYntpByYntp(id);
            yntpSocietyService.removeYntp(toRemove.getYntp(), user);
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/clearYntp")
    @ResponseBody
    public boolean clearYntp(){
        boolean response=false;
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/yntp");
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findUserByUserName(auth.getName());
            yntpSocietyService.clearYntp(user);
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/yntp/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=SociedadesYntp_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<YntpSociety> yntpList = new ArrayList<YntpSociety>();
        List<Object[]> yntpListSub = new ArrayList<Object[]>();

        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            yntpList = yntpSocietyService.findAll();
            YntpSocietyListReport listReport = new YntpSocietyListReport(yntpList);
            listReport.export(response);
        }
        else{
            yntpListSub = yntpSocietyService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
            YntpSocietyListReport listReport = new YntpSocietyListReport(yntpList,yntpListSub);
            listReport.exportConvert(response);
        }
    }

    @GetMapping(value = "/parametric/searchYntp")
    @ResponseBody
    public ModelAndView searchYntp(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Object[]> list= yntpSocietyService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Object[]> pageYntp = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageYntp.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allYntp",pageYntp.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchYntp");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/yntp");
        return modelAndView;
    }
}

