package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountAndByProduct;
import com.inter.proyecto_intergrupo.model.parametric.Ciiu;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.CiiuService;
import com.inter.proyecto_intergrupo.service.parametricServices.CiiuListReport;
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
public class CiiuController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private CiiuService ciiuService;


    private List<String> listColumns=List.of("Ciiu", "Descripcion");

    @GetMapping(value="/parametric/ciiu")
    public ModelAndView showCiiu(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver CIIU")){

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<Ciiu> pageCiiu=ciiuService.getAll(pageRequest);
        int totalPage=pageCiiu.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allCiiu",pageCiiu.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","ciiu");
        List<Ciiu> list = ciiuService.findAll();
        modelAndView.addObject("registers",list.size());

        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/ciiu");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/ciiu")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/ciiu");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Ciiu> ciiuList = ciiuService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            CiiuListReport listReport = new CiiuListReport(ciiuList);
            ArrayList<String[]> list=ciiuService.saveFileBD(fileContent,user);
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
        }
        finally {
            return  modelAndView;
        }
    }

    @GetMapping(value = "/parametric/modifyCiiu/{id}")
    @ResponseBody
    public ModelAndView modifyCiiu(@PathVariable Integer id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Ciiu toModify = ciiuService.findCiiubyId(id).get(0);
        modelAndView.addObject("ciiuModify",toModify);
        modelAndView.setViewName("parametric/modifyCiiu");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyCiiu")
    @ResponseBody
    public ModelAndView updateCiiu(@ModelAttribute Ciiu ciiu,@Param(value = "id") Integer id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/ciiu");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            Ciiu searchCiiu = ciiuService.findCiiubyId(id).get(0);
            if (searchCiiu!=null)
            {
                ciiuService.modifyCiiu(ciiu, id);
                modelAndView.addObject("resp", "Modify1");
            }
            else
            {
                modelAndView.addObject("resp", "Modify0");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }


    @GetMapping(value="/parametric/addCiiu")
    public ModelAndView showAddCiiu(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Ciiu ciiu = new Ciiu();
        modelAndView.addObject("ciiu", ciiu);
        modelAndView.setViewName("parametric/addCiiu");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addCiiu")
    public ModelAndView createNewCiiu(@ModelAttribute Ciiu ciiu) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/ciiu");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (ciiuService.findCiiubyId(ciiu.getId()).size() == 0) {
                ciiuService.saveCiiu(ciiu);
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

    @GetMapping(value = "/parametric/removeCiiu/{id}")
    @ResponseBody
    public boolean removeCiiu(@PathVariable Integer id,@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/ciiu");
        boolean response=false;
        try {
            Ciiu toRemove = ciiuService.findCiiubyId(id).get(0);
            ciiuService.removeCiiu(toRemove.getId());
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/clearCiiu")
    public ModelAndView clearCiiu(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ciiuService.clearCiiu(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/ciiu");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/ciiu/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CIIU_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Ciiu> ciiuList;
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            ciiuList = ciiuService.findAll();
        }
        else{
            ciiuList = ciiuService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        CiiuListReport listReport = new CiiuListReport(ciiuList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchCiiu")
    @ResponseBody
    public ModelAndView searchCiiu(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Ciiu> list=ciiuService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Ciiu> pageCiiu = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageCiiu.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allCiiu",pageCiiu.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchCiiu");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/ciiu");
        return modelAndView;
    }
}