package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.DesconValConcil;
import com.inter.proyecto_intergrupo.model.ifrs9.DesconValDif;
import com.inter.proyecto_intergrupo.model.ifrs9.DesconValidationUpload;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.DesconValidationConcilListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.DesconValidationConcilService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.DesconValidationListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.ReconciliationDiffListReport;
import org.dom4j.rule.Mode;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class DesconValidationConcilController {
    private static final int PAGINATIONCOUNT=12;

    @Autowired
    DesconValidationConcilService desconService;

    @Autowired
    private UserService userService;

    /*
    @GetMapping(value = "/ifrs9/desconValConcil")
    public ModelAndView getDesconConcil(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Validación Descontabilización")) {
        String todayString="";
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

        List<Object[]> companies = desconService.getCompanies(todayString);

        boolean noQuery = !companies.isEmpty();


        modelAndView.addObject("companies", companies);
        modelAndView.addObject("isPost", false);
        modelAndView.addObject("noQuery", noQuery);
        modelAndView.addObject("directory", "DesconValConcil");
        modelAndView.addObject("period", todayString);

        modelAndView.setViewName("/ifrs/desconValidationConcil");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }*/

    @GetMapping(value = "/ifrs9/desconValConcil")
    public ModelAndView uploadData(HttpServletRequest request, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();

        String company;
        String queryV;
        String todayString;
        String match;
        String lvl;

        if(params.get("period") == null || params.get("period").toString() == null) {
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

        //company = params.get("company").toString();
        //queryV = params.get("queryV").toString();
        //match = params.get("concilType").toString();
        //lvl = params.get("concilLvl").toString();

        //desconService.createDifAndConcil(todayString,company,queryV);


        List<DesconValConcil> valDif = desconService.getDiff(todayString);

        if(params.get("concilLvl")!=null && params.get("concilLvl").toString()!=null) {
            modelAndView.addObject("concilLvl", params.get("concilLvl").toString());
        }
        else
        {
            if(!valDif.isEmpty())
            {
                modelAndView.addObject("concilLvl", valDif.get(0).getNivel());
            }
        }

        if(params.get("concilType")!=null && params.get("concilType").toString()!=null) {
            modelAndView.addObject("concilType", params.get("concilType").toString());
        }
        else
        {
            if(!valDif.isEmpty())
            {
                modelAndView.addObject("concilType", valDif.get(0).getTipoCuenta());
            }
        }

        int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), valDif.size());
        Page<DesconValConcil> pageDesconVal = new PageImpl<>(valDif.subList(start, end), pageRequest, valDif.size());

        int totalPage = pageDesconVal.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }

        modelAndView.addObject("reconciliation", pageDesconVal.getContent());
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("registers",valDif.size());
        modelAndView.addObject("directory","desconValConcil");
        modelAndView.addObject("period",todayString);
        if(valDif.isEmpty())
            modelAndView.addObject("isPost", false);
        else
            modelAndView.addObject("isPost", true);

        modelAndView.setViewName("/ifrs/desconValidationConcil");

        return modelAndView;
    }

    @GetMapping(value = "/ifrs9/desconValConcil/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String period = params.get("period").toString();

        List<DesconValConcil> valDif = desconService.getDiff(period);

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ValidacionDescona_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        DesconValidationConcilListReport listReport = new DesconValidationConcilListReport(valDif);
        listReport.export(response);
    }

    @RequestMapping(value = "/ifrs9/desconValConcil/upload")
    @ResponseBody
    public ModelAndView uploadValConicl(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/desconValConcil");
        boolean listData = desconService.dataConcilU(params.get("period").toString(),params.get("concilLvl").toString(),params.get("concilType").toString());

        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("concilLvl",params.get("concilLvl").toString());
        modelAndView.addObject("concilType",params.get("concilType").toString());

        if(listData)
            modelAndView.addObject("resp", "Concil01");
        else
            modelAndView.addObject("resp", "Concil-01");
        return  modelAndView;
    }
}
