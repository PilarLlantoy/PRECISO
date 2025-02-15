package com.inter.proyecto_intergrupo.controller.parametric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountingLoadListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountingRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.JobAutoService;
import com.inter.proyecto_intergrupo.service.parametricServices.SourceSystemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@EnableScheduling
public class AccountingLoadController {
    private static final int PAGINATIONCOUNT=5;
    private static final int PAGINATIONCOUNTDATA=500;
    public static final String rutaArchivoFormato = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\DP10\\Preciso\\Temporales\\archivo.fmt";
    public static final String rutaArchivoFormato1 = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\DP10\\Preciso\\Temporales\\";
    //public static final String rutaArchivoFormato = "D:\\archivo.fmt";
    //public static final String rutaArchivoFormato1 = "D:\\";

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private JobAutoService jobAutoService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @GetMapping(value="/parametric/accountingLoad")
    public ModelAndView showAccountingLoad(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Cargue Contable");
        if(userService.validateEndpoint(user.getId(),"Ver Cargue Contable")) {

            List<AccountingRoute> listAroutes = accountingRouteService.findAllActive();
            List<Object[]> aroutes = new ArrayList<>();
            List<CampoRC> colAroutes = new ArrayList<>();
            List<LogAccountingLoad> logAroutes = new ArrayList<>();
            if(params.get("arhcont") != null && params.get("arhcont").toString() != null && params.get("period") != null && params.get("period").toString() != null)
            {
                modelAndView.addObject("period",params.get("period").toString());
                AccountingRoute ac = accountingRouteService.findById(Integer.parseInt(params.get("arhcont").toString()));
                modelAndView.addObject("arhcont",ac);
                if(ac!=null && ac.getNombre()!=null)
                    modelAndView.addObject("nomb",ac.getNombre());
                else
                    modelAndView.addObject("nomb","Vacio");

                List<CampoRC> campos =accountingRouteService.findByCampoVisual(ac);

                logAroutes = accountingRouteService.findAllLog(ac,params.get("period").toString());
                aroutes = accountingRouteService.processList(accountingRouteService.findAllData(ac,params.get("period").toString(), null, null,campos),campos);

                CampoRC crc= new CampoRC();
                crc.setNombre("periodo_preciso");
                campos.add(crc);
                colAroutes = campos;
            }
            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), logAroutes.size());
            Page<LogAccountingLoad> pageLog= new PageImpl<>(logAroutes.subList(start, end), pageRequest, logAroutes.size());

