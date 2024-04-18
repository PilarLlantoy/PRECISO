package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.DatesLoadEeFF;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionInicialCuadreGeneral;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.QueryBanco;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.tablaUnificadaEliminacionesPatrimoniales;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.DatesLoadEeffRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class VistaPatrimonialesEeffController {

    private static final int PAGINATIONCOUNT = 12;

    @Autowired
    private EliminacionesPatrimonio eliminacionesPatrimonio;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/eeffConsolidated/EeffPatrimoniales")
    public ModelAndView showTemplatePatrimoniales(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        if (userService.validateEndpoint(user.getId(), "Ver EEFF Patrimoniales Fiduciaria")) {
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

            List<tablaUnificadaEliminacionesPatrimoniales> list = eliminacionesPatrimonio.getDataPatrimonio(todayString, "Verificación Patrimonio Fiduciaria");
            List<tablaUnificadaEliminacionesPatrimoniales> list1 = eliminacionesPatrimonio.getDataPatrimonio(todayString, "Total Patrimonio Fiduciaria");
            List<tablaUnificadaEliminacionesPatrimoniales> list2 = eliminacionesPatrimonio.getDataPatrimonio(todayString, "Contabilidad Eliminación Fiduciaria");
            List<tablaUnificadaEliminacionesPatrimoniales> list3 = eliminacionesPatrimonio.getDataPatrimonio(todayString, "Validación Contabilidad Eliminación");
            List<tablaUnificadaEliminacionesPatrimoniales> list4 = eliminacionesPatrimonio.getDataPatrimonio(todayString, "Validacion Patrimonio Fiduciaria");
            List<tablaUnificadaEliminacionesPatrimoniales> list5 = eliminacionesPatrimonio.getDataPatrimonio(todayString, "Validacion Final Fiduciaria");

            int page1 = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest2 = PageRequest.of(page1, PAGINATIONCOUNT);
            Page<tablaUnificadaEliminacionesPatrimoniales> pageType = eliminacionesPatrimonio.getAllEliminacion(pageRequest2, todayString, list);
            int totalPage = pageType.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("current", page1 + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("registers",pageType.getTotalElements());
            modelAndView.addObject("directory", "EeffPatrimoniales");
            modelAndView.addObject("ListaEliminacion", list);
            modelAndView.addObject("VerificacionF", list1);
            modelAndView.addObject("ContabilidadE", list2);
            modelAndView.addObject("ValidacionC", list3);
            modelAndView.addObject("ValidacionP", list4);
            modelAndView.addObject("ValidacionFinal", list5);
            modelAndView.setViewName("eeffConsolidated/EeffPatrimoniales");

        } else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }

        return modelAndView;
    }

    @GetMapping("/eeffConsolidated/procesarEliminacionesPatrimonio")
    public ModelAndView ProcesarEliminacionesPatrimonio(@RequestParam("period") String periodo)
    {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/EeffPatrimoniales");
       eliminacionesPatrimonio.ProcesarEliminacion(periodo);
        modelAndView.addObject("resp", "ProcessExi");
        modelAndView.addObject("period", periodo);
        return modelAndView;
    }

}