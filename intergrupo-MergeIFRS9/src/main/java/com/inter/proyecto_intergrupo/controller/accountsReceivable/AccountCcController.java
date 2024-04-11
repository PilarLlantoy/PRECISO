package com.inter.proyecto_intergrupo.controller.accountsReceivable;

import com.inter.proyecto_intergrupo.model.accountsReceivable.AccountCc;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.service.accountsReceivable.AccountCcListReport;
import com.inter.proyecto_intergrupo.service.accountsReceivable.AccountCcPService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureService;
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

import javax.servlet.ServletOutputStream;
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
public class AccountCcController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private AccountCcPService accountCcService;

    private List<String> listColumns=List.of("Concepto", "Cuenta", "Centro","Naturaleza","Impuesto", "Evento ");

    @GetMapping(value="/accountsReceivable/accountCc")
    public ModelAndView showAccountCc(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Parametrica Cuentas por Cobrar")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<AccountCc> pageAccount = accountCcService.getAll(pageRequest);
            int totalPage = pageAccount.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allAccountCc", pageAccount.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "accountCc");
            modelAndView.addObject("registers",pageAccount.getTotalElements());
            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("accountsReceivable/accountCc");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/accountsReceivable/accountCc")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/accountsReceivable/accountCc");
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
            ArrayList<String[]> list = accountCcService.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/accountsReceivable/modifyAccountCc/{id}")
    @ResponseBody
    public ModelAndView modifyAccountCc(@PathVariable String id){
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
        AccountCc toModify = accountCcService.findByIdCuentas(idTemp);
        modelAndView.addObject("accountCcModify",toModify);
        modelAndView.setViewName("accountsReceivable/modifyAccountCc");
        return modelAndView;
    }

    @PostMapping(value = "/accountsReceivable/modifyAccountCc")
    @ResponseBody
    public ModelAndView updateAccountCc(@ModelAttribute AccountCc accountCc){
        ModelAndView modelAndView = new ModelAndView("redirect:/accountsReceivable/accountCc");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            accountCcService.modifyAccount(accountCc, user);
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value="/accountsReceivable/addAccountCc")
    public ModelAndView showAddAccountCc(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        AccountCc accountCc = new AccountCc();
        modelAndView.addObject("accountCc", accountCc);
        modelAndView.setViewName("accountsReceivable/addAccountCc");
        return modelAndView;
    }

    @PostMapping(value = "/accountsReceivable/addAccountCc")
    public ModelAndView createNewAccountCc(@ModelAttribute AccountCc accountCc) {
        ModelAndView modelAndView = new ModelAndView("redirect:/accountsReceivable/accountCc");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            accountCcService.saveAccount(accountCc, user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/accountsReceivable/removeAccountCc/{id}")
    @ResponseBody
    public ModelAndView removeAccountCc(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        AccountCc toRemove = accountCcService.findByIdCuentas(Long.parseLong(id));
        accountCcService.removeAccount(toRemove.getIdCuentas(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/accountsReceivable/accountCc");
        return  modelAndView;
    }

    @GetMapping(value = "/accountsReceivable/clearAccountCc")
    public ModelAndView clearAccountCc(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        accountCcService.clearAccount(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/accountsReceivable/accountCc");
        return  modelAndView;
    }

    @GetMapping(value = "/accountsReceivable/accountCc/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Cuentas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountCc> accountCcList= new ArrayList<AccountCc>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            accountCcList = accountCcService.findAll();
        }
        else{
            accountCcList = accountCcService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        AccountCcListReport listReport = new AccountCcListReport(null,accountCcList);
        listReport.export(response);
    }

    @GetMapping(value = "/accountsReceivable/searchAccountCc")
    @ResponseBody
    public ModelAndView searchAccountCc(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<AccountCc> list=accountCcService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<AccountCc> pageAccountCc = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageAccountCc.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allAccountCc",pageAccountCc.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchAccountCc");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("accountsReceivable/accountCc");
        return modelAndView;
    }

}