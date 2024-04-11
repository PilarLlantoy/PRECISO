package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricAjustesMinimosEEFF;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricEEFF;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.*;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
public class VistaParametricAjustesMinimosConsolidatedController {

    private static final int PAGINATIONCOUNT = 12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ParametricAjustesMinimosEEFFService parametricAjustesMinimosEEFFService;

    @Autowired
    ConcilFilialesService concilFilialesService;

    private List<String> listColumns = List.of("Cuenta Origen", "Empresa Origen", "Empresa Destino", "Cuenta destino");

    @GetMapping(value = "/eeffConsolidated/parametricsAjustesMinimos")
    public ModelAndView VistaParametricAjustesMinimos(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if (userService.validateEndpoint(user.getUsuario(), "Ver Parametrica Ajustes Minimos EEFF Consolidado")) {

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
            Page<ParametricAjustesMinimosEEFF> pageAccount = parametricAjustesMinimosEEFFService.getAll1(pageRequest);
            int totalPage = pageAccount.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allParametric1", pageAccount.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "parametricsConsolidated");
            modelAndView.addObject("registers", pageAccount.getTotalElements());
            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("/eeffConsolidated/parametricsAjustesMinimos");

            boolean confirmado = concilFilialesService.validarConfirmado(todayString);
            modelAndView.addObject("confirmado", confirmado);

        } else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/Cargarparametrics1")
    public ModelAndView uploadFile8(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMinimos");
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
            ArrayList<String[]> list = parametricAjustesMinimosEEFFService.saveFileBDAjustesMinimos(fileContent, user);
            String[] part = list.get(0);

            if (part[2].equals("SUCCESS")) {
                modelAndView.addObject("resp", "AddRep1");
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

    @GetMapping(value = "/eeffConsolidated/modifyAccountAjusteMinimoParametric/{id}")
    @ResponseBody
    public ModelAndView modifyAccountParametric0(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Long idTemp = null;
        try {
            idTemp = Long.parseLong(id);

        } catch (Exception e) {
            e.printStackTrace();
        }
        ParametricAjustesMinimosEEFF toModify = parametricAjustesMinimosEEFFService.findByIdTipoParametro(idTemp);
        modelAndView.addObject("accountCcModify", toModify);
        modelAndView.setViewName("eeffConsolidated/modifyAccountAjusteMinimoParametric");
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/modifyAccountAjusteMinimoParametric")
    @ResponseBody
    public ModelAndView updateAccountParametric0(@ModelAttribute ParametricAjustesMinimosEEFF accountCc) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMinimos");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            parametricAjustesMinimosEEFFService.modifyAccount1(accountCc, user);
            modelAndView.addObject("resp", "Modify1");
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;

    }
    @GetMapping(value = "/eeffConsolidated/addAccountAjusteMinimoParametric")
    public ModelAndView showAddAccountParametric0() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        ParametricAjustesMinimosEEFF accountCc = new ParametricAjustesMinimosEEFF();
        modelAndView.addObject("accountCc", accountCc);
        modelAndView.setViewName("/eeffConsolidated/addAccountAjusteMinimoParametric");
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/addAccountAjusteMinimoParametric")
    public ModelAndView createNewAccountCc0(@ModelAttribute ParametricAjustesMinimosEEFF accountCc) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMinimos");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            parametricAjustesMinimosEEFFService.saveAccount1(accountCc, user);
            modelAndView.addObject("resp", "Add1");
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/removeAccountParametric1/{id}")
    @ResponseBody
    public ModelAndView removeAccountParametric0(@PathVariable String id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ParametricAjustesMinimosEEFF toRemove = parametricAjustesMinimosEEFFService.findByIdTipoParametro(Long.parseLong(id));
        parametricAjustesMinimosEEFFService.removeAccount1(toRemove.getIdTipoParametro(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMinimos");
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/clearAccountParametric1")
    public ModelAndView clearAccountParametric0() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        parametricAjustesMinimosEEFFService.clearAccount1(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMinimos");
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/Parametricsdownload1")
    @ResponseBody
    public void DescargarExcel0(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Parametria_Ajustes_Minimos_ " + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ParametricAjustesMinimosEEFF> accountCcList = new ArrayList<ParametricAjustesMinimosEEFF>();
        if ((params.get("vFilter").toString()).equals("Original") || params.get("vFilter") == null || (params.get("vFilter").toString()).equals("")) {
            accountCcList = parametricAjustesMinimosEEFFService.findAll();
        } else {
            accountCcList = parametricAjustesMinimosEEFFService.findByFilter(params.get("vId").toString(), params.get("vFilter").toString());
        }
        ParametricEEFFAjustesMinimosListReport listReport = new ParametricEEFFAjustesMinimosListReport(null, accountCcList);
        listReport.export(response);
    }

    @GetMapping(value = "/eeffConsolidated/searchParametric1")
    @ResponseBody
    public ModelAndView searchAccountCc0(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page = params.get("page") == null ? 0 : (Integer.valueOf(params.get("page").toString()) - 1);
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
        List<ParametricAjustesMinimosEEFF> list = parametricAjustesMinimosEEFFService.findByFilter(params.get("vId").toString(), params.get("vFilter").toString());

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ParametricAjustesMinimosEEFF> pageAccountCc = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage = pageAccountCc.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }
        modelAndView.addObject("allParametric1", pageAccountCc.getContent());
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("vId", params.get("vId").toString());
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("vFilter", params.get("vFilter").toString());
        modelAndView.addObject("columns", listColumns);
        modelAndView.addObject("directory", "searchAccountCc");
        modelAndView.addObject("registers", list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        modelAndView.setViewName("/eeffConsolidated/parametricsAjustesMinimos");
        return modelAndView;
    }

    @GetMapping("/eeffConsolidated/procesarAjustesMinimos")
    public ModelAndView ProcesarAjustesMinimos(@RequestParam("period") String periodo)
    {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsAjustesMinimos");
        parametricAjustesMinimosEEFFService.ProcesarAjustesMinimos(periodo);
        modelAndView.addObject("resp", "ProcessExi");
        return modelAndView;
    }

}
