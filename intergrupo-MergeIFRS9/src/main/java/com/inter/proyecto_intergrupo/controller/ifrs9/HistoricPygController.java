package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.ParametersPYG;
import com.inter.proyecto_intergrupo.model.ifrs9.PlainIFRS9;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.HistoricPygService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.PlainIFRS9ListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.PlainIFRS9Service;
import com.inter.proyecto_intergrupo.service.reportsServices.Intergrupo3PListReport;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
public class HistoricPygController {

    private static final int PAGINATIONCOUNT=10;

    @Autowired
    HistoricPygService validationIFRS9;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @GetMapping(value="/ifrs9/historicPyg")
    public ModelAndView showHistoricPyg(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Histórico PYG & Impuesto")) {

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

        List<Object[]> validateDataImpu = validationIFRS9.getValuesImpu(todayString);
        ParametersPYG object1 = validationIFRS9.getParam().get(0);
        boolean pygImpu = false;
        if (validateDataImpu.size() == 0) {
            pygImpu = false;
        } else {
            pygImpu = true;
        }

        List<PlainIFRS9> valIFRS9 = validationIFRS9.getPlainIFRS9Impuestos(todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), valIFRS9.size());
        Page<PlainIFRS9> pagePlain = new PageImpl<>(valIFRS9.subList(start, end), pageRequest, valIFRS9.size());

        int totalPage=pagePlain.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        if (valIFRS9 != null){
            modelAndView.addObject("allValidation",pagePlain.getContent());
            modelAndView.addObject("object1",object1);
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("directory","plainIFRS9");
            modelAndView.addObject("registers",valIFRS9.size());
        }
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("pygImpu", pygImpu);
        modelAndView.setViewName("/ifrs/historicPyg");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;

    }

    @RequestMapping(value = "/ifrs9/historicPyg/downloadAll")
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

            String headerValue = "attachment; filename=HistoricoPYG_" + todayString + "_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<PlainIFRS9> cuadreValList = validationIFRS9.getPlainIFRS9Impuestos(todayString);
            PlainIFRS9ListReport listReport = new PlainIFRS9ListReport(cuadreValList,null);
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/ifrs9/historicPyg/downloadCargaMasiva")
    @ResponseBody
    public void exportNeocon(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        try {
            if(params.get("period")!=null) {

                response.setContentType("text/plain");
                String period = params.get("period").toString();
                boolean reversionAplica = false;

                if(params.get("rever")!=null)
                    reversionAplica = true;

                String[] periodSep = period.split("-");

                String type = "PYG";
                String type2 = "'IMPUE'";

                response.setHeader("Content-Disposition", "attachment;  filename="+periodSep[1]+"_"+periodSep[0]+" CARGA_MASIVA_IMPUESTOS" + ".txt");

                List<Object[]> plainList = validationIFRS9.getPlain(period, type2,reversionAplica);

                CsvService.downloadMassiveChargeImpuestos(response.getWriter(),plainList, period);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/ifrs9/historicPyg/updateParameters")
    @ResponseBody
    public ModelAndView updateParameters(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/historicPyg");
        try {
            boolean respon = true;
            if(params.get("period")!=null) {

                String period = params.get("period").toString();

                String[] periodSep = period.split("-");

                String type = "PYG";
                String type2 = "'IMPUE'";

                respon = validationIFRS9.setDataParameter(params.get("centro").toString(),params.get("tercero").toString(),params.get("cuentaD").toString(),params.get("cuentaH").toString(),params.get("codiconsD").toString(),params.get("codiconsH").toString(),params.get("tpD").toString(),params.get("dvD").toString());
            }
            modelAndView.addObject("period",params.get("period").toString());
            if(respon)
                modelAndView.addObject("resp", "Modify1");
            else
                modelAndView.addObject("resp", "Modify-5");
            return  modelAndView;
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
            e.printStackTrace();
            return  modelAndView;
        }
    }
}