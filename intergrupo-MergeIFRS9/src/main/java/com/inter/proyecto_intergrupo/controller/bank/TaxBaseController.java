package com.inter.proyecto_intergrupo.controller.bank;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.TaxBase;
import com.inter.proyecto_intergrupo.model.bank.TaxBaseComplete;
import com.inter.proyecto_intergrupo.model.bank.TaxBaseLoad;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.TaxBaseListReport;
import com.inter.proyecto_intergrupo.service.bankServices.TaxBaseService;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class TaxBaseController {
    private static final int PAGINATIONCOUNT = 15;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;


    private List<String> listColumns = List.of("Cuenta", "NIT", "Contrato", "Divisa", "Importe");
    @Autowired
    private ThirdService thirdService;

    @Autowired
    private TaxBaseService taxBaseService;

    @GetMapping(value = "/bank/taxBase")
    public ModelAndView showAudit(@RequestParam Map<String, Object> params) throws ParseException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Base Fiscal")) {
            String todayString;
            String Id;
            String Vf;
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            if (params.get("vId") == null) {
                Id = "";
            } else {
                Id = params.get("vId").toString();
            }
            if (params.get("vFilter") == null) {
                Vf = "";
            } else {
                Vf = params.get("vFilter").toString();
            }

            if (params.get("period") == null | Objects.equals(params.get("period").toString(), "")) {
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

            ArrayList<TaxBase> list;

            if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
                list = taxBaseService.getTaxBase(todayString);
            } else {
                list = taxBaseService.findByFilter(Id, Vf, todayString);
            }


            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<TaxBase> pageTax = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageTax.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("vId", Id);
            modelAndView.addObject("vFilter", Vf);
            modelAndView.addObject("allTaxBase", pageTax.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("directory", "taxBase");
            modelAndView.addObject("registers",list.size());

            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("bank/taxBase");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/bank/taxBaseThirds")
    @ResponseBody
    public void showThirdsTaxBase(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {

        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=BaseFiscalTerceros_" + currentDateTime + ".txt";
        response.setHeader(headerKey, headerValue);
        List<Third> thirdList = new ArrayList<Third>();
        thirdList = thirdService.findAll();
        ThirdListReport listReport = new ThirdListReport(thirdList);
        listReport.exportNit(response);
    }

    @PostMapping(value = "/bank/taxBaseProcess")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/bank/taxBase");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            String month = params.get("period").toString();
            modelAndView.addObject("period", month);
            InputStream fileContent = filePart.getInputStream();
            boolean resultRead = taxBaseService.getGOFCode(fileContent, month, user);

            if (resultRead) {
                modelAndView.addObject("resp", "TX-1");
            } else {
                modelAndView.addObject("resp", "TX-2");
            }

        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "TX-3");
        }

        return modelAndView;

    }

    @GetMapping(value = "/bank/taxBase/delete")
    public ModelAndView deleteFromTB(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/bank/taxBase");

        taxBaseService.deleteFromBaseFiscal(params.get("period").toString());

        modelAndView.addObject("period", params.get("period").toString());
        modelAndView.addObject("directory", "taxBase");
        modelAndView.addObject("resp","deleteBaseFiscal");

        return modelAndView;
    }

    @GetMapping(value = "/bank/taxBase/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Base Fiscal_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ArrayList<TaxBase> taxList = taxBaseService.getTaxBase(params.get("period").toString());
        TaxBaseListReport listReport = new TaxBaseListReport(taxList, null);
        listReport.exportMatch(response);
    }

    @GetMapping(value = "/bank/taxBase/downloadAccum")
    @ResponseBody
    public void exportToExcelAccum(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Base Fiscal_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ArrayList<TaxBase> taxList = taxBaseService.getTaxBaseAccum(params.get("period").toString());
        TaxBaseListReport listReport = new TaxBaseListReport(taxList, null);
        listReport.exportMatch(response);
    }

    @GetMapping(value = "/bank/taxBase/downloadComplete")
    @ResponseBody
    public void exportTaxBase(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException{
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment;  filename=Base Fiscal_" + currentDateTime + ".txt");

        ArrayList<TaxBaseComplete> tax = taxBaseService.getTaxBaseComplete(params.get("period").toString());

        CsvService.downloadTaxBase(response.getWriter(), tax);
    }

    @GetMapping(value = "/bank/taxBase/downloadReportNoApply")
    @ResponseBody
    public void exportToExcelNoMatch(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Base Fiscal No Aplica_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ArrayList<Object[]> taxList = taxBaseService.getNoMatchBF(params.get("period").toString());
        TaxBaseListReport listReport = new TaxBaseListReport(null, taxList);
        listReport.exportNoMatch(response);
    }


    @GetMapping(value = "/bank/taxBase/downloadNoApplyComplete")
    @ResponseBody
    public void exportNoApply(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment;  filename=BaseFiscalNoAplica_" + currentDateTime + ".txt");

        List<Object[]> tax = taxBaseService.getNoMatchBFComplete(params.get("period").toString());

        CsvService.downloadCsv(response.getWriter(), tax);

    }

    @GetMapping(value = "/bank/taxBaseStateLoads")
    public ModelAndView showLoadTax(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
        List<TaxBaseLoad> list = taxBaseService.findAllLoad();

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<TaxBaseLoad> pageTax = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage = pageTax.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }
        modelAndView.addObject("allTaxBaseLoad", pageTax.getContent());
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("directory", "taxBaseStateLoads");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        modelAndView.setViewName("bank/taxBaseLoad");
        return modelAndView;
    }

    @GetMapping(value="/bank/taxBaseStateLoads/add")
    public ModelAndView showAddIndicators(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        TaxBaseLoad load = new TaxBaseLoad();
        modelAndView.addObject("load", load);
        modelAndView.setViewName("bank/addTaxBaseLoad");
        return modelAndView;
    }

    @PostMapping(value = "/bank/taxBaseStateLoads/add")
    public ModelAndView createNewIndicators(@ModelAttribute TaxBaseLoad load) {
        ModelAndView modelAndView = new ModelAndView("redirect:/bank/taxBaseStateLoads");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try
        {
            taxBaseService.saveLoad(load,user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

}