            int totalPage=pageLog.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }

            int pageData=params.get("pageData")!=null?(Integer.valueOf(params.get("pageData").toString())-1):0;
            PageRequest pageRequestData=PageRequest.of(pageData,PAGINATIONCOUNTDATA);
            int startData = (int) pageRequestData.getOffset();
            int endData = Math.min((startData + pageRequestData.getPageSize()), aroutes.size());
            Page<Object[]> pageLogData= new PageImpl<>(aroutes.subList(startData, endData), pageRequestData, aroutes.size());

            int totalPageData=pageLogData.getTotalPages();
            if(totalPageData>0){
                List<Integer> pagesData = IntStream.rangeClosed(1, totalPageData).boxed().collect(Collectors.toList());
                modelAndView.addObject("pagesData",pagesData);
            }

            modelAndView.addObject("allLog",pageLog.getContent());
            modelAndView.addObject("allRCs",pageLogData.getContent());
            modelAndView.addObject("allColRCs",colAroutes);
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("currentData",pageData+1);
            modelAndView.addObject("nextData",pageData+2);
            modelAndView.addObject("prevData",pageData);
            modelAndView.addObject("lastData",totalPageData);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("listRouteCont",listAroutes);
            modelAndView.addObject("directory","accountingLoad");
            modelAndView.addObject("registers",logAroutes.size());
            modelAndView.addObject("registersData",aroutes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/accountingLoad");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping("/parametric/accountingLoad/leerArchivoS")
    @ResponseBody
    public ResponseEntity<String> leerArchivoServidor(@RequestParam int id, @RequestParam String fecha) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        AccountingRoute ac = accountingRouteService.findById(id);
        try {
            accountingRouteService.createTableTemporal(ac);
            accountingRouteService.generarArchivoFormato(ac.getCampos(), rutaArchivoFormato);
            if(ac.getTipoArchivo().equalsIgnoreCase("XLS") || ac.getTipoArchivo().equalsIgnoreCase("XLSX"))
                accountingRouteService.importXlsx(ac,rutaArchivoFormato,fecha,null);
            else
                accountingRouteService.bulkImport(ac,rutaArchivoFormato,fecha,null);
            if(ac.getCondiciones().size()!=0)
                accountingRouteService.conditionData(ac);
            if(ac.getValidaciones().size()!=0)
                accountingRouteService.validationData(ac);
            accountingRouteService.copyData(ac,fecha);
            accountingRouteService.updateLoads(ac,fecha);
            if(accountingRouteService.findAllDataValidation(ac,fecha)) {
                jobAutoService.loadLogCargue(user, ac, fecha, "Cargar Manual", "Exitoso", "");
                return ResponseEntity.ok("Bulk1");
            }
            else if(accountingRouteService.findAllDataTemporal(ac,fecha)) {
                jobAutoService.loadLogCargue(user, ac, fecha, "Cargar Manual", "Fallido", "La ruta "+ac.getRuta()+" es inaccesible. (El sistema no puede encontrar el archivo especificado)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk-1");
            }
            else {
                jobAutoService.loadLogCargue(user, ac, fecha, "Cargar Manual", "Fallido", "Valide el formato de los campos de tipo Float y Bigint");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk-1");
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause(); // Navega a la causa raíz
            }
            jobAutoService.loadLogCargue(user,ac,fecha,"Cargar Manual","Fallido",rootCause.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk-1");
        }
    }

    @PostMapping("/parametric/accountingLoad/leerArchivoL")
    @ResponseBody
    public ResponseEntity<String> leerArchivoLocal(@RequestParam String id, @RequestParam String fecha, @RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        AccountingRoute ac = accountingRouteService.findById(Integer.parseInt(id));
        String rutaArchivo = rutaArchivoFormato1 + file.getOriginalFilename();
        try {
            File dest = new File(rutaArchivo);
            file.transferTo(dest);
            accountingRouteService.createTableTemporal(ac);
            accountingRouteService.generarArchivoFormato(ac.getCampos(), rutaArchivoFormato);
            if(ac.getTipoArchivo().equalsIgnoreCase("XLS") || ac.getTipoArchivo().equalsIgnoreCase("XLSX"))
                accountingRouteService.importXlsx(ac,rutaArchivoFormato,fecha,rutaArchivo);
            else
                accountingRouteService.bulkImport(ac,rutaArchivoFormato,fecha,rutaArchivo);
            if(ac.getCondiciones().size()!=0)
                accountingRouteService.conditionData(ac);
            if(ac.getValidaciones().size()!=0)
                accountingRouteService.validationData(ac);
            accountingRouteService.copyData(ac,fecha);
            accountingRouteService.updateLoads(ac,fecha);
            if(accountingRouteService.findAllDataValidation(ac,fecha)) {
                jobAutoService.loadLogCargue(user, ac, fecha, "Cargar Archivo", "Exitoso", "");
                return ResponseEntity.ok("Bulk1");
            }
            else {
                jobAutoService.loadLogCargue(user, ac, fecha, "Cargar Archivo", "Fallido", "Valide el formato de los campos de tipo Float y Bigint");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk-1");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause(); // Navega a la causa raíz
            }
            jobAutoService.loadLogCargue(user,ac,fecha,"Cargar Archivo","Fallido",rootCause.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk-1");
        }
    }

    @GetMapping(value = "/parametric/accountingLoad/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response,@RequestParam(defaultValue = "0") String id, @RequestParam(defaultValue = "0") String fecha) throws IOException {
        response.setContentType("application/octet-stream");
        AccountingRoute ac = accountingRouteService.findById(Integer.parseInt(id));
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename="+ac.getNombre().replace(" ","_") + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        CampoRC crc= new CampoRC();
        crc.setNombre("periodo_preciso");
        List<CampoRC> colAroutes = accountingRouteService.findByCampoVisual(ac);
        List<Object[]> aroutes = accountingRouteService.findAllData(ac,fecha, null, null,colAroutes);
        ac.getCampos().add(crc);
        AccountingLoadListReport listReport = new AccountingLoadListReport(aroutes,colAroutes,ac);
        listReport.export(response);
    }

/*
    @GetMapping(value = "/parametric/createAccountingRoute")
    public ModelAndView showCreateAccountingRoute(){
        ModelAndView modelAndView = new ModelAndView();
        AccountingRoute aroute = new AccountingRoute();
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        modelAndView.addObject("allSFs", allSFs);
        modelAndView.addObject("aroute",aroute);
        modelAndView.setViewName("/parametric/createAccountingRoute");
        return modelAndView;
    }

    /*
    @GetMapping(value = "/parametric/modifyCountry/{id}")
    @ResponseBody
    public ModelAndView modifyCountry(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Country toModify = countryService.findCountryById(id);
        modelAndView.addObject("countryModify",toModify);
        modelAndView.addObject("paisId",toModify.getId());
        modelAndView.setViewName("parametric/modifyCountry");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyCountry")
    public ModelAndView updateCountry(@ModelAttribute Country pais){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
        countryService.modificarCountry(pais);
        return modelAndView;
    }

    @PostMapping(value = "/parametric/deleteCountry/{id}")
    public ModelAndView deleteCountry(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
        try {
            Country pais = countryService.findCountryById(id);
            pais.setEstado(false);
            countryService.modificarCountry(pais);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }

 */



    /*

            @GetMapping(value = "/parametric/validateIdCountry")
            @ResponseBody
            public String validateIdCountry(@RequestParam String idNew,@RequestParam String idT){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String result = "invalid";
                if(countryService.findCountryById(idNew)==null||idNew.equals(idT))
                    result="valid";
                return  result;
            }

            @GetMapping(value = "/parametric/removeCountry/{id}")
            @ResponseBody
            public boolean removeCountry(@PathVariable String id){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User user = userService.findUserByUserName(auth.getName());
                ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
                boolean response=false;
                try {
                    Country toRemove = countryService.findCountryById(id);
                    countryService.removeCountry(toRemove.getId()+"",user);
                    response=true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return  response;

            }

            @GetMapping(value = "/parametric/clearCountry")
            @ResponseBody
            public boolean clearCountry(){
                boolean response=false;
                ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User user = userService.findUserByUserName(auth.getName());
                try{
                    countryService.clearCountry(user);
                    response=true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return  response;
            }

            @GetMapping(value = "/parametric/country/download")
            @ResponseBody
            public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
                response.setContentType("application/octet-stream");
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String currentDateTime = dateFormatter.format(new Date());

                String headerKey = "Content-Disposition";
                String headerValue = "attachment; filename=País_" + currentDateTime + ".xlsx";
                response.setHeader(headerKey, headerValue);
                List<Country> countryList= new ArrayList<Country>();
                if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
                    countryList = countryService.findAll();
                }
                else{
                    countryList = countryService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
                }
                CountryListReport listReport = new CountryListReport(countryList);
                listReport.export(response);
            }

        */


/*

    @PostMapping(value = "/parametric/createAccountingRoute")
    public ModelAndView createAccountingRoute(
            @ModelAttribute AccountingRoute aroute,
            @RequestParam(name = "selectedSF") String sistFuente,
            @RequestParam(name = "selectedTipoArchivo") String tipoArch,
            @RequestParam(name = "selectedFormatoFecha") String formFecha,
            @RequestParam(name = "selectedIdiomaFecha") String idiomFecha,

            BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountingRoutes");
        AccountingRoute arouteExists = accountingRouteService.findById(aroute.getId());
        if(arouteExists != null){
            bindingResult
                    .rejectValue("pais", "error.pais",
                            "El pais ya se ha registrado");
        }
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createAccountingRoute");
        }else{
            SourceSystem SF = sourceSystemService.findByNombre(sistFuente);
            aroute.setSfrc(SF);
            aroute.setTipoArchivo(tipoArch);
            aroute.setFormatoFecha(formFecha);
            aroute.setIdiomaFecha(idiomFecha);
            accountingRouteService.modificar(aroute);
        }
        return modelAndView;

    }

    @GetMapping(value = "/parametric/modifyAccountingRoute/{id}")
    public ModelAndView modifyAccountingRoute(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        AccountingRoute aroute = accountingRouteService.findById(id);
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        modelAndView.addObject("allSFs", allSFs);
        modelAndView.addObject("aroute",aroute);
        modelAndView.setViewName("parametric/modifyAccountingRoute");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/fieldLoadingAccountingRoute/{id}")
    public ModelAndView cargueCampos(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        AccountingRoute aroute = accountingRouteService.findById(id);
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        modelAndView.addObject("allSFs", allSFs);
        modelAndView.addObject("aroute",aroute);
        CampoRC campoRC = new CampoRC();
        modelAndView.addObject("campoRC",campoRC);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        modelAndView.addObject("p_modificar", p_modificar);
        modelAndView.setViewName("parametric/fieldLoadingAccountingRoute");
        return modelAndView;
    }

*/


}
