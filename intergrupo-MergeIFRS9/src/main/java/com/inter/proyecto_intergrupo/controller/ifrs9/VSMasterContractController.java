package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Anexo;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.VSMasterContractListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.VSMasterContractService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class VSMasterContractController {

    @Autowired
    private VSMasterContractService vsMasterContractService;

    @Autowired
    private UserService userService;

    @GetMapping("/reports/vsMasterContract")
    public ModelAndView getVsMasterContract(@RequestParam Map<String, Object> params) throws ParseException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

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

        if (params.get("order") == null || params.get("order").toString() == "") {
            modelAndView.addObject("order", "SA");
            vsMasterContractService.createResumeWithoutAdjust(todayString);
        }else {
            modelAndView.addObject("order", params.get("order").toString());
            if (params.get("order") != null && params.get("order").toString().equals("A")) {
                vsMasterContractService.createResumeAdjust(todayString);
            } else if (params.get("order") != null && params.get("order").toString().equals("SA")){
                vsMasterContractService.createResumeWithoutAdjust(todayString);
            }
        }

        List<Object[]> list = vsMasterContractService.getResume();

        modelAndView.addObject("arrayData", list);
        modelAndView.addObject("period", todayString);

        modelAndView.setViewName("reports/vsMasterContract");

        return modelAndView;
    }

    @GetMapping("/reports/vsMasterContract/downloadResume")
    public void downloadResume(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException{
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= VS_MC_Validacion" + currentDateTime + ".xlsx";

        response.setHeader(headerKey, headerValue);

        List<Object[]> dataResume = vsMasterContractService.getResume();

        /*if (params.get("order") != null && params.get("order").toString().equals("A")) {

            dataResume = vsMasterContractService.getResumeAdjust(params.get("period").toString());
        } else if(params.get("order") != null && params.get("order").toString().equals("SA")){
            dataResume = vsMasterContractService.getResumeWithoutAdjust(params.get("period").toString());
        }*/

        VSMasterContractListReport listReport = new VSMasterContractListReport(null, dataResume);
        listReport.exportResume(response);
    }

    @GetMapping("/reports/vsMasterContract/downloadComplete")
    public void downloadComplete(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException{
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        List<Object[]> dataComplete = vsMasterContractService.getComplete();

        if (params.get("order") != null && params.get("order").toString().equals("A")) {
            //dataComplete = vsMasterContractService.getCompleteAdjust(params.get("period").toString());
        } else if(params.get("order") != null && params.get("order").toString().equals("SA")){
            //dataComplete = vsMasterContractService.getCompleteWithoutAdjust(params.get("period").toString());
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= VS_MC_Validacion" + currentDateTime + ".txt";

        response.setHeader(headerKey, headerValue);

        CsvService.downloadMasterContract(response.getWriter(), dataComplete);
    }
}
