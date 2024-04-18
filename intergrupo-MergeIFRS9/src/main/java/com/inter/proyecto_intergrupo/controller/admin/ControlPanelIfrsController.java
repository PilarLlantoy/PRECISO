package com.inter.proyecto_intergrupo.controller.admin;

import com.inter.proyecto_intergrupo.model.admin.ControlPanelIfrs;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelIfrs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.adminServices.*;
import com.inter.proyecto_intergrupo.service.parametricServices.ResponsibleAccountService;
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

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ControlPanelIfrsController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ControlPanelIfrsService controlPanelService;

    boolean generalState = true;

    @GetMapping(value="/admin/controlPanelIfrs")
    public ModelAndView showControlPanel(@RequestParam Map<String, Object> params)
    {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;

        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
        List<ControlPanelIfrs> list = controlPanelService.findAll();
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ControlPanelIfrs> pageControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage = pageControl.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }
        modelAndView.addObject("allControlPanel", pageControl.getContent());
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("page", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("directory", "controlPanelIfrs");
        modelAndView.addObject("state", this.generalState);

        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        modelAndView.setViewName("admin/controlPanelIfrs");
        return modelAndView;
    }

    @GetMapping(value = "/admin/controlPanelIfrs/download")
    @ResponseBody
    public void exportToExcelControl(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CuadroMando" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ControlPanelIfrs> auditList= new ArrayList<ControlPanelIfrs>();
        ControlPanelIfrsListReport listReport = new ControlPanelIfrsListReport(controlPanelService.findAll());
        listReport.export(response);
    }

    @GetMapping (value = "/admin/controlPanelIfrs/changeState")
    public ModelAndView changeEstado(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/controlPanelIfrs");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        int page = params.get("page") != null  ? (Integer.parseInt(params.get("page").toString())) : 0;

        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ControlPanelIfrs> list=controlPanelService.findAll();

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ControlPanelIfrs> pageControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageControl.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            //modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allControlPanel",pageControl.getContent());
        if(params.get("page")!=null)
            modelAndView.addObject("page",Integer.parseInt(params.get("page").toString())+1);

        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","controlPanelIfrs");

        controlPanelService.changeState(controlPanelService.findByIdCuadroMando(params.get("input").toString(),params.get("comp").toString()).get(0));
        return modelAndView;
    }

    @GetMapping (value = "/admin/controlPanelIfrs/fullDates")
    public ModelAndView loadDates(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/controlPanelIfrs");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        int page = params.get("page") != null  ? (Integer.parseInt(params.get("page").toString())) : 0;

        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ControlPanelIfrs> list=controlPanelService.findAll();

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ControlPanelIfrs> pageControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageControl.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            //modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allControlPanel",pageControl.getContent());
        if(params.get("page")!=null)
            modelAndView.addObject("page",Integer.parseInt(params.get("page").toString())+1);

        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","controlPanelIfrs");

        controlPanelService.loadDates();
        return modelAndView;
    }

    @GetMapping(value = "/admin/controlPanelIfrs/changeAllStates")
    public ModelAndView changeState(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/controlPanelIfrs");

        if(this.generalState){
            controlPanelService.changeAllStates("Disable");
            this.generalState = false;
        }else {
            controlPanelService.changeAllStates("Enable");
            this.generalState = true;
        }
        modelAndView.addObject("state", this.generalState);

        return modelAndView;
    }

}
