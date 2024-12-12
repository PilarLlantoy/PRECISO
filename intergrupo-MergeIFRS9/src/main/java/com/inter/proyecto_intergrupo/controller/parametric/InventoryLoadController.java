package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class InventoryLoadController {
    private static final int PAGINATIONCOUNT=5;
    private static final int PAGINATIONCOUNTDATA=500;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    private static final String rutaArchivoFormato = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\DP10\\Preciso\\archivo.fmt";
    private static final String rutaArchivoFormato1 = "\\\\co.igrupobbva\\svrfilesystem\\BBVA_VIC06\\DP10\\Preciso\\";

    //private static final String rutaArchivoFormato = "D:\\archivo.fmt";
    //private static final String rutaArchivoFormato1 = "D:\\";

    @Autowired
    private UserService userService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @GetMapping(value="/parametric/inventoryLoad")
    public ModelAndView inventoryLoad(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Cargue Inventarios");
        if(userService.validateEndpoint(user.getId(),"Ver Cargue Inventarios")) {

            List<Conciliation> listConciliations = conciliationService.findAllActive();
            List<AccountingRoute> listCroutes = accountingRouteService.findAllActive();
            List<ConciliationRoute> listConcilroutes = conciliationRouteService.findAllActive();

            List<Object[]> croutes = new ArrayList<>();
            List<CampoRConcil> colAroutes = new ArrayList<>();
            List<LogInventoryLoad> logConcilroutes = new ArrayList<>();
            if(params.get("arhcont") != null && params.get("arhcont").toString() != null
                    &&
                    params.get("period") != null && params.get("period").toString() != null
                    &&
                    params.get("selectedConciliacion") != null && params.get("selectedConciliacion").toString() != null
            )
            {
                modelAndView.addObject("period",params.get("period").toString());

                ConciliationRoute cr = conciliationRouteService.findById(Integer.parseInt(params.get("arhcont").toString()));
                modelAndView.addObject("arhcont",cr);
                if(cr!=null && cr.getDetalle()!=null)
                    modelAndView.addObject("nomb",cr.getDetalle());
                else
                    modelAndView.addObject("nomb","Vacio");

                Conciliation con = conciliationService.findById(Integer.parseInt(params.get("selectedConciliacion").toString()));
                modelAndView.addObject("selectedConciliacion",con);
                //modelAndView.addObject("ConArch",con.getId());

                logConcilroutes = conciliationRouteService.findAllLog(cr,params.get("period").toString());
                croutes = conciliationRouteService.findAllData(cr,params.get("period").toString());
                CampoRConcil crc= new CampoRConcil();
                crc.setNombre("periodo_preciso");
                cr.getCampos().add(crc);
                colAroutes = cr.getCampos();
            }
            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), logConcilroutes.size());
            Page<LogInventoryLoad> pageLog= new PageImpl<>(logConcilroutes.subList(start, end), pageRequest, logConcilroutes.size());

            int totalPage=pageLog.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }

            int pageData=params.get("pageData")!=null?(Integer.valueOf(params.get("pageData").toString())-1):0;
            PageRequest pageRequestData=PageRequest.of(pageData,PAGINATIONCOUNTDATA);
            int startData = (int) pageRequestData.getOffset();
            int endData = Math.min((startData + pageRequestData.getPageSize()), croutes.size());
            Page<Object[]> pageLogData= new PageImpl<>(croutes.subList(startData, endData), pageRequestData, croutes.size());

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
            modelAndView.addObject("listRouteCont",listCroutes);
            modelAndView.addObject("listRouteConcil",listConcilroutes);
            modelAndView.addObject("listConciliations",listConciliations);
            modelAndView.addObject("directory","inventoryLoad");
            modelAndView.addObject("registers",logConcilroutes.size());
            modelAndView.addObject("registersData",croutes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/inventoryLoad");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping("/parametric/inventoryLoad/leerArchivoS")
    @ResponseBody
    public ResponseEntity<String> leerArchivoServidor(@RequestParam int id, @RequestParam String fecha) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ConciliationRoute cr = conciliationRouteService.findById(id);
        try {
            Hibernate.initialize(cr.getCampos());
            conciliationRouteService.createTableTemporal(cr);
            conciliationRouteService.generarArchivoFormato(cr.getCampos(), rutaArchivoFormato);
            if(cr.getTipoArchivo().equalsIgnoreCase("XLS") || cr.getTipoArchivo().equalsIgnoreCase("XLSX"))
                conciliationRouteService.importXlsx(cr,rutaArchivoFormato,fecha,null);
            else
                conciliationRouteService.bulkImport(cr,rutaArchivoFormato,fecha,null);
            conciliationRouteService.validationData(cr);
            conciliationRouteService.copyData(cr,fecha);
            conciliationRouteService.loadLogCargue(user,cr,fecha,"Trasladar Servidor","Exitoso","");
            return ResponseEntity.ok("Bulk2");
        }
        catch (Exception e) {
            e.printStackTrace();
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause(); // Navega a la causa raíz
            }
            conciliationRouteService.loadLogCargue(user,cr,fecha,"Trasladar Servidor","Fallido",rootCause.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk-2");
        }
    }

    @PostMapping("/parametric/inventoryLoad/leerArchivoL")
    @ResponseBody
    public ResponseEntity<String> leerArchivoLocal(@RequestParam String id, @RequestParam String fecha, @RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ConciliationRoute cr = conciliationRouteService.findById(Integer.parseInt(id));
        String rutaArchivo = rutaArchivoFormato1 + file.getOriginalFilename();
        try {
            File dest = new File(rutaArchivo);
            file.transferTo(dest);
            conciliationRouteService.createTableTemporal(cr);
            conciliationRouteService.generarArchivoFormato(cr.getCampos(), rutaArchivoFormato);
            if(cr.getTipoArchivo().equalsIgnoreCase("XLS") || cr.getTipoArchivo().equalsIgnoreCase("XLSX"))
                conciliationRouteService.importXlsx(cr,rutaArchivoFormato,fecha,rutaArchivo);
            else
                conciliationRouteService.bulkImport(cr,rutaArchivoFormato,fecha,rutaArchivo);
            conciliationRouteService.validationData(cr);
            conciliationRouteService.copyData(cr,fecha);
            conciliationRouteService.loadLogCargue(user,cr,fecha,"Trasladar Local","Exitoso","");
            return ResponseEntity.ok("Bulk2");
        }
        catch (Exception e) {
            e.printStackTrace();
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause(); // Navega a la causa raíz
            }
            conciliationRouteService.loadLogCargue(user,cr,fecha,"Trasladar Local","Fallido",rootCause.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk-2");
        }
    }

    @Scheduled(cron = "0 0/30 * * * ?")
    @Transactional
    public void jobLeerArchivos() {
        LocalDateTime fechaOrigen = LocalDateTime.now();
        LocalDateTime fechaHoy = fechaOrigen.minusDays(1);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fecha = fechaHoy.format(formato);

        List<ConciliationRoute> list = conciliationRouteService.findByJob();
        for (ConciliationRoute cr :list)
        {
            try {
                Hibernate.initialize(cr.getCampos());
                conciliationRouteService.createTableTemporal(cr);
                conciliationRouteService.generarArchivoFormato(cr.getCampos(), rutaArchivoFormato);
                if(cr.getTipoArchivo().equalsIgnoreCase("XLS") || cr.getTipoArchivo().equalsIgnoreCase("XLSX"))
                    conciliationRouteService.importXlsx(cr,rutaArchivoFormato,fecha,null);
                else
                    conciliationRouteService.bulkImport(cr,rutaArchivoFormato,fecha,null);
                conciliationRouteService.validationData(cr);
                conciliationRouteService.copyData(cr,fecha);
                conciliationRouteService.loadLogCargue(null,cr,fecha,"Automático","Exitoso","");
            }
            catch (Exception e) {
                e.printStackTrace();
                Throwable rootCause = e;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause(); // Navega a la causa raíz
                }
                conciliationRouteService.loadLogCargue(null,cr,fecha,"Automático","Fallido",rootCause.getMessage());
            }
        }
    }

    @GetMapping(value = "/parametric/inventoryLoad/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, @RequestParam(defaultValue = "0") String id, @RequestParam(defaultValue = "0") String fecha) throws IOException {
        response.setContentType("application/octet-stream");
        ConciliationRoute cr = conciliationRouteService.findById(Integer.parseInt(id));
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename="+cr.getNombreArchivo().replace(" ","_") + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> croutes = conciliationRouteService.findAllData(cr,fecha);
        List<CampoRConcil> colCroutes = cr.getCampos();
        CampoRConcil crc= new CampoRConcil();
        crc.setNombre("periodo_preciso");
        cr.getCampos().add(crc);
        InventoryLoadListReport listReport = new InventoryLoadListReport(croutes,colCroutes,cr);
        listReport.export(response);
    }



}
