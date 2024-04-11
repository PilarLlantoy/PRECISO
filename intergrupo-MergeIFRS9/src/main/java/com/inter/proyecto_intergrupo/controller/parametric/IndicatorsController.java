package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Indicators;
import com.inter.proyecto_intergrupo.model.parametric.Subsidiaries;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class IndicatorsController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private IndicatorsService indicatorsService;

    private List<String> listColumns=List.of("Cuenta Neocon", "Signo");

    @GetMapping(value="/parametric/indicators")
    public ModelAndView showIndicators(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Indicadores Intergrupo")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<Indicators> pageIndicators = indicatorsService.getAll(pageRequest);
            int totalPage = pageIndicators.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allIndicators", pageIndicators.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "indicators");
            modelAndView.addObject("registers",pageIndicators.getTotalElements());

            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/indicators");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/indicators")
    public ModelAndView uploadFileIndicators(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/indicators");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Indicators> indicatorsList = indicatorsService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            IndicatorsListReport listReport = new IndicatorsListReport(indicatorsList);
            ArrayList<String[]> list=indicatorsService.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/parametric/modifyIndicators/{id}")
    @ResponseBody
    public ModelAndView modifyIndicators(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Indicators toModify = indicatorsService.findIndicatorsByCuentaNeocon(id);
        modelAndView.addObject("indicatorsModify",toModify);
        modelAndView.setViewName("parametric/modifyIndicators");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/modifySign/{id}")
    @ResponseBody
    public ModelAndView modifyIndicatorsSign(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("resp", "Modify1");
        Indicators toModify = indicatorsService.findIndicatorsByCuentaNeocon(id);
        indicatorsService.modifySign(toModify);
        modelAndView.setViewName("parametric/indicators");
        return showIndicators(modelAndView.getModel());
    }

    @PostMapping(value = "/parametric/modifyIndicators")
    @ResponseBody
    public ModelAndView updateIndicators(@ModelAttribute Indicators indicators,@Param(value = "id") String id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/indicators");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            Indicators searchIndicators = indicatorsService.findIndicatorsByCuentaNeocon(indicators.getCuentaNeocon());
            if (searchIndicators==null||id.equals(indicators.getCuentaNeocon()))
            {
                indicators.setCuentaNeocon(searchIndicators.getCuentaNeocon());
                indicatorsService.modifyIndicators(indicators, id,user);
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

   @GetMapping(value = "/parametric/validateIndicators")
    @ResponseBody
    public String validateIndicators(@RequestParam String idNew,@RequestParam String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(indicatorsService.findIndicatorsByCuentaNeocon(idNew)==null||idNew.equals(id))
            result="valid";
        return  result;
    }

    @GetMapping(value="/parametric/addIndicators")
    public ModelAndView showAddIndicators(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Indicators indicators = new Indicators();
        modelAndView.addObject("indicators", indicators);
        modelAndView.setViewName("parametric/addIndicators");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addIndicators")
    public ModelAndView createNewIndicators(@ModelAttribute Indicators indicators) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/indicators");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (indicatorsService.findIndicatorsByCuentaNeocon(indicators.getCuentaNeocon()) == null) {
                indicatorsService.saveIndicators(indicators,user);
                modelAndView.addObject("resp", "Add1");
            } else {
                modelAndView.addObject("resp", "Add0");
            }
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/removeIndicators/{id}")
    @ResponseBody
    public ModelAndView removeIndicators(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Indicators toRemove = indicatorsService.findIndicatorsByCuentaNeocon(id);
        indicatorsService.removeIndicators(toRemove.getCuentaNeocon(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/indicators");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearIndicators")
    public ModelAndView clearIndicators(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        indicatorsService.clearIndicators(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/indicators");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/indicators/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Indicadores_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Indicators> indicatorsList= new ArrayList<Indicators>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            indicatorsList = indicatorsService.findAll();
        }
        else{
            indicatorsList = indicatorsService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        IndicatorsListReport listReport = new IndicatorsListReport(indicatorsList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchIndicators")
    @ResponseBody
    public ModelAndView searchIndicators(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Indicators> list=indicatorsService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Indicators> pageIndicators = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageIndicators.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allIndicators",pageIndicators.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchIndicators");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/indicators");
        return modelAndView;
    }
}
