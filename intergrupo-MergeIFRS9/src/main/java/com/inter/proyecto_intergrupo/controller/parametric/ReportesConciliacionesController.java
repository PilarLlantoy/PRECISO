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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ReportesConciliacionesController {
    private static final int PAGINATIONCOUNT = 12;
    //private static final int PAGINATIONCOUNT=5;
    private static final int PAGINATIONCOUNTDATA = 500;


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

    @GetMapping(value = "/parametric/reportesConciliaciones")
    public ModelAndView reportesConciliaciones(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar = userService.validateEndpointModificar(user.getId(), "Ver Matriz de Eventos");
        if (userService.validateEndpoint(user.getId(), "Ver Matriz de Eventos")) { //CAMBIAR A VER Matriz de Eventos

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            List<Object[]> eventMatrixes = eventMatrixService.findAllOrdered();


            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), eventMatrixes.size());
            Page<Object[]> pageEventMatrix = new PageImpl<>(eventMatrixes.subList(start, end), pageRequest, eventMatrixes.size());

            int totalPage = pageEventMatrix.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allEvents", pageEventMatrix.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "eventMatrix");
            modelAndView.addObject("registers", eventMatrixes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);

            List<EventType> allTEs = eventTypeService.findAll();
            modelAndView.addObject("allTEs", allTEs);

            List<Conciliation> allConcils = conciliationService.findAll();
            modelAndView.addObject("allConcils", allConcils);

            List<ParametrosReportes> reportes = parametrosReportesService.findAll();
            modelAndView.addObject("reportes", reportes);


            //List<AccountEventMatrix> cuentas = accountEventMatrixService.findAllActive();


            modelAndView.setViewName("parametric/reportesConciliaciones");
        } else {
            modelAndView.addObject("anexo", "/home");
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
    public String generarReporteConciliacion(@RequestParam("reporteId") int reporteId,
                                             @RequestParam String filtros,
                                             Model model) {

        List<Map<String, Object>> reporteData = obtenerReporte(reporteId, filtros);
        List<CampoParamReportes> cabeceras = parametrosReportesService.findById(reporteId).getCampos();

        StringBuilder tableHtml = new StringBuilder();
        tableHtml.append("<table id='example2' class='table table-striped table-hover text-center table-bordered table-sm' width='100%'>");
        tableHtml.append("<thead class='bg-primary'><tr>");
        for (CampoParamReportes cabecera : cabeceras)
            tableHtml.append("<th>").append(cabecera.getDetalle()).append("</th>");
        tableHtml.append("</tr></thead>");

        tableHtml.append("<tbody>");
        for (Map<String, Object> row : reporteData) {
            tableHtml.append("<tr>");
            for (CampoParamReportes cabecera : cabeceras) {
                tableHtml.append("<td>").append(row.get(cabecera.getDetalle())).append("</td>");
            }
            tableHtml.append("</tr>");
        }
        tableHtml.append("</tbody>");
        tableHtml.append("</table>");

        return tableHtml.toString();
    }

    private List<Map<String, Object>> obtenerReporte(int reporteId, String filtros) {

        //LOGICA DE GENERACION DE REPORTE

        //primero hacemos match con estructuras
        ParametrosReportes reporte = parametrosReportesService.findById(reporteId);
        System.out.println("REPORTE "+reporteId);


        // Convertir los resultados en una lista de Map<String, Object> para mayor claridad
        List<Map<String, Object>> reporteResultados = new ArrayList<>();


        //verificamos que tipo de reporte es
        String tipo = reporte.getTipoInsumo();
        System.out.println(tipo);
        if(tipo.equalsIgnoreCase("inventarios"))
            reporteResultados = generarReporteInventario(reporte);
        if(tipo.equalsIgnoreCase("contable"))
            reporteResultados = generarReporteContable(reporte);
        // Devolver los resultados como una lista de mapas
        return reporteResultados;
    }

    List<Map<String, Object>> generarReporteInventario(ParametrosReportes reporte){
        List<CampoParamReportes> cabeceras = parametrosReportesService.findById(reporte.getId()).getCampos();;
        ConciliationRoute inventario = reporte.getFuentes().get(0).getInventario();
        SourceParametroReportes fuente = reporte.getFuentes().get(0);
        System.out.println(inventario.getDetalle());
        List<StructureParametroReportes> allEstructuras = structureParametroReportesService.findByParamByFuente(reporte.getId(), fuente.getId());
        System.out.println(allEstructuras.size());
        List<String> campos = new ArrayList<>();

        for (StructureParametroReportes estructura : allEstructuras)
            campos.add(estructura.getCampo1().getNombre());
        String[] camposArray = campos.toArray(new String[0]);
        System.out.println("GENERANDO");  // Esto imprime los campos, solo como ejemplo
        System.out.println(Arrays.toString(camposArray));  // Esto imprime los campos, solo como ejemplo


        // Obtener los resultados usando los campos seleccionados
        List<Object[]> resultados = parametrosReportesService.findDatosxEstructuraInventario(inventario.getId(), campos);

        // Convertir los resultados en una lista de Map<String, Object> para mayor claridad
        List<Map<String, Object>> reporteResultados = new ArrayList<>();

        for (Object[] row : resultados) {

            Map<String, Object> rowMap = new HashMap<>();
            for (int i = 0; i < campos.size(); i++) {
                System.out.println(row[i]);
                rowMap.put(cabeceras.get(i).getDetalle(), row[i]);  // Mapear cada valor a su campo correspondiente
            }
            reporteResultados.add(rowMap);  // Agregar la fila al reporte final
        }
        return reporteResultados;
    }

    List<Map<String, Object>> generarReporteContable(ParametrosReportes reporte){
        List<CampoParamReportes> cabeceras = parametrosReportesService.findById(reporte.getId()).getCampos();;
        AccountingRoute contable = reporte.getFuentes().get(0).getContable();
        SourceParametroReportes fuente = reporte.getFuentes().get(0);
        System.out.println(contable.getNombre());
        List<StructureParametroReportes> allEstructuras = structureParametroReportesService.findByParamByFuente(reporte.getId(), fuente.getId());
        System.out.println(allEstructuras.size());
        List<String> campos = new ArrayList<>();

        for (StructureParametroReportes estructura : allEstructuras)
            campos.add(estructura.getCampo1rc().getNombre());
        String[] camposArray = campos.toArray(new String[0]);
        System.out.println("GENERANDO");  // Esto imprime los campos, solo como ejemplo


        // Obtener los resultados usando los campos seleccionados
        List<Object[]> resultados = parametrosReportesService.findDatosxEstructuraContable(contable.getId(), campos);

        // Convertir los resultados en una lista de Map<String, Object> para mayor claridad
        List<Map<String, Object>> reporteResultados = new ArrayList<>();

        for (Object[] row : resultados) {

            Map<String, Object> rowMap = new HashMap<>();
            for (int i = 0; i < campos.size(); i++) {
                rowMap.put(cabeceras.get(i).getDetalle(), row[i]);  // Mapear cada valor a su campo correspondiente
            }
            reporteResultados.add(rowMap);  // Agregar la fila al reporte final
        }
        return reporteResultados;
    }




}
