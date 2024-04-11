package com.inter.proyecto_intergrupo.controller.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.AccountBanco;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.AccountBancoListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.AccountBancoService;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamMDAListReport;
import com.inter.proyecto_intergrupo.service.reportNIC34.ParamMDAService;
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
public class AccountBancoController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private AccountBancoService accountBancoService;

    private List<String> listColumns=List.of("Cuenta", "Naturaleza");

    @GetMapping(value="/parametric/accountBanco")
    public ModelAndView showParamAccountBank(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Parametrica Naturaleza Cuentas")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            List<AccountBanco> list = accountBancoService.findAll();

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<AccountBanco> pageData= new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageData.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allData", pageData.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "accountBanco");
            modelAndView.addObject("registers",pageData.getTotalElements());
            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/accountBanco");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/accountBanco")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountBanco");
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
            ArrayList<String[]> list = accountBancoService.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                AccountBancoListReport accountBancoListReport = new AccountBancoListReport(list,null);
                accountBancoListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyAccountBanco/{id}")
    @ResponseBody
    public ModelAndView modifyAccountBanco(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountBanco");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        accountBancoService.changeNaturaleza(id);
        modelAndView.addObject("resp", "nat1");
        return modelAndView;
    }

    @GetMapping(value="/parametric/addAccountBanco")
    public ModelAndView showAddAccountBanco(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        AccountBanco paramAccountBanco = new AccountBanco();
        modelAndView.addObject("accountBanco", paramAccountBanco);
        modelAndView.setViewName("parametric/addAccountBanco");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addAccountBanco")
    public ModelAndView createNewAccountBanco(@ModelAttribute AccountBanco paramAccountBanco) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountBanco");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            accountBancoService.saveAccount(paramAccountBanco, user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/removeAccountBanco/{id}")
    @ResponseBody
    public ModelAndView removeAccountBanco(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        AccountBanco toRemove = accountBancoService.findByCuenta(id);
        accountBancoService.removeAccount(toRemove.getCuenta(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountBanco");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearAccountBanco")
    public ModelAndView clearAccountBanco(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        accountBancoService.clearAccount(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountBanco");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/accountBanco/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Naturaleza_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountBanco> accountBancosList= new ArrayList<AccountBanco>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            accountBancosList = accountBancoService.findAll();
        }
        else{
            accountBancosList = accountBancoService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        AccountBancoListReport listReport = new AccountBancoListReport(null,accountBancosList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchAccountBanco")
    @ResponseBody
    public ModelAndView searchAccountBanco(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<AccountBanco> list=accountBancoService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<AccountBanco> pageMda = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageMda.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allData",pageMda.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchAccountBanco");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/accountBanco");
        return modelAndView;
    }

}