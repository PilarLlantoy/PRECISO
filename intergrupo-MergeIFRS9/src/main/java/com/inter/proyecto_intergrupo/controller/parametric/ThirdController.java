package com.inter.proyecto_intergrupo.controller.parametric;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdService;
import com.inter.proyecto_intergrupo.service.parametricServices.YntpSocietyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ThirdController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ThirdService thirdService;


    @Autowired
    private YntpSocietyService yntpSocietyService;

    private List<String> listColumns=List.of("NIT", "Contraparte", "Código Cliente","Tipo","DV","YNTP","Marca Tipo de Institución");

    @GetMapping(value="/parametric/third")
    public ModelAndView showThirds(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Histórico de terceros")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<Third> pageThird = thirdService.getAll(pageRequest);
            int totalPage = pageThird.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allThirds", pageThird.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "third");
            modelAndView.addObject("registers",pageThird.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/third");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/third")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/third");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Third> thirdList = thirdService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ThirdListReport listReport = new ThirdListReport(thirdList);
            ArrayList<String[]> list=thirdService.saveFileBD(fileContent, user);
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

    @GetMapping(value = "/parametric/modifyThirds/{id}")
    @ResponseBody
    public ModelAndView modifyThirds(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Third toModify = thirdService.findThirdByNit(id);
        modelAndView.addObject("yntpId",toModify.getYntp());
        modelAndView.addObject("thirdModify",toModify);
        modelAndView.addObject("nitId",toModify.getNit());
        modelAndView.setViewName("parametric/modifyThirds");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyThirds")
    @ResponseBody
    public ModelAndView updateThird(@ModelAttribute Third third,@Param(value = "id") String id,@RequestParam String yntpNew){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/third");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            Third searchThird = thirdService.findThirdByNit(third.getNit());
            if (searchThird==null||id.equals(third.getNit()))
            {
                thirdService.modifyThird(third, id,yntpNew, user);
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

    @GetMapping(value = "/parametric/validateNit")
    @ResponseBody
    public String validateNitThirds(@RequestParam String idNew,@RequestParam String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(thirdService.findThirdByNit(idNew)==null||idNew.equals(id))
            result="valid";
        return  result;
    }

    @GetMapping(value = "/parametric/validateYntpThird")
    @ResponseBody
    public String validateYntpThirds(@RequestParam String idNew,@RequestParam String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(yntpSocietyService.findYntpByYntp(idNew)==null||idNew.equals(id))
            result="valid";
        return  result;
    }

    @GetMapping(value="/parametric/addThird")
    public ModelAndView showAddThird(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Third third = new Third();
        modelAndView.addObject("third", third);
        modelAndView.setViewName("parametric/addThird");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addThird")
    public ModelAndView createNewUser(@ModelAttribute Third third,@RequestParam String yntpNew) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/third");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (thirdService.findThirdByNit(third.getNit()) == null) {
                thirdService.saveThird(third,yntpNew, user);
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

    @GetMapping(value = "/parametric/removeThirds/{id}")
    @ResponseBody
    public ModelAndView removeThirds(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Third toRemove = thirdService.findThirdByNit(id);
        thirdService.removeThird(toRemove.getNit(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/third");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearThird")
    public ModelAndView clearThirds(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        thirdService.clearThird(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/third");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/third/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Terceros_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Third> thirdList= new ArrayList<Third>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            thirdList = thirdService.findAll();
        }
        else{
            thirdList = thirdService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ThirdListReport listReport = new ThirdListReport(thirdList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchThird")
    @ResponseBody
    public ModelAndView searchThirds(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Third> list=thirdService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Third> pageThird = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

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
        modelAndView.addObject("directory","searchThird");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/third");
        return modelAndView;
    }
}