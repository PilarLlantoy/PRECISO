package com.inter.proyecto_intergrupo.controller.briefcase;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.PduIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaPduIcrv;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.briefcaseServices.PduIcrvListReport;
import com.inter.proyecto_intergrupo.service.briefcaseServices.PduIcrvService;
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
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class PduIcrvController {

    @Autowired
    PduIcrvService pduIcrvService;

    @Autowired
    private UserService userService;

    private static final int PAGINATIONCOUNT=12;
    private final List<String> listColumns = List.of("NOISIN", "GRUPO","ENTIDAD");


    @GetMapping(value = "/briefcase/pduicrv")
    public ModelAndView showPriceView(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver PDU ICRV")){
            int page = params.get("page") != null ? (Integer.parseInt(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString = "";
            String Id = "";
            String Vf = "";

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

            List<PduIcrv> list;

            if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
                list = pduIcrvService.findAllPdu(todayString);
                modelAndView.addObject("filterExport", "Original");
            } else {
                list = pduIcrvService.findByFilter(Id,Vf,todayString);
                modelAndView.addObject("filterExport", "Filtrado");
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<PduIcrv> pageList = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageList.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("vId", Id);
            modelAndView.addObject("vFilter", Vf);
            modelAndView.addObject("pduicrvList", pageList.getContent());
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("directory", "pduicrv");
            modelAndView.addObject("registers",list.size());
            modelAndView.setViewName("/briefcase/pduicrv");

        }
        else
        {
            modelAndView.addObject("pduicrv","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value = "/briefcase/pduicrv")
    public ModelAndView loadPlantilla(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/pduicrv");
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
            ArrayList<String[]> list = pduIcrvService.saveFileBDPlantilla(fileContent,user,params.get("period").toString());
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                PduIcrvListReport pduIcrvListReport = new PduIcrvListReport(list,null,null);
                pduIcrvListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/briefcase/pduicrv/download")
    @ResponseBody
    public void downloadPrecio(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        String Id = "";
        String Vf = "";

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
        List<PduIcrv> list;
        if (Objects.equals(Id, "") | Objects.equals(Vf, "")) {
            list = pduIcrvService.findAllPdu(params.get("period").toString());
        } else {
            list = pduIcrvService.findByFilter(Id,Vf,params.get("period").toString());
        }
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= PduICRV_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        PduIcrvListReport listReport = new PduIcrvListReport(null, list,null);
        listReport.export(response);
    }

    @GetMapping(value = "/briefcase/pduicrv/downloadPlantilla")
    @ResponseBody
    public void downloadPrecioPlantilla(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= PlantillaPduICRV_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<PlantillaPduIcrv> list = pduIcrvService.findAllPlantilla();
        PduIcrvListReport listReport = new PduIcrvListReport(null,null, list);
        listReport.exportPlantilla(response);
    }

    @GetMapping(value = "/briefcase/modifyPduicrv/{id}")
    @ResponseBody
    public ModelAndView modifyPduicrv(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        PduIcrv toModify = pduIcrvService.findByIdPdu(idTemp);
        modelAndView.addObject("pduicrvModify",toModify);
        modelAndView.setViewName("briefcase/modifyPduicrv");
        return modelAndView;
    }

    @PostMapping(value = "/briefcase/modifyPduicrv")
    @ResponseBody
    public ModelAndView updatePduicrv(@ModelAttribute PduIcrv pduicrv){
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/pduicrv");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            pduIcrvService.modifyPdu(pduicrv, user);
            pduIcrvService.completeDataTable(pduicrv.getPeriodo());
            modelAndView.addObject("period", pduicrv.getPeriodo());
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value = "/briefcase/removePduicrv/{id}")
    @ResponseBody
    public ModelAndView removePduicrv(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        pduIcrvService.clearPdu(user,id);
        ModelAndView modelAndView = new ModelAndView("redirect:/briefcase/pduicrv?period="+id);
        return  modelAndView;
    }
}
