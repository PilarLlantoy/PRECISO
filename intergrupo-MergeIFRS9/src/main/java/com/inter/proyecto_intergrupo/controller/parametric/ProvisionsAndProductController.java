package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountAndByProduct;
import com.inter.proyecto_intergrupo.model.parametric.ProvisionsAndProduct;
import com.inter.proyecto_intergrupo.model.parametric.ProvisionsAndProduct;
import com.inter.proyecto_intergrupo.model.parametric.SegmentDecisionTree;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ProvisionsAndProductListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ProvisionsAndProductService;
import com.inter.proyecto_intergrupo.service.parametricServices.ProvisionsAndProductListReport;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ProvisionsAndProductController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ProvisionsAndProductService provisionsAndProductService;


    private List<String> listColumns=List.of("Instrumento", "Jerarquia", "Descripcion", "Cuenta", "Minimo", "PerimetroIFRS9", "StagesEspaña", "ProductoEspaña", "SectorEspaña", "Signo");

    @GetMapping(value="/parametric/provisionsAndProduct")
    public ModelAndView showProvisionsAndProduct(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<ProvisionsAndProduct> pageProvisionsAndProduct=provisionsAndProductService.getAll(pageRequest);
        int totalPage=pageProvisionsAndProduct.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allProvisionsAndProduct",pageProvisionsAndProduct.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","provisionsAndProduct");
        List<ProvisionsAndProduct> list = provisionsAndProductService.findAll();
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/provisionsAndProduct");
        return modelAndView;
    }

    @PostMapping(value="/parametric/provisionsAndProduct")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/provisionsAndProduct");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ProvisionsAndProduct> provisionsAndProductList = provisionsAndProductService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ProvisionsAndProductListReport listReport = new ProvisionsAndProductListReport(provisionsAndProductList);
            ArrayList<String[]> list=provisionsAndProductService.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/parametric/modifyProvisionsAndProduct/{cuenta}")
    @ResponseBody
    public ModelAndView modifyProvisionsAndProduct(@PathVariable String cuenta){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ProvisionsAndProduct toModify = provisionsAndProductService.findProvisionsAndProductbyCuenta(cuenta).get(0);
        modelAndView.addObject("provisionsAndProductModify",toModify);
        modelAndView.setViewName("parametric/modifyProvisionsAndProduct");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyProvisionsAndProduct")
    @ResponseBody
    public ModelAndView updateProvisionsAndProduct(@ModelAttribute ProvisionsAndProduct provisionsAndProduct,@Param(value = "cuenta") String cuenta){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/provisionsAndProduct");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {

            ProvisionsAndProduct searchProvisionsAndProduct = provisionsAndProductService.findProvisionsAndProductbyCuenta(cuenta).get(0);
            if (searchProvisionsAndProduct!=null)
            {
                if (validarRegistro(modelAndView, provisionsAndProduct)){
                    provisionsAndProductService.modifyProvisionsAndProduct(provisionsAndProduct, cuenta);
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


    @GetMapping(value="/parametric/addProvisionsAndProduct")
    public ModelAndView showAddProvisionsAndProduct(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ProvisionsAndProduct provisionsAndProduct = new ProvisionsAndProduct();
        modelAndView.addObject("provisionsAndProduct", provisionsAndProduct);
        modelAndView.setViewName("parametric/addProvisionsAndProduct");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addProvisionsAndProduct")
    public ModelAndView createNewProvisionsAndProduct(@ModelAttribute ProvisionsAndProduct provisionsAndProduct) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/provisionsAndProduct");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (provisionsAndProductService.findProvisionsAndProductbyCuenta(provisionsAndProduct.getCuenta()).size() == 0) {
                if (validarRegistro(modelAndView, provisionsAndProduct)){
                    provisionsAndProductService.saveProvisionsAndProduct(provisionsAndProduct);
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

    @GetMapping(value = "/parametric/removeProvisionsAndProduct/{cuenta}")
    @ResponseBody
    public boolean removeProvisionsAndProduct(@PathVariable String cuenta,@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/provisionsAndProduct");
        boolean response=false;
        try {
            ProvisionsAndProduct toRemove = provisionsAndProductService.findProvisionsAndProductbyCuenta(cuenta).get(0);
            provisionsAndProductService.removeProvisionsAndProduct(toRemove.getCuenta());
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/clearProvisionsAndProduct")
    public ModelAndView clearProvisionsAndProduct(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        provisionsAndProductService.clearProvisionsAndProduct(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/provisionsAndProduct");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/provisionsAndProduct/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ProvisionesYProducto_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ProvisionsAndProduct> provisionsAndProductList= new ArrayList<ProvisionsAndProduct>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            provisionsAndProductList = provisionsAndProductService.findAll();
        }
        else{
            provisionsAndProductList = provisionsAndProductService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ProvisionsAndProductListReport listReport = new ProvisionsAndProductListReport(provisionsAndProductList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchProvisionsAndProduct")
    @ResponseBody
    public ModelAndView searchProvisionsAndProduct(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ProvisionsAndProduct> list=provisionsAndProductService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ProvisionsAndProduct> pageProvisionsAndProduct = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageProvisionsAndProduct.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allProvisionsAndProduct",pageProvisionsAndProduct.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchProvisionsAndProduct");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/provisionsAndProduct");
        return modelAndView;
    }

    private Boolean validarRegistro(ModelAndView modelAndView, ProvisionsAndProduct provisionsAndProduct){
        boolean salida = true;
        if (provisionsAndProduct.getCuenta().length() < 4 || provisionsAndProduct.getCuenta().length() > 18){
            modelAndView.addObject("resp", "ErrorCuentaTamanio9");
            salida = false;
        }
        else{
            try{
                Double.parseDouble(provisionsAndProduct.getCuenta());
            } catch (Exception e){
                modelAndView.addObject("resp", "ErrorCuentaCaracteres");
                salida = false;
            }
        }
        if (!(Objects.equals(provisionsAndProduct.getSigno().strip(), "-") || Objects.equals(provisionsAndProduct.getSigno().strip(), "+"))){
            modelAndView.addObject("resp", "ErrorSigno");
            salida = false;
        }
        return salida;
    }

}