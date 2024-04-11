package com.inter.proyecto_intergrupo.controller.Ifrs9Parametrics;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.GenericsParametric;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.GenericsParametricListReport;
import com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices.GenericsParametricService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class GenericsParametricController {

    private static final int PAGINATIONCOUNT=12;

    @Autowired
    GenericsParametricService genericsParametricService;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/ifrs9Parametrics/genericsParametric")
    public ModelAndView showGenerics(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Genéricas")) {
        int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
        ArrayList<GenericsParametric> genericsReport = genericsParametricService.getGenerics();

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), genericsReport.size());
        Page<GenericsParametric> pageGenerics = new PageImpl<>(genericsReport.subList(start, end), pageRequest, genericsReport.size());

        int totalPage = pageGenerics.getTotalPages();
        if (totalPage > 0) {
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages", pages);
        }

        modelAndView.addObject("allGenerics", pageGenerics.getContent());
        modelAndView.addObject("current", page + 1);
        modelAndView.addObject("next", page + 2);
        modelAndView.addObject("prev", page);
        modelAndView.addObject("last", totalPage);
        modelAndView.addObject("directory", "genericsParametric");
        modelAndView.addObject("registers",genericsReport.size());

        modelAndView.setViewName("/Ifrs9Parametrics/genericsParametric");
        }
            else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/ifrs9Parametrics/uploadGenerics")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9Parametrics/genericsParametric");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Log_Genéricas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();

            ArrayList<String[]> result = genericsParametricService.saveFileDB(fileContent);
            String[] temp = result.get(0);

            if(temp[2].equals("true")){
                modelAndView.addObject("resp","true");
            } else if(temp[2].equals("Documento Invalido")){
                modelAndView.addObject("resp","invalid");
            }
            else{
                GenericsParametricListReport genericsParametricListReport = new GenericsParametricListReport(null,result);
                genericsParametricListReport.exportLog(response);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return modelAndView;
    }

    @GetMapping(value = "/ifrs9Parametrics/genericsParametric/download")
    @ResponseBody
    public void exportToExcelReport(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Paramétrica Genéricas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        ArrayList<GenericsParametric> result = genericsParametricService.getGenerics();
        GenericsParametricListReport genericsParametricListReport = new GenericsParametricListReport(result,null);
        genericsParametricListReport.export(response);
    }



}
