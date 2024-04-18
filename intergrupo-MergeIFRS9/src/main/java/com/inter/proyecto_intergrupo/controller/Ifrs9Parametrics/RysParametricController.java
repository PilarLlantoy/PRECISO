package com.inter.proyecto_intergrupo.controller.Ifrs9Parametrics;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RysParametric;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.RysParametricListReport;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.RysParametricService;
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
public class RysParametricController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private RysParametricService rysParametricService;

    private List<String> listColumns=List.of("Código","Código Nombre","Cuenta","Cuenta PYG","Cuenta Neocon","Cuenta Neocon PYG");

    @GetMapping(value="/ifrs/rysParametric")
    public ModelAndView showRysParametric(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Parametrica RYS")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<RysParametric> pageRysParametric = rysParametricService.getAll(pageRequest);
            int totalPage = pageRysParametric.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allRysParametric", pageRysParametric.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "rysParametric");
            modelAndView.addObject("registers",pageRysParametric.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/rysParametric");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/rysParametric")
    public void uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rysParametric");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<RysParametric> rysParametricList = rysParametricService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            RysParametricListReport listReport = new RysParametricListReport(rysParametricList);
            ArrayList<String[]> list=rysParametricService.saveFileBD(fileContent,user);
            String[] part=list.get(0);

            modelAndView.addObject("resp", "AddRep1");
            modelAndView.addObject("row", part[0]);
            modelAndView.addObject("colum", part[1]);
            listReport.exportLog(response,list);
            response.sendRedirect("/ifrs/rysParametric");


        }catch(Exception e){
            //return  modelAndView;
        }
        //return  modelAndView;
    }

    @GetMapping(value = "/ifrs/modifyRysParametric/{id}")
    @ResponseBody
    public ModelAndView modifyRysParametric(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        RysParametric toModify = rysParametricService.findByIdRys(Long.parseLong(id));
        modelAndView.addObject("rysParametricModify",toModify);
        modelAndView.addObject("cuentaId",toModify.getCuenta());
        modelAndView.setViewName("ifrs/modifyRysParametric");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/modifyRysParametric")
    @ResponseBody
    public ModelAndView updateRysParametric(@ModelAttribute RysParametric rysParametric,@Param(value = "id") String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rysParametric");
        try {
            RysParametric searchRysParametric = rysParametricService.findByIdRys(rysParametric.getIdRys());
            if (searchRysParametric==null||id.equals(rysParametric.getIdRys()))
            {
                rysParametricService.modifyRysParametric(rysParametric, Long.parseLong(id),user);
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

    @GetMapping(value="/ifrs/addRysParametric")
    public ModelAndView showAddRysParametric(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        RysParametric rysParametric = new RysParametric();
        modelAndView.addObject("RysParametric", rysParametric);
        modelAndView.setViewName("ifrs/addRysParametric");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/removeRysParametric/{id}")
    @ResponseBody
    public ModelAndView removeRysParametric(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        RysParametric toRemove = rysParametricService.findByIdRys(Long.parseLong(id));
        rysParametricService.removeRysParametric(toRemove.getIdRys(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rysParametric");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/clearRysParametric")
    public ModelAndView clearRysParametric(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        rysParametricService.clearRysParametric(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/rysParametric");
        return  modelAndView;
    }

    @PostMapping(value = "/ifrs/addRysParametric/add")
    public ModelAndView addRysParametric(@ModelAttribute RysParametric rysParametric){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/addRysParametric");

        boolean result = rysParametricService.insertRysParametric(rysParametric);

        if(result){
            modelAndView.addObject("resp", "true");
        }else {
            modelAndView.addObject("resp","false");
        }

        return modelAndView;
    }

    @GetMapping(value = "/ifrs/rysParametric/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=RYS_Parametrica_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<RysParametric> rysParametricList= new ArrayList<RysParametric>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            rysParametricList = rysParametricService.findAll();
        }
        else{
            rysParametricList = rysParametricService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        RysParametricListReport listReport = new RysParametricListReport(rysParametricList);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/searchRysParametric")
    @ResponseBody
    public ModelAndView searchRysParametric(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<RysParametric> list=rysParametricService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<RysParametric> pageRysParametric = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageRysParametric.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allRysParametric",pageRysParametric.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchRysParametric");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/rysParametric");
        return modelAndView;
    }
}
