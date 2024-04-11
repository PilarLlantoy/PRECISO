package com.inter.proyecto_intergrupo.controller.ifrs9;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.GpsReport;
import com.inter.proyecto_intergrupo.model.bank.SubsidiariesTemplate;
import com.inter.proyecto_intergrupo.model.ifrs9.FirstAdjustment;

import com.inter.proyecto_intergrupo.model.ifrs9.IncurredLoss;
import com.inter.proyecto_intergrupo.service.bankServices.GpsReportService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.FirstAdjustmentListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.FirstAdjustmentService;

import com.inter.proyecto_intergrupo.service.ifrs9Services.IncurredLossListReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
@Controller
public class FirstAdjustmentController {


    private static final int PAGINATIONCOUNT=12;

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private FirstAdjustmentService firstAdjustmentService;

    @GetMapping(value="/ifrs9/firstAdjustment")
    public ModelAndView showfirstadjustment(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Ajuste primera vez")) {

            ArrayList<Object[]> result = firstAdjustmentService.getFirstAdjustment();

            int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), result.size());
            Page<Object[]> pageFirst = new PageImpl<>(result.subList(start, end), pageRequest, result.size());

            int totalPage = pageFirst.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allFirst", pageFirst.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("directory", "firstAdjustment");
            modelAndView.addObject("registers",result.size());


            modelAndView.setViewName("/ifrs/firstAdjustment");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }


    @PostMapping(value="/ifrs9/firstAdjustment/upload")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/firstAdjustment");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());


        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logInsert_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            firstAdjustmentService.loadQueryDatabase(fileContent);
        }catch(Exception e){
            e.printStackTrace();
        }

        return modelAndView;
    }

    @GetMapping(value = "/ifrs9/firstAdjustment/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {

        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=PrimeraVez_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> fisrtList = firstAdjustmentService.getFirstAdjustment();
        FirstAdjustmentListReport listReport = new FirstAdjustmentListReport(null,fisrtList);
        listReport.exportResume(response);
    }

    @GetMapping(value = "/ifrs9/firstAdjustment/downloadDinamic")
    @ResponseBody
    public void exportToExcelDinamic(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());

            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=PrimeraVez_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<FirstAdjustment> firstList = firstAdjustmentService.findAllDinamic(params.entrySet());

            FirstAdjustmentListReport listReport = new FirstAdjustmentListReport(firstList,null);
            listReport.export(response);
        }
        catch(Exception e)
        {
            List<FirstAdjustment> firstList = new ArrayList<FirstAdjustment>();
            FirstAdjustmentListReport listReport = new FirstAdjustmentListReport(firstList,null);
            listReport.export(response);
        }
    }
}
