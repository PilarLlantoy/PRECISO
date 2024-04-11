package com.inter.proyecto_intergrupo.controller.dataquality;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.dataquality.PointRulesDQ;
import com.inter.proyecto_intergrupo.model.dataquality.RulesDQ;
import com.inter.proyecto_intergrupo.model.parametric.Subsidiaries;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.bankServices.TemplateBankListReport;
import com.inter.proyecto_intergrupo.service.dataqualityServices.PointRulesDQListReport;
import com.inter.proyecto_intergrupo.service.dataqualityServices.RulesDQListReport;
import com.inter.proyecto_intergrupo.service.dataqualityServices.RulesDQService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.SegmentsLogListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.CurrencyService;
import com.inter.proyecto_intergrupo.service.parametricServices.SubsidiariesListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.SubsidiariesService;
import com.inter.proyecto_intergrupo.service.parametricServices.YntpSocietyService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
public class RulesDQController {

    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private RulesDQService rulesDQService;

    @GetMapping(value="/dataquality/rules")
    public ModelAndView showRulesDQ(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Reglas DQ")) {

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

            List<String[]> listData1 = rulesDQService.getAllData1(todayString);
            List<String[]> listData2 = rulesDQService.getAllData2(todayString);
            List<String[]> listData3 = rulesDQService.getAllDataFallidos(todayString);
            List<String[]> listData4 = rulesDQService.getAllDataCumplidos(todayString);

            List<Object[]> folders=rulesDQService.getFolders();
            int validateData = rulesDQService.getCountAll();

            modelAndView.addObject("folders",folders);
            modelAndView.addObject("allRulesData1", listData1);
            modelAndView.addObject("allRulesData2", listData2);
            modelAndView.addObject("allRulesData3", listData3);
            modelAndView.addObject("allRulesData4", listData4);
            modelAndView.addObject("validateData", validateData);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("directory", "rules");

            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("dataquality/rules");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/dataquality/rules")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/dataquality/rules");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Log_Cargue_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = rulesDQService.saveFileBD(fileContent);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                rulesDQService.loadAudit(user, "Cargue exitoso plantilla Reglas DQ");
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                rulesDQService.loadAudit(user, "Cargue fallido plantilla Reglas DQ");
                RulesDQListReport rulesDQListReport = new RulesDQListReport(list,null);
                rulesDQListReport.exportLog(response);
            }

            modelAndView.addObject("period",params.get("period").toString());
            modelAndView.addObject("vFilter",params.get("period").toString());

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/dataquality/rules/downloadBase")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ReglasDQ_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<RulesDQ> templateList = rulesDQService.getAllList();
        RulesDQListReport listReport = new RulesDQListReport(null,templateList);
        listReport.export(response);
    }

    @GetMapping(value = "/dataquality/rules/downloadIns")
    @ResponseBody
    public void exportTxtIns(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment;  filename=DataQuality_Fallido_" + currentDateTime + ".xlsx");

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ReglasDQ_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<PointRulesDQ>templateList = rulesDQService.getAllListPointIns(params.get("period").toString());
        PointRulesDQListReport listReport = new PointRulesDQListReport(null,templateList);
        listReport.export(response);

    }

    @GetMapping(value = "/dataquality/rules/downloadTxt")
    @ResponseBody
    public void exportTxt(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment;  filename=DataQuality_" + currentDateTime + ".txt");

        List<PointRulesDQ>data = rulesDQService.getAllListPoint(params.get("period").toString());

        CsvService.downloadTxtDQ(response.getWriter(), data);

    }

    @GetMapping(value = "/dataquality/rules/processRules")
    public ModelAndView processRulesDQ(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/dataquality/rules");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            modelAndView.addObject("period", params.get("period").toString());
            if(rulesDQService.getCountAll()!=0)
            {
                rulesDQService.processRules(params.get("period").toString(),params.get("folder1").toString());
                rulesDQService.loadAudit(user, "Procesamiento exitoso Reglas DQ");
                modelAndView.addObject("resp", "ExecRules1");
            }
            else
            {
                rulesDQService.loadAudit(user, "Procesamiento Reglas DQ con plantilla vacia");
                modelAndView.addObject("resp", "ExecRules2");
            }

        }
        catch (Exception e){
            e.printStackTrace();
            rulesDQService.loadAudit(user, "Procesamiento fallido Reglas DQ");
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }
}
