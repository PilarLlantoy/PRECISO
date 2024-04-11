package com.inter.proyecto_intergrupo.controller.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamFechas;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamFechasListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamFechasService;
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
public class ParamFechasController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ParamFechasService paramFechasService;

    private List<String> listColumns=List.of("Año", "Mes", "Balance", "PYG", "Q Aplica");

    @GetMapping(value="/parametric/fechas")
    public ModelAndView showParamFechas(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Parametrica Fechas")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            List<ParamFechas> list = paramFechasService.findAllOrder();

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<ParamFechas> pageFechas= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageFechas.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allFechas", pageFechas.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "fechas");
            modelAndView.addObject("registers",pageFechas.getTotalElements());
            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/fechas");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/fechas")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fechas");
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
            ArrayList<String[]> list = paramFechasService.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/parametric/modifyFechas/{id}")
    @ResponseBody
    public ModelAndView modifyFecha(@PathVariable String id){
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
        ParamFechas toModify = paramFechasService.findByIdFecha(idTemp);
        modelAndView.addObject("fechasModify",toModify);
        modelAndView.setViewName("parametric/modifyFechas");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyFechas")
    @ResponseBody
    public ModelAndView updateFecha(@ModelAttribute ParamFechas paramFechas){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fechas");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            paramFechasService.modifyFecha(paramFechas, user);
            paramFechasService.validateStatus("CARGADO");
            paramFechasService.validateStatus("PENDIENTE");
            paramFechasService.validateStatusConsol("CARGADO");
            paramFechasService.validateStatusConsol("PENDIENTE");
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value="/parametric/addFechas")
    public ModelAndView showAddFechas(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ParamFechas paramFechas = new ParamFechas();
        modelAndView.addObject("fechas", paramFechas);
        modelAndView.setViewName("parametric/addFechas");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addFechas")
    public ModelAndView createNewFecha(@ModelAttribute ParamFechas paramFechas) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fechas");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            paramFechasService.saveFecha(paramFechas, user);
            paramFechasService.validateStatus("CARGADO");
            paramFechasService.validateStatus("PENDIENTE");
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/removeFechas/{id}")
    @ResponseBody
    public ModelAndView removeFechas(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ParamFechas toRemove = paramFechasService.findByIdFecha(Long.parseLong(id));
        paramFechasService.removeFecha(toRemove.getIdFecha(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fechas");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearFechas")
    public ModelAndView clearFecha(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        paramFechasService.clearFecha(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fechas");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/fechas/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Fechas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ParamFechas> fechaList= new ArrayList<ParamFechas>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            fechaList = paramFechasService.findAllOrder();
        }
        else{
            fechaList = paramFechasService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ParamFechasListReport listReport = new ParamFechasListReport(null,fechaList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchFechas")
    @ResponseBody
    public ModelAndView searchFecha(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ParamFechas> list=paramFechasService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ParamFechas> pageMda = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageMda.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allFechas",pageMda.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchFechas");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/fechas");
        return modelAndView;
    }

}