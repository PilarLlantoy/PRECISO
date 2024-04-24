package com.inter.proyecto_intergrupo.controller.admin;

import com.inter.proyecto_intergrupo.model.admin.ControlPanelJobs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelJobsService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.TaxBaseService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.CondetaRIService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.Desconnv15Service;
import com.inter.proyecto_intergrupo.service.ifrs9Services.H140Service;
import com.inter.proyecto_intergrupo.service.ifrs9Services.OnePercentService;
import com.inter.proyecto_intergrupo.service.informationServices.MisService;
import com.inter.proyecto_intergrupo.service.informationServices.SiccService;
import com.inter.proyecto_intergrupo.service.parametricServices.QueryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ControlPanelJobsController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ControlPanelJobsService controlPanelJobsService;

    @Autowired
    private TaxBaseService taxBaseService;

    @Autowired
    private H140Service h140Service;

    @Autowired
    private CondetaRIService condetaRIService;

    @Autowired
    private Desconnv15Service desconnv15Service;

    @Autowired
    private QueryService queryService;

    @Autowired
    private SiccService siccService;

    @Autowired
    private MisService misService;

    @Autowired
    private OnePercentService onePercentService;

    @GetMapping(value="/admin/controlPanelJobs")
    public ModelAndView showControlPanel(@RequestParam Map<String, Object> params)
    {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Cuadro Mando Jobs")) {
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            List<ControlPanelJobs> list = controlPanelJobsService.findAll();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ControlPanelJobs> pageControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageControl.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allJob", pageControl.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("directory", "controlPanelJobs");

            modelAndView.setViewName("admin/controlPanelJobs");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/admin/controlPanelJobsLoad140")
    public ModelAndView loadData140H(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params)
    {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Cuadro Mando Jobs")) {
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            List<ControlPanelJobs> list = controlPanelJobsService.findAll();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ControlPanelJobs> pageControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageControl.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allJob", pageControl.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("directory", "controlPanelJobs");

            try {
                Part filePart = request.getPart("file");
                InputStream fileContent = filePart.getInputStream();
                h140Service.loadH140File(fileContent);
                modelAndView.addObject("resp", "contAdd");
                modelAndView.setViewName("home");
            }
            catch(Exception e)
            {
                e.printStackTrace();
                modelAndView.addObject("resp", "ContingentesLoadFail");
                modelAndView.setViewName("home");
                return modelAndView;
            }
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/admin/controlPanelJobs/changeState")
    public ModelAndView changeEstado(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, IOException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ControlPanelJobs> list=controlPanelJobsService.findAll();
        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ControlPanelJobs> pageControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageControl.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allJob",pageControl.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","controlPanelJobs");

        controlPanelJobsService.changeState(controlPanelJobsService.findByIdJob(Integer.parseInt(params.get("id").toString())));

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("admin/controlPanelJobs");
        return modelAndView;
    }

    @GetMapping(value = "/admin/controlPanelJobs/executeJob")
    public ModelAndView executeJob(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, IOException, InvalidFormatException, ParseException {
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/controlPanelJobs");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String todayString="";
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
            todayString = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
        }
        else {
            todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
        }

        ControlPanelJobs job =controlPanelJobsService.findByIdJob(Integer.parseInt(params.get("id").toString()));

        //final String dirPrimario = "\\\\co.igrupobbva\\svrfilesystem\\TX\\RECEPCION_HOST\\XC\\ASES_FISCAL_IMPUESTOS\\";
        final String dirPrimario = "\\\\co.igrupobbva\\svrfilesystem\\TX\\ENVIO_HOST\\XC\\CONSOLIDACION\\";
        //final String dirSecundaria = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\infocontable\\01-NEXCO\\";
        final String dirSecundaria = "C:\\Users\\CE66916\\Documents\\Mario\\BaseFiscal\\";

        if(job.getNombre().equals("GOF Base Fiscal")){

            boolean state = taxBaseService.loadData(dirPrimario);
            boolean state2 = taxBaseService.loadData(dirSecundaria);
            job.setFechaEjecucion(new Date());
            System.out.println(state);
            System.out.println(state2);
            if(state || state2) {
                job.setFechaEjecucionExitosa(new Date());
                System.out.println("aca");
            }
            else
            {
                modelAndView.addObject("resp","JobBaseFiscal-1");
                System.out.println("aca no");
            }
            controlPanelJobsService.save(job);
        }
        else if(job.getNombre().equals("HADT140")){
            boolean state=h140Service.loadH140(user);
            job.setFechaEjecucion(new Date());
            if(state)
                job.setFechaEjecucionExitosa(new Date());
            controlPanelJobsService.save(job);
        }
        else if(job.getNombre().equals("CONDETARI")){
            boolean state=condetaRIService.loadFileQueryManual(user);
            job.setFechaEjecucion(new Date());
            if(state)
                job.setFechaEjecucionExitosa(new Date());
            controlPanelJobsService.save(job);
        }
        else if(job.getNombre().equals("Query")){
            boolean state = queryService.loadFileQuerySub(user);
            job.setFechaEjecucion(new Date());
            if(state==true)
                job.setFechaEjecucionExitosa(new Date());
            controlPanelJobsService.save(job);
        }else if(job.getNombre().equals("SICC")){
            try {
                boolean state = siccService.saveFromSiccDatabase(todayString.replace("-", ""),user);
                job.setFechaEjecucion(new Date());
                if (state == true)
                    job.setFechaEjecucionExitosa(new Date());
                controlPanelJobsService.save(job);
            }
            catch(Exception e){
                job.setFechaEjecucion(new Date());
                controlPanelJobsService.save(job);
                e.printStackTrace();
            }
        }else if(job.getNombre().equals("Depositos")){
            try {
                boolean state = misService.insertFromMis(todayString,user);
                job.setFechaEjecucion(new Date());
                if (state)
                    job.setFechaEjecucionExitosa(new Date());
                controlPanelJobsService.save(job);
            }
            catch(Exception e){
                job.setFechaEjecucion(new Date());
                controlPanelJobsService.save(job);
                e.printStackTrace();
            }
        }else if(job.getNombre().equals("Calculo 1%")){
            try {
                boolean state = onePercentService.executeJobManual(user);
                job.setFechaEjecucion(new Date());
                if (state)
                    job.setFechaEjecucionExitosa(new Date());
                controlPanelJobsService.save(job);
            }
            catch(Exception e){
                job.setFechaEjecucion(new Date());
                controlPanelJobsService.save(job);
                e.printStackTrace();
            }
        }

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ControlPanelJobs> list=controlPanelJobsService.findAll();
        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ControlPanelJobs> pageControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageControl.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        modelAndView.addObject("allJob",pageControl.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","controlPanelJobs");

        //modelAndView.setViewName("admin/controlPanelJobs");
        return modelAndView;
    }
}
