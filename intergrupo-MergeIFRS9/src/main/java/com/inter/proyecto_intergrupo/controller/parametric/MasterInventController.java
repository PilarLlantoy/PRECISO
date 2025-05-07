package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.model.parametric.MasterInvent;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.EventTypeListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.GeneralListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.MasterInventListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.MasterInventService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class MasterInventController {
    private static final int PAGINATIONCOUNT=12;
    private List<String> listColumns=List.of("Conciliación","Contable");
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private MasterInventService masterInventService;

    @GetMapping(value="/parametric/masterinvent")
    public ModelAndView showEventType(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Maestro Inventarios");
        if(userService.validateEndpoint(user.getId(),"Ver Maestro Inventarios")) {

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            LocalDate localDate= LocalDate.now();
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM");
            String mes = localDate.format(formato);

            if(params.get("vperiod")!=null)
                mes = params.get("vperiod").toString();
            List<Object[]> data = masterInventService.findAllObj(mes);

            modelAndView.addObject("vperiod",mes);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), data.size());
            Page<Object[]> pageET = new PageImpl<>(data.subList(start, end), pageRequest, data.size());

            int totalPage=pageET.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allData",pageET.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("registers",data.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "masterinvent");
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/masterinvent");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/masterinvent")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/masterinvent");
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
            ArrayList<String[]> list = masterInventService.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                GeneralListReport generalListReport = new GeneralListReport(list);
                generalListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyMasterinvent/{id}")
    @ResponseBody
    public ModelAndView modifyMasterinvent(@PathVariable Long id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        MasterInvent toModify = masterInventService.findAllById(id);
        modelAndView.addObject("objeto",toModify);
        modelAndView.addObject("concil",masterInventService.getAllConcil());
        modelAndView.addObject("id",toModify.getId());
        modelAndView.addObject("fechaConciliacion1",toModify.getFechaConciliacion().format(formatter));
        modelAndView.addObject("fechaCargueContable1",toModify.getFechaCargueContable().format(formatter));
        modelAndView.setViewName("parametric/modifyMasterinvent");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyMasterinvent")
    public ModelAndView updateMasterinvent(@ModelAttribute MasterInvent objeto,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/masterinvent");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        masterInventService.eliminar(objeto);
        if(masterInventService.findByConcilConta(String.valueOf(objeto.getCodigoConciliacion().getNombre()),String.valueOf(objeto.getCodigoCargueContable().getNombre()),LocalDate.parse(params.get("fechaConciliacion1").toString(), dateFormat)).size()==0) {
            modelAndView.addObject("resp", "Modify1");
            objeto.setFechaConciliacion(LocalDate.parse(params.get("fechaConciliacion1").toString(), dateFormat));
            objeto.setFechaCargueContable(LocalDate.parse(params.get("fechaCargueContable1").toString(), dateFormat));
            masterInventService.modificar(objeto);
        }
        else {
            modelAndView.addObject("resp", "Maes-2");
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/deleteMasterinvent/{id}")
    public ModelAndView deletemasterinvent(@PathVariable Long id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/masterinvent");
        try {
            MasterInvent objeto = masterInventService.findAllById(id);
            masterInventService.eliminar(objeto);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }


    @GetMapping(value = "/parametric/generateDates")
    public ModelAndView generateDates(){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/masterinvent");
        try {
            masterInventService.generateDates();
            modelAndView.addObject("resp", "Maes1");
        }
        catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "Maes-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/createMasterinvent")
    public ModelAndView showCreateMasterinvent(){
        ModelAndView modelAndView = new ModelAndView();
        MasterInvent objeto = new MasterInvent();
        modelAndView.addObject("objeto",objeto);
        modelAndView.addObject("concil",masterInventService.getAllConcil());
        modelAndView.setViewName("/parametric/createMasterinvent");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createMasterinvent")
    public ModelAndView createMasterInvent(@ModelAttribute MasterInvent objeto,@RequestParam Map<String, Object> params){
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/masterinvent");
        if(masterInventService.findByConcilConta(String.valueOf(objeto.getCodigoConciliacion().getNombre()),String.valueOf(objeto.getCodigoCargueContable().getNombre()),LocalDate.parse(params.get("fechaConciliacion1").toString(), dateFormat)).size()==0) {
            modelAndView.addObject("resp", "Add1");
            objeto.setFechaConciliacion(LocalDate.parse(params.get("fechaConciliacion1").toString(), dateFormat));
            objeto.setFechaCargueContable(LocalDate.parse(params.get("fechaCargueContable1").toString(), dateFormat));
            masterInventService.modificar(objeto);
        }
        else {
            modelAndView.addObject("resp", "Maes-2");
        }
        return modelAndView;

    }

    @GetMapping(value = "/parametric/searchMasterinvent")
    @ResponseBody
    public ModelAndView searchMasterInvent(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Object[]> list;
        if((params.get("vId")==null && params.get("vFilter") ==null) || (params.get("vId").toString().equalsIgnoreCase("") && params.get("vFilter").toString().equalsIgnoreCase("")) )
            list = masterInventService.findAllObj(params.get("vperiod").toString());
        else {
            list = masterInventService.findByFilter(params.get("vId").toString(), params.get("vFilter").toString(), params.get("vperiod"));
            modelAndView.addObject("vId",params.get("vId").toString());
            modelAndView.addObject("vFilter",params.get("vFilter").toString());
        }

        modelAndView.addObject("vperiod",params.get("vperiod").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Object[]> pageTypeEntity = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageTypeEntity.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allData",pageTypeEntity.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchMasterinvent");
        modelAndView.addObject("filterExport", "Filtrado");
        modelAndView.addObject("registers",list.size());
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Maestro Inventarios");

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("p_modificar", p_modificar);

        modelAndView.setViewName("parametric/masterinvent");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/masterinvent/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Maestro_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> list= new ArrayList<>();
        //if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            list = masterInventService.findAllObj(params.get("vperiod").toString());
        /*}
        else{
            list = masterInventService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }*/
        MasterInventListReport listReport = new MasterInventListReport(null);
        listReport.export(response,list);
    }
}
