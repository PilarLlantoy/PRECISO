package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.DatesLoadEeFF;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.DatesLoadEeffService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.FiduciariaSFCconsolidatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


@Controller
public class FiduciariaEeffConsolidatedSFCcontroller {

    @Autowired
    private UserService userService;

    @Autowired
    private DatesLoadEeffService datesLoadEeffService;

    @Autowired
    private FiduciariaSFCconsolidatedService fiduciariaSFCconsolidatedService;


    @PostMapping(value = "/eeffConsolidated/SFC")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/filiales");
        response.setContentType("application/octet-stream");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            InputStream fileContent1 = filePart.getInputStream();


            boolean respuesta = fiduciariaSFCconsolidatedService.ValidarFecha(fileContent, params.get("period").toString());


            if (respuesta == true) {
                fiduciariaSFCconsolidatedService.loadAudit(user, "Cargue exitoso del archivo SFC");
                Date fechaCargue = new Date(); // Obtener la fecha actual
                fiduciariaSFCconsolidatedService.guardarSoporteEnBD(fileContent1, params.get("period").toString(), "Fiduciaria", user);

                datesLoadEeffService.guardarFechasEnTabla("Fiduciaria", params.get("period").toString(), "SFC", fechaCargue);
                modelAndView.addObject("resp", "AddRepFiliales");

            } else {
                fiduciariaSFCconsolidatedService.loadAudit(user, "¡Falla al cargar el Archivo");
                modelAndView.addObject("resp", "AddRepFilialesFallido");
            }

            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("vFilter", params.get("period").toString());

        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/descargarSfc")
    public ModelAndView DescargarSFC(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/filiales");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CopiaSoporteSFC" + currentDateTime + ".txt";
        response.setHeader(headerKey, headerValue);
        try {
            DatesLoadEeFF archivoDesdeBD = fiduciariaSFCconsolidatedService.obtenerSoporteDesdeBD(params.get("rol").toString(),params.get("period").toString());
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(archivoDesdeBD.getSoporteSfcDescarga());
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return modelAndView;
    }
}
