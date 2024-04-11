package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Deaccount;
import com.inter.proyecto_intergrupo.model.ifrs9.ManualAdjustments;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.*;
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
public class ManualAdjustmentsController {

    private static final int PAGINATIONCOUNT=12;

    @Autowired
    private ManualAdjustmentsService manualService;

    @Autowired
    private UserService userService;

    @GetMapping(value="/ifrs/manualAdjustments")
    public ModelAndView showAdjustments(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Diferencias Conciliación automática")) {

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

            List<ManualAdjustments> manuals = manualService.getManuals(todayString);

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), manuals.size());
            Page<ManualAdjustments> pageManuals = new PageImpl<>(manuals.subList(start, end), pageRequest, manuals.size());

            int totalPage = pageManuals.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            if (manuals != null) {
                modelAndView.addObject("allManuals", pageManuals.getContent());
                modelAndView.addObject("current", page + 1);
                modelAndView.addObject("next", page + 2);
                modelAndView.addObject("prev", page);
                modelAndView.addObject("last", totalPage);
                modelAndView.addObject("directory", "manualAdjustments");
                modelAndView.addObject("registers",manuals.size());
            }

            modelAndView.addObject("period", todayString);
            modelAndView.setViewName("/ifrs/manualAdjustments");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @RequestMapping(value = "/ifrs/manualAdjustments/download")
    @ResponseBody
    public void exportAll(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=AjustesManuales_" + currentDateTime + ".xlsx";
            String todayString="";
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
            response.setHeader(headerKey, headerValue);
            List<ManualAdjustments> manualList = manualService.getManualsDate(todayString);
            ManualAdjustmentsListReport listReport = new ManualAdjustmentsListReport(manualList);
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @PostMapping(value="/ifrs/manualAdjustments/upload")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "period") String period, @RequestParam Map<String, Object> params, ModelMap model) throws ParseException {
        response.setContentType("application/octet-stream");
        model.addAttribute("period",period);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/manualAdjustments");
        modelAndView.addObject("period",params.get("period").toString());

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

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
                todayString = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        } else {
            todayString = params.get("period").toString();
        }

        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        try{
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = manualService.saveFileBD(fileContent, user, todayString);
            String[] part = list.get(0);

            if(part[0] != null && part[0].equals("Fallo Estructura") ){
                modelAndView.addObject("resp","false");
                modelAndView.addObject("period", params.get("period").toString());
            } else if(part[0] != null && part[0].equals("duplicados") ){
                modelAndView.addObject("resp","segments-error");
                modelAndView.addObject("period", params.get("period").toString());
            } else {
                if(part[2].equals("true")){
                    modelAndView.addObject("resp", "manualsCorrect");
                    modelAndView.addObject("period", params.get("period").toString());
                }else{
                    SegmentsLogListReport segmentsLogListReport = new SegmentsLogListReport(list);
                    segmentsLogListReport.exportLog(response);
                }
            }
        } catch (ServletException | IOException e) {
            e.printStackTrace();
            modelAndView.addObject("resp","false");
            modelAndView.addObject("period", params.get("period").toString());
        }

        return modelAndView;
    }


    @GetMapping(value = "/ifrs/clearManualAdjustments")
    public ModelAndView clearManuals(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params){

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
        manualService.clearManuals(user, todayString);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/manualAdjustments");
        return  modelAndView;
    }

}
