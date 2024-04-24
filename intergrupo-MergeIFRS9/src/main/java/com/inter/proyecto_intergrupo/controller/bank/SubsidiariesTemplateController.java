package com.inter.proyecto_intergrupo.controller.bank;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.SubsidiariesTemplate;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.SubsidiariesTemplateListReport;
import com.inter.proyecto_intergrupo.service.bankServices.SubsidiariesTemplateService;
import com.inter.proyecto_intergrupo.service.bankServices.TemplateBankListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.SubsidiariesListReport;
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
public class SubsidiariesTemplateController {
    private static final int PAGINATIONCOUNT=12;

    @Autowired
    SubsidiariesTemplateService subsidiariesTemplateService;

    @Autowired
    UserService userService;

    @GetMapping(value = "/bank/subsidiariesTemplate")
    public ModelAndView showSubsidiariesView(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Plantilla Filiales")) {
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

            List<SubsidiariesTemplate> result = subsidiariesTemplateService.getActualSubsidiaries(todayString, user);

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), result.size());
            Page<SubsidiariesTemplate> pageTax = new PageImpl<SubsidiariesTemplate>(result.subList(start, end), pageRequest, result.size());

            int totalPage = pageTax.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("directory", "subsidiariesTemplate");
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("registers",result.size());

            modelAndView.addObject("allSubsidiaries", pageTax.getContent());
            modelAndView.setViewName("/bank/subsidiariesTemplate");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/bank/subsidiariesTemplate")
    public ModelAndView getSubsidiariesTemplate(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "period") String period, @RequestParam Map<String, Object> params, ModelMap model) throws ParseException {
        response.setContentType("application/octet-stream");
        model.addAttribute("period",period);
        ModelAndView modelAndView = new ModelAndView("redirect:/bank/subsidiariesTemplate");
        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("vFilter",params.get("period").toString());
        DateFormat formatter = new SimpleDateFormat("yyyy-MM");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Date fecontGet = formatter.parse(period);
        String fecont = formatter.format(fecontGet);
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logInsert_" + fecont + ".xlsx";
        response.setHeader(headerKey, headerValue);

        try{
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = subsidiariesTemplateService.saveFileBD(fileContent, user, fecont);
            String[] part = list.get(0);

            if(part[0] != null && part[0].equals("Fallo Estrucutura") ){
                modelAndView.addObject("resp","false");
                modelAndView.addObject("period", params.get("period").toString());
            } else {
                if(part[2].equals("true")){
                    modelAndView.addObject("resp", "subsidiariesCorrect");
                    modelAndView.addObject("period", params.get("period").toString());
                }else{
                    SubsidiariesTemplateListReport subsidiariesTemplateListReport = new SubsidiariesTemplateListReport(null,list,null);
                    if(list.get(0).length == 3){
                        subsidiariesTemplateListReport.exportLog(response);
                    }else{
                        if(list.get(0)[3].equals("Divisa")){
                            subsidiariesTemplateListReport.exportCurrency(response);
                        }else{
                            subsidiariesTemplateListReport.exportQuery(response);
                        }

                    }
                }
            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
            modelAndView.addObject("resp","false");
            modelAndView.addObject("period", params.get("period").toString());
        }
        return modelAndView;
    }

    @GetMapping(value = "/bank/subsidiariesTemplate/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Plantilla_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<SubsidiariesTemplate> templateList= subsidiariesTemplateService.getActualSubsidiaries(params.get("period").toString(),user);
        SubsidiariesTemplateListReport listReport = new SubsidiariesTemplateListReport(templateList,null,null);
        listReport.export(response);
    }

    @RequestMapping(value = "/bank/subsidiariesTemplate/sendIntergrupo")
    public ModelAndView sendToIntergrupo(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/bank/subsidiariesTemplate");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        boolean result = subsidiariesTemplateService.sendIntergrupo(params.get("period").toString(), user);

        if(result){
            modelAndView.addObject("resp","sendSubTemplateCorrect");
        }else{
            modelAndView.addObject("resp","sendSubTemplateFail");
        }
        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("vFilter",params.get("period").toString());

        return modelAndView;
    }



}
