package com.inter.proyecto_intergrupo.controller.briefcase;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.BalfiduciariaIcrv;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.briefcaseServices.BalfiduciariaIcrvListReport;
import com.inter.proyecto_intergrupo.service.briefcaseServices.BalfiduciariaIcrvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
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
public class BalfiduciariaIcrvController {

    @Autowired
    BalfiduciariaIcrvService balfiduciariaIcrvService;

    @Autowired
    private UserService userService;

    private static final int PAGINATIONCOUNT=12;
    private final List<String> listColumns = List.of("Corte","Código Contabilidad","Nombre Fideicomiso","Año","Mes","Código PUC", "Código Cuenta NIIF","Código PUC Local","Nombre","Saldo Final","Nivel", "Código Consolidación", "Código Gestión", "L4");


    @GetMapping(value = "/briefcase/balfiduciariaicrv")
    public ModelAndView showBalView(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Bal Fiduciaria ICRV")){
            int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";
            String Id = "";
            String Vf = "";

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

            if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                if(calendar.get(Calendar.MONTH)==0)
                {
                    calendar.add(Calendar.YEAR,-1);
                    todayString = calendar.get(Calendar.YEAR) +"" /*"-12"*/;
                }
                else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + ""/*"-" + calendar.get(Calendar.MONTH)*/;
                } else {
                    todayString = calendar.get(Calendar.YEAR) + ""/*"-0" + calendar.get(Calendar.MONTH)*/;
                }
            } else {
                todayString = params.get("period").toString();
            }

            List<BalfiduciariaIcrv> list;

            if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
                list = balfiduciariaIcrvService.findAllBal(todayString);
                modelAndView.addObject("filterExport", "Original");
            } else {
                list = balfiduciariaIcrvService.findByFilter(Id,Vf,todayString);
                modelAndView.addObject("filterExport", "Filtrado");
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<BalfiduciariaIcrv> pageList = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageList.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("vId", Id);
            modelAndView.addObject("vFilter", Vf);
            modelAndView.addObject("balfiduciariaicrvList", pageList.getContent());
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("directory", "balfiduciariaicrv");
            modelAndView.addObject("registers",list.size());
            modelAndView.setViewName("/briefcase/balfiduciariaicrv");

        }
        else
        {
            modelAndView.addObject("balfiduciariaicrv","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/briefcase/balfiduciariaicrv")
    public ModelAndView loadBal(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/balfiduciariaicrv");
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
            ArrayList<String[]> list = balfiduciariaIcrvService.saveFileBDPlantilla(fileContent,user,params.get("period").toString());
            modelAndView.addObject("period", params.get("period").toString());
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                BalfiduciariaIcrvListReport balfiduciariaicrvListReport = new BalfiduciariaIcrvListReport(list,null);
                balfiduciariaicrvListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/balfiduciariaicrv/download")
    @ResponseBody
    public void downloadBal(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        String Id = "";
        String Vf = "";

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
        List<BalfiduciariaIcrv> list;
        if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
            list = balfiduciariaIcrvService.findAllBal(params.get("period").toString());
        } else {
            list = balfiduciariaIcrvService.findByFilter(Id,Vf,params.get("period").toString());
        }
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= BalFiduciariaICRV_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        BalfiduciariaIcrvListReport listReport = new BalfiduciariaIcrvListReport(null, list);
        listReport.export(response);
    }

    @GetMapping(value = "/briefcase/modifyBalfiduciariaicrv/{id}")
    @ResponseBody
    public ModelAndView modifyBalfiduciariaicrv(@PathVariable String id){
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
        BalfiduciariaIcrv toModify = balfiduciariaIcrvService.findByIdBal(idTemp);
        modelAndView.addObject("balfiduciariaicrvModify",toModify);
        modelAndView.setViewName("briefcase/modifyBalfiduciariaicrv");
        return modelAndView;
    }

    @PostMapping(value = "/briefcase/modifyBalfiduciariaicrv")
    @ResponseBody
    public ModelAndView updateBalfiduciariaicrv(@ModelAttribute BalfiduciariaIcrv balfiduciariaicrv){
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/balfiduciariaicrv");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            balfiduciariaIcrvService.modifyBal(balfiduciariaicrv, user);
            modelAndView.addObject("period", balfiduciariaicrv.getPeriodo());
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value = "/briefcase/removeBalfiduciariaicrv/{id}")
    @ResponseBody
    public ModelAndView removeBalfiduciariaicrv(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        balfiduciariaIcrvService.clearBalfiduciariaIcrv(user,id);
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/balfiduciariaicrv?period="+id);
        return  modelAndView;
    }
}
