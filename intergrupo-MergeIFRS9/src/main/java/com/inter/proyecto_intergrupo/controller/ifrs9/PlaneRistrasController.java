package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreationPlane;
import com.inter.proyecto_intergrupo.model.ifrs9.PlaneRistras;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.PlaneRistrasListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.PlaneRistrasService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class PlaneRistrasController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private PlaneRistrasService planeRistrasService;

    private List<String> listColumns=List.of("Banco","Interfaz","Cuenta Definitiva","Producto","Tipo De Cartera","Campo12","Calificacion","Campo14","Código Sector","Código Subsector","Forma De Pago","Línea De Crédito","Entid Redescuento","Morosidad","Tipo Inversión","Tipo De Gasto","Concepto Contable","Divisa","Tipo Moneda","Filler","Varios","Valor","Sagrupas");

    @GetMapping(value="/ifrs/planeRistras")
    public ModelAndView showPlaneRistras(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Plano Ristras")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<PlaneRistras> pagePlaneRistras = planeRistrasService.getAll(pageRequest);
            int totalPage = pagePlaneRistras.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allPlaneRistras", pagePlaneRistras.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "planeRistras");
            modelAndView.addObject("registers",pagePlaneRistras.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/planeRistras");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/planeRistras")
    public void uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/planeRistras");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<PlaneRistras> planeRistrasList = planeRistrasService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            PlaneRistrasListReport listReport = new PlaneRistrasListReport(planeRistrasList);
            ArrayList<String[]> list=planeRistrasService.saveFileBD(fileContent,user);
            String[] part=list.get(0);

            modelAndView.addObject("resp", "AddRep1");
            modelAndView.addObject("row", part[0]);
            modelAndView.addObject("colum", part[1]);
            listReport.exportLog(response,list);
            response.sendRedirect("/ifrs/planeRistras");


        }catch(Exception e){
            //return  modelAndView;
        }
        //return  modelAndView;
    }

    @GetMapping(value = "/ifrs/modifyPlaneRistras/{id}")
    @ResponseBody
    public ModelAndView modifyPlaneRistras(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        PlaneRistras toModify = planeRistrasService.findByIdPlano(Long.parseLong(id));
        modelAndView.addObject("planeRistrasModify",toModify);
        modelAndView.addObject("cuentaId",toModify.getIdPlano());
        modelAndView.setViewName("ifrs/modifyPlaneRistras");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/modifyPlaneRistras")
    @ResponseBody
    public ModelAndView updatePlaneRistras(@ModelAttribute PlaneRistras planeRistras,@Param(value = "id") String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/planeRistras");
        try {
            PlaneRistras searchPlaneRistras = planeRistrasService.findByIdPlano(planeRistras.getIdPlano());
            if (searchPlaneRistras==null||id.equals(planeRistras.getIdPlano()))
            {
                planeRistrasService.modifyPlaneRistras(planeRistras, Long.parseLong(id),user);
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

    @GetMapping(value="/ifrs/addPlaneRistras")
    public ModelAndView showAddPlaneRistras(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        PlaneRistras planeRistras = new PlaneRistras();
        modelAndView.addObject("PlaneRistras", planeRistras);
        modelAndView.setViewName("ifrs/addPlaneRistras");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/removePlaneRistras/{id}")
    @ResponseBody
    public ModelAndView removePlaneRistras(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        PlaneRistras toRemove = planeRistrasService.findByIdPlano(Long.parseLong(id));
        planeRistrasService.removePlaneRistras(toRemove.getIdPlano(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/planeRistras");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/clearPlaneRistras")
    public ModelAndView clearPlaneRistras(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        planeRistrasService.clearPlaneRistras(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/planeRistras");
        return  modelAndView;
    }

    @PostMapping(value = "/ifrs/addPlaneRistras/add")
    public ModelAndView addPlaneRistras(@ModelAttribute PlaneRistras planeRistras){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/addPlaneRistras");

        boolean result = planeRistrasService.insertPlaneRistras(planeRistras);

        if(result){
            modelAndView.addObject("resp", "true");
        }else {
            modelAndView.addObject("resp","false");
        }

        return modelAndView;
    }

    @GetMapping(value = "/ifrs/planeRistras/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Equivalencias_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<PlaneRistras> planeRistrasList= new ArrayList<PlaneRistras>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            planeRistrasList = planeRistrasService.findAll();
        }
        else{
            planeRistrasList = planeRistrasService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        PlaneRistrasListReport listReport = new PlaneRistrasListReport(planeRistrasList);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/searchPlaneRistras")
    @ResponseBody
    public ModelAndView searchPlaneRistras(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<PlaneRistras> list=planeRistrasService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<PlaneRistras> pagePlaneRistras = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pagePlaneRistras.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allPlaneRistras",pagePlaneRistras.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchPlaneRistras");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/planeRistras");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/planeRistras/downloadPlane")
    @ResponseBody
    public void exportToExcelPlane(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=PlanoRistras" + currentDateTime + ".txt";
        response.setHeader(headerKey, headerValue);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        List<PlaneRistras> planeRistrasList= planeRistrasService.findAll();
        CsvService.downloadCsvPlaneRistras(response.getWriter(), planeRistrasList);
    }

    @GetMapping(value = "/ifrs/planeRistrasLoad")
    public ModelAndView loadPlaneRistras(@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/planeRistras");

        try
        {
            boolean respIf = planeRistrasService.planeRistrasLoad(user);
            if(respIf)
            {
                modelAndView.addObject("resp", "loadPR1");
            }
            else
            {
                modelAndView.addObject("resp", "loadPR-1");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            modelAndView.addObject("resp", "loadPR-2");
        }

        return  modelAndView;

    }

}
