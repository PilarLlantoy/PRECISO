package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Collections;
import java.util.Map;


@Controller
public class CampoRConcilController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private CampoRConcilService campoRConcilService;

    @Autowired
    private ConciliationRouteService conciliationRouteService;


    @PostMapping(value = "/parametric/createCampoRConcil")
    public ModelAndView createCampoRConcil(@ModelAttribute CampoRConcil campoNuevo,
                                            @RequestParam(name = "crouteId") String crouteId,
                                            @RequestParam(name = "longitud") String longitud,
                                            BindingResult bindingResult,
                                           @RequestParam Map<String, Object> params){
        String page="";
        if(params.get("page")!=null && params.get("page").toString()!=null)
            page="?page="+params.get("page").toString();
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fieldLoadingConciliationRoute/" + crouteId+page);
        if(longitud!=null && longitud.length()>0 ){
            campoNuevo.setLongitud(longitud);
        }
        else {
            campoNuevo.setLongitud("MAX");
        }
        ConciliationRoute route = conciliationRouteService.findById(Integer.parseInt(crouteId));
        campoNuevo.setRutaConciliacion(route);
        List<CampoRConcil> campoBusqueda= campoRConcilService.findCamposByRutaConcilVsNombre(Integer.parseInt(crouteId),campoNuevo.getNombre());
        CampoRConcil campoAntiguo= campoRConcilService.findById(campoNuevo.getId());
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
            campoRConcilService.modificar(campoNuevo);
        }
        else {
            modelAndView.addObject("resp","Maes-2");
        }

        campoRConcilService.recreateTable(route);

        if(params.get("page1")!=null && !params.get("page1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page1",params.get("page1").toString());
        else
            modelAndView.addObject("page1","1");

        return modelAndView;
    }

    public String getMaxCharacterLength(String dataType) {
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

    @DeleteMapping("/parametric/deleteCampoRConcil/{id}")
    public ResponseEntity<?> deleteCampoRConcil(@PathVariable int id) {
        try {
            ConciliationRoute cr = campoRConcilService.findById(id).getRutaConciliacion();
            System.out.println("ID->"+id);
            campoRConcilService.deleteById(id);
            campoRConcilService.recreateTable(cr);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el registro");
        }
    }

    @GetMapping("/parametric/obtenerCamposFichero/{ficheroId}")
    @ResponseBody
    public List<Object[]> getCamposByFicheroId(@PathVariable("ficheroId") Integer ficheroId) {
        List<Object[]> campos = campoRConcilService.findCamposByRutaConcil(ficheroId);
        System.out.println(campos.size());
        return campos;
    }


}
