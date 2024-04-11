package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.CountryService;
import com.inter.proyecto_intergrupo.service.parametricServices.GarantBankService;
import com.inter.proyecto_intergrupo.service.parametricServices.GarantBankListReport;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class GarantBankController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private GarantBankService garantBankService;

    @Autowired
    private CountryService countryService;

    private List<String> listColumns=List.of("NIT", "Nombre Banco Real", "Nombre Similar","País");

    @GetMapping(value="/parametric/garantBank")
    public ModelAndView showGarantBanks(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Banco Garante")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<GarantBank> pageGarantBank = garantBankService.getAll(pageRequest);
            int totalPage = pageGarantBank.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allGarantBanks", pageGarantBank.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "garantBank");
            modelAndView.addObject("registers",pageGarantBank.getTotalElements());

            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/garantBank");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/garantBank")
    public void uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/garantBank");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<GarantBank> GarantBankList = garantBankService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            GarantBankListReport listReport = new GarantBankListReport(GarantBankList);
            ArrayList<String[]> list=garantBankService.saveFileBD(fileContent,user);
            String[] part=list.get(0);

            modelAndView.addObject("resp", "AddRep1");
            modelAndView.addObject("row", part[0]);
            modelAndView.addObject("colum", part[1]);
            listReport.exportLog(response,list);
            response.sendRedirect("/parametric/garantBank");


        }catch(Exception e){
            //return  modelAndView;
        }
        //return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyGarantBank/{id}")
    @ResponseBody
    public ModelAndView modifyGarantBanks(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        GarantBank toModify = garantBankService.findGarantBankByNombreSmiliar(id);
        modelAndView.addObject("garantBankModify",toModify);
        if(toModify!=null && toModify.getPais()!=null)
            modelAndView.addObject("garantC",countryService.findCountryById(toModify.getPais()).getNombre());
        List <Country> allCountry = countryService.findAll();
        modelAndView.addObject("paises",allCountry);
        modelAndView.addObject("nitId",toModify.getNombreSimilar());
        modelAndView.setViewName("parametric/modifyGarantBank");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyGarantBank")
    @ResponseBody
    public ModelAndView updateGarantBank(@ModelAttribute GarantBank GarantBank,@Param(value = "id") String id,@RequestParam("grupoSelect") String pais){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/garantBank");
        try {
            GarantBank searchGarantBank = garantBankService.findGarantBankByNombreSmiliar(GarantBank.getNombreSimilar());
            if (searchGarantBank==null||id.equals(GarantBank.getNombreSimilar()))
            {
                garantBankService.modifyGarantBank(GarantBank, id,user,pais);
                modelAndView.addObject("resp", "Modify1");
            }
            else
            {
                modelAndView.addObject("resp", "Modify0");
            }
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value = "/parametric/validatePalabra")
    @ResponseBody
    public String validateNitGarantBanks(@RequestParam String idNew,@RequestParam String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(garantBankService.findGarantBankByNombreSmiliar(idNew)==null||idNew.equals(id))
            result="valid";
        return  result;
    }

    @GetMapping(value="/parametric/addGarantBank")
    public ModelAndView showAddGarantBank(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        GarantBank GarantBank = new GarantBank();
        modelAndView.addObject("GarantBank", GarantBank);
        modelAndView.setViewName("parametric/addGarantBank");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addGarantBank/add")
    public ModelAndView addGarantBank(@ModelAttribute GarantBank garantBank){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/addGarantBank");

        boolean result = garantBankService.insertGarantBank(garantBank);

        if(result){
            modelAndView.addObject("resp", "true");
        }else {
            modelAndView.addObject("resp","false");
        }

        return modelAndView;
    }

    @GetMapping(value = "/parametric/removeGarantBanks/{id}")
    @ResponseBody
    public ModelAndView removeGarantBanks(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());GarantBank toRemove = garantBankService.findGarantBankByNombreSmiliar(id);
        garantBankService.removeGarantBank(toRemove.getNombreSimilar(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/garantBank");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearGarantBank")
    public ModelAndView clearGarantBanks(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        garantBankService.clearGarantBank(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/garantBank");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/garantBank/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=BancoGarante_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<GarantBank> garantBankList= new ArrayList<GarantBank>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            garantBankList = garantBankService.findAll();
        }
        else{
            garantBankList = garantBankService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        GarantBankListReport listReport = new GarantBankListReport(garantBankList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchGarantBank")
    @ResponseBody
    public ModelAndView searchGarantBanks(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<GarantBank> list=garantBankService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<GarantBank> pageGarantBank = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageGarantBank.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allGarantBanks",pageGarantBank.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchGarantBank");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/garantBank");
        return modelAndView;
    }
}
