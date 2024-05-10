package com.inter.proyecto_intergrupo.configuration;
import com.inter.proyecto_intergrupo.model.admin.LDAP;
import com.inter.proyecto_intergrupo.service.parametricServices.MyUserDetailsService;
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

    private LDAP ldap;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        System.out.println("VALIDAR "+username+password);

        ldap = new LDAP();
        String rta = ldap.inicializarLDAP(username,password);

        UserDetails u = userDetailsService.loadUserByUsername(username);

        if (u!=null && rta.contains("exitosa")){
            if(bCryptPasswordEncoder.matches(password,u.getPassword())){
                System.out.println("Contra coincide con BD");
            }
            return new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList());
        } else if (u==null && !rta.contains("exitosa")) {
            System.out.println("Usuario no existe en BD");
        }
        return null;

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}