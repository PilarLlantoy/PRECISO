package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.DatesLoadEeFF;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.DatesLoadEeffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller

public class VistaVerificationEliminacionesController {
    private static final int PAGINATIONCOUNT = 12;

    @Autowired
    private UserService userService;

    @Autowired
    private DatesLoadEeffService datesLoadEeffService;

    @GetMapping(value = "/eeffConsolidated/VerificacionEliminaciones")
    public ModelAndView showTemplateEliminacionesVerification(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if (userService.validateEndpoint(user.getUsuario(), "Ver Intergrupo Verificacion Eliminaciones")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            String todayString = "";
            if (params.get("period") == null || params.get("period").toString() == "") {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                if (calendar.get(Calendar.MONTH) == 0) {
                    calendar.add(Calendar.YEAR, -1);
                    todayString = calendar.get(Calendar.YEAR) + "-12";
                } else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
                } else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
                }
            } else {
                todayString = params.get("period").toString();
            }
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("period", todayString);

            modelAndView.setViewName("eeffConsolidated/VerificacionEliminaciones");

            List<StatusInfo> eeffDataListVerificacionEliminaciones = datesLoadEeffService.getEliminacionesByPeriodo(todayString);
            modelAndView.addObject("listaDeDatosVerificadosEliminaciones", eeffDataListVerificacionEliminaciones);
        }  else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }
}
