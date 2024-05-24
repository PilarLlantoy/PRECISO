package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ConciliationService;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ConciliationController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ConciliationService conciliationService;

    @GetMapping(value="/parametric/conciliation")
    public ModelAndView showConciliation(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        if(userService.validateEndpoint(user.getId(),"Ver Países")) { //CAMBIAR A VER Conciliaciones

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<Conciliation> conciliations = conciliationService.findAllActive();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), conciliations.size());
            Page<Conciliation> pageConciliation = new PageImpl<>(conciliations.subList(start, end), pageRequest, conciliations.size());

            int totalPage=pageConciliation.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allCountry",pageConciliation.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","country");
            modelAndView.addObject("registers",conciliations.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/conciliation");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/createConciliation")
    public ModelAndView showCreateConciliation(){
        ModelAndView modelAndView = new ModelAndView();
        Conciliation concil = new Conciliation();
        modelAndView.addObject("concil",concil);
        modelAndView.setViewName("/parametric/createConciliation");
        return modelAndView;
    }

    /*
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

*/

    /*
            @PostMapping(value = "/parametric/modifyCountry")
            @ResponseBody
            public ModelAndView updateCountry(@ModelAttribute Country country,@RequestParam String idOld){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User user = userService.findUserByUserName(auth.getName());
                ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
                try {
                    Country searchCountry = countryService.findCountryById(country.getId()+"");
                    if (searchCountry==null||idOld.equals(country.getId()))
                    {
                        countryService.modifyCountry(country, idOld,user);
                        modelAndView.addObject("resp", "Modify1");
                    }
                    else
                    {
                        modelAndView.addObject("resp", "Modify0");
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                    modelAndView.addObject("resp", "UpdateCascade-1");
                }
                return  modelAndView;

            }

            @GetMapping(value = "/parametric/validateIdCountry")
            @ResponseBody
            public String validateIdCountry(@RequestParam String idNew,@RequestParam String idT){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String result = "invalid";
                if(countryService.findCountryById(idNew)==null||idNew.equals(idT))
                    result="valid";
                return  result;
            }

            @GetMapping(value = "/parametric/removeCountry/{id}")
            @ResponseBody
            public boolean removeCountry(@PathVariable String id){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User user = userService.findUserByUserName(auth.getName());
                ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
                boolean response=false;
                try {
                    Country toRemove = countryService.findCountryById(id);
                    countryService.removeCountry(toRemove.getId()+"",user);
                    response=true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return  response;

            }

            @GetMapping(value = "/parametric/clearCountry")
            @ResponseBody
            public boolean clearCountry(){
                boolean response=false;
                ModelAndView modelAndView = new ModelAndView("redirect:/parametric/country");
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User user = userService.findUserByUserName(auth.getName());
                try{
                    countryService.clearCountry(user);
                    response=true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return  response;
            }

            @GetMapping(value = "/parametric/country/download")
            @ResponseBody
            public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
                response.setContentType("application/octet-stream");
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String currentDateTime = dateFormatter.format(new Date());

                String headerKey = "Content-Disposition";
                String headerValue = "attachment; filename=País_" + currentDateTime + ".xlsx";
                response.setHeader(headerKey, headerValue);
                List<Country> countryList= new ArrayList<Country>();
                if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
                    countryList = countryService.findAll();
                }
                else{
                    countryList = countryService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
                }
                CountryListReport listReport = new CountryListReport(countryList);
                listReport.export(response);
            }

            @GetMapping(value = "/parametric/searchCountry")
            @ResponseBody
            public ModelAndView searchCountry(@RequestParam Map<String, Object> params) {
                ModelAndView modelAndView = new ModelAndView();
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
                PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
                List<Country> list=countryService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

                int start = (int)pageRequest.getOffset();
                int end = Math.min((start + pageRequest.getPageSize()), list.size());
                Page<Country> pageCountry = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

                int totalPage=pageCountry.getTotalPages();
                if(totalPage>0){
                    List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                    modelAndView.addObject("pages",pages);
                }
                modelAndView.addObject("allCountry",pageCountry.getContent());
                modelAndView.addObject("current",page+1);
                modelAndView.addObject("next",page+2);
                modelAndView.addObject("prev",page);
                modelAndView.addObject("vId",params.get("vId").toString());
                modelAndView.addObject("last",totalPage);
                modelAndView.addObject("vFilter",params.get("vFilter").toString());
                modelAndView.addObject("columns",listColumns);
                modelAndView.addObject("directory","searchCountry");
                modelAndView.addObject("registers",list.size());

                User user = userService.findUserByUserName(auth.getName());
                modelAndView.addObject("userName",user.getPrimerNombre());
                modelAndView.addObject("userEmail",user.getCorreo());
                modelAndView.setViewName("parametric/country");
                return modelAndView;
            }
        */

    /*


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
            countryService.modificarCountry(pais);
        }
        return modelAndView;

    }

    */

}
