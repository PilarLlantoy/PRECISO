package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.CurrencyListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.CurrencyService;
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
public class CurrencyController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private CurrencyService currencyService;

    private List<String> listColumns=List.of("Código Divisa", "Nombre Divisa", "Divisa Neocon");

    @GetMapping(value="/parametric/currency")
    public ModelAndView showCurrency(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Transformación de Divisas")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<Currency> pageCurrency = currencyService.getAll(pageRequest);
            int totalPage = pageCurrency.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allCurrencies", pageCurrency.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "currency");
            modelAndView.addObject("registers",pageCurrency.getTotalElements());

            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/currency");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/currency")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/currency");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Currency> currencyList = currencyService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            CurrencyListReport listReport = new CurrencyListReport(currencyList);
            ArrayList<String[]> list=currencyService.saveFileBD(fileContent, user);
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
        }
        finally {
            return  modelAndView;
        }
    }

    @GetMapping(value = "/parametric/modifyCurrencies/{id}")
    @ResponseBody
    public ModelAndView modifyCurrencies(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Currency toModify = currencyService.findCurrencyById(id);
        modelAndView.addObject("currencyModify",toModify);
        modelAndView.addObject("idOld",toModify.getId());
        modelAndView.setViewName("parametric/modifyCurrencies");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyCurrencies")
    @ResponseBody
    public ModelAndView updateCurrency(@ModelAttribute Currency currency,@RequestParam String idOld){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/currency");
        try {
            Currency searchCurrency = currencyService.findCurrencyById(currency.getId());
            if (searchCurrency==null||idOld.equals(currency.getId()))
            {
                currencyService.modifyCurrencies(currency, idOld,user );
                modelAndView.addObject("resp", "Modify1");
            }
            else
            {
                modelAndView.addObject("resp", "Modify0");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "UpdateCascade-1");
        }
        return  modelAndView;

    }

    @GetMapping(value = "/parametric/validateId")
    @ResponseBody
    public String validateIdCurrencies(@RequestParam String idNew,@RequestParam String idT){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(currencyService.findCurrencyById(idNew)==null||idNew.equals(idT))
            result="valid";
        return  result;
    }

    @GetMapping(value="/parametric/addCurrency")
    public ModelAndView showAddCurrency(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Currency currency = new Currency();
        modelAndView.addObject("currency", currency);
        modelAndView.setViewName("parametric/addCurrency");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addCurrency")
    public ModelAndView createNewCurrency(@ModelAttribute Currency currency) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/currency");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (currencyService.findCurrencyById(currency.getId()) == null) {
                currencyService.saveCurrency(currency,user);
                modelAndView.addObject("resp", "Add1");
            } else {
                modelAndView.addObject("resp", "Add0");
            }
        }
        catch (Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/removeCurrencies/{id}")
    @ResponseBody
    public boolean removeCurrencies(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/currency");
        boolean response=false;
        try {
            Currency toRemove = currencyService.findCurrencyById(id);
            currencyService.removeCurrency(toRemove.getId(),user);
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;

    }

    @GetMapping(value = "/parametric/clearCurrency")
    @ResponseBody
    public boolean clearCurrencies(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        boolean response=false;
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/currency");
        try{
            currencyService.clearCurrency(user);
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/currency/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Divisas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Currency> currencyList= new ArrayList<Currency>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            currencyList = currencyService.findAll();
        }
        else{
            currencyList = currencyService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        CurrencyListReport listReport = new CurrencyListReport(currencyList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchCurrency")
    @ResponseBody
    public ModelAndView searchCurrencies(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Currency> list=currencyService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Currency> pageCurrency = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageCurrency.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allCurrencies",pageCurrency.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchCurrency");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/currency");
        return modelAndView;
    }
}
