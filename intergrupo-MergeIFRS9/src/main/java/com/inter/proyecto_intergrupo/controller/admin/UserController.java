package com.inter.proyecto_intergrupo.controller.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inter.proyecto_intergrupo.model.admin.*;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.*;
import com.inter.proyecto_intergrupo.service.parametricServices.ConciliationService;
import com.inter.proyecto_intergrupo.service.parametricServices.TypeEntityListReport;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import com.inter.proyecto_intergrupo.utility.Utility;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class UserController {

    private static final int PAGINATIONCOUNT=12;
    private List<String> listColumns=List.of("Código","Documento","Tipo Documento","Nombre", "Perfil", "Cargo","Estado");

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TipoDocumentoService tipoDocumentoService;

    @Autowired
    private CargoService cargoService;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private UserConciliationService userConciliationService;

    @Autowired
    SendEmailService sendEmailService;


    @RequestMapping(value="/home")
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        userService.loadAudit(user);
        modelAndView.addObject(user);
        modelAndView.addObject("userName", user.getUsername());
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
        modelAndView.addObject("userName", user.getPrimerNombre());
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
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Usuarios");
        if(userService.validateEndpointVer(user.getId(),"Ver Usuarios")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            List<User> list = userService.findAll();
            Page<User> pageType = userService.getAll(pageRequest);
            int totalPage = pageType.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }


            List<Conciliation> titulares = new ArrayList<>();
            List<Conciliation> backups = new ArrayList<>();

            List<Conciliation> conciliaciones = conciliationService.findAllActive();
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.addObject("allUsers", pageType.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "users");
            modelAndView.addObject("registers",list.size());
            modelAndView.addObject("conciliaciones",conciliaciones);
            modelAndView.addObject("titulares",titulares);
            modelAndView.addObject("backups",backups);

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

    @GetMapping(value = "/admin/modifyUsuario/{id}")
    @ResponseBody
    public ModelAndView modifyUsers(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        System.out.println("HOLAAA");
            modelAndView.addObject("userName", user.getUsuario());
            modelAndView.addObject("userEmail", user.getCorreo());
            List<Role> allRoles = roleService.findAllActiveRoles();
            List<Cargo> allCargos = cargoService.findAllActiveCargos();
            modelAndView.addObject("roles", allRoles);
            List<TipoDocumento> allTipos = tipoDocumentoService.findAll();
            modelAndView.addObject("tipos", allTipos);
            modelAndView.addObject("cargos", allCargos);
            User toModify = userService.findUserByUserName(id);
            modelAndView.addObject("userModify", toModify);
            Set<Role> userRoles = toModify.getRoles();
            List<Role> finalRoles = new ArrayList<>(userRoles);
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String hasRoles = gson.toJson(finalRoles);
            modelAndView.addObject("hasRoles", hasRoles);
            modelAndView.setViewName("admin/modifyUsuario");

        return modelAndView;
    }

    @PostMapping(value = "/admin/modifyUsers")
    public ModelAndView updateUser(
            @ModelAttribute User user,
            @RequestParam(name = "selectedRoles", defaultValue = "N") String[] roles,
            @RequestParam(name = "selectedCargo") String cargo,
            @RequestParam(name = "selectedTipoDoc") String tipodoc,
            @RequestParam(name = "newU") String id,
            @RequestParam(name = "validaRoles", defaultValue = "OK") String validaRoles,
            BindingResult bindingResult){

        ModelAndView modelAndView = new ModelAndView("redirect:/admin/searchUsers?vId=activo&vFilter=Estado");
        //Verifica que no haya usuario y correo repetido
        User userExists = userService.findUserByUserName(user.getUsuario());
        User userByEmail = userService.findUserByEmail(user.getCorreo());

        System.out.println(roles + " " + roles[0]);
        if(roles[0].equals("N") || roles.length == 0) {
            bindingResult
                    .rejectValue("roles", "error.roles",
                            "Roles no puede estar vacio");
            System.out.println("TENEMOS UN ERROR");
            validaRoles="NOTok";
        }

        if(bindingResult.hasErrors()) {
            modelAndView.addObject("bindingResult", bindingResult);

            System.out.println("ENTRAMOS A ERROR");
            modelAndView.addObject("userName", user.getUsuario());
            modelAndView.addObject("userEmail", user.getCorreo());
            List<Role> allRoles = roleService.findAll();
            List<Cargo> allCargos = cargoService.findAll();
            List<TipoDocumento> allTipos = tipoDocumentoService.findAll();
            modelAndView.addObject("roles", allRoles);
            modelAndView.addObject("tipos", allTipos);
            modelAndView.addObject("cargos", allCargos);
            modelAndView.addObject("userModify", user);
            modelAndView.addObject("validaRoles", validaRoles);
            Set<Role> userRoles = null;
            if(roles[0].equals("N")){
                userRoles = userService.findUserByUserName(user.getUsuario()).getRoles();
            }
            else{
                userRoles = new HashSet<>();
                for (String role : roles) {
                    Role myRole = roleService.findRole(role);
                    userRoles.add(myRole);
                }
            }
            List<Role> finalRoles = new ArrayList<>(userRoles);
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String hasRoles = gson.toJson(finalRoles);
            modelAndView.addObject("hasRoles", hasRoles);

            Cargo cargoseleccionado = cargoService.findCargoByNombre(cargo);
            user.setCargo(cargoseleccionado);
            TipoDocumento tiposeleccionado = tipoDocumentoService.findTipoDocumentoByNombre(tipodoc);
            user.setTipoDocumento(tiposeleccionado);
            Set<Role> newRoles = new HashSet<Role>();
            for (String role : roles) {
                Role myRole = roleService.findRole(role);
                newRoles.add(myRole);
            }
                user.setRoles(newRoles);
            modelAndView.addObject("userModify",user);
            modelAndView.setViewName("admin/modifyUsuario");
        }
        else{
            Set<Role> newRoles = new HashSet<Role>();
            for (String role : roles) {
                Role myRole = roleService.findRole(role);
                newRoles.add(myRole);
            }
            Cargo newCargo = cargoService.findCargoByNombre(cargo);
            user.setCargo(newCargo);
            TipoDocumento newTipoDoc = tipoDocumentoService.findTipoDocumentoByNombre(tipodoc);
            user.setTipoDocumento(newTipoDoc);
            User searchUser = userService.findUserByUserName(user.getUsuario());
            User searchUserOld = userService.findUserByUserName(id);
            List<Role> allRoles = roleService.findAll();
            modelAndView.addObject("roles", allRoles);
            modelAndView.addObject("message", "Usuario modificado");

            if(searchUser!=null) {
                userService.modifyUser(user, searchUser.getCreacion(), newRoles, searchUser.getId());
            }
            else {
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
        }

        return  modelAndView;
    }

    @GetMapping(value="/admin/createUser")
    public ModelAndView registration(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user1 = userService.findUserByUserName(auth.getName());

        User user = new User();
        TipoDocumento tipodoc = new TipoDocumento();
        user.setTipoDocumento(tipodoc);
        modelAndView.addObject("user", user);

        List<Role> allRoles = roleService.findAll();
        List<Cargo> allCargos = cargoService.findAll();
        List<TipoDocumento> allTipos = tipoDocumentoService.findAll();
        modelAndView.addObject("tipos", allTipos);
        modelAndView.addObject("cargos", allCargos);
        modelAndView.addObject("roles", allRoles);
        modelAndView.setViewName("admin/createUser");
        return modelAndView;
    }

    @PostMapping(value = "/admin/createUser")
    public ModelAndView createNewUser(
            @ModelAttribute User user,
            @RequestParam(name = "selectedTipoDoc") String tipodoc,
            @RequestParam(name = "selectedCargo") String cargo,
            @RequestParam(name = "selectedRoles", defaultValue = "N") String[] roles,
            BindingResult bindingResult, HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView("redirect:/admin/searchUsers?vId=activo&vFilter=Estado");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List <Role> allRoles = roleService.findAll();
        modelAndView.addObject("roles",allRoles);

        //Verifica que no haya usuario y correo repetido
        User userExists = userService.findUserByUserName(user.getUsuario());
        User userByEmail = userService.findUserByEmail(user.getCorreo());

        if(roles[0].equals("N")) {
            bindingResult
                    .rejectValue("roles", "error.roles",
                            "Roles no puede estar vacio");
        }

        if (userExists != null) {
            bindingResult
                    .rejectValue("usuario", "error.usuario",
                            "El usuario ya se encuentra registrado");
        }
        if(userByEmail != null){
            bindingResult
                    .rejectValue("correo",
                            "error.correo",
                            "El correo ya se encuentra registrado");
        }
        if(bindingResult.hasErrors()) {
            List<Cargo> allCargos = cargoService.findAll();
            List<TipoDocumento> allTipos = tipoDocumentoService.findAll();
            modelAndView.addObject("tipos", allTipos);
            modelAndView.addObject("cargos", allCargos);
            Cargo cargoseleccionado = cargoService.findCargoByNombre(cargo);
            user.setCargo(cargoseleccionado);
            TipoDocumento tiposeleccionado = tipoDocumentoService.findTipoDocumentoByNombre(tipodoc);
            user.setTipoDocumento(tiposeleccionado);

            modelAndView.setViewName("admin/createUser");
        }
        else{
            HashSet<Role> userRoles = new HashSet<>();
            for(int i =0; i< roles.length;i++){
                try{
                    Role myRole = roleService.findRole(roles[i]);
                    userRoles.add(myRole);
                }catch(Exception e){}
            }
            TipoDocumento newTipoDoc = tipoDocumentoService.findTipoDocumentoByNombre(tipodoc);
            user.setTipoDocumento(newTipoDoc);
            Cargo newCargo = cargoService.findCargoByNombre(cargo);
            user.setCargo(newCargo);

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
        String subject = "Nuevo usuario Preciso";

        String content = "<p>Hola,</p>"
                + "<p>Bienvenido al aplicativo BBVA Preciso</p>"
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


        System.out.println("PARAMS "+params.get("vId").toString()+" "+params.get("vFilter").toString());
        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<User> list;
        if(params==null) list=userService.findByFilter("inactivo", "Estado");
        else list=userService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<User> pageTypeEntity = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageTypeEntity.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        List<Conciliation> conciliaciones = conciliationService.findAllActive();

        List<Conciliation> titulares = new ArrayList<>();
        List<Conciliation> backups = new ArrayList<>();

        System.out.println("Conciliaciones donde el usuario es titular: " + titulares);
        System.out.println("Conciliaciones donde el usuario es backup: " + backups);

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

        modelAndView.addObject("conciliaciones",conciliaciones);
        modelAndView.addObject("titulares",titulares);
        modelAndView.addObject("backups",backups);


        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Usuarios");

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("p_modificar", p_modificar);

        modelAndView.setViewName("admin/users");
        return modelAndView;
    }

    //LDAP PARA INICIO DE SESION
    public boolean validarIngreso(User usuario, String contraseña) throws SQLException {

        User valida;
        boolean result;
        LDAP ldap = new LDAP();
        valida = userService.findUserByUserName(usuario.getUsuario());

        if (valida == null){ result = false; }
        else {
            String res = ldap.inicializarLDAP(usuario.getUsuario(), contraseña);
            if (res.contains("exitosa")) { result = true; }
            else {
                String mss = "Error. ";
                if (res.contains("52e")) { mss += "Credenciales no válidas."; }
                if (res.contains("525")) { mss += "Usuario no encontrado"; }
                if (res.contains("532")) { mss += "Contraseña caducada."; }
                if (res.contains("773")) { mss += "El usuario debe restablecer la contraseña en Intranet."; }
                if (res.contains("775")) { mss += "Cuenta de usuario bloqueado, restablezca en intranet."; }
                result = false;
            }
        }
        return result;
    }

/*
    @PostMapping("/guardarConciliaciones")
    public ResponseEntity<?> guardarConciliaciones(@RequestParam Integer usuarioId, @RequestParam List<String> titulares,
                                                   @RequestParam List<String> backups) {

        System.out.println(usuarioId+" "+ titulares.size()+" "+ backups.size());



        // Buscar al usuario y la conciliación en la base de datos
        User user = userService.findById(usuarioId);
        Conciliation conciliacion = conciliationService.findById(conciliacionId);

        if (rol.equals("TITULAR")) {
            conciliationService.assignUserToConciliation(user, conciliacion, UserConciliation.RoleConciliation.TITULAR);
        } else if (rol.equals("BACKUP")) {
            conciliationService.assignUserToConciliation(user, conciliacion, UserConciliation.RoleConciliation.BACKUP);
        }

        return ResponseEntity.ok("Los cambios se guardaron correctamente.");
    }
*/
    @ResponseBody
    @PostMapping("/guardarConciliaciones")
    public ResponseEntity<String> guardarConciliaciones(@RequestBody Map<String, List<String>> params) {

        // Extraer titulares y backups del request

        String usuario = params.get("usuario").get(0);
        List<String> concilTitulares = params.get("titulares");
        List<String> concilBackups = params.get("backups");
        try {
            System.out.println(usuario);
            for(String e:concilTitulares)
                System.out.println(e);
            for(String i:concilBackups)
                System.out.println(i);
            conciliationService.generarRelacionUserConciliation(usuario, concilTitulares, concilBackups);
            // Responder con éxito
            return ResponseEntity.ok("Asignaciones correctas.");
        } catch (Exception e) {
            // Manejo de errores
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al asignar usuarios: " + e.getMessage());
        }
    }


    @PostMapping("/parametric/obtenerTitularesBackups")
    @ResponseBody
    public String obtenerTitulares(@RequestParam("usuario") Integer id,
                                  Model model) {

        System.out.println("USUARIO: "+id);

        List<Conciliation> titulares = userConciliationService.getConciliationsByUserAndRole(id, UserConciliation.RoleConciliation.TITULAR);
        List<Conciliation> backups = userConciliationService.getConciliationsByUserAndRole(id, UserConciliation.RoleConciliation.BACKUP);

        System.out.println(backups.size());
        List<Conciliation> conciliaciones = conciliationService.findAllActive();

        // Guardar los datos en el modelo para enviarlos de vuelta a la vista
        model.addAttribute("titulares",titulares);
        model.addAttribute("backups",backups);

        StringBuilder tablaHtml = new StringBuilder();
        tablaHtml.append("<table id='parametros' class='table table-striped table-hover text-center table-bordered table-sm' width='100%'>");
        tablaHtml.append("<thead class=\"bg-primary\">\n" +
                "                                    <tr>\n" +
                "                                        <th>Código</th>\n" +
                "                                        <th>Detalle Conciliación</th>\n" +
                "                                        <th>Titular</th>\n" +
                "                                        <th>Back Up</th>\n" +
                "                                    </tr>\n" +
                "                                    </thead>\n" +
                "                                    <tbody>");



        // Crear filas
        for (int index = 0; index < conciliaciones.size(); index++) {
            Conciliation concil = conciliaciones.get(index);
            tablaHtml.append("<tr>");

            // Agregar número de fila (código)
            tablaHtml.append("<td>").append(index).append("</td>");
            tablaHtml.append("<td>").append(concil.getNombre()).append("</td>");

            // Agregar el checkbox para el titular
            boolean isTitularChecked = titulares.stream().anyMatch(t -> t.getId() == concil.getId());
            tablaHtml.append("<td><input class='big-checkbox ver-checkbox' type='checkbox' ")
                    .append("value='").append(concil.getId()).append("' ")
                    .append("name='titular' id='titular_").append(concil.getId()).append("' ")
                    .append(isTitularChecked ? "checked" : "").append(" />")
                    .append("</td>");

            // Agregar el checkbox para el backup
            boolean isBackupChecked = backups.stream().anyMatch(t -> t.getId() == concil.getId());
            tablaHtml.append("<td><input class='big-checkbox modificar-checkbox' type='checkbox' ")
                    .append("value='").append(concil.getId()).append("' ")
                    .append("name='backup' id='backup_").append(concil.getId()).append("' ")
                    .append(isBackupChecked ? "checked" : "").append(" />")
                    .append("</td>");

            tablaHtml.append("</tr>");
        }

        tablaHtml.append("</tbody></table>");

        model.addAttribute("tablaHtml", tablaHtml.toString());

        // Retorna la vista
        return tablaHtml.toString(); // Ajusta esto según la vista que deseas retornar

    }





}
