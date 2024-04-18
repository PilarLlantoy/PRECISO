package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AvalTypes;
import com.inter.proyecto_intergrupo.model.parametric.ReclassificationIntergroup;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.AvalTypeService;
import com.inter.proyecto_intergrupo.service.parametricServices.ReclassificationV2ListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ReclassificationV2Service;
import com.inter.proyecto_intergrupo.service.reportsServices.ReconciliationService;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
public class ReclassificationV2Controller {
    private static final int PAGINATIONCOUNT = 12;
    @Autowired
    private ReclassificationV2Service reclassificationV2Service;

    @Autowired
    private UserService userService;

    @PersistenceContext
    EntityManager entityManager;


    List<String> listColumns = List.of("Concepto", "Código Consolidación", "Tipo Sociedad", "Segmento", "Producto", "Tipo", "Stage", "Cuenta", "Cuenta Contrapartida");



    @GetMapping(value = "/parametric/reclassificationV2")
    public ModelAndView showRec(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Reclasificación Intergrupo V2")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<ReclassificationIntergroup> pageRec = reclassificationV2Service.getAll(pageRequest);
            int totalPage = pageRec.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("recon", pageRec.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "reclassificationV2");
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("registers",pageRec.getTotalElements());

            modelAndView.setViewName("parametric/reclassificationIntergroup");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/reclassificationV2")
    public ModelAndView uploadFileRecla(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reclassificationV2");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ReclassificationIntergroup> recList = reclassificationV2Service.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ReclassificationV2ListReport listReport = new ReclassificationV2ListReport(recList);
            ArrayList<String[]> list = reclassificationV2Service.saveFileBD(fileContent,user);
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
            } else if (part[2].equals("falseAccount")) {
                int rowTemp = (Integer.parseInt(part[0]) + 1);
                part[0] = String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep-2");
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

    @GetMapping(value = "/parametric/modifyRec/{id}")
    @ResponseBody
    public ModelAndView modifyRec(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String pathList [] = id.split(":");
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        ReclassificationIntergroup toModify = reclassificationV2Service.findRec(id).get(0);
        modelAndView.addObject("recModify", toModify);
        modelAndView.setViewName("parametric/modifyReclassificationV2");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyRec")
    public ModelAndView updateAvalTypes(@ModelAttribute ReclassificationIntergroup rec, @Param(value = "id") String id) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reclassificationV2");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            ReclassificationIntergroup recla = reclassificationV2Service.findRec(id).get(0);
            if (recla!=null) {
                Query query1 = entityManager.createNativeQuery("select * from cuentas_puc where NUCTA = ? and CODICONS46 = ? and EMPRESA = '0013'");
                query1.setParameter(1, rec.getCuenta());
                query1.setParameter(2, rec.getCodicons());

                if(!query1.getResultList().isEmpty()) {
                    reclassificationV2Service.modifyRec(rec, id);
                    modelAndView.addObject("resp", "Modify1");
                }else{
                    modelAndView.addObject("resp", "AddRecla-1");
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

    @GetMapping(value = "/parametric/removeRec/{id}")
    @ResponseBody
    public boolean removeRec(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean response = false;
        try {
            ReclassificationIntergroup toRemove = reclassificationV2Service.findRec(id).get(0);
            reclassificationV2Service.removeRec(toRemove.getId().toString());
            response = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @GetMapping(value = "/parametric/addRec")
    public ModelAndView showAddRec() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        ReclassificationIntergroup rec = new ReclassificationIntergroup();
        modelAndView.addObject("rec", rec);
        modelAndView.setViewName("parametric/addReconciliationV2");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addRec")
    public ModelAndView createNewRec(@ModelAttribute ReclassificationIntergroup rec) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reclassificationV2");

        try {
                Query query1 = entityManager.createNativeQuery("select * from cuentas_puc where NUCTA = ? and CODICONS46 = ? and EMPRESA = '0013'");
                query1.setParameter(1, rec.getCuenta());
                query1.setParameter(2, rec.getCodicons());

                if(!query1.getResultList().isEmpty()) {
                    reclassificationV2Service.saveRecla(rec);

                    modelAndView.addObject("resp", "Add1");
                }else{
                    modelAndView.addObject("resp", "AddRecla-1");
                }
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/reclassificationV2/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Reclasificación_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ReclassificationIntergroup> rec;

        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            rec = reclassificationV2Service.findAll();
        }
        else{
            rec = reclassificationV2Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ReclassificationV2ListReport listReport = new ReclassificationV2ListReport(rec);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchRec")
    @ResponseBody
    public ModelAndView searchRec(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ReclassificationIntergroup> list= reclassificationV2Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ReclassificationIntergroup> pageRec = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageRec.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("recon",pageRec.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","reclassificationV2");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/reclassificationIntergroup");
        return modelAndView;
    }
}
