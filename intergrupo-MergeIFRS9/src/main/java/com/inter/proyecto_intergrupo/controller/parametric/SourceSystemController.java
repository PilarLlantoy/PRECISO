package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.TipoDocumento;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.SourceSystem;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.CountryService;
import com.inter.proyecto_intergrupo.service.parametricServices.SourceSystemService;
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
public class SourceSystemController {
    private static final int PAGINATIONCOUNT = 12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private SourceSystemService sourceSystemService;

    private List<String> listColumns = List.of("Código País", "Nombre País");

    @GetMapping(value = "/parametric/sourceSystem")
    public ModelAndView showSourceSystem(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar = userService.validateEndpointModificar(user.getId(), "Ver Países");
        if (userService.validateEndpoint(user.getId(), "Ver Países")) { //CAMBIAR A VER Sistema fuente

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            List<SourceSystem> sfs = sourceSystemService.findAllActive();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), sfs.size());
            Page<SourceSystem> pageSF = new PageImpl<>(sfs.subList(start, end), pageRequest, sfs.size());

            int totalPage = pageSF.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allSFS", pageSF.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "country");
            modelAndView.addObject("registers", sfs.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/sourceSystem");
        } else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }


    @GetMapping(value = "/parametric/modifySourceSystem/{id}")
    @ResponseBody
    public ModelAndView modifySourceSystem(@PathVariable int id) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        List<Country> allPaises = countryService.findAll();
        modelAndView.addObject("paises", allPaises);
        SourceSystem toModify = sourceSystemService.findSourceSystemById(id);
        modelAndView.addObject("sf", toModify);
        modelAndView.addObject("sfId", toModify.getId());
        modelAndView.setViewName("parametric/modifySourceSystem");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifySourceSystem")
    public ModelAndView updateSourceSystem(@ModelAttribute SourceSystem sf,
                                           @RequestParam(name = "selectedPais") String pais) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/sourceSystem");
        Country paisSelected = countryService.findCountryByName(pais);
        sf.setPais(paisSelected);
        sourceSystemService.modificarSourceSystem(sf);
        return modelAndView;
    }



    @PostMapping(value = "/parametric/deleteSourceSystem/{id}")
    public ModelAndView deleteSourceSystem(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/sourceSystem");
        try {
            SourceSystem sf = sourceSystemService.findSourceSystemById(id);
            sf.setEstado(false);
            sourceSystemService.modificarSourceSystem(sf);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }



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


    @GetMapping(value = "/parametric/createSourceSystem")
    public ModelAndView showCreateSourceSystem(){
        ModelAndView modelAndView = new ModelAndView();
        SourceSystem sf = new SourceSystem();
        Country pais = new Country();
        sf.setPais(pais);
        modelAndView.addObject("sf",sf);

        List<Country> paises = countryService.findAllActiveCountries();
        modelAndView.addObject("paises",paises);
        modelAndView.setViewName("/parametric/createSourceSystem");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createSourceSystem")
    public ModelAndView createSourceSystem(@ModelAttribute SourceSystem sf,
            @RequestParam(name = "selectedPais") String pais,
            BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/sourceSystem");
        SourceSystem psfExists = sourceSystemService.findSourceSystemById(sf.getId());
        if(psfExists != null){
            bindingResult
                    .rejectValue("sf", "error.sf",
                            "El sf ya se ha registrado");
        }
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createSourceSystem");
        }else{
            Country newPais = countryService.findCountryByName(pais);
            sf.setPais(newPais);
            sourceSystemService.modificarSourceSystem(sf);
        }
        return modelAndView;

    }



}
