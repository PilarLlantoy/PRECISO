package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.RechazosDescontabilizacionListReport;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.RechazosDescontabilizacionService;
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
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class RechazosDescontabilizacionController {

    private static final int PAGINATIONCOUNT=10;

    @Autowired
    RechazosDescontabilizacionService rechazosDescontabilizacionService;

    @Autowired
    private UserService userService;

    public ModelAndView showRechazosDescontabilizacion(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @GetMapping(value="/parametric/rechazosDescontabilizacion")
    public ModelAndView showTemplateConsolidacion(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Rechazos Descontabilización")) {

        int page=params.get("page")!=null?(Integer.parseInt(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

        String todayString = "";
        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
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
        modelAndView.addObject("period", todayString);

        String order = "";
        if (params.get("order") == null || Objects.equals(params.get("order").toString(), "")) {
            modelAndView.addObject("order", "ContratoEnVerticalNoConciliacion");
            order = "2";
        } else {
            modelAndView.addObject("order", params.get("order").toString());
            if(params.get("order").toString().equals("NoFiltro")){
                order = "%";
            } else if(params.get("order").toString().equals("CuentaNoHomologada")){
                order = "1";
            } else {
                order = "2";
            }
        }

        int opcion;
        if (params.get("opcion") == null || Objects.equals(params.get("opcion").toString(), "")) {
            modelAndView.addObject("opcion", "Pre-Carga");
            opcion = 1;
        } else {
            modelAndView.addObject("opcion", params.get("opcion").toString());
            if(params.get("opcion").toString().equals("Pre-Carga")){
                opcion = 1;
            } else {
                opcion = 2;
            }
        }

        List<Object[]> rechazosDescontabilizacionList;
        if(opcion == 1){
            rechazosDescontabilizacionList = rechazosDescontabilizacionService.getRechazosDescontabilizacionPre(todayString,order);
        } else {
            rechazosDescontabilizacionList = rechazosDescontabilizacionService.getRechazosDescontabilizacion(todayString);
        }

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), rechazosDescontabilizacionList.size());
        Page<Object[]> pageRechazosDescontabilizacion = new PageImpl<>(rechazosDescontabilizacionList.subList(start, end), pageRequest, rechazosDescontabilizacionList.size());

        int totalPage=pageRechazosDescontabilizacion.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        modelAndView.addObject("allTemplateRechazos",pageRechazosDescontabilizacion.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","rechazosDescontabilizacion");
        modelAndView.addObject("registers", rechazosDescontabilizacionList.size());

        modelAndView.setViewName("/Ifrs9Parametrics/rechazosDescontabilizacion");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/rejectDescon/upload")
    public ModelAndView loadData(@RequestParam Map<String, Object> params){
        String period = params.get("fileDate").toString();
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/rechazosDescontabilizacion");
        boolean salida;

        try{
            salida = rechazosDescontabilizacionService.cargarRechazosDescontabilizacionPre(period,"DIRECTORY",null,"NO APLICA");
            if (!salida){
                modelAndView.addObject("resp", "RechazosNoEncontrado");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        modelAndView.addObject("period",period);

        return  modelAndView;

    }

    @PostMapping(value = "/parametric/rejectDescon/uploadExam")
    public ModelAndView loadDataExam(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        String period = params.get("period").toString();
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/rechazosDescontabilizacion");
        boolean salida;

        try{
            /*Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();*/
            salida = rechazosDescontabilizacionService.cargarRechazosDescontabilizacionPre(period,"FILE",null,params.get("ruta").toString());
            if (!salida){
                modelAndView.addObject("resp", "RechazosNoEncontrado");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        modelAndView.addObject("period",period);

        return  modelAndView;

    }

    @PostMapping(value="/parametric/rechazosDescontabilizacionPreCarga")
    public ModelAndView loadRechazosDescontabilizacionPre(@RequestParam Map<String, Object> params) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/rechazosDescontabilizacion");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean salida;

        Date todayString = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        String realDate = formatter.format(todayString);

        Date dateMinusMonth = formatter.parse(realDate);
        Calendar c = Calendar.getInstance();
        c.setTime(dateMinusMonth);
        c.add(Calendar.MONTH,-1);
        Date resultAfter  = c.getTime();

        String finalDate = formatter.format(resultAfter);



        try{
            salida = rechazosDescontabilizacionService.cargarRechazosDescontabilizacionPre(finalDate,"DIRECTORY",null,"NO APLICA");
            if (!salida){
                modelAndView.addObject("resp", "RechazosNoEncontrado");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return  modelAndView;
    }

    @PostMapping(value="/parametric/rechazosDescontabilizacion")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/rechazosDescontabilizacion");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logInsercion_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

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


        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = rechazosDescontabilizacionService.saveFileBD(fileContent, user, todayString);
            String[] part = list.get(0);
            if (part[2].equals("true")) {
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                //listReport.exportLog(response,list);
            } else if (part[2].equals("falseFormat")) {
                int rowTemp = (Integer.parseInt(part[0]) + 1);
                part[0] = String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep-1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            } else {
                int rowTemp = (Integer.parseInt(part[0]) + 1);
                part[0] = String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep0");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                return modelAndView;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return modelAndView;
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/clearRechazosDescontabilizacion/{id}")
    public ModelAndView clearRechazosDescontabilizacion(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        rechazosDescontabilizacionService.clearRechazosDescontabilizacion(user,id);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/rechazosDescontabilizacion?period="+id);
        return  modelAndView;
    }

    @RequestMapping(value = "/parametric/rechazosDescontabilizacion/downloadPerCarga")
    @ResponseBody
    public void exportRechazosDescontabilizacionPre(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";

            String headerValue = "attachment; filename=RechazosDescontabilizacionPreCarga_" + params.get("period").toString() + "_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);

            String order = "";
            if (params.get("order") == null || params.get("order").toString() == "") {
                order = "%";
            } else {
                if(params.get("order").toString().equals("NoFiltro")){
                    order = "%";
                } else if(params.get("order").toString().equals("CuentaNoHomologada")){
                    order = "1";
                } else {
                    order = "2";
                }
            }

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

            List<Object[]> rechazosDescontabilizacionPreCargaList = rechazosDescontabilizacionService.getRechazosDescontabilizacionPre(todayString,order);
            RechazosDescontabilizacionListReport listReport = new RechazosDescontabilizacionListReport(rechazosDescontabilizacionPreCargaList,"Pre-Carga");
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/parametric/rechazosDescontabilizacion/download")
    @ResponseBody
    public void exportRechazosDescontabilizacion(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=RechazosDescontabilizacion_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<Object[]> rechazosDescontabilizacionList = rechazosDescontabilizacionService.getRechazosDescontabilizacion(params.get("period").toString());
            RechazosDescontabilizacionListReport listReport = new RechazosDescontabilizacionListReport(rechazosDescontabilizacionList,"Salida");
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/parametric/rechazosDescontabilizacion/downloadCV")
    @ResponseBody
    public void exportRechazosDescontabilizacionCV(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=CruceValidaciónRechazosDescontabilizacion_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<Object[]> rechazosDescontabilizacionList = rechazosDescontabilizacionService.getRechazosDescontabilizacionCV(params.get("period").toString());
            RechazosDescontabilizacionListReport listReport = new RechazosDescontabilizacionListReport(rechazosDescontabilizacionList,"CV");
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}