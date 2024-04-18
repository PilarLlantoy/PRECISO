package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.accountsReceivable.AccountCc;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricEEFF;

import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.ParametricEEFFListReport;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.ParametricEEFFService;
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
public class VistaParametricConsolidatedController {

    private static final int PAGINATIONCOUNT = 12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ParametricEEFFService parametricEEFFService;
    private List<String> listColumns = List.of("Parametro", "Concepto", "Cuenta", "Cuenta Aux", "Porcentaje");

    @GetMapping(value = "/eeffConsolidated/parametricsConsolidated")
    public ModelAndView VistaParametric(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if (userService.validateEndpoint(user.getId(), "Ver Parametrica EEFF Consolidado")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<ParametricEEFF> pageAccount = parametricEEFFService.getAll(pageRequest);
            int totalPage = pageAccount.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allParametric", pageAccount.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "parametricsConsolidated");
            modelAndView.addObject("registers", pageAccount.getTotalElements());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("/eeffConsolidated/parametricsConsolidated");
        } else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/Cargarparametrics")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsConsolidated");
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
            ArrayList<String[]> list = parametricEEFFService.saveFileBD(fileContent, user);
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

    @GetMapping(value = "/eeffConsolidated/modifyAccountParametric/{id}")
    @ResponseBody
    public ModelAndView modifyAccountParametric(@PathVariable String id) {
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
        ParametricEEFF toModify = parametricEEFFService.findByIdTipoParametro(idTemp);
        modelAndView.addObject("accountCcModify", toModify);
        modelAndView.setViewName("eeffConsolidated/modifyAccountParametric");
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/modifyAccountParametric")
    @ResponseBody
    public ModelAndView updateAccountParametric(@ModelAttribute ParametricEEFF accountCc) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsConsolidated");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            parametricEEFFService.modifyAccount(accountCc, user);
            modelAndView.addObject("resp", "Modify1");
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;

    }
    @GetMapping(value = "/eeffConsolidated/addAccountParametric")
    public ModelAndView showAddAccountParametric() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        ParametricEEFF accountCc = new ParametricEEFF();
        modelAndView.addObject("accountCc", accountCc);
        modelAndView.setViewName("/eeffConsolidated/addAccountParametric");
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/addAccountParametric")
    public ModelAndView createNewAccountCc(@ModelAttribute ParametricEEFF accountCc) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsConsolidated");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            parametricEEFFService.saveAccount(accountCc, user);
            modelAndView.addObject("resp", "Add1");
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/removeAccountParametric/{id}")
    @ResponseBody
    public ModelAndView removeAccountParametric(@PathVariable String id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ParametricEEFF toRemove = parametricEEFFService.findByIdTipoParametro(Long.parseLong(id));
        parametricEEFFService.removeAccount(toRemove.getIdTipoParametro(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsConsolidated");
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/clearAccountParametric")
    public ModelAndView clearAccountParametric() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        parametricEEFFService.clearAccount(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/parametricsConsolidated");
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/Parametricsdownload")
    @ResponseBody
    public void DescargarExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Cuentas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ParametricEEFF> accountCcList = new ArrayList<ParametricEEFF>();
        if ((params.get("vFilter").toString()).equals("Original") || params.get("vFilter") == null || (params.get("vFilter").toString()).equals("")) {
            accountCcList = parametricEEFFService.findAll();
        } else {
            accountCcList = parametricEEFFService.findByFilter(params.get("vId").toString(), params.get("vFilter").toString());
        }
        ParametricEEFFListReport listReport = new ParametricEEFFListReport(null, accountCcList);
        listReport.export(response);
    }

    @GetMapping(value = "/eeffConsolidated/searchParametric")
    @ResponseBody
    public ModelAndView searchAccountCc(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page = params.get("page") == null ? 0 : (Integer.valueOf(params.get("page").toString()) - 1);
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
        List<ParametricEEFF> list = parametricEEFFService.findByFilter(params.get("vId").toString(), params.get("vFilter").toString());

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ParametricEEFF> pageAccountCc = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage = pageAccountCc.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }
        modelAndView.addObject("allParametric", pageAccountCc.getContent());
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
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        modelAndView.setViewName("/eeffConsolidated/parametricsConsolidated");
        return modelAndView;
    }


}
