package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.ResponsibleAccount;
import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ResponsibleAccountListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ResponsibleAccountService;
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
public class ResponsibleAccountController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ResponsibleAccountService responsibleAccountService;

    private List<String> listColumns=List.of("Cuenta Local", "Input","Componente","Aplica SICC","Aplica Base Fiscal","Aplica Metodología", "Aplica MIS","Centro");
    @GetMapping(value="/parametric/responsibleAccount")
    public ModelAndView showResponsibleAccount(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Cuenta Responsable")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            List<ResponsibleAccount> pageResponsibleAccount = responsibleAccountService.getAll();
            int startA = (int) pageRequest.getOffset();
            int endA = Math.min((startA + pageRequest.getPageSize()), pageResponsibleAccount.size());
            Page<ResponsibleAccount> pageCenterA = new PageImpl<>(pageResponsibleAccount.subList(startA, endA), pageRequest, pageResponsibleAccount.size());

            int totalPageCenterA = pageCenterA.getTotalPages();
            if (totalPageCenterA > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPageCenterA).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allResponsibleAccount", pageCenterA.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPageCenterA);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "responsibleAccount");
            modelAndView.addObject("registers",pageResponsibleAccount.size());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/responsibleAccount");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/responsibleAccount")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/responsibleAccount");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ResponsibleAccount> responsibleAccountList = responsibleAccountService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ResponsibleAccountListReport listReport = new ResponsibleAccountListReport(responsibleAccountList,userService);
            ArrayList<String[]> list=responsibleAccountService.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/parametric/modifyResponsibleAccount/{id}")
    @ResponseBody
    public ModelAndView modifyResponsibleAccount(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ResponsibleAccount toModify = responsibleAccountService.findResponsibleAccountById(Long.parseLong(id));
        modelAndView.addObject("responsibleAccountModify",toModify);
        modelAndView.addObject("centro",toModify.getCentro());
        modelAndView.setViewName("parametric/modifyResponsibleAccount");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyResponsibleAccount")
    @ResponseBody
    public ModelAndView updateResponsibleAccount(@ModelAttribute ResponsibleAccount responsibleAccount, @RequestParam String idOld,@RequestParam String idCentro,@RequestParam String idComponente,@RequestParam String idInput){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/responsibleAccount");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            Integer.parseInt(responsibleAccount.getCentro());
        }
        catch(Exception e){
            modelAndView.addObject("resp", "responsibleAccount-1");
            return  modelAndView;
        }
        try {
            ResponsibleAccount searchResponsibleAccount = responsibleAccountService.findResponsibleAccountById(responsibleAccount.getCuentaLocal());
            if (searchResponsibleAccount==null||idOld.equals(String.valueOf(responsibleAccount.getCuentaLocal())))
            {
                List<User> users=userService.findUserByCentro(responsibleAccount.getCentro());
                String resp= responsibleAccountService.modifyResponsibleAccount(responsibleAccount, Long.parseLong(idOld),users,idCentro,idInput,idComponente,user);
                if(resp.length()==7)
                {
                    modelAndView.addObject("resp", resp);
                }
                else{
                    String[] part = resp.split("/");
                    modelAndView.addObject("resp", part[0]);
                    modelAndView.addObject("row", part[1]);
                }
            }
            else
            {
                modelAndView.addObject("resp", "Modify0");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "UpdateCascade-1");
        }
        return  modelAndView;

    }

    @GetMapping(value = "/parametric/validateCuentaLocal")
    @ResponseBody
    public String validateCuentaLocalResponsibleAccount(@RequestParam String idNew,@RequestParam String idT){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(responsibleAccountService.findResponsibleAccountById(Long.parseLong(idNew))==null||idNew.equals(idT))
            result="valid";
        return  result;
    }

    @GetMapping(value="/parametric/addResponsibleAccount")
    public ModelAndView showAddResponsibleAccount(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ResponsibleAccount responsibleAccount = new ResponsibleAccount();
        modelAndView.addObject("responsibleAccount", responsibleAccount);
        modelAndView.setViewName("parametric/addResponsibleAccount");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addResponsibleAccount")
    public ModelAndView createNewResponsibleAccount(@ModelAttribute ResponsibleAccount responsibleAccount) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/responsibleAccount");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (responsibleAccountService.findResponsibleAccountById(responsibleAccount.getCuentaLocal()) == null) {
                responsibleAccountService.saveResponsibleAccount(responsibleAccount,user);
                modelAndView.addObject("resp", "Add1");
            } else {
                modelAndView.addObject("resp", "Add0");
            }
        }
        catch (Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/removeResponsibleAccount/{id}")
    @ResponseBody
    public boolean removeResponsibleAccount(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/responsibleAccount");
        User user = userService.findUserByUserName(auth.getName());
        boolean response=false;
        try {
            ResponsibleAccount toRemove = responsibleAccountService.findResponsibleAccountById(Long.parseLong(id));
            responsibleAccountService.removeResponsibleAccount(toRemove,user);
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;

    }

    @GetMapping(value = "/parametric/clearResponsibleAccount")
    @ResponseBody
    public boolean clearResponsibleAccount(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        boolean response=false;
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/responsibleAccount");
        try{
            responsibleAccountService.clearResponsibleAccount(user);
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/responsibleAccount/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CuentaResponsable_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ResponsibleAccount> responsibleAccountList= new ArrayList<ResponsibleAccount>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            responsibleAccountList = responsibleAccountService.findAll();
        }
        else{
            responsibleAccountList = responsibleAccountService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ResponsibleAccountListReport listReport = new ResponsibleAccountListReport(responsibleAccountList,userService);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchResponsibleAccount")
    @ResponseBody
    public ModelAndView searchResponsibleAccount(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ResponsibleAccount> list=responsibleAccountService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ResponsibleAccount> pageResponsibleAccount = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageResponsibleAccount.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        modelAndView.addObject("allResponsibleAccount",pageResponsibleAccount.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchResponsibleAccount");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/responsibleAccount");
        return modelAndView;
    }

    @GetMapping(value="/parametric/costCenter/{id}")
    @ResponseBody
    public ModelAndView showCostCenter(@RequestParam Map<String, Object> params,@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<UserAccount> list=responsibleAccountService.getCostCenterUser(id);
        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<UserAccount> pageCostCenter = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageCostCenter.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allCostCenter",pageCostCenter.getContent());
        modelAndView.addObject("account",id);

        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","costCenter");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/costCenter");
        return modelAndView;
    }
}
