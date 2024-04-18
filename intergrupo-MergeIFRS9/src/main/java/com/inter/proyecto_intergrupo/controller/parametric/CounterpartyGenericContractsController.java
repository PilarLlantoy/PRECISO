package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountAndByProduct;
import com.inter.proyecto_intergrupo.model.parametric.CounterpartyGenericContracts;
import com.inter.proyecto_intergrupo.model.parametric.CounterpartyGenericContracts;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.CounterpartyGenericContractsListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.CounterpartyGenericContractsService;
import com.inter.proyecto_intergrupo.service.parametricServices.CounterpartyGenericContractsListReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xpath.operations.Bool;
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
public class CounterpartyGenericContractsController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private CounterpartyGenericContractsService counterpartyGenericContractsService;


    private List<String> listColumns=List.of("Cuenta", "Concepto", "Contrato", "FuenteInformacion", "Saldo");

    @GetMapping(value="/parametric/counterpartyGenericContracts")
    public ModelAndView showCounterpartyGenericContracts(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Contrapartida y Contratos Genéricos")) {

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<CounterpartyGenericContracts> pageCounterpartyGenericContracts=counterpartyGenericContractsService.getAll(pageRequest);
        int totalPage=pageCounterpartyGenericContracts.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allCounterpartyGenericContracts",pageCounterpartyGenericContracts.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","counterpartyGenericContracts");
        List<CounterpartyGenericContracts> list = counterpartyGenericContractsService.findAll();
        modelAndView.addObject("registers",list.size());

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/counterpartyGenericContracts");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/counterpartyGenericContracts")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/counterpartyGenericContracts");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<CounterpartyGenericContracts> counterpartyGenericContractsList = counterpartyGenericContractsService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            CounterpartyGenericContractsListReport listReport = new CounterpartyGenericContractsListReport(counterpartyGenericContractsList);
            ArrayList<String[]> list=counterpartyGenericContractsService.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/parametric/modifyCounterpartyGenericContracts/{id}")
    @ResponseBody
    public ModelAndView modifyCounterpartyGenericContracts(@PathVariable Integer id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        CounterpartyGenericContracts toModify = counterpartyGenericContractsService.findCounterpartyGenericContractsbyId(id).get(0);
        modelAndView.addObject("counterpartyGenericContractsModify",toModify);
        modelAndView.setViewName("parametric/modifyCounterpartyGenericContracts");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyCounterpartyGenericContracts")
    @ResponseBody
    public ModelAndView updateCounterpartyGenericContracts(@ModelAttribute CounterpartyGenericContracts counterpartyGenericContracts,@Param(value = "id") Integer id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/counterpartyGenericContracts");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            CounterpartyGenericContracts searchCounterpartyGenericContracts = counterpartyGenericContractsService.findCounterpartyGenericContractsbyId(id).get(0);
            if (searchCounterpartyGenericContracts!=null)
            {
                if (validarRegistro(modelAndView, counterpartyGenericContracts)) {
                    counterpartyGenericContractsService.modifyCounterpartyGenericContracts(counterpartyGenericContracts, id);
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


    @GetMapping(value="/parametric/addCounterpartyGenericContracts")
    public ModelAndView showAddCounterpartyGenericContracts(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        CounterpartyGenericContracts counterpartyGenericContracts = new CounterpartyGenericContracts();
        modelAndView.addObject("counterpartyGenericContracts", counterpartyGenericContracts);
        modelAndView.setViewName("parametric/addCounterpartyGenericContracts");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addCounterpartyGenericContracts")
    public ModelAndView createNewCounterpartyGenericContracts(@ModelAttribute CounterpartyGenericContracts counterpartyGenericContracts) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/counterpartyGenericContracts");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (counterpartyGenericContractsService.findCounterpartyGenericContractsbyId(counterpartyGenericContracts.getId()).size() == 0) {
                if (validarRegistro(modelAndView, counterpartyGenericContracts)) {
                    counterpartyGenericContractsService.saveCounterpartyGenericContracts(counterpartyGenericContracts);
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

    @GetMapping(value = "/parametric/removeCounterpartyGenericContracts/{id}")
    @ResponseBody
    public boolean removeCounterpartyGenericContracts(@PathVariable Integer id,@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/counterpartyGenericContracts");
        boolean response=false;
        try {
            CounterpartyGenericContracts toRemove = counterpartyGenericContractsService.findCounterpartyGenericContractsbyId(id).get(0);
            counterpartyGenericContractsService.removeCounterpartyGenericContracts(toRemove.getId());
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/clearCounterpartyGenericContracts")
    public ModelAndView clearCounterpartyGenericContracts(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        counterpartyGenericContractsService.clearCounterpartyGenericContracts(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/counterpartyGenericContracts");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/counterpartyGenericContracts/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ContrapartidaYContratosGenericos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<CounterpartyGenericContracts> counterpartyGenericContractsList= new ArrayList<CounterpartyGenericContracts>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            counterpartyGenericContractsList = counterpartyGenericContractsService.findAll();
        }
        else{
            counterpartyGenericContractsList = counterpartyGenericContractsService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        CounterpartyGenericContractsListReport listReport = new CounterpartyGenericContractsListReport(counterpartyGenericContractsList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchCounterpartyGenericContracts")
    @ResponseBody
    public ModelAndView searchCounterpartyGenericContracts(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<CounterpartyGenericContracts> list=counterpartyGenericContractsService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<CounterpartyGenericContracts> pageCounterpartyGenericContracts = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageCounterpartyGenericContracts.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allCounterpartyGenericContracts",pageCounterpartyGenericContracts.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchCounterpartyGenericContracts");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/counterpartyGenericContracts");
        return modelAndView;
    }

    private Boolean validarRegistro(ModelAndView modelAndView, CounterpartyGenericContracts counterpartyGenericContracts){
        boolean salida = true;
        if (counterpartyGenericContracts.getCuenta().length() < 4 || counterpartyGenericContracts.getCuenta().length() > 18){
            modelAndView.addObject("resp", "ErrorCuentaTamanio9");
            salida = false;
        }
        else{
            try{
                Double.parseDouble(counterpartyGenericContracts.getCuenta());
            } catch (Exception e){
                modelAndView.addObject("resp", "ErrorCuentaCaracteres");
                salida = false;
            }
        }
        if (counterpartyGenericContracts.getContrato().length() != 18){
            modelAndView.addObject("resp", "ErrorContratoTamanio18");
            salida = false;
        }
        else{
            try{
                Double.parseDouble(counterpartyGenericContracts.getContrato());
            } catch (Exception e){
                modelAndView.addObject("resp", "ErrorContratoCaracteres");
                salida = false;
            }
        }
        try{
            counterpartyGenericContracts.getSaldo();
        } catch (Exception e){
            modelAndView.addObject("resp", "ErrorSaldo");
            salida = false;
        }

        return salida;
    }
}
