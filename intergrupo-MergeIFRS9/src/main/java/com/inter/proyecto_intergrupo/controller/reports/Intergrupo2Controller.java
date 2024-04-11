package com.inter.proyecto_intergrupo.controller.reports;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1Final;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV2;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV2Final;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.YntpSocietyRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.reportsServices.*;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class Intergrupo2Controller {
    private static final int PAGINATIONCOUNT = 12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    private List<String> listColumns = List.of("Cuenta", "Contrato", "Nit", "Cod Neocon", "Divisa", "YNTP", "Sociedad", "Cod Pais", "Pais");

    @Autowired
    private UserService userService;

    @Autowired
    private Intergrupo2Service intergrupo2Service;

    @Autowired
    private Intergrupo1Service intergrupo1Service;

    @Autowired
    Intergrupo2Service intergrupo2;

    @Autowired
    Intergrupo1Service intergrupo1;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private YntpSocietyRepository yntpSocietyRepository;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @GetMapping(value = "/reports/intergroup2")
    public ModelAndView showInterV2(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if (userService.validateEndpoint(user.getUsuario(), "Ver Intergrupo 2 Banco")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";
            String Id;
            String Vf;
            if (params.get("vId") == null) {
                Id = "";
            } else {
                Id = params.get("vId").toString();
            }
            if (params.get("vFilter") == null) {
                Vf = "";
            } else {
                Vf = params.get("vFilter").toString();
            }

            if (params.get("period") == null || (params.get("period") != null && params.get("period").toString() == "")) {
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

            List<IntergrupoV2> list;
            if (Id == "" | Vf == "") {
                list = intergrupo2.getAllFromV2(todayString);
            } else {
                list = intergrupo2.findByFilter(Id, Vf, todayString);
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<IntergrupoV2> pageInter = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageInter.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            List<Object[]> neocon = intergrupo2.getNeoconInter(todayString);

            List<Object[]> validateInterAjuV2 = intergrupo2.validateTableInterV2Aju(todayString);

            if (!validateInterAjuV2.isEmpty()) {
                modelAndView.addObject("validateInterAjuV2", true);
                Date dateInterAjuV2 = new Date();
                try {
                    dateInterAjuV2 = StatusInfoRepository.findByInputAndPeriodo("INTER_V2_AJU", todayString).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateInterAjuV2);
                modelAndView.addObject("dateInterAjuV2", dateAsString);
            } else {
                modelAndView.addObject("validateInterAjuV2", false);
            }

            modelAndView.addObject("neocon", neocon);
            modelAndView.addObject("intergroup", pageInter.getContent());
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("vId", Id);
            modelAndView.addObject("vFilter", Vf);
            modelAndView.addObject("directory", "intergroup2");
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("reports/intergroup2");

        } else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/reports/intergroupV2Dep/upload")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) throws ServletException, IOException, InvalidFormatException {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

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

        Collection<Part> parts = request.getParts();
        ReclassificationV2ListReport listReport = new ReclassificationV2ListReport();

        List<String[]> list = intergrupo2Service.saveFileBD(parts, user, todayString);
        List<Object[]> diff = intergrupo2Service.validateVsIntergroup(todayString);
        List<Object[]> diffRest = intergrupo2Service.validateVsIntergroupRest(todayString);

        try {

            if (list.get(0) != null && list.get(0)[0].equals("ERROR_PAGINA")) {
                modelAndView.addObject("resp", "LoadDocPage-1");
            } else {
                intergrupo2Service.insertfromTempDep(todayString);
                intergrupo2Service.insertCargaMasiva(todayString);

                listReport.exportLog(response, list, diff, diffRest);
            }

        } catch (Exception e) {
            e.printStackTrace();

            intergrupo2Service.insertfromTempDep(todayString);
            intergrupo2Service.insertCargaMasiva(todayString);

            listReport.exportLog(response, list, diff, diffRest);
        }

        return modelAndView;
    }

    @GetMapping(value = "/reports/intergroup2/downloadFinalAjustes")
    public void downloadIntergrupoV2FAju(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= IntergrupoV2ConAjustes_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV2> intList = intergrupo2.getAllFromV2FinalAju(params.get("period").toString());

        Intergrupo2ListReport listReport = new Intergrupo2ListReport(intList);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/intergroup2/download")
    public void downloadIntergrupoV2(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= IntergrupoV2" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV2> intList = intergrupo2.getAllFromV2(params.get("period").toString());

        Intergrupo2ListReport listReport = new Intergrupo2ListReport(intList);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/intergroup2Cartera")
    public ModelAndView getReclassficationCartera(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2");

        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= ReclasificacionCartera_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);


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

        intergrupo2.insertCargaMasivaCartera(todayString);

        List<Object[]> error1 = intergrupo2.getErrorRecon1(todayString);

        List<Object[]> error2 = intergrupo2.getErrorRecon2(todayString);


        if (error1.isEmpty() && error2.isEmpty()) {
            modelAndView.addObject("resp", "Reclas-1");
        } else {

            ReclassificationV2ListReport3 listReport = new ReclassificationV2ListReport3(error1, error2);
            listReport.export(response);
        }

        modelAndView.addObject("period", todayString);


        return modelAndView;
    }

    @RequestMapping(value = "/reports/intergrupo2/downloadPlano")
    @ResponseBody
    public void exportCargaMasiva(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            if (params.get("CmRec") != null || params.get("CmRev") != null) {
                response.setContentType("text/plain");
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String currentDateTime = dateFormatter.format(new Date());

                response.setHeader("Content-Disposition", "attachment;  filename=CargaMasivaIntergrupo_" + currentDateTime + ".csv");

                List<Object[]> contingentList = intergrupo2.getReclasficacionCSV(params.get("CmRec"), params.get("CmRev"), params.get("CmDep1"), params.get("CmCar1"), params.get("period").toString());
                ArrayList<String> contingentListNew = new ArrayList<>();
                for (Object row : contingentList) {
                    contingentListNew.add(row.toString());
                }

                CsvService.downloadCsvCargaMasiva(response.getWriter(), contingentListNew);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/reports/reclassifications")
    @ResponseBody
    public void exportCargaMasivaExcelV2(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Reclasificaciones_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<Object[]> RecList = intergrupo2.getAllReportResumen(params.get("period").toString(), params.get("CmRec").toString(), params.get("CmRev").toString(), params.get("CmDep1").toString(), params.get("CmCar1").toString());

            ReclassificationV2ListReport2 listReport = new ReclassificationV2ListReport2(RecList);
            listReport.exportReport(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/reports/intergroup2/processInterV2")
    public ModelAndView processIntergroupV2(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            intergrupo2.processAjuInterV2(params.get("period").toString());
            intergrupo2.auditCode("Procesamiento Exitoso Ajustes Intergrupo V2", user);
            modelAndView.addObject("resp", "Add1");

            Date today = new Date();
            StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo("INTER_V2_DEF", params.get("period").toString());

            if (validateStatus == null) {
                StatusInfo status = new StatusInfo();
                status.setInput("INTER_V2_DEF");
                status.setPeriodo(params.get("period").toString());
                status.setFecha(today);
                status.setStatus("nexco_intergrupo_v2_def");
                StatusInfoRepository.save(status);
            } else {
                validateStatus.setFecha(today);
                validateStatus.setStatus("nexco_intergrupo_v2_def");
                StatusInfoRepository.save(validateStatus);
            }
        }
        catch (Exception e){
            intergrupo2.auditCode("Procesamiento Fallido Ajustes Intergrupo V2", user);
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @PostMapping(value = "/reports/sendIntergroup")
    public ModelAndView sendIntergroup(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String todayString = "";
        String Id;
        String Vf;
        if (params.get("vId") == null) {
            Id = "";
        } else {
            Id = params.get("vId").toString();
        }
        if (params.get("vFilter") == null) {
            Vf = "";
        } else {
            Vf = params.get("vFilter").toString();
        }

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

        List<IntergrupoV2> list;
        if (Id == "" | Vf == "") {
            list = intergrupo2.getAllFromV2(todayString);
        } else {
            list = intergrupo2.findByFilter(Id, Vf, todayString);
        }


        String exclude = "'',";

        if (params.get("AS") != null) exclude = exclude + "'AS',";
        if (params.get("MA") != null) exclude = exclude + "'MA',";
        if (params.get("IC") != null) exclude = exclude + "'IC',";
        if (params.get("NG") != null) exclude = exclude + "'NG',";
        if (params.get("GE") != null) exclude = exclude + "'GE',";
        if (params.get("NA") != null) exclude = exclude + "'NA',";
        if (params.get("SC") != null) exclude = exclude + "'SC',";
        if (params.get("GF") != null) exclude = exclude + "'GF',";
        if (params.get("AM") != null) exclude = exclude + "'AM',";
        if (params.get("TD") != null) exclude = exclude + "'TD',";
        if (params.get("GP") != null) exclude = exclude + "'GP',";
        if (params.get("MG") != null) exclude = exclude + "'MG',";
        if (params.get("PE") != null) exclude = exclude + "'PE',";

        if (exclude.length() > 0) exclude = exclude.substring(0, exclude.length() - 1);


        //
        intergrupo2.insertV2(params.get("CmDep"), params.get("CmCar"), todayString, exclude, user);
        //

        //modelAndView.addObject("intergroup",pageInter.getContent());
        modelAndView.addObject("period", todayString);
        //modelAndView.addObject("current",page+1);
        //modelAndView.addObject("next",page+2);
        //modelAndView.addObject("prev",page);
        //modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vId", Id);
        modelAndView.addObject("vFilter", Vf);
        modelAndView.addObject("directory", "intergroup2");
        modelAndView.addObject("columns", listColumns);

        modelAndView.addObject("resp", "Reclas-2");

        return modelAndView;
    }

    @GetMapping(value = "/reports/intergroupVersions")
    public ModelAndView intergroupVersions(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if (userService.validateEndpoint(user.getUsuario(), "Ver Descargable Intergrupo")) {
            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";
            String Id;
            String Vf;
            if (params.get("vId") == null) {
                Id = "";
            } else {
                Id = params.get("vId").toString();
            }
            if (params.get("vFilter") == null) {
                Vf = "";
            } else {
                Vf = params.get("vFilter").toString();
            }

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

            String version = "";
            if (params.get("version") == null) {
                version = "v1";
            } else {
                version = params.get("version").toString();
            }

            List<Object[]> list;
            if (Id == "" | Vf == "") {
                list = intergrupo2.getAllIntVersions(todayString, version);
            } else {
                list = intergrupo2.findByFilterVersions(Id, Vf, todayString, version);
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageInter = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageInter.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("intergroup", pageInter.getContent());
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("version", version);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("vId", Id);
            modelAndView.addObject("vFilter", Vf);
            modelAndView.addObject("directory", "intergroupVersions");
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("reports/intergroupVersions");

        } else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/reports/intergrupo2/downloadPlanoVersions")
    @ResponseBody
    public void exportPlainVersions(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {

            List<Object[]> validation = intergrupo2.getErrorsIntVersionsPlain(params.get("period").toString());

            if (validation.isEmpty()) {

                response.setContentType("text/plain");
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String currentDateTime = dateFormatter.format(new Date());

                response.setHeader("Content-Disposition", "attachment;  filename=PlanoIntergrupo_" + currentDateTime + ".txt");

                List<Object[]> contingentList = intergrupo2.getAllIntVersionsPlain(params.get("version").toString(), params.get("period").toString());
                ArrayList<String> contingentListNew = new ArrayList<>();
                for (Object row : contingentList) {
                    contingentListNew.add(row.toString());
                }

                CsvService.downloadCsvCargaMasiva(response.getWriter(), contingentListNew);
            } else {
                response.setContentType("application/octet-stream");
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String currentDateTime = dateFormatter.format(new Date());

                String version = "";
                if (params.get("version") == null) {
                    version = "v1";
                } else {
                    version = params.get("version").toString();
                }

                String headerKey = "Content-Disposition";
                String headerValue = "attachment; filename=LogErroresPlano_" + currentDateTime + ".xlsx";
                response.setHeader(headerKey, headerValue);

                Intergrupo2VerListReport listReport = new Intergrupo2VerListReport(validation);
                listReport.exportErrors(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/reports/intergrupo2/downloadExcelVersions")
    public void exportExcelVersions(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String version = "";
        if (params.get("version") == null) {
            version = "v1";
        } else {
            version = params.get("version").toString();
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Intergrupo_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> intList = intergrupo2.getAllIntVersions(params.get("period").toString(), version);

        Intergrupo2VerListReport listReport = new Intergrupo2VerListReport(intList);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/intergroup2/modify")
    @ResponseBody
    public ModelAndView modifyInt(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getNombre());
        modelAndView.addObject("userEmail", user.getCorreo());

        String Id;
        String Vf;

        String period = params.get("period").toString();
        if (params.get("vId") == null) {
            Id = "";
        } else {
            Id = params.get("vId").toString();
        }
        if (params.get("vFilter") == null) {
            Vf = "";
        } else {
            Vf = params.get("vFilter").toString();
        }


        IntergrupoV2 toModify = intergrupo2.findIntergrupo2(Integer.parseInt(params.get("id").toString())).get(0);
        List<Currency> divisas = intergrupo2.getDivisas();
        List<Country> paises = intergrupo2.getPaises();

        modelAndView.addObject("divisas", divisas);
        modelAndView.addObject("paises", paises);
        modelAndView.addObject("intModify", toModify);
        modelAndView.addObject("vId", Id);
        modelAndView.addObject("vFilter", Vf);
        modelAndView.setViewName("reports/modifyIntergroup2");
        return modelAndView;
    }

    @PostMapping(value = "/reports/intergroup2/modify")
    @ResponseBody
    public ModelAndView updateInt(@ModelAttribute IntergrupoV2 intergrupoV2, @Param(value = "id") Integer id, @Param(value = "vId") String vId, @Param(value = "vFilter") String vFilter, @Param(value = "page") String page) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            IntergrupoV2 search = intergrupo2.findIntergrupo2(id).get(0);

            if (search != null) {
                YntpSociety yntp = yntpSocietyRepository.findByYntp(intergrupoV2.getYntp());
                List<Object[]> cuenta = intergrupo2.getCuenta(intergrupoV2.getCuentaLocal());
                List<Object[]> codicons = intergrupo2.getCodiCons(intergrupoV2.getCodNeocon());

                if (yntp == null) {

                    ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2/modify/" + id);
                    modelAndView.addObject("vId", vId);
                    modelAndView.addObject("vFilter", vFilter);
                    modelAndView.addObject("resp", "ErrorIntV2-0");
                    modelAndView.addObject("intModify", intergrupoV2);
                    return modelAndView;

                } else if (cuenta.isEmpty()) {

                    ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2/modify/" + id);
                    modelAndView.addObject("vId", vId);
                    modelAndView.addObject("vFilter", vFilter);
                    modelAndView.addObject("resp", "ErrorIntV2-1");
                    modelAndView.addObject("intModify", intergrupoV2);
                    return modelAndView;

                } else if (codicons.isEmpty()) {

                    ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2/modify/" + id);
                    modelAndView.addObject("vId", vId);
                    modelAndView.addObject("vFilter", vFilter);
                    modelAndView.addObject("resp", "ErrorIntV2-2");
                    modelAndView.addObject("intModify", intergrupoV2);
                    return modelAndView;

                } else {

                    intergrupo2.modifyIntergrupoV2(intergrupoV2, id);
                    ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2");
                    modelAndView.addObject("vId", vId);
                    modelAndView.addObject("vFilter", vFilter);
                    modelAndView.addObject("resp", "Modify1");

                    Date today = new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Edición Manual Registros Intergrupo V2");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("INTERGRUPO V2");
                    insert.setFecha(today);
                    insert.setInput("INTERGRUPO V2");
                    insert.setNombre(user.getNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                    return modelAndView;

                }
            } else {
                ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2");
                modelAndView.addObject("vId", vId);
                modelAndView.addObject("vFilter", vFilter);
                modelAndView.addObject("resp", "Modify0");
                return modelAndView;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2");
            modelAndView.addObject("vId", vId);
            modelAndView.addObject("vFilter", vFilter);
            modelAndView.addObject("resp", "General-1");
            return modelAndView;

        }

    }

    @GetMapping(value = "/reports/intergroup2/addIntergurpo2")
    public ModelAndView showAddIntergurpoV2(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getNombre());
        modelAndView.addObject("userEmail", user.getCorreo());

        IntergrupoV2 intergrupoV2 = new IntergrupoV2();
        intergrupoV2.setPeriodo(params.get("period").toString());
        intergrupoV2.setFuente("MANUAL");
        intergrupoV2.setComponente("INTERAZ MANUAL");
        intergrupoV2.setInput("INTERAZ MANUAL");

        List<Currency> divisas = intergrupo2.getDivisas();
        List<Country> paises = intergrupo2.getPaises();
        List<YntpSociety> yntps = intergrupo2.getYntps();

        modelAndView.addObject("divisas", divisas);
        modelAndView.addObject("paises", paises);
        modelAndView.addObject("yntps", yntps);
        modelAndView.addObject("intergrupoV2", intergrupoV2);
        modelAndView.addObject("period", params.get("period").toString());
        modelAndView.setViewName("reports/addIntergroup2");
        return modelAndView;
    }

    @PostMapping(value = "/reports/intergroup2/addIntergrupo2")
    public ModelAndView createNewIntergurpoV2(@ModelAttribute IntergrupoV2 intergrupoV2) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2");

        try {
            intergrupoV2.setPais(intergrupo2.getPais(intergrupoV2.getCodPais()).get(0).getNombre());
            intergrupo2.addInfo(intergrupoV2);
            modelAndView.addObject("resp", "Add1");
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/reports/intergrupoVersions/downloadPlaneContract")
    @ResponseBody
    public void exportContractsV2(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("text/plain");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());

            response.setHeader("Content-Disposition", "attachment;  filename=CONTRATOS_INTERGRUPO" + ".TXT");

            List<Object[]> contingentList = intergrupo2.getAllContractVersion(params.get("period").toString());
            ArrayList<String> contingentListNew = new ArrayList<>();
            for (Object row : contingentList) {
                contingentListNew.add(row.toString());
            }

            CsvService.downloadCsvCargaMasiva(response.getWriter(), contingentListNew);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/reports/intergroup2/downloadFinal")
    public void downloadIntergrupoV2F(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Ajuste_IntergrupoV2_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV2Final> intList = intergrupo2.getAllFromV2Final(params.get("period").toString());

        Intergrupo2ListReport listReport = new Intergrupo2ListReport(null);
        listReport.exportFinal(response, intList);
    }

    @PostMapping(value="/reports/intergroup2/loadFinal")
    public ModelAndView uploadFileFinal(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String todayString = "";

        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        } else {
            todayString = params.get("period").toString();
        }

        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logIntergrupoFinalV2_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            List<String[]> list = intergrupo2.saveFileFinalIntergrupo(fileContent, user, todayString);
            String[] part=list.get(list.size()-1);
            if(part[2].equals("COMPLETE"))
            {
                Date today = new Date();
                StatusInfo validateStatus1 = StatusInfoRepository.findByInputAndPeriodo("INTER_V2_AJU", todayString);

                if (validateStatus1 == null) {
                    StatusInfo status = new StatusInfo();
                    status.setInput("INTER_V2_AJU");
                    status.setPeriodo(todayString);
                    status.setFecha(today);
                    status.setStatus("nexco_intergrupo_v2_final");
                    StatusInfoRepository.save(status);
                } else {
                    validateStatus1.setFecha(today);
                    validateStatus1.setStatus("nexco_intergrupo_v2_final");
                    StatusInfoRepository.save(validateStatus1);
                }

                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else
            {
                Intergrupo2ListReport report = new Intergrupo2ListReport(null);
                report.exportLog(response, list);
            }

        }catch(Exception e){
            modelAndView.addObject("resp", "Descon-3");
            e.printStackTrace();
        }
        return  modelAndView;
    }
}
