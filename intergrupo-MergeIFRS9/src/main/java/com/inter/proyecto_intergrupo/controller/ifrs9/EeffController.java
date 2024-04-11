package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Desconnv15;
import com.inter.proyecto_intergrupo.model.ifrs9.Eeff;
import com.inter.proyecto_intergrupo.model.ifrs9.Perimeter;
import com.inter.proyecto_intergrupo.service.ifrs9Services.EeffListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.EeffService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;

import com.inter.proyecto_intergrupo.service.ifrs9Services.PerimeterListReport;
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
public class EeffController {

    private static final int PAGINATIONCOUNT=12;

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private EeffService eeffService;

    private List<String> listColumns=List.of("Código Sociedad Informante","Descripción","Denominación de la cuenta",
            "Tipo de cuenta", "Cuenta", "Soc. IC","Descripción IC","Desgloses","Divisa España",
            "Saldo","Intergrupo","Entrada");

    @GetMapping(value="/ifrs/Eeff")
    public ModelAndView showEeff(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver EEFF Neocon")) {
        String tipo="";
        String todayString;
        String Id;
        String Vf;
        int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
        if (params.get("vId") == null) {
            Id = "";
        } else {
            Id = params.get("vId").toString();
        }
        if (params.get("vFilter") == null) {
            Vf = "";
        } else {
            Vf = params.get("vFilter").toString();
        }

        if (params.get("period") == null | Objects.equals(params.get("period").toString(), "")) {
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

        if(params.get("type")==null  || params.get("type").toString()=="") {
            tipo="Local";
        }
        else {
            tipo=params.get("type").toString();
        }

        List<Eeff> eeff;
        if (Objects.equals(Id, "") || Objects.equals(Vf, "")) {
            eeff = eeffService.findAllEeff(todayString,tipo);
        } else {
            eeff = eeffService.findByFilter(Id, Vf, todayString, tipo);
        }

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), eeff.size());
        Page<Eeff> pageEEFF = new PageImpl<>(eeff.subList(start, end), pageRequest, eeff.size());

        int totalPage=pageEEFF.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        if (eeff != null){
            modelAndView.addObject("vId", Id);
            modelAndView.addObject("vFilter", Vf);
            modelAndView.addObject("alleeff",pageEEFF.getContent());
            modelAndView.addObject("columns",listColumns);
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("period",todayString);
            modelAndView.addObject("type",tipo);
            modelAndView.addObject("registers",eeff.size());
            modelAndView.addObject("directory", "Eeff");
        }

        modelAndView.setViewName("/ifrs/Eeff");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/Eeff")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/Eeff");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("vFilter",params.get("period").toString());
        modelAndView.addObject("type",params.get("type").toString());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logInsert_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            String month=params.get("period").toString();
            String tipo=params.get("type").toString();
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = eeffService.saveFileBD(fileContent,month,tipo);
            String[] part=list.get(0);
            if(part[2].equals("true"))
            {
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
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
                //int rowTemp=(Integer.parseInt(part[0])+1);
                //part[0]=String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep0");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                return  modelAndView;
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        finally {
            return  modelAndView;
        }
    }

    @GetMapping(value = "/ifrs/Eeff/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=EEFF_" + params.get("period").toString() + "_" + params.get("type").toString() + "_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Eeff> eeffList = new ArrayList<Eeff>();
        eeffList = eeffService.findAllEeff(params.get("period").toString(),params.get("type").toString());
        EeffListReport listReport = new EeffListReport(eeffList);
        listReport.export(response);
    }
}

