package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@Controller
public class ValoresPucConsolidatedController {

    private static final int PAGINATIONCOUNT = 12;



    @Autowired
    private ValoresPucConsolidatedService valoresPucConsolidatedService;

   /* @Autowired
    //*private RiskService riskService;* */

    @Autowired
    private DatesLoadEeffService datesLoadEeffService;

    @Autowired
    private UserService userService;



    @PostMapping(value = "/eeffConsolidated/Pucvalores")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/filialesValores");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Log_Cargue_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = valoresPucConsolidatedService.saveFileBD(fileContent, params.get("period").toString());

            String[] part = list.get(0);

            if (part[2].equals("SUCCESS")) {
                valoresPucConsolidatedService.loadAudit(user, "Cargue exitoso plantilla PUC Valores");
                Date fechaCargue = new Date();
                datesLoadEeffService.guardarFechasEnTabla("Valores", params.get("period").toString(), "PUC" , fechaCargue);
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            } else {
                valoresPucConsolidatedService.loadAudit(user, "Cargue fallido plantilla PUC Valores");
                ValoresEeffConsolidatedListReport rulesDQListReport = new ValoresEeffConsolidatedListReport(list, null);
                rulesDQListReport.exportLog(response);
            }

            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("vFilter", params.get("period").toString());

        }

        catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return modelAndView;
    }
}