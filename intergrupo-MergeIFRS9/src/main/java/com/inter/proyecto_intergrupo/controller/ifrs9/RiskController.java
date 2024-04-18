package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.H140;
import com.inter.proyecto_intergrupo.model.ifrs9.MarketRisk;
import com.inter.proyecto_intergrupo.model.ifrs9.Risk;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.H140Service;
import com.inter.proyecto_intergrupo.service.ifrs9Services.MarketRiskService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class RiskController {

    private static final int PAGINATIONCOUNT=12;

    @Autowired
    private MarketRiskService marketRiskService;

    @Autowired
    private RiskService riskService;

    @Autowired
    private UserService userService;

    @GetMapping(value="/ifrs/Risk")
    public ModelAndView showTemplateH140(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Riesgos de mercado")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

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

            //List<Risk> templatesRisk = riskService.getAllRisksByPeriodo();
            List<MarketRisk> templatesRisk = marketRiskService.findByPeriodo("2021-07");

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), templatesRisk.size());
            Page<MarketRisk> pageRisk = new PageImpl<>(templatesRisk.subList(start, end), pageRequest, templatesRisk.size());

            int totalPage = pageRisk.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            if (templatesRisk != null) {
                modelAndView.addObject("allTemplateRisk", pageRisk.getContent());
                modelAndView.addObject("current", page + 1);
                modelAndView.addObject("next", page + 2);
                modelAndView.addObject("prev", page);
                modelAndView.addObject("last", totalPage);
                modelAndView.addObject("directory", "Risk");
                modelAndView.addObject("registers",templatesRisk.size());
            }
            modelAndView.setViewName("/ifrs/Risk");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/Risk")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/Risk");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logInsercion_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            //ArrayList<String[]> list=riskService.saveFileBD(fileContent,headerKey);
            ArrayList<String[]> list=marketRiskService.saveFileBD(fileContent,headerKey);
            String[] part=list.get(0);
            if(part[2].equals("true"))
            {
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                //listReport.exportLog(response,list);
            }
            else if(part[2].equals("falseFormat"))
            {
                int rowTemp=(Integer.parseInt(part[0])+1);
                part[0]=String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep-1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else
            {
                int rowTemp=(Integer.parseInt(part[0])+1);
                part[0]=String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep0");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                return  modelAndView;
            }

        }catch(Exception e){
            e.printStackTrace();
            return  modelAndView;
        }
        return  modelAndView;

    }

    @GetMapping(value = "/ifrs/removeRisk/{id}")
    @ResponseBody
    public ModelAndView removeRisk(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Risk> toRemove = riskService.findById(id);
        riskService.removeThird(toRemove.get().getCMCO_COD_CCONTR());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/Risk");
        return  modelAndView;
    }
}
