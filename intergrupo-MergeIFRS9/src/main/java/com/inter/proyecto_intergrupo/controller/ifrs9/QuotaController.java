package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Centers;
import com.inter.proyecto_intergrupo.model.ifrs9.Perimeter;
import com.inter.proyecto_intergrupo.model.ifrs9.Quotas;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.CenterListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.QuotaListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.QuotaService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xpath.operations.Quo;
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

import javax.mail.Quota;
import javax.persistence.criteria.CriteriaBuilder;
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
public class QuotaController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private QuotaService quotaService;


    private List<String> listColumns=List.of("Cuentas Puc", "Contrato Origen","Contrato ifrs9");

    @GetMapping(value="/ifrs/quotas")
    public ModelAndView showQuotas(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Parametría Contingentes Anexo 8")) {
        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<Quotas> pageSubsidiaries=quotaService.getAll(pageRequest);
        int totalPage=pageSubsidiaries.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allquotas",pageSubsidiaries.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","quotas");
        List<Quotas> list = quotaService.findAll();
        modelAndView.addObject("registers",list.size());

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/quotas");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/modifyQuotas/{id}")
    @ResponseBody
    public ModelAndView modifyQuotas(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());

        String[] contratoCuenta = id.split(":");
        Quotas toModify = quotaService.findQuotabyContratoCuenta(contratoCuenta[0],contratoCuenta[1]).get(0);
        modelAndView.addObject("quotaModify",toModify);
        modelAndView.setViewName("ifrs/modifyQuotas");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/modifyQuotas")
    @ResponseBody
    public ModelAndView updateSubsidiaries(@ModelAttribute Quotas quota,@Param(value = "id") String id){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/quotas");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String[] contratoCuenta = id.split(":");

        try {
            Quotas searchQuotas = quotaService.findQuotabyContratoCuenta(contratoCuenta[0],contratoCuenta[1]).get(0);
            if (searchQuotas!=null)
            {
                if(validarRegistro(modelAndView,quota)){
                    quotaService.modifyQuota(quota, contratoCuenta[0], contratoCuenta[1]);
                    modelAndView.addObject("resp", "Modify1");
                }
                else{
                    modelAndView.addObject("resp", "Modify0");
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

    @GetMapping(value="/ifrs/addQuota")
    public ModelAndView showAddQuota(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Quotas quota = new Quotas();
        modelAndView.addObject("quotas", quota);
        modelAndView.setViewName("ifrs/addQuota");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/addQuota")
    public ModelAndView createNewQuota(@ModelAttribute Quotas quota) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/quotas");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        try {
            if (quotaService.findQuotabyContratoCuenta(quota.getContratoOrigen(),quota.getCuentasPuc()).size() == 0) {
                if(validarRegistro(modelAndView, quota)){
                    quotaService.saveQuota(quota);
                    modelAndView.addObject("resp", "Add1");
                }
                else{
                    modelAndView.addObject("resp", "Add0");
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

    @GetMapping(value = "/ifrs/removeQuota/{id}")
    @ResponseBody
    public boolean removeQuota(@PathVariable String id, @RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/quotas");
        boolean response=false;
        String[] contratoCuenta = id.split(":");
        try {
            quotaService.removeQuota(contratoCuenta[0],contratoCuenta[1]);
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/ifrs/clearQuota")
    public ModelAndView clearQuota(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        quotaService.clearQuota(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/quotas");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/quotas/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ContingentesAnexos8_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Quotas> quotasList= new ArrayList<Quotas>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            quotasList = quotaService.findAll();
        }
        else{
            quotasList = quotaService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        QuotaListReport listReport = new QuotaListReport(quotasList);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/searchQuotas")
    @ResponseBody
    public ModelAndView searchQuotas(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Quotas> list=quotaService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Quotas> pageSubsidiaries = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageSubsidiaries.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allquotas",pageSubsidiaries.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchQuotas");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/quotas");
        return modelAndView;
    }

    @PostMapping(value="/ifrs/quotas")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/quotas");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Quotas> quotasList = quotaService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            QuotaListReport listReport = new QuotaListReport(quotasList);
            ArrayList<String[]> list=quotaService.saveFileBD(fileContent,user);
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

    private Boolean validarRegistro(ModelAndView modelAndView, Quotas quotas){
        boolean salida = true;
        if (quotas.getCuentasPuc().length() < 9 || quotas.getCuentasPuc().length() > 18){
            modelAndView.addObject("resp", "Error1CuentaTamanio9");
            salida = false;
        }
        else{
            try{
                Double.parseDouble(quotas.getCuentasPuc());
            } catch (Exception e){
                modelAndView.addObject("resp", "ErrorCuentaCaracteres");
                salida = false;
            }
        }
        if (quotas.getContratoOrigen().length() != 0 && (quotas.getContratoOrigen().length() < 17 || quotas.getContratoOrigen().length() > 20)){
            modelAndView.addObject("resp", "Error1ContratoTamanio20");
            salida = false;
        }
        else{
            try{
                if (quotas.getContratoOrigen().length() != 0){
                    Double.parseDouble(quotas.getContratoOrigen());
                }
            } catch (Exception e){
                modelAndView.addObject("resp", "ErrorContratoCaracteres");
                salida = false;
            }
        }
        if (quotas.getContratoIfrs9().length() < 4 || quotas.getContratoIfrs9().length() > 20){
            modelAndView.addObject("resp", "Error2ContratoTamanio20");
            salida = false;
        }
        return salida;
    }

}
