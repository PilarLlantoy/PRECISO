package com.inter.proyecto_intergrupo.controller;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import com.inter.proyecto_intergrupo.utility.Utility;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ResetPasswordController {

    @Autowired
    UserService userService;
    @Autowired
    SendEmailService sendEmailService;

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @GetMapping(value = "/password/forgotPassword")
    public ModelAndView setForgotPasswordView() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("password/forgotPassword");
        return modelAndView;
    }

    @PostMapping(value = "/password/forgotPassword")
    public ModelAndView processForgotPassword(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        String user = request.getParameter("user");
        User myUser = userService.findUserByUserName(user);
        if (myUser != null) {
            String email = myUser.getCorreo();
            String token = RandomStringUtils.randomAlphanumeric(10);
            try {
                userService.updateResetPasswordToken(token, user);
                String resetPasswordLink = Utility.getSiteURL(request) + "/password/resetPassword?token=" + token;
                sendEmail(email, resetPasswordLink);
                modelAndView.addObject("message", "Se ha enviado un enlace para reestablecer la contraseña");
            } catch (UsernameNotFoundException e) {
                modelAndView.addObject("error", e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                modelAndView.addObject("error", "Error al enviar el mensaje");
                e.printStackTrace();
            }
            modelAndView.setViewName("/password/forgotPassword");
        } else {
            modelAndView.addObject("error", "No se ha encontrado el usuario");
            modelAndView.setViewName("/password/forgotPassword");
        }

        return modelAndView;
    }

    public void sendEmail(String recipientEmail, String link) {
        String subject = "Cambio de contraseña BBVA PRECISO";

        String content = "<p>Hola,</p>"
                + "<p>Ha solicitado recuperar su contraseña</p>"
                + "<p>Ingrese al siguiente enlace para recuperar su contraseña</p>"
                + "<p><a href=\"" + link + "\">Cambiar mi contraseña</a></p>"
                + "<br>"
                + "<p>Ignore este correo si no ha solicitado el cambio de contraseña</p>";

        sendEmailService.sendEmail(recipientEmail, subject, content);
    }

    @GetMapping(value = "/password/resetPassword")
    public ModelAndView setResetPasswordView(@Param(value = "token") String token) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getByResetPasswordToken(token);
        modelAndView.addObject("token", token);
        if (user == null) {
            modelAndView.addObject("error", "Token invalido");
        } else {
            modelAndView.addObject("success", "El token es valido");
        }
        modelAndView.setViewName("password/resetPassword");
        return modelAndView;
    }

    @PostMapping("/password/resetPassword")
    public ModelAndView processResetPassword(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        String token = request.getParameter("token");
        String password = request.getParameter("password");

        User user = userService.getByResetPasswordToken(token);
        modelAndView.addObject("title", "Reset your password");
        modelAndView.addObject("success", "true");
        if (user == null) {
            modelAndView.addObject("errorReset", "No se pudo cambiar la contraseña");
        } else {
            logger.warn(password);
            userService.updatePassword(user, password);
            modelAndView.addObject("successReset", "La contraseña se cambió correctamente");
        }

        modelAndView.setViewName("password/resetPassword");

        return modelAndView;
    }


}
