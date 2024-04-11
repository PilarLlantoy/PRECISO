package com.inter.proyecto_intergrupo.controller.admin;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.ResponsibleAccount;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelAccounts;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelListReport;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ResponsibleAccountService;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.rule.Mode;
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

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class    ControlPanelController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ControlPanelService controlPanelService;

    @Autowired
    private ResponsibleAccountService responsibleAccountService;

    boolean generalState = true;

    private List<String> listColumns=List.of("Componente","Input", "Centro Costos", "Empresa","Usuario Carga","Estado Carga");

    @GetMapping(value="/admin/controlPanel")
    public ModelAndView showControlPanel(@RequestParam Map<String, Object> params)
    {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Cuadro Mando Intergrupo")) {
            int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;

            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";
            String filtroText = "";
            String filtro = "VACIO";
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
            }
            else {
                    todayString = params.get("period").toString();
            }
            if (params.get("vId") != null) {
                filtroText=params.get("vId").toString();
            }
            if (params.get("vFilter") != null && params.get("vFilter").toString()!="VACIO") {
                filtro=params.get("vFilter").toString();
            }
            List<ControlPanel> list = controlPanelService.findByFechaReporte(todayString,filtro,filtroText);
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ControlPanel> pageControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageControl.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allControlPanel", pageControl.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("page", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", filtro);
            modelAndView.addObject("vId",filtroText);
            modelAndView.addObject("directory", "controlPanel");
            modelAndView.addObject("state", this.generalState);

            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("admin/controlPanel");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/admin/controlPanel/download")
    @ResponseBody
    public void exportToExcelControl(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CuadroMando" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        String filtroText = "";
        String filtro = "VACIO";
        if (params.get("vId") != null) {
            filtroText=params.get("vId").toString();
        }
        if (params.get("vFilter") != null&& params.get("vFilter").toString()!="VACIO") {
            filtro=params.get("vFilter").toString();
        }
        ControlPanelListReport listReport = new ControlPanelListReport(controlPanelService.findByFechaReporte(params.get("period").toString(),filtro,filtroText));
        listReport.export(response);
    }

    @GetMapping (value = "/admin/controlPanel/changeState")
    public ModelAndView changeEstado(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/controlPanel");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        int page = params.get("page") != null  ? (Integer.parseInt(params.get("page").toString())) : 0;

        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        String filtroText = "";
        String filtro = "VACIO";
        if (params.get("vId") != null) {
            filtroText=params.get("vId").toString();
        }
        if (params.get("vFilter") != null && params.get("vFilter").toString()!="VACIO") {
            filtro=params.get("vFilter").toString();
        }

        controlPanelService.changeState(controlPanelService.findByIdCuadroMando(params.get("center").toString(),params.get("input").toString(),params.get("comp").toString(),params.get("period").toString()).get(0),user);
        List<ControlPanel> list=controlPanelService.findByFechaReporte(params.get("period").toString(),filtro,filtroText);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ControlPanel> pageControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageControl.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            //modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allControlPanel",pageControl.getContent());
        if(params.get("page")!=null)
            modelAndView.addObject("page",Integer.parseInt(params.get("page").toString())+1);

        modelAndView.addObject("columns", listColumns);
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter", filtro);
        modelAndView.addObject("vId",filtroText);
        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("directory","controlPanel");

        return modelAndView;
    }

    @GetMapping(value = "/admin/controlPanel/sendNotification")
    public ModelAndView sendNotification(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, IOException, MessagingException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")!=null?(Integer.parseInt(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

        String filtroText = "";
        String filtro = "VACIO";
        if (params.get("vId") != null) {
            filtroText=params.get("vId").toString();
        }
        if (params.get("vFilter") != null&& params.get("vFilter").toString()!="VACIO") {
            filtro=params.get("vFilter").toString();
        }
        List<ControlPanel> list=controlPanelService.findByFechaReporte(params.get("period").toString(),filtro,filtroText);
        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ControlPanel> pageControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageControl.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allControlPanel",pageControl.getContent());
        modelAndView.addObject("columns", listColumns);
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("vFilter",params.get("period").toString());
        modelAndView.addObject("directory","controlPanel");

        ControlPanel panel= controlPanelService.findByIdCuadroMando(params.get("center").toString(),params.get("input").toString(),params.get("comp").toString(),params.get("period").toString()).get(0);
        controlPanelService.sendEmail(panel,userService.findUserByUserName(panel.getUsuarioCarga()));

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("admin/controlPanel");
        return modelAndView;
    }

    @GetMapping(value = "/admin/accounts")
    public ModelAndView showAccounts(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Object> list=controlPanelService.findByAccounts(params.get("center").toString(),params.get("compo").toString(),params.get("inpu").toString());
        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Object> pageControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageControl.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allAccounts",pageControl.getContent());
        modelAndView.addObject("columns", listColumns);
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("center",params.get("center").toString());
        modelAndView.addObject("compo",params.get("compo").toString());
        modelAndView.addObject("inpu",params.get("inpu").toString());
        modelAndView.addObject("directory","accounts");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("admin/accounts");
        return modelAndView;
    }

    @GetMapping(value="/admin/validateRp21")
    public ModelAndView validateRp21Query(HttpServletResponse response,RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String todayString="";
        if(params.get("period").toString()==null || params.get("period").toString()=="") {
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
        List<String> accounts = controlPanelService.validateRp21AndQuery("2021-08","1234");
        if(accounts.size() > 0 && !accounts.get(0).equals("Validación exitosa")){
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=AccountsValidate" + todayString + ".xlsx";
            response.setHeader(headerKey, headerValue);
            //ControlPanelAccounts listReport = new ControlPanelAccounts(accounts);
            //listReport.exportAccount(response);
        }
        modelAndView.setViewName("/admin/controlPanel");
        return  modelAndView;
    }

    @GetMapping(value="/admin/addComponente")
    public ModelAndView showAddComponent(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("inputs",responsibleAccountService.getComponents());
        modelAndView.addObject("compon",responsibleAccountService.getInputs());
        ControlPanel controlPanel = new ControlPanel();
        modelAndView.addObject("control", controlPanel);
        modelAndView.setViewName("admin/addControl");
        return modelAndView;
    }

    @PostMapping(value = "/admin/addComponente")
    public ModelAndView createNewComponent(@ModelAttribute ControlPanel control) {
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/controlPanel");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            controlPanelService.save(control);
            modelAndView.addObject("resp", "AddControl1");

        }
        catch (Exception e){
            modelAndView.addObject("resp", "AddControl0-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/admin/controlPanel/changeAllStates")
    public ModelAndView changeState(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/controlPanel");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(this.generalState){
            controlPanelService.changeAllStates(params.get("period").toString(),"Disable",user);
            this.generalState = false;
        }else {
            controlPanelService.changeAllStates(params.get("period").toString(),"Enable",user);
            this.generalState = true;
        }

        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("state", this.generalState);

        return modelAndView;
    }

    @PostMapping(value = "/admin/controlPanel/deleteIntergrupo")
    public ModelAndView deleteIntergrupo(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/controlPanel");

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

        String period = todayString;
        String inter = params.get("inter").toString();

        try{
            controlPanelService.deleteIntergrupo(period,inter);
            modelAndView.addObject("resp", "true");

        }catch (Exception e){
            modelAndView.addObject("resp", "false");
        }

        modelAndView.addObject("period",period);
        return modelAndView;
    }


}
