package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.DatesLoadEeffService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.FiduciariaEeffConsolidatedListReport;

import com.inter.proyecto_intergrupo.service.eeffconsolidated.ValoresEeffConsolidatedService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


@Controller
public class ValoresEeffConsolidatedController {

    private static final int PAGINATIONCOUNT = 12;

    @Autowired
    private ValoresEeffConsolidatedService valoresEeffConsolidatedService;

    @Autowired
    private DatesLoadEeffService datesLoadEeffService;


   /* @Autowired
    //*private RiskService riskService;* */

    @Autowired
    private UserService userService;


    @PostMapping(value = "/eeffConsolidated/EEFFvalores")
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
            ArrayList<String[]> list = valoresEeffConsolidatedService.saveFileBD(fileContent, params.get("period").toString());

            String[] part = list.get(0);

            if (part[2].equals("SUCCESS")) {
                    valoresEeffConsolidatedService.loadAudit(user, "Cargue exitoso plantilla EEFF Valores");
                    Date fechaCargue = new Date(); // Obtener la fecha actual
                    datesLoadEeffService.guardarFechasEnTabla("Valores", params.get("period").toString(), "Eeff" , fechaCargue);
                    modelAndView.addObject("resp", "AddRep1");
                    modelAndView.addObject("row", part[0]);
                    modelAndView.addObject("colum", part[1]);


            } else {
                valoresEeffConsolidatedService.loadAudit(user, "Cargue fallido plantilla EEFF Valores");
                FiduciariaEeffConsolidatedListReport rulesDQListReport = new FiduciariaEeffConsolidatedListReport(list, null);
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


    @RequestMapping(value = "/eeffConsolidated/DescargarEeffValores")
    public void descargarEeffValores(HttpServletResponse response,
                              @RequestParam(value = "period", required = false) String periodo
    ) {
        try {
            response.setContentType("application/vnd.ms-excel");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String todayString = "";

            if (StringUtils.isEmpty(periodo)) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);

                if (calendar.get(Calendar.MONTH) == 0) {
                    calendar.add(Calendar.YEAR, -1);
                    todayString = calendar.get(Calendar.YEAR) + "-12";
                } else {
                    todayString = String.format("%d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
                }
            } else {
                todayString = periodo;
            }
            valoresEeffConsolidatedService.descargarEeffValores(response, todayString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
