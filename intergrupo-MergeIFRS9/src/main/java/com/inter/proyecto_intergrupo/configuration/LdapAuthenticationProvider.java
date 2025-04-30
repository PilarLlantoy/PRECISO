package com.inter.proyecto_intergrupo.configuration;
import com.inter.proyecto_intergrupo.model.admin.LDAP;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.MyUserDetailsService;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class LdapAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private MyUserDetailsService userDetailsService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private SendEmailService sendEmailService;

    private LDAP ldap;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        ldap = new LDAP();
        String rta = ldap.inicializarLDAP(username,password);
        if (rta.contains("exitosa")){
                //verificar en tabla local
                User existe = userService.findUserByUserName(username);
                if(existe==null){
                    //si no está, mandar correo
                    String subject = "Solicitud de registro de usuario BBVA PRECISO";
                    String content = "<p>Hola,</p>"
                            + username+"<p> ha solicitado registrarse en PRECISO</p>";
                    String recipientEmail = "pilar.llantoy.contractor@bbva.com";
                    sendEmailService.sendEmail(recipientEmail, subject, content);
                    //userService.saveUsarLDAP(username); //para guardar al usuario
                }

            return new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList());
        }
        return null;

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}