package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Centers;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.CenterListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.CenterService;
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
public class CentersController {

    private static final int PAGINATIONCOUNT = 12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private CenterService centerService;


    private List<String> listColumns = List.of("Oficina", "Tipo unidad", "Nomtip", "Clase unidad", "Nombre unidad", "Dar", "Dug", "Territorio", "Direccion regional", "Area operativa", "Suprarea", "Fecha cierre", "Ofinegocio", "Fecha apertura", "Domicilio", "Telefono");

    @GetMapping(value = "/ifrs/center")
    public ModelAndView showTemplateCenters(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Centros de Oficinas")) {

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<Centers> pageCenter=centerService.getAll(pageRequest);
        int totalPage=pageCenter.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allTemplateCenters",pageCenter.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","center");
        List<Centers> list = centerService.findAll();
        modelAndView.addObject("registers",list.size());

        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/center");
        }
            else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/center")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/center");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logInsercion_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = centerService.saveFileBD(fileContent, headerKey);
            String[] part = list.get(0);
            if (part[2].equals("true")) {
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                //listReport.exportLog(response,list);
            } else if (part[2].equals("falseFormat")) {
                int rowTemp = (Integer.parseInt(part[0]) + 1);
                part[0] = String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep-1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            } else {
                int rowTemp = (Integer.parseInt(part[0]) + 1);
                part[0] = String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep0");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                return modelAndView;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return modelAndView;
        }
        return modelAndView;

    }

    @GetMapping(value = "/ifrs/modifyCenter/{oficina}")
    @ResponseBody
    public ModelAndView modifyCenter(@PathVariable String oficina) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Centers toModify = centerService.findCenterbyOficina(oficina).get(0);
        modelAndView.addObject("centerModify", toModify);
        modelAndView.setViewName("ifrs/modifyCenter");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/modifyCenter")
    @ResponseBody
    public ModelAndView updateCenter(@ModelAttribute Centers center, @Param(value = "oficina") String oficina) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/center");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            Centers searchCenter = centerService.findCenterbyOficina(oficina).get(0);
            if (searchCenter != null) {
                if (validarRegistro(modelAndView, center)) {
                    centerService.modifyCenter(center, oficina);
                    modelAndView.addObject("resp", "Modify1");
                }
                else{
                    modelAndView.addObject("resp", "Modify0");
                }
            } else {
                modelAndView.addObject("resp", "Modify0");
            }
        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;

    }


    @GetMapping(value = "/ifrs/addCenter")
    public ModelAndView showAddCenter() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Centers center = new Centers();
        modelAndView.addObject("center", center);
        modelAndView.setViewName("ifrs/addCenter");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/addCenter")
    public ModelAndView createNewCenter(@ModelAttribute Centers center) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/center");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (centerService.findCenterbyOficina(center.getOficina()).size() == 0) {
                if (validarRegistro(modelAndView, center)) {
                    centerService.saveCenter(center);
                    modelAndView.addObject("resp", "Add1");
                }
            } else {
                modelAndView.addObject("resp", "Add0");
            }
        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/removeCenter/{oficina}")
    @ResponseBody
    public boolean removeCenter(@PathVariable String oficina, @RequestParam Map<String, Object> params) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/center");
        boolean response = false;
        try {
            Centers toRemove = centerService.findCenterbyOficina(oficina).get(0);
            centerService.removeCenter(toRemove.getOficina());
            response = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @GetMapping(value = "/ifrs/clearCenter")
    public ModelAndView clearCenter() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        centerService.clearCenter(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/center");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/center/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CentrosDeOficinas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Centers> centerList = new ArrayList<Centers>();
        if ((params.get("vFilter").toString()).equals("Original") || params.get("vFilter") == null || (params.get("vFilter").toString()).equals("")) {
            centerList = centerService.findAll();
        } else {
            centerList = centerService.findByFilter(params.get("vId").toString(), params.get("vFilter").toString());
        }
        CenterListReport listReport = new CenterListReport(centerList);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/searchCenter")
    @ResponseBody
    public ModelAndView searchCenter(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Centers> list=centerService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Centers> pageSubsidiaries = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageSubsidiaries.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allTemplateCenters",pageSubsidiaries.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchCenter");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/center");
        return modelAndView;
    }

    //TODO incluir qué hay que validar en esta parametria
    private Boolean validarRegistro(ModelAndView modelAndView, Centers center) {
        boolean salida = true;
        return salida;
    }
}