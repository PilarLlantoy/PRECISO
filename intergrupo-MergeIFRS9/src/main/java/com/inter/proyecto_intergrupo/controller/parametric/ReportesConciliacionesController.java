package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ReportesConciliacionesController {
    private static final int PAGINATIONCOUNT=12;
    //private static final int PAGINATIONCOUNT=5;
    private static final int PAGINATIONCOUNTDATA=500;


    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ParametrosReportesService parametrosReportesService;

    @Autowired
    private FilterParametroReportesService filterParametroReportesService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private CampoParametroReportesService campoParametroReportesService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private SourceParametroReportesService sourceParametroReportesService;

    @Autowired
    private AdditionalSourceParametroReportesService additionalSourceParametroReportesService;

    @Autowired
    private CampoRConcilService campoRConcilService;

    @Autowired
    private StructureParametroReportesService structureParametroReportesService;

    @Autowired
    private ValidationParametroReportesService validationParametroReportesService;

    @Autowired
    private ResultingFieldsParametroReportesService resultingFieldsParametroReportesService;

    @Autowired
    private EventMatrixService eventMatrixService;

    @GetMapping(value="/parametric/reportesConciliaciones")
    public ModelAndView reportesConciliaciones(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Matriz de Eventos");
        if(userService.validateEndpoint(user.getId(),"Ver Matriz de Eventos")) { //CAMBIAR A VER Matriz de Eventos

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<Object[]> eventMatrixes = eventMatrixService.findAllOrdered();


            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), eventMatrixes.size());
            Page<Object[]> pageEventMatrix = new PageImpl<>(eventMatrixes.subList(start, end), pageRequest, eventMatrixes.size());

            int totalPage=pageEventMatrix.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allEvents",pageEventMatrix.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","eventMatrix");
            modelAndView.addObject("registers",eventMatrixes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);

            List<EventType> allTEs = eventTypeService.findAll();
            modelAndView.addObject("allTEs", allTEs);

            List<Conciliation> allConcils = conciliationService.findAll();
            modelAndView.addObject("allConcils", allConcils);

            List<ParametrosReportes> reportes = parametrosReportesService.findAll();
            modelAndView.addObject("reportes",reportes);


            //List<AccountEventMatrix> cuentas = accountEventMatrixService.findAllActive();


            modelAndView.setViewName("parametric/reportesConciliaciones");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }


    @GetMapping("/parametric/obtenerCamposRangos/{reporteId}")
    @ResponseBody
    public List<Object[]> obtenerCamposRangos(@PathVariable("reporteId") int reporteId) {
        List<Object[]> campos = parametrosReportesService.findCampoReporteFiltro(reporteId, "Rangos");
        return campos;
    }

    @GetMapping("/parametric/obtenerCamposPuntual/{reporteId}")
    @ResponseBody
    public List<Object[]> obtenerCamposPuntual(@PathVariable("reporteId") int reporteId) {
        List<Object[]> campos = parametrosReportesService.findCampoReporteFiltro(reporteId, "Puntual");
        return campos;
    }

    @PostMapping("/generarReporteConciliacion")
    @ResponseBody
    public String generarReporteConciliacion(@RequestParam("reporteId") String reporteId,
                                             @RequestParam String filtros,
                                  Model model) {

        System.out.println(filtros); // Aquí puedes usar los filtros para generar el reporte


        //CampoRC campo = campoRCService.findById(Integer.valueOf(campoRutaContable));
        // Asegúrate de que el cuentaContable no sea nulo o vacío
        /*
        if (cadenaBusqueda == null || cadenaBusqueda.isEmpty()) {
            return "<tr><td colspan='100%'>Por favor ingrese una cuenta contable válida.</td></tr>";
        }

        // Ejecutar las operaciones
        //ConciliationRoute ruta = conciliationRouteService.findById(Integer.valueOf(cuentaContable));
        GeneralParam generalParam = generalParamService.findAllById(3L);
        String campo = generalParamService.findAllById(4L).getValorUnidad();
        AccountingRoute ruta = accountingRouteService.findByName(generalParam.getValorUnidad());
        if (ruta == null) {
            return "<tr><td colspan='100%'>Tabla No Encontrada</td></tr>"; // Mensaje de error en HTML
        }

        String ultimaFecha = conciliationRouteService.encontrarUltimaFechaSubida(ruta);

        List<Object[]> aroutes = accountingRouteService.findAllData(ruta, ultimaFecha, cadenaBusqueda, campo);

        CampoRC crc = new CampoRC();
        crc.setNombre("periodo_preciso");
        ruta.getCampos().add(crc);
        List<CampoRC> colAroutes = ruta.getCampos();

        int indice = 0; //para saber el indice del campo
        for(CampoRC c:colAroutes){
            if(c.getNombre().equalsIgnoreCase(campo)) break;
            indice=indice+1;
        }

        // Guardar los datos en el modelo para enviarlos de vuelta a la vista
        model.addAttribute("allRCs", aroutes);
        model.addAttribute("allColRCs",colAroutes);

        StringBuilder tablaHtml = new StringBuilder();
        tablaHtml.append("<table id='example2' class='table table-striped table-hover text-center table-bordered table-sm' width='100%'>");
        tablaHtml.append("<thead class='bg-primary'><tr><td></td>");

        // Crear encabezados
        for (CampoRC col : colAroutes) {
            tablaHtml.append("<td>").append(col.getNombre()).append("</td>");
        }
        tablaHtml.append("</tr></thead><tbody>");

        System.out.println("INDICE---------------------------->"+indice);
        System.out.println("TAMAÑO---------------------------->"+aroutes.size()+" - "+cadenaBusqueda);

        // Crear filas
        for (Object[] row : aroutes) {
            tablaHtml.append("<tr>");

            // Agregar checkbox, usando el ID de la fila como valor
            String rowId = row[indice].toString(); // Supongamos que el primer elemento es el ID
            tablaHtml.append("<td><input type='checkbox' class='row-checkbox' data-dismiss=\"modal\" value='").append(rowId).append("' onclick='captarId(this)'></td>");

            for (Object col : row) {
                tablaHtml.append("<td>").append(col).append("</td>");
            }
            tablaHtml.append("</tr>");
        }
        tablaHtml.append("</tbody></table>");

        model.addAttribute("tablaHtml", tablaHtml.toString());

        System.out.println(tablaHtml);

        // Retorna la vista
        return tablaHtml.toString(); // Ajusta esto según la vista que deseas retornar
*/
        return null;

    }
}
