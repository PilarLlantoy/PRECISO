package com.inter.proyecto_intergrupo.controller.Ifrs9Parametrics;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RejectionIdP2;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.RejectIdP1ListReport;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.RejectIdP1Service;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.RejectIdP2ListReport;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.RejectIdP2Service;
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
public class RejectIdP2Controller {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private RejectIdP2Service rejectIdP2Service;

    private List<String> listColumns=List.of("Línea Producto", "Segmentos");

    @GetMapping(value="/ifrs/rejectIdP2")
    public ModelAndView showRejectIdP1(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Identificación Rechazos")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<RejectionIdP2> pageRejectIdP2 = rejectIdP2Service.getAll(pageRequest);
            int totalPage = pageRejectIdP2.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allRejectIdP2", pageRejectIdP2.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "rejectIdP2");
            modelAndView.addObject("registers",pageRejectIdP2.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/rejectIdP2");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/rejectIdP2")
    public void uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectIdP2");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<RejectionIdP2> rejectIdP2List = rejectIdP2Service.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            RejectIdP2ListReport listReport = new RejectIdP2ListReport(rejectIdP2List);
            ArrayList<String[]> list=rejectIdP2Service.saveFileBD(fileContent,user);
            String[] part=list.get(0);

            modelAndView.addObject("resp", "AddRep1");
            modelAndView.addObject("row", part[0]);
            modelAndView.addObject("colum", part[1]);
            listReport.exportLog(response,list);
            response.sendRedirect("/ifrs/rejectIdP2");


        }catch(Exception e){
            //return  modelAndView;
        }
        //return  modelAndView;
    }

    @GetMapping(value = "/ifrs/modifyRejectIdP2/{id}")
    @ResponseBody
    public ModelAndView modifyRejectIdP1(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        RejectionIdP2 toModify = rejectIdP2Service.findByLineaProducto(id);
        modelAndView.addObject("rejectIdP2Modify",toModify);
        modelAndView.addObject("cuentaId",toModify.getLineaProducto());
        modelAndView.setViewName("ifrs/modifyRejectIdP2");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/modifyRejectIdP2")
    @ResponseBody
    public ModelAndView updateRejectIdP1(@ModelAttribute RejectionIdP2 rejectIdP2,@Param(value = "id") String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectIdP2");
        try {
            rejectIdP2.setLineaProducto(String.format("%03d", Integer.parseInt(rejectIdP2.getLineaProducto())));
            RejectionIdP2 searchRejectIdP1 = rejectIdP2Service.findByLineaProducto(rejectIdP2.getLineaProducto());
            if (searchRejectIdP1==null||id.equals(rejectIdP2.getLineaProducto()))
            {
                rejectIdP2Service.modifyRejectIdP1(rejectIdP2, id,user);
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

    @GetMapping(value="/ifrs/addRejectIdP2")
    public ModelAndView showAddRejectIdP2(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        RejectionIdP2 rejectIdP2 = new RejectionIdP2();
        modelAndView.addObject("RejectIdP2", rejectIdP2);
        modelAndView.setViewName("ifrs/addRejectIdP2");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/removeRejectIdP2/{id}")
    @ResponseBody
    public ModelAndView removeRejectIdP1(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        RejectionIdP2 toRemove = rejectIdP2Service.findByLineaProducto(id);
        rejectIdP2Service.removeRejectionIdP2(toRemove.getLineaProducto(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectIdP2");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/clearRejectIdP2")
    public ModelAndView clearRejectIdP2(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        rejectIdP2Service.clearRejectionIdP2(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rejectIdP2");
        return  modelAndView;
    }

    @PostMapping(value = "/ifrs/addRejectIdP2/add")
    public ModelAndView addRejectIdP2(@ModelAttribute RejectionIdP2 rejectIdP2){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/addRejectIdP2");

        boolean result = rejectIdP2Service.insertRejectIdP2(rejectIdP2);

        if(result){
            modelAndView.addObject("resp", "true");
        }else {
            modelAndView.addObject("resp","false");
        }

        return modelAndView;
    }

    @GetMapping(value = "/ifrs/rejectIdP2/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=IdentificaciónRechazos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<RejectionIdP2> rejectIdP2List= new ArrayList<RejectionIdP2>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            rejectIdP2List = rejectIdP2Service.findAll();
        }
        else{
            rejectIdP2List = rejectIdP2Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        RejectIdP2ListReport listReport = new RejectIdP2ListReport(rejectIdP2List);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/searchRejectIdP2")
    @ResponseBody
    public ModelAndView searchRejectIdP2(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<RejectionIdP2> list=rejectIdP2Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<RejectionIdP2> pageRejectIdP2 = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageRejectIdP2.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allRejectIdP2",pageRejectIdP2.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchRejectIdP2");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/rejectIdP2");
        return modelAndView;
    }
}
