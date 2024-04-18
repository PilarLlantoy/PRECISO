package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Contract;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
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
public class ContractController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private YntpSocietyService yntpSocietyService;

    @Autowired
    private GarantBankService garantBankService;

    @Autowired
    private AvalTypeService avalTypeService;


    private List<String> listColumns=List.of("Contrato", "Origen", "Banco","Tipo Aval","Tipo Aval Origen","Tipo Proceso","Moneda","País");

    @GetMapping(value="/parametric/contract")
    public ModelAndView showContract(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Histórico de Contratos")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            List<Object> list = contractService.getAllJoin();

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object> pageContract = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageContract.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allContract", pageContract.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "contract");
            modelAndView.addObject("registers",list.size());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/contract");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/contract")
    public ModelAndView uploadFileContract(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/contract");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Contract> contractList = contractService.findAll();
        //List<Object[]> yntpList=yntpSocietyService.findAllCeros();
        List<GarantBank> bancoList=garantBankService.findAll();
        List<Country> countryList=countryService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ContractListReport listReport = new ContractListReport(contractList,null,null,bancoList,countryList);
            ArrayList<String[]> list=contractService.saveFileBD(fileContent,user, null, bancoList, countryList);
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
            else if(part[2].equals("fail"))
            {
                int rowTemp=(Integer.parseInt(part[0])+1);
                part[0]=String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRepCont-1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                modelAndView.addObject("message", part[3]);
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

    @GetMapping(value = "/parametric/modifyContract/{id}")
    @ResponseBody
    public ModelAndView modifyContract(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        List<Object[]> toModify = contractService.findContractJoin(id);
        modelAndView.addObject("contractModify",toModify);
        List <Country> allCountry = countryService.findAll();
        modelAndView.addObject("paises",allCountry);
        List <Currency> allCurrency = currencyService.findAll();
        modelAndView.addObject("divisas",allCurrency);
        List <GarantBank> allSociety = garantBankService.findAll();
        modelAndView.addObject("sociedades",allSociety);
        List <Object[]> allAval = avalTypeService.findAllDistinct();
        modelAndView.addObject("avales",allAval);
        modelAndView.setViewName("parametric/modifyContract");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyContract")
    @ResponseBody
    public ModelAndView updateContract(@ModelAttribute Contract contract,@RequestParam String idOld,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/contract");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String id_contrato_old = params.get("idOld").toString();
        String id_contrato = params.get("idNew").toString();
        String origen = params.get("origen").toString();
        String tipo_aval = params.get("tipoAval").toString();
        String yntp = params.get("yntpSelect").toString();
        String tipo_proceso = params.get("tipoProceso").toString();
        String pais = params.get("paisSelect").toString();

        try {
            Contract searchContract = contractService.findContract(String.valueOf(contract.getContrato()));
            if (searchContract==null||idOld.equals(searchContract.getContrato()))
            {
                contractService.modifyContractN(id_contrato_old, id_contrato, origen, tipo_aval, yntp, tipo_proceso, pais, idOld,user);
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

    @GetMapping(value = "/parametric/validateIdContract")
    @ResponseBody
    public String validateIdContract(@RequestParam String idNew,@RequestParam String idT){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(contractService.findContract(idNew)==null||idNew.equals(idT))
            result="valid";
        return  result;
    }

    @GetMapping(value = "/parametric/removeContract/{id}")
    @ResponseBody
    public boolean removeContract(@PathVariable String id,@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/contract");
        User user = userService.findUserByUserName(auth.getName());
        boolean response=false;
        try {
            Contract toRemove = contractService.findContract(id);
            contractService.removeContract(String.valueOf(toRemove.getContrato()),user);
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  response;

    }

    @GetMapping(value = "/parametric/clearContract")
    @ResponseBody
    public boolean clearContract(){
        boolean response=false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/contract");
        try{
            contractService.clearContract(user);
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/contract/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        List<Object[]> yntpList=yntpSocietyService.findAllCeros();
        List<GarantBank> bancoList=garantBankService.findAll();
        List<Country> countryList=countryService.findAll();
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Contrato_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Contract> contractList= new ArrayList<Contract>();
        List<Object[]> contractListF= new ArrayList<Object[]>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            contractList = contractService.findAll();
        }
        else{
            contractListF = contractService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ContractListReport listReport = new ContractListReport(contractList,contractListF,yntpList,bancoList, countryList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchContract")
    @ResponseBody
    public ModelAndView searchContract(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Object[]> list=contractService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Object[]> pageContract = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageContract.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allContract",pageContract.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchContract");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/contract");
        return modelAndView;
    }
}
