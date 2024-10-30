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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class CuentasNoteTemplateController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private EventMatrixService eventMatrixService;


    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private CampoRCService campoRCService;

    @Autowired
    private CampoRConcilService campoRConcilService;

    @Autowired
    private AccountEventMatrixService accountEventMatrixService;

    @Autowired
    private AccountNoteTemplateService accountNoteTemplateService;

    @GetMapping(value="/parametric/cuentasNoteTemplate/{id}")
    public ModelAndView showcuentasNoteTemplate(@PathVariable("id") int id, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        if(userService.validateEndpoint(user.getId(),"Ver Países")) { //CAMBIAR A VER Matriz de Eventos

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<AccountNoteTemplate> registros = accountNoteTemplateService.findAllActive();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), registros.size());
            Page<AccountNoteTemplate> pageEventMatrix = new PageImpl<>(registros.subList(start, end), pageRequest, registros.size());

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
            modelAndView.addObject("registers",registros.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);

            EventMatrix matriz = eventMatrixService.findById(id);
            modelAndView.addObject("matriz", matriz);

            modelAndView.addObject("plantillaId", id);

            modelAndView.setViewName("parametric/cuentasNoteTemplate");

            List<Object[]> camposConcil = campoRConcilService.findCamposByRutaConcil(matriz.getInventarioConciliacion().getId());
            modelAndView.addObject("camposConcil", camposConcil);

            AccountNoteTemplate cuentaParam = new AccountNoteTemplate();
            modelAndView.addObject("cuentaParam", cuentaParam);

        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createCuentaNT")
    public ModelAndView createCuentaNT(@ModelAttribute AccountNoteTemplate cuenta,
                                      @RequestParam(defaultValue = "0" ,name = "plantillaId") String plantillaId,
                                      @RequestParam(defaultValue = "0" ,name = "selectedDivisa") String campoDivisa,
                                      @RequestParam(defaultValue = "0" ,name = "selectedOperacion") String operacion,
                                      @RequestParam(defaultValue = "0" ,name = "selectedCI1") String inv1,
                                      @RequestParam(defaultValue = "0" ,name = "selectedOpInv") String operInv,
                                      @RequestParam(defaultValue = "0" ,name = "selectedCI2") String inv2,
                                      @RequestParam(defaultValue = "0" ,name = "selectedCentro") String centro,
                                      BindingResult bindingResult){

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/cuentasNoteTemplate/"+plantillaId);


        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/cuentasNoteTemplate/"+plantillaId);
        }else{

            /*
            if(!campoDivisa.equals("0")){
                AccountingRoute ruta = accountingRouteService.findById(Integer.valueOf(rutaContable));
                cuenta.setRutaContable(ruta);
            }

            if(!campoRutaContable.equals("0")) {
                CampoRC cRutaContable = campoRCService.findById(Integer.valueOf(campoRutaContable));
                cuenta.setCampoRutaContable(cRutaContable);
            }

            if(!campoDivisa.equals("0")) {
                CampoRC cDivisa = campoRCService.findById(Integer.valueOf(campoDivisa));
                cuenta.setCampoDivisa(cDivisa);
            }

            if(!campoValorCuenta.equals("0")) {
                CampoRC cValorCuenta = campoRCService.findById(Integer.valueOf(campoValorCuenta));
                cuenta.setCampoValorCuenta(cValorCuenta);
            }

            if(!campoValOpUno.equals("0")) {
                CampoRC cValOpUno = campoRCService.findById(Integer.valueOf(campoValOpUno));
                cuenta.setCampoValorOp1(cValOpUno);
            }

            if(!campoValOpDos.equals("0")) {
                CampoRC cValOpDos = campoRCService.findById(Integer.valueOf(campoValOpDos));
                cuenta.setCampoValorOp2(cValOpDos);
            }

            cuenta.setOperacion(operacion);

            cuenta.setTipo("1");

            EventMatrix matrz = eventMatrixService.findById(Integer.valueOf(matrizId));
            cuenta.setMatrizEvento(matrz);

            accountEventMatrixService.modificar(cuenta);

*/

            System.out.println(cuenta.getCuentaGanancia());
            System.out.println(cuenta.getCuentaPerdida());
            System.out.println(cuenta.getAplicaA());
        }

        return modelAndView;
    }


}
