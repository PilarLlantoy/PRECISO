package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Anexo;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelAccounts;
import com.inter.proyecto_intergrupo.service.adminServices.UserListReport;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.AnexoListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.AnexoService;
import com.inter.proyecto_intergrupo.service.informationServices.SiccListReport;
import com.inter.proyecto_intergrupo.service.informationServices.SiccService;
import com.inter.proyecto_intergrupo.service.reportsServices.ContingentesListReport;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
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
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class AnexoController {
    @Autowired
    AnexoService anexoService;

    @Autowired
    private UserService userService;

    @Autowired
    private statusInfoRepository statusInfoRepositoryL;

    private static final int PAGINATIONCOUNT=12;
    private final List<String> listColumns = List.of("identificacion", "divisa", "cuenta","empresa","contrato","centro","tipo","digito verificacion", "nombre", "fecha origen","fecha cierr","saldo");


    @GetMapping(value = "/ifrs/anexo")
    public ModelAndView showSiccView(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Anexo 8 Cupos")){
            int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";
            String Id = "";
            String Vf = "";

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

            List<Object[]> companies = anexoService.getCompany(todayString);

            boolean noQuery = false;
            if (companies.size() == 0) {
                noQuery = false;
            } else {
                noQuery = true;
            }

            List<Anexo> list;

            if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
                list = anexoService.getAnexo(todayString);
                modelAndView.addObject("filterExport", "Original");
            } else {
                list = anexoService.findByFilter(Id,Vf,todayString);
                modelAndView.addObject("filterExport", "Filtrado");
            }

            List<Object[]> validateSicc = anexoService.validateLoad(todayString);
            if (validateSicc.size() !=0) {
                modelAndView.addObject("validateSicc", true);
                Date dateSicc = new Date();
                try {
                    dateSicc = statusInfoRepositoryL.findByInputAndPeriodo("ANEXO8", todayString).getFecha();
                }catch (Exception e){
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateSicc);
                modelAndView.addObject("dateSicc", dateAsString);
            } else {
                modelAndView.addObject("validateSicc", false);
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Anexo> pageAnexo = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageAnexo.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("vId", Id);
            modelAndView.addObject("vFilter", Vf);
            modelAndView.addObject("anexoList", pageAnexo.getContent());
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("directory", "sicc");
            modelAndView.addObject("registers",list.size());
            modelAndView.addObject("noQuery", noQuery);
            modelAndView.addObject("companies", companies);
            modelAndView.setViewName("/ifrs/anexo");

        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/anexoload")
    public ModelAndView loadSicc(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/anexo");
        response.setContentType("application/octet-stream");
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
        anexoService.deleteFromSicc(todayString);
        boolean respuesta = false;
        try {
            respuesta = anexoService.saveFromAnexoDatabase(todayString);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        List<Object[]> validateSicc = anexoService.validateLoad(todayString);
        if (validateSicc.size() !=0) {
            modelAndView.addObject("validateSicc", true);
            Date dateSicc = new Date();
            try {
                dateSicc = statusInfoRepositoryL.findByInputAndPeriodo("ANEXO8", todayString).getFecha();
            }catch (Exception e){
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateSicc);
            modelAndView.addObject("dateSicc", dateAsString);
        } else {
            modelAndView.addObject("validateSicc", false);
        }

        List<Object[]> companies = anexoService.getCompany(todayString);

        boolean noQuery = false;
        if (companies.size() == 0) {
            noQuery = false;
        } else {
            noQuery = true;
        }

        if(respuesta==true) {
            modelAndView.addObject("resp", "ANX1");
        }
        else
        {
            modelAndView.addObject("resp", "ANX-1");
        }
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("noQuery", noQuery);
        modelAndView.addObject("companies", companies);

        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/anexo/download")
    public void downloadSiccReport(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= ANEXO8_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Anexo> siccList;
        siccList = (List<Anexo>) anexoService.getSiccByMonth(params.get("period").toString()).get(0);
        AnexoListReport listReport = new AnexoListReport(siccList, null);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/anexo/downloadCsv")
    @ResponseBody
    public void exportNoApply(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, ParseException {
        response.setContentType("text/plain");
        List<Anexo> Anexo = anexoService.getAnexo(params.get("period").toString());
        String separador = "|";

        if/*(!Anexo.isEmpty() && Anexo.size()>0)*/(params.get("extension").toString().equals("txt"))
        {
            response.setHeader("Content-Disposition", "attachment;  filename=ANEXO8_" + Anexo.get(0).getPerido().replace("-","") + "."+params.get("extension").toString());
        }
        else {
            separador = ";";
            response.setHeader("Content-Disposition", "attachment;  filename=CCMFI_D02_" + Anexo.get(0).getPerido().replace("-","").substring(0,6) + "_udahaconx8_conting_anexo8."+params.get("extension").toString());
        }

        CsvService.downloadAnexoCsv(response.getWriter(), Anexo,separador);

    }

    @GetMapping(value = "/ifrs/anexoQuery")
    public void downloadQuery(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= ANEXO8_Query_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> resultQuery = anexoService.validateQuery(params.get("company").toString(),params.get("period").toString());
        AnexoListReport listReport = new AnexoListReport(null,resultQuery);
        listReport.exportAccount(response);
    }
}
