package com.inter.proyecto_intergrupo.controller.bank;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.IntergrupoSubsidiariesTemplate;
import com.inter.proyecto_intergrupo.model.bank.SubsidiariesTemplate;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.IntergrupoSubsidiariesTemplateService;
import com.inter.proyecto_intergrupo.service.bankServices.SubsidiariesTemplateListReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class IntergrupoSubsidiariesTemplateController {

    private static final int PAGINATIONCOUNT=12;

    @Autowired
    IntergrupoSubsidiariesTemplateService subsidiariesService;

    @Autowired
    UserService userService;

    @GetMapping(value = "/bank/intergrupoSubsidiaries")
    public ModelAndView getSubsidiaries(@RequestParam Map<String, Object> params){

        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Intergrupo Filiales")) {
            String todayString = "";

            if (params.get("period") != null && params.get("period").toString() == "") {
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

            List<IntergrupoSubsidiariesTemplate> result = subsidiariesService.getIntergrupoSubsidiaries(todayString, user);

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), result.size());
            Page<IntergrupoSubsidiariesTemplate> pageSub = new PageImpl<>(result.subList(start, end), pageRequest, result.size());

            int totalPage = pageSub.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("directory", "intergrupoSubsidiaries");
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("registers",result.size());

            modelAndView.addObject("allSubsidiaries", pageSub.getContent());
            modelAndView.setViewName("/bank/intergrupoSubsidiariesTemplate");

        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/bank/intergrupoSubsidiaries/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Intergrupo Filiales " + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoSubsidiariesTemplate> result = subsidiariesService.getIntergrupoSubsidiaries(params.get("period").toString(),user);
        SubsidiariesTemplateListReport listReport = new SubsidiariesTemplateListReport(null,null,result);
        listReport.exportReportInter(response);
    }
}
