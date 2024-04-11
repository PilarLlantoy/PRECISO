package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.information.ChangeAccountQuery;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.ChangeAccountQueryListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.ChangeAccountQueryService;
import com.inter.proyecto_intergrupo.service.parametricServices.QueryListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdService;
import com.inter.proyecto_intergrupo.service.parametricServices.YntpSocietyService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ChangeAccountQueryController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ChangeAccountQueryService changeAccountQueryService;

    @Autowired
    private statusInfoRepository statusInfoRepositoryL;

    @Autowired
    private YntpSocietyService yntpSocietyService;

    private List<String> listColumns=List.of("Empresa", "Cuenta", "Código de consolidación anterior","Código de consolidación nuevo","Fecha de corte","Perímetro IFRS9","Observación");

    @GetMapping(value="/ifrs/changeAccountQuery")
    public ModelAndView showAccounts(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Historico Cambio Cuentas")) {

            String todayString="";
            String markString="IFRS9";
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

            if(params.get("typemark") != null && params.get("typemark").toString()!="")
            {
                markString = params.get("typemark").toString();
            }
            List<ChangeAccountQuery> list= new ArrayList<>();

            if(params.get("vId")!=null && params.get("vFilter")!=null)
            {
                list=changeAccountQueryService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),todayString);
                modelAndView.addObject("vFilter", params.get("vFilter").toString());
                modelAndView.addObject("vId", params.get("vId").toString());
            }
            else
            {
                list = changeAccountQueryService.getAccountsCodicons(todayString,markString);
                modelAndView.addObject("filterExport", "Original");
                modelAndView.addObject("vFilter", "Original");
            }

            List<Object[]> validateSicc = changeAccountQueryService.validateLoad(todayString);
            if (validateSicc.size() !=0) {
                modelAndView.addObject("validateSicc", true);
                Date dateSicc = new Date();
                try {
                    dateSicc = statusInfoRepositoryL.findByInputAndPeriodo("CUENTASCAMBIOS", todayString).getFecha();
                }catch (Exception e){
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateSicc);
                modelAndView.addObject("dateSicc", dateAsString);
            } else {
                modelAndView.addObject("validateSicc", false);
            }

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ChangeAccountQuery> pageChange = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageChange.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allChanges", pageChange.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("typemark", markString);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("directory", "changeAccountQuery");
            modelAndView.addObject("registers",pageChange.getTotalElements());

            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/changeAccountQuery");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/modifyChanges/{id}")
    @ResponseBody
    public ModelAndView modifyAccount(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ChangeAccountQuery toModify = changeAccountQueryService.findById(Long.parseLong(id));
        modelAndView.addObject("changeModify",toModify);
        modelAndView.addObject("idId",toModify.getIdCambio());
        modelAndView.setViewName("ifrs/modifyChanges");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/modifyChanges")
    @ResponseBody
    public ModelAndView updateChange(@ModelAttribute ChangeAccountQuery changeAccountQuery,@Param(value = "id") String id){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/changeAccountQuery");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            ChangeAccountQuery searchChange = changeAccountQueryService.findById(Long.parseLong(id));
            searchChange.setObservacion(changeAccountQuery.getObservacion());
            changeAccountQueryService.modifyAccount(searchChange, id, user);
            modelAndView.addObject("resp", "Modify1");

        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value = "/ifrs/removeChanges/{id}")
    @ResponseBody
    public ModelAndView removeChanges(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ChangeAccountQuery toRemove = changeAccountQueryService.findById(Long.parseLong(id));
        changeAccountQueryService.removeChangeAccount(toRemove.getIdCambio(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/changeAccountQuery");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/clearChanges")
    public ModelAndView clearChanges(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        changeAccountQueryService.clearChangeAccount(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/changeAccountQuery");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/changeAccountQuery/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CambioCuenta_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ChangeAccountQuery> thirdList= new ArrayList<ChangeAccountQuery>();

        if(params.get("vFilter")==null||params.get("vId").toString()==null||params.get("vFilter").toString().equals("Original")) {
            thirdList = changeAccountQueryService.getAccountsCodicons(params.get("period").toString(),"IFRS9");
        }
        else{
            thirdList = changeAccountQueryService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),params.get("period").toString());
        }
        QueryListReport listReport = new QueryListReport(null,thirdList);
        listReport.exportCodicons(response);
    }

    @GetMapping(value = "/ifrs/searchChangeAccount")
    @ResponseBody
    public ModelAndView searchAccounts(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

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

        List<Object[]> validateSicc = changeAccountQueryService.validateLoad(todayString);
        if (validateSicc.size() !=0) {
            modelAndView.addObject("validateSicc", true);
            Date dateSicc = new Date();
            try {
                dateSicc = statusInfoRepositoryL.findByInputAndPeriodo("CUENTASCAMBIOS", todayString).getFecha();
            }catch (Exception e){
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateSicc);
            modelAndView.addObject("dateSicc", dateAsString);
        } else {
            modelAndView.addObject("validateSicc", false);
        }

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ChangeAccountQuery> list=changeAccountQueryService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ChangeAccountQuery> pageThird = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageThird.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        modelAndView.addObject("allChanges",pageThird.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchChangeAccount");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/changeAccountQuery");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/validateChangeAccount")
    @ResponseBody
    public void validateCodes(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Codicons_"+params.get("periodValidate").toString()+"_VS_"+params.get("period").toString()+"_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Object[]> queryList = changeAccountQueryService.validateCodicons(params.get("periodValidate").toString(),params.get("period").toString(),"IFRS9");
        ChangeAccountQueryListReport listReport = new ChangeAccountQueryListReport(null,queryList);
        listReport.exportCodicons(response);
    }

    @GetMapping(value = "/ifrs/validateChangeAccountAfter")
    @ResponseBody
    public void validateCodesAfter(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString ="";
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM");
        Date fecha = formato.parse(params.get("period").toString());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
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

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Codicons_"+todayString+"_VS_"+params.get("period").toString()+"_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        changeAccountQueryService.searchCodicons(todayString,params.get("period").toString(),"IFRS9");
        List<ChangeAccountQuery> queryList = changeAccountQueryService.getAccountsCodicons(params.get("period").toString(),params.get("typemark").toString());
        QueryListReport listReport = new QueryListReport(null,queryList);
        listReport.exportCodicons(response);
    }
}