package com.inter.proyecto_intergrupo.controller.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.admin.View;
import com.inter.proyecto_intergrupo.service.adminServices.RoleService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.adminServices.ViewService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class RoleController {

    @Autowired
    RoleService roleService;

    @Autowired
    ViewService viewService;

    @Autowired
    private UserService userService;

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @GetMapping(value = "/profile/roles")
    public ModelAndView showRoles(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Roles")) {
            List<Role> allRoles = roleService.findAllActiveRoles();
            modelAndView.addObject("roles", allRoles);
            modelAndView.setViewName("profile/roles");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/profile/modifyRole/{id}")
    public ModelAndView showModifyRole(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Role myRole = roleService.findRoleById(id);

        List<View> allViews = viewService.findAll();
        List<String> allViewsPrincipal = viewService.findAllPrincipal();
        modelAndView.addObject("views",allViews);
        modelAndView.addObject("viewsPrincipal",allViewsPrincipal);
        modelAndView.addObject("viewsPrincipal1",null);
        modelAndView.addObject("role",myRole);

        List<View> roleViews = myRole.getVistas();
        String hasViews = gson.toJson(roleViews);

        modelAndView.addObject("roleViews", hasViews);
        modelAndView.setViewName("profile/modifyRole");
        return modelAndView;
    }

    @GetMapping(value = "/profile/buscarRol")
    public List<View> buscarVistas(@RequestParam String menu){

        List<View> allViews = null;
        allViews = viewService.findByMenuPrincipal(menu);
        //allViews = viewService.findAll();

        return allViews;
    }

    @PostMapping(value = "/profile/deleteRole/{id}")
    public boolean deleteRole(@PathVariable int id){
        Role role = roleService.findRoleById(id);
        role.setEstado(false);
        roleService.deleteRole(role);
        return true;
    }

    @PostMapping(value = "/profile/modifyActivoRole/{id}/{activo}")
    public boolean modifyActivoRole(@PathVariable int id, @PathVariable boolean activo){
        Role role = roleService.findRoleById(id);
        role.setActivo(!activo);
        roleService.deleteRole(role);
        return true;
    }


    /*
    @PostMapping(value = "/profile/modifyRole")
    public ModelAndView updateRole(
            @ModelAttribute Role role,
            @RequestParam(defaultValue = "N" ,name = "selectedViews") String[] views,
            @RequestParam(defaultValue = "" ,name = "principalSelect") String menu
        ){
        ModelAndView modelAndView = new ModelAndView("redirect:/profile/roles");
        ArrayList<View> newViews = new ArrayList<>();
        if(menu=="") menu = null;
        System.out.println("menu");
        System.out.println(menu);

        // if(!views[0].equals("N")) {
            for (String view : views) {
                View toAdd = viewService.findViewByName(view);
                if(toAdd != null) menu = toAdd.getMenuPrincipal();
                newViews.add(toAdd);
            }
            List<View> allViews = viewService.findAll();
            modelAndView.addObject("views", allViews);
            if (newViews.size() > 0)
                roleService.registrarNuevasVistas(role, newViews, menu);
        // }
        return modelAndView;
    }*/

    @PostMapping(value = "/profile/modifyRole")
    public ModelAndView updateRole(@ModelAttribute Role role, @RequestParam(defaultValue = "N" ,name = "selectedViews") String[] views){
        ModelAndView modelAndView = new ModelAndView("redirect:/profile/roles");
        ArrayList<View> newViews = new ArrayList<>();
        if(!views[0].equals("N")) {
            for (String view : views) {
                View toAdd = viewService.findViewByName(view);
                newViews.add(toAdd);
                System.out.println("seleccionada");
                System.out.println(view);
            }
            List<View> allViews = viewService.findAll();
            modelAndView.addObject("views", allViews);
            if (newViews.size() > 0)
                roleService.modifyRole(role, newViews);
        }
        return modelAndView;
    }

    @GetMapping(value = "/profile/createRole")
    public ModelAndView showCreateRole(){
        ModelAndView modelAndView = new ModelAndView();
        Role rol = new Role();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        List<String> allViewsPrincipal = viewService.findAllPrincipal();
        List<View> allViews = viewService.findAll();

        List<View> roleViews = null;
        String hasViews = gson.toJson(roleViews);

        modelAndView.addObject("roleViews", hasViews);

        modelAndView.addObject("role",rol);
        modelAndView.addObject("viewsPrincipal",allViewsPrincipal);
        modelAndView.addObject("views",allViews);
        modelAndView.setViewName("/profile/createRole");
        return modelAndView;
    }

    @PostMapping(value = "/profile/createRole")
    public ModelAndView createRole(@ModelAttribute Role role, @RequestParam(defaultValue = "N" ,name = "selectedViews")String[] views, BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/profile/roles");
        List<View> allViews = viewService.findAll();
        modelAndView.addObject("views",allViews);
        Role roleExists = roleService.findRoleById(role.getId());
        if(roleExists != null){
            bindingResult
                    .rejectValue("role", "error.role",
                            "El Rol ya se ha registrado");
        }
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("profile/createRole");
        }else{
            ArrayList<View> roleViews = new ArrayList<>();
            for(String view: views){
                View v = viewService.findViewByName(view);
                roleViews.add(v);
            }
            roleService.saveRole(role,roleViews);
            modelAndView.addObject("role",new Role());
        }
        return modelAndView;

    }

}
