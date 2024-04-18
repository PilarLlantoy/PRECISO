package com.inter.proyecto_intergrupo.controller.Ifrs9Parametrics;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RejectionIdP1;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.RejectIdP1ListReport;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.RejectIdP1Service;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.CountryService;
import com.inter.proyecto_intergrupo.service.parametricServices.GarantBankListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.GarantBankService;
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
public class RejectIdP1Controller {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private RejectIdP1Service rejectIdP1Service;

    private List<String> listColumns=List.of("Inicial Cuenta", "Asignación", "Tipo Cuenta","Inicial Línea","Cantidad Línea","Inicial Segmento","Cantidad Segmento","Inicial Stage","Cantidad Stage");

    @GetMapping(value="/ifrs/rejectIdP1")
    public ModelAndView showRejectIdP1(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Identificación Cuenta")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<RejectionIdP1> pageRejectIdP1 = rejectIdP1Service.getAll(pageRequest);
            int totalPage = pageRejectIdP1.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allRejectIdP1", pageRejectIdP1.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "rejectIdP1");
            modelAndView.addObject("registers",pageRejectIdP1.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/rejectIdP1");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/rejectIdP1")
    public void uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectIdP1");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<RejectionIdP1> rejectIdP1List = rejectIdP1Service.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            RejectIdP1ListReport listReport = new RejectIdP1ListReport(rejectIdP1List);
            ArrayList<String[]> list=rejectIdP1Service.saveFileBD(fileContent,user);
            String[] part=list.get(0);

            modelAndView.addObject("resp", "AddRep1");
            modelAndView.addObject("row", part[0]);
            modelAndView.addObject("colum", part[1]);
            listReport.exportLog(response,list);
            response.sendRedirect("/ifrs/rejectIdP1");


        }catch(Exception e){
            //return  modelAndView;
        }
        //return  modelAndView;
    }

    @GetMapping(value = "/ifrs/modifyRejectIdP1/{id}")
    @ResponseBody
    public ModelAndView modifyRejectIdP1(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        RejectionIdP1 toModify = rejectIdP1Service.findByInicialCuenta(id);
        modelAndView.addObject("rejectIdP1Modify",toModify);
        modelAndView.addObject("cuentaId",toModify.getInicialCuenta());
        modelAndView.setViewName("ifrs/modifyRejectIdP1");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/modifyRejectIdP1")
    @ResponseBody
    public ModelAndView updateRejectIdP1(@ModelAttribute RejectionIdP1 rejectIdP1,@Param(value = "id") String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectIdP1");
        try {
            RejectionIdP1 searchRejectIdP1 = rejectIdP1Service.findByInicialCuenta(rejectIdP1.getInicialCuenta());
            if (searchRejectIdP1==null||id.equals(rejectIdP1.getInicialCuenta()))
            {
                rejectIdP1Service.modifyRejectIdP1(rejectIdP1, id,user);
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

    @GetMapping(value="/ifrs/addRejectIdP1")
    public ModelAndView showAddRejectIdP1(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        RejectionIdP1 rejectIdP1 = new RejectionIdP1();
        modelAndView.addObject("RejectIdP1", rejectIdP1);
        modelAndView.setViewName("ifrs/addRejectIdP1");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/removeRejectIdP1/{id}")
    @ResponseBody
    public ModelAndView removeRejectIdP1(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        RejectionIdP1 toRemove = rejectIdP1Service.findByInicialCuenta(id);
        rejectIdP1Service.removeRejectionIdP1(toRemove.getInicialCuenta(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectIdP1");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/clearRejectIdP1")
    public ModelAndView clearRejectIdP1(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        rejectIdP1Service.clearRejectionIdP1(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectIdP1");
        return  modelAndView;
    }

    @PostMapping(value = "/ifrs/addRejectIdP1/add")
    public ModelAndView addRejectIdP1(@ModelAttribute RejectionIdP1 rejectIdP1){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/addRejectIdP1");

        boolean result = rejectIdP1Service.insertRejectIdP1(rejectIdP1);

        if(result){
            modelAndView.addObject("resp", "true");
        }else {
            modelAndView.addObject("resp","false");
        }

        return modelAndView;
    }

    @GetMapping(value = "/ifrs/rejectIdP1/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=IdentificaciónCuenta_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<RejectionIdP1> rejectIdP1List= new ArrayList<RejectionIdP1>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            rejectIdP1List = rejectIdP1Service.findAll();
        }
        else{
            rejectIdP1List = rejectIdP1Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        RejectIdP1ListReport listReport = new RejectIdP1ListReport(rejectIdP1List);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/searchRejectIdP1")
    @ResponseBody
    public ModelAndView searchRejectIdP1(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<RejectionIdP1> list=rejectIdP1Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<RejectionIdP1> pageRejectIdP1 = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageRejectIdP1.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allRejectIdP1",pageRejectIdP1.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchRejectIdP1");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/rejectIdP1");
        return modelAndView;
    }
}
