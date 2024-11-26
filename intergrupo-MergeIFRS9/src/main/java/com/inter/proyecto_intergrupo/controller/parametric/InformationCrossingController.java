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

import javax.servlet.http.HttpServletResponse;
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
@EnableScheduling
public class InformationCrossingController {
    private static final int PAGINATIONCOUNT=5;
    private static final int PAGINATIONCOUNTDATA=500;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private InformationCrossingService informationCrossingService;

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

            List<Object[]> aroutes = new ArrayList<>();
            List<CampoRC> colAroutes = new ArrayList<>();
            List<LogInformationCrossing> logCruces = new ArrayList<>();
            if(params.get("arhcont") != null && params.get("arhcont").toString() != null
                    && params.get("period") != null && params.get("period").toString() != null
                    && params.get("evento") != null && params.get("evento").toString() != null)
            {
                modelAndView.addObject("period",params.get("period").toString());
                Conciliation concil = conciliationService.findById(Integer.parseInt(params.get("arhcont").toString()));
                EventType evento = eventTypeService.findAllById(Integer.parseInt(params.get("arhcont").toString()));
                modelAndView.addObject("arhcont",concil);
                modelAndView.addObject("evento",evento);
                logCruces = informationCrossingService.findAllLog(concil,params.get("period").toString(), evento);
                /*aroutes = accountingRouteService.findAllData(ac,params.get("period").toString(), null, null);
                CampoRC crc= new CampoRC();
                crc.setNombre("periodo_preciso");
                ac.getCampos().add(crc);
                colAroutes = ac.getCampos();*/
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
            modelAndView.addObject("listConcil",listConcil);
            modelAndView.addObject("listTypeEvent",listTypeEvent);
            modelAndView.addObject("directory","accountingLoad");
            modelAndView.addObject("registers",logCruces.size());
            modelAndView.addObject("registersData",aroutes.size());
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

    @GetMapping("/parametric/informationCrossing/generateAccount")
    @ResponseBody
    public ResponseEntity<String> generarCuentas(@RequestParam int id, @RequestParam String fecha, @RequestParam int evento) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        AccountingRoute ac = accountingRouteService.findById(id);
        try {
            //accountingRouteService.loadLogCargue(user,ac,fecha,"Trasladar Servidor","Exitoso","");
            return ResponseEntity.ok("Bulk1");
        }
        catch (Exception e) {
            e.printStackTrace();
            //accountingRouteService.loadLogCargue(user,ac,fecha,"Trasladar Servidor","Fallido","Verifique el fichero se encuetre en la ruta y la estructura de los campos este correcta en el tamaño de los campos.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bulk-1");
        }
    }


}
