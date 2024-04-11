package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.ValIFRS9;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.ValidationIFRS9ListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.ValidationQueryEEFFListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.ValidationIFRS9Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
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
public class ValidationIFRS9Controller {

    private static final int PAGINATIONCOUNT=10;

    @Autowired
    ValidationIFRS9Service validationIFRS9;

    @Autowired
    private UserService userService;

    @GetMapping(value="/ifrs9/valIFRS9")
    public ModelAndView showTemplateIFRS9(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Validación Información IFRS9")) {

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

        String todayString="";

        if(params.get("period")==null || params.get("period").toString()=="") {
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

        List<Object[]> companies = validationIFRS9.getCompany(todayString);
        boolean noQuery = false;
        if (companies.size() == 0) {
            noQuery = false;
        } else {
            noQuery = true;
        }

        List<ValIFRS9> valIFRS9 = validationIFRS9.getValIFRS9(todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), valIFRS9.size());
        Page<ValIFRS9> pageValQueryEEFF = new PageImpl<>(valIFRS9.subList(start, end), pageRequest, valIFRS9.size());

        int totalPage=pageValQueryEEFF.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        if (valIFRS9 != null){
            modelAndView.addObject("allValidation",pageValQueryEEFF.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("directory","valIFRS9");
            modelAndView.addObject("registers",valIFRS9.size());
        }
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("noQuery", noQuery);
        modelAndView.addObject("companies",companies);
        modelAndView.setViewName("/ifrs/valIFRS9");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;

    }

    @GetMapping(value="/ifrs9/CalValIFRS9")
    public ModelAndView CalValIFRS9(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/valIFRS9");
        response.setContentType("application/octet-stream");

        String todayString = "";

        if(params.get("period")==null || params.get("period").toString()=="") {
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
        validationIFRS9.valIFRS9(todayString);

        modelAndView.addObject("resp","ejecucionCorrecta");

        return  modelAndView;
    }

    @GetMapping(value = "/ifrs9/clearValIFRS9")
    public ModelAndView clearCuadreConcilifrs9(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params){

        String todayString = "";

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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        validationIFRS9.clearValIFRS9(user, todayString);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/valIFRS9");
        return  modelAndView;
    }

    @RequestMapping(value = "/ifrs9/valIFRS9/download")
    @ResponseBody
    public void exportAllCuadre(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";

            String todayString = "";

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

            String headerValue = "attachment; filename=CuadrePlantillas_" + todayString + "_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<ValIFRS9> cuadreValList = validationIFRS9.getValIFRS9Filter(todayString);
            List<Object[]> queryLocList = validationIFRS9.getQueryLocal(todayString);
            List<Object[]> queryIFRS9List = validationIFRS9.getQueryIfrs9(todayString);
            List<Object[]> eeffLocList = validationIFRS9.getEeffLocal(todayString);
            List<Object[]> eeffIFRS9List = validationIFRS9.getEeffIfrs9(todayString);
            List<Object[]> planosList = validationIFRS9.getPlanos(todayString);
            List<Object[]> divisasList = validationIFRS9.getDivisas(todayString);
            List<Object[]> intergrupoList = validationIFRS9.getTableIntergrupo(todayString);
            ValidationIFRS9ListReport listReport = new ValidationIFRS9ListReport(cuadreValList, queryLocList, queryIFRS9List, eeffLocList, eeffIFRS9List, planosList, divisasList,intergrupoList);
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}