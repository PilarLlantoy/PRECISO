package com.inter.proyecto_intergrupo.controller.briefcase;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.ContactosIcrv;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.briefcaseServices.ContactosIcrvListReport;
import com.inter.proyecto_intergrupo.service.briefcaseServices.ContactosIcrvService;
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
public class ContactosIcrvController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ContactosIcrvService contactosIcrvService;

    private List<String> listColumns=List.of("Proceso", "Nombre", "Empresa", "Correo Principal","Correo Secundario","Superior","Superior 1","Extensión","Página");

    @GetMapping(value="/briefcase/contactosicrv")
    public ModelAndView showGeneral(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Contactos ICRV")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<ContactosIcrv> pageContactosIcrv = contactosIcrvService.getAll(pageRequest);
            int totalPage = pageContactosIcrv.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allContactosicrv", pageContactosIcrv.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "contactosicrv");
            modelAndView.addObject("registers",pageContactosIcrv.getTotalElements());
            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("briefcase/contactosicrv");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/briefcase/contactosicrv")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/contactosicrv");
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
            ArrayList<String[]> list = contactosIcrvService.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                ContactosIcrvListReport contactosIcrvListReport = new ContactosIcrvListReport(list,null);
                contactosIcrvListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/modifyContactosicrv/{id}")
    @ResponseBody
    public ModelAndView modifyContactosicrv(@PathVariable String id){
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
        ContactosIcrv toModify = contactosIcrvService.findByIdContacto(idTemp);
        modelAndView.addObject("contactosicrvModify",toModify);
        modelAndView.setViewName("briefcase/modifyContactosicrv");
        return modelAndView;
    }

    @PostMapping(value = "/briefcase/modifyContactosicrv")
    @ResponseBody
    public ModelAndView updateContactosicrv(@ModelAttribute ContactosIcrv contactosicrv){
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/contactosicrv");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            contactosIcrvService.modifyContactos(contactosicrv, user);
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value="/briefcase/addContactosicrv")
    public ModelAndView showAddContactosicrv(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ContactosIcrv contactosicrv = new ContactosIcrv();
        modelAndView.addObject("contactosicrv", contactosicrv);
        modelAndView.setViewName("briefcase/addContactosicrv");
        return modelAndView;
    }

    @PostMapping(value = "/briefcase/addContactosicrv")
    public ModelAndView createNewContactosicrv(@ModelAttribute ContactosIcrv contactosicrv) {
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/contactosicrv");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            contactosIcrvService.saveContactos(contactosicrv, user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/removeContactosicrv/{id}")
    @ResponseBody
    public ModelAndView removeContactosicrv(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ContactosIcrv toRemove = contactosIcrvService.findByIdContacto(Long.parseLong(id));
        contactosIcrvService.removeContactos(toRemove.getIdContacto(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/contactosicrv");
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/clearContactosicrv")
    public ModelAndView clearContactosicrv(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        contactosIcrvService.clearContactos(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/contactosicrv");
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/contactosicrv/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ContactosICRV_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ContactosIcrv> contactosIcrvList= new ArrayList<ContactosIcrv>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            contactosIcrvList = contactosIcrvService.findAll();
        }
        else{
            contactosIcrvList = contactosIcrvService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ContactosIcrvListReport listReport = new ContactosIcrvListReport(null,contactosIcrvList);
        listReport.export(response);
    }

    @GetMapping(value = "/briefcase/searchContactosicrv")
    @ResponseBody
    public ModelAndView searchContactosicrv(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ContactosIcrv> list=contactosIcrvService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ContactosIcrv> pageContactosIcrv = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageContactosIcrv.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allContactosicrv",pageContactosIcrv.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchContactosicrv");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("briefcase/contactosicrv");
        return modelAndView;
    }
}