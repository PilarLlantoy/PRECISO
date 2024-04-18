package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountAndByProduct;
import com.inter.proyecto_intergrupo.model.parametric.GenericAccount;
import com.inter.proyecto_intergrupo.model.parametric.CounterpartyGenericContracts;
import com.inter.proyecto_intergrupo.model.parametric.GenericAccount;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.GenericAccountListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.GenericAccountService;
import com.inter.proyecto_intergrupo.service.parametricServices.GenericAccountListReport;
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
public class GenericAccountController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private GenericAccountService genericAccountService;


    private List<String> listColumns=List.of("Cuenta", "Empresa", "CodCons", "Observaciones", "Divisa", "Concepto", "Fecha");

    @GetMapping(value="/parametric/genericAccount")
    public ModelAndView showGenericAccount(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Cuentas Genéricas")) {

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<GenericAccount> pageGenericAccount=genericAccountService.getAll(pageRequest);
        int totalPage=pageGenericAccount.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allGenericAccount",pageGenericAccount.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","genericAccount");
        List<GenericAccount> list = genericAccountService.findAll();
        modelAndView.addObject("registers",list.size());

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/genericAccount");
        }
            else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/genericAccount")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/genericAccount");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<GenericAccount> genericAccountList = genericAccountService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            GenericAccountListReport listReport = new GenericAccountListReport(genericAccountList);
            ArrayList<String[]> list=genericAccountService.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/parametric/modifyGenericAccount/{id}")
    @ResponseBody
    public ModelAndView modifyGenericAccount(@PathVariable Integer id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        GenericAccount toModify = genericAccountService.findGenericAccountbyId(id).get(0);
        modelAndView.addObject("genericAccountModify",toModify);
        modelAndView.setViewName("parametric/modifyGenericAccount");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyGenericAccount")
    @ResponseBody
    public ModelAndView updateGenericAccount(@ModelAttribute GenericAccount genericAccount,@Param(value = "id") Integer id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/genericAccount");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            GenericAccount searchGenericAccount = genericAccountService.findGenericAccountbyId(id).get(0);
            if (searchGenericAccount!=null)
            {
                if (validarRegistro(modelAndView, genericAccount)){
                    genericAccountService.modifyGenericAccount(genericAccount, id);
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


    @GetMapping(value="/parametric/addGenericAccount")
    public ModelAndView showAddGenericAccount(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        GenericAccount genericAccount = new GenericAccount();
        modelAndView.addObject("genericAccount", genericAccount);
        modelAndView.setViewName("parametric/addGenericAccount");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addGenericAccount")
    public ModelAndView createNewGenericAccount(@ModelAttribute GenericAccount genericAccount) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/genericAccount");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (genericAccountService.findGenericAccountbyId(genericAccount.getId()).size() == 0) {
                if(validarRegistro(modelAndView, genericAccount)){
                    genericAccountService.saveGenericAccount(genericAccount);
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

    @GetMapping(value = "/parametric/removeGenericAccount/{id}")
    @ResponseBody
    public boolean removeGenericAccount(@PathVariable Integer id,@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/genericAccount");
        boolean response=false;
        try {
                GenericAccount toRemove = genericAccountService.findGenericAccountbyId(id).get(0);
            genericAccountService.removeGenericAccount(toRemove.getId());
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/clearGenericAccount")
    public ModelAndView clearGenericAccount(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        genericAccountService.clearGenericAccount(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/genericAccount");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/genericAccount/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CuentasGenericas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<GenericAccount> genericAccountList= new ArrayList<GenericAccount>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            genericAccountList = genericAccountService.findAll();
        }
        else{
            genericAccountList = genericAccountService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        GenericAccountListReport listReport = new GenericAccountListReport(genericAccountList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchGenericAccount")
    @ResponseBody
    public ModelAndView searchGenericAccount(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<GenericAccount> list=genericAccountService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<GenericAccount> pageGenericAccount = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageGenericAccount.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allGenericAccount",pageGenericAccount.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchGenericAccount");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/genericAccount");
        return modelAndView;
    }

    private Boolean validarRegistro(ModelAndView modelAndView, GenericAccount genericAccount){
        boolean salida = true;
        if (genericAccount.getCodCons().length() != 5){
            modelAndView.addObject("resp", "ErrorCodigoTamanio5");
            salida = false;
        }
        else{
            try{
                Double.parseDouble(genericAccount.getCodCons());
            } catch (Exception e){
                modelAndView.addObject("resp", "ErrorCodigoCaracteres");
                salida = false;
            }
        }
        if (genericAccount.getCuenta().length() < 4 || genericAccount.getCuenta().length() > 18){
            modelAndView.addObject("resp", "ErrorCuentaTamanio9");
            salida = false;
        }
        else{
            try{
                Double.parseDouble(genericAccount.getCuenta());
            } catch (Exception e){
                modelAndView.addObject("resp", "ErrorCuentaCaracteres");
                salida = false;
            }
        }
        if (genericAccount.getDivisa().length() != 3){
            modelAndView.addObject("resp", "ErrorDivisa");
            salida = false;
        }
        return salida;
    }
}
