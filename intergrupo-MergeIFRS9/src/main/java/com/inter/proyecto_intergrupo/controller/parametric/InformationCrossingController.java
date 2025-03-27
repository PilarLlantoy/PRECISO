package com.inter.proyecto_intergrupo.controller.parametric;
import com.inter.proyecto_intergrupo.model.admin.Cargo;
import org.springframework.transaction.annotation.Transactional;

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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@EnableScheduling
public class InformationCrossingController {
    private static final int PAGINATIONCOUNT=5;
    private static final int PAGINATIONCOUNTDATA=500;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private EventMatrixService eventMatrixService;

    @Autowired
    private InformationCrossingService informationCrossingService;

    @Autowired
    private CondicionMEService condicionMEService;

    @Autowired
    private ValidationMEService validationMEService;

    @Autowired
    private AccountEventMatrixService accountEventMatrixService;

    @GetMapping(value="/parametric/informationCrossing")
    public ModelAndView showinformationCrossing(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Cargue Contable");
        if(userService.validateEndpoint(user.getId(),"Ver Cargue Contable")) {

            List<AccountingRoute> listAroutes = accountingRouteService.findAllActive();
            List<Conciliation> listConcil = conciliationService.findAllActive();
            List<EventType> listTypeEvent = eventTypeService.findAllActive();

            List<Object[]> datos = new ArrayList<>();
            List<String> colDatos = new ArrayList<>();
            List<LogInformationCrossing> logCruces = new ArrayList<>();

            //El ultimo log de la fecha, conciliacion y tipo evento registrado
            Object[] ultLog = null;

            if(params.get("arhcont") != null && params.get("arhcont").toString() != null
                    && params.get("period") != null && params.get("period").toString() != null
                    && params.get("evento") != null && params.get("evento").toString() != null)
            {
                modelAndView.addObject("period",params.get("period").toString());
                Conciliation concil = conciliationService.findById(Integer.parseInt(params.get("arhcont").toString()));
                EventType evento = eventTypeService.findAllById(Integer.parseInt(params.get("evento").toString()));
                String fecha = params.get("period").toString();
                modelAndView.addObject("arhcont",concil);
                modelAndView.addObject("evento",evento);
                logCruces = informationCrossingService.findAllLog(concil,params.get("period").toString(), evento);

                ultLog = informationCrossingService.findLatestLog(params.get("period").toString(),concil.getId(), evento.getId());

                CampoRC crc= new CampoRC();
                crc.setNombre("periodo_preciso");
                colDatos =List.of("FECHA CONCILIACION", "CENTRO CONTABLE", "CUENTA CONTABLE","DIVISA","SALDO INVENTARIO", "TIPO EVENTO");
                datos = informationCrossingService.processList(informationCrossingService.findAllData(concil, fecha, evento), colDatos);
                colDatos =List.of("FECHA CONCILIACION", "CENTRO CONTABLE", "CUENTA CONTABLE","DIVISA","SALDO INVENTARIO");

                if(concil!=null && concil.getNombre()!=null)
                    modelAndView.addObject("nomb",concil.getNombre());
                else
                    modelAndView.addObject("nomb","Vacio");
            }
            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), logCruces.size());
            Page<LogInformationCrossing> pageLog= new PageImpl<>(logCruces.subList(start, end), pageRequest, logCruces.size());

