package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountAndByProduct;
import com.inter.proyecto_intergrupo.model.parametric.AccountHistoryIFRS9;
import com.inter.proyecto_intergrupo.model.parametric.AccountHistoryIFRS9;
import com.inter.proyecto_intergrupo.model.parametric.SegmentDecisionTree;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountHistoryIFRS9ListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountHistoryIFRS9Service;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountHistoryIFRS9ListReport;
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
public class AccountHistoryIFRS9Controller {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private AccountHistoryIFRS9Service accountHistoryIFRS9Service;


    private List<String> listColumns=List.of("Cuenta", "Empresa", "CodCons", "Observaciones", "Divisa");

    @GetMapping(value="/parametric/accountHistoryIFRS9")
    public ModelAndView showAccountHistoryIFRS9(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Histórico de cuentas IFRS9")) {

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<AccountHistoryIFRS9> pageAccountHistoryIFRS9=accountHistoryIFRS9Service.getAll(pageRequest);
        int totalPage=pageAccountHistoryIFRS9.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allAccountHistoryIFRS9",pageAccountHistoryIFRS9.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","accountHistoryIFRS9");
        List<AccountHistoryIFRS9> list = accountHistoryIFRS9Service.findAll();
        modelAndView.addObject("registers",list.size());

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/accountHistoryIFRS9");
        }
            else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/accountHistoryIFRS9")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountHistoryIFRS9");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountHistoryIFRS9> accountHistoryIFRS9List = accountHistoryIFRS9Service.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            AccountHistoryIFRS9ListReport listReport = new AccountHistoryIFRS9ListReport(accountHistoryIFRS9List);
            ArrayList<String[]> list=accountHistoryIFRS9Service.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/parametric/modifyAccountHistoryIFRS9/{id}")
    @ResponseBody
    public ModelAndView modifyAccountHistoryIFRS9(@PathVariable Integer id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        AccountHistoryIFRS9 toModify = accountHistoryIFRS9Service.findAccountHistoryIFRS9byId(id).get(0);
        modelAndView.addObject("accountHistoryIFRS9Modify",toModify);
        modelAndView.setViewName("parametric/modifyAccountHistoryIFRS9");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyAccountHistoryIFRS9")
    @ResponseBody
    public ModelAndView updateAccountHistoryIFRS9(@ModelAttribute AccountHistoryIFRS9 accountHistoryIFRS9,@Param(value = "id") Integer id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountHistoryIFRS9");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            AccountHistoryIFRS9 searchAccountHistoryIFRS9 = accountHistoryIFRS9Service.findAccountHistoryIFRS9byId(id).get(0);
            if (searchAccountHistoryIFRS9!=null)
            {
                if (validarRegistro(modelAndView, accountHistoryIFRS9)){
                    accountHistoryIFRS9Service.modifyAccountHistoryIFRS9(accountHistoryIFRS9, id);
                    modelAndView.addObject("resp", "Modify1");
                }
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


    @GetMapping(value="/parametric/addAccountHistoryIFRS9")
    public ModelAndView showAddAccountHistoryIFRS9(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        AccountHistoryIFRS9 accountHistoryIFRS9 = new AccountHistoryIFRS9();
        modelAndView.addObject("accountHistoryIFRS9", accountHistoryIFRS9);
        modelAndView.setViewName("parametric/addAccountHistoryIFRS9");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addAccountHistoryIFRS9")
    public ModelAndView createNewAccountHistoryIFRS9(@ModelAttribute AccountHistoryIFRS9 accountHistoryIFRS9) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountHistoryIFRS9");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (accountHistoryIFRS9Service.findAccountHistoryIFRS9byId(accountHistoryIFRS9.getId()).size() == 0) {
                if (validarRegistro(modelAndView, accountHistoryIFRS9)){
                    accountHistoryIFRS9Service.saveAccountHistoryIFRS9(accountHistoryIFRS9);
                    modelAndView.addObject("resp", "Add1");
                }
            } else {
                modelAndView.addObject("resp", "Add0");
            }
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/removeAccountHistoryIFRS9/{id}")
    @ResponseBody
    public boolean removeAccountHistoryIFRS9(@PathVariable Integer id,@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountHistoryIFRS9");
        boolean response=false;
        try {
            AccountHistoryIFRS9 toRemove = accountHistoryIFRS9Service.findAccountHistoryIFRS9byId(id).get(0);
            accountHistoryIFRS9Service.removeAccountHistoryIFRS9(toRemove.getId());
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/clearAccountHistoryIFRS9")
    public ModelAndView clearAccountHistoryIFRS9(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        accountHistoryIFRS9Service.clearAccountHistoryIFRS9(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountHistoryIFRS9");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/accountHistoryIFRS9/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=HistoricoCuentasIFRS9_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountHistoryIFRS9> accountHistoryIFRS9List= new ArrayList<AccountHistoryIFRS9>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            accountHistoryIFRS9List = accountHistoryIFRS9Service.findAll();
        }
        else{
            accountHistoryIFRS9List = accountHistoryIFRS9Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        AccountHistoryIFRS9ListReport listReport = new AccountHistoryIFRS9ListReport(accountHistoryIFRS9List);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchAccountHistoryIFRS9")
    @ResponseBody
    public ModelAndView searchAccountHistoryIFRS9(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<AccountHistoryIFRS9> list=accountHistoryIFRS9Service.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<AccountHistoryIFRS9> pageAccountHistoryIFRS9 = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageAccountHistoryIFRS9.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allAccountHistoryIFRS9",pageAccountHistoryIFRS9.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchAccountHistoryIFRS9");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/accountHistoryIFRS9");
        return modelAndView;
    }

    private Boolean validarRegistro(ModelAndView modelAndView, AccountHistoryIFRS9 accountHistoryIFRS9){
        boolean salida = true;
        if (accountHistoryIFRS9.getCodCons().length() != 5){
            modelAndView.addObject("resp", "ErrorCodigoTamanio5");
            salida = false;
        }
        else{
            try{
                Double.parseDouble(accountHistoryIFRS9.getCodCons());
            } catch (Exception e){
                modelAndView.addObject("resp", "ErrorCodigoCaracteres");
                salida = false;
            }
        }
        if (accountHistoryIFRS9.getCuenta().length() < 4 || accountHistoryIFRS9.getCuenta().length() > 18){
            modelAndView.addObject("resp", "ErrorCuentaTamanio9");
            salida = false;
        }
        else{
            try{
                Double.parseDouble(accountHistoryIFRS9.getCuenta());
            } catch (Exception e){
                modelAndView.addObject("resp", "ErrorCuentaCaracteres");
                salida = false;
            }
        }
        if (accountHistoryIFRS9.getDivisa().length() != 3){
            modelAndView.addObject("resp", "ErrorDivisa");
            salida = false;
        }
        return salida;
    }
}