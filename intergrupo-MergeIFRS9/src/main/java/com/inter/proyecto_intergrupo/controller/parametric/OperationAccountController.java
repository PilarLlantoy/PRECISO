package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.OperationAccount;
import com.inter.proyecto_intergrupo.model.parametric.ResponsibleAccount;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.OperationAccountListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.OperationAccountService;
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
public class OperationAccountController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private OperationAccountService responsibleAccountService;

    private List<String> listColumnsOperation=List.of("Cuenta Local","Tipo Operación","Tipo Riesgo");

    @GetMapping(value="/parametric/operationAccount")
    public ModelAndView showOperationAccount(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Operación y Riesgo de Cuentas"))
        {
            int page = params.get("page") == null ? 0 : (Integer.valueOf(params.get("page").toString()) - 1);
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            List<OperationAccount> list = responsibleAccountService.getExceptEmpty();

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<OperationAccount> pageResponsibleAccount = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageResponsibleAccount.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allOperationAccount", pageResponsibleAccount.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumnsOperation);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "operationAccount");
            modelAndView.addObject("registers",list.size());

            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/operationAccount");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }
    @GetMapping(value = "/parametric/searchOperationAccount")
    @ResponseBody
    public ModelAndView searchOperationAccount(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<OperationAccount> list=responsibleAccountService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<OperationAccount> pageResponsibleAccount = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageResponsibleAccount.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allOperationAccount",pageResponsibleAccount.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumnsOperation);
        modelAndView.addObject("directory","searchOperationAccount");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/operationAccount");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/operationAccount/download")
    @ResponseBody
    public void exportToExcelOperation(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=OperaciónCuenta_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<OperationAccount> responsibleAccountList= new ArrayList<OperationAccount>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            responsibleAccountList = responsibleAccountService.findAll();
        }
        else{
            responsibleAccountList = responsibleAccountService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        OperationAccountListReport listReport = new OperationAccountListReport(responsibleAccountList,userService);
        listReport.exportOperation(response);
    }

    @PostMapping(value="/parametric/operationAccount")
    public ModelAndView uploadFileOperation(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/operationAccount");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logInsert_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<OperationAccount> responsibleAccountList = responsibleAccountService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            OperationAccountListReport listReport = new OperationAccountListReport(responsibleAccountList,userService);
            ArrayList<String[]> list=responsibleAccountService.saveFileBDOperation(fileContent,user);
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

    @GetMapping(value = "/parametric/modifyOperationAccount/{id}")
    @ResponseBody
    public ModelAndView modifyOperationAccount(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        OperationAccount toModify = responsibleAccountService.findResponsibleAccountById(id);
        modelAndView.addObject("operationAccountModify",toModify);
        modelAndView.setViewName("parametric/modifyOperationAccount");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyOperationAccount")
    @ResponseBody
    public ModelAndView updateOperationAccount(@ModelAttribute OperationAccount responsibleAccount, @RequestParam String idOld){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/operationAccount");
        try {
            OperationAccount searchResponsibleAccount = responsibleAccountService.findResponsibleAccountById(idOld);
            if (searchResponsibleAccount!=null)
            {
                responsibleAccountService.modifyOperationAccount(responsibleAccount, idOld,searchResponsibleAccount,user);
                modelAndView.addObject("resp", "Modify1");
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

    @GetMapping(value = "/parametric/removeOperation/{id}")
    @ResponseBody
    public ModelAndView removeOperations(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        OperationAccount toRemove = responsibleAccountService.findResponsibleAccountById(id);
        responsibleAccountService.removeOperation(toRemove.getCuentaLocal(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/operationAccount");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearOperation")
    @ResponseBody
    public boolean clearContract(){
        boolean response=false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/operationAccount");
        try{
            responsibleAccountService.clearOperation(user);
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value="/parametric/addOperationAccount")
    public ModelAndView showAddOperation(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        OperationAccount operation = new OperationAccount();
        modelAndView.addObject("operation", operation);
        modelAndView.setViewName("parametric/addOperationAccount");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addOperationAccount")
    public ModelAndView createNewUser(@ModelAttribute OperationAccount operation) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/operationAccount");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (responsibleAccountService.findResponsibleAccountById(operation.getCuentaLocal()) == null) {
                responsibleAccountService.saveOperation(operation,user);
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

}
