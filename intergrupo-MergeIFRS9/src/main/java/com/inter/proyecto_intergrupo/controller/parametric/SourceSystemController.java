package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.TipoDocumento;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.EventType;
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

import java.util.Collections;
import java.util.Comparator;
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

    private List<String> listColumns = List.of("Nombre","Sigla","Aplica Festivo","Código País","País","Estado");

    @GetMapping(value = "/parametric/sourceSystem")
    public ModelAndView showSourceSystem(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar = userService.validateEndpointModificar(user.getId(), "Ver Sistemas Fuentes");
        if (userService.validateEndpoint(user.getId(), "Ver Sistemas Fuentes")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            List<SourceSystem> sfs = sourceSystemService.findAll();
            Collections.sort(sfs, Comparator.comparingInt(SourceSystem::getId));

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
            modelAndView.addObject("directory", "sourceSystem");
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
        Country paisSelected = countryService.findCountryByName(pais).get(0);
        sf.setPais(paisSelected);
        sourceSystemService.modificarSourceSystem(sf);
        modelAndView.addObject("resp", "Modify1");
        return modelAndView;
    }



    @PostMapping(value = "/parametric/deleteSourceSystem/{id}")
    public ModelAndView deleteSourceSystem(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/sourceSystem");
        try {
            SourceSystem sf = sourceSystemService.findSourceSystemById(id);
            sf.setActivo(false);
            sourceSystemService.modificarSourceSystem(sf);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }

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
            modelAndView.addObject("resp", "Add1");
            Country newPais = countryService.findCountryByName(pais).get(0);
            sf.setPais(newPais);
            sourceSystemService.modificarSourceSystem(sf);
        }
        return modelAndView;

    }
    @GetMapping(value = "/parametric/searchSourceSystem")
    @ResponseBody
    public ModelAndView searchSourceSystem(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<SourceSystem> list;
        if(params==null)
            list=sourceSystemService.findAll();
        else
            list=sourceSystemService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<SourceSystem> pageTypeEntity = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageTypeEntity.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allSFS",pageTypeEntity.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchSourceSystem");
        modelAndView.addObject("registers",list.size());
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Sistemas Fuentes");

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("p_modificar", p_modificar);

        modelAndView.setViewName("parametric/sourceSystem");
        return modelAndView;
    }
}
