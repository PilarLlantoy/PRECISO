package com.inter.proyecto_intergrupo.controller.Ifrs9Parametrics;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.Equivalences;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.EquivalencesService;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.EquivalencesListReport;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.EquivalencesService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
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
public class EquivalencesController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private EquivalencesService equivalencesService;

    private List<String> listColumns=List.of("Cuenta Contable", "Contrapartida");

    @GetMapping(value="/ifrs/equivalences")
    public ModelAndView showRejectIdP1(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Equivalencias")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<Equivalences> pageEquivalences = equivalencesService.getAll(pageRequest);
            int totalPage = pageEquivalences.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allEquivalences", pageEquivalences.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "equivalences");
            modelAndView.addObject("registers",pageEquivalences.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/equivalences");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/equivalences")
    public void uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/equivalences");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Equivalences> equivalencesList = equivalencesService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            EquivalencesListReport listReport = new EquivalencesListReport(equivalencesList);
            ArrayList<String[]> list=equivalencesService.saveFileBD(fileContent,user);
            String[] part=list.get(0);

            modelAndView.addObject("resp", "AddRep1");
            modelAndView.addObject("row", part[0]);
            modelAndView.addObject("colum", part[1]);
            listReport.exportLog(response,list);
            response.sendRedirect("/ifrs/equivalences");


        }catch(Exception e){
            //return  modelAndView;
        }
        //return  modelAndView;
    }

    @GetMapping(value = "/ifrs/modifyEquivalences/{id}")
    @ResponseBody
    public ModelAndView modifyRejectIdP1(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Equivalences toModify = equivalencesService.findByCuentaContable(id);
        modelAndView.addObject("equivalencesModify",toModify);
        modelAndView.addObject("cuentaId",toModify.getCuentaContable());
        modelAndView.setViewName("ifrs/modifyEquivalences");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/modifyEquivalences")
    @ResponseBody
    public ModelAndView updateRejectIdP1(@ModelAttribute Equivalences equivalences,@Param(value = "id") String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/equivalences");
        try {
            Equivalences searchRejectIdP1 = equivalencesService.findByCuentaContable(equivalences.getCuentaContable());
            if (searchRejectIdP1==null||id.equals(equivalences.getCuentaContable()))
            {
                equivalencesService.modifyEquivalences(equivalences, id,user);
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

    @GetMapping(value="/ifrs/addEquivalences")
    public ModelAndView showAddEquivalences(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Equivalences equivalences = new Equivalences();
        modelAndView.addObject("Equivalences", equivalences);
        modelAndView.setViewName("ifrs/addEquivalences");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/removeEquivalences/{id}")
    @ResponseBody
    public ModelAndView removeRejectIdP1(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Equivalences toRemove = equivalencesService.findByCuentaContable(id);
        equivalencesService.removeEquivalences(toRemove.getCuentaContable(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/equivalences");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/clearEquivalences")
    public ModelAndView clearEquivalences(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        equivalencesService.clearEquivalences(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/equivalences");
        return  modelAndView;
    }

    @PostMapping(value = "/ifrs/addEquivalences/add")
    public ModelAndView addEquivalences(@ModelAttribute Equivalences equivalences){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/addEquivalences");

        boolean result = equivalencesService.insertEquivalences(equivalences);

        if(result){
            modelAndView.addObject("resp", "true");
        }else {
            modelAndView.addObject("resp","false");
        }

        return modelAndView;
    }

    @GetMapping(value = "/ifrs/equivalences/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Equivalencias_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Equivalences> equivalencesList= new ArrayList<Equivalences>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            equivalencesList = equivalencesService.findAll();
        }
        else{
            equivalencesList = equivalencesService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        EquivalencesListReport listReport = new EquivalencesListReport(equivalencesList);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/searchEquivalences")
    @ResponseBody
    public ModelAndView searchEquivalences(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Equivalences> list=equivalencesService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Equivalences> pageEquivalences = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageEquivalences.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allEquivalences",pageEquivalences.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchEquivalences");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/equivalences");
        return modelAndView;
    }
}
