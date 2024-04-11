package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.bank.TaxBase;
import com.inter.proyecto_intergrupo.service.ifrs9Services.VSMasterContractListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.VSQueryBalanceListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.VSQueryBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class VSQueryBalanceController {

    private static final int PAGINATIONCOUNT = 15;

    @Autowired
    VSQueryBalanceService vsQueryBalanceService;

    @GetMapping("/reports/vsQueryBalance")
    public ModelAndView getVsMasterContract(@RequestParam Map<String, Object> params) throws ParseException {
        ModelAndView modelAndView = new ModelAndView();
        String todayString = "";
        int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
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
        List<Object[]> list;

        list = vsQueryBalanceService.getBalanceResume(todayString);

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Object[]> pageTax = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage = pageTax.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }

        modelAndView.addObject("period", todayString);
        modelAndView.addObject("arrayData", pageTax.getContent());
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("registers",list.size());

        modelAndView.setViewName("reports/vsQueryBalance");

        return modelAndView;
    }

    @GetMapping("/reports/vsQueryBalance/downloadResume")
    public void downloadResume(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException{
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= VS_CC_Query" + currentDateTime + ".xlsx";

        response.setHeader(headerKey, headerValue);

        List<Object[]> dataResume = new ArrayList<>();

        /*if (params.get("order") != null && params.get("order").toString().equals("A")) {

            dataResume = vsMasterContractService.getResumeAdjust(params.get("period").toString());
        } else if(params.get("order") != null && params.get("order").toString().equals("SA")){
            dataResume = vsMasterContractService.getResumeWithoutAdjust(params.get("period").toString());
        }*/

        dataResume = vsQueryBalanceService.getBalanceResume(params.get("period").toString());

        VSQueryBalanceListReport listReport = new VSQueryBalanceListReport(null, dataResume);
        listReport.exportResume(response);
    }

    @GetMapping("/reports/vsQueryBalance/downloadComplete")
    public void downloadComplete(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException{
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= VS_CC_Query" + currentDateTime + ".xlsx";

        response.setHeader(headerKey, headerValue);

        List<Object[]> dataComplete;

        /*if (params.get("order") != null && params.get("order").toString().equals("A")) {

            dataResume = vsMasterContractService.getResumeAdjust(params.get("period").toString());
        } else if(params.get("order") != null && params.get("order").toString().equals("SA")){
            dataResume = vsMasterContractService.getResumeWithoutAdjust(params.get("period").toString());
        }*/

        dataComplete = vsQueryBalanceService.getBalance(params.get("period").toString());

        VSQueryBalanceListReport listReport = new VSQueryBalanceListReport(dataComplete, null);
        listReport.export(response);
    }

}
