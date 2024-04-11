package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.IncurredLoss;
import com.inter.proyecto_intergrupo.model.ifrs9.ValQueryEEFF;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;

import com.inter.proyecto_intergrupo.service.ifrs9Services.IncurredLossListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.IncurredLossService;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdListReport;
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
public class IncurredLossController {
    private static final int PAGINATIONCOUNT=12;

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private IncurredLossService incurredLossService;

    private List<String> listColumns=List.of("Cuenta","Centro","Contrato","Segmento","Stage","Indicador contrato","Saldo","Código consolidación");

    @GetMapping(value="/ifrs/incurredloss")
    public ModelAndView showincurredloss(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Perdida incurrida")) {
            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<Object[]> lostList = incurredLossService.getLostResume();

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), lostList.size());
            Page<Object[]> pageLost = new PageImpl<>(lostList.subList(start, end), pageRequest, lostList.size());

            int totalPage=pageLost.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }

            modelAndView.addObject("allLost",pageLost.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("directory","incurredloss");
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("registers",lostList.size());

            modelAndView.addObject("columns", listColumns);
            modelAndView.setViewName("/ifrs/incurredloss");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }


    @PostMapping(value="/ifrs/incurredloss")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/incurredloss");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());


        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logInsert_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = incurredLossService.loadQueryDatabase(fileContent);
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

    @GetMapping(value = "/ifrs/incurredloss/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {

        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=PerdidaIncurrida_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> lossList = incurredLossService.getLostResume();
        IncurredLossListReport listReport = new IncurredLossListReport(null,lossList);
        listReport.exportResume(response);
    }

    @GetMapping(value = "/ifrs/incurredloss/downloadDinamic")
    @ResponseBody
    public void exportToExcelDinamic(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());

            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=PerdidaIncurrida_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<IncurredLoss> lossList = incurredLossService.findAllDinamic(params.entrySet());

            IncurredLossListReport listReport = new IncurredLossListReport(lossList,null);
            listReport.export(response);
        }
        catch(Exception e)
        {
            List<IncurredLoss> lossList = new ArrayList<IncurredLoss>();
            IncurredLossListReport listReport = new IncurredLossListReport(lossList,null);
            listReport.export(response);
        }
    }

    @GetMapping(value = "/ifrs/searchIncurredloss")
    @ResponseBody
    public ModelAndView searchIncurredloss(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<IncurredLoss> list=incurredLossService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<IncurredLoss> pageThird = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageThird.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allLost",pageThird.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchIncurredloss");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/incurredloss");
        return modelAndView;
    }
}
