package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.TemplateBankListReport;
import com.inter.proyecto_intergrupo.service.bankServices.TemplateBankService;
import com.inter.proyecto_intergrupo.service.parametricServices.CurrencyService;
import com.inter.proyecto_intergrupo.service.parametricServices.YntpSocietyService;
import com.inter.proyecto_intergrupo.service.reportsServices.Intergrupo1Service;
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
public class AccountingLoadController {
    private static final int PAGINATIONCOUNT=12;

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private YntpSocietyService yntpSocietyService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private TemplateBankService templateBankService;

    @Autowired
    Intergrupo1Service intergrupo1Service;

    private List<String> listColumns=List.of("Yntp Empresa",
            "Cod Neocon", "Divisa", "YNTP","Sociedad YNTP","Contrato","NIT Contraparte","valor","cod Pais","Pais","Cuenta local");

    @GetMapping(value="/parametric/accountingLoad")
    public ModelAndView showTemplateBanks(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Plantilla Banco")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";
            if (params.get("period") == null || params.get("period").toString() == "") {
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

            List<String[]> list = new ArrayList<>();
            if (params.get("order") != null && params.get("order").toString() == "") {
                modelAndView.addObject("order", "Plantilla");
                list = templateBankService.findAllTemplatesPantilla(user.getUsuario(), todayString, user);
            } else {
                if (params.get("order") != null && params.get("order").toString().equals("Pre-Carga")) {
                    list = templateBankService.findAllTemplates(user.getUsuario(), todayString, user);
                } else if (params.get("order") != null && params.get("order").toString().equals("Plantilla")) {
                    list = templateBankService.findAllTemplatesPantilla(user.getUsuario(), todayString, user);
                }
                if (params.get("order") == null) {
                    modelAndView.addObject("order", "Plantilla");
                    list = templateBankService.findAllTemplatesPantilla(user.getUsuario(), todayString, user);
                } else {
                    modelAndView.addObject("order", params.get("order").toString());
                }
            }

            boolean sendinter = intergrupo1Service.validateComponentTemplate(user,todayString);
            modelAndView.addObject("inter",sendinter);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<String[]> pageTax = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageTax.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allTemplateBanks", pageTax.getContent());
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("directory", "templateBank");
            modelAndView.addObject("registers",list.size());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/accountingLoad");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/accountingLoad")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountingLoad");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("order","Plantilla");
        modelAndView.addObject("vFilter",params.get("period").toString());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = templateBankService.saveFileBD(fileContent, user,params.get("period").toString());
            String[] part = list.get(0);

            if(part[2].equals("true")){
                ArrayList<String[]> validateAndInsert = templateBankService.validateTemporalAndQuery(params.get("period").toString(),user);
                String[] result = validateAndInsert.get(0);

                if(result[2].equals("true")){
                    List<Object[]> validateCodicons = templateBankService.validateCodicons(params.get("period").toString(),user);
                    if(!validateCodicons.isEmpty()) {
                        TemplateBankListReport templateBankListReport = new TemplateBankListReport(validateAndInsert);
                        templateBankListReport.exportLogCodicons(response,validateCodicons);
                    }
                    else {
                        modelAndView.addObject("resp", "AddRep1");
                        modelAndView.addObject("row", part[0]);
                        modelAndView.addObject("colum", part[1]);
                    }
                }
                else{
                    TemplateBankListReport templateBankListReport = new TemplateBankListReport(validateAndInsert);
                    if(validateAndInsert.get(0).length == 3) {
                        templateBankListReport.exportLogDatabase(response);
                    } else {
                        templateBankListReport.exportLogQuery(response);
                    }
                }
            }
            else if(part[2].equals("PERMISO"))
            {
                modelAndView.addObject("resp", "RP21-2");
            }
            else{
                TemplateBankListReport templateBankListReport = new TemplateBankListReport(list);
                templateBankListReport.exportLog(response);
            }

            boolean sendinter = intergrupo1Service.validateComponentTemplate(user,params.get("period").toString());
            modelAndView.addObject("inter",sendinter);

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/accountingLoad/downloadReport")
    @ResponseBody
    public void exportToExcelReport(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Precarga_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<String[]> templateList= new ArrayList<String[]>();
        templateList = templateBankService.findAllTemplates(user.getUsuario(),params.get("period").toString(),user);
        TemplateBankListReport listReport = new TemplateBankListReport(templateList);
        listReport.exportReport(response);
    }

    @GetMapping(value = "/parametric/accountingLoad/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Plantilla_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<String[]> templateList= new ArrayList<String[]>();
        templateList = templateBankService.findAllTemplatesPantilla(user.getUsuario(),params.get("period").toString(),user);
        TemplateBankListReport listReport = new TemplateBankListReport(templateList);
        listReport.export(response);
    }

    @RequestMapping (value = "/parametric/accountingLoad/sendIntergrupo")
    public ModelAndView getFromTB(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountingLoad");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String todayString = "";
        if (params.get("period") == null || params.get("period").toString() == "") {
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

        String order = params.get("order").toString();
        if(order.equals("Plantilla")){
            boolean result = intergrupo1Service.getFromTemplateBank(params.get("period").toString(), user.getUsuario());

            if(result){
                modelAndView.addObject("resp","sendBankTemplateCorrect");
            }else{
                modelAndView.addObject("resp","sendBankTemplateFail");
            }
        } else{
            List<String[]> preCharge = templateBankService.findAllTemplates(user.getUsuario(), todayString, user);

            boolean result = intergrupo1Service.getFromTemplateBankPreCharge(preCharge,params.get("period").toString(),user.getUsuario());

            if(result){
                modelAndView.addObject("resp","sendBankTemplateCorrect");
            }else{
                modelAndView.addObject("resp","sendBankTemplateFail");
            }
        }

        boolean sendinter = intergrupo1Service.validateComponentTemplate(user,todayString);
        modelAndView.addObject("inter",sendinter);


        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("order","Plantilla");
        modelAndView.addObject("vFilter",params.get("period").toString());

        return modelAndView;
    }

    @GetMapping(value = "/parametric/clearTemplate/{id}")
    @ResponseBody
    public boolean clearTemplate(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        boolean state = templateBankService.clearRegisterFront(user,id);
        return state;
    }
}