package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.GpsReport;
import com.inter.proyecto_intergrupo.model.bank.SubsidiariesTemplate;
import com.inter.proyecto_intergrupo.model.ifrs9.GeneralInterestProvision;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.GpsListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.GeneralInterestProvisionListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.GeneralInterestProvisionService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
public class GeneralInterestProvisionController {
    private static final int PAGINATIONCOUNT=12;

    @Autowired
    GeneralInterestProvisionService provisionService;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/ifrs9/generalInterestProvision")
    public ModelAndView showGeneralInterestProvision(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Provisión general de interés")) {
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
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        } else {
            todayString = params.get("period").toString();
        }

        ArrayList<Object[]> result = provisionService.getGeneralInterest(todayString);

        int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), result.size());
        Page<Object[]> pageProv = new PageImpl<>(result.subList(start, end), pageRequest, result.size());

        int totalPage = pageProv.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }

        modelAndView.addObject("allProvisions", pageProv.getContent());
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("directory", "generalInterestProvision");
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("registers",result.size());

        modelAndView.setViewName("/ifrs/generalInterestProvision");
        }
            else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/ifrs9/generalInterestProvision/upload")
    public ModelAndView uploadProvisions(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/generalInterestProvision");
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerValue = "attachment; filename=Prov Log_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        String period = params.get("period").toString();

        try{
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();

            List<String[]> list = provisionService.validateTemplateAndInsert(fileContent,period);

            if(list.get(0)[3].equals("true")){
                modelAndView.addObject("resp","true");
            }else{
                GeneralInterestProvisionListReport provisionListReport = new GeneralInterestProvisionListReport(null,list);
                if(list.size() == 4){
                    provisionListReport.exportLog(response);
                } else{
                    provisionListReport.exportLogParam(response);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        modelAndView.addObject("period",period);

        return modelAndView;

    }

    @GetMapping(value = "/ifrs9/generalInterestProvision/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Nexco Tablón Provision_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ArrayList<GeneralInterestProvision> result= provisionService.getGeneralInterestComplete(params.get("period").toString());

        GeneralInterestProvisionListReport listReport = new GeneralInterestProvisionListReport(result,null);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs9/generalInterestProvision/massiveCharge")
    @ResponseBody
    public void exportMassiveCharge(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Carga Masiva Provisiones " + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> result= provisionService.generateMassiveCharge(params.get("period").toString());

        GeneralInterestProvisionListReport listReport = new GeneralInterestProvisionListReport();
        listReport.exportMassive(result,response);
    }

    @GetMapping(value = "/ifrs9/generalInterestProvision/downloadCsv")
    @ResponseBody
    public void exportProvisionCsv(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment;  filename = Carga Masiva Provisiones_" + currentDateTime + ".txt");

        List<Object[]> result= provisionService.generateMassiveCharge(params.get("period").toString());

        CsvService.downloadMassiveCharge(response.getWriter(), result);
    }

    @GetMapping(value = "/ifrs9/generalInterestProvision/downloadAnexo8")
    @ResponseBody
    public void exportAnexo8(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException{
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment;  filename = Anexo 8 Provisiones_" + currentDateTime + ".txt");

        List<Object[]> result= provisionService.generateAnexo8(params.get("period").toString());

        CsvService.downloadAnexo8(response.getWriter(), result);
    }

}
