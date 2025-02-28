package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.functions.Even;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class CuentasEventMatrixController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private EventMatrixService eventMatrixService;


    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private GeneralParamService generalParamService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private CampoRCService campoRCService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;

    @Autowired
    private CampoRConcilService campoRConcilService;

    @Autowired
    private AccountEventMatrixService accountEventMatrixService;

    @GetMapping(value="/parametric/cuentasEventMatrix/{id}")
    public ModelAndView showCuentasEventMatrix(@PathVariable("id") int id, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        if(userService.validateEndpoint(user.getId(),"Ver Países")) { //CAMBIAR A VER Matriz de Eventos

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<EventMatrix> eventMatrixes = eventMatrixService.findAllActive();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), eventMatrixes.size());
            Page<EventMatrix> pageEventMatrix = new PageImpl<>(eventMatrixes.subList(start, end), pageRequest, eventMatrixes.size());

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
            modelAndView.addObject("directory","country");
            modelAndView.addObject("registers",eventMatrixes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);

            List<EventType> allTEs = eventTypeService.findAll();
            modelAndView.addObject("allTEs", allTEs);

            List<Conciliation> allConcils = conciliationService.findAll();
            modelAndView.addObject("allConcils", allConcils);

            List<AccountingRoute> rutasContables = accountingRouteService.findAllActive();
            modelAndView.addObject("rutasContables", rutasContables);

            EventMatrix matriz = eventMatrixService.findById(id);
            modelAndView.addObject("matrizId", id);
            modelAndView.addObject("matriz", matriz);

            AccountEventMatrix cuenta1 = accountEventMatrixService.findByMatrizEventoTipo1(matriz);
            List<CampoRC> campos = null;
            if(cuenta1==null){
                cuenta1 = new AccountEventMatrix();
            }
            else{
                campos=cuenta1.getRutaContable().getCampos();
            }

            List<Object[]> camposInventario = campoRConcilService.findCamposByRutaConcil(matriz.getInventarioConciliacion().getId());
            modelAndView.addObject("camposInventario", camposInventario);

            modelAndView.addObject("cuenta1", cuenta1);
            System.out.println(cuenta1.getOperacion());
            modelAndView.addObject("campos", campos);

            AccountEventMatrix cuenta2 = accountEventMatrixService.findByMatrizEventoTipo2(matriz);
            List<CampoRC> campos2 = null;
            if(cuenta2==null){
                cuenta2 = new AccountEventMatrix();
            }
            else{
                campos2=cuenta2.getRutaContable().getCampos();
            }
            modelAndView.addObject("cuenta2", cuenta2);
            modelAndView.addObject("campos2", campos2);

            modelAndView.setViewName("parametric/cuentasEventMatrix");

            List<Object[]> camposConcil = campoRConcilService.findCamposByRutaConcil(matriz.getInventarioConciliacion().getId());
            modelAndView.addObject("camposConcil", camposConcil);

            ConstructionParameter parametro = new ConstructionParameter();
            modelAndView.addObject("parametro", parametro);

            if(params.get("selectedConcil")!= null && !params.get("selectedConcil").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedConcil1", params.get("selectedConcil").toString());
            if(params.get("selectedInv")!= null && !params.get("selectedInv").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedInv1", params.get("selectedInv").toString());
            if(params.get("selectedET")!= null && !params.get("selectedET").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedET1", params.get("selectedET").toString());
            if(params.get("selectedEstado")!= null && !params.get("selectedEstado").toString().equalsIgnoreCase(""))
                modelAndView.addObject("selectedEstado1", params.get("selectedEstado").toString());

        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping("/consultarCuenta")
    @ResponseBody
    public String consultarCuenta(@RequestParam("cuentaContable") String cuentaContable,
                                  @RequestParam("campoRutaContable") String campoRutaContable,
                                  @RequestParam("busqueda") String cadenaBusqueda,
                                  Model model) {

        //CampoRC campo = campoRCService.findById(Integer.valueOf(campoRutaContable));
        // Asegúrate de que el cuentaContable no sea nulo o vacío
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


    }

    @PostMapping(value = "/parametric/createCuenta1")
    public ModelAndView createCuenta1(@ModelAttribute AccountEventMatrix cuenta,
            @RequestParam(defaultValue = "0" ,name = "matrizId") String matrizId,
            @RequestParam(defaultValue = "0" ,name = "selectedRC1") String rutaContable,
            @RequestParam(defaultValue = "0" ,name = "selectedCRC1") String campoRutaContable,
            @RequestParam(defaultValue = "0" ,name = "selectedCD1") String campoDivisa,
            @RequestParam(defaultValue = "0" ,name = "selectedCVC1") String campoValorCuenta,
            @RequestParam(defaultValue = "0" ,name = "selectedCVOU1") String campoValOpUno,
            @RequestParam(defaultValue = "0" ,name = "selectedCVOD1") String campoValOpDos,
            @RequestParam(defaultValue = "0" ,name = "selectedOperacion1") String operacion,
            BindingResult bindingResult){

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/cuentasEventMatrix/"+matrizId);
        System.out.println("DIVISA "+campoDivisa);

        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/cuentasEventMatrix/"+matrizId);
        }else{
            if(!rutaContable.equals("0")){
                AccountingRoute ruta = accountingRouteService.findById(Integer.valueOf(rutaContable));
                cuenta.setRutaContable(ruta);
            }

            if(!campoRutaContable.equals("0")) {
                CampoRC cRutaContable = campoRCService.findById(Integer.valueOf(campoRutaContable));
                cuenta.setCampoRutaContable(cRutaContable);
            }

            if(!campoDivisa.equals("0")) {
                CampoRConcil cDivisa = campoRConcilService.findById(Integer.valueOf(campoDivisa));
                cuenta.setCampoDivisa(cDivisa);
            }

            if(!campoValorCuenta.equals("0")) {
                CampoRConcil cValorCuenta = campoRConcilService.findById(Integer.valueOf(campoValorCuenta));
                cuenta.setCampoValorCuenta(cValorCuenta);
            }

            if(!campoValOpUno.equals("0")) {
                CampoRConcil cValOpUno = campoRConcilService.findById(Integer.valueOf(campoValOpUno));
                cuenta.setCampoValorOp1(cValOpUno);
            }

            if(!campoValOpDos.equals("0")) {
                CampoRConcil cValOpDos = campoRConcilService.findById(Integer.valueOf(campoValOpDos));
                cuenta.setCampoValorOp2(cValOpDos);
            }

            if(!operacion.equals("0")) {
                cuenta.setOperacion(operacion);
            }

            cuenta.setTipo("1");

            EventMatrix matrz = eventMatrixService.findById(Integer.valueOf(matrizId));
            cuenta.setMatrizEvento(matrz);

            accountEventMatrixService.modificar(cuenta);
        }

        return modelAndView;
    }

    @PostMapping(value = "/parametric/createCuenta2")
    public ModelAndView createCuenta2(@ModelAttribute AccountEventMatrix cuenta,
                                      @RequestParam(defaultValue = "0" ,name = "matrizId") String matrizId,
                                      @RequestParam(defaultValue = "0" ,name = "selectedRC2") String rutaContable,
                                      @RequestParam(defaultValue = "0" ,name = "selectedCRC2") String campoRutaContable,
                                      @RequestParam(defaultValue = "0" ,name = "selectedCD2") String campoDivisa,
                                      @RequestParam(defaultValue = "0" ,name = "selectedCVC2") String campoValorCuenta,
                                      @RequestParam(defaultValue = "0" ,name = "selectedCVOU2") String campoValOpUno,
                                      @RequestParam(defaultValue = "0" ,name = "selectedCVOD2") String campoValOpDos,
                                      @RequestParam(defaultValue = "0" ,name = "selectedOperacion2") String operacion,
                                      BindingResult bindingResult){

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/cuentasEventMatrix/"+matrizId);


        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/cuentasEventMatrix/"+matrizId);
        }else{
            AccountingRoute ruta = accountingRouteService.findById(Integer.valueOf(rutaContable));
            cuenta.setRutaContable(ruta);

            CampoRC cRutaContable = campoRCService.findById(Integer.valueOf(campoRutaContable));
            CampoRConcil cDivisa = campoRConcilService.findById(Integer.valueOf(campoDivisa));
            CampoRConcil cValorCuenta = campoRConcilService.findById(Integer.valueOf(campoValorCuenta));
            CampoRConcil cValOpUno = campoRConcilService.findById(Integer.valueOf(campoValOpUno));
            CampoRConcil cValOpDos = campoRConcilService.findById(Integer.valueOf(campoValOpDos));

            cuenta.setCampoRutaContable(cRutaContable);
            cuenta.setCampoDivisa(cDivisa);
            cuenta.setCampoValorCuenta(cValorCuenta);
            cuenta.setCampoValorOp1(cValOpUno);
            cuenta.setCampoValorOp2(cValOpDos);

            cuenta.setOperacion(operacion);

            cuenta.setTipo("2");

            EventMatrix matrz = eventMatrixService.findById(Integer.valueOf(matrizId));
            cuenta.setMatrizEvento(matrz);

            accountEventMatrixService.modificar(cuenta);
        }

        return modelAndView;
    }


}
