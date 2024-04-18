package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.AvalTypeService;
import com.inter.proyecto_intergrupo.service.parametricServices.AvalTypesListReport;
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
import javax.validation.Valid;
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
public class AvalTypeController {
    private static final int PAGINATIONCOUNT = 12;
    @Autowired
    private AvalTypeService avalTypeService;
    @Autowired
    private UserService userService;

    List<String> listColumns = List.of("Tipo de aval origen", "Tipo de aval", "Cuenta Contable 13", "Cuenta Contable 60", "Tipo de Archivo", "Contrapartida Genérica");

    @GetMapping(value = "/parametric/avalType")
    public ModelAndView showAvalType(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Tipos de aval")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<AvalTypes> pageAval = avalTypeService.getAll(pageRequest);
            int totalPage = pageAval.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("allAval", pageAval.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "avalType");
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("registers",pageAval.getTotalElements());
            modelAndView.setViewName("parametric/avalType");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/avalType")
    public ModelAndView uploadFileAvalType(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/avalType");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AvalTypes> avalTypesList = avalTypeService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            AvalTypesListReport listReport = new AvalTypesListReport(avalTypesList);
            ArrayList<String[]> list = avalTypeService.saveFileBD(fileContent,user);
            String[] part = list.get(0);
            if (part[2].equals("true")) {
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                listReport.exportLog(response, list);
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
        } finally {
            return modelAndView;
        }
    }

    @GetMapping(value = "/parametric/modifyAvalTypes/{id}")
    @ResponseBody
    public ModelAndView modifyAvalTypes(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String pathList [] = id.split(":");
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        AvalTypes toModify = avalTypeService.findAval(pathList[0],pathList[1]).get(0);
        modelAndView.addObject("avalModify", toModify);
        modelAndView.setViewName("parametric/modifyAvalTypes");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyAvalTypes")
    public ModelAndView updateAvalTypes(@ModelAttribute AvalTypes aval, @Param(value = "id") String id, @Param(value = "cuenta13") String cuenta13) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/avalType");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            AvalTypes avalTypes = avalTypeService.findAval(aval.getAvalOrigen(),aval.getCuentaContable13()).get(0);
            if ( avalTypes==null || id.equals(aval.getAvalOrigen()) && cuenta13.equals(aval.getCuentaContable13())) {
                avalTypeService.modifyAval(aval, id, cuenta13);
                modelAndView.addObject("resp", "Modify1");
            } else {
                modelAndView.addObject("resp", "Modify0");
            }
        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/addAval")
    public ModelAndView showAddAval() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        AvalTypes avalTypes = new AvalTypes();
        modelAndView.addObject("aval", avalTypes);
        modelAndView.setViewName("parametric/addAval");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addAval")
    public ModelAndView createNewAval(@ModelAttribute AvalTypes aval) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/avalType");

        try {
            if (avalTypeService.findAval(aval.getAvalOrigen(), aval.getCuentaContable13()).size() == 0) {
                avalTypeService.saveAval(aval);
                modelAndView.addObject("resp", "Add1");
            } else {
                modelAndView.addObject("resp", "Add0");
            }
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/validateAval")
    @ResponseBody
    public String validateAvalid(@RequestParam String idNew,@RequestParam String id,@RequestParam String id2, @RequestParam String idold){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(avalTypeService.findAval(idNew,id2).size()==0||(idNew.equals(id) && idNew.equals(idold)))
            result="valid";
        return  result;
    }


    @GetMapping(value = "/parametric/removeAval/{id}")
    @ResponseBody
    public boolean removeAval(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/avalType");
        String pathList [] = id.split(":");
        boolean response = false;
        try {
            AvalTypes toRemove = avalTypeService.findAval(pathList[0],pathList[1]).get(0);
            avalTypeService.removeAval(toRemove.getAvalOrigen(),toRemove.getCuentaContable13());
            response = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @GetMapping(value = "/parametric/clearAval")
    @ResponseBody
    public boolean clearAval() {
        boolean response = false;
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/avalType");
        try {
            avalTypeService.clearAval();
            response = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @GetMapping(value = "/parametric/avalType/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=TipoAval_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AvalTypes> avalTypesList;

        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            avalTypesList = avalTypeService.findAll();
        }
        else{
            avalTypesList = avalTypeService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        AvalTypesListReport listReport = new AvalTypesListReport(avalTypesList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchAval")
    @ResponseBody
    public ModelAndView searchAval(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<AvalTypes> list= avalTypeService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<AvalTypes> pageAval = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageAval.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allAval",pageAval.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchAval");
        modelAndView.addObject("registers", pageAval.getTotalElements());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/avalType");
        return modelAndView;
    }
}
