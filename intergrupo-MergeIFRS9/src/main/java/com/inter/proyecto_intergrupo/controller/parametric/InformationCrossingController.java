package com.inter.proyecto_intergrupo.controller.parametric;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
            System.out.println("INICIO "+params.get("arhcont"));
            System.out.println("INICIO "+params.get("period"));
            System.out.println("INICIO "+params.get("evento"));
            if(params.get("arhcont") != null && params.get("arhcont").toString() != null
                    && params.get("period") != null && params.get("period").toString() != null
                    && params.get("evento") != null && params.get("evento").toString() != null)
            {
                System.out.println("NO VENIA VACIO");
                modelAndView.addObject("period",params.get("period").toString());
                Conciliation concil = conciliationService.findById(Integer.parseInt(params.get("arhcont").toString()));
                EventType evento = eventTypeService.findAllById(Integer.parseInt(params.get("evento").toString()));
                String fecha = params.get("period").toString();
                modelAndView.addObject("arhcont",concil);
                modelAndView.addObject("evento",evento);
                logCruces = informationCrossingService.findAllLog(concil,params.get("period").toString(), evento);

                CampoRC crc= new CampoRC();
                crc.setNombre("periodo_preciso");
                colDatos =List.of("FECHA CONCILIACION", "CENTRO CONTABLE", "CUENTA CONTABLE","DIVISA","SALDO INVENTARIO");;
                datos = informationCrossingService.processList(informationCrossingService.findAllData(concil, fecha, evento), colDatos);


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
            modelAndView.addObject("directory","accountingLoad");
            modelAndView.addObject("registers",logCruces.size());
            modelAndView.addObject("registersData",datos.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
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
        System.out.println(listRoutes.size());
        List<Object[]> croutes = new ArrayList<>();
        List<String> faltaCarga = new ArrayList<>();

        List<LogInventoryLoad> logConcilroutes = new ArrayList<>();
        try {
            for (ConciliationRoute ruta : listRoutes) {
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
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk-1");
            } else
                System.out.println("NO falta cargar archivos");


            //GENERAR CRUCE DE INVENTARIO
            //-----------------------------------------------------------------------------------
            for (ConciliationRoute ruta : listRoutes) {

                //Creamos las tablas finales vacias de cada inventario con los campos agregados
                informationCrossingService.recreateTable(ruta, id);

                List<EventMatrix> matrices = eventMatrixService.findByConciliationxInventarioxTipoEvento(id, ruta.getId(), evento);
                for (EventMatrix matriz : matrices) {

                    //Creamos tablas temporales con la data total
                    informationCrossingService.creatTablaTemporalCruce(ruta);

                    //Primero veremos las condiciones
                    List<CondicionEventMatrix> condiciones = condicionMEService.findByMatrizEvento(matriz);
                    List<Object[]> resultados = null;
                    if (condiciones.size() != 0)
                        informationCrossingService.conditionData(ruta, matriz);

                    //Completamos informacion de cruce
                    AccountEventMatrix cuenta1 = accountEventMatrixService.findByMatrizEventoTipo1(matriz);
                    AccountEventMatrix cuenta2 = accountEventMatrixService.findByMatrizEventoTipo2(matriz);
                    informationCrossingService.completarTablaCruce(ruta, fecha, tipoEvento, matriz, cuenta1, cuenta2);

                    //Realizamos las validaciones
                    //recuerda corregir para las validaciones que solo sean un reemplazo de palabras

                    //Agregamos estos registros a la tabla final
                    informationCrossingService.rellenarTablaCruceTotal(ruta, id);

                }

            }


            //RESMIR POR CUENTA
            //-----------------------------------------------------------------------------------

            /*File dest = new File(rutaArchivo);
            file.transferTo(dest);
            accountingRouteService.createTableTemporal(ac);
            accountingRouteService.generarArchivoFormato(ac.getCampos(), rutaArchivoFormato);
            if(ac.getTipoArchivo().equalsIgnoreCase("XLS") || ac.getTipoArchivo().equalsIgnoreCase("XLSX"))
                accountingRouteService.importXlsx(ac,rutaArchivoFormato,fecha,rutaArchivo);
            else
                accountingRouteService.bulkImport(ac,rutaArchivoFormato,fecha,rutaArchivo);
            accountingRouteService.conditionData(ac);
            accountingRouteService.validationData(ac);
            accountingRouteService.copyData(ac,fecha);
            accountingRouteService.loadLogCargue(user,ac,fecha,"Trasladar Local","Exitoso","");*/
            return ResponseEntity.ok("Bulk->1");
        }
        catch (Exception e) {
            e.printStackTrace();
            Throwable rootCause = e;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause(); // Navega a la causa raíz
            }
            //accountingRouteService.loadLogCargue(user,ac,fecha,"Trasladar Local","Fallido",rootCause.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk-1");
        }
    }


}