            int totalPage=pageLog.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }

            int pageData=params.get("pageData")!=null?(Integer.valueOf(params.get("pageData").toString())-1):0;
            PageRequest pageRequestData=PageRequest.of(pageData,PAGINATIONCOUNTDATA);
            int startData = (int) pageRequestData.getOffset();
            int endData = Math.min((startData + pageRequestData.getPageSize()), datos.size());
            Page<Object[]> pageLogData= new PageImpl<>(datos.subList(startData, endData), pageRequestData, datos.size());

            int totalPageData=pageLogData.getTotalPages();
            if(totalPageData>0){
                List<Integer> pagesData = IntStream.rangeClosed(1, totalPageData).boxed().collect(Collectors.toList());
                modelAndView.addObject("pagesData",pagesData);
            }

            modelAndView.addObject("allLog",pageLog.getContent());
            modelAndView.addObject("allRCs",pageLogData.getContent());
            modelAndView.addObject("allColRCs",colDatos);
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
            modelAndView.addObject("listConcil",listConcil);
            modelAndView.addObject("listTypeEvent",listTypeEvent);
            modelAndView.addObject("directory","informationCrossing");
            modelAndView.addObject("registers",logCruces.size());
            modelAndView.addObject("registersData",datos.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.addObject("ultLog", ultLog);
            modelAndView.setViewName("parametric/informationCrossing");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }


    @PostMapping("/parametric/informationCrossing/generateAccount")
    @ResponseBody
    public ResponseEntity<String> generarCuentas(@RequestParam int id,
                                                 @RequestParam String fecha,
                                                 @RequestParam int evento) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        EventType tipoEvento = eventTypeService.findAllById(evento);

        //VALIDAR QUE LOS INVENTARIOS ESTEN SUBIDOS
        List<ConciliationRoute> listRoutes = conciliationRouteService.getRoutesByConciliation(id); //RUTAS CONCILIACIONES
        List<Object[]> croutes = new ArrayList<>();
        List<String> faltaCarga = new ArrayList<>();


        Object[] result = informationCrossingService.findLatestLog(fecha, id, evento);

        Boolean ultLog = result != null && result.length > 3 && result[3] instanceof Boolean
                ? (Boolean) result[3]
                : false;

        System.out.println("ES APLICADO "+ultLog);
        if(ultLog){
            return ResponseEntity.ok("Bulk->3");
        }

        List<LogInventoryLoad> logConcilroutes = new ArrayList<>();
        try {
            /*for (ConciliationRoute ruta : listRoutes) {
                croutes = conciliationRouteService.findAllData(ruta, fecha);
                System.out.println(ruta.getDetalle() + " " + croutes);
                if (croutes.isEmpty()) {
                    faltaCarga.add(ruta.getDetalle());
                }
            }
            if (!faltaCarga.isEmpty()) {
                String message = "falta cargar archivos: ";
                for (String error : faltaCarga)
                    message += (error) + " ";
                informationCrossingService.loadLogInformationCrossing(user, id, evento, fecha, "Generar Cuentas", "Fallido", message);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk->2");
            } else
                System.out.println("NO falta cargar archivos");*/


            //GENERAR CRUCE DE INVENTARIO
            //-----------------------------------------------------------------------------------
            for (ConciliationRoute ruta : listRoutes) {

                List<EventMatrix> matrices = eventMatrixService.findByConciliationxInventarioxTipoEvento(id, ruta.getId(), evento);
                /*if(matrices.isEmpty()){
                    System.out.println("NO se ha creado ninguna matriz");
                    informationCrossingService.loadLogCargue(user, id, fecha, "Generar cuentas", "Fallido",
                            "La conciliacion no tiene minguna matriz asociada");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk->2");
                }*/

                //Creamos tablas temporales con la data total
                informationCrossingService.creatTablaTemporalCruce(ruta, fecha);

                for (EventMatrix matriz : matrices) {
                    if(matriz.isEstado()){ //SOLO LAS MATRICES ACTIVAS
                        //Primero veremos las condiciones
                        List<CondicionEventMatrix> condiciones = condicionMEService.findByMatrizEvento(matriz);
                        String condicion = null;
                        if (condiciones.size() != 0)
                            condicion = informationCrossingService.conditionData(ruta, matriz);

                        //Completamos informacion de cruce
                        AccountEventMatrix cuenta1 = accountEventMatrixService.findByMatrizEventoTipo1(matriz);
                        AccountEventMatrix cuenta2 = accountEventMatrixService.findByMatrizEventoTipo2(matriz);
                        informationCrossingService.completarTablaCruce(ruta, fecha, tipoEvento, matriz, cuenta1, cuenta2, condicion);

                        //Realizamos las validaciones
                        List<ValidationME> validaciones = validationMEService.findByEventMatrix(matriz);
                        if (validaciones.size() != 0)
                            informationCrossingService.validationData(ruta, matriz, condicion);
                    }

                }

                //Agregamos estos registros a la tabla final
                //Creamos las tablas finales vacias de cada inventario con los campos agregados
                informationCrossingService.recreateTable(ruta, id, fecha);
                System.out.println("RUTA CONCILIACION "+ruta.getDetalle());

            }

            //SE LOGRO EL CRUCE
            conciliationService.generarTablaCruceCompleto_x_Conciliacion(id, fecha, evento);
            informationCrossingService.loadLogInformationCrossing(user, id, evento, fecha, "Generar Cuentas", "Exitoso", "");
            return ResponseEntity.ok("Bulk->1");
        }
        catch (Exception e) {
            e.printStackTrace();
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause(); // Navega a la causa raíz
            }
            informationCrossingService.loadLogInformationCrossing(user, id, evento, fecha, "Generar Cuentas", "Fallido",rootCause.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk->2");
        }

    }

    @PostMapping(value = "/parametric/confirmarParaConciliacion/{id}")
    public  ResponseEntity<Boolean>  confirmarParaConciliacion(@PathVariable int id) {
        System.out.println(id);

        try {
            if ( id==0) {
                System.out.println("Id incorrecto para cruce de informacion");
                return ResponseEntity.ok(false);
            }
            informationCrossingService.confirmarConciliacion(id);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            // Manejar excepciones
            System.out.println("Error al confirmar conciliacion: " + e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping(value = "/parametric/informationCrossing/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, @RequestParam(defaultValue = "0") String id, @RequestParam(defaultValue = "0") String fecha, @RequestParam(defaultValue = "0") String evento) throws IOException {
        response.setContentType("application/octet-stream");
        Conciliation cr = conciliationService.findById(Integer.parseInt(id));
        EventType eventType = eventTypeService.findAllById(Integer.parseInt(id));
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename="+cr.getNombre().replace(" ","_") + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> croutes = informationCrossingService.findAllData(cr,fecha,eventType);
        List<String> colConcil = Arrays.asList("FECHA_CONCILIACIÓN","CENTRO_CONTABLE","CUENTA_CONTABLE","DIVISA","TOTAL");
        InformationCrossingListReport listReport = new InformationCrossingListReport(croutes,colConcil,cr,null);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/informationCrossing/downloadDetail")
    @ResponseBody
    public void exportToExcelDetail(HttpServletResponse response, @RequestParam(defaultValue = "0") String id, @RequestParam(defaultValue = "0") String fecha, @RequestParam(defaultValue = "0") String evento) throws IOException {
        response.setContentType("application/octet-stream");
        Conciliation cr = conciliationService.findById(Integer.parseInt(id));
        List<ConciliationRoute> crList = conciliationRouteService.findByConcil(Integer.parseInt(id));
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename="+cr.getNombre().replace(" ","_") + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        InformationCrossingListReport listReport = new InformationCrossingListReport(null,null,cr,entityManager);
        listReport.exportDetail(response,crList,fecha);
    }

    @GetMapping(value = "/parametric/informationCrossing/downloadNov")
    @ResponseBody
    public void exportToExcelNov(HttpServletResponse response, @RequestParam(defaultValue = "0") String id, @RequestParam(defaultValue = "0") String fecha, @RequestParam(defaultValue = "0") String evento) throws IOException {
        response.setContentType("application/octet-stream");
        Conciliation cr = conciliationService.findById(Integer.parseInt(id));
        EventType eventType = eventTypeService.findAllById(Integer.parseInt(id));
        List<LogInformationCrossing> crList = informationCrossingService.findAllLog(cr,fecha,eventType);
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename="+cr.getNombre().replace(" ","_") + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        InformationCrossingListReport listReport = new InformationCrossingListReport(null,null,cr,entityManager);
        listReport.exportNove(response,crList,fecha);
    }
}
