package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.ChangeCurrency;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
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
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ChangeCurrencyController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ChangeCurrencyService changeCurrencyService;

    private List<String> listColumns=List.of("Fecha", "Divisa", "Valor");

    @GetMapping(value="/parametric/changeCurrency")
    public ModelAndView showChangeCurrency(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Cambio Valor Divisa")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            List<ChangeCurrency> list=changeCurrencyService.findAll();

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ChangeCurrency> pageChangeCurrency = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageChangeCurrency.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
                modelAndView.addObject("allChanges", pageChangeCurrency.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "changeCurrency");
            modelAndView.addObject("registers",pageChangeCurrency.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/changeCurrency");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/changeCurrency")
    public void uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/changeCurrency");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ChangeCurrency> changeCurrencyList = changeCurrencyService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ChangeCurrencyListReport listReport = new ChangeCurrencyListReport(changeCurrencyList);
            ArrayList<String[]> list=changeCurrencyService.saveFileBD(fileContent,user);
            String[] part=list.get(0);

            modelAndView.addObject("resp", "AddRep1");
            modelAndView.addObject("row", part[0]);
            modelAndView.addObject("colum", part[1]);
            listReport.exportLog(response,list);
            response.sendRedirect("/parametric/changeCurrency");


        }catch(Exception e){
            //return  modelAndView;
        }
        //return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyChangeCurrency/{id}")
    @ResponseBody
    public ModelAndView modifyChangeCurrency(@PathVariable String id,@PathVariable String di) throws ParseException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        Date fecha = formato.parse(id);
        ChangeCurrency toModify = changeCurrencyService.findChangeCurrencyByDivisaYFecha(null,null,di).get(0);
        modelAndView.addObject("changeCurrencyModify",toModify);
        modelAndView.addObject("fechaId",toModify.getFecha());
        modelAndView.addObject("divisaId",toModify.getDivisa());
        modelAndView.setViewName("parametric/modifyChangeCurrency");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyChangeCurrency")
    @ResponseBody
    public ModelAndView updateChangeCurrency(@ModelAttribute ChangeCurrency changeCurrency,@Param(value = "fecha") String fecha,@RequestParam("divisa") String divisa){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/changeCurrency");
        try {
            ChangeCurrency searchGarantBank = changeCurrencyService.findChangeCurrencyByDivisaYFecha(fecha,fecha,divisa).get(0);
            //if (searchGarantBank==null||fecha.equals(searchGarantBank.getFecha()))
            {
                changeCurrencyService.modifyChangeCurrency(searchGarantBank,divisa,user,fecha);
                modelAndView.addObject("resp", "Modify1");
            }
            //else
            {
              //  modelAndView.addObject("resp", "Modify0");
            }
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value="/parametric/addChangeCurrency")
    public ModelAndView showAddChangeCurrency(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/addChangeCurrency");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addChangeCurrency/add")
    public ModelAndView addChangeCurrency(@RequestParam Map<String, Object> params) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/changeCurrency");
        ChangeCurrency insert= new ChangeCurrency();
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        formato.applyPattern("yyyy-MM-dd");
        Date fecha = formato.parse(params.get("fecha").toString()+"-01");
        insert.setFecha(fecha);
        insert.setDivisa(params.get("divisa").toString().toUpperCase(Locale.ROOT));
        insert.setValor(Double.parseDouble(params.get("valor").toString()));
        boolean result = changeCurrencyService.insertChangeCurrency(insert);

        if(result){
            modelAndView.addObject("resp", "Add1");
        }else {
            modelAndView.addObject("resp","Add0");
        }

        return modelAndView;
    }

    @GetMapping(value = "/parametric/removeChangeCurrency/{id}")
    @ResponseBody
    public ModelAndView removeChangeCurrency(@PathVariable String id) throws ParseException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String[] partsId= id.split("00:00:00.0");
        String[] partsDate = partsId[0].split("-");
        ChangeCurrency toRemove = changeCurrencyService.findChangeCurrencyByDivisaYFecha(partsDate[0],partsDate[1],partsId[1]).get(0);
        changeCurrencyService.removeChangeCurrency(toRemove.getFecha(),toRemove.getDivisa(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/changeCurrency");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearChangeCurrency")
    public ModelAndView clearChangeCurrency(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        changeCurrencyService.clearChangeCurrency(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/changeCurrency");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/changeCurrency/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CambioValorDivisa" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ChangeCurrency> changeCurrencyList= new ArrayList<ChangeCurrency>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            changeCurrencyList = changeCurrencyService.findAll();
        }
        else{
            changeCurrencyList = changeCurrencyService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ChangeCurrencyListReport listReport = new ChangeCurrencyListReport(changeCurrencyList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchChangeCurrency")
    @ResponseBody
    public ModelAndView searchChangeCurrency(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ChangeCurrency> list=changeCurrencyService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ChangeCurrency> pageChangeCurrency = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageChangeCurrency.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allChanges",pageChangeCurrency.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchChangeCurrency");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/changeCurrency");
        return modelAndView;
    }
}
