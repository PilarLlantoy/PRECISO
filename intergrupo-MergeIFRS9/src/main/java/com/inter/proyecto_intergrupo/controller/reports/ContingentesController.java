package com.inter.proyecto_intergrupo.controller.reports;

import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.reports.*;
import com.inter.proyecto_intergrupo.model.temporal.CorepTeporalH;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.model.parametric.Contract;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelAccounts;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.IntergrupoContingentesService;
import com.inter.proyecto_intergrupo.service.parametricServices.ContractService;
import com.inter.proyecto_intergrupo.service.parametricServices.CountryService;
import com.inter.proyecto_intergrupo.service.parametricServices.GarantBankService;
import com.inter.proyecto_intergrupo.service.parametricServices.YntpSocietyService;
import com.inter.proyecto_intergrupo.service.reportsServices.ContingentesConListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.ContingentesListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.ContingentesService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ContingentesController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ContingentesService contingentesService;

    @Autowired
    private IntergrupoContingentesService intergrupoContingentesService;

    @Autowired
    private YntpSocietyService yntpSocietyService;

    @Autowired
    private GarantBankService garantBankService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @Autowired
    private ControlPanelService controlPanelService;

    @GetMapping(value="/reports/contingentesAvalType")
    public ModelAndView showContingentesAval(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Contingentes Contratos")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<Contract> pageContract = contractService.getAll(pageRequest);
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
            modelAndView.addObject("directory", "contingentesAvalType");
            modelAndView.addObject("registers",pageContract.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("reports/contingentesAvalType");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value="/reports/contingentReclassification")
    public ModelAndView showContingentesR(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<Contract> pageContract=contractService.getAll(pageRequest);
        int totalPage=pageContract.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allContract",pageContract.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","contingentesAvalType");
        modelAndView.addObject("registers",pageContract.getTotalPages());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("button","Query13");
        modelAndView.setViewName("reports/contingentReclassification");
        return modelAndView;
    }

    @PostMapping(value="/reports/contingentesAvalType")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/contingentesAvalType");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        //List<ContingentTemplate> contingentesList = contingentesService.findAll();
        List<YntpSociety> yntpList=yntpSocietyService.findAll();
        List<GarantBank> bancoList=garantBankService.findAll();

        try {
            Collection<Part> parts = request.getParts();
            List<String[]> list = contingentesService.saveFileBD(parts, user);
            ContingentesListReport listReport = new ContingentesListReport(null, null, yntpList, bancoList,null,null,null,null,null,null);
            listReport.exportLog(response, list, "TipoAval");

        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "LoadDoc-1");
        }

        return  modelAndView;

    }

    @PostMapping(value="/reports/contingentesAvalTypeCred")
    public ModelAndView uploadFileCred(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/contingentesAvalType");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ContingentTemplate> contingentesList = contingentesService.findAll();
        List<YntpSociety> yntpList=yntpSocietyService.findAll();
        List<GarantBank> bancoList=garantBankService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            List<String[]> list=contingentesService.saveFileBDCred(fileContent,user);
            ContingentesListReport listReport = new ContingentesListReport(contingentesList,null,yntpList,bancoList,null,null,null,null,null, null);
            listReport.exportLog(response,list,"AvalesCRED");

        }catch(Exception e){
            e.printStackTrace();
            return  modelAndView;
        }
        return  modelAndView;
    }

    @GetMapping(value="/reports/contingentes")
    public ModelAndView showContingentes(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Contingentes Plantilla")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";
            if (params.get("period") == null || params.get("period").toString() == "") {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
                } else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
                }
            } else {
                todayString = params.get("period").toString();
            }
            List<Object[]> list = contingentesService.getAllReportResumen(todayString);
            List<Object[]> companies = contingentesService.getCompany(todayString);
            List<Object[]> companiesIfrs = contingentesService.getCompanyIfrs(todayString);
            List<ControlPanel> listControl = controlPanelService.findByFechaReporteContingentes(todayString, user);

            int countFull = 0;

            for (int i = 0; i < listControl.size(); i++) {
                if (listControl.get(i).getSemaforoComponente().equals("FULL")) {
                    countFull++;
                }
            }

            if (countFull >= 2) {
                modelAndView.addObject("statusComponent", true);
            } else {
                modelAndView.addObject("statusComponent", false);
            }

            boolean noQuery = false;
            if (companies.size() == 0) {
                noQuery = false;
            } else {
                noQuery = true;
            }

            boolean noQueryIfrs = false;
            if (companiesIfrs.size() == 0) {
                noQueryIfrs = false;
            } else {
                noQueryIfrs = true;
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageContingentes = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageContingentes.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allContingentes", pageContingentes.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("noQuery", noQuery);
            modelAndView.addObject("noQueryIfrs", noQueryIfrs);
            modelAndView.addObject("companies", companies);
            modelAndView.addObject("companiesIfrs", companiesIfrs);
            modelAndView.addObject("directory", "contingentes");
            String button = contingentesService.ShowButton(todayString, user.getCentro());
            modelAndView.addObject("button", button);
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("registers",list.size());

            modelAndView.setViewName("reports/contingentes");

            List<Object[]> validateSicc = contingentesService.validateSicc(todayString);

            if (validateSicc.size() != 0) {
                modelAndView.addObject("validateSicc", true);
                Date dateSicc = new Date();
                try {
                    dateSicc = StatusInfoRepository.findByInputAndPeriodo("CONTINGENTES-SICC", todayString).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateSicc);
                modelAndView.addObject("dateSicc", dateAsString);
            } else {
                modelAndView.addObject("validateSicc", false);
            }
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/reports/contingentes")
    public ModelAndView uploadFileContin(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/contingentes");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ContingentTemplate> contingentesList = contingentesService.findAll();
        List<YntpSociety> yntpList=yntpSocietyService.findAll();
        List<GarantBank> bancoList=garantBankService.findAll();
        List<Country> countryList=countryService.findAll();

        boolean validateDivisas = contingentesService.validateDivisas(params.get("period").toString());
        if(validateDivisas) {
            try {
            List<Object[]> validateSicc = contingentesService.validateSicc(params.get("period").toString());

            if (validateSicc.size() != 0) {
                modelAndView.addObject("validateSicc", true);
                Date dateSicc = new Date();
                try {
                    dateSicc = StatusInfoRepository.findByInputAndPeriodo("CONTINGENTES-SICC", params.get("period").toString()).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateSicc);
                modelAndView.addObject("dateSicc", dateAsString);
            } else {
                modelAndView.addObject("validateSicc", false);
            }

                Collection<Part> parts = request.getParts();
                String month = params.get("period").toString();
                modelAndView.addObject("period", month);
                parts.remove(request.getPart("period"));
                ContingentesListReport listReport = new ContingentesListReport(contingentesList, null, yntpList, bancoList,null,null,null,null,null,countryList);
                List<String[]> list = contingentesService.saveFilePlantilla(parts, month, user);
                if(list.get(list.size() - 1)[3].equals("Plantilla")||list.get(list.size() - 1)[3].equals("PLANTILLA"))
                {
                    listReport.exportLog(response, list, "Plantilla");
                    modelAndView.addObject("resp", "contAdd");
                }
                else if(list.get(list.size() - 1)[3].equals("BANCO"))
                {
                    listReport.exportLog(response, list, "Banco");
                }
                else if(list.get(list.size() - 1)[3].equals("CONTRATO")) {
                    listReport.exportLog(response, list, "Contrato");
                }
                else if(list.get(list.size() - 1)[3].equals("ERROR")) {
                    modelAndView.addObject("resp", "RP21-2");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                modelAndView.addObject("resp", "ContingentesLoadFail");
                return modelAndView;
            }
        }else{
            modelAndView.addObject("resp", "ContingentesLoadFailDivisa");
        }
        return  modelAndView;
    }

    @PostMapping(value="/reports/contingentesSociedades")
    public ModelAndView uploadFileContinSociedades(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/contingentes");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> contractsOffBalance;
        contractsOffBalance=contingentesService.contractsOffBalance();
        List<Object[]> yntpList=yntpSocietyService.findAllCeros();
        List<GarantBank> bancoList=garantBankService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ContingentesConListReport listReport = new ContingentesConListReport(contractsOffBalance,yntpList,bancoList);
            ArrayList<String[]> list=contingentesService.saveFileBDCon(fileContent,user);

            if(list.size()==0){
                modelAndView.addObject("resp", "AddConCon1");
            }else{
                listReport.exportLog(response,list);
            }


        }catch(Exception e){
            e.printStackTrace();
        }
        finally {
            return  modelAndView;
        }
    }


    @GetMapping(value = "/reports/contingentes/DownloadSociedades")
    @ResponseBody
    public void exportToExcelContractCon(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        List<Object[]> contractsOffBalance;
        contractsOffBalance=contingentesService.contractsOffBalance();

        List<Object[]> yntpList=yntpSocietyService.findAllCeros();
        List<GarantBank> bancoList=garantBankService.findAll();

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ContratosContingentes_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ContingentesConListReport listReport = new ContingentesConListReport(contractsOffBalance,yntpList,bancoList);
        listReport.export(response);
    }

    @RequestMapping(value = "/reports/contingentes/downloadArmado")
    @ResponseBody
    public void exportArmadoCargaMasiva(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        try {

            if(params.get("CmRec")!=null || params.get("CmRev")!=null) {
                response.setContentType("text/plain");
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String currentDateTime = dateFormatter.format(new Date());

                response.setHeader("Content-Disposition", "attachment;  filename=CargaMasivaContingentes_" + currentDateTime + ".csv");
                String[] partes = params.get("period").toString().split("-");

                contingentesService.getArmadoCargaMasiva(partes[1], partes[0]);
                List<Object[]> contingentList = contingentesService.getReclasficacionCSV(params.get("CmRec"), params.get("CmRev"), params.get("period").toString());
                ArrayList<String> contingentListNew = new ArrayList<>();
                for (Object row : contingentList) {
                    contingentListNew.add(row.toString());
                }
                contingentesService.updateArmadoIntergrupo(params.get("period").toString());

                CsvService.downloadCsvCargaMasiva(response.getWriter(), contingentListNew);

            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/reports/contingentes/download")
    @ResponseBody
    public void export(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        try
        {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Contingentes_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<ContingentTemplate> contingentList = contingentesService.getAllReport(params.get("period").toString());
            List<YntpSociety> yntpList=yntpSocietyService.findAll();
            List<GarantBank> bancoList=garantBankService.findAll();
            ContingentesListReport listReport = new ContingentesListReport(contingentList,null,yntpList,bancoList,null,null,null,null,null, null);
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/reports/contingentes/downloadReport")
    @ResponseBody
    public void exportReport(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Reporte Contingentes_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<Object[]> contingentList = contingentesService.getAllReportResumen(params.get("period").toString());
            List<YntpSociety> yntpList=yntpSocietyService.findAll();
            List<GarantBank> bancoList=garantBankService.findAll();
            ContingentesListReport listReport = new ContingentesListReport(null,contingentList,yntpList,bancoList,null,null,null,null,null, null);
            listReport.exportReport(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/reports/contingentes/validateQuery")
    @ResponseBody
    public ModelAndView validateQuery(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String todayString = "";
        if(params.get("period").toString()==null || params.get("period").toString()=="") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }
        String company = params.get("company").toString();
        String type = params.get("inventory").toString();
        //List<String[]> accounts = contingentesService.validateAccount13Query(todayString,user.getCentro(),company,type);
        List<Object[]> resultQuery = contingentesService.validateQueryGlobal(company,todayString,type);
        List<Object[]> companies=contingentesService.getCompany(todayString);
        List<Object[]> companiesIfrs=contingentesService.getCompanyIfrs(todayString);

        List<ControlPanel> listControl = controlPanelService.findByFechaReporteContingentes(todayString, user);

        int countFull = 0;

        for (int i = 0; i < listControl.size(); i++) {
            if (listControl.get(i).getSemaforoComponente().equals("FULL")) {
                countFull++;
            }
        }

        if (countFull >= 2) {
            modelAndView.addObject("statusComponent", true);
        } else {
            modelAndView.addObject("statusComponent", false);
        }

        boolean noQuery = false;
        if (companies.size() == 0){
            noQuery = false;
        }else{
            noQuery = true;
        }

        boolean noQueryIfrs = false;
        if (companiesIfrs.size() == 0){
            noQueryIfrs = false;
        }else{
            noQueryIfrs = true;
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=DifCont"+company+ type+"_"+todayString + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ControlPanelAccounts listReport = new ControlPanelAccounts(resultQuery);
        listReport.exportAccount(response,"LOCAL");

        modelAndView.addObject("pages",1);
        modelAndView.addObject("current",1);
        modelAndView.addObject("next",2);
        modelAndView.addObject("prev",0);
        modelAndView.addObject("last",1);
        modelAndView.addObject("directory","contingentesAvalType");
        modelAndView.addObject("noQuery",noQuery);
        modelAndView.addObject("noQueryIfrs",noQueryIfrs);
        modelAndView.addObject("companies",companies);
        modelAndView.addObject("companiesIfrs",companiesIfrs);
        modelAndView.addObject("period",todayString);

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());

        List<Object[]> validateSicc = contingentesService.validateSicc(params.get("period").toString());
        if (validateSicc.size() !=0) {
            modelAndView.addObject("validateSicc", true);
            Date dateSicc = new Date();
            try {
                dateSicc = StatusInfoRepository.findByInputAndPeriodo("CONTINGENTES-SICC", params.get("period").toString()).getFecha();
            }catch (Exception e){
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateSicc);
            modelAndView.addObject("dateSicc", dateAsString);
        } else {
            modelAndView.addObject("validateSicc", false);
        }

        return  modelAndView;
    }

    @RequestMapping(value = "/reports/contingentes/validateQueryIfrs")
    @ResponseBody
    public ModelAndView validateQueryIfrs(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String todayString = "";
        if(params.get("period").toString()==null || params.get("period").toString()=="") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }
        String company = params.get("companyIfrs").toString();

        //List<String[]> accounts = contingentesService.validateAccount13Query(todayString,user.getCentro(),company,type);
        List<Object[]> resultQuery = contingentesService.validateQueryGlobalIfrs(company,todayString);
        List<Object[]> companies=contingentesService.getCompany(todayString);
        List<Object[]> companiesIfrs=contingentesService.getCompanyIfrs(todayString);
        List<ControlPanel> listControl = controlPanelService.findByFechaReporteContingentes(todayString, user);

        int countFull = 0;

        for (int i = 0; i < listControl.size(); i++) {
            if (listControl.get(i).getSemaforoComponente().equals("FULL")) {
                countFull++;
            }
        }

        if (countFull >= 2) {
            modelAndView.addObject("statusComponent", true);
        } else {
            modelAndView.addObject("statusComponent", false);
        }

        boolean noQuery = false;
        if (companies.size() == 0){
            noQuery = false;
        }else{
            noQuery = true;
        }

        boolean noQueryIfrs = false;
        if (companiesIfrs.size() == 0){
            noQueryIfrs = false;
        }else{
            noQueryIfrs = true;
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=DifCont"+company+"COREP_"+todayString + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ControlPanelAccounts listReport = new ControlPanelAccounts(resultQuery);
        listReport.exportAccount(response,"IFRS9");

        modelAndView.addObject("pages",1);
        modelAndView.addObject("current",1);
        modelAndView.addObject("next",2);
        modelAndView.addObject("prev",0);
        modelAndView.addObject("last",1);
        modelAndView.addObject("directory","contingentesAvalType");
        modelAndView.addObject("noQuery",noQuery);
        modelAndView.addObject("noQueryIfrs",noQueryIfrs);
        modelAndView.addObject("companies",companies);
        modelAndView.addObject("companiesIfrs",companiesIfrs);
        modelAndView.addObject("period",todayString);

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());

        List<Object[]> validateSicc = contingentesService.validateSicc(params.get("period").toString());
        if (validateSicc.size() !=0) {
            modelAndView.addObject("validateSicc", true);
            Date dateSicc = new Date();
            try {
                dateSicc = StatusInfoRepository.findByInputAndPeriodo("CONTINGENTES-SICC", params.get("period").toString()).getFecha();
            }catch (Exception e){
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateSicc);
            modelAndView.addObject("dateSicc", dateAsString);
        } else {
            modelAndView.addObject("validateSicc", false);
        }

        return  modelAndView;
    }

    @RequestMapping(value = "/reports/contingentes/downloadCOREP")
    @ResponseBody
    public void exportCOREP(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=COREP_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<Corep> corep = intergrupoContingentesService.sqlinsertCorep(params.get("period").toString(),params.get("omitir").toString(),params.get("incluirR").toString(),params.get("incluirP").toString(),params.get("tcliente").toString());
            List<Object[]> corepTemp = intergrupoContingentesService.getTemporalCorep(params.get("period").toString());
            List<Object[]> corepTempSub = intergrupoContingentesService.getTemporalCorepSecond(params.get("period").toString());
            List<ReclassificationContingent> contingentList = contingentesService.getReclasficacion();
            ContingentesListReport listReport = new ContingentesListReport(null,null,null,null,null,contingentList,corep,null,null, null);
            listReport.exportCorep(response,corepTemp,corepTempSub);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/reports/contingentes/downloadPlano")
    @ResponseBody
    public void exportPlano(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
            String currentDateTime = dateFormatter.format(new Date());
            if(params.get("extension").toString().equals("txt"))
                response.setHeader("Content-Disposition", "attachment;  filename=CCMOL_D02_"+currentDateTime+"_udarvrecfi_reclas_finanzas.txt");
            else
                response.setHeader("Content-Disposition", "attachment;  filename=RECLASIFICACION_CONTA_"+currentDateTime+".txt");
            List<Object[]> contingentList =contingentesService.getPlanoReclasficacion(params.get("period").toString());
            ArrayList<String> contingentListNew = new ArrayList<>();
            for (Object row : contingentList){
                contingentListNew.add(row.toString());
            }
            CsvService.downloadCsvPlanoRecla(response.getWriter(), contingentListNew);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/reports/contingentes/downloadArmadoIntergrupo")
    @ResponseBody
    public void exportInter(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params)
    {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=ArmadoIntergrupo_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<ContingentIntergroup> contingentList =contingentesService.getArmadoIntergrupo(params.get("period").toString());
            ContingentesListReport listReport = new ContingentesListReport(null,null,null,null,null,null,null,null,contingentList, null);
            listReport.exportIntergrupo(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    @GetMapping(value="/reports/contingentes/uploadSicc")
    public ModelAndView uploadFileSicc(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/contingentes");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("period", params.get("period"));
        try
        {
            boolean list = contingentesService.updateSicc(params.get("period").toString(),user);

            if (list) {
                modelAndView.addObject("resp", "updateS");
            } else {
                modelAndView.addObject("resp", "updateSF");
            }

            List<Object[]> validateSicc = contingentesService.validateSicc(params.get("period").toString());
            if (validateSicc.size() !=0) {
                modelAndView.addObject("validateSicc", true);
                Date dateSicc = new Date();
                try {
                    dateSicc = StatusInfoRepository.findByInputAndPeriodo("CONTINGENTES-SICC", params.get("period").toString()).getFecha();
                }catch (Exception e){
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateSicc);
                modelAndView.addObject("dateSicc", dateAsString);
            } else {
                modelAndView.addObject("validateSicc", false);
            }

        }
        catch(Exception e){
            modelAndView.addObject("resp", "updateSF");
            e.printStackTrace();
        }
        return  modelAndView;
    }
    @RequestMapping(value = "/reports/contingentes/downloadInvenReclasificationCom")
    @ResponseBody
    public void exportReclasification(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params)
    {
        try
        {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Tablon" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<ReclassificationContingentCom> contingentList =contingentesService.getReclasification(params.get("period").toString());
            ContingentesListReport listReport = new ContingentesListReport(null,null,null,null,contingentList,null,null,null,null, null);
            listReport.exportReclasificacionCom(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping(value="/reports/contingentes/sendIntergrupo")
    public ModelAndView sendIntergroup(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        String todayString="";
        if(params.get("period") == null || params.get("period").toString()=="") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }
        List<Object[]> list=contingentesService.getAllReportResumen(todayString);
        List<Object[]> companies=contingentesService.getCompany(todayString);
        List<Object[]> companiesIfrs = contingentesService.getCompanyIfrs(todayString);

        List<ControlPanel> listControl = controlPanelService.findByFechaReporteContingentes(todayString, user);

        int countFull = 0;

        for (int i = 0; i < listControl.size(); i++) {
            if (listControl.get(i).getSemaforoComponente().equals("FULL")) {
                countFull++;
            }
        }

        if (countFull >= 2) {
            modelAndView.addObject("statusComponent", true);
        } else {
            modelAndView.addObject("statusComponent", false);
        }

        boolean noQuery = false;
        if (companies.size() == 0) {
            noQuery = false;
        } else {
            noQuery = true;
        }

        boolean noQueryIfrs = false;
        if (companiesIfrs.size() == 0) {
            noQueryIfrs = false;
        } else {
            noQueryIfrs = true;
        }

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Object[]> pageContingentes = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageContingentes.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allContingentes",pageContingentes.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("vFilter",todayString);
        modelAndView.addObject("noQuery",noQuery);
        modelAndView.addObject("noQueryIfrs", noQueryIfrs);
        modelAndView.addObject("companies", companies);
        modelAndView.addObject("companiesIfrs", companiesIfrs);
        modelAndView.addObject("directory","contingentes");
        String button = contingentesService.ShowButton(todayString,user.getCentro());
        modelAndView.addObject("button",button);
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());

        List<Object[]> validateSicc = contingentesService.validateSicc(todayString);

        contingentesService.sendIntergrupo(todayString);

        if (validateSicc.size() !=0) {
            modelAndView.addObject("validateSicc", true);
            Date dateSicc = new Date();
            try {
                dateSicc = StatusInfoRepository.findByInputAndPeriodo("CONTINGENTES-SICC", todayString).getFecha();
            }catch (Exception e){
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateSicc);
            modelAndView.addObject("dateSicc", dateAsString);
        } else {
            modelAndView.addObject("validateSicc", false);
        }

        modelAndView.addObject("resp", "IRP-2");
        modelAndView.addObject("statusComponent", true);

        modelAndView.setViewName("reports/contingentes");

        return modelAndView;
    }

}
