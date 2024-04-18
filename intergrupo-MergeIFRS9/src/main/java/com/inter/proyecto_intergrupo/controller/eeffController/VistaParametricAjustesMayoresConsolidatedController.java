package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionInicialDetalle;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricAjustesMayoresEEFF;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricAjustesMinimosEEFF;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.ParametricAjustesMayoresEEFFService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.ParametricAjustesMinimosEEFFService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.ParametricEEFFAjustesMayoresListReport;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.ParametricEEFFAjustesMinimosListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class VistaParametricAjustesMayoresConsolidatedController {

    private static final int PAGINATIONCOUNT = 12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ParametricAjustesMayoresEEFFService parametricAjustesMayoresEEFFService;


    private List<String> listColumns = List.of("Cuenta", "Moneda", "Saldo", "Concepto", "Periodo");

    @GetMapping(value = "/eeffConsolidated/parametricsAjustesMayores")
    public ModelAndView VistaParametricAjustesMayores(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if (userService.validateEndpoint(user.getId(), "Ver Parametrica Ajustes Mayores EEFF Consolidado")) {

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

            int page1 = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest2 = PageRequest.of(page1, PAGINATIONCOUNT);
            Page<ParametricAjustesMayoresEEFF> pageType = parametricAjustesMayoresEEFFService.getAll1(pageRequest2, todayString);
            int totalPage = pageType.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allParametric1", pageType.getContent());
            modelAndView.addObject("current", page1 + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
        //    modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "parametricsConsolidated");
            modelAndView.addObject("registers", pageType.getTotalElements());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("/eeffConsolidated/parametricsAjustesMayores");
        } else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/CargarparametricsMayores")
    public ModelAndView uploadFileMayores(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMayores");
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
            ArrayList<String[]> list = parametricAjustesMayoresEEFFService.saveFileBDAjustesMayores(fileContent, params.get("period").toString(), user);
            String[] part = list.get(0);

            if (part[2].equals("SUCCESS")) {
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("period", params.get("period").toString());
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            } else {
                SignatureListReport signatureListReport = new SignatureListReport(list, null);
                signatureListReport.exportLog(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/modifyAccountAjusteMayoresParametric/{id}")
    @ResponseBody
    public ModelAndView modifyAccountParametricMayores(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Long idTemp = null;
        try {
            idTemp = Long.parseLong(id);

        } catch (Exception e) {
            e.printStackTrace();
        }
        ParametricAjustesMayoresEEFF toModify = parametricAjustesMayoresEEFFService.findByIdTipoParametro(idTemp);
        modelAndView.addObject("accountCcModify", toModify);
        modelAndView.setViewName("eeffConsolidated/modifyAccountAjusteMayoresParametric");
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/modifyAccountAjusteMayoresParametric")
    @ResponseBody
    public ModelAndView updateAccountParametricMayores(@ModelAttribute ParametricAjustesMayoresEEFF accountCc) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMayores");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            parametricAjustesMayoresEEFFService.modifyAccount1(accountCc, user);
            modelAndView.addObject("resp", "Modify1");
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;

    }
    @GetMapping(value = "/eeffConsolidated/addAccountAjusteMayoresParametric")
    public ModelAndView showAddAccountParametricMayores(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        ParametricAjustesMayoresEEFF accountCc = new ParametricAjustesMayoresEEFF();
        modelAndView.addObject("accountCc", accountCc);
        System.out.println(params.get("period").toString());
        modelAndView.addObject("period", params.get("period").toString());
        modelAndView.setViewName("/eeffConsolidated/addAccountAjusteMayoresParametric");
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/addAccountAjusteMayoresParametric")
    public ModelAndView createNewAccountCcMayores(@ModelAttribute ParametricAjustesMayoresEEFF accountCc, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMayores");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            accountCc.setPeriodo(params.get("period").toString());
            parametricAjustesMayoresEEFFService.saveAccount1(accountCc, user);
            modelAndView.addObject("resp", "Add1");
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
            e.printStackTrace();
        }
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/removeAccountParametricMayores/{id}")
    @ResponseBody
    public ModelAndView removeAccountParametricMayores(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ParametricAjustesMayoresEEFF toRemove = parametricAjustesMayoresEEFFService.findByIdTipoParametro(Long.parseLong(id));
        parametricAjustesMayoresEEFFService.removeAccount1(toRemove.getIdTipoParametro(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMayores");
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/clearAccountParametricMayores")
    public ModelAndView clearAccountParametricMayores() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        parametricAjustesMayoresEEFFService.clearAccount1(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMayores");
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/ParametricsdownloadMayores")
    @ResponseBody
    public void DescargarExcelMayores(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String periodo = params.get("period").toString();
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Parametria_Ajustes_Mayores_" + periodo + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ParametricAjustesMayoresEEFF> accountCcList = new ArrayList<ParametricAjustesMayoresEEFF>();
        accountCcList = parametricAjustesMayoresEEFFService.findByPeriodo(params.get("period").toString());
        ParametricEEFFAjustesMayoresListReport listReport = new ParametricEEFFAjustesMayoresListReport(null, accountCcList);
        listReport.export(response);
    }

    @GetMapping("/eeffConsolidated/ProcesarAjustesMayores")
    public ModelAndView ProcesarAjustesMinimos(@RequestParam("period") String periodo)
    {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMayores");
        parametricAjustesMayoresEEFFService.ProcesarAjustesMayores(periodo);
        modelAndView.addObject("resp", "ProcessExi");
        modelAndView.addObject("period", periodo);
        return modelAndView;
    }
}
