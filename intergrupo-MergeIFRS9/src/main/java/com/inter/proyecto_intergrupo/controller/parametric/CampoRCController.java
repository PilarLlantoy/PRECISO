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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class CampoRCController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private CampoRCService campoRCService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @GetMapping(value="/parametric/mostrarCamposRC")
    public ModelAndView mostrarCamposRC(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Conciliaciones");
        if(userService.validateEndpoint(user.getId(),"Ver Conciliaciones")) {

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<Conciliation> conciliations = conciliationService.findAllActive();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), conciliations.size());
            Page<Conciliation> pageConciliation = new PageImpl<>(conciliations.subList(start, end), pageRequest, conciliations.size());

            int totalPage=pageConciliation.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allCountry",pageConciliation.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","country");
            modelAndView.addObject("registers",conciliations.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/conciliation");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/cargueCamposRC")
    public ModelAndView cargueCamposRC(){
        ModelAndView modelAndView = new ModelAndView();
        Campo campo = new Campo();
        modelAndView.addObject("campo",campo);
        modelAndView.setViewName("/parametric/cargueCampos");
        return modelAndView;
    }


    @PostMapping(value = "/parametric/createCampoRC")
    public ModelAndView createCampoRC(@ModelAttribute CampoRC campoNuevo,
                                      @RequestParam(name = "arouteId") String arouteId,
                                      @RequestParam(name = "longitud") String longitud,
                                      @RequestParam(name = "funcion") String funcion,
                                      @RequestParam(name = "page", defaultValue = "0") String page,
                                      @RequestParam(name = "page1", defaultValue = "0") String page1,
                                      BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fieldLoadingAccountingRoute/" + arouteId);
        if(longitud!=null && longitud.length()>0 ){
            campoNuevo.setLongitud(longitud);
        }
        else {
            campoNuevo.setLongitud("MAX");
        }
        AccountingRoute aroute = accountingRouteService.findById(Integer.parseInt(arouteId));
        campoNuevo.setRutaContable(aroute);

        List<CampoRC> campoBusqueda= campoRCService.findCamposByRutaVsNombre(Integer.parseInt(arouteId),campoNuevo.getNombre());
        CampoRC campoAntiguo= campoRCService.findById(campoNuevo.getId());
        System.out.println("Nuevo-> ID:"+campoNuevo.getId()+" - NOM:"+campoNuevo.getNombre());
        if(campoAntiguo!=null)
            System.out.println("Antiguo-> ID:"+campoAntiguo.getId()+" - NOM:"+campoAntiguo.getNombre());
        else
            System.out.println("Antiguo-> ID:Vacio - NOM:Vacio");
        if(!campoBusqueda.isEmpty())
            System.out.println("Busqueda-> ID:"+campoBusqueda.get(0).getId()+" - NOM:"+campoBusqueda.get(0).getNombre());
        else
            System.out.println("Busqueda-> ID:Vacio - NOM:Vacio");
        if((campoBusqueda.isEmpty() && campoNuevo.getId()==0) ||
        (campoAntiguo!=null && campoAntiguo.getId() == campoNuevo.getId() && campoAntiguo.getNombre().equalsIgnoreCase(campoNuevo.getNombre()) && campoNuevo.getId()!=0) ||
        (campoAntiguo!=null && campoAntiguo.getId() == campoNuevo.getId() && !campoAntiguo.getNombre().equalsIgnoreCase(campoNuevo.getNombre()) && campoBusqueda.isEmpty() && campoNuevo.getId()!=0))
        {
            campoRCService.modificar(campoNuevo);
        }
        else
        {
            modelAndView.addObject("resp","Maes-2");
        }

        campoRCService.recreateTable(aroute);

        if(!page.equalsIgnoreCase("0"))
            modelAndView.addObject("page",page);
        if(!page1.equalsIgnoreCase("0"))
            modelAndView.addObject("page1",page1);

        return modelAndView;

    }

    public String getMaxCharacterLength(String dataType) {
        System.out.println("RA");
        switch (dataType.toUpperCase()) {
            case "INTEGER":
                return "11"; // Hasta 11 caracteres
            case "BIGINT":
                return "20"; // Hasta 20 caracteres
            case "FLOAT":
                return "16"; // Indefinido, depende de la precisión
            case "VARCHAR":
                return "2147483647"; // Hasta 2,147,483,647 caracteres
            case "DATE":
                return "10"; // Formato YYYY-MM-DD
            case "TIME":
                return "16"; // Formato HH:MM:SS.ffffff
            case "DATETIME":
                return "23"; // Formato YYYY-MM-DD HH:MM:SS.fff
            case "BIT":
                return "1"; // Representación como '0' o '1'
            default:
                return "Tipo de dato desconocido";
        }
    }

    @DeleteMapping("/parametric/deleteCampoRC/{id}")
    public ResponseEntity<?> deleteCampoRC(@PathVariable int id) {
        try {
            AccountingRoute ac = campoRCService.findById(id).getRutaContable();
            campoRCService.deleteById(id);
            campoRCService.recreateTable(ac);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }

    @GetMapping("/parametric/obtenerCamposRC/{idRCont}")
    @ResponseBody
    public List<Object[]> obtenerCamposRC(@PathVariable("idRCont") Integer idRCont) {
        List<Object[]> campos = campoRCService.findCamposByRutaCont(idRCont);
        return campos;
    }

    @GetMapping(value = "/parametric/camposRC/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, @RequestParam int id) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Campos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> accountRouteCam = accountingRouteService.findByCamposSelect(id);
        AccountingRoutesListReport listReport = new AccountingRoutesListReport(null,null,null,accountRouteCam);
        listReport.export(response);
    }

}
