package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.DesconValidationUpload;
import com.inter.proyecto_intergrupo.model.ifrs9.OnePercent;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.DesconValidationListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.DesconValidationService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.OnePercentListReport;
import org.dom4j.rule.Mode;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class DesconValidationController {
    private static final int PAGINATIONCOUNT=12;

    @Autowired
    DesconValidationService desconService;

    @Autowired
    private UserService userService;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @GetMapping(value = "/ifrs9/desconValidation")
    public ModelAndView getDesconValidation(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Archivos Descontabilización Host")) {
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

        ArrayList<DesconValidationUpload> result = (ArrayList<DesconValidationUpload>) desconService.getInfo(todayString);

        int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), result.size());
        Page<DesconValidationUpload> pageDesconVal = new PageImpl<>(result.subList(start, end), pageRequest, result.size());

        int totalPage = pageDesconVal.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }

        modelAndView.addObject("allVal", pageDesconVal.getContent());
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("directory", "desconValidation");
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("registers",result.size());

        modelAndView.setViewName("/ifrs/desconValidation");

        List<Object[]> validateDescon = desconService.validateDescon(todayString);

        if (validateDescon.size() != 0) {
            modelAndView.addObject("validateDesconH", true);
            Date dateSicc = new Date();
            try {
                dateSicc = StatusInfoRepository.findByInputAndPeriodo("FICHERODESCONHOST", todayString).getFecha();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateSicc);
            modelAndView.addObject("dateDesconH", dateAsString);
        } else {
            modelAndView.addObject("validateDesconH", false);
        }
        }
            else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/ifrs9/desconValidation/upload")
    public ModelAndView uploadData(HttpServletRequest request, @RequestParam(value = "files") MultipartFile[] files){

        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/desconValidation");

        String period = request.getParameter("period");

        String todayString="";
        if (period == null || Objects.equals(period, "")) {
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
            todayString = period;
        }

        try{
            desconService.uploadFiles(todayString, files);
            Date today = new Date();
            String input = "FICHERODESCONHOST";

            StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo(input, todayString);

            if (validateStatus == null) {
                StatusInfo status = new StatusInfo();
                status.setInput(input);
                status.setPeriodo(todayString);
                status.setFecha(today);
                StatusInfoRepository.save(status);
            } else {
                validateStatus.setFecha(today);
                StatusInfoRepository.save(validateStatus);
            }
            modelAndView.addObject("resp","correct");
        }catch (Exception e){
            modelAndView.addObject("resp","fail");
        }

        modelAndView.addObject("period", todayString);

        return modelAndView;
    }

    @GetMapping(value = "/ifrs9/desconValidation/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Nexco Tanque Descon. " + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

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

        List<DesconValidationUpload> result = desconService.getInfo(todayString);

        DesconValidationListReport listReport = new DesconValidationListReport(result);
        listReport.export(response);
    }

}
