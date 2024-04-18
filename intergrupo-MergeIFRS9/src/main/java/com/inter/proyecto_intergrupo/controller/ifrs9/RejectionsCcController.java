package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelIfrs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.RejectionsCc;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.reports.Rp21;
import com.inter.proyecto_intergrupo.repository.parametric.TypeEntityRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelIfrsService;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.RejectionsCcListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.RejectionsCcService;
import com.inter.proyecto_intergrupo.service.parametricServices.QueryService;
import com.inter.proyecto_intergrupo.service.reportsServices.Rp21DiffListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.Rp21ListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.Rp21Service;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class RejectionsCcController {
    private static final int PAGINATIONCOUNT=20;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private RejectionsCcService rejectionsCcService;

    @Autowired
    private ControlPanelIfrsService controlPanelService;

    @GetMapping(value="/ifrs/rejectionsCc")
    public ModelAndView showRejectionsCc(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Rechazos (Creación Cuentas/Cambios Segementos)")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            List<Object[]> list = rejectionsCcService.getAllReport();
            List<ControlPanelIfrs> listControl = controlPanelService.findByCPI(user);

            int countFull = 0;

            for (int i = 0; i < listControl.size(); i++) {
                if (listControl.get(i).getSemaforoComponente().equals("FULL")) {
                    countFull++;
                }
            }

            if (countFull >= 6 ){
                modelAndView.addObject("statusComponent", true);
            } else {
                modelAndView.addObject("statusComponent", false);
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageRejectionsCc = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageRejectionsCc.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allRejections", pageRejectionsCc.getContent());
            modelAndView.addObject("allControlData", listControl);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("directory", "rejectionsCc");
            modelAndView.addObject("registers",list.size());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/rejectionsCc");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/rejectionsCc")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectionsCc");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<RejectionsCc> rejectionsCcList = new ArrayList<>();//rejectionsCcService.findAll();
        try {
            Collection<Part> parts = request.getParts();
            RejectionsCcListReport listReport = new RejectionsCcListReport(rejectionsCcList,null);
            List<String[]> list = new ArrayList<>();
            try{
                long startTime = System.nanoTime();
                list=rejectionsCcService.saveFileBD(parts,user);
            }catch(Exception e){
                e.printStackTrace();
            }
            if(list.get(0)[0].equals("ERROR"))
            {
                modelAndView.addObject("resp", "RCH-1");
            }
            else if(list.get(0)[0].equals("PERMISO"))
            {
                modelAndView.addObject("resp", "RP21-2");
            }
            else if(list.get(0)[0].equals("DUPLICADO"))
            {
                modelAndView.addObject("resp", "RP21-3");
            }
            else
            {
                try{
                listReport.exportLog(response,list);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return  modelAndView;

    }

    @GetMapping(value = "/ifrs/rejectionsCc/downloadReport")
    @ResponseBody
    public void exportToExcelReport(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ReporteRechazos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> rp21List= new ArrayList<Object[]>();
        rp21List = rejectionsCcService.getAllReport();
        RejectionsCcListReport listReport = new RejectionsCcListReport(null,rp21List);
        listReport.exportReport(response);
    }


    @GetMapping(value = "/ifrs/rejectionsCc/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Rechazos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<RejectionsCc> rejectionsCcList= new ArrayList<>();
        rejectionsCcList = rejectionsCcService.findAllExclude(params.get("ci"),params.get("cp"),params.get("cr"),params.get("ri"), params.get("rp"),params.get("rr"));
        RejectionsCcListReport listReport = new RejectionsCcListReport(rejectionsCcList,null);
        listReport.export(response);
    }

    @GetMapping(value="/ifrs/deleteDataR")
    @ResponseBody
    public ModelAndView deleteInputR(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectionsCc");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String input = params.get("input").toString();

        boolean resultDelete = rejectionsCcService.deleteInputRejectionsCc(input,user);

        if (resultDelete){
            modelAndView.addObject("resp","DRP-0");
        }else{
            modelAndView.addObject("resp","DRP-1");
        }

        return modelAndView;
    }

    @GetMapping(value="/ifrs/confirmDataR")
    @ResponseBody
    public ModelAndView confirmInputR(@RequestParam Map<String, Object> params,HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectionsCc");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        boolean resultData = rejectionsCcService.confirmData(user,request);

        if (resultData) {
            modelAndView.addObject("resp", "confirmDataRG");
        } else {
            modelAndView.addObject("resp", "confirmDataR-1");
        }

        return modelAndView;
    }

    @GetMapping(value="/ifrs/processDataLoad")
    @ResponseBody
    public ModelAndView processDataLoad(@RequestParam Map<String, Object> params,HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectionsCc");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        boolean resultData = rejectionsCcService.processDataLoad();

        if (resultData) {
            modelAndView.addObject("resp", "updateDataC");
        } else {
            modelAndView.addObject("resp", "updateDataC-1");
        }

        return modelAndView;
    }

    @GetMapping(value="/ifrs/deleteReject")
    @ResponseBody
    public boolean deleteAll(@RequestParam Map<String, Object> params,HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectionsCc");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        boolean resp= rejectionsCcService.deleteAll();

        return resp;
    }

    @GetMapping(value = "/ifrs/modifyRejectionsCc/{id}")
    @ResponseBody
    public ModelAndView modifyRejections(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        String[] partsId = id.split("---");
        modelAndView.addObject("account",partsId[0]);
        modelAndView.addObject("divisa",partsId[1]);
        modelAndView.addObject("valida",partsId[2]);
        modelAndView.setViewName("ifrs/modifyRejectionsCc");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/modifyRejectionsCc")
    @ResponseBody
    public ModelAndView updateRejectionCc(@Param(value = "id") String id,@RequestParam String typeNew){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectionsCc");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (rejectionsCcService.changeRejectReal(typeNew,id))
            {
                modelAndView.addObject("resp", "Modify1");
            }
            else
            {
                modelAndView.addObject("resp", "Modify0");
            }
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }
}
