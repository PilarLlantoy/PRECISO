package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.ComerParametric;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ComerParametricListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ComerParametricService;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdListReport;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ComerParametricController {

    private static final int PAGINATIONCOUNT=12;

    @Autowired
    ComerParametricService comerService;

    @Autowired
    UserService userService;

    private List<String> listColumns=List.of("Cuenta Local", "Clase", "Nombre clase","Doc.compr.","Prorrata de iva","Tipo de importe");

    @GetMapping(value = "/parametric/comerParametric")
    public ModelAndView showComerParametric(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Metodología Comercializadora")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            ArrayList<ComerParametric> list = comerService.getAll();

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ComerParametric> pageComer = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageComer.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allComer", pageComer.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "comerParametric");
            modelAndView.addObject("registers",list.size());

            modelAndView.setViewName("parametric/comerParametric");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/comerParametric")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/comerParametric");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerValue = "attachment; filename= ResultadoComer_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        try{
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = comerService.saveFileBD(fileContent, user);
            ComerParametricListReport comerTemplateListReport = new ComerParametricListReport(list,null);
            comerTemplateListReport.exportLog(response);
        } catch (IOException | ServletException | InvalidFormatException e) {
            e.printStackTrace();
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/comerParametric/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=MétodoComer_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ComerParametric> comerList= new ArrayList<ComerParametric>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            comerList = comerService.getAll();
        }
        else{
            comerList = comerService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ComerParametricListReport listReport = new ComerParametricListReport(null,comerList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchComerMetodo")
    @ResponseBody
    public ModelAndView searchThirds(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ComerParametric> list=comerService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ComerParametric> pageComer = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageComer.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allComer",pageComer.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchComerMetodo");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/comerParametric");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/clearComerParametric")
    public ModelAndView clearThirds(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        comerService.clearTable(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/comerParametric");

        return  modelAndView;
    }
}