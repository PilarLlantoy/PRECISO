package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Provisions;
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
public class ProvisionsController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ProvisionsService provisionsService;

    private List<String> listColumns=List.of("Cuenta Neocon", "Instrumento", "Jerarquía","Descripción","Mínimo","Perímetro IFRS9","Stage España","Producto España","Sector","Signo");

    @GetMapping(value="/parametric/provisions")
    public ModelAndView showProvisions(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Producto y Provisión")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<Provisions> pageProvisions = provisionsService.getAll(pageRequest);
            int totalPage = pageProvisions.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allProvisions", pageProvisions.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "provisions");
            modelAndView.addObject("registers",pageProvisions.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/provisions");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/provisions")
    public ModelAndView uploadFileProvisions(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/provisions");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Provisions> provisionsList = provisionsService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ProvisionsListReport listReport = new ProvisionsListReport(provisionsList);
            ArrayList<String[]> list=provisionsService.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/parametric/modifyProvisions/{id}")
    @ResponseBody
    public ModelAndView modifyProvisions(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Provisions toModify = provisionsService.findProvisionsByCuentaNeocon(id);
        modelAndView.addObject("provisionsModify",toModify);
        modelAndView.addObject("cuentaId",toModify.getCuentaNeocon());
        modelAndView.setViewName("parametric/modifyProvisions");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyProvisions")
    @ResponseBody
    public ModelAndView updateProvisions(@ModelAttribute Provisions provisions,@Param(value = "id") String id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/provisions");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        try {
            Provisions searchProvisions = provisionsService.findProvisionsByCuentaNeocon(provisions.getCuentaNeocon());
            if (searchProvisions!=null)
            {
                provisionsService.modifyProvisions(provisions, id,user);
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

    @GetMapping(value = "/parametric/validateNitProvisions")
    @ResponseBody
    public String validateNitProvisions(@RequestParam String idNew,@RequestParam String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(provisionsService.findProvisionsByCuentaNeocon(idNew)==null||idNew.equals(id))
            result="valid";
        return  result;
    }

    @GetMapping(value="/parametric/addProvisions")
    public ModelAndView showAddProvisions(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Provisions provisions = new Provisions();
        modelAndView.addObject("provisions", provisions);
        modelAndView.setViewName("parametric/addProvisions");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addProvisions")
    public ModelAndView createNewProvisions(@ModelAttribute Provisions provisions) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/provisions");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        try {
            if (provisionsService.findProvisionsByCuentaNeocon(provisions.getCuentaNeocon()) == null) {
                provisionsService.saveProvisions(provisions,user);
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

    @GetMapping(value = "/parametric/removeProvisions/{id}")
    @ResponseBody
    public ModelAndView removeProvisions(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Provisions toRemove = provisionsService.findProvisionsByCuentaNeocon(id);
        User user = userService.findUserByUserName(auth.getName());
        provisionsService.removeProvisions(toRemove.getCuentaNeocon(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/provisions");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearProvisions")
    public ModelAndView clearProvisions(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        provisionsService.clearProvisions(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/provisions");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/provisions/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Provisiones_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Provisions> provisionsList= new ArrayList<Provisions>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            provisionsList = provisionsService.findAll();
        }
        else{
            provisionsList = provisionsService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ProvisionsListReport listReport = new ProvisionsListReport(provisionsList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchProvisions")
    @ResponseBody
    public ModelAndView searchProvisions(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Provisions> list=provisionsService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Provisions> pageProvisions = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageProvisions.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allProvisions",pageProvisions.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchProvisions");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/provisions");
        return modelAndView;
    }
}
