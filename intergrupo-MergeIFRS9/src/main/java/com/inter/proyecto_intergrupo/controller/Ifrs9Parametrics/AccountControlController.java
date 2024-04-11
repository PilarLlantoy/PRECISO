package com.inter.proyecto_intergrupo.controller.Ifrs9Parametrics;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.AccountControl;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.AccountControlService;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.AccountControlListReport;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.AccountControlService;
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
public class AccountControlController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private AccountControlService accountControlService;

    private List<String> listColumns=List.of("Cuenta","Descripción Cuenta","Código de Control","Días de Plazo","Indicador de la Cuenta","Tipo de Apunte","Inventariable");

    @GetMapping(value="/ifrs/accountControl")
    public ModelAndView showAccountControl(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Control Contable")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<AccountControl> pageAccountControl = accountControlService.getAll(pageRequest);
            int totalPage = pageAccountControl.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allAccountControl", pageAccountControl.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "accountControl");
            modelAndView.addObject("registers",pageAccountControl.getTotalElements());

            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/accountControl");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/accountControl")
    public void uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountControl");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountControl> accountControlList = accountControlService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            AccountControlListReport listReport = new AccountControlListReport(accountControlList);
            ArrayList<String[]> list=accountControlService.saveFileBD(fileContent,user);
            String[] part=list.get(0);

            modelAndView.addObject("resp", "AddRep1");
            modelAndView.addObject("row", part[0]);
            modelAndView.addObject("colum", part[1]);
            listReport.exportLog(response,list);
            response.sendRedirect("/ifrs/accountControl");


        }catch(Exception e){
            //return  modelAndView;
        }
        //return  modelAndView;
    }

    @GetMapping(value = "/ifrs/modifyAccountControl/{id}")
    @ResponseBody
    public ModelAndView modifyAccountControl(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        AccountControl toModify = accountControlService.findByCUENTA(id);
        modelAndView.addObject("accountControlModify",toModify);
        modelAndView.addObject("cuentaId",toModify.getCUENTA());
        modelAndView.setViewName("ifrs/modifyAccountControl");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/modifyAccountControl")
    @ResponseBody
    public ModelAndView updateAccountControl(@ModelAttribute AccountControl accountControl,@Param(value = "id") String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountControl");
        try {
            AccountControl searchAccountControl = accountControlService.findByCUENTA(accountControl.getCUENTA());
            if (searchAccountControl==null||id.equals(accountControl.getCUENTA()))
            {
                accountControlService.modifyAccountControl(accountControl, id,user);
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

    @GetMapping(value="/ifrs/addAccountControl")
    public ModelAndView showAddAccountControl(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        AccountControl accountControl = new AccountControl();
        modelAndView.addObject("AccountControl", accountControl);
        modelAndView.setViewName("ifrs/addAccountControl");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/removeAccountControl/{id}")
    @ResponseBody
    public ModelAndView removeAccountControl(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        AccountControl toRemove = accountControlService.findByCUENTA(id);
        accountControlService.removeAccountControl(toRemove.getCUENTA(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountControl");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/clearAccountControl")
    public ModelAndView clearAccountControl(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        accountControlService.clearAccountControl(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountControl");
        return  modelAndView;
    }

    @PostMapping(value = "/ifrs/addAccountControl/add")
    public ModelAndView addAccountControl(@ModelAttribute AccountControl accountControl){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/addAccountControl");

        boolean result = accountControlService.insertAccountControl(accountControl);

        if(result){
            modelAndView.addObject("resp", "true");
        }else {
            modelAndView.addObject("resp","false");
        }

        return modelAndView;
    }

    @GetMapping(value = "/ifrs/accountControl/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Control_Contable_Parametrica_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountControl> accountControlList= new ArrayList<AccountControl>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            accountControlList = accountControlService.findAll();
        }
        else{
            accountControlList = accountControlService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        AccountControlListReport listReport = new AccountControlListReport(accountControlList);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/searchAccountControl")
    @ResponseBody
    public ModelAndView searchAccountControl(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<AccountControl> list=accountControlService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<AccountControl> pageAccountControl = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageAccountControl.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allAccountControl",pageAccountControl.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchAccountControl");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/accountControl");
        return modelAndView;
    }
}
