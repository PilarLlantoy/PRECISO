package com.inter.proyecto_intergrupo.controller.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureService;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamMDAListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamMDAService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import javax.servlet.ServletOutputStream;
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
public class ParamMDAController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ParamMDAService paramMDAService;

    private List<String> listColumns=List.of("Fecha", "Divisa", "Moneda");

    @GetMapping(value="/parametric/mda")
    public ModelAndView showParamMDA(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Parametrica Moneda")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            List<ParamMDA> list = paramMDAService.findAll();

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ParamMDA> pageMda= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageMda.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allMda", pageMda.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "mda");
            modelAndView.addObject("registers",pageMda.getTotalElements());
            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/mda");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/mda")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/mda");
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
            ArrayList<String[]> list = paramMDAService.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                SignatureListReport signatureListReport = new SignatureListReport(list,null);
                signatureListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyMda/{id}")
    @ResponseBody
    public ModelAndView modifyMda(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        ParamMDA toModify = paramMDAService.findByIdMda(idTemp);
        modelAndView.addObject("mdaModify",toModify);
        modelAndView.setViewName("parametric/modifyMda");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyMda")
    @ResponseBody
    public ModelAndView updateSMda(@ModelAttribute ParamMDA paramMDA){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/mda");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            paramMDAService.modifyMda(paramMDA, user);
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value="/parametric/addMda")
    public ModelAndView showAddMda(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ParamMDA paramMDA = new ParamMDA();
        modelAndView.addObject("mda", paramMDA);
        modelAndView.setViewName("parametric/addMda");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addMda")
    public ModelAndView createNewMda(@ModelAttribute ParamMDA paramMda) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/mda");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            paramMDAService.saveMda(paramMda, user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/removeMda/{id}")
    @ResponseBody
    public ModelAndView removeMda(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ParamMDA toRemove = paramMDAService.findByIdMda(Long.parseLong(id));
        paramMDAService.removeMda(toRemove.getIdMda(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/mda");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearMda")
    public ModelAndView clearMda(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        paramMDAService.clearMda(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/mda");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/mda/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Moneda_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ParamMDA> mdaList= new ArrayList<ParamMDA>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            mdaList = paramMDAService.findAll();
        }
        else{
            mdaList = paramMDAService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ParamMDAListReport listReport = new ParamMDAListReport(null,mdaList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchMda")
    @ResponseBody
    public ModelAndView searchMda(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ParamMDA> list=paramMDAService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ParamMDA> pageMda = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageMda.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allMda",pageMda.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchMda");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/mda");
        return modelAndView;
    }

}