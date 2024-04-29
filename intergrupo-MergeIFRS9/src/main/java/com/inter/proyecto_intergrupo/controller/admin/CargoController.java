package com.inter.proyecto_intergrupo.controller.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inter.proyecto_intergrupo.model.admin.Cargo;
import com.inter.proyecto_intergrupo.service.adminServices.CargoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.model.admin.User;

import org.springframework.validation.BindingResult;
import java.util.List;

@RestController
public class CargoController {

    @Autowired
    CargoService cargoService;

    @Autowired
    private UserService userService;

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @GetMapping(value = "/admin/cargos")
    public ModelAndView showCargos(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Cargos");
        if(userService.validateEndpointVer(user.getId(),"Ver Cargos")) {
            List<Cargo> allCargos = cargoService.findAllActiveCargos();
            modelAndView.addObject("cargos", allCargos);
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("admin/cargos");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/admin/createCargo")
    public ModelAndView showCreateCargo(){
        ModelAndView modelAndView = new ModelAndView();
        Cargo cargo = new Cargo();
        modelAndView.addObject("cargo",cargo);
        modelAndView.setViewName("/admin/createCargo");
        return modelAndView;
    }

    @GetMapping(value = "/admin/modifyCargo/{id}")
    public ModelAndView showModifyCargo(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Cargo myCargo = cargoService.findCargoById(id);
        modelAndView.addObject("cargo",myCargo);
        modelAndView.setViewName("admin/modifyCargo");
        return modelAndView;
    }

    @PostMapping(value = "/admin/deleteCargo/{id}")
    public boolean deleteCargo(@PathVariable int id){
        Cargo cargo = cargoService.findCargoById(id);
        cargo.setEstado(false);
        cargoService.actualizarCargo(cargo);
        return true;
    }

    @PostMapping(value = "/admin/modifyActivoCargo/{id}/{activo}")
    public boolean modifyActivoRole(@PathVariable int id, @PathVariable boolean activo){
        Cargo cargo = cargoService.findCargoById(id);
        cargo.setActivo(!activo);
        cargoService.actualizarCargo(cargo);
        return true;
    }

    @PostMapping(value = "/admin/modifyCargo")
    public ModelAndView updateRole(@ModelAttribute Cargo cargo){
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/cargos");
        System.out.println("cargo ID"+cargo.getId());
        cargoService.actualizarCargo(cargo);
        return modelAndView;
    }

    @PostMapping(value = "/admin/createCargo")
    public ModelAndView createRole(@ModelAttribute Cargo cargo, BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/cargos");

        Cargo cargoExists = cargoService.findCargoById(cargo.getId());
        if(cargoExists != null){
            bindingResult
                    .rejectValue("cargo", "error.cargo",
                            "El cargo ya se ha registrado");
        }
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("admin/createCargo");
        }else{
            cargoService.actualizarCargo(cargo);
        }
        return modelAndView;

    }

}
