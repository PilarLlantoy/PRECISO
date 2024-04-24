package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Deaccount;
import com.inter.proyecto_intergrupo.model.ifrs9.SegmentosFinalTemp;
import com.inter.proyecto_intergrupo.model.ifrs9.ValQueryEEFF;
import com.inter.proyecto_intergrupo.model.reports.ConciliationComer;
import com.inter.proyecto_intergrupo.model.temporal.SubsidiariesTemplateTemporal;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.TemplateBankListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.*;
import com.inter.proyecto_intergrupo.service.reportsServices.ConciliationComerListReportService;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.Query;
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
public class DescontabilizacionController {

    private static final int PAGINATIONCOUNT=12;

    @Autowired
    private ConciliacionService conciliacionService;

    @Autowired
    private UserService userService;

    @PostMapping(value="/ifrs/calculaDiff")
    public ModelAndView ExportReportCondeta(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/descontabilizacion");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Infome Descontabilizacion" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        String order = params.get("order")==null?null:params.get("order").toString();

        String todayString="";
        if(params.get("period")!=null && params.get("period").toString()=="") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);

            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }

        List<Object[]> h140List = conciliacionService.get140(todayString);
        List<Object[]> condetaList = conciliacionService.getCondeta(todayString);

        if(h140List.size() > 0 && condetaList.size() > 0) {

            if (order == null || order == "") {
                conciliacionService.getAccountsAjusteSinRiesgo(todayString);

                List<Object[]> listaCruza = conciliacionService.selDiffAutoMatch(todayString);
                List<Object[]> listaNoCruza = conciliacionService.selDiffAutoNoMatch(todayString);
                List<Object[]> listaContratos = conciliacionService.selDiffContratoSinAjuste(todayString);

                ConciliacionReport conciliacionReport = new ConciliacionReport(listaCruza, listaNoCruza, listaContratos, null);
                conciliacionReport.export(response);
                conciliacionService.insertDeaccount(todayString);
            } else {
                if (!order.equals("on")) {
                    conciliacionService.getAccountsAjusteSinRiesgo(todayString);

                    List<Object[]> listaCruza = conciliacionService.selDiffAutoMatch(todayString);
                    List<Object[]> listaNoCruza = conciliacionService.selDiffAutoNoMatch(todayString);
                    List<Object[]> listaContratos = conciliacionService.selDiffContratoSinAjuste(todayString);

                    ConciliacionReport conciliacionReport = new ConciliacionReport(listaCruza, listaNoCruza, listaContratos, null);
                    conciliacionReport.export(response);
                    conciliacionService.insertDeaccount(todayString);
                } else {
                    conciliacionService.getAccountsAjuste(todayString);

                    List<Object[]> listaCruza = conciliacionService.selDiffAutoMatch(todayString);
                    List<Object[]> listaNoCruza = conciliacionService.selDiffAutoNoMatch(todayString);
                    List<Object[]> listaContratos = conciliacionService.selDiffContratoAjuste(todayString);

                    ConciliacionReport conciliacionReport = new ConciliacionReport(listaCruza, listaNoCruza, null, listaContratos);
                    conciliacionReport.export(response);
                    conciliacionService.insertDeaccount(todayString);
                }
            }

        }else{

            modelAndView.addObject("resp", "errorConAuto");

        }

        return modelAndView;

    }

    @GetMapping(value="/ifrs/descontabilizacion")
    public ModelAndView showTemplateH140(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Diferencias Conciliación automática")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            String todayString = "";
            if (params.get("period") == null || params.get("period").toString() == "") {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);

                if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
                } else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
                }
            } else {
                todayString = params.get("period").toString();
            }

            List<Deaccount> templatesH140 = conciliacionService.getDeaccount(todayString);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), templatesH140.size());
            Page<Deaccount> pageH140 = new PageImpl<>(templatesH140.subList(start, end), pageRequest, templatesH140.size());

            int totalPage = pageH140.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            if (templatesH140 != null) {
                modelAndView.addObject("allTemplateH140", pageH140.getContent());
                modelAndView.addObject("current", page + 1);
                modelAndView.addObject("next", page + 2);
                modelAndView.addObject("prev", page);
                modelAndView.addObject("last", totalPage);
                modelAndView.addObject("directory", "descontabilizacion");
                modelAndView.addObject("registers",templatesH140.size());
            }

            modelAndView.addObject("period", todayString);
            modelAndView.setViewName("/ifrs/descontabilizacion");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/descontabilizacion/validar")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/descontabilizacion");
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
        String headerValue = "attachment; filename=logValidaDiferencias_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            List<String[]> list = conciliacionService.saveFileDiffBD(fileContent, user, todayString);
            String[] part=list.get(0);
            String[] part2=list.get(list.size()-1);
            if(part[2].equals("true") || part[2].equals("trueCon"))
            {
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else
            {
                if(list.size()>600000)
                {
                    modelAndView.addObject("resp", "Descon-2");
                }
                else if(part2[2].equals("trueCon")){
                    ConciliacionDiffListReport report = new ConciliacionDiffListReport(list);
                    report.export(response);
                }else{
                    ConciliacionListReport report = new ConciliacionListReport(list);
                    report.export(response);
                }
            }

        }catch(Exception e){
            modelAndView.addObject("resp", "Descon-3");
            e.printStackTrace();
        }
        return  modelAndView;
    }

    @RequestMapping(value = "/ifrs/descontabilizacion/download")
    @ResponseBody
    public void exportAll(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Diferencias_" + currentDateTime + ".xlsx";
            String todayString="";
            if(params.get("period")!=null && params.get("period").toString()=="") {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);

                if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
                }
                else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
                }
            }
            else {
                todayString=params.get("period").toString();
            }
            response.setHeader(headerKey, headerValue);
            List<Deaccount> DeaccountList = conciliacionService.getDeaccount(todayString);
            DescontabilizacionListReport listReport = new DescontabilizacionListReport(DeaccountList);
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @PostMapping(value="/ifrs/descontabilizacion/validarApuntes")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "period") String period, @RequestParam Map<String, Object> params, ModelMap model) throws ParseException {
        response.setContentType("application/octet-stream");
        model.addAttribute("period",period);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/descontabilizacion");
        modelAndView.addObject("period",params.get("period").toString());

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
        String headerValue = "attachment; filename=logActualizacion_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        try{
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = conciliacionService.saveFileApuntesBD(fileContent, user, todayString);
            String[] part = list.get(0);

            if(part[0] != null && part[0].equals("Fallo Estructura") ){
                modelAndView.addObject("resp","false");
                modelAndView.addObject("period", params.get("period").toString());
            } else if(part[0] != null && part[0].equals("duplicados") ){
                modelAndView.addObject("resp","segments-error");
                modelAndView.addObject("period", params.get("period").toString());
            } else {
                if(part[2].equals("true")){
                    modelAndView.addObject("resp", "ApuntesCorrect");
                    modelAndView.addObject("period", params.get("period").toString());
                }else{
                    SegmentsLogListReport segmentsLogListReport = new SegmentsLogListReport(list);
                    segmentsLogListReport.exportLog(response);
                }
            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
            modelAndView.addObject("resp","false");
            modelAndView.addObject("period", params.get("period").toString());
        }

        return modelAndView;
    }
}
