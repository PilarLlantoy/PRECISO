package com.inter.proyecto_intergrupo.controller;

import com.inter.proyecto_intergrupo.model.admin.LDAP;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.sql.SQLException;

@Controller
@CrossOrigin(origins = "*", methods = {RequestMethod.GET,RequestMethod.POST})
public class LoginController {

    @Autowired
    private LDAP ldap;

    @GetMapping(value={"/", "/login"})
    public ModelAndView login(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @PostMapping(value = "/validateLogin")
    public ModelAndView validarIngreso(
            @RequestParam(name = "user_name") String username,
            @RequestParam(name = "password") String password
    ) throws SQLException {
        ModelAndView modelAndView;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Creamos una nueva instancia de autenticación con el nuevo nombre de usuario
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                username, auth.getCredentials(), auth.getAuthorities());
        // Establecemos la nueva instancia de autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        String validacion = ldap.inicializarLDAP(username, password);

        if(validacion.equals("Autenticación exitosa")){
            modelAndView = new ModelAndView("redirect:/home");
        }
        else{
            modelAndView = new ModelAndView("redirect:/login?error=true");
        }
        return modelAndView;
    }


}
