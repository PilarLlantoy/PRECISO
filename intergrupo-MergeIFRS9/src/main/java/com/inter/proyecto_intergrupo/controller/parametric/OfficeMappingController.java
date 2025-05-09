package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.MasterInvent;
import com.inter.proyecto_intergrupo.model.parametric.OfficeMapping;
import com.inter.proyecto_intergrupo.model.parametric.ParamOfficeMapping;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountingRouteService;
import com.inter.proyecto_intergrupo.service.parametricServices.GeneralListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.OfficeMappingListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.OfficeMappingService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class OfficeMappingController {
    private static final int PAGINATIONCOUNT=20;
    private List<String> listColumns=List.of("Centro Origen","Nombre Centro","Centro Destino");
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private OfficeMappingService officeMappingService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @GetMapping(value="/parametric/officeMapping")
    public ModelAndView showPrincipal(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Homologación Oficinas");
        if(userService.validateEndpoint(user.getId(),"Ver Homologación Oficinas")) {

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<OfficeMapping> data = officeMappingService.findAll();
            List<AccountingRoute> dataAccountingRoute = accountingRouteService.findAllActive();
            List<ParamOfficeMapping> paramOfficeMapping = officeMappingService.getParam();

            if(!paramOfficeMapping.isEmpty())
            {
                ParamOfficeMapping param = paramOfficeMapping.get(0);
                modelAndView.addObject("selectedRoute1",param.getIdRcOrigen());
                modelAndView.addObject("selectedCampoOrigen1",param.getCampoRcCentroOrigen());
                modelAndView.addObject("selectedCampoDetalle1",param.getCampoRc1DetalleOrigen());
                modelAndView.addObject("selectedCampoDestino1",param.getCampoRcCentroResultado());
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), data.size());
            Page<OfficeMapping> pageData = new PageImpl<>(data.subList(start, end), pageRequest, data.size());

            int totalPage=pageData.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allData",pageData.getContent());
            modelAndView.addObject("allDataRoute",dataAccountingRoute);
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("registers",data.size());
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "officeMapping");
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/officeMapping");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping("/parametric/obtenerRutasContable/{contaID}")
    @ResponseBody
    public List<Object[]> obtenerRutasCont(@PathVariable("contaID") Integer contaID) {
        List<Object[]> campos = officeMappingService.findAccountRoute(contaID);
        return campos;
    }

    @PostMapping(value = "/parametric/loadParamOfficeMapping")
    public ModelAndView updateData(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/officeMapping");
        try{
            if(!params.get("selectedRutaConatble").toString().equalsIgnoreCase("0") && !params.get("selectedCampoOrigen").toString().equalsIgnoreCase("0") && !params.get("selectedCampoDetalle").toString().equalsIgnoreCase("0") && !params.get("selectedCampoDestino").toString().equalsIgnoreCase("0"))
            {
                officeMappingService.saveParam(params.get("selectedRutaConatble").toString(),params.get("selectedCampoOrigen").toString(),params.get("selectedCampoDetalle").toString(),params.get("selectedCampoDestino").toString());
                modelAndView.addObject("resp", "Office1");
            }
            else {
                modelAndView.addObject("resp", "Office-1");
            }
        }
        catch(Exception e) {
            modelAndView.addObject("resp", "Office-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/loadOfficeMapping")
    public ModelAndView updateTable(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/officeMapping");
        try {
            officeMappingService.saveData();
            modelAndView.addObject("resp", "Office2");
        }
        catch(Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "Office-2");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/officeMapping/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Homologacion_Centros_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<OfficeMapping> list = officeMappingService.findAll();
        OfficeMappingListReport listReport = new OfficeMappingListReport(list);
        listReport.export(response);
    }
}
