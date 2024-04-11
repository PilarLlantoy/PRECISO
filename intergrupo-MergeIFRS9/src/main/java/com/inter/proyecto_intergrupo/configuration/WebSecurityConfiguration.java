package com.inter.proyecto_intergrupo.configuration;


import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.View;
import com.inter.proyecto_intergrupo.service.adminServices.RoleService;
import com.inter.proyecto_intergrupo.service.adminServices.ViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.inter.proyecto_intergrupo.service.parametricServices.MyUserDetailsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private MyUserDetailsService userDetailsService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ViewService viewService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        String loginPage = "/login";
        String logoutPage = "/logout";

        http.authorizeRequests().antMatchers("/", loginPage,"/password/**").permitAll();

        /*List<View> vistas = viewService.findAll();
        for(View view: vistas){
            List<String> paths = new ArrayList<>();
            List<String> pathRoles = new ArrayList<>();
            paths.add(view.getPath());
            if(!view.getUnique()){
                String path = view.getPath() + "/**";
                paths.add(path);
            }
            Set<Role> roles = view.getRoles();
            roles.forEach(role -> {
                pathRoles.add(role.getNombre());
            });
            String[] finalPaths = paths.toArray(new String[0]);
            String[] finalRoles = pathRoles.toArray(new String[0]);
            http.authorizeRequests().mvcMatchers(finalPaths).hasAnyAuthority(finalRoles);

        }*/

        http.formLogin()
                .loginPage(loginPage)
                .loginPage("/")
                .failureUrl("/login?error=true")
                .defaultSuccessUrl("/home",true)
                .usernameParameter("user_name")
                .passwordParameter("password")
                .and().authorizeRequests().anyRequest().authenticated()
                .and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher(logoutPage))
                .logoutSuccessUrl(loginPage).and().exceptionHandling();
    }

    @Override
    public void configure(WebSecurity web) throws Exception
    {
        web
                .ignoring()
                .antMatchers( "/static/**", "/css/**", "/js/**", "/images/**", "/fonts/**", "/css/bootstrap/**","../static/**");
    }

}
