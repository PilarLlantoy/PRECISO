package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Reclassification;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ReclassificationListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ReclassificationService;
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
public class ReclassificationController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ReclassificationService reclassificationService;

    private List<String> listColumns=List.of("NIT Contraparte", "Cuenta Local", "Cuenta Local Reclasificada");

    @GetMapping(value="/parametric/reclassification")
    public ModelAndView showReclassification(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<Reclassification> pageReclassification=reclassificationService.getAll(pageRequest);
        int totalPage=pageReclassification.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allReclassification",pageReclassification.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","reclassification");
        modelAndView.addObject("registers",pageReclassification.getTotalElements());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/reclassification");
        return modelAndView;
    }

    @PostMapping(value="/parametric/reclassification")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reclassification");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Reclassification> reclassificationList = reclassificationService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ReclassificationListReport listReport = new ReclassificationListReport(reclassificationList);
            ArrayList<String[]> list=reclassificationService.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/parametric/modifyReclassification/{id}")
    @ResponseBody
    public ModelAndView modifyReclassification(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Reclassification toModify = reclassificationService.findReclassificationByNit(Long.parseLong(id));
        modelAndView.addObject("reclassificationModify",toModify);
        modelAndView.addObject("nitId",toModify.getNitContraparte());
        modelAndView.setViewName("parametric/modifyReclassification");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyReclassification")
    @ResponseBody
    public ModelAndView updateReclassification(@ModelAttribute Reclassification reclassification,@Param(value = "id") long id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reclassification");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            Reclassification searchReclassification = reclassificationService.findReclassificationByNit(reclassification.getNitContraparte());
            if (searchReclassification==null||id==reclassification.getNitContraparte())
            {
                reclassificationService.modifyReclassification(reclassification, id,user);
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

    @GetMapping(value = "/parametric/validateNitReclassification")
    @ResponseBody
    public String validateNitReclassification(@RequestParam String idNew,@RequestParam String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(reclassificationService.findReclassificationByNit(Long.parseLong(idNew))==null||idNew.equals(id))
            result="valid";
        return  result;
    }

    @GetMapping(value="/parametric/addReclassification")
    public ModelAndView showAddReclassification(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Reclassification reclassification = new Reclassification();
        modelAndView.addObject("reclassification", reclassification);
        modelAndView.setViewName("parametric/addReclassification");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addReclassification")
    public ModelAndView createNewReclassification(@ModelAttribute Reclassification reclassification) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reclassification");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (reclassificationService.findReclassificationByNit(reclassification.getNitContraparte()) == null) {
                reclassificationService.saveReclassification(reclassification,user);
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

    @GetMapping(value = "/parametric/removeReclassification/{id}")
    @ResponseBody
    public ModelAndView removeReclassification(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Reclassification toRemove = reclassificationService.findReclassificationByNit(Long.parseLong(id));
        reclassificationService.removeReclassification(toRemove.getNitContraparte(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reclassification");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearReclassification")
    public ModelAndView clearReclassification(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        reclassificationService.clearReclassification(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reclassification");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/reclassification/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Reclasificación_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Reclassification> reclassificationList= new ArrayList<Reclassification>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            reclassificationList = reclassificationService.findAll();
        }
        else{
            reclassificationList = reclassificationService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ReclassificationListReport listReport = new ReclassificationListReport(reclassificationList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchReclassification")
    @ResponseBody
    public ModelAndView searchReclassification(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Reclassification> list=reclassificationService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Reclassification> pageReclassification = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageReclassification.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allReclassification",pageReclassification.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchReclassification");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/reclassification");
        return modelAndView;
    }
}
