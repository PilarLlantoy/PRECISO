package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Centers;
import com.inter.proyecto_intergrupo.model.parametric.AccountAndByProduct;
import com.inter.proyecto_intergrupo.model.parametric.AccountHistoryIFRS9;
import com.inter.proyecto_intergrupo.model.parametric.ResponsibleAccount;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountAndByProductService;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountAndByProductListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ResponsibleAccountListReport;
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
public class AccountAndByProductController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private AccountAndByProductService accountAndByProductService;


    private List<String> listColumns=List.of("Cuenta", "Subproducto");

    @GetMapping(value="/parametric/accountAndByProduct")
    public ModelAndView showAccountAndByProduct(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Cuenta y subproductos locales")) {

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<AccountAndByProduct> pageAccountAndByProduct=accountAndByProductService.getAll(pageRequest);
        int totalPage=pageAccountAndByProduct.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allAccountAndByProduct",pageAccountAndByProduct.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","accountAndByProduct");
        List<AccountAndByProduct> list = accountAndByProductService.findAll();
        modelAndView.addObject("registers",list.size());

        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/accountAndByProduct");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/accountAndByProduct")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountAndByProduct");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountAndByProduct> accountAndByProductList = accountAndByProductService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            AccountAndByProductListReport listReport = new AccountAndByProductListReport(accountAndByProductList);
            ArrayList<String[]> list=accountAndByProductService.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/parametric/modifyAccountAndByProduct/{id}")
    @ResponseBody
    public ModelAndView modifyAccountAndByProduct(@PathVariable Integer id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        AccountAndByProduct toModify = accountAndByProductService.findAccountAndByProductbyId(id).get(0);
        modelAndView.addObject("accountAndByProductModify",toModify);
        modelAndView.setViewName("parametric/modifyAccountAndByProduct");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyAccountAndByProduct")
    @ResponseBody
    public ModelAndView updateAccountAndByProduct(@ModelAttribute AccountAndByProduct accountAndByProduct,@Param(value = "id") Integer id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountAndByProduct");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            AccountAndByProduct searchAccountAndByProduct = accountAndByProductService.findAccountAndByProductbyId(id).get(0);
            if (searchAccountAndByProduct!=null)
            {
                if (validarRegistro(modelAndView, accountAndByProduct)){
                    accountAndByProductService.modifyAccountAndByProduct(accountAndByProduct, id);
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


    @GetMapping(value="/parametric/addAccountAndByProduct")
    public ModelAndView showAddAccountAndByProduct(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        AccountAndByProduct accountAndByProduct = new AccountAndByProduct();
        modelAndView.addObject("accountAndByProduct", accountAndByProduct);
        modelAndView.setViewName("parametric/addAccountAndByProduct");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addAccountAndByProduct")
    public ModelAndView createNewAccountAndByProduct(@ModelAttribute AccountAndByProduct accountAndByProduct) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountAndByProduct");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (accountAndByProductService.findAccountAndByProductbyId(accountAndByProduct.getId()).size() == 0) {
                if (validarRegistro(modelAndView, accountAndByProduct)){
                    accountAndByProductService.saveAccountAndByProduct(accountAndByProduct);
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

    @GetMapping(value = "/parametric/removeAccountAndByProduct/{id}")
    @ResponseBody
    public boolean removeAccountAndByProduct(@PathVariable Integer id,@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountAndByProduct");
        boolean response=false;
        try {
            AccountAndByProduct toRemove = accountAndByProductService.findAccountAndByProductbyId(id).get(0);
            accountAndByProductService.removeAccountAndByProduct(toRemove.getId());
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/clearAccountAndByProduct")
    public ModelAndView clearAccountAndByProduct(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        accountAndByProductService.clearAccountAndByProduct(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountAndByProduct");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/accountAndByProduct/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CuentasYSubproductoLocal_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountAndByProduct> accountAndByProductList= new ArrayList<AccountAndByProduct>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            accountAndByProductList = accountAndByProductService.findAll();
        }
        else{
            accountAndByProductList = accountAndByProductService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        AccountAndByProductListReport listReport = new AccountAndByProductListReport(accountAndByProductList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchAccountAndByProduct")
    @ResponseBody
    public ModelAndView searchAccountAndByProduct(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<AccountAndByProduct> list=accountAndByProductService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<AccountAndByProduct> pageAccountAndByProduct = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageAccountAndByProduct.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allAccountAndByProduct",pageAccountAndByProduct.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchAccountAndByProduct");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/accountAndByProduct");
        return modelAndView;
    }

    private Boolean validarRegistro(ModelAndView modelAndView, AccountAndByProduct accountAndByProduct){
        boolean salida = true;
        if (accountAndByProduct.getCuenta().length() < 4 || accountAndByProduct.getCuenta().length() > 18){
            modelAndView.addObject("resp", "ErrorCuentaTamanio9");
            salida = false;
        }
        try{
            Double.parseDouble(accountAndByProduct.getCuenta());
        } catch (Exception e){
            modelAndView.addObject("resp", "ErrorCuentaCaracteres");
            salida = false;
        }
        return salida;
    }
}