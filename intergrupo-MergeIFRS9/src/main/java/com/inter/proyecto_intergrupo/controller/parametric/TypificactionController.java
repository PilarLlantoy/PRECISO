package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Typification;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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
public class TypificactionController {
    private static final int PAGINATIONCOUNT=12;
    private List<String> listColumns=List.of("Nombre", "Aplica Conciliación", "Estado");
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private TypificationService typificationService;

    @Autowired
    private ConciliationService conciliationService;

    @GetMapping(value="/parametric/typification")
    public ModelAndView showCountry(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Tipificaciones");
        if(userService.validateEndpoint(user.getId(),"Ver Tipificaciones")) {

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<Typification> data = typificationService.findAll();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), data.size());
            Page<Typification> pageData = new PageImpl<>(data.subList(start, end), pageRequest, data.size());

            int totalPage=pageData.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }

            List<Conciliation> conciliations = conciliationService.findAllActive();
            modelAndView.addObject("conciliaciones",conciliations);
            modelAndView.addObject("allData",pageData.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("columns",listColumns);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","typification");
            modelAndView.addObject("registers",data.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/typification");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/typification")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/typification");
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
            ArrayList<String[]> list = typificationService.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                GeneralListReport generalListReport = new GeneralListReport(list);
                generalListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyTypification/{id}")
    @ResponseBody
    public ModelAndView modifyTypification(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Typification toModify = typificationService.findById(Long.parseLong(id));
        modelAndView.addObject("dataModify",toModify);
        modelAndView.addObject("dataId",toModify.getId());
        modelAndView.setViewName("parametric/modifyTypification");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyTypification")
    public ModelAndView updateTypification(@ModelAttribute Typification typification){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/typification");
        typificationService.modificarTypification(typification);
        modelAndView.addObject("resp", "Modify1");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/deleteTypification/{id}")
    public ModelAndView deleteTypification(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/typification");
        try {
            Typification typification = typificationService.findById(Long.parseLong(id));
            typification.setEstado(false);
            typificationService.modificarTypification(typification);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }


    @GetMapping(value = "/parametric/createTypification")
    public ModelAndView showCreateTypification(){
        ModelAndView modelAndView = new ModelAndView();
        Typification typification = new Typification();
        modelAndView.addObject("data",typification);
        modelAndView.setViewName("/parametric/createTypification");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createTypification")
    public ModelAndView createCountry(@ModelAttribute Typification typification, BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/typification");
        Typification typificationSearch = typificationService.findById(typification.getId());
        if(typificationSearch != null){
            bindingResult
                    .rejectValue("pais", "error.pais",
                            "La Tipificación ya se ha registrado");
        }
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createTypification");
        }else{
            modelAndView.addObject("resp", "Add1");
            typificationService.modificarTypification(typification);
        }
        return modelAndView;

    }

    @GetMapping(value = "/parametric/searchTypification")
    @ResponseBody
    public ModelAndView searchTypification(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Typification> list;
        if(params==null)
            list=typificationService.findAll();
        else
            list=typificationService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Typification> pageTypeEntity = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageTypeEntity.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allData",pageTypeEntity.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchTypification");
        modelAndView.addObject("registers",list.size());
        modelAndView.addObject("filterExport","Filtrado");
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Tipificaciones");

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("p_modificar", p_modificar);

        modelAndView.setViewName("parametric/typification");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/typification/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Tipificaciones_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Typification> list= new ArrayList<Typification>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            list = typificationService.findAll();
        }
        else{
            list = typificationService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        TypificationListReport listReport = new TypificationListReport(list);
        listReport.export(response);
    }
}
