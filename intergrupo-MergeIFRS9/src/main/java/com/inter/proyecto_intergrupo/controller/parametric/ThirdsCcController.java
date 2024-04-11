package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.ThirdsCc;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.dataqualityServices.RulesDQListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
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
public class ThirdsCcController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ThirdsCcService thirdsCcService;

    private List<String> listColumns=List.of("NIT", "Nombre", "Impuesto", "Teléfono", "Correo", "Correo Alterno", "Correo Alterno 2","Dirección", "Correo Copia 1", "Correo Copia 2");

    @GetMapping(value="/parametric/thirdsCc")
    public ModelAndView showThirds(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Parametrica Terceros (CC)")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<ThirdsCc> pageThirdsCc = thirdsCcService.getAll(pageRequest);
            int totalPage = pageThirdsCc.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allThirds", pageThirdsCc.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "thirdsCc");
            modelAndView.addObject("registers",pageThirdsCc.getTotalElements());

            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/thirdsCc");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/thirdsCc")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/thirdsCc");
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
            ArrayList<String[]> list = thirdsCcService.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                ThirdsCcListReport thirdsCcListReport = new ThirdsCcListReport(list,null);
                thirdsCcListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyThirdsCc/{id}")
    @ResponseBody
    public ModelAndView modifyThirds(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ThirdsCc toModify = thirdsCcService.findByNit(id);
        modelAndView.addObject("thirdModify",toModify);
        modelAndView.addObject("nitId",toModify.getNit());
        modelAndView.setViewName("parametric/modifyThirdsCc");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyThirdsCc")
    @ResponseBody
    public ModelAndView updateThird(@ModelAttribute ThirdsCc third,@Param(value = "id") String id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/thirdsCc");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            ThirdsCc searchThird = thirdsCcService.findByNit(third.getNit());
            if (searchThird==null||id.equals(third.getNit()))
            {
                thirdsCcService.modifyThird(third, id, user);
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

    @GetMapping(value = "/parametric/validateNitThirdsCc")
    @ResponseBody
    public String validateNitThirds(@RequestParam String idNew,@RequestParam String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(thirdsCcService.findByNit(idNew)==null||idNew.equals(id))
            result="valid";
        return  result;
    }

    @GetMapping(value="/parametric/addThirdsCc")
    public ModelAndView showAddThird(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ThirdsCc third = new ThirdsCc();
        modelAndView.addObject("third", third);
        modelAndView.setViewName("parametric/addThirdsCc");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addThirdsCc")
    public ModelAndView createNewUser(@ModelAttribute ThirdsCc third) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/thirdsCc");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (thirdsCcService.findByNit(third.getNit()) == null) {
                thirdsCcService.saveThird(third, user);
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

    @GetMapping(value = "/parametric/removeThirdsCc/{id}")
    @ResponseBody
    public ModelAndView removeThirds(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ThirdsCc toRemove = thirdsCcService.findByNit(id);
        thirdsCcService.removeThird(toRemove.getNit(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/thirdsCc");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearThirdsCc")
    public ModelAndView clearThirds(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        thirdsCcService.clearThird(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/thirdsCc");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/thirdsCc/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=TercerosCC_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ThirdsCc> thirdList= new ArrayList<ThirdsCc>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            thirdList = thirdsCcService.findAll();
        }
        else{
            thirdList = thirdsCcService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ThirdsCcListReport listReport = new ThirdsCcListReport(null,thirdList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchThirdsCc")
    @ResponseBody
    public ModelAndView searchThirds(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ThirdsCc> list=thirdsCcService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ThirdsCc> pageThird = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageThird.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allThirds",pageThird.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchThirdsCc");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/thirdsCc");
        return modelAndView;
    }
}