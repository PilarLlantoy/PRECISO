package com.inter.proyecto_intergrupo.controller.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.TypeEntity;
import com.inter.proyecto_intergrupo.service.adminServices.RoleService;
import com.inter.proyecto_intergrupo.service.adminServices.UserListReport;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.TypeEntityListReport;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import com.inter.proyecto_intergrupo.utility.Utility;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class UserController {

    private static final int PAGINATIONCOUNT=12;
    private List<String> listColumns=List.of("Usuario", "Nombre", "Correo","Centro","Estado","Roles","YNTP Empresa");

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    SendEmailService sendEmailService;


    @RequestMapping(value="/home")
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        userService.loadAudit(user);
        modelAndView.addObject(user);
        modelAndView.addObject("userName", user.getNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        modelAndView.addObject("userComp", user.getEmpresa());
        modelAndView.setViewName("home");
        return modelAndView;
    }
    @RequestMapping(value="/admin/errorMenu")
    public ModelAndView home1() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject(user);
        modelAndView.addObject("userName", user.getNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        modelAndView.addObject("userComp", user.getEmpresa());
        modelAndView.setViewName("admin/errorMenu");
        return modelAndView;
    }

    @GetMapping(value = "/admin/users")
    public ModelAndView showUsers(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Usuarios")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            List<User> list = userService.findAll();
            Page<User> pageType = userService.getAll(pageRequest);
            int totalPage = pageType.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allUsers", pageType.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "users");
            modelAndView.addObject("registers",list.size());

            modelAndView.addObject("userName", user.getUsuario());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("admin/users");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/admin/users/download")
    @ResponseBody
    public void exportToExcelUser(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Usuarios_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<User> userList= new ArrayList<User>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            userList = userService.findAll();
        }
        else{
            userList = userService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        UserListReport listReport = new UserListReport(userList);
        listReport.export(response);
    }

    @GetMapping(value = "/admin/modifyUsers/{id}")
    @ResponseBody
    public ModelAndView modifyUsers(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

            modelAndView.addObject("userName", user.getUsuario());
            modelAndView.addObject("userEmail", user.getCorreo());
            List<Role> allRoles = roleService.findAll();
            modelAndView.addObject("roles", allRoles);
            User toModify = userService.findUserByUserName(id);
            modelAndView.addObject("userModify", toModify);
            Set<Role> userRoles = toModify.getRoles();
            List<Role> finalRoles = new ArrayList<>(userRoles);
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String hasRoles = gson.toJson(finalRoles);
            modelAndView.addObject("hasRoles", hasRoles);
            modelAndView.setViewName("admin/modifyUsers");

        return modelAndView;
    }

    @PostMapping(value = "/admin/modifyUsers")
    public ModelAndView updateUser(@ModelAttribute User user, @RequestParam(name = "selectedRoles") String[] roles,@RequestParam("newU") String id){
        Set<Role> newRoles = new HashSet<Role>();
        for (String role : roles) {
            Role myRole = roleService.findRole(role);
            newRoles.add(myRole);
        }
        User searchUser = userService.findUserByUserName(user.getUsuario());
        User searchUserOld = userService.findUserByUserName(id);
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/users");
        List<Role> allRoles = roleService.findAll();
        modelAndView.addObject("roles", allRoles);
        modelAndView.addObject("message", "Usuario modificado");
        if(searchUser!=null)
        {
            userService.modifyUser(user, searchUser.getCreacion(), newRoles, searchUser.getUsuario());
        }
        else
        {
            user.setUsuario(searchUserOld.getUsuario());
            HashSet<Role> userRoles = new HashSet<>();
            for(int i =0; i< roles.length;i++){
                try{
                    Role myRole = roleService.findRole(roles[i]);
                    userRoles.add(myRole);
                }catch(Exception e){}
            }
            userService.saveUser(user,userRoles);
        }
        return  modelAndView;
    }

    @GetMapping(value="/admin/createUser")
    public ModelAndView registration(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user1 = userService.findUserByUserName(auth.getName());

        User user = new User();
        modelAndView.addObject("user", user);
        List<Role> allRoles = roleService.findAll();
        modelAndView.addObject("roles", allRoles);
        modelAndView.setViewName("admin/createUser");
        return modelAndView;
    }

    @PostMapping(value = "/admin/createUser")
    public ModelAndView createNewUser(@ModelAttribute User user, @RequestParam("selectedRoles") String[] roles, BindingResult bindingResult, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("redirect:/admin/users");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List <Role> allRoles = roleService.findAll();
        modelAndView.addObject("roles",allRoles);
        User userExists = userService.findUserByUserName(user.getUsuario());
        User userByEmail = userService.findUserByEmail(user.getCorreo());
        if (userExists != null) {
            bindingResult
                    .rejectValue("usuario", "error.usuario",
                            "El usuario ya se encuentra registrado");
        }
        if(userByEmail != null){
            bindingResult
                    .rejectValue("correo","error.correo",
                            "El correo ya se encuentra registrado");
        }
        if(bindingResult.hasErrors()) {
            modelAndView.setViewName("admin/createUser");
        }else{
            HashSet<Role> userRoles = new HashSet<>();
            for(int i =0; i< roles.length;i++){
                try{
                    Role myRole = roleService.findRole(roles[i]);
                    userRoles.add(myRole);
                }catch(Exception e){}
            }
            userService.saveUser(user,userRoles);

            String mail = user.getCorreo();
            String username = user.getUsuario();
            String token = RandomStringUtils.randomAlphanumeric(10);

            try{
                userService.updateResetPasswordToken(token,username);
                String resetPasswordLink = Utility.getSiteURL(request)+"/password/resetPassword?token=" +token;
                sendEmail(mail,resetPasswordLink,username);
                modelAndView.addObject("message","Se ha enviado un enlace para cambiar la contrasena");
            }catch (UsernameNotFoundException e){
                modelAndView.addObject("error",e.getMessage());
            }
            catch (Exception e) {
                modelAndView.addObject("error","Error al enviar el mensaje");
            }

            modelAndView.addObject("user", new User());
        }
        User userView = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",userView.getUsuario());
        modelAndView.addObject("userEmail",userView.getCorreo());
        List<User> allUsers = userService.findAll();
        modelAndView.addObject("allUsers",allUsers);

        return modelAndView;
    }

    public void sendEmail(String recipientEmail, String link, String username) {
        String subject = "Nuevo usuario Nexco";

        String content = "<p>Hola,</p>"
                + "<p>Bienvenido al aplicativo BBVA Nexco</p>"
                + "<p>Tu usuario: \""+username+"\" </p>"
                + "<p>Ingresa al siguiente enlace para asignar tu contraseña:</p>"
                + "<p><a href=\"" + link + "\">Cambiar mi contraseña</a></p>"
                + "<br>"
                + "<p>Gracias</p>";

        sendEmailService.sendEmail(recipientEmail,subject,content);
    }

    @GetMapping(value = "/admin/dropdown")
    public ModelAndView dropdownTest(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/admin/dropdownExample");
        return modelAndView;
    }

    @GetMapping(value = "/admin/validatePrincipal")
    @ResponseBody
    public List<String> validatePincipal(@RequestParam String principalSelect)
    {
        return userService.validatePrincipal(principalSelect);
    }

    @GetMapping(value = "/admin/searchUsers")
    @ResponseBody
    public ModelAndView searchUsers(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<User> list=userService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<User> pageTypeEntity = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageTypeEntity.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allUsers",pageTypeEntity.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchUsers");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("admin/users");
        return modelAndView;
    }
}
