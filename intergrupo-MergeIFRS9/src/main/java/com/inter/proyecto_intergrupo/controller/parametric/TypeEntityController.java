package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.TypeEntity;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.TypeEntityListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.TypeEntityService;
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
public class TypeEntityController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private TypeEntityService typeEntityService;

    private List<String> listColumns=List.of("NIT", "Contraparte", "Tipo Contraparte","Intergrupo","Tipo Entidad","Eliminación");

    @GetMapping(value="/parametric/typeEntity")
    public ModelAndView showTypeEntity(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Tipo de Entidad")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<TypeEntity> pageType = typeEntityService.getAll(pageRequest);
            int totalPage = pageType.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allTypes", pageType.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "typeEntity");

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/typeEntity");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/typeEntity")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/typeEntity");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<TypeEntity> typeEntityList = typeEntityService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            TypeEntityListReport listReport = new TypeEntityListReport(typeEntityList);
            ArrayList<String[]> list=typeEntityService.saveFileBD(fileContent, user);
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
            return  modelAndView;
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyTypeEntity/{id}")
    @ResponseBody
    public ModelAndView modifyTypeEntity(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        TypeEntity toModify = typeEntityService.findTypeEntityById(Long.parseLong(id));
        modelAndView.addObject("typeEntityModify",toModify);
        modelAndView.addObject("id",toModify.getIdTipoEntidad());
        modelAndView.setViewName("parametric/modifyTypeEntity");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyTypeEntity")
    @ResponseBody
    public ModelAndView updateTypeEntity(@ModelAttribute TypeEntity typeEntity,@Param(value = "id") String id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/typeEntity");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            typeEntityService.modifyTypeEntity(typeEntity, id,user);
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value="/parametric/addTypeEntity")
    public ModelAndView showAddTypeEntity(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        TypeEntity typeEntity = new TypeEntity();
        modelAndView.addObject("typeEntity", typeEntity);
        modelAndView.setViewName("parametric/addTypeEntity");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addTypeEntity")
    public ModelAndView createTypeEntity(@ModelAttribute TypeEntity typeEntity) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/typeEntity");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (typeEntityService.findTypeEntityById(typeEntity.getIdTipoEntidad()) == null) {
                typeEntityService.saveTypeEntity(typeEntity, user);
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

    @GetMapping(value = "/parametric/removeTypeEntity/{id}")
    @ResponseBody
    public ModelAndView removeTypeEntity(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        TypeEntity toRemove = typeEntityService.findTypeEntityById(Long.parseLong(id));
        typeEntityService.removeTypeEntity(toRemove.getIdTipoEntidad(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/typeEntity");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearTypeEntity")
    public ModelAndView clearTypeEntity(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        typeEntityService.clearTypeEntity(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/typeEntity");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/typeEntity/download")
    @ResponseBody
    public void exportToExcelTypeEntity(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=TipoEntidad_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<TypeEntity> typeEntityList= new ArrayList<TypeEntity>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            typeEntityList = typeEntityService.findAll();
        }
        else{
            typeEntityList = typeEntityService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        TypeEntityListReport listReport = new TypeEntityListReport(typeEntityList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchTypeEntity")
    @ResponseBody
    public ModelAndView searchTypeEntity(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<TypeEntity> list=typeEntityService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<TypeEntity> pageTypeEntity = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageTypeEntity.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allTypes",pageTypeEntity.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchTypeEntity");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/typeEntity");
        return modelAndView;
    }
}