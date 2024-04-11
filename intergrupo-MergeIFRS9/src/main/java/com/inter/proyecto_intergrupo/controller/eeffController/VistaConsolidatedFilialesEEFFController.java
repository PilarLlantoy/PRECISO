package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ConcilFiliales;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.ConcilFilialesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class VistaConsolidatedFilialesEEFFController {

    private static final int PAGINATIONCOUNT = 16;

    @Autowired
    private UserService userService;

    @Autowired
    ConcilFilialesService concilFilialesService;

    @GetMapping(value = "/eeffConsolidated/ConcilFilialesEEFF")
    public ModelAndView showTemplateParametricConsolidated(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        if (userService.validateEndpoint(user.getUsuario(), "Ver EEFF Consolidado")) {

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
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<ConcilFiliales> pageType = concilFilialesService.getAll(pageRequest, todayString);
            List<Object[]> listResum = concilFilialesService.getLevel1(todayString);
            List<Object[]> listResum39 = concilFilialesService.getLevel2(todayString);
            int totalPage = pageType.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("directory", "ConcilFilialesEEFF");
            modelAndView.addObject("registers", pageType.getTotalElements());
            modelAndView.addObject("listaDeDatosConsolidado", pageType.getContent());
            modelAndView.addObject("listaResum", listResum);
            modelAndView.addObject("listaResum39", listResum39);
            modelAndView.setViewName("eeffConsolidated/ConcilFilialesEEFF");

            boolean confirmado = concilFilialesService.validarConfirmado(todayString);
            modelAndView.addObject("confirmado", confirmado);
        }
        return modelAndView;
    }
}
