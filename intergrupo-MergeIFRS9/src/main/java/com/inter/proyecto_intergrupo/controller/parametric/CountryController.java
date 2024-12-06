package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.CountryListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.CountryService;
import com.inter.proyecto_intergrupo.service.parametricServices.EventTypeListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.GeneralListReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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
public class CountryController {
    private static final int PAGINATIONCOUNT=12;
    private List<String> listColumns=List.of("Nombre", "Sigla", "Estado");
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private CountryService countryService;

    @GetMapping(value="/parametric/country")
    public ModelAndView showCountry(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        if(userService.validateEndpoint(user.getId(),"Ver Países")) {

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<Country> countries = countryService.findAll();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), countries.size());
            Page<Country> pageCountry = new PageImpl<>(countries.subList(start, end), pageRequest, countries.size());

            int totalPage=pageCountry.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allCountry",pageCountry.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("columns",listColumns);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","country");
            modelAndView.addObject("registers",countries.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/country");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/country")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
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
            ArrayList<String[]> list = countryService.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                GeneralListReport generalListReport = new GeneralListReport(list);
                generalListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifyCountry/{id}")
    @ResponseBody
    public ModelAndView modifyCountry(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Country toModify = countryService.findCountryById(id);
        modelAndView.addObject("countryModify",toModify);
        modelAndView.addObject("paisId",toModify.getId());
        modelAndView.setViewName("parametric/modifyCountry");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyCountry")
    public ModelAndView updateCountry(@ModelAttribute Country pais){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
        countryService.modificarCountry(pais);
        modelAndView.addObject("resp", "Modify1");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/deleteCountry/{id}")
    public ModelAndView deleteCountry(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
        try {
            Country pais = countryService.findCountryById(id);
            pais.setEstado(false);
            countryService.modificarCountry(pais);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }


    @GetMapping(value = "/parametric/createCountry")
    public ModelAndView showCreateCountry(){
        ModelAndView modelAndView = new ModelAndView();
        Country pais = new Country();
        modelAndView.addObject("pais",pais);
        modelAndView.setViewName("/parametric/createCountry");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createCountry")
    public ModelAndView createCountry(@ModelAttribute Country pais, BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
        Country paisExists = countryService.findCountryById(pais.getId());
        if(paisExists != null){
            bindingResult
                    .rejectValue("pais", "error.pais",
                            "El pais ya se ha registrado");
        }
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createCountry");
        }else{
            modelAndView.addObject("resp", "Add1");
            countryService.modificarCountry(pais);
        }
        return modelAndView;

    }

    @GetMapping(value = "/parametric/searchCountry")
    @ResponseBody
    public ModelAndView searchCountry(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Country> list;
        if(params==null)
            list=countryService.findAll();
        else
            list=countryService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Country> pageTypeEntity = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageTypeEntity.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allCountry",pageTypeEntity.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchCountry");
        modelAndView.addObject("registers",list.size());
        modelAndView.addObject("filterExport","Filtrado");
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("p_modificar", p_modificar);

        modelAndView.setViewName("parametric/country");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/country/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Paises_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Country> list= new ArrayList<Country>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            list = countryService.findAll();
        }
        else{
            list = countryService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        CountryListReport listReport = new CountryListReport(list);
        listReport.export(response);
    }
}
